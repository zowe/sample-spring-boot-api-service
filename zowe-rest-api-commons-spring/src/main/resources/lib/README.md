# `resources/lib` folder

This folder is a placeholder for `.so` files that are z/OS shared object libraries.

Zowe GitHub repositories cannot contain binary files so you need to build it using
`zowe-api-dev zosbuild` command.

This folder `resources/lib` is packaged into the JAR file. `.so` files in the JAR file
can be extracted to regular files using `LibsExtractor`.
These files need to be executable and program-controlled on z/OS in order to be loadable by Java.
