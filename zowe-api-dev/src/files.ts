import Command from "@oclif/command";
import { execSync } from 'child_process';
import * as Debug from "debug";
import { copyFileSync, existsSync, mkdirSync, readFileSync, writeFileSync } from "fs";
import * as Handlebars from "handlebars";
import { dirname, resolve } from "path";
import * as tmp from "tmp";
import { checkZoweProfileName, ITransferredFile, IUserConfig } from "./config";
import { execSshCommandWithDefaultEnv, execSshCommandWithDefaultEnvCwd, zoweSync } from "./zowe";

const debug = Debug("files");
const userDataDir = ".zowe-api-dev";

function cachedOldFilePath(zosFile: string, profileName: string) {
    const oldFile = resolve(".", userDataDir, "uploadedFiles", profileName, zosFile.substring(1));
    return oldFile;
}

function saveFileToOld(file: string, zosFile: string, profileName: string): void {
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

export function transferFiles(
    files: { [filename: string]: ITransferredFile },
    zosTargetDir: string,
    userConfig: IUserConfig,
    command: Command,
    force: boolean,
    context?: {},
) {
    console.time("transferFile");
    checkZoweProfileName(userConfig);
    for (const [file, options] of Object.entries(files)) {
        const zosFile = `${zosTargetDir}/${options.target}`;
        const zosDir = dirname(zosFile);
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
        } else if (file.endsWith(".jar")) {
            const jarpatcherPath = resolve(__dirname, "..", "lib", "jarpatcher.jar");
            const oldFile = cachedOldFilePath(zosFile, userConfig.zoweProfileName);
            debug("Old file: ", oldFile);
            if (!force && existsSync(oldFile)) {
                if (Buffer.compare(readFileSync(file), readFileSync(oldFile)) === 0) {
                    command.log(`${file} has not changed`)
                    return;
                }
                else {
                    command.log(`Patching ${zosFile} to be same as ${file}`);
                    const patchFile = file + "-patch";
                    const zosPatchFile = zosFile + "-patch";
                    const output = execSync(`java -cp ${jarpatcherPath} jarpatcher.JarPatcher diff ${oldFile} ${file} ${patchFile} ${jarpatcherPath}`, { encoding: "utf8" })
                    debug(output);
                    zoweSync(`files upload ftu ${patchFile} ${zosPatchFile} --binary`);
                    execSshCommandWithDefaultEnv(`${userConfig.javaHome}/bin/java  -cp ${zosPatchFile} jarpatcher.JarPatcher patch ${zosFile} ${zosPatchFile} jarpatcher`, zosTargetDir);
                    saveFileToOld(file, zosFile, userConfig.zoweProfileName);
                }
            }
            else {
                uploadFullFile(command, zosDir, file, zosFile, options, userConfig.zoweProfileName);
            }
        } else {
            uploadFullFile(command, zosDir, file, zosFile, options, userConfig.zoweProfileName);
        }
        for (const postCommand of options.postCommands || []) {
            let finalCommand = postCommand;
            if (postCommand.startsWith("java") && userConfig.javaHome) {
                finalCommand = userConfig.javaHome + "/bin/" + postCommand;
            }
            command.log(`Executing post-command: '${finalCommand}'`);
            execSshCommandWithDefaultEnv(finalCommand, zosTargetDir);
        }
    }
    console.timeEnd("transferFile");
}
