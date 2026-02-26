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
  const htmlLang = language as Language;
  const translation = translations[label]?.[htmlLang];
  return translation || label;
}

const translations: {
  [key in TranslationKey]: { [lang in Language]?: string };
} = {
  closeChatbot: {
    it_IT: "Chiudi il chatbot",
    en_US: "Close the chatbot",
    fr_FR: "Fermez le chatbot",
    es_ES: "Cerrar el chatbot",
    de_DE: "SchlieÃŸen Sie den Chatbot",
  },
  openChatbot: {
    it_IT: "Apri il chatbot",
    en_US: "Open the chatbot",
    fr_FR: "Ouvrir le chatbot",
    es_ES: "Abrir el chatbot",
    de_DE: "Ã–ffnen Sie den Chatbot",
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
    fr_FR: "vous avez envoyÃ© un message Ã  ",
    es_ES: "tÃº enviaste un mensaje a las ",
    de_DE: "du hast eine Nachricht um ",
  },
  sendMessage: {
    it_IT: "ha mandato un messaggio alle ",
    en_US: "sent a message at ",
    fr_FR: "a envoyÃ© un message Ã  ",
    es_ES: "enviÃ³ un mensaje a las ",
    de_DE: "hat eine Nachricht um ",
  },
  customPlaceholder: {
    it_IT: "scrivi il tuo messaggio...",
    en_US: "write your message...",
    fr_FR: "Ã©cris ton message...",
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
  | "searchLabel";

