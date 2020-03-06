/*
* This program and the accompanying materials are made available and may be used, at your option, under either:
* * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
* * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Copyright Contributors to the Zowe Project.
*/

import { Command, flags } from "@oclif/command";
import { execSync } from "child_process";
import { existsSync, lstatSync, readdirSync } from "fs";
import * as logSymbols from "log-symbols";
import { dirname } from "path";
import { readConfiguration } from "../config";
import { isFileSame, saveFileToOld } from '../files';
import { execSshCommandWithDefaultEnv, execSshCommandWithDefaultEnvCwd, getDefaultProfile, zoweSync } from "../zowe";

export default class ZosBuild extends Command {
    static description = "build z/OS source on z/OS UNIX";

    static flags = {
        curl: flags.boolean({ char: "c", description: "uses curl to download build artifacts" }),
        force: flags.boolean({ char: "f", description: "forces full upload and build even if there is no change" }),
    };

    async run() {
        const f = this.parse(ZosBuild);
        const [userConfig, projectConfig] = readConfiguration(this);
        if (!projectConfig.zosSourcesDir) {
            this.error("There is no directory with z/OS sources defined in `zowe-api.json`. Are in the correct directory?")
        }
        const zosDir = `${userConfig.zosTargetDir}/${projectConfig.zosSourcesDir}`;
        const uploadedFiles = uploadDir(projectConfig.zosSourcesDir, zosDir, userConfig.zoweProfileName, this, f.flags.force);
        if (uploadedFiles) {
            const env: { [name: string]: string } = {};
            if (userConfig.javaHome) {
                env.JAVA_HOME = userConfig.javaHome;
            }
            this.log(`Building z/OS native code`);
            execSshCommandWithDefaultEnv(projectConfig.buildCommand, zosDir, env);
            for (const [zosFile, targetFile] of Object.entries(projectConfig.buildFiles)) {
                this.log(`Downloading ${zosDir}/${zosFile} to ${targetFile}`);
                if (f.flags.curl) {
                    const zosmfProfile = getDefaultProfile("zosmf");
                    const p = zosmfProfile.profile;
                    const targetDir = dirname(targetFile);
                    const curlCommand = `curl${p.rejectUnauthorized?"":" -k"} --user ${p.user}:${p.password} --tlsv1.2 -H \"X-IBM-Data-Type: binary\" \"https://${p.host}:${p.port}/zosmf/restfiles/fs${zosDir}/${zosFile}.pax\" > ${targetFile}.pax; tar -xvf ${targetFile}.pax -C ${targetDir}; rm ${targetFile}.pax`;
                    this.log(`Executing: ${curlCommand}`);
                    execSync(curlCommand);
                }
                else {
                    zoweSync(`files download uss-file ${zosDir}/${zosFile} --binary -f ${targetFile}`);
                }
            }
            this.log(logSymbols.success, "z/OS build completed");
        }
        else {
            this.log(logSymbols.success, "z/OS build up to date");
        }
        this.log(logSymbols.info, "Use 'zowe-api-dev deploy' to deploy your application to z/OS");
    }
}

function uploadDir(dir: string, zosDir: string, profileName: string, command: Command, force = false) {
    let uploadedFiles = 0;
    if (existsSync(dir) && lstatSync(dir).isDirectory()) {
        const files = readdirSync(dir);
        files.forEach(file => {
            const sourceFile = `${dir}/${file}`;
            const targetFile = `${zosDir}/${file}`;
            if (force || !isFileSame(sourceFile, targetFile, profileName)) {
                uploadedFiles += 1;
                if (uploadedFiles === 1) {
                    execSshCommandWithDefaultEnvCwd(`mkdir -p ${zosDir}`);
                }
                command.log(`Uploading ${file} to ${targetFile}`);
                zoweSync(`files upload ftu "${sourceFile}" "${targetFile}"`);
                saveFileToOld(sourceFile, targetFile, profileName);
            }
        });
    } else {
        command.error(`Directory '${dir}' does not exist`);
    }
    return uploadedFiles;
}
