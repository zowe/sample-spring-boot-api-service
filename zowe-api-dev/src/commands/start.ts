import { Command, flags } from "@oclif/command";
import * as Debug from "debug";
import { readFileSync, writeFileSync } from "fs";
import * as Handlebars from "handlebars";
import * as logSymbols from "log-symbols";
import { readConfiguration } from "../config";
import { IJob } from "../jes";
import { execSshCommandWithDefaultEnv, zoweSync } from "../zowe";

const debug = Debug("start");

export default class Start extends Command {
    static description = "start the API service on z/OS";

    static flags = {
        job: flags.boolean({ char: "j" })
    };

    async run() {
        const [userConfig, projectConfig] = readConfiguration(this);
        const f = this.parse(Start).flags;

        if (!projectConfig.shellStartCommand) {
            this.error("Nothing to start");
        }

        if (f.job) {
            this.log(`Writing JCL from ${projectConfig.jobTemplatePath} to ${projectConfig.jobPath}`);
            const template = Handlebars.compile(readFileSync(projectConfig.jobTemplatePath).toString(), {
                strict: true
            });
            const jcl = template({ user: userConfig, project: projectConfig });
            debug(jcl);
            writeFileSync(projectConfig.jobPath, jcl);
            this.log(`Submitting job ${projectConfig.jobPath}`);
            const result = zoweSync(`jobs submit lf ${projectConfig.jobPath}`);
            debug(result);
            if (result) {
                const job = result.data as IJob;
                writeFileSync("lastJob.json", JSON.stringify(result.data, null, 4));
                this.log(logSymbols.success, `Job ${job.jobname} (${job.jobid}) submitted`);
            } else {
                this.error("No job result returned");
            }
        } else {
            let startCommand = projectConfig.shellStartCommand;
            if (startCommand.startsWith("java") && userConfig.javaHome) {
                startCommand = userConfig.javaHome + "/bin/" + startCommand;
            }
            this.log(
                `Starting application in SSH z/OS UNIX session using command '${startCommand}' in directory '${userConfig.zosTargetDir}'`
            );
            this.log(logSymbols.info, "You can stop it using Ctrl+C");
            execSshCommandWithDefaultEnv(startCommand, userConfig.zosTargetDir, {}, { direct: true });
        }
    }
}
