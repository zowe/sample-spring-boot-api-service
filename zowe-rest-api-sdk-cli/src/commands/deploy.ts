import { Command } from '@oclif/command'
import { readConfiguration } from '../config';
import { zoweSync } from '../zowe';

export default class Deploy extends Command {
    static description = 'deploy the API service artifacts to z/OS'

    async run() {
        const [userConfig, projectConfig] = readConfiguration()

        if (!projectConfig.deployFiles) {
            this.warn("Nothing to deploy")
            this.exit(1)
        }

        for (const [file, options] of Object.entries(projectConfig.deployFiles)) {
            const zosFile = `${userConfig.zosTargetDir}/${options.target}`;
            console.log(`Deploying ${file} to ${zosFile}`);
            zoweSync(`files upload ftu ${file} ${zosFile}${options.binary ? ' --binary' : ''}`)
            for (const command of options.postCommands) {
                zoweSync(`zos-uss issue ssh "${command}" --cwd "${userConfig.zosTargetDir}"`)
            }
        }
    }
}
