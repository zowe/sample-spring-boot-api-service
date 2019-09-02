import { Command, flags } from '@oclif/command';
import { existsSync, writeFileSync } from 'fs';
import { resolve } from 'path';
import { zoweSync } from '../zowe';
import { readProjectConfiguration } from '../config';

const debug = require('debug')('init')

export default class Init extends Command {
    static description = 'initialize user configuration file'

    static flags = {
        zosTargetDir: flags.string({ char: 't', default: '', helpValue: '<directory>', description: 'target z/OS UNIX directory' }),
        zosHlq: flags.string({ char: 'h', default: '', helpValue: '<HLQ>', description: 'target z/OS dataset HLQ' }),
        account: flags.string({ char: 'a', helpValue: '<account>', default: 'ACCT', description: 'JES account number' }),
        force: flags.boolean({ char: 'f', description: 'overwrite existing configuration' }),
    }

    async run() {
        const { args, flags } = this.parse(Init)
        const configPath = 'user-zowe-api.json'
        const projectConfig = readProjectConfiguration()

        if (flags.force || !existsSync(configPath)) {
            console.log('Getting information about your Zowe profile')
            const profiles = zoweSync('profiles list zosmf-profiles --show-contents').data as [{ profile: { user: string } }]
            const userid = profiles[0].profile.user.toUpperCase()
            console.log(`Your user ID is ${userid}`)
            const jobname = userid.substring(0, 7) + 'Z'
            const data = {
                "zosTargetDir": flags.zosTargetDir || zosUnixHomeDir() + '/' + projectConfig.defaultDirName,
                "zosHlq": flags.zosHlq || `${userid}.${projectConfig.defaultHlqSegment}`,
                "jobcard": [
                    `//${jobname} JOB ${flags.account},'ZOWE API',MSGCLASS=A,CLASS=A,`,
                    "//  MSGLEVEL=(1,1),REGION=0M",
                    "/*JOBPARM SYSAFF=*"
                ]
            }
            const config = JSON.stringify(data, null, 4);
            writeFileSync(configPath, config);
            console.log(`Configuration initalized in: ${resolve(configPath)}`)
            console.log(config)
        }
        else {
            console.log(`Configuration already exists in: ${resolve(configPath)}`)
        }
    }
}

function zosUnixHomeDir() {
    const words = zoweSync('zos-uss issue ssh "echo ~"').stdout.trim().split(' ')
    return words[words.length - 1]
}
