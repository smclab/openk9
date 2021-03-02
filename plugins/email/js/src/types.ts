import { GenericResultItem } from "@openk9/http-api";

export type WebResultItem = GenericResultItem<{
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
}>;
