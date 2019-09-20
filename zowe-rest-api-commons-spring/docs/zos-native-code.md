# Common z/OS Native Code

The commons library contains following native libraries:

- `libzowe-commons-secur.so` - contains security related functionality

## Building

The code is `zossrc` folder including the makefile.

These scripts are experimental and will be improved a lot in future and integrated to the Gradle build process.

1. `cd zowe-rest-api-commons-cli`
2. `npm install; npm link`
3. Create a z/OSMF and SSH profiles with Zowe CLI
4. Mount a zFS file system for the build
5. `zowe-api-dev init -t <zosTargetDir>`
6. `zowe-api-dev zosbuild`
7. `./gradlew build` includes the `libzowe-commons-secur.so` in the to `.jar` file
