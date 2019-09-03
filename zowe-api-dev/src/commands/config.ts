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
            transferFiles(configuration.files, userConfig.zosTargetDir, this, context);
            this.log(logSymbols.success, "Configuration on z/OS completed");
        }
    }
}
