# z/OS Native Code of the SDK

SDK contains following native libraries:

- `libzowe-sdk-secur.so` - contains security related functionality

## Building

The code is `zossrc` folder including the makefile.

These scripts are experimental and will be improved a lot in future and integrated to the Gradle build process.

1. `cd zowe-rest-api-sdk-cli`
2. `npm install; npm link`
3. Create a z/OSMF and SSH profiles with Zowe CLI
4. Mount a zFS file system for the build
5. `zowe-api init -t <zosTargetDir>`
6. `zowe-api zosbuild`
7. `./gradlew build` includes the `libzowe-sdk-secur.so` in the to `.jar` file
