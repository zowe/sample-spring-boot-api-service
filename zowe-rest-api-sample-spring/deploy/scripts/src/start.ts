import { execSync } from "child_process";

let job = JSON.parse(execSync(`zowe jobs submit lf deploy/templates/jcl/out/samplapi.jcl --rfj --zosmf-p=ca32`).toString());

const intervalObj = setInterval(() => {
    console.log(`${job.data.jobname} is in status: ${job.data.status}`);
    if (job.data.status === "ACTIVE") {
        clearInterval(intervalObj);
        console.log(`API started at: https://usilca11.lvn.broadcom.net:1687/api/v1/wto`)

    } else if (job.data.status === "OUTPUT") {
        clearInterval(intervalObj);
    }

    job = JSON.parse(execSync(`zowe jobs view jsbj ${job.data.jobid} --rfj --zosmf-p=ca32`).toString());
}, 2000);
