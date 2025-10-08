import React from "react";
import { useLocation } from "react-router-dom";

export type NamePath =
  | "admin"
  | "dashboard"
  | "bucket"
  | "buckets"
  | "analyzer"
  | "analyzers"
  | "autocorrections"
  | "data-source"
  | "data-sources"
  | "document-types"
  | "document-type"
  | "document-type-template"
  | "document-type-templates"
  | "plugin-drivers"
  | "suggestion-category"
  | "suggestion-categories"
  | "enrich-item"
  | "enrich-items"
  | "pipeline"
  | "pipelines"
  | "plugin-driver"
  | "plugin-drivers"
  | "tab"
  | "tabs"
  | "token-tab"
  | "token-tabs"
  | "dataindex"
  | "dataindices"
  | "rule"
  | "rules"
  | "annotator"
  | "annotators"
  | "token-filter"
  | "token-filters"
  | "char-filter"
  | "char-filters"
  | "tokenizer"
  | "tokenizers"
  | "query-analysis"
  | "query-analyses"
  | "search-config"
  | "search-configs"
  | "large-language-model"
  | "large-languages-model"
  | "embedding-model"
  | "embedding-models"
  | "datasource-group"
  | "mappings-group"
  | "analysis-group"
  | "search-config-group"
  | "ai-tools-group"
  | "generative-ai-group"
  | "query-analysis-group"
  | "rag-configuration"
  | "rag-configurations"
  | "connectors";

export const namePath: { label: NamePath; value: NamePath }[] = [
  { label: "admin", value: "admin" },
  { label: "dashboard", value: "dashboard" },
  { label: "bucket", value: "buckets" },
  { label: "buckets", value: "buckets" },
  { label: "analyzer", value: "analyzers" },
  { label: "analyzers", value: "analyzers" },
  { label: "data-source", value: "data-sources" },
  { label: "data-sources", value: "data-sources" },
  { label: "document-types", value: "document-types" },
  { label: "document-type", value: "document-types" },
  { label: "document-type-template", value: "document-type-templates" },
  { label: "document-type-templates", value: "document-type-templates" },
  { label: "suggestion-category", value: "suggestion-categories" },
  { label: "suggestion-categories", value: "suggestion-categories" },
  { label: "enrich-item", value: "enrich-items" },
  { label: "enrich-items", value: "enrich-items" },
  { label: "pipeline", value: "pipelines" },
  { label: "pipelines", value: "pipelines" },
  { label: "plugin-driver", value: "plugin-drivers" },
  { label: "plugin-drivers", value: "plugin-drivers" },
  { label: "tab", value: "tabs" },
  { label: "tabs", value: "tabs" },
  { label: "token-tab", value: "token-tabs" },
  { label: "token-tabs", value: "token-tabs" },
  { label: "dataindex", value: "dataindices" },
  { label: "dataindices", value: "dataindices" },
  { label: "rule", value: "rules" },
  { label: "rules", value: "rules" },
  { label: "annotator", value: "annotators" },
  { label: "annotators", value: "annotators" },
  { label: "token-filter", value: "token-filters" },
  { label: "token-filters", value: "token-filters" },
  { label: "char-filter", value: "char-filters" },
  { label: "char-filters", value: "char-filters" },
  { label: "tokenizer", value: "tokenizers" },
  { label: "tokenizers", value: "tokenizers" },
  { label: "query-analysis", value: "query-analyses" },
  { label: "query-analyses", value: "query-analyses" },
  { label: "search-config", value: "search-configs" },
  { label: "search-configs", value: "search-configs" },
  { label: "large-language-model", value: "large-language-model" },
  { label: "large-languages-model", value: "large-languages-model" },
  { label: "embedding-model", value: "embedding-models" },
  { label: "embedding-models", value: "embedding-models" },
  { label: "rag-configuration", value: "rag-configurations" },
  { label: "rag-configurations", value: "rag-configurations" },
];

type SideNavigationContextValue = {
  changaSideNavigation: React.Dispatch<React.SetStateAction<NamePath>>;
  navigation: NamePath;
};

const AuthenticationContext = React.createContext<SideNavigationContextValue>(null as any);

export function SideNavigationContextProvider({ children }: { children: React.ReactNode }) {
  const params = useLocation().pathname.replace("/", "");
  const pathNavigate = namePath.find((item) => params.startsWith(item.label));
  const [navigation, changaSideNavigation] = React.useState<NamePath>(pathNavigate?.value || "dashboard");

  return (
    <AuthenticationContext.Provider value={{ navigation, changaSideNavigation }}>
      {children}
    </AuthenticationContext.Provider>
  );
}

export function useSideNavigation() {
  return React.useContext(AuthenticationContext);
}
