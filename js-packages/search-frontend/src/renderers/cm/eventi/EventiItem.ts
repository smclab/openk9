export type EventiResultItem = {
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
};
