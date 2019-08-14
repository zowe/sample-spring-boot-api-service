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
import { exec } from "child_process";
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
function uploadFolder(folder: string, file?: string) {
    const dir = `zossrc/${folder}`;

    // make sure file exists
    if (existsSync(dir)) {

        // upload a specific file
        if (file) {
            issueUploadCommnad(`${dir}/${file}`, `${rootDir}${uploads[folder]}/${file}`);

            // upload all files in a folder
        } else {
            if (lstatSync(dir).isDirectory()) {
                const files = readdirSync(dir);
                files.forEach((file) => {
                    issueUploadCommnad(`${dir}/${file}`, `${rootDir}${uploads[folder]}/${file}`);
                });
            }
            else {
                issueUploadCommnad(`${dir}`, `${rootDir}/${folder}`);
            }
        }
    } else {
        console.error(`>>> ${dir} does not exist`);
    }
}


/**
 * Create and invoke the zowe files upload command
 * @param {string} localFile - local file source
 * @param {string} dataSet - data set target
 */
function issueUploadCommnad(localFile: string, dataSet: string) {
    const cmd = `zowe files upload ftu "${localFile}" "${dataSet}"`;
    console.log(cmd);
    exec(cmd, (err, stdout, stderr) => {
        if (err) console.log(err)
        if (stdout) console.log(stdout.toString());
        if (stderr) console.log(stderr.toString());
    });
}