#! /bin/env node

/**
 * Use to run the z/OS build using make
 *
 * Examples:
 *
 *   npm run zosbuild
 */

import { basename, dirname, normalize } from "path";
process.env.NODE_CONFIG_DIR = normalize(__dirname + "../../../../deploy/config");

import * as config from "config";
import { exec } from "child_process";
import { readdirSync, existsSync, lstatSync } from "fs";
import { Uploads } from "./doc/IUploads";
import { uploadFolder } from "./upload";

// get config
const rootDir: string = config.get<string>('build.rootDir');
const uploads: Uploads = config.get<Uploads>('uploads');

// upload everything
Object.keys(uploads).forEach((key) => {
    uploadFolder(key);
});

issueSshCommand("make; make install", rootDir);

function issueSshCommand(command: string, currentWorkingDirectory: string) {
    const cmd = `zowe zos-uss issue ssh "${command}" --cwd "${currentWorkingDirectory}"`;
    console.log(cmd);
    exec(cmd, (err, stdout, stderr) => {
        if (err) console.log(err)
        if (stdout) console.log(stdout.toString());
        if (stderr) console.log(stderr.toString());
    });
}
