/*
* This program and the accompanying materials are made available and may be used, at your option, under either:
* * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
* * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Copyright Contributors to the Zowe Project.
*/

import { Command } from "@oclif/command";
import * as Debug from "debug";
import { clearLastJob, findJob, IJob, readLastJob, saveLastJob } from "../jes";
import { zoweSync } from "../zowe";

const debug = Debug("stop");

export default class Stop extends Command {
    static description = "stop the API service on z/OS";

    async run() {
        const lastJob = readLastJob();
        if (lastJob == null) {
            this.error("No job previously started from this directory");
        } else {
            const job = findJob(lastJob.jobid);
            if (job == null) {
                clearLastJob();
                this.error(`Job ${lastJob.jobname} (${lastJob.jobid}) not found. Removing the 'lastJob.json' file`);
            } else {
                debug(job);
                saveLastJob(job);
                if (job.status === "ACTIVE") {
                    this.log(`Stopping job ${job.jobname} (${job.jobid})`);
                    const updatedJob = zoweSync(`zos-console issue command "P ${job.jobname}"`).data as IJob;
                    saveLastJob(updatedJob);
                } else {
                    this.warn(`Stopping job ${job.jobname} (${job.jobid}) is not active but in ${job.status} status`);
                }
            }
        }
    }
}
