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
import { clearLastJob, findJob, readLastJob, saveLastJob } from "../jes";

const debug = Debug("status");

export default class Status extends Command {
    static description = "get status of API service";

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
                this.log(`Job ${job.jobname} (${job.jobid}) is in ${job.status} status, retcode=${job.retcode}`);
            }
        }
    }
}
