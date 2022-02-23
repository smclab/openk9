export type ProcessiResultItem = {
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  processi: {
    name: string;
    startDate?: string;
    endDate?: string;
    partecipants?: string;
    area?: string;
    imgUrl?: string;
  };
}
