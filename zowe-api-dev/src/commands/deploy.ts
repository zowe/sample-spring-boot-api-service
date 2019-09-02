import { Command } from "@oclif/command";
import { readConfiguration } from "../config";
import { transferFiles } from "../files";
import { checkZowe } from "../zowe";

export default class Deploy extends Command {
    static description = "deploy the API service artifacts to z/OS";

    async run() {
        const [userConfig, projectConfig] = readConfiguration();

        checkZowe(this);

        if (!projectConfig.deployment.files) {
            this.warn("Nothing to deploy");
        } else {
            transferFiles(projectConfig.deployment.files, userConfig.zosTargetDir);
        }
    }
}
