import { execSync } from 'child_process';
import { Command } from '@oclif/command';

const debug = require('debug')('zowe');

export interface ZoweResult {
    success: boolean,
    exitCode: number,
    message: string,
    stdout: string,
    stderr: string,
    data: {}
}

export interface ZoweOptions {
    direct?: boolean
    logOutput?: boolean
    throwError?: boolean
}

export function zoweSync(command: string, options?: ZoweOptions): ZoweResult {
    const default_options: ZoweOptions = { direct: false, logOutput: true, throwError: true }
    if (options === undefined) {
        options = default_options
    }
    const direct = (options.direct === undefined) ? default_options.direct : options.direct
    const logOutput = (options.logOutput === undefined) ? default_options.logOutput : options.logOutput
    const throwError = (options.throwError === undefined) ? default_options.throwError : options.throwError

    try {
        debug(command);
        if (!direct) {
            const json: string = execSync(`zowe --rfj ${command}`, { encoding: 'utf8' });
            const result: ZoweResult = JSON.parse(json);
            debug(result);
            if (logOutput) {
                logResult(result)
            }
            return result;
        }
        else {
            execSync(`zowe ${command}`, { stdio: 'inherit' });
            return { success: true, exitCode: 0, message: '', stdout: '', stderr: '', data: {} };
        }
    }
    catch (error) {
        debug(error);
        var result: ZoweResult
        try {
            result = JSON.parse(error.stdout)
            debug(result)
        }
        catch (error2) {
            throw error
        }

        if (throwError) {
            if (result) {
                throw Error(result.message || result.stderr || result.stdout)
            }
            else {
                throw error
            }
        }
        if (logOutput) {
            logResult(result)
        }
        return result
    }
}

function logResult(result: ZoweResult) {
    if (result.stdout.trim().length > 0) {
        console.log(result.stdout.trim())
    }
    if (result.stderr.trim().length) {
        console.log(result.stderr.trim())
    }
}

export function checkZowe(command: Command) {
    try {
        const zosmfProfiles = zoweSync('profiles list zosmf-profiles', {logOutput: false}).data as []
        if (zosmfProfiles.length == 0) {
            command.error('No zosmf-profile defined in Zowe CLI. Use "zowe profiles create zosmf-profile" to define it')
        }

        const sshProfiles = zoweSync('profiles list ssh-profiles', {logOutput: false}).data as []
        if (sshProfiles.length == 0) {
            command.error('No ssh-profile defined in Zowe CLI. Use "zowe profiles create ssh-profile" to define it')
        }
    }
    catch (error) {
        if (error.message.indexOf('command not found') > -1) {
            command.error('Zowe CLI is not installed. Use "npm install -g @zowe/cli" to install it')
        }
        else {
            throw error
        }
    }
}
