import { Command } from '@oclif/command'
import { readConfiguration } from '../config';
import { zoweSync } from '../zowe';

export default class Deploy extends Command {
    static description = 'start the API service on z/OS'

    async run() {
        const [userConfig, projectConfig] = readConfiguration()

        if (!projectConfig.shellStartCommand) {
            this.error("Nothing to start")
            this.exit(1)
        }

        zoweSync(`zos-uss issue ssh "${projectConfig.shellStartCommand}" --cwd "${userConfig.zosTargetDir}"`, true)
    }
}
