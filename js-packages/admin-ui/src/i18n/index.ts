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
import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import en from "./locales/en.json";
import it from "./locales/it.json";

declare module "i18next" {
  interface CustomTypeOptions {
    returnNull: false;
  }
}

export const supportedLanguages = ["en", "it"] as const;

export type SupportedLanguage = (typeof supportedLanguages)[number];

export const languageStorageKey = "language";

function isSupportedLanguage(value: string | null): value is SupportedLanguage {
  return supportedLanguages.includes(value as SupportedLanguage);
}

function detectLanguage(): SupportedLanguage {
  const stored = localStorage.getItem(languageStorageKey);
  if (isSupportedLanguage(stored)) return stored;
  const browserLanguage = navigator.language.split("-")[0];
  return isSupportedLanguage(browserLanguage) ? browserLanguage : "en";
}

export function changeLanguage(language: SupportedLanguage) {
  localStorage.setItem(languageStorageKey, language);
  i18n.changeLanguage(language);
}

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    it: { translation: it },
  },
  lng: detectLanguage(),
  fallbackLng: "en",
  returnNull: false,
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;
