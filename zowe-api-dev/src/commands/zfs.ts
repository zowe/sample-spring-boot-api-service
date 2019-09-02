import { Command, flags } from "@oclif/command";
import { readConfiguration } from "../config";
import { zoweSync, checkZowe } from "../zowe";

const debug = require("debug")("zfs");

interface MountPoint {
    path: string;
    filesystem: string;
}

export default class Zfs extends Command {
    static description = "initialize user configuration file";

    static flags = {
        defineParams: flags.string({
            char: "p",
            default: "",
            helpValue: "<parameters>",
            description: "zfsadm define parameters (-storageclass...)"
        }),
        delete: flags.boolean({ char: "d" }),
        unmount: flags.boolean({ char: "u" })
    };

    async run() {
        const { args, flags } = this.parse(Zfs);
        const [user, project] = readConfiguration();
        const zfsDsn = user.zosHlq + ".ZFS";

        checkZowe(this);

        if (flags.unmount) {
            const mp = mountPoint(user.zosTargetDir);
            if (mp && mp.path == user.zosTargetDir && mp.filesystem == zfsDsn) {
                console.log(`Unmounting filesystem ${zfsDsn} at mount point ${user.zosTargetDir}`);
                zoweSync(`zos-uss issue ssh "/usr/sbin/unmount ${user.zosTargetDir}"`);
                console.log(`${mp.filesystem} is no longer mounted at ${mp.path}`);
            } else {
                this.error(`${zfsDsn} is not mounted at ${user.zosTargetDir}`);
            }
        }

        if (flags.delete) {
            console.log(`Deleting zFS filesystem ${zfsDsn}`);
            zoweSync(`zos-files delete vsam ${zfsDsn} -f`);
        }

        if (!flags.delete && !flags.unmount) {
            console.log("Listing existing datasets");
            const data = zoweSync(`zos-files list data-set "${zfsDsn}"`).data as { apiResponse: { items: [] } };
            if (data.apiResponse.items.length > 0) {
                this.warn(`Dataset with name '${zfsDsn}' already exists`);
            } else {
                console.log(`Allocating zFS filesystem ${zfsDsn}`);
                zoweSync(
                    `zos-uss issue ssh "zfsadm define -aggregate ${zfsDsn} -megabytes ${project.zfsMegabytes} ${project.zfsMegabytes} ${flags.defineParams}"`
                );
                console.log(`Formatting zFS filesystem ${zfsDsn}`);
                zoweSync(`zos-uss issue ssh "zfsadm format -aggregate ${zfsDsn}"`);
            }

            zoweSync(`zos-uss issue ssh "mkdir -p ${user.zosTargetDir}"`);
            const mp = mountPoint(user.zosTargetDir);
            if (mp === null || mp.path != user.zosTargetDir) {
                console.log(`Mounting zFS filesystem ${zfsDsn} to ${user.zosTargetDir}`);
                zoweSync(`zos-uss issue ssh "/usr/sbin/mount -v -o aggrgrow -f ${zfsDsn} ${user.zosTargetDir}"`);
                console.log(`${zfsDsn} mounted at ${user.zosTargetDir}`);
            } else {
                this.warn(`${mp.filesystem} already mounted at ${mp.path}`);
            }
        }
    }
}

function mountPoint(path: string): MountPoint | null {
    const regex = /.*\n(.+) \(([^)]+)\).*/g;
    try {
        const stdout = zoweSync(`zos-uss issue ssh "df ${path}"`).stdout;
        const matches = regex.exec(stdout);
        if (matches) {
            const mp: MountPoint = { path: matches[1], filesystem: matches[2] };
            debug(mp);
            return mp;
        }
    } catch (error) {}
    return null;
}
