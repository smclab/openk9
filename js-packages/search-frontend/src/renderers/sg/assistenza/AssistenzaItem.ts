import { GenericResultItem } from "@openk9/rest-api";

export type AssistenzaResultItem = GenericResultItem<{
  entrate: {
    linkedUrls: Array<string>;
  };
  topic: {
    topics: Array<string>;
  };
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
}>;
