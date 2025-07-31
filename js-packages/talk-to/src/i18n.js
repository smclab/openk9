import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import it_IT from "./translations/translation_it.json";
import fr_FR from "./translations/translation_fr.json";
import es_ES from "./translations/translation_es.json";
import en_US from "./translations/translation_en.json";

const resources = {
	en_US,
	it_IT,
	fr_FR,
	es_ES,
};

i18n.use(initReactI18next).init({
	resources,
	lng: "en",
	interpolation: {
		escapeValue: false,
	},
});

export default i18n;
