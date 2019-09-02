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

You can use the [Zowe API Development tool](devtool.md) or issue following commands your workstation:

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
- `make install` to copy `libwtojni.so` to the directory `../lib` so the Java can load it (`-p` preserves the extended attribute that is set by `makefile`)

You can issue these commands from your workstation using Zowe CLI.

You need to setup an SSH profile:

```bash
zowe profiles create ssh-profile ssh_host --host host.domain --user userid --password "password"
```

And issue the commands:

```bash
zowe zos-uss issue ssh "make; make install" --cwd "/u/ibmuser/samplapi/zossrc"
```

### Manual Build Steps

The follow commands can be used to build individual pieces of the "shared object" on USS.

Compile Metal C code to assembler:

`xlc -S -W "c,metal,langlvl(extended),sscom,nolongname,inline,genasm,inlrpt,csect,nose,lp64,list,warn64,optimize(2),list,showinc,showmacro,source,aggregate" -qlist=wtoexec.mtl.lst -I/usr/include/metal -o wtoexec.s wtoexec.c`

Assemble an assembly source file:

`as -mrent -a=wtoexec.asm.lst -ISYS1.MACLIB  -ICBC.SCCNSAM -o wtoexec.o wtoexec.s`

Generate object code from C++ file:

`xlc++ -o libwtojni.so -W "l,xplink,dll" -W "c,lp64,langlvl(extended),xplink,dll,exportall" -I/sys/java64bt/v8r0m0/usr/lpp/java/J8.0_64/include wtojni.cpp`

Create the `libwtojni.so`:

`xlc++ -W "l,lp64,dll,dynam=dll,xplink,map,list"  -qsource -o libwtojni.so wtojni.o wtoexec.o`

Be sure to add the [program control](https://github.com/zowe/sample-spring-boot-api-service/issues/14) attribute to avoid `java.lang.UnsatisfiedLinkError` errors:

`extattr +p libwtojni.so`

## Testing

To test this new endpoint, provide the `Authorizaton: Basic ...` header and use the `../api/v1/wto` endpoint.

You can use the [HTTPie](https://httpie.org/) client:

```bash
http -a "ibmuser:<password>" --verify=False --body GET "https://ca32.lvn.broadcom.net:10087/api/v1/wto?name=KELDA"
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

## Packaging shared objects (.so) in Java archives (.jar)

There are multiple ways how native libraries can be packaged. Java applications outside of z/OS can package the `.so` files for various platforms
into their `.jar` file and extract them into a temporary directory and load them from there.

That option is not useful for z/OS since the extended attribute `+p` needs to set and that cannot be done by the user ID that starts the Java application.

We want to make the distribution of the native libraries (`.so`) easy.
The `.so` files are packaged into the `.jar` libraries that are using them.
For example, the SDK library `.so` files are packaged into `libs/` directory inside the SDK library JAR.
The same is done for `.so` files of the sample and they will be packaged in the sample application JAR.
The sample is able to extract all the `.so` files to a target directory.
The developer can just care about deploying the application JAR to z/OS and then extracting all `.so` files to a LIBPATH directory and calling `extattr +p`.
For SMP/E packaging, the `.so` are extracted during the packaging process and distributed via SMP/E.
The benefit of packaging `.so` into jars is that we do not need care about additional channel for publishing via Maven.

After successful build you need to download the `.so` file from z/OSMF in your `zowe-rest-api-sample-spring` directory:

```bash
zowe files download uss-file "/u/ibmuser/samplapi/zossrc/libwtojni.so" -f "src/main/resources/lib/libwtojni.so" --binary
```

The Gradle build includes the `.so` file into the application jar automatically.
