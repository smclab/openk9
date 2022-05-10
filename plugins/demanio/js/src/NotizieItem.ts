export type NotizieResultItem = {
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  notizie: {
    topics?: Array<string>;
    imgUrl: string;
    pubDate: string;
    linkedUrls?: Array<string>;
  };
};
