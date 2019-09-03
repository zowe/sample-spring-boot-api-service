import Command from "@oclif/command";
import { readFileSync } from "fs";
import { join } from "path";

export interface IUserConfig {
    zosTargetDir: string;
    zosHlq: string;
    jobcard: [string];
}

export interface ITransferredFile {
    binary: boolean;
    template: boolean;
    target: string;
    postCommands: [string];
}

export interface IConfiguration {
    files: { [filename: string]: ITransferredFile };
}

export interface IProjectConfig {
    zosSourcesDir: string;
    buildCommand: string;
    buildFiles: { [filename: string]: string };
    deployment: { files: { [filename: string]: ITransferredFile } };
    configurations: { [id: string]: IConfiguration };
    shellStartCommand: string;
    jobTemplatePath: string;
    jobPath: string;
    defaultDirName: string;
    defaultHlqSegment: string;
    zfsMegabytes: number;
}

const missingProjectConfigHelp = "Change current working directory to a project with zowe-api.json";
const missingUserConfigHelp = "Use 'zowe-api init' to initialize it";

export function readConfiguration(command: Command, dir: string = "."): [IUserConfig, IProjectConfig] {
    const userConfigPath: string = join(dir, "user-zowe-api.json");
    const projectConfigPath: string = join(dir, "zowe-api.json");
    return [
        readConfigFile(userConfigPath, command, missingUserConfigHelp) as IUserConfig,
        readConfigFile(projectConfigPath, command, missingProjectConfigHelp) as IProjectConfig
    ];
}

export function readProjectConfiguration(command: Command, dir: string = "."): IProjectConfig {
    const projectConfigPath: string = join(dir, "zowe-api.json");
    return readConfigFile(projectConfigPath, command, missingProjectConfigHelp) as IProjectConfig;
}

function readConfigFile(path: string, command: Command, help: string): IUserConfig | IProjectConfig {
    try {
        return JSON.parse(readFileSync(path, "utf8"));
    } catch (e) {
        const code: string = e.code;
        if (code === "ENOENT") {
            command.error(`File '${path}' not found. ${help}`);
        }
        process.exit(1);
        throw e;
    }
}
