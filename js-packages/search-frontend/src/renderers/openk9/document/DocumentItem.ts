import { GenericResultItem } from "@openk9/rest-api";

export type DocumentResultItem = GenericResultItem<{
  document: {
    title: string;
    relativeUrl: Array<string>;
    contentType: string;
    url: string;
    content: string;
  };
}>;
