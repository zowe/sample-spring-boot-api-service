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
        debugPort: flags.integer({ char: "d", description: "Enable remote debugging with the specified port", default: 0 }),
        job: flags.boolean({ char: "j", description: "Submit the Java application in a job" }),
        killPrevious: flags.boolean({ char: "k", description: "Kill all jobs with the same job name" })
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

            if (f.killPrevious) {
                const jobName = parseJobName(userConfig.jobcard);
                const jobList = zoweSync(`jobs list jobs --prefix ${jobName} --owner \\*`, {logOutput: false});
                debug(jobList);
                if (jobList) {
                    const jobs = jobList.data as IJob[];
                    for (const job of jobs) {
                        if (["ACTIVE", "INPUT"].includes(job.status)) {
                            this.log(`Canceling job ${job.jobname} (${job.jobid})`);
                            zoweSync(`jobs cancel job ${job.jobid}`);
                        }
                    }
                }
            }

            const jcl = template({ user: userConfig, project: projectConfig, debugPort: f.debugPort });
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
            startCommand = startCommand.replace("$JAVA", userConfig.javaHome + "/bin/java");
            if (f.debugPort) {
                startCommand = startCommand.replace("/java ", `/java -Xdebug -Xrunjdwp:server=y,suspend=n,transport=dt_socket,address=${f.debugPort} `);
            }
            this.log(
                `Starting application in SSH z/OS UNIX session using command '${startCommand}' in directory '${userConfig.zosTargetDir}'`
            );
            this.log(logSymbols.info, "You can stop it using Ctrl+C");
            execSshCommandWithDefaultEnv(startCommand, userConfig.zosTargetDir, {}, { direct: true });
        }
    }
}

function parseJobName(jobCard: string[]) {
    return jobCard[0].split(" ")[0].substr(2);
}
