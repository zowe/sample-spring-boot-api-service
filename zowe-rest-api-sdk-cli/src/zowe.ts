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

export function zoweSync(command: string, direct = false, throwError = true, logOutput = true): ZoweResult | null {
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
            return null;
        }
    }
    catch (error) {
        debug(error);
        if (error.status === 127) {
            throw error;
        }
        try {
            const result: ZoweResult = JSON.parse(error.stdout);
            debug(result);
            if (throwError) {
                throw Error(error.message);
            }
            if (logOutput) {
                logResult(result)
            }
            return result;
        }
        catch (error2) {
            throw error;
        }
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
