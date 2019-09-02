import { Command, flags } from '@oclif/command';
import { readConfiguration, Configuration } from '../config';
import { transferFiles } from '../files';
import { checkZowe } from '../zowe';

export default class Config extends Command {
    static description = 'configure the API service on z/OS'

    static flags = {
        name: flags.string({ char: 'n', description: 'configuration name' }),
        parameter: flags.string({ char: 'p', description: 'parameter (name=value)', multiple: true }),
    }

    async run() {
        const { args, flags } = this.parse(Config)
        const [userConfig, projectConfig] = readConfiguration()

        checkZowe(this)

        if (!projectConfig.configurations) {
            this.error('There are no defined configurations for this project')
        }

        const configurationNames = Object.keys(projectConfig.configurations)
        if (!flags.name || !configurationNames.includes(flags.name)) {
            this.error(`Configuration name is missing or invalid. Available names are: ${configurationNames.join(', ')}`)
        }
        else {
            let context: { [key: string]: string } = {}
            for (const param of flags.parameter || []) {
                const [key, value] = param.split('=')
                context[key] = value
            }
            this.debug(context)
            const configuration: Configuration = projectConfig.configurations[flags.name]
            transferFiles(configuration.files, userConfig.zosTargetDir, context)
        }
    }
}
