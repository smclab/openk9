/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import express from "express";
import { spawn } from "child_process";

const cmdPath = process.env.DOCKER_PATH || "/usr/bin/docker";

async function execWithPromise(
  command: string,
  params?: string[],
): Promise<string> {
  return new Promise(async (resolve, reject) => {
    const process = spawn(command, params);
    let result = "";
    process.stdout.on("data", (data) => {
      result += data.toString();
    });
    process.on("error", (err) => reject(err));
    process.on("close", () => resolve(result));
  });
}

const app = express();

app.get("/status", async (req, res) => {
  try {
    const results = await execWithPromise(cmdPath, [
      "ps",
      "--format",
      `{"ID":"{{ .ID }}", "Image": "{{ .Image }}", "Names":"{{ .Names }}", "Status":"{{ .Status }}"}`,
    ]);
    const lines = results.split("\n").filter(Boolean);
    res.json(JSON.parse(`[${lines.join(",")}]`));
  } catch (err) {
    console.error(err);
    res.status(500);
    res.send(JSON.stringify(err));
  }
});

app.get("/status/:id/:n", async (req, res) => {
  try {
    const results = await execWithPromise(cmdPath, [
      "logs",
      req.params.id,
      "--tail",
      req.params.n,
    ]);
    res.send(results);
  } catch (err) {
    console.error(err);
    res.status(500);
    res.send(JSON.stringify(err));
  }
});

app.listen(3005, "0.0.0.0", () => {
  console.log("Listening on port 3005");
});
