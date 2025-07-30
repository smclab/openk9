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
