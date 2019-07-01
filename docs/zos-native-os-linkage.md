# z/OS Native OS Linkage Example

These steps describe how to build and test this sample REST API in a way that
uses JNI to call a native z/OS service - WTO.  You can follow these steps and modify them to
call your own assembler / C API.

These instructions assume that you follow the [z/OS deployment setup](./zos-deployment).

## JNI Overview

There are many tutorials online to describe details and examples of JNI.  

At a high level, the process involves making use of the `native` keyword in a class.  After this, you run the `javah` command against the class to emit a C/C++ header file.  This emitted header file will contain function prototypes for the native method for which you must provide the implementation.

Here is the example `javah` command for this project:
`javah -o ./zossrc/chdr/wtojni.h -classpath ./build/classes/java/main/ org.zowe.sample.apiservice.hello.NativeGreeting`

After you implement the function(s) from the header file, you must build the native code on z/OS into a "shared object" which is analogous to a Window's DLL.

The "shared object" is located within the project and loaded at run time.  The file name for the "shared object" must be prefixed with `lib` and end in `.so`, e.g. `libwtojni.so` even though the name to be loaded is simply `wtojni`.

## Uploading Source Code to z/OS

## Building JNI

You can build the JNI code via:

- [make](#makefile)
- [manually](#manual-build-steps)

### Makefile

You can upload the [makefile](../zossrc/makefile) to build via `make`.

### Manual Build Steps

The follow commands can be used to build individual pieces of the "shared object" on USS.

Compile Metal C code to assembler:

`xlc -S -W "c,metal, langlvl(extended), sscom, nolongname, inline, genasm, inlrpt, csect, nose, lp64, list, warn64, optimize(2), list, showinc, showmacro, source, aggregate" -qlist=wtoexec.mtl.lst -I/usr/include/metal -o wtoexec.s wtoexec.c`

Assemble an assembly source file:

`as -mrent -a=wtoexec.asm.lst -ISYS1.MACLIB  -ICBC.SCCNSAM -o wtoexec.o wtoexec.s`

Generate object code from C++ file:

`xlC -o libwtojni.so -W "l,xplink,dll" -W "c,lp64,langlvl(extended),xplink,dll,exportall" -I/sys/java64bt/v8r0m0/usr/lpp/java/J8.0_64/include wtojni.cpp`

Create the `libwtojni.so`:

`xlC -W "l,lp64,dll,dynam=DLL,XPLINK,map,list"  -qsource -o libwtojni.so wtojni.o wtoexec.o`

Be sure to add the [program control](https://github.com/zowe/sample-spring-boot-api-service/issues/14) attribute to avoid `java.lang.UnsatisfiedLinkError` errors:

`extattr +p libwtojni.so`

## Testing

To test this new endpoint, provide the `Authorizaton: Basic ...` header and use the `../api/v1/nativegreeting` endpoint.
