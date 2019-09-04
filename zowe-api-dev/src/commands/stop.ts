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
                    this.log(`Cancelling job ${job.jobname} (${job.jobid})`);
                    const updatedJob = zoweSync(`jobs cancel job ${job.jobid}`).data as IJob;
                    saveLastJob(updatedJob);
                } else {
                    this.warn(`Cancelling job ${job.jobname} (${job.jobid}) is not active but in ${job.status} status`);
                }
            }
        }
    }
}
