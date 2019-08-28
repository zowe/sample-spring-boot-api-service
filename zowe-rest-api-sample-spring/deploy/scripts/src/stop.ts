import { execSync } from "child_process";

const allJobs: string[] = execSync(`zowe jobs list jobs --rft table --rff jobname status jobid --zosmf-p=ca32`).toString().split(`\n`);
allJobs.forEach((jobEntry) => {
    const tokenized = jobEntry.split(" ");
    if (tokenized[0] === "SAMPLAPI") {
        if (tokenized[1] === "ACTIVE") {
            console.log(execSync(`zowe jobs cancel job ${tokenized[2]} --zosmf-p=ca32`).toString())
        }
    }
});
