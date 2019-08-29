export interface Config {
    jobcard: {
        hlq: string,
        name: string,
        account: string,
        description: string,
        messageClass: string,
        jobClass: string,
    },
    build: {
        rootDir: string,
    },
}
