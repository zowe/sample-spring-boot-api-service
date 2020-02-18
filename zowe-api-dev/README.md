# Zowe REST API SDK Command Line Tool

Provides convenience functions to build native code, deploy REST API, and start it in a form of a CLI tool `zowe-api`.

## Installation

### From NPM (for users)

```bash
npm -g install @zowedev/zowe-api-dev
```

### From sources (for developers)

```bash
git clone https://github.com/zowe/sample-spring-boot-api-service
cd sample-spring-boot-api-service
./gradlew :jarpatcher:build
cd zowe-api-dev
npm install
npm pack
npm link
```

**Note:** The `npm pack` is required to include the `jarpatcher.jar` that is created by `./gradlew :jarpatcher:build`.

## Usage

See [zowe-api-dev - Zowe API Development CLI Tool](/zowe-rest-api-sample-spring/docs/devtool.md).

```txt
$ zowe-api-dev

Zowe REST API Development CLI

USAGE
  $ zowe-api-dev [COMMAND]

COMMANDS
  config    configure the API service on z/OS
  deploy    deploy the API service artifacts to z/OS
  help      display help for zowe-api-dev
  init      initialize user configuration file
  start     start the API service on z/OS
  status    get status of API service
  stop      stop the API service on z/OS
  zfs       initialize user configuration file
  zosbuild  build z/OS source on z/OS UNIX
```
