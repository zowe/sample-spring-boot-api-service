# Versioning

- The version of the sample REST API service is set to `version = '0.0.1-SNAPSHOT'` in `build.gradle` file. You can change it when you develop your REST API service.

- The version of the SDK commons library `org.zowe:zowe-rest-api-commons-spring` follow [semantic version](https://semver.org/) as it is specified in `build.gradle` in this line:

    ```return "org.zowe:zowe-rest-api-commons-spring:<version>"```

- When you download the latest sample ZIP file <https://github.com/zowe/sample-spring-boot-api-service/releases/latest/download/zowe-rest-api-sample-spring.zip> it will use the latest SDK commons library at that time.

- If you want to use newer version in future, you need to manually update the version number. Since the current version is `0.x` there can be breaking changes at any time until `1.x` is reached but we will try to keep number of breaking changes at minimum.

- For SDK developers: The published versions are controlled by Git tags. You do not need to change the versions in the code.
