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
import React, { createContext, useState, useEffect, ReactNode } from "react";

type LanguageContextType = {
  language: string;
  setLanguage: (language: string) => void;
};

const LanguageContext = createContext<LanguageContextType | undefined>(
  undefined
);

type LanguageProviderProps = {
  children: ReactNode;
  initialLanguage?: string;
};

function remappingLanguage(language: string): string {
  switch (language) {
    case "en":
      return "en_US";
    case "it":
      return "it_IT";
    case "es":
      return "es_ES";
    case "de":
      return "de_DE";
    case "fr":
      return "fr_FR";
    default:
      return language;
  }
}

export const LanguageProvider: React.FC<LanguageProviderProps> = ({
  children,
  initialLanguage,
}) => {
  const [language, setLanguage] = useState<string>(
    initialLanguage
      ? remappingLanguage(initialLanguage)
      : remappingLanguage(document.documentElement.lang || "en")
  );

  useEffect(() => {
    if (initialLanguage) {
      setLanguage(remappingLanguage(initialLanguage));
    }
  }, [initialLanguage]);

  useEffect(() => {
    const handleLangChange = () => {
      setLanguage(remappingLanguage(document.documentElement.lang || "en"));
    };

    const observer = new MutationObserver((mutationsList) => {
      for (const mutation of mutationsList) {
        if (
          mutation.type === "attributes" &&
          mutation.attributeName === "lang"
        ) {
          handleLangChange();
        }
      }
    });

    observer.observe(document.documentElement, { attributes: true });

    return () => observer.disconnect();
  }, []);

  return (
    <LanguageContext.Provider value={{ language, setLanguage }}>
      {children}
    </LanguageContext.Provider>
  );
};

export default LanguageContext;

