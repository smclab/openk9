/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { useLanguage } from "./useLanguage";

export function Translate({ label }: { label: TranslationKey }): string {
  const { language } = useLanguage();
  return getTranslation(label, language);
}

export function getTranslation(label: TranslationKey, language: string): string {
  const htmlLang = language as Language;
  const translation = translations[label]?.[htmlLang];
  return translation || translations[label]?.en_US || label;
}

const translations: {
  [key in TranslationKey]: { [lang in Language]?: string };
} = {
  closeChatbot: {
    it_IT: "Chiudi il chatbot",
    en_US: "Close the chatbot",
    fr_FR: "Fermez le chatbot",
    es_ES: "Cerrar el chatbot",
    de_DE: "Schließen Sie den Chatbot",
  },
  openChatbot: {
    it_IT: "Apri il chatbot",
    en_US: "Open the chatbot",
    fr_FR: "Ouvrir le chatbot",
    es_ES: "Abrir el chatbot",
    de_DE: "Öffnen Sie den Chatbot",
  },
  clearChat: {
    it_IT: "Svuota la chat",
    en_US: "Clear the chat",
    fr_FR: "Effacer le chat",
    es_ES: "Borrar el chat",
    de_DE: "Leeren Sie den Chat",
  },
  startQuestion: {
    it_IT: "Fai partire una domanda",
    en_US: "Start a question",
    fr_FR: "Commencez une question",
    es_ES: "Iniciar una pregunta",
    de_DE: "Starten Sie eine Frage",
  },
  youSendMessage: {
    it_IT: "tu hai mandato un messaggio alle ",
    en_US: "you sent a message at ",
    fr_FR: "vous avez envoyé un message à ",
    es_ES: "tú enviaste un mensaje a las ",
    de_DE: "du hast eine Nachricht um ",
  },
  sendMessage: {
    it_IT: "ha mandato un messaggio alle ",
    en_US: "sent a message at ",
    fr_FR: "a envoyé un message à ",
    es_ES: "envió un mensaje a las ",
    de_DE: "hat eine Nachricht um ",
  },
  customPlaceholder: {
    it_IT: "scrivi il tuo messaggio...",
    en_US: "write your message...",
    fr_FR: "écris ton message...",
    es_ES: "escribe tu mensaje...",
    de_DE: "Schreiben Sie Ihre Nachricht...",
  },
  searchLabel: {
    it_IT: "Cerca nel chatbot",
    en_US: "Search in the chatbot",
    fr_FR: "Rechercher dans le chatbot",
    es_ES: "Buscar en el chatbot",
    de_DE: "Im Chatbot suchen",
  },
  guardrailViolation: {
    it_IT: "La tua richiesta non può essere evasa perché viola le linee guida sui contenuti.",
    en_US: "Your request cannot be processed because it violates the content guidelines.",
    fr_FR: "Votre demande ne peut pas être traitée car elle enfreint les règles relatives au contenu.",
    es_ES: "Tu solicitud no puede ser procesada porque infringe las directrices de contenido.",
    de_DE: "Ihre Anfrage kann nicht bearbeitet werden, da sie gegen die Inhaltsrichtlinien verstößt.",
  },
  generateError: {
    it_IT: "Risposta generata non disponibile.",
    en_US: "Generated answer not available.",
    fr_FR: "Réponse générée non disponible.",
    es_ES: "Respuesta generada no disponible.",
    de_DE: "Generierte Antwort nicht verfügbar.",
  },
  welcomeMessage: {
    it_IT: "Benvenuti su Openk9, hai dei dubbi? non esitare a chiedere",
    en_US: "Welcome to Openk9, any questions? Don't hesitate to ask",
    fr_FR:
      "Bienvenue sur Openk9, vous avez des questions ? N'hésitez pas à demander",
    es_ES: "Bienvenido a Openk9, ¿tienes dudas? No dudes en preguntar",
    de_DE:
      "Willkommen bei Openk9, haben Sie Fragen? Zögern Sie nicht zu fragen",
  },
  responseCancelled: {
    it_IT: "La risposta è stata annullata",
    en_US: "The answer was canceled",
    fr_FR: "La réponse a été annulée",
    es_ES: "La respuesta ha sido cancelada",
    de_DE: "Die Antwort wurde abgebrochen",
  },
  // `{count}` is replaced by the caller with the number of sources
  sourcesCount: {
    it_IT: "{count} fonti",
    en_US: "{count} sources",
    fr_FR: "{count} sources",
    es_ES: "{count} fuentes",
    de_DE: "{count} Quellen",
  },
  sourcesCountOne: {
    it_IT: "{count} fonte",
    en_US: "{count} source",
    fr_FR: "{count} source",
    es_ES: "{count} fuente",
    de_DE: "{count} Quelle",
  },
  showMoreSources: {
    it_IT: "Mostra {count} in più",
    en_US: "Show {count} more",
    fr_FR: "Afficher {count} de plus",
    es_ES: "Mostrar {count} más",
    de_DE: "{count} weitere anzeigen",
  },
  showLessSources: {
    it_IT: "Mostra meno",
    en_US: "Show less",
    fr_FR: "Afficher moins",
    es_ES: "Mostrar menos",
    de_DE: "Weniger anzeigen",
  },
  aiDisclosure: {
    it_IT:
      "Stai interagendo con un sistema di intelligenza artificiale. Le risposte sono generate automaticamente e possono contenere errori: verifica le informazioni importanti.",
    en_US:
      "You are interacting with an artificial intelligence system. Answers are generated automatically and may contain errors: check important information.",
    fr_FR:
      "Vous interagissez avec un système d'intelligence artificielle. Les réponses sont générées automatiquement et peuvent contenir des erreurs : vérifiez les informations importantes.",
    es_ES:
      "Estás interactuando con un sistema de inteligencia artificial. Las respuestas se generan automáticamente y pueden contener errores: verifica la información importante.",
    de_DE:
      "Sie interagieren mit einem System der künstlichen Intelligenz. Die Antworten werden automatisch generiert und können Fehler enthalten: Überprüfen Sie wichtige Informationen.",
  },
  loginRequired: {
    it_IT: "Per usare la chat devi effettuare l'accesso.",
    en_US: "You need to log in to use the chat.",
    fr_FR: "Vous devez vous connecter pour utiliser le chat.",
    es_ES: "Debes iniciar sesión para usar el chat.",
    de_DE: "Melden Sie sich an, um den Chat zu nutzen.",
  },
  login: {
    it_IT: "Accedi",
    en_US: "Log in",
    fr_FR: "Se connecter",
    es_ES: "Iniciar sesión",
    de_DE: "Anmelden",
  },
  close: {
    it_IT: "Chiudi",
    en_US: "Close",
    fr_FR: "Fermer",
    es_ES: "Cerrar",
    de_DE: "Schließen",
  },
};

type Language = "it_IT" | "en_US" | "fr_FR" | "es_ES" | "de_DE";

type TranslationKey =
  | "closeChatbot"
  | "openChatbot"
  | "clearChat"
  | "startQuestion"
  | "youSendMessage"
  | "sendMessage"
  | "customPlaceholder"
  | "searchLabel"
  | "guardrailViolation"
  | "generateError"
  | "welcomeMessage"
  | "responseCancelled"
  | "sourcesCount"
  | "sourcesCountOne"
  | "showMoreSources"
  | "showLessSources"
  | "aiDisclosure"
  | "loginRequired"
  | "login"
  | "close";

