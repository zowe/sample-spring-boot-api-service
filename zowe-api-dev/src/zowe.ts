import { Command } from "@oclif/command";
import { execSync } from "child_process";
import * as Debug from "debug";

const debug = Debug("zowe");

export interface IZoweResult {
    success: boolean;
    exitCode: number;
    message: string;
    stdout: string;
    stderr: string;
    data: {};
}

export interface IZoweOptions {
    direct?: boolean;
    logOutput?: boolean;
    throwError?: boolean;
}

export interface IApiResponse {
    apiResponse: {
        items: [];
        totalRows: number;
        returnedRows: number;
        JSONversion: number;
    }
}

export function zoweSync(command: string, options?: IZoweOptions): IZoweResult {
    const defaultOptions: IZoweOptions = { direct: false, logOutput: true, throwError: true };
    if (options === undefined) {
        options = defaultOptions;
    }
    const direct = options.direct === undefined ? defaultOptions.direct : options.direct;
    const logOutput = options.logOutput === undefined ? defaultOptions.logOutput : options.logOutput;
    const throwError = options.throwError === undefined ? defaultOptions.throwError : options.throwError;

    try {
        debug(command);
        if (!direct) {
            const json: string = execSync(`zowe --rfj ${command}`, { encoding: "utf8" });
            if (!json) {
                throw { stdout: "" }
            }
            const result: IZoweResult = JSON.parse(json);
            debug(result);
            if (logOutput) {
                logResult(result);
            }
            return result;
        } else {
            execSync(`zowe ${command}`, { stdio: "inherit" });
            return { success: true, exitCode: 0, message: "", stdout: "", stderr: "", data: {} };
        }
    } catch (error) {
        debug(error);
        let result: IZoweResult;
        try {
            result = error.stdout ? JSON.parse(error.stdout) : {
                data: {},
                exitCode: -1,
                message: "empty JSON response from Zowe CLI",
                stderr: "",
                stdout: "",
                success: false
            };
            debug(result);
        } catch (error2) {
            throw error;
        }

        if (throwError) {
            if (result) {
                throw Error(result.message || result.stderr || result.stdout);
            } else {
                throw error;
            }
        }
        if (logOutput) {
            logResult(result);
        }
        return result;
    }
}

function logResult(result: IZoweResult) {
    if (result.stdout.trim().length > 0) {
        process.stdout.write(result.stdout.trim());
        process.stdout.write("\n");
    }
    if (result.stderr.trim().length > 0) {
        process.stdout.write(result.stderr.trim());
        process.stdout.write("\n");
    }
}

export function checkZowe(command: Command) {
    try {
        const zosmfProfiles = zoweSync("profiles list zosmf-profiles", { logOutput: false }).data as [];
        if (zosmfProfiles.length === 0) {
            command.error(
                'No zosmf-profile defined in Zowe CLI. Use "zowe profiles create zosmf-profile" to define it'
            );
        }

        const sshProfiles = zoweSync("profiles list ssh-profiles", { logOutput: false }).data as [];
        if (sshProfiles.length === 0) {
            command.error('No ssh-profile defined in Zowe CLI. Use "zowe profiles create ssh-profile" to define it');
        }
    } catch (error) {
        if (error.message.indexOf("command not found") > -1) {
            command.error('Zowe CLI is not installed. Use "npm install -g @zowe/cli" to install it');
        } else {
            throw error;
        }
    }
}

export function trimProfileName(profileName: string): string {
    return profileName.replace("(default)", "").trim();
}

export function getDefaultProfile(profileType: string) {
    const profiles = zoweSync(`profiles list ${profileType}-profiles --show-contents`, { logOutput: false }).data as [
        { name: string; profile: { user: string, password: string, host: string, port: number, rejectUnauthorized: boolean, account: string } }
    ];
    let defaultProfile = profiles[0];
    for (const profile of profiles) {
        if (profile.name.indexOf('(default)') > -1) {
            defaultProfile = profile;
        }
    }
    return defaultProfile;
}

export function execSshCommands(
    commands: [string],
    cwd: string,
    env?: { [name: string]: string },
    options?: IZoweOptions
) {
    for (const command of commands) {
        execSshCommand(command, cwd, env, options);
    }
}

export function execSshCommand(
    command: string,
    cwd: string | null,
    env?: { [name: string]: string },
    options?: IZoweOptions
) {
    let environmentCommand = "";
    if (env) {
        for (const [name, value] of Object.entries(env)) {
            environmentCommand += `export ${name}="${value}"; `;
        }
    }
    process.stdout.write(`Executing z/OS UNIX command '${command}'` + (cwd ? ` in directory ${cwd}` : "") + "\n");
    debug(environmentCommand);
    return zoweSync(`zos-uss issue ssh "${environmentCommand}${command}"` + (cwd ? ` --cwd "${cwd}"` : ""), options);
}

export function execSshCommandWithDefaultEnv(
    command: string,
    cwd: string,
    env?: { [name: string]: string },
    options?: IZoweOptions
) {
    return execSshCommand(command, cwd, { ...env, ...defaultUssEnv }, options);
}

export function execSshCommandWithDefaultEnvCwd(command: string, options?: IZoweOptions) {
    return execSshCommand(command, null, defaultUssEnv, options);
}

export const defaultUssEnv = { _EDC_ADD_ERRNO2: "1", _BPXK_JOBLOG: "STDERR" };
