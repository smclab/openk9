import { GenericResultItem } from "@openk9/rest-api";

export type EmailResultItem = GenericResultItem<{
  email: {
    cc?: string;
    date: number; // timestamp
    htmlBody: string;
    body: string;
    from: string;
    subject: string;
    to: string;
  };
  document: {
    title: string;
    relativeUrl: Array<string>;
    contentType: string;
    url: string;
    content: string;
  };
}>;
