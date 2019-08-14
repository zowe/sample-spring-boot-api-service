#! /bin/env node

// bypass immutability
process.env.ALLOW_CONFIG_MUTATIONS = "yes"; // value doesn't matter

import * as config from "config";
import * as handlebars from "handlebars";
import * as fs from "fs";

// render the JCL
const jcl = fs.readFileSync("./deploy/templates/jcl/src/samplapi.jcl").toString();
const compiled = handlebars.compile(jcl);
const rendered = compiled(config);

if (!fs.existsSync("./deploy/templates/jcl/out")) fs.mkdirSync("./deploy/templates/jcl/out");
fs.writeFileSync("./deploy/templates/jcl/out/samplapi.jcl", rendered);
console.log("./deploy/templates/jcl/out/samplapi.jcl");