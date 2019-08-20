# z/OS Native OS Linkage Example

These steps describe how to build and test this sample REST API in a way that
uses JNI to call a native z/OS service - WTO.  You can follow these steps and modify them to
call your own assembler / C API.

These instructions assume that you follow the [z/OS deployment setup](./zos-deployment.md).

## JNI Overview

There are many tutorials online to describe details and examples of JNI.

At a high level, the process involves making use of the `native` keyword in a class.  After this, you run the `javah` command against the class to emit a C/C++ header file.  This emitted header file will contain function prototypes for the native method for which you must provide the implementation.

Here is the example `javah` command for this project:
`javah -o ./zossrc/chdr/wtojni.h -classpath ./build/classes/java/main/ org.zowe.sample.apiservice.wto.ZosWto`

---

**Note**: The javah tool is deprecated as of JDK 9 and might be removed in a future JDK release. The tool has been superseded by the -h option added to javac in JDK 8

---

After you implement the function(s) from the header file, you must build the native code on z/OS into a "shared object" which is analogous to a Window's DLL.

The "shared object" is located within the project and loaded at run time.  The file name for the "shared object" must be prefixed with `lib` and end in `.so`, e.g. `libwtojni.so` even though the name to be loaded is simply `wtojni`.

## Uploading Source Code to z/OS

You can use the following CLI commands to upload sources in `zossrc` folder to z/OS Unix.

On z/OS Unix:

- `cd /u/ibmuser/samplapi`
- `mkdir -p zossrc`


You can use the [deployment script](deploy-scripts.md) or issue followinng commands your workstation:

- `export ZOS_TARGET_DIR="/u/ibmuser/samplapi"` (Bash)
  - or `set -gx ZOS_TARGET_DIR "/u/ibmuser/samplapi"` (Fish)
- `zowe files upload ftu zossrc/wto.h $ZOS_TARGET_DIR/zossrc/wto.h`
- `zowe files upload ftu zossrc/wtoexec.h $ZOS_TARGET_DIR/zossrc/wtoexec.h`
- `zowe files upload ftu zossrc/wtojni.h $ZOS_TARGET_DIR/zossrc/wtojni.h`
- `zowe files upload ftu zossrc/wtoexec.c $ZOS_TARGET_DIR/zossrc/wtoexec.c`
- `zowe files upload ftu zossrc/wtojni.cpp $ZOS_TARGET_DIR/zossrc/wtojni.cpp`
- `zowe files upload ftu zossrc/makefile $ZOS_TARGET_DIR/zossrc/makefile`

## Building JNI

You can build the JNI code via:

- using [make](#makefile)
- [manually](#manual-build-steps)

### Makefile

You use the uploaded [makefile](../zossrc/makefile) to build via `make` on z/OS Unix:

- `cd /u/ibmuser/samplapi/zossrc`
- `make`
- `make install` to copy `libwtojni.so` to the directory above so the Java can load it (`-p` preserves the extended attribute that is set by `makefile`)

### Manual Build Steps

The follow commands can be used to build individual pieces of the "shared object" on USS.

Compile Metal C code to assembler:

`xlc -S -W "c,metal, langlvl(extended), sscom, nolongname, inline, genasm, inlrpt, csect, nose, lp64, list, warn64, optimize(2), list, showinc, showmacro, source, aggregate" -qlist=wtoexec.mtl.lst -I/usr/include/metal -o wtoexec.s wtoexec.c`

Assemble an assembly source file:

`as -mrent -a=wtoexec.asm.lst -ISYS1.MACLIB  -ICBC.SCCNSAM -o wtoexec.o wtoexec.s`

Generate object code from C++ file:

`xlc++ -o libwtojni.so -W "l,xplink,dll" -W "c,lp64,langlvl(extended),xplink,dll,exportall" -I/sys/java64bt/v8r0m0/usr/lpp/java/J8.0_64/include wtojni.cpp`

Create the `libwtojni.so`:

`xlc++ -W "l,lp64,dll,dynam=DLL,XPLINK,map,list"  -qsource -o libwtojni.so wtojni.o wtoexec.o`

Be sure to add the [program control](https://github.com/zowe/sample-spring-boot-api-service/issues/14) attribute to avoid `java.lang.UnsatisfiedLinkError` errors:

`extattr +p libwtojni.so`

## Testing

To test this new endpoint, provide the `Authorizaton: Basic ...` header and use the `../api/v1/wto` endpoint.

You can use the [HTTPie](https://httpie.org/) client:

```bash
http -a "ibmuser:<password>" --verify=False --body GET "https://ca32.ca.com:10087/api/v1/wto?name=KELDA"
```

You should get:

```json
{
    "content": "Hello, KELDA!",
    "id": 1,
    "message": "Message set from JNI",
    "rc": 0
}
```

And the message should be in the z/OS syslog:

```text
03:51:18.64 STC46227 00000014 +Number was: '1'; String was: 'Hello, KELDA!'
```
