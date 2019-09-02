import { readFileSync, writeFileSync } from "fs";
import * as Handlebars from "handlebars";
import { dirname } from "path";
import * as tmp from "tmp";
import { TransferredFile } from "./config";
import { zoweSync } from "./zowe";

const debug = require("debug")("files");

export function transferFiles(files: { [filename: string]: TransferredFile }, zosTargetDir: string, context?: {}) {
    for (const [file, options] of Object.entries(files)) {
        const zosFile = `${zosTargetDir}/${options.target}`;
        const zosDir = dirname(zosFile);
        console.log(`Making directory ${zosDir}`);
        zoweSync(`zos-uss issue ssh "mkdir -p ${zosDir}"`);
        console.log(`Uploading ${file} to ${zosFile}`);
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
        for (const command of options.postCommands || []) {
            zoweSync(`zos-uss issue ssh "${command}" --cwd "${zosTargetDir}"`);
        }
    }
}
