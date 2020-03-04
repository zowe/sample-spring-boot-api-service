/*
* This program and the accompanying materials are made available and may be used, at your option, under either:
* * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
* * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Copyright Contributors to the Zowe Project.
*/

import Command from "@oclif/command";
import { readFileSync, writeFileSync } from "fs";
import { join } from "path";
import { getDefaultProfile, trimProfileName } from './zowe';

export const userConfigFilename = "user-zowe-api.json";

export interface IUserConfig {
    zosTargetDir: string ;
    zosHlq: string;
    jobcard: string[];
    javaHome: string | null;
    javaLoadlib: string;
    zoweProfileName: string;
}

export interface ITransferredFile {
    binary: boolean;
    template: boolean;
    target: string;
    postCommands: string[];
    postSoUpdateCommands: string[];
}

export interface IConfiguration {
    files: { [filename: string]: ITransferredFile };
}

export interface IProjectConfig {
    name: string;
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
    const userConfigPath: string = join(dir, userConfigFilename);
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

export function checkZoweProfileName(userConfig: IUserConfig) {
    if (!userConfig.zoweProfileName) {
        userConfig.zoweProfileName = trimProfileName(getDefaultProfile("zosmf").name);
        const config = JSON.stringify(userConfig, null, 4);
        writeFileSync(userConfigFilename, config);
    }
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
