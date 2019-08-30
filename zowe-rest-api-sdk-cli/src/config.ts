import { readFileSync } from "fs";
import { join } from "path";

export interface UserConfig {
    zosTargetDir: string
}

export interface DeployedFile {
    binary: boolean
    target: string
    postCommands: [string]
}

export interface ProjectConfig {
    zosSourcesDir: string
    buildCommand: string
    buildFiles: {string: string}
    deployFiles: {string: DeployedFile}
    shellStartCommand: string
}

export function readConfiguration(dir: string = "."): [UserConfig, ProjectConfig] {
    const userConfigPath: string = join(dir, "user-zowe-api.json")
    const projectConfigPath: string = join(dir, "zowe-api.json")
    return [readConfigFile(userConfigPath) as UserConfig, readConfigFile(projectConfigPath) as ProjectConfig]
}

function readConfigFile(path: string): UserConfig | ProjectConfig {
    try {
        return JSON.parse(readFileSync(path, "utf8"))
    }
    catch (e) {
        const code: string = e['code']
        if (code === 'ENOENT') {
            console.error(`File '${path}' not found. Use 'zowe-api init' to initalize it`)
        }
        process.exit(1)
        throw e
    }
}
