export type NotizieResultItem = {
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  notizie: {
    category: string;
    categories?: Array<string>;
    imgUrl: string;
    pubDate: string;
    topic: string;
    linkedUrls?: Array<string>;
  };
};
