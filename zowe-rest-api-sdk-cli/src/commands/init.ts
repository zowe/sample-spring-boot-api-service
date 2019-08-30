import { Command, flags } from '@oclif/command'
import { writeFileSync, existsSync } from 'fs';
import { resolve } from 'path';

export default class Init extends Command {
    static description = 'initialize user configuration file'

    static flags = {
        zosTargetDir: flags.string({ char: 't', default: '/u/ibmuser/samplapi', helpLabel: '<dir>', description: 'target z/OS Unix directory' }),
        force: flags.boolean({ char: 'f' }),
    }

    async run() {
        const { args, flags } = this.parse(Init)
        const configPath = 'user-zowe-api.json'

        if (flags.force || !existsSync(configPath)) {
            const data = {
                "zosTargetDir": flags.zosTargetDir
            }
            writeFileSync(configPath, JSON.stringify(data, null, 4));
            console.log(`Configuration initalized in: ${resolve(configPath)}`)
        }
        else {
            console.log(`Configuration already exists in: ${resolve(configPath)}`)
        }
    }
}
