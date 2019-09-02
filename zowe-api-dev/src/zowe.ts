import { execSync } from 'child_process';

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
    const direct = options.direct || default_options.direct
    const logOutput = options.logOutput || default_options.logOutput
    const throwError = options.direct || default_options.throwError

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
        console.log(result.stdout)
    }
    if (result.stderr.trim().length) {
        console.log(result.stderr)
    }
}
