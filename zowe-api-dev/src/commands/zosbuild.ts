import { Command } from '@oclif/command'
import { existsSync, lstatSync, readdirSync } from 'fs'
import { zoweSync } from '../zowe';
import { readConfiguration } from '../config';

export default class Zosbuild extends Command {
    static description = 'build z/OS source on z/OS UNIX'

    async run() {
        const [userConfig, projectConfig] = readConfiguration()
        const zosDir = `${userConfig.zosTargetDir}/${projectConfig.zosSourcesDir}`
        zoweSync(`zos-uss issue ssh "mkdir -p ${zosDir}"`)
        uploadDir(projectConfig.zosSourcesDir, zosDir);
        zoweSync(`zos-uss issue ssh "${projectConfig.buildCommand}" --cwd "${zosDir}"`)
        for (const [zosFile, targetFile] of Object.entries(projectConfig.buildFiles)) {
            zoweSync(`files download uss-file ${zosDir}/${zosFile} --binary -f ${targetFile}`)
        }
    }
}

function uploadDir(dir: string, zosDir: string) {
    if (existsSync(dir) && lstatSync(dir).isDirectory()) {
        const files = readdirSync(dir);
        files.forEach((file) => {
            const targetFile = `${zosDir}/${file}`
            console.log(`Uploading ${file} to ${targetFile}`)
            zoweSync(`files upload ftu "${dir}/${file}" "${targetFile}"`)
        })
    }
    else {
        console.error(`Directory '${dir}' does not exist`)
    }
}
