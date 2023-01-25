import React from "react";
import * as monaco from "monaco-editor";
import { BaseInputProps } from "./Form";
import prettier from "prettier/standalone";
import parserTypeScript from "prettier/parser-typescript";

// NICE TO HAVE: colorize JSX (see https://github.com/cancerberoSgx/jsx-alone/blob/master/jsx-explorer/HOWTO_JSX_MONACO.md)

monaco.editor.defineTheme("clayui", {
  base: "vs",
  inherit: true,
  rules: [],
  colors: {
    "editor.background": "#f1f2f5",
  },
});

monaco.languages.typescript.typescriptDefaults.setCompilerOptions({
  jsx: monaco.languages.typescript.JsxEmit.Preserve,
  target: monaco.languages.typescript.ScriptTarget.ES2020,
  esModuleInterop: true,
});

// monaco.languages.typescript.typescriptDefaults.setDiagnosticsOptions({
//   noSemanticValidation: false,
//   noSyntaxValidation: false,
// });

(async () => {
  const response = await fetch("https://unpkg.com/@types/react@18.0.24/index.d.ts");
  const data = await response.text();
  monaco.languages.typescript.typescriptDefaults.addExtraLib(data, `file:///node_modules/@react/types/index.d.ts`);
  monaco.languages.typescript.typescriptDefaults.addExtraLib(templateTypeDefinition(), "./template.d.ts");
})();

for (const language of ["javascript", "typescript", "json"]) {
  monaco.languages.registerDocumentFormattingEditProvider(language, {
    provideDocumentFormattingEdits(model, options) {
      const formatted = prettier.format(model.getValue(), { parser: "typescript", plugins: [parserTypeScript] });
      return [
        {
          range: model.getFullModelRange(),
          text: formatted,
        },
      ];
    },
  });
}

let nextUriId = 1;
function createInMemoriUri(language: MonacoLanguage) {
  switch (language) {
    case "javascript":
      return `${nextUriId++}.js`;
    case "javascript-react":
      return `${nextUriId++}.jsx`;
    case "typescript":
      return `${nextUriId++}.ts`;
    case "typescript-react":
      return `${nextUriId++}.tsx`;
    case "json":
      return `${nextUriId++}.json`;
    case "text":
      return `${nextUriId++}.txt`;
    case undefined:
      return `${nextUriId++}`;
    default:
      throw new Error();
  }
}

type MonacoLanguage = "javascript" | "javascript-react" | "typescript" | "typescript-react" | "json" | "text" | undefined;

function labelPostfix(language: MonacoLanguage) {
  switch (language) {
    case "javascript":
      return "(JavaScript)";
    case "javascript-react":
      return "(JavaScript + React)";
    case "typescript":
      return "(TypeScript)";
    case "typescript-react":
      return "(TypeScript + React)";
    case "json":
      return "(JSON)";
    case "text":
      return "(Text)";
    case undefined:
      return "(Code)";
    default:
      throw new Error();
  }
}

function remapLanguage(language: MonacoLanguage) {
  switch (language) {
    case "javascript-react":
      return "javascript";
    case "typescript-react":
      return "typescript";
    default:
      return language;
  }
}

export function CodeInput({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  language,
  height = "200px",
  readonly,
}: BaseInputProps<string> & {
  language: MonacoLanguage;
  height?: string;
  readonly?: boolean;
}) {
  const editorElementRef = React.useRef<HTMLDivElement>(null);
  const editorRef = React.useRef<{ editor?: monaco.editor.IStandaloneCodeEditor; model?: monaco.editor.ITextModel }>({});
  React.useEffect(() => {
    if (editorElementRef.current) {
      const editor = monaco.editor.create(
        editorElementRef.current,
        { readOnly: readonly },
        { minimap: { enabled: false }, theme: "clayui" }
      );
      editor.updateOptions({ tabSize: 2 });
      editor.onDidBlurEditorText(() => {
        if (editorRef.current.model) {
          editor.getAction("editor.action.formatDocument").run();
          onChange(editorRef.current.model.getValue());
        }
      });
      editorRef.current.editor = editor;
      return () => {
        editor.dispose();
      };
    }
  }, [onChange]);
  React.useEffect(() => {
    if (editorRef.current.editor) {
      const model = monaco.editor.createModel(
        value,
        remapLanguage(language),
        monaco.Uri.from({ scheme: "inmemory", path: createInMemoriUri(language) })
      );
      editorRef.current.editor.setModel(model);
      editorRef.current.model = model;
      return () => {
        model.dispose();
      };
    }
  }, [language, value, onChange]);
  return (
    <div className={`form-group ${validationMessages.length ? "has-warning" : ""}`}>
      <label htmlFor={id}>
        {label} {labelPostfix(language)}
      </label>
      <div className="form-control">
        <div ref={editorElementRef} style={{ height }} />
      </div>
      <div className="form-feedback-group">
        {validationMessages.map((validationMessage, index) => {
          return (
            <div key={index} className="form-feedback-item">
              {validationMessage}
            </div>
          );
        })}
      </div>
    </div>
  );
}

function templateTypeDefinition() {
  return `
    export {} 
    declare global {
      type Template<E> = {
        resultType: string;
        priority: number;
        result: React.FC<ResultRendererProps<E>>;
        detail: React.FC<DetailRendererProps<E>>;
      };
      var rendererComponents: any;
    }
    type ResultRendererProps<E> = {
      result: GenericResultItem<E>;
    };
    
    type DetailRendererProps<E> = {
      result: GenericResultItem<E>;
    };
    
    type GenericResultItem<E = {}> = {
      source: {
        documentTypes: (keyof E)[];
        contentId: string;
        id: string;
        parsingDate: number;
        rawContent: string;
        tenantId: number;
        datasourceId: number;
        entities?: Array<{
          entityType: string;
          context: DeepKeys<Without<GenericResultItem<E>["source"], "entities">>[];
          id: string;
        }>;
        resources: {
          binaries: {
            id: string;
            name: string;
            contentType: string;
          }[];
        };
      } & E;
      highlight: {
        [field in GenericResultItemFields<E>]?: string[];
      };
    };
    
    type GenericResultItemFields<E> = DeepKeys<Without<GenericResultItem<E>["source"], "type" | "entities">>;
    
    type PathImpl<T, Key extends keyof T> = Key extends string
      ? T[Key] extends Record<string, any>
        ? \`\${Key}.\${PathImpl<T[Key], Exclude<keyof T[Key], keyof any[]>> & string}\` | \`\${Key}.\${Exclude<keyof T[Key], keyof any[]> & string}\`
        : never
      : never;
    type PathImpl2<T> = PathImpl<T, keyof T> | keyof T;
    type DeepKeys<T> = PathImpl2<T> extends string | keyof T ? PathImpl2<T> : keyof T;
    type Without<T, K> = Pick<T, Exclude<keyof T, K>>; 
`;
}
