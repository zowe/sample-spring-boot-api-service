/*
* This program and the accompanying materials are made available and may be used, at your option, under either:
* * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
* * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Copyright Contributors to the Zowe Project.
*/

import Command from "@oclif/command";
import { execSync } from 'child_process';
import * as Debug from "debug";
import { copyFileSync, createReadStream, existsSync, mkdirSync, readFileSync, writeFileSync } from "fs";
import * as Handlebars from "handlebars";
import { dirname, resolve } from "path";
import * as tmp from "tmp";
import { Entry, Parse } from "unzipper";
import { checkZoweProfileName, ITransferredFile, IUserConfig } from "./config";
import { execSshCommandWithDefaultEnv, execSshCommandWithDefaultEnvCwd, IApiResponse, zoweSync } from "./zowe";

const debug = Debug("files");
const userDataDir = ".zowe-api-dev";

export function cachedOldFilePath(zosFile: string, profileName: string) {
    const oldFile = resolve(".", userDataDir, "uploadedFiles", profileName, zosFile.substring(1));
    return oldFile;
}

export function saveFileToOld(file: string, zosFile: string, profileName: string): void {
    const oldFile = cachedOldFilePath(zosFile, profileName);
    const oldFileDir = dirname(oldFile);
    if (!existsSync(oldFileDir)) {
        mkdirSync(oldFileDir, { recursive: true });
    }
    copyFileSync(file, oldFile);
}

function uploadFullFile( command: Command, zosDir: string, file: string, zosFile: string, options: ITransferredFile, profileName: string): void {
    command.log(`Making directory ${zosDir}`);
    execSshCommandWithDefaultEnvCwd(`mkdir -p ${zosDir}`);
    command.log(`Uploading ${file} to ${zosFile}`);
    zoweSync(`files upload ftu ${file} ${zosFile}${options.binary ? " --binary" : ""}`);
    saveFileToOld(file, zosFile, profileName);
}

export function isFileSame(file: string, zosFile: string, profileName: string): boolean {
    const oldFile = cachedOldFilePath(zosFile, profileName);
    debug("Old file: ", oldFile);
    debug("Current file: ", file);
    if (existsSync(oldFile)) {
        const cmp = Buffer.compare(readFileSync(file), readFileSync(oldFile));
        debug(cmp);
        return cmp === 0;
    }
    return false;
}

export function zosExistsSync(zosFile: string): boolean {
    const data = zoweSync(`files list uss ${zosFile}`, { throwError: false, logOutput: false }).data as IApiResponse;
    try {
        return data.apiResponse.totalRows === 1;
    }
    catch (e) {
        return false;
    }
}

export function transferFiles(
    files: { [filename: string]: ITransferredFile },
    zosTargetDir: string,
    userConfig: IUserConfig,
    command: Command,
    force: boolean,
    context?: {},
) {
    // tslint:disable-next-line: no-console
    console.time("transferFile");
    checkZoweProfileName(userConfig);
    for (const [file, options] of Object.entries(files)) {
        debug("options", options);
        const zosFile = `${zosTargetDir}/${options.target}`;
        const zosDir = dirname(zosFile);
        let soUpdated = true;
        if (options.template) {
            const tmpPath = tmp.tmpNameSync();
            debug(tmpPath);
            const template = Handlebars.compile(readFileSync(file).toString(), { strict: true });
            const result = template(context);
            debug(result);
            writeFileSync(tmpPath, result);
            command.log(`Making directory ${zosDir}`);
            execSshCommandWithDefaultEnvCwd(`mkdir -p ${zosDir}`);
            command.log(`Uploading template ${file} to ${zosFile}`);
            zoweSync(`files upload ftu ${tmpPath} ${zosFile}${options.binary ? " --binary" : ""}`);
        } else {
            const zosFileExists = !force && zosExistsSync(zosFile);
            if (!force && zosFileExists && isFileSame(file, zosFile, userConfig.zoweProfileName)) {
                command.log(`${file} has not changed`)
                continue;
            }
            const oldFile = cachedOldFilePath(zosFile, userConfig.zoweProfileName);
            if (!force && file.endsWith(".jar") && existsSync(oldFile) && zosFileExists) {
                command.log(`Patching ${zosFile} to be same as ${file}`);
                const patchFile = file + "-patch";
                const zosPatchFile = zosFile + "-patch";
                const jarpatcherPath = resolve(__dirname, "..", "lib", "jarpatcher.jar");
                const output = execSync(`java -cp ${jarpatcherPath} jarpatcher.JarPatcher diff ${oldFile} ${file} ${patchFile} ${jarpatcherPath}`, { stdio: "pipe" });
                debug(output);
                let hasSo = false;
                const promise = createReadStream(patchFile).pipe(Parse()).on('entry', function (entry: Entry) {
                    if (entry.path.endsWith(".so") || entry.path.endsWith(".jar") || entry.path.endsWith("LibsExtractor.class")) {
                        hasSo = true;
                    }
                    entry.autodrain();
                }).promise();
                Promise.all([promise]);
                debug("hasSo: ", hasSo);
                if (!hasSo) {
                    soUpdated = false;
                }
                zoweSync(`files upload ftu ${patchFile} ${zosPatchFile} --binary`);
                execSshCommandWithDefaultEnv(`${userConfig.javaHome}/bin/java  -cp ${zosPatchFile} jarpatcher.JarPatcher patch ${zosFile} ${zosPatchFile} jarpatcher`, zosTargetDir);
                saveFileToOld(file, zosFile, userConfig.zoweProfileName);
            }
            else {
                uploadFullFile(command, zosDir, file, zosFile, options, userConfig.zoweProfileName);
            }
        }
        const postCommands: string[] = [];
        if ((soUpdated || force) && options.postSoUpdateCommands) {
            postCommands.push(...options.postSoUpdateCommands);
        }
        if (options.postCommands) {
            postCommands.push(...options.postCommands);
        }
        debug("postCommands", postCommands);
        for (const postCommand of postCommands) {
            let finalCommand = postCommand;
            if (postCommand.startsWith("java") && userConfig.javaHome) {
                finalCommand = userConfig.javaHome + "/bin/" + postCommand;
            }
            finalCommand = finalCommand.replace("$JAVA", userConfig.javaHome + "/bin/java");
            command.log(`Executing post-command: '${finalCommand}'`);
            execSshCommandWithDefaultEnv(finalCommand, zosTargetDir);
        }
    }
    // tslint:disable-next-line: no-console
    console.timeEnd("transferFile");
}
