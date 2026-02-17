import React from "react";
import { BaseInputProps } from "..";
import { Box, Paper, Theme, Typography, useTheme, CircularProgress } from "@mui/material";

// Lazy load di Monaco Editor stesso
const loadMonaco = () => import("monaco-editor");

// Lazy load di Prettier (usato solo per formattazione)
const loadPrettier = () => Promise.all([import("prettier/standalone"), import("prettier/parser-typescript")]);

let monacoInstance: typeof import("monaco-editor") | null = null;
let prettierInstance: { prettier: any; parserTypeScript: any } | null = null;

function applyMonacoTheme(monaco: typeof import("monaco-editor"), theme: Theme) {
  monaco.editor.defineTheme("materialTheme", {
    base: theme.palette.mode === "dark" ? "vs-dark" : "vs",
    inherit: true,
    rules: [],
    colors: {
      "editor.background": theme.palette.mode === "dark" ? "#1e1e1e" : "#ffffff",
      "editor.foreground": theme.palette.mode === "dark" ? "#d4d4d4" : "#000000",
      "editor.lineHighlightBackground": theme.palette.mode === "dark" ? "#2a2d2e" : "#fafafa",
      "editor.selectionBackground": theme.palette.mode === "dark" ? "#264f78" : "#ADD6FF",
      "editor.inactiveSelectionBackground": theme.palette.mode === "dark" ? "#3a3d41" : "#D3D4D7",
    },
  });
}

// Cache per evitare di caricare più volte lo stesso linguaggio
const loadedLanguages = new Set<string>();

// Lazy load dei linguaggi
async function loadLanguage(monaco: typeof import("monaco-editor"), language: string) {
  if (loadedLanguages.has(language)) return;

  try {
    switch (language) {
      case "typescript":
      case "javascript":
        if (!loadedLanguages.has("typescript")) {
          await import("monaco-editor/esm/vs/language/typescript/monaco.contribution");
          monaco.languages.typescript.typescriptDefaults.setCompilerOptions({
            jsx: monaco.languages.typescript.JsxEmit.Preserve,
            target: monaco.languages.typescript.ScriptTarget.ES2020,
            esModuleInterop: true,
          });
          loadedLanguages.add("typescript");
          loadedLanguages.add("javascript");
        }
        break;
      case "json":
        await import("monaco-editor/esm/vs/language/json/monaco.contribution");
        loadedLanguages.add("json");
        break;
    }
  } catch (error) {
    console.error(`Failed to load language: ${language}`, error);
  }
}

// Inizializzazione TypeScript types (lazy)
let typesInitialized = false;
async function initializeTypeScriptTypes(monaco: typeof import("monaco-editor")) {
  if (typesInitialized) return;
  typesInitialized = true;

  try {
    const response = await fetch("https://unpkg.com/@types/react@18.0.24/index.d.ts");
    const data = await response.text();
    monaco.languages.typescript.typescriptDefaults.addExtraLib(data, `file:///node_modules/@react/types/index.d.ts`);
    monaco.languages.typescript.typescriptDefaults.addExtraLib(templateTypeDefinition(), "./template.d.ts");
  } catch (error) {
    console.error("Failed to load TypeScript types:", error);
  }
}

// Formatter registration (lazy)
const formatterRegistered = new Set<string>();
async function registerFormatter(monaco: typeof import("monaco-editor"), language: string) {
  if (formatterRegistered.has(language)) return;

  if (!prettierInstance) {
    const [prettier, parserTypeScript] = await loadPrettier();
    prettierInstance = { prettier: prettier.default, parserTypeScript: parserTypeScript.default };
  }

  monaco.languages.registerDocumentFormattingEditProvider(language, {
    provideDocumentFormattingEdits(model, options) {
      const formatted = prettierInstance!.prettier.format(model.getValue(), {
        parser: "typescript",
        plugins: [prettierInstance!.parserTypeScript],
      });
      return [
        {
          range: model.getFullModelRange(),
          text: formatted,
        },
      ];
    },
  });

  formatterRegistered.add(language);
}

let nextUriId = 1;
function createInMemoriUri(language: MonacoLanguage) {
  switch (language) {
    case "javascript":
      return `inmemory://model/${nextUriId++}.js`;
    case "javascript-react":
      return `inmemory://model/${nextUriId++}.jsx`;
    case "typescript":
      return `inmemory://model/${nextUriId++}.ts`;
    case "typescript-react":
      return `inmemory://model/${nextUriId++}.tsx`;
    case "json":
      return `inmemory://model/${nextUriId++}.json`;
    case "text":
      return `inmemory://model/${nextUriId++}.txt`;
    case undefined:
      return `inmemory://model/${nextUriId++}`;
    default:
      throw new Error(`Unknown language: ${language}`);
  }
}

type MonacoLanguage =
  | "javascript"
  | "javascript-react"
  | "typescript"
  | "typescript-react"
  | "json"
  | "text"
  | undefined;

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
      throw new Error(`Unknown language: ${language}`);
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
  description,
  tooltip,
}: BaseInputProps<string> & {
  language: MonacoLanguage;
  height?: string;
  readonly?: boolean;
  description?: string;
  tooltip?: React.ReactElement;
}) {
  const editorElementRef = React.useRef<HTMLDivElement>(null);
  const editorRef = React.useRef<{
    editor?: import("monaco-editor").editor.IStandaloneCodeEditor;
    model?: import("monaco-editor").editor.ITextModel;
  }>({});
  const [isLoading, setIsLoading] = React.useState(true);

  const theme = useTheme();

  // Inizializza Monaco Editor e il linguaggio
  React.useEffect(() => {
    let cancelled = false;

    const initialize = async () => {
      try {
        setIsLoading(true);

        // 1. Carica Monaco Editor se non già caricato
        if (!monacoInstance) {
          const monaco = await loadMonaco();
          monacoInstance = monaco;
        }

        if (cancelled) return;

        // 2. Applica tema
        applyMonacoTheme(monacoInstance, theme);

        // 3. Carica linguaggio specifico
        const remapped = remapLanguage(language);
        if (remapped) {
          await loadLanguage(monacoInstance, remapped);

          // 4. Registra formatter
          if (remapped === "typescript" || remapped === "javascript" || remapped === "json") {
            await registerFormatter(monacoInstance, remapped);
          }

          // 5. Inizializza types per TypeScript/JavaScript
          if (remapped === "typescript" || remapped === "javascript") {
            await initializeTypeScriptTypes(monacoInstance);
          }
        }

        if (cancelled) return;

        // 6. Crea editor
        if (editorElementRef.current && !editorRef.current.editor) {
          const editor = monacoInstance.editor.create(editorElementRef.current, {
            readOnly: readonly,
            minimap: { enabled: false },
            theme: "materialTheme",
            tabSize: 2,
            automaticLayout: true,
          });

          editor.onDidBlurEditorText(() => {
            if (editorRef.current.model) {
              onChange(editorRef.current.model.getValue());
            }
          });

          editorRef.current.editor = editor;
        }

        setIsLoading(false);
      } catch (error) {
        console.error("Failed to initialize Monaco Editor:", error);
        setIsLoading(false);
      }
    };

    initialize();

    return () => {
      cancelled = true;
      if (editorRef.current.editor) {
        editorRef.current.editor.dispose();
        editorRef.current.editor = undefined;
      }
    };
  }, [readonly]);

  // Aggiorna tema
  React.useEffect(() => {
    if (monacoInstance && editorRef.current.editor) {
      applyMonacoTheme(monacoInstance, theme);
      monacoInstance.editor.setTheme("materialTheme");
    }
  }, [theme]);

  // Aggiorna model quando cambiano value o language
  React.useEffect(() => {
    if (monacoInstance && editorRef.current.editor && !isLoading) {
      // Disponi il model precedente se esiste
      if (editorRef.current.model) {
        editorRef.current.model.dispose();
      }

      const uri = monacoInstance.Uri.parse(createInMemoriUri(language));
      const model = monacoInstance.editor.createModel(value, remapLanguage(language), uri);

      editorRef.current.editor.setModel(model);
      editorRef.current.model = model;

      return () => {
        model.dispose();
      };
    }
  }, [language, value, isLoading]);

  const editorStyle = {
    height,
    width: "100%",

    borderRadius: "4px",
    padding: "8px",
    boxShadow: "0 0 5px rgba(0, 0, 0, 0.1)",
    overflow: "hidden",
  };

  return (
    <div className={`${validationMessages.length ? "has-warning" : ""}`}>
      <Box display={"flex"} alignItems={"center"} marginBottom={1}>
        <Typography key={id}>
          {label} {labelPostfix(language)}
        </Typography>
        {tooltip}
      </Box>
      <Box style={{ width: "100%", position: "relative" }}>
        {isLoading ? (
          <Box display="flex" alignItems="center" justifyContent="center" style={editorStyle}>
            <CircularProgress size={24} />
            <Typography style={{ marginLeft: 8 }}>Loading editor...</Typography>
          </Box>
        ) : (
          <Paper variant="outlined" ref={editorElementRef} style={editorStyle} />
        )}
      </Box>
      <Box className="form-feedback-group">
        {validationMessages.map((validationMessage, index) => {
          return (
            <Typography key={index} className="form-feedback-item">
              {validationMessage}
            </Typography>
          );
        })}
      </Box>
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
