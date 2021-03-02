import { GenericResultItem } from "./searchResults";

export type PluginInfo = {
  pluginId: string;
  bundleInfo: {
    id: number;
    lastModified: number;
    state: string;
    symbolicName: string;
    version: string;
  };
};

export type Plugin<E> = {
  pluginId: string;
  displayName: string;
  pluginType: ("DATASOURCE" | "SUGGESTION" | "ENRICH")[];
  dataSourceAdminInterfacePath?: DataSourceAdminInterface;
  dataSourceRenderingInterface?: DataSourceRenderingInterface<E>;
};

export type DataSourceAdminInterface = {
  iconRenderer: React.FC<{}>;
  settingsRenderer: React.FC<{}>;
};

export type ResultRendererProps<E> = {
  data: GenericResultItem<E>;
  onSelect(): void;
};

export type ResultRenderersType<E> = {
  [key: string]: React.FC<ResultRendererProps<E>>;
};

export type SidebarRendererProps<E> = {
  result: GenericResultItem<E>;
};

export type SidebarRenderersType<E> = {
  [key: string]: React.FC<SidebarRendererProps<E>>;
};

export type DataSourceRenderingInterface<E> = {
  resultRenderers: ResultRenderersType<E>;
  sidebarRenderers: SidebarRenderersType<E>;
};
