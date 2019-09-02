import { Command, flags } from "@oclif/command";
import { readFileSync, writeFileSync } from "fs";
import * as Handlebars from "handlebars";
import { readConfiguration } from "../config";
import { zoweSync } from "../zowe";

const debug = require("debug")("start");

export default class Start extends Command {
    static description = "start the API service on z/OS";

    static flags = {
        job: flags.boolean({ char: "j" })
    };

    async run() {
        const [userConfig, projectConfig] = readConfiguration();
        const { args, flags } = this.parse(Start);

        if (!projectConfig.shellStartCommand) {
            this.error("Nothing to start");
        }

        if (flags.job) {
            console.log(`Writing JCL from ${projectConfig.jobTemplatePath} to ${projectConfig.jobPath}`);
            const template = Handlebars.compile(readFileSync(projectConfig.jobTemplatePath).toString(), {
                strict: true
            });
            const jcl = template({ user: userConfig, project: projectConfig });
            debug(jcl);
            writeFileSync(projectConfig.jobPath, jcl);
            console.log(`Submitting job ${projectConfig.jobPath}`);
            const result = zoweSync(`jobs submit lf ${projectConfig.jobPath}`);
            debug(result);
            if (result) {
                writeFileSync("lastJob.json", JSON.stringify(result.data, null, 4));
            } else {
                this.error("No job result returned");
            }
        } else {
            zoweSync(`zos-uss issue ssh "${projectConfig.shellStartCommand}" --cwd "${userConfig.zosTargetDir}"`, {
                direct: true
            });
        }
    }
}
