import { GenericResultItem } from "@openk9/rest-api";

export type ProcessiResultItem = GenericResultItem<{
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  processi: {
    name: string;
    startDate?: string;
    endDate?: string;
    partecipants?: string;
    area?: string;
    imgUrl?: string;
  };
}>;
