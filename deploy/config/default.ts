import uploads from "./imports/uploads";

export default {

    // settings for all other sections
    jobcard: {
        hlq: "PUBLIC.TEMPLATE",
        name: "TEMPLATE",
        account: "#ACCT",
        description: "ASM/BIND/RUN",
        messageClass: "A",
        jobClass: "B",
    },
    build: {
        rootDir: "/ibmuser/samplapi"
    },

    uploads,
}
