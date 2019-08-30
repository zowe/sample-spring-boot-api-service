#! /bin/env node

import * as handlebars from "handlebars";
import * as fs from "fs";

// render the JCL
const jcl = fs.readFileSync("./deploy/templates/jcl/src/samplapi.jcl").toString();
const compiled = handlebars.compile(jcl);
const rendered = compiled({
    jobcard: {
        hlq: "PUBLIC.TEMPLATE",
        name: "TEMPLATE",
        account: "#ACCT",
        description: "ASM/BIND/RUN",
        messageClass: "A",
        jobClass: "A"
    },
    build: {
        rootDir: "/ibmuser/samplapi"
    }
});

if (!fs.existsSync("./deploy/templates/jcl/out")) fs.mkdirSync("./deploy/templates/jcl/out");
fs.writeFileSync("./deploy/templates/jcl/out/samplapi.jcl", rendered);
console.log("./deploy/templates/jcl/out/samplapi.jcl");
