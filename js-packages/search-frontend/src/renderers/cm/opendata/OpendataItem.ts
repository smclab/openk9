export type OpendataResultItem = {
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  opendata: {
    dataDiModifica: string;
    titolare: string;
    coperturaGeografica: string;
    autore: string;
    temiDelDataset: Array<string>;
    tags: Array<string>;
    summary: string;
    startDate: string;
    endDate: string;
  };
};
