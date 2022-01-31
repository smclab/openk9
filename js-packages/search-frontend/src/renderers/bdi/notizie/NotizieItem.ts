import { GenericResultItem } from "@openk9/rest-api";

export type NotizieResultItem = GenericResultItem<{
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  notizie: {
    category: string;
    imgUrl: string;
    pubDate: string;
    topic: string;
    linkedUrls?: Array<string>;
  };
}>;
