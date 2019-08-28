#! /bin/env node

/**
 * Use to upload all source or a subset of it
 *
 * Examples:
 *  npm run upload
 *  npm run upload -- asmpgm
 *  npm run upload -- asmpgm asmmac/#entry asmmac/#exit
 */

import { basename, dirname, normalize } from "path";
process.env.NODE_CONFIG_DIR = normalize(__dirname + "../../../../deploy/config");

import * as config from "config";
import { exec, ExecException } from "child_process";
import { readdirSync, existsSync, lstatSync } from "fs";
import { Uploads } from "./doc/IUploads";

// get config
const rootDir: string = config.get<string>('build.rootDir');
const uploads: Uploads = config.get<Uploads>('uploads');

// get command args
const numOfParms = process.argv.length - 2;

// upload command line input only
if (numOfParms > 0) {
    for (let i = 0; i < numOfParms; i++) {
        if (dirname(process.argv[2 + i]) === ".") {
            uploadFolder(process.argv[2 + i]);
        } else {
            uploadFolder(dirname(process.argv[2 + i]), basename(process.argv[2 + i]));
        }
    }

    // otherwise upload everything by default
} else {
    Object.keys(uploads).forEach((key) => {
        uploadFolder(key);
    });
}

/**
 * Upload a local folder to z/OS
 * @param {string} folder - folder name
 * @param {string} [file] - option file within the folder
 */
export function uploadFolder(folder: string, file?: string): Promise<boolean>[] {
    const dir = `zossrc/${folder}`;
    var promises = [];

    // make sure file exists
    if (existsSync(dir)) {

        // upload a specific file
        if (file) {
            promises.push(issueUploadCommand(`${dir}/${file}`, `${rootDir}${uploads[folder]}/${file}`));

            // upload all files in a folder
        } else {
            if (lstatSync(dir).isDirectory()) {
                const files = readdirSync(dir);
                files.forEach((file) => {
                    promises.push(issueUploadCommand(`${dir}/${file}`, `${rootDir}${uploads[folder]}/${file}`));
                });
            }
            else {
                promises.push(issueUploadCommand(`${dir}`, `${rootDir}/${folder}`));
            }
        }
    } else {
        console.error(`>>> ${dir} does not exist`);
    }

    return promises;
}

/**
 * Create and invoke the zowe files upload command
 * @param {string} localFile - local file source
 * @param {string} dataSet - data set target
 */
function issueUploadCommand(localFile: string, dataSet: string) {
    const cmd = `zowe files upload ftu "${localFile}" "${dataSet}"`;
    console.log(cmd);
    return execShellCommand(cmd);
}

/**
 * Executes a shell command and return it as a Promise.
 * @param cmd {string} shell command to be executed
 * @return {Promise<boolean>} true if successful
 */
function execShellCommand(cmd: string): Promise<boolean> {
    const exec = require('child_process').exec;
    return new Promise((resolve, reject) => {
        exec(cmd, (err: ExecException | null, stdout: string, stderr: string) => {
            if (err) console.log(err);
            if (stdout) console.log(stdout.toString());
            if (stderr) console.log(stderr.toString());
            resolve(err ? false : true);
        });
    });
}
