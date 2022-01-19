import { GenericResultItem } from "@openk9/rest-api";

export type VenditeResultItem = GenericResultItem<{
  vendite: {
    descrizione: string;
    tipologia: string;
    category: string;
    regione: string;
    provincia: string;
    proprieta: string;
    rendita: string;
    foglio: string;
    mappale: string;
    subalterno: string;
    vani: string;
    catastale: string;
    prezzoBase: string;
    prezzoDiAggiudicazione: string;
    classificazione: string;
    destinazione: string;
    modalita: string;
    lotto: string;
    criterio: string;
    referente: string;
    email: string;
    telefono: string;
    indirizzo: string;
    oggettoGara: string;
    status: string;
    comune: string;
    scadenzaOfferte: string;
    esameOfferte: string;
    documentiVendita?: Array<string>;
  };
  web: {
    favicon: string;
    title: string;
    url: string;
    content: string;
  };
}>;
