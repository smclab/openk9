export type EntrateResultItem = {
  entrate: {
    linkedUrls: Array<string>;
    category: string
  };
  topic: {
    topics: Array<string>;
  };
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
};
