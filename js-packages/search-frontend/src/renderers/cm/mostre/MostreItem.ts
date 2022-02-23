export type MostreResultItem = {
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  mostre: {
    endDate: string;
    imgUrl: string;
    location: string;
    periods: Array<string>;
    startDate: string;
  };
};
