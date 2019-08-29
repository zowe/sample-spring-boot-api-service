#! /bin/env node

import * as handlebars from "handlebars";
import * as fs from "fs";

import { readConfiguration } from "./config";
import { Config } from "./doc/IConfig";

const config: Config = readConfiguration();

// render the JCL
const jcl = fs.readFileSync("./deploy/templates/jcl/src/samplapi.jcl").toString();
const compiled = handlebars.compile(jcl);
const rendered = compiled(config);

if (!fs.existsSync("./deploy/templates/jcl/out")) fs.mkdirSync("./deploy/templates/jcl/out");
fs.writeFileSync("./deploy/templates/jcl/out/samplapi.jcl", rendered);
console.log("./deploy/templates/jcl/out/samplapi.jcl");
