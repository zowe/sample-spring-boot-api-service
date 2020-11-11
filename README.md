# [Zowe](https://zowe.org/) REST API SDK and Sample Service &middot; [![CircleCI](https://circleci.com/gh/zowe/sample-spring-boot-api-service.svg?style=shield)](https://circleci.com/gh/zowe/sample-spring-boot-api-service) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=zowe_sample-spring-boot-api-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=zowe_sample-spring-boot-api-service) [ ![download](https://api.bintray.com/packages/plavjanik/zowe/zowe-rest-api-commons-spring/images/download.svg?version=1.1.0) ](https://bintray.com/plavjanik/zowe/zowe-rest-api-commons-spring/1.1.0/link)<a href="https://scan.coverity.com/projects/zowe-sample-spring-boot-api-service"><img alt="Coverity Scan Build Status" src="https://scan.coverity.com/projects/21953/badge.svg"/></a>

This project provides:

1. [Sample REST API service](zowe-rest-api-sample-spring/README.md) that run on z/OS and can use native z/OS interfaces
2. SDK (software development kit) for creating such services that includes:
   - [Common Java library for REST APIs on z/OS](zowe-rest-api-commons-spring/README.md) that includes the reusable functionality for developing such API services on z/OS
   - [CLI tool zowe-api-dev](zowe-api-dev/README.md) that will allow use to develop your API service on PC and deploy it to z/OS UNIX environment

## Getting Started

### Developing New REST API Service

Follow the instructions at [Zowe Sample API Service](zowe-rest-api-sample-spring/README.md).

## Demo

### Deploying the sample REST API to z/OS under 5 minutes

[![asciicast](https://asciinema.org/a/266002.svg)](https://asciinema.org/a/266002)

## Documentation for SDK Developers

- [Building SDK](docs/building.md)
- [Building z/OS native code](docs/zos-native-code.md)
- [Releasing](docs/releasing.md)

## License

The repository is dual-licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0) and [Eclipse Public License - v 2.0](https://www.eclipse.org/legal/epl-2.0/).
