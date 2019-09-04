import { Command, flags } from "@oclif/command";
import * as Debug from "debug";
import { existsSync, writeFileSync } from "fs";
import * as logSymbols from "log-symbols";
import { resolve } from "path";
import { readProjectConfiguration } from "../config";
import { checkZowe, execSshCommandWithDefaultEnvCwd, zoweSync } from "../zowe";

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
        javaHome: flags.string({
            char: "j",
            default: "",
            description: "home of Java 8 on z/OS (JAVA_HOME)",
            helpValue: "<path>"
        }),
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
            this.log(`Initializing user configuration file for ${projectConfig.name}`);
            this.log("Getting information about your Zowe profile");
            const profiles = zoweSync("profiles list zosmf-profiles --show-contents").data as [
                { profile: { user: string } }
            ];
            const userid = profiles[0].profile.user.toUpperCase();
            this.log(`Your user ID is ${userid}`);
            const jobname = userid.substring(0, 7) + "Z";
            const data = {
                javaHome: validateJavaHome(f.javaHome, this) || detectJavaHome(this),
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
            this.log(logSymbols.success, `Configuration initialized in: ${resolve(configPath)}`);
            this.log(config);
        } else {
            this.log(`Configuration already exists in: ${resolve(configPath)}`);
        }
        this.log(logSymbols.info, "Use 'zowe-api-dev zfs' to allocate your zFS filesystem for development");
    }
}

function zosUnixHomeDir() {
    const words = execSshCommandWithDefaultEnvCwd("echo ~", { logOutput: false })
        .stdout.trim()
        .split(" ");
    return words[words.length - 1];
}

function validateJavaHome(javaHome: string, command: Command): string | null {
    const path = stripTrailingSlash(javaHome);
    if (path) {
        debug(path);
        command.log(`Validating JAVA_HOME ${path}`);
        if (
            execSshCommandWithDefaultEnvCwd(`${path}/bin/java -version`, { throwError: false }).stdout.indexOf(
                'java version "1.8.0_'
            ) > -1
        ) {
            return path;
        }
    }
    return null;
}

function stripTrailingSlash(path: string): string {
    return path.endsWith("/") ? path.slice(0, -1) : path;
}

function detectJavaHome(command: Command): string | null {
    const candidates = ["/sys/java64bt/v8r0m0/usr/lpp/java/J8.0_64", "/usr/lpp/java/J8.0_64"];
    const type = execSshCommandWithDefaultEnvCwd("type java").stdout.trim();
    const javaIs = "java is /";
    const javaIsIndex = type.indexOf(javaIs);
    if (javaIsIndex > -1) {
        const javaHome = type.substring(javaIsIndex + javaIs.length - 1).replace("/bin/java", "");
        if (candidates.indexOf(javaHome) === -1) {
            candidates.unshift(javaHome);
        }
    }
    debug(candidates);
    for (const candidate of candidates) {
        const validated = validateJavaHome(candidate, command);
        if (validated !== null) {
            command.log(`JAVA_HOME detected as ${validated}`);
            return validated;
        }
    }
    command.log(
        logSymbols.warning,
        "No JAVA_HOME detected. Please update the configuration file manually or add path to Java into your .profile on z/OS"
    );
    return null;
}
