import { GenericResultItem } from "@openk9/rest-api";

export type GaraResultItem = GenericResultItem<{
  gare: {
    descrizione: string;
    tipologia: string;
    regione: string;
    provincia: string;
    stazione: string;
    datapubblicazione: string;
    datascadenza: string;
    importo: string;
    criterio: string;
    nominativo: string;
    email: string;
    linkedUrls?: Array<string>;
    oggettoGara: string;
    status: string;
    comune: string;
    dataEsito: string;
    documentiGara?: Array<string>;
    documentiEsito?: Array<string>;
  };
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
}>;
