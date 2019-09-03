import { Command, flags } from "@oclif/command";
import * as Debug from "debug";
import { existsSync, writeFileSync } from "fs";
import { resolve } from "path";
import { readProjectConfiguration } from "../config";
import { checkZowe, zoweSync } from "../zowe";

const debug = Debug("init");

export default class Init extends Command {
    static description = "initialize user configuration file";

    static flags = {
        account: flags.string({
            char: "a",
            default: "ACCT",
            description: "JES account number",
            helpValue: "<account>"
        }),
        force: flags.boolean({ char: "f", description: "overwrite existing configuration" }),
        zosHlq: flags.string({ char: "h", default: "", helpValue: "<HLQ>", description: "target z/OS dataset HLQ" }),
        zosTargetDir: flags.string({
            char: "t",
            default: "",
            description: "target z/OS UNIX directory",
            helpValue: "<directory>"
        })
    };

    async run() {
        const f = this.parse(Init).flags;
        const configPath = "user-zowe-api.json";
        const projectConfig = readProjectConfiguration(this);

        checkZowe(this);

        if (f.force || !existsSync(configPath)) {
            this.log("Getting information about your Zowe profile");
            const profiles = zoweSync("profiles list zosmf-profiles --show-contents").data as [
                { profile: { user: string } }
            ];
            const userid = profiles[0].profile.user.toUpperCase();
            this.log(`Your user ID is ${userid}`);
            const jobname = userid.substring(0, 7) + "Z";
            const data = {
                jobcard: [
                    `//${jobname} JOB ${f.account},'ZOWE API',MSGCLASS=A,CLASS=A,`,
                    "//  MSGLEVEL=(1,1),REGION=0M",
                    "/*JOBPARM SYSAFF=*"
                ],
                zosHlq: f.zosHlq || `${userid}.${projectConfig.defaultHlqSegment}`,
                zosTargetDir: f.zosTargetDir || zosUnixHomeDir() + "/" + projectConfig.defaultDirName
            };
            const config = JSON.stringify(data, null, 4);
            writeFileSync(configPath, config);
            this.log(`Configuration initialized in: ${resolve(configPath)}`);
            this.log(config);
        } else {
            this.log(`Configuration already exists in: ${resolve(configPath)}`);
        }
    }
}

function zosUnixHomeDir() {
    const words = zoweSync('zos-uss issue ssh "echo ~"')
        .stdout.trim()
        .split(" ");
    return words[words.length - 1];
}
