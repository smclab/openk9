import fs from "fs";
import path from "path";
import type { Plugin } from "vite";

export function noCustomOutput(): Plugin {
  let outDir: string;

  return {
    name: "no-custom-output",
    apply: "build",
    configResolved(config) {
      outDir = config.build.outDir;
    },
    writeBundle() {
      const files = fs.readdirSync(outDir);
      files.forEach((file) => {
        if (file.endsWith(".css") || file.endsWith(".ico")) {
          const filePath = path.join(outDir, file);
          fs.unlinkSync(filePath);
          console.log(`Deleted: ${file}`);
        }
      });
    },
  };
}
