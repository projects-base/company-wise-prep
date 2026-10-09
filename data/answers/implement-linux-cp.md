**Short answer:** Open the source read-only, `fstat` it to get size and permissions, create the destination with `O_WRONLY | O_CREAT | O_TRUNC` and the source's mode, then loop `read` into a buffer and `write` it out until `read` returns 0, handling short writes and `EINTR`. Close both and check the errors from `close` too. Faster versions let the kernel do the copy (`copy_file_range`, `sendfile`) so data never enters user space. Then handle the edge cases: directories (`-r`), same file, symlinks, sparse files, and preserving metadata (`-p`).

## Explanation

**Core loop.** `read` may return fewer bytes than asked, and `write` may write fewer than given (especially on pipes and sockets, or when interrupted by a signal), so you loop on both. Buffer size matters: 4 KB is many syscalls; 64 KB-1 MB amortises syscall cost (GNU `cp` uses at least 128 KiB).

**What the OS does underneath** (the "deep OS" follow-ups):
- `open` walks the path through the VFS, checks permissions, creates an entry in the process's file descriptor table pointing to an open-file description (with the offset).
- `read` on a regular file copies from the **page cache**; on a miss the filesystem issues block I/O and the process sleeps until it completes. Readahead fetches ahead on sequential access.
- `write` copies into the page cache and marks pages dirty; it returns before data reaches disk. Writeback threads flush later. Only `fsync` guarantees durability.
- Each `read`+`write` is two user/kernel copies. `copy_file_range` (Linux 4.5+) and `sendfile` avoid the user copy; on filesystems such as Btrfs and XFS (with reflink), `copy_file_range` or the `FICLONE` ioctl can share extents instead of copying (copy-on-write clone).
- `mmap` the source and `write` from the mapping is another option, but page faults and TLB costs often make it no faster.

**Edge cases a good answer mentions**
- Source and destination are the same file (compare `st_dev` and `st_ino`) - truncating the destination would destroy the source.
- Destination is a directory: copy into it with the source's basename.
- Recursive copy: `opendir`/`readdir`, recreate directories, skip `.` and `..`, detect cycles via symlinks.
- Symlinks: copy the link (`readlink` + `symlink`) or follow it (`-L`).
- Sparse files: use `lseek(SEEK_DATA/SEEK_HOLE)` to skip holes.
- Metadata for `-p`: `fchmod`, `fchown`, `futimens`; extended attributes and ACLs.
- Crash safety: write to a temp file, `fsync`, then `rename` over the target.

## Example

```cpp
#include <fcntl.h>
#include <sys/stat.h>
#include <unistd.h>
#include <cerrno>
#include <vector>

int copyFile(const char* src, const char* dst) {
    int in = open(src, O_RDONLY);
    if (in < 0) return -1;
    struct stat st;
    if (fstat(in, &st) < 0) { close(in); return -1; }
    int out = open(dst, O_WRONLY | O_CREAT | O_TRUNC, st.st_mode & 07777);
    if (out < 0) { close(in); return -1; }

    std::vector<char> buf(128 * 1024);
    for (;;) {
        ssize_t n = read(in, buf.data(), buf.size());
        if (n == 0) break;                         // EOF
        if (n < 0) { if (errno == EINTR) continue; goto fail; }
        for (ssize_t off = 0; off < n; ) {         // handle short writes
            ssize_t w = write(out, buf.data() + off, n - off);
            if (w < 0) { if (errno == EINTR) continue; goto fail; }
            off += w;
        }
    }
    close(in);
    return close(out);                             // close can report I/O errors
fail:
    close(in); close(out); return -1;
}
```

## Pitfalls and follow-ups

- **Why check `close`'s return?** Delayed write errors (e.g. NFS, disk full) may surface there.
- **Is the copy durable after `cp` returns?** No, only after `fsync` on the file (and its directory for a new name).
- **Windows equivalent?** `CreateFile`, `ReadFile`/`WriteFile`, or the `CopyFileEx` API, which handles attributes and progress callbacks.
- **Copy across filesystems?** `copy_file_range` may fail with `EXDEV` depending on kernel version and filesystems; keep the `read`/`write` loop as the fallback. Moving (`mv`) across devices is copy + delete, since `rename` cannot cross filesystems.
- **Java comparison:** `Files.copy(src, dst, StandardCopyOption.COPY_ATTRIBUTES)`, or `FileChannel.transferTo`, which can use `sendfile`/`copy_file_range` under the hood.
