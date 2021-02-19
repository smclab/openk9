import express from "express";
import { spawn } from "child_process";

const cmdPath = process.env.DOCKER_PATH || "/usr/local/bin/docker";

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
