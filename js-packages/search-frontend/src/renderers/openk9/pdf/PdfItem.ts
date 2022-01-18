import { GenericResultItem } from "@openk9/rest-api";

export type PdfResultItem = GenericResultItem<{
  document: {
    title: string;
    relativeUrl: Array<string>;
    contentType: string;
    url: string;
    content: string;
  };
  file: {
    path: string;
    lastModifiedDate: string;
  };
}>;
