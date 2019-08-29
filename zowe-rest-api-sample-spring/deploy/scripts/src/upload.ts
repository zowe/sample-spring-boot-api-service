#! /bin/env node

/**
 * Use to upload all source or a subset of it
 *
 * Examples:
 *  npm run upload
 *  npm run upload -- asmpgm
 *  npm run upload -- asmpgm asmmac/#entry asmmac/#exit
 */

import { ExecException } from "child_process";
import { existsSync, lstatSync, readdirSync } from "fs";
import { join } from "path";
import { readConfiguration } from "./config";
import { Config } from "./doc/IConfig";

const sourceDir = (process.argv.length >= 3) ? process.argv[2] : ".";
upload(sourceDir);

function upload(sourceDir: string) {
    // get config
    const config: Config = readConfiguration(sourceDir);
    const rootDir: string = config.build.rootDir;

    // get command args
    const numOfParms = process.argv.length - 2;

    uploadFolder(join(sourceDir, 'zossrc'), `${rootDir}/zossrc`);
}

/**
 * Upload a local folder to z/OS
 * @param {string} folder - folder name
 * @param {string} [file] - option file within the folder
 */
export async function uploadFolder(dir: string, zosDir: string): Promise<boolean[]> {
    var promises: Promise<boolean>[] = [];

    const value = await issueSshCommand(`mkdir -p "${zosDir}"`, "/");
    // make sure file exists
    if (existsSync(dir) && lstatSync(dir).isDirectory()) {
        const files = readdirSync(dir);
        files.forEach((file) => {
            promises.push(issueUploadCommand(`${dir}/${file}`, `${zosDir}/${file}`));
        });
    }
    else {
        console.error(`>>> ${dir} does not exist`);
    }
    return Promise.all(promises);
}

/**
 * Create and invoke the zowe files upload command
 * @param {string} localFile - local file source
 * @param {string} zosFile - z/OS Unix file targe path
 */
function issueUploadCommand(localFile: string, zosFile: string): Promise<boolean> {
    const cmd = `zowe files upload ftu "${localFile}" "${zosFile}"`;
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

export function issueSshCommand(command: string, currentWorkingDirectory: string): Promise<boolean> {
    const exec = require('child_process').exec;
    const cmd = `zowe zos-uss issue ssh "${command}" --cwd "${currentWorkingDirectory}"`;
    console.log(cmd);
    return new Promise((resolve, reject) => {
        exec(cmd, (err: ExecException | null, stdout: string, stderr: string) => {
            if (err) console.log(err)
            if (stdout) console.log(stdout.toString());
            if (stderr) console.log(stderr.toString());
            resolve(err ? false : true);
        });
    });
}
