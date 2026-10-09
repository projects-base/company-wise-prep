**Short answer:** First see why it will not start: `ldd ./app` lists the shared libraries and marks missing ones as `not found`. Find the `.so` on disk and run with `LD_LIBRARY_PATH=/path/to/libdir ./app`. Then, to learn what config file it wants, run it under `strace -f -e trace=file ./app` and look for `open`/`openat`/`stat` calls that return `ENOENT`. Create the file at that path (or point the binary to it) and read the output. `strings` and `ltrace` help when strace is not enough.

## Explanation

**Step 1 - loader errors.** A message like `error while loading shared libraries: libfoo.so.1: cannot open shared object file` comes from the dynamic loader (`ld-linux`), before `main` runs. The loader searches, in order: `DT_RPATH` (if no `RUNPATH`), `LD_LIBRARY_PATH`, `DT_RUNPATH`, the `/etc/ld.so.cache` built by `ldconfig`, then default directories such as `/lib` and `/usr/lib`.

```bash
file ./app                         # 32/64-bit, dynamically linked?
ldd ./app                          # libfoo.so.1 => not found
find / -name 'libfoo.so*' 2>/dev/null
LD_LIBRARY_PATH=/opt/vendor/lib ./app
readelf -d ./app | grep -E 'NEEDED|RPATH|RUNPATH'
```

Other ways to fix it: `export LD_LIBRARY_PATH=...`, add the directory to `/etc/ld.so.conf.d/` and run `ldconfig` (needs root), or create a symlink if the soname differs only by version (risky if the ABI changed). `LD_DEBUG=libs ./app` prints the loader's search.

**Step 2 - what is it looking for.** `strace` shows every system call with arguments and results.

```bash
strace -f -e trace=file ./app 2>&1 | grep -E 'ENOENT|EACCES'
# openat(AT_FDCWD, "/etc/app/secret.conf", O_RDONLY) = -1 ENOENT (No such file or directory)
strace -f -e trace=openat,read -s 200 ./app   # see the bytes it reads
```

- `-f` follows child processes and threads.
- `-s 200` shows longer strings in buffers.
- `-e trace=file` limits to path-taking calls (`openat`, `stat`, `access`, `readlink`...). On newer systems use `-e trace=%file`.
- `-o out.txt` writes to a file; `-p PID` attaches to a running process.

Look at what the binary does right after the failed open: it might fall back to an environment variable (`getenv` is not a syscall - use `ltrace -e getenv ./app` or `strings ./app | grep -i conf`). If it reads the file and still fails, strace shows the read contents and the next failing call, which hints at the expected format.

**Step 3 - extract the data.** Create the config at the path, fix permissions if you see `EACCES`, rerun.

## Example

```bash
$ ./extract
./extract: error while loading shared libraries: libcrypto_v.so: cannot open shared object file
$ export LD_LIBRARY_PATH=$PWD/lib
$ strace -e trace=openat ./extract 2>&1 | grep ENOENT
openat(AT_FDCWD, "/home/user/.extract/config.ini", O_RDONLY) = -1 ENOENT
$ mkdir -p ~/.extract && printf 'key=value\n' > ~/.extract/config.ini
$ ./extract
```

## Pitfalls and follow-ups

- **Setuid binaries ignore `LD_LIBRARY_PATH`** (secure-execution mode), and tracing a setuid binary as a normal user runs it without the extra privileges.
- **strace overhead:** it uses ptrace and slows every syscall a lot; do not leave it on a production latency-critical process.
- **strace vs ltrace vs gdb:** syscalls vs library calls vs full debugging with breakpoints.
- **Static binary?** `ldd` says "not a dynamic executable" and no library path is needed.
- **Java comparison:** the same tools work on a JVM; `-Djava.library.path` plays the role of `LD_LIBRARY_PATH` for JNI libraries loaded with `System.loadLibrary`.

Related: [H3 · Debugging and production habits](../academy/lessons/H3.md).
