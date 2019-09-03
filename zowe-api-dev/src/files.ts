import Command from "@oclif/command";
import * as Debug from "debug";
import { readFileSync, writeFileSync } from "fs";
import * as Handlebars from "handlebars";
import { dirname } from "path";
import * as tmp from "tmp";
import { ITransferredFile, IUserConfig } from "./config";
import { zoweSync } from "./zowe";

const debug = Debug("files");

export function transferFiles(
    files: { [filename: string]: ITransferredFile },
    zosTargetDir: string,
    userConfig: IUserConfig,
    command: Command,
    context?: {}
) {
    for (const [file, options] of Object.entries(files)) {
        const zosFile = `${zosTargetDir}/${options.target}`;
        const zosDir = dirname(zosFile);
        command.log(`Making directory ${zosDir}`);
        zoweSync(`zos-uss issue ssh "mkdir -p ${zosDir}"`);
        command.log(`Uploading ${file} to ${zosFile}`);
        if (options.template) {
            const tmpPath = tmp.tmpNameSync();
            debug(tmpPath);
            const template = Handlebars.compile(readFileSync(file).toString(), { strict: true });
            const result = template(context);
            debug(result);
            writeFileSync(tmpPath, result);
            zoweSync(`files upload ftu ${tmpPath} ${zosFile}${options.binary ? " --binary" : ""}`);
        } else {
            zoweSync(`files upload ftu ${file} ${zosFile}${options.binary ? " --binary" : ""}`);
        }
        for (const postCommand of options.postCommands || []) {
            let finalCommand = postCommand;
            if (postCommand.startsWith("java") && userConfig.javaHome) {
                finalCommand = userConfig.javaHome + "/bin/" + postCommand;
            }
            command.log(`Executing post-command: '${finalCommand}'`);
            zoweSync(`zos-uss issue ssh "${finalCommand}" --cwd "${zosTargetDir}"`);
        }
    }
}
