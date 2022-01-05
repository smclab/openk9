import { GenericResultItem } from "@openk9/rest-api";

export type PetizioniResultItem = GenericResultItem<{
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  petizioni: {
    pubDate: string;
    status: string;
  };
}>;
