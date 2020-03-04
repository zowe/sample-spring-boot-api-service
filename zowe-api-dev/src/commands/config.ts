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
import * as logSymbols from "log-symbols";
import { IConfiguration, readConfiguration } from "../config";
import { transferFiles } from "../files";
import { checkZowe } from "../zowe";

export default class Config extends Command {
    static description = "configure the API service on z/OS";

    static flags = {
        name: flags.string({ char: "n", description: "configuration name" }),
        parameter: flags.string({ char: "p", description: "parameter (name=value)", multiple: true })
    };

    async run() {
        const f = this.parse(Config).flags;
        const [userConfig, projectConfig] = readConfiguration(this);

        checkZowe(this);

        if (!projectConfig.configurations) {
            this.error("There are no defined configurations for this project");
        }

        const configurationNames = Object.keys(projectConfig.configurations);
        if (!f.name || !configurationNames.includes(f.name)) {
            this.error(
                `Configuration name is missing or invalid. Available names are: ${configurationNames.join(", ")}`
            );
        } else {
            const context: { [key: string]: string } = {};
            for (const param of f.parameter || []) {
                const [key, value] = param.split("=");
                context[key] = value;
            }
            this.debug(context);
            const configuration: IConfiguration = projectConfig.configurations[f.name];
            transferFiles(configuration.files, userConfig.zosTargetDir, userConfig, this, false, context);
            this.log(logSymbols.success, "Configuration on z/OS completed");
            this.log(
                logSymbols.info,
                "Use 'zowe-api-dev start' or 'zowe-api-dev start --job' to start the application"
            );
        }
    }
}
