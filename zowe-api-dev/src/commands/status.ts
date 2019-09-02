import { Command } from '@oclif/command';
import { clearLastJob, findJob, readLastJob, saveLastJob } from '../jes';

const debug = require('debug')('status')

export default class Status extends Command {
    static description = 'get status of API service'

    async run() {
        const lastJob = readLastJob()
        if (lastJob == null) {
            this.error('No job previously started from this directory')
        }
        else {
            const job = findJob(lastJob.jobid)
            if (job == null) {
                clearLastJob()
                this.error(`Job ${lastJob.jobname} (${lastJob.jobid}) not found. Removing the 'lastJob.json' file`)
            }
            else {
                debug(job)
                saveLastJob(job)
                console.log(`Job ${job.jobname} (${job.jobid}) is in ${job.status} status, retcode=${job.retcode}`)
            }
        }
    }
}
