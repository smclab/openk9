export type WemiResultItem = {
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
  wemi: {
    destinatari: Array<string>;
    attivita: Array<string>;
    sedi: Array<string>;
    momento: Array<string>;
    municipi: Array<string>;
    prezzi: Array<{ label: string; value: string }>;
    procedura: string;
    categoria: string;
    servizio: string;
  };
};
