import JSON5 from "json5";
import fs from "fs";
import path from "path";
import { Config } from "./doc/IConfig";

export function readConfiguration(dir: string = "."): Config {
    const configPath: string = path.join(dir, "deploy-config.json5");
    try {
        const confText = fs.readFileSync(configPath, "utf8");
        return JSON5.parse(confText);
    }
    catch (e) {
        const code: string = e['code'];
        if (code === 'ENOENT') {
            console.error(`File ${configPath} not found. Use 'npm run init' to initalize it`)
        }
        process.exit(1);
        throw e;
    }
}

if ((process.argv.length >= 3) && (process.argv[2] == "init")) {
    const configDir = (process.argv.length >= 4) ? process.argv[3] : ".";
    const configPath = path.join(configDir, "deploy-config.json5");
    const templatePath = path.resolve(__dirname, "..", "..", "templates", "deploy-config.template.json5");
    if (!fs.existsSync(configPath)) {
        fs.copyFileSync(templatePath, configPath);
        console.log(`Configuration initalized in: ${path.resolve(configPath)}`)
        console.log("You need to open the file in editor and update the values")
    }
}
