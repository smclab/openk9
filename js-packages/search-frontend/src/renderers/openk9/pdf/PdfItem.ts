export type PdfResultItem = {
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
};
