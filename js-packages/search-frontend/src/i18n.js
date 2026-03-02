import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import it from "./translations/translation_it.json";
import fr from "./translations/translation_fr.json";
import es from "./translations/translation_es.json";
import en from "./translations/translation_en.json";
import de from "./translations/translation_de.json";

const resources = {
  it,
  en,
  fr,
  es,
  de,
};

i18n.use(initReactI18next).init({
  resources,
  lng: "en",
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;
