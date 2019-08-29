#! /bin/env node

/**
 * Use to run the z/OS build using make
 *
 * Examples:
 *
 *   npm run zosbuild
 */

import { join } from "path";
import { readConfiguration } from "./config";
import { Config } from "./doc/IConfig";
import { issueSshCommand, uploadFolder } from "./upload";

// get config
const sourceDir = (process.argv.length >= 3) ? process.argv[2] : ".";
const config: Config = readConfiguration(sourceDir);
const rootDir: string = config.build.rootDir;

// upload everything
const promise = uploadFolder(join(sourceDir, 'zossrc'), rootDir);
promise.then(function (values: boolean[]) {
    if (values.every(x => x === true)) {
        issueSshCommand("make; make install", rootDir);
    }
    else {
        console.error("No build started because upload has failed");
    }
});
