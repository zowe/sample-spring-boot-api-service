import * as Debug from "debug";
import { existsSync, readFileSync, unlinkSync, writeFileSync } from "fs";
import { zoweSync } from "./zowe";

const debug = Debug("jes");

export interface IJob {
    jobname: string;
    jobid: string;
    status: string;
    retcode: string | null;
    owner: string;
}

const lastJobPath = "lastJob.json";

export function readLastJob(): IJob | null {
    if (existsSync(lastJobPath)) {
        const job = JSON.parse(readFileSync(lastJobPath, "utf8")) as IJob;
        debug(job);
        return job;
    }
    return null;
}

export function clearLastJob() {
    unlinkSync(lastJobPath);
}

export function saveLastJob(job: IJob) {
    writeFileSync(lastJobPath, JSON.stringify(job, null, 4));
}

export function findJob(jobid: string): IJob | null {
    try {
        const job = zoweSync(`jobs view job-status-by-jobid ${jobid}`).data as IJob;
        debug(job);
        return job;
    } catch (error) {
        if (error.message.indexOf("Job not found") > -1) {
            return null;
        } else {
            throw error;
        }
    }
}
