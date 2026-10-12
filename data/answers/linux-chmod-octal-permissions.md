**Short answer:** Each file has three permission sets: owner (user), group, and others. Each set is three bits: read = 4, write = 2, execute = 1. Add them to get one octal digit per set, and pass the three digits to `chmod`. So `chmod 755 deploy.sh` gives the owner `rwx` (4+2+1), and group and others `r-x` (4+1). `chmod 644 app.properties` gives the owner `rw-` and everyone else `r--`.

## Explanation

**Reading `ls -l`:**

```text
-rwxr-x---  1 akhil devs  1200 Oct 12 10:00 deploy.sh
 └┬┘└┬┘└┬┘
  │  │  └── others: ---  = 0
  │  └───── group:  r-x  = 4+0+1 = 5
  └──────── owner:  rwx  = 4+2+1 = 7      ->  750
```

The first character is the type (`-` file, `d` directory, `l` symlink).

**Digit table:**

| Digit | Bits | Meaning |
|---|---|---|
| 7 | rwx | read, write, execute |
| 6 | rw- | read, write |
| 5 | r-x | read, execute |
| 4 | r-- | read only |
| 0 | --- | nothing |

**Directories are different:** `r` lets you list names, `w` lets you create, delete and rename entries inside, and `x` lets you enter it (`cd`) and reach files by path. A directory usually needs `x` to be useful, so `755` or `750` is typical.

**Common values:** `755` scripts and directories, `644` normal files, `600` private files like SSH keys (`ssh` refuses keys that others can read), `700` private directories like `~/.ssh`. Avoid `777`: anyone can modify the file.

**A fourth, leading digit** sets special bits: setuid = 4, setgid = 2, sticky = 1. `chmod 4755` runs a program as its owner (like `passwd`); `chmod 1777 /tmp` lets everyone write but only delete their own files.

**umask** sets defaults for new files: files start from `666`, directories from `777`, minus the umask bits. With the common umask `022`, new files get `644` and directories `755`.

## Example

```text
$ chmod 755 deploy.sh          # owner rwx, group r-x, others r-x
$ chmod 600 ~/.ssh/id_ed25519  # owner rw, no one else
$ chmod -R 750 /opt/orders     # recursively: owner rwx, group r-x, others none
$ chmod u+x run.sh             # symbolic form: add execute for the owner only
$ chmod go-w config.yaml       # symbolic form: remove write from group and others
$ stat -c '%a %n' deploy.sh    # print the octal mode
755 deploy.sh
```

For a Spring Boot service deployed on a VM, a typical setup is the JAR owned by a dedicated service user with `500` or `550`, and `application.yml` with secrets at `600` or `640`.

## Pitfalls and follow-ups

- **`chmod` vs `chown`:** `chmod` changes permissions; `chown user:group file` changes the owner and group.
- **`chmod -R 755` on a tree** makes every file executable. Safer: `find dir -type d -exec chmod 755 {} +` and `find dir -type f -exec chmod 644 {} +`, or the symbolic `chmod -R u=rwX,go=rX dir` (capital `X` adds execute only to directories and already-executable files).
- **Who can change permissions?** Only the owner or root.
- **Why does my script say "Permission denied"?** It lacks `x`, or it sits on a filesystem mounted `noexec`; `bash script.sh` works without `x` because bash reads it as a file.
- **Root and permissions:** root bypasses read and write checks, which is why services should not run as root.
- **ACLs** (`setfacl`, `getfacl`) give finer control than the three sets when one extra user needs access.

Go deeper: [S0 · Foundations: computers, programs, networks, the request](../academy/lessons/S0.md).
