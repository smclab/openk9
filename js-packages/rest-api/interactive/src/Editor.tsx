import React from "react";
import * as monaco from "monaco-editor";
import { StaticServices } from "monaco-editor/esm/vs/editor/standalone/browser/standaloneServices";

// code importing
// @ts-ignore
self.MonacoEnvironment = {
  getWorkerUrl: function (_moduleId: any, label: string) {
    if (label === "json") {
      return "./json.worker.bundle.js";
    }
    if (label === "css" || label === "scss" || label === "less") {
      return "./css.worker.bundle.js";
    }
    if (label === "html" || label === "handlebars" || label === "razor") {
      return "./html.worker.bundle.js";
    }
    if (label === "typescript" || label === "javascript") {
      return "./ts.worker.bundle.js";
    }
    return "./editor.worker.bundle.js";
  },
};

// react component
export const Editor: React.FC<{
  container: React.ReactElement;
  model: monaco.editor.ITextModel;
  readOnly?: boolean;
}> = ({ container, model, readOnly }) => {
  const divRef = React.useRef<HTMLDivElement>(null);
  const editorRef = React.useRef<monaco.editor.IStandaloneCodeEditor>(null);
  React.useEffect(() => {
    if (divRef.current) {
      editorRef.current = monaco.editor.create(divRef.current);
    }
    return () => {
      editorRef.current.dispose();
    };
  }, []);
  React.useEffect(() => {
    editorRef.current.setModel(model);
    editorRef.current.updateOptions({ readOnly });
  }, [model]);
  return React.cloneElement(container, { ref: divRef });
};
