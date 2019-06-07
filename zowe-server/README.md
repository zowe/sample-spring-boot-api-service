# Zowe Server

Zowe Server is a prototype of the top-level package that contains all Zowe components.

It is described at <https://github.com/zowe/zowe-install-packaging/wiki/Packaging-of-Zowe-on-server#prototype>.

The server includes other packages as dependencies. It in the prototype, it is just `zowe-fake-cm` and the sample service itself. In the real Zowe, it would be every Zowe component.
Some packages can be skipped on some platforms using `os` (<https://docs.npmjs.com/files/package.json#os>) and `optionalDependencies`.
For example, `zss` would not be installed outside of z/OS.

## Scripts

The scripts in `bin` orchestrate actions for multiple components.
In the real implementation, they will have full logic of understanding package dependencies
(probably implemented in a better programming language then shell - e.g. Java).

This script can have the entry point(s) defined in `bin` sections of `package.json` so they are easily accessible when installed globally by NPM on developer's workstations.
