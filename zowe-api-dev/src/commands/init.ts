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
import { existsSync, writeFileSync } from "fs";
import * as logSymbols from "log-symbols";
import { resolve } from "path";
import { IUserConfig, readProjectConfiguration, userConfigFilename } from "../config";
import { checkZowe, execSshCommandWithDefaultEnvCwd, getDefaultProfile, trimProfileName, zoweSync } from "../zowe";

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
        javaLoadlib: flags.string({
            char: "l",
            default: "",
            description: "dataset with JVMLDM86 (SIEALNKE)",
            helpValue: "<dsn>"
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
        const projectConfig = readProjectConfiguration(this);

        checkZowe(this);

        if (f.force || !existsSync(userConfigFilename)) {
            this.log(`Initializing user configuration file for ${projectConfig.name}`);
            this.log("Getting information about your Zowe profile");
            const defaultZosmfProfile = getDefaultProfile("zosmf");
            if (f.account === "ACCT") {
                const defaultTsoProfile = getDefaultProfile("tso");
                const account = defaultTsoProfile.profile.account;
                if (account) {
                    f.account = account;
                } else {
                    this.log(logSymbols.warning, "Accounting information not found in your Zowe TSO profile. Please substitute 'ACCT' string with your accounting information in user-zowe-api.json manually.")
                }
            }
            const userid = defaultZosmfProfile.profile.user.toUpperCase();
            this.log(`Your user ID is ${userid}`);
            const jobname = userid.substring(0, 7) + "Z";
            const data: IUserConfig = {
                javaHome: f.javaHome || detectJavaHome(this),
                javaLoadlib: f.javaLoadlib,
                jobcard: [
                    `//${jobname} JOB ${f.account},'ZOWE API',MSGCLASS=A,CLASS=A,`,
                    "//  MSGLEVEL=(1,1),REGION=0M",
                    "/*JOBPARM SYSAFF=*"
                ],
                zosHlq: f.zosHlq || `${userid}.${projectConfig.defaultHlqSegment}`,
                zosTargetDir: f.zosTargetDir || zosUnixHomeDir() + "/" + projectConfig.defaultDirName,
                zoweProfileName: trimProfileName(defaultZosmfProfile.name)
            };
            const config = JSON.stringify(data, null, 4);
            writeFileSync(userConfigFilename, config);
            this.log(logSymbols.success, `Configuration initialized in: ${resolve(userConfigFilename)}`);
            this.log(config);
        } else {
            this.log(`Configuration already exists in: ${resolve(userConfigFilename)}`);
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
    const typeResult = execSshCommandWithDefaultEnvCwd("type java", { throwError: false });
    if (!typeResult.success && !typeResult.stdout) {
        zoweSync("profiles list ssh-profiles --show-contents");
        command.log(logSymbols.error, "The Zowe CLI has returned no output for a z/OS UNIX command. Check the port in your Zowe ssh profile. The typical SSH port is 22.");
        command.exit(1);
    }
    const type = typeResult.stdout.trim();
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
