import { readFileSync } from "fs";
import { join } from "path";

export interface UserConfig {
    zosTargetDir: string;
    zosHlq: string;
    jobcard: [string];
}

export interface TransferredFile {
    binary: boolean;
    template: boolean;
    target: string;
    postCommands: [string];
}

export interface Configuration {
    files: { [filename: string]: TransferredFile };
}

export interface ProjectConfig {
    zosSourcesDir: string;
    buildCommand: string;
    buildFiles: { [filename: string]: string };
    deployment: { files: { [filename: string]: TransferredFile } };
    configurations: { [id: string]: Configuration };
    shellStartCommand: string;
    jobTemplatePath: string;
    jobPath: string;
    defaultDirName: string;
    defaultHlqSegment: string;
    zfsMegabytes: number;
}

export function readConfiguration(dir: string = "."): [UserConfig, ProjectConfig] {
    const userConfigPath: string = join(dir, "user-zowe-api.json");
    const projectConfigPath: string = join(dir, "zowe-api.json");
    return [readConfigFile(userConfigPath) as UserConfig, readConfigFile(projectConfigPath) as ProjectConfig];
}

export function readProjectConfiguration(dir: string = "."): ProjectConfig {
    const projectConfigPath: string = join(dir, "zowe-api.json");
    return readConfigFile(projectConfigPath) as ProjectConfig;
}

function readConfigFile(path: string): UserConfig | ProjectConfig {
    try {
        return JSON.parse(readFileSync(path, "utf8"));
    } catch (e) {
        const code: string = e["code"];
        if (code === "ENOENT") {
            console.error(`File '${path}' not found. Use 'zowe-api init' to initalize it`);
        }
        process.exit(1);
        throw e;
    }
}
