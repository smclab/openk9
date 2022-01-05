import { GenericResultItem } from "@openk9/rest-api";

export type PubblicazioniResultItem = GenericResultItem<{
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  pubblicazioni: {
    category: string;
    imgUrl: string;
    pubDate: string;
    topic: string;
    authors: string;
    linkedUrls: Array<string>;
  };
}>;
