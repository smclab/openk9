export type PdfResultItem = {
  document: {
    title: string;
    relativeUrl: Array<string>;
    contentType: string;
    url: string;
    content: string;
    summary: string;
  };
  file: {
    path: string;
    lastModifiedDate: string;
  };
};
