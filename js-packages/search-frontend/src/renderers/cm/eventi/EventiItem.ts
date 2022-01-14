import { GenericResultItem } from "@openk9/rest-api";

export type EventiResultItem = GenericResultItem<{
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  eventi: {
    category?: string;
    endDate?: string;
    date?: string;
    imgUrl: string;
    location: string;
    periods: string;
    startDate?: string;
    subLocation: string;
  };
}>;
