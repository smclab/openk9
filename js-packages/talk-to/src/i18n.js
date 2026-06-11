import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import it_IT from "./translations/translation_it.json";
import fr_FR from "./translations/translation_fr.json";
import es_ES from "./translations/translation_es.json";
import en_US from "./translations/translation_en.json";
import de_DE from "./translations/translation_de.json";

const resources = {
	en_US,
	it_IT,
	fr_FR,
	es_ES,
	de_DE,
};

i18n.use(initReactI18next).init({
	resources,
	lng: "en_US",
	fallbackLng: {
		en: ["en_US"],
		it: ["it_IT", "en_US"],
		fr: ["fr_FR", "en_US"],
		es: ["es_ES", "en_US"],
		de: ["de_DE", "en_US"],
		default: ["en_US"],
	},
	supportedLngs: Object.keys(resources),
	nonExplicitSupportedLngs: true,
	interpolation: {
		escapeValue: false,
	},
});

export default i18n;
