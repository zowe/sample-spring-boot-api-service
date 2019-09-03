import { Command, flags } from "@oclif/command";
import * as Debug from "debug";
import * as logSymbols from "log-symbols";
import { readConfiguration } from "../config";
import { checkZowe, zoweSync } from "../zowe";

const debug = Debug("zfs");

interface IMountPoint {
    path: string;
    filesystem: string;
}

export default class Zfs extends Command {
    static description = "initialize user configuration file";

    static flags = {
        defineParams: flags.string({
            char: "p",
            default: "",
            description: "zfsadm define parameters (-storageclass...)",
            helpValue: "<parameters>"
        }),
        delete: flags.boolean({ char: "d" }),
        unmount: flags.boolean({ char: "u" })
    };

    async run() {
        const f = this.parse(Zfs).flags;
        const [user, project] = readConfiguration(this);
        const zfsDsn = user.zosHlq + ".ZFS";

        checkZowe(this);

        if (f.unmount) {
            this.log(`Checking mount point ${user.zosTargetDir}`);
            const mp = mountPoint(user.zosTargetDir);
            if (mp && mp.path === user.zosTargetDir && mp.filesystem === zfsDsn) {
                this.log(`Unmounting filesystem ${zfsDsn} at mount point ${user.zosTargetDir}`);
                zoweSync(`zos-uss issue ssh "/usr/sbin/unmount ${user.zosTargetDir}"`);
                this.log(`${mp.filesystem} is no longer mounted at ${mp.path}`);
            } else {
                this.error(`${zfsDsn} is not mounted at ${user.zosTargetDir}`);
            }
        }

        if (f.delete) {
            this.log(`Deleting zFS filesystem ${zfsDsn}`);
            zoweSync(`zos-files delete vsam ${zfsDsn} -f`);
        }

        if (!f.delete && !f.unmount) {
            this.log("Listing existing datasets");
            const data = zoweSync(`zos-files list data-set "${zfsDsn}"`).data as { apiResponse: { items: [] } };
            if (data.apiResponse.items.length > 0) {
                this.warn(`Dataset with name '${zfsDsn}' already exists`);
            } else {
                this.log(`Allocating zFS filesystem ${zfsDsn}`);
                zoweSync(
                    `zos-uss issue ssh "zfsadm define -aggregate ${zfsDsn} -megabytes ${project.zfsMegabytes} ${project.zfsMegabytes} ${f.defineParams}"`
                );
                this.log(`Formatting zFS filesystem ${zfsDsn}`);
                zoweSync(`zos-uss issue ssh "zfsadm format -aggregate ${zfsDsn}"`);
            }

            zoweSync(`zos-uss issue ssh "mkdir -p ${user.zosTargetDir}"`);
            const mp = mountPoint(user.zosTargetDir);
            if (mp === null || mp.path !== user.zosTargetDir) {
                this.log(`Mounting zFS filesystem ${zfsDsn} to ${user.zosTargetDir}`);
                zoweSync(`zos-uss issue ssh "/usr/sbin/mount -v -o aggrgrow -f ${zfsDsn} ${user.zosTargetDir}"`);
                this.log(logSymbols.success, `${zfsDsn} mounted at ${user.zosTargetDir}`);
            } else {
                this.warn(`${mp.filesystem} already mounted at ${mp.path}`);
            }
        }
    }
}

function mountPoint(path: string): IMountPoint | null {
    const regex = /.*\n(.+) \(([^)]+)\).*/g;
    try {
        const stdout = zoweSync(`zos-uss issue ssh "df ${path}"`).stdout;
        const matches = regex.exec(stdout);
        if (matches) {
            const mp: IMountPoint = { path: matches[1], filesystem: matches[2] };
            debug(mp);
            return mp;
        }
    } catch (error) {
        debug(error);
    }
    return null;
}
