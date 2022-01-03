import React from "react";
import * as monaco from "monaco-editor";
import * as TypeScript from "typescript";
import "./index.css";
import { Editor } from "./Editor";
// @ts-ignore
import srcText from "../../src/index.ts?raw";
// @ts-ignore
import exampleText from "../../src/example.ts?raw";
import { fetchToCurl } from "./fecthToCurl";

// link files together
monaco.languages.typescript.javascriptDefaults.setEagerModelSync(true);

const srcModel = monaco.editor.createModel(
  srcText,
  "typescript",
  new monaco.Uri().with({ path: "index.ts" }),
);
const scriptModel = monaco.editor.createModel(
  exampleText,
  "typescript",
  new monaco.Uri().with({ path: "playground.ts" }),
);
const resultModel = monaco.editor.createModel("", "javascript");

export function App() {
  return (
    <div
      style={{
        width: "100vw",
        height: "100vh",
        display: "grid",
        gridTemplateRows: "50% 50%",
        gridTemplateColumns: "50% 50%",
      }}
    >
      <Editor
        container={<div style={{ gridColumn: "1", gridRow: "1" }} />}
        model={srcModel}
        readOnly={true}
      />
      <Editor
        container={<div style={{ gridColumn: "2", gridRow: "1" }} />}
        model={scriptModel}
      />
      <Editor
        container={<div style={{ gridColumn: "1 / span 2", gridRow: "2" }} />}
        model={resultModel}
        readOnly={true}
      />
      <button
        onClick={() => run()}
        style={{ position: "fixed", bottom: "8px", right: "8px" }}
      >
        RUN
      </button>
    </div>
  );
}

function run() {
  resultModel.setValue("");
  const replaced1 = srcModel.getValue().replaceAll("export ", "");
  const replaced2 = scriptModel
    .getValue()
    .split("\n")
    .slice(1)
    .join("\n")
    .replaceAll("restApi.", "");
  const chained = replaced1 + replaced2;
  const transpiled = TypeScript.transpile(chained);
  const executable = new Function("fetch", transpiled);
  executable((a1: any, a2: any) => {
    const query = fetchToCurl(a1, a2);
    const result = fetch(a1, a2)
      .then((response) => response.json())
      .then((data) => JSON.stringify(data, null, 2));
    result.then((result) =>
      resultModel.setValue(
        resultModel.getValue() +
          "// " +
          query +
          "\n" +
          "console.log(" +
          result +
          ")" +
          "\n\n",
      ),
    );
    return fetch(a1, a2);
  });
}
