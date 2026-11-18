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
import React from "react";
import Chatbot, { ChatbotHandle } from "../lib/components/Chatbot";
import { CloseIcon as CloseSmallIcon } from "../lib/components/icons";
import { Logo } from "./svg/Logo";
import { User } from "./svg/User";
import { TrashIcon } from "./svg/Trash";
import { SearchIcon } from "./svg/SearchSvg";
import { CloseIcon } from "./svg/CloseIcon";

// da sostituire con l'informativa privacy reale dell'integrazione
const PRIVACY_POLICY_URL = "https://www.example.com/privacy";

function InfoIcon() {
  return (
    <svg
      className="harness-ai-banner-icon"
      focusable="false"
      aria-hidden="true"
      viewBox="0 0 24 24"
    >
      <path d="M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2m0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8" />
    </svg>
  );
}

/**
 * Informativa AI richiudibile. Chiusa non sparisce: si riduce a una ⓘ che la
 * riapre, così resta sempre raggiungibile dall'utente.
 *
 * Tutto qui dentro è contenuto di frase (`span`, `a`, `button`, `svg`) perché il
 * pannello monta il nodo dentro un `<p>`: un `<div>` verrebbe estratto dal
 * paragrafo dal parser HTML, rompendo il layout e l'associazione con
 * `aria-describedby`. I colori stanno in `index.css`.
 */
function AiDisclosureBanner() {
  const [isOpen, setIsOpen] = React.useState(true);

  if (!isOpen) {
    return (
      <button
        type="button"
        className="harness-ai-banner-button harness-ai-banner-reopen"
        aria-label="Mostra l'informativa sull'uso dell'intelligenza artificiale"
        onClick={() => setIsOpen(true)}
      >
        <InfoIcon />
      </button>
    );
  }

  return (
    <span className="harness-ai-banner">
      <InfoIcon />
      <span className="harness-ai-banner-text">
        Questo assistente virtuale utilizza tecnologie di Intelligenza
        Artificiale. Le risposte sono generate automaticamente e hanno finalità
        esclusivamente informative. È opportuno non inserire dati personali o
        informazioni riservate. Consulta l’
        <a href={PRIVACY_POLICY_URL} target="_blank" rel="noreferrer">
          Informativa Privacy
        </a>{" "}
        per maggiori dettagli.
      </span>
      <button
        type="button"
        className="harness-ai-banner-button harness-ai-banner-close"
        aria-label="Chiudi l'informativa"
        onClick={() => setIsOpen(false)}
      >
        <CloseSmallIcon />
      </button>
    </span>
  );
}

function App() {
  const chatbotRef = React.useRef<ChatbotHandle>(null);
  const [isDark, setIsDark] = React.useState(false);
  // le variabili ridefinite stanno in `index.css`, sotto `.harness-custom-vars`:
  // e' il modo in cui un integratore adatta il chatbot al suo design system
  const [hasCustomVars, setHasCustomVars] = React.useState(false);
  // senza pulsante tondo il pannello si apre solo dai pulsanti qui sopra
  const [showLauncher, setShowLauncher] = React.useState(true);
  const [hasSourcesList, setHasSourcesList] = React.useState(false);
  const [hasTimestamps, setHasTimestamps] = React.useState(true);

  return (
    <React.Fragment>
      {/* un bottone della pagina ospite, fuori dal chatbot, che fa la domanda
          al posto dell'utente: è il caso di un'integrazione in un portale */}
      <button
        type="button"
        onClick={() => chatbotRef.current?.send("Che cos'è OpenK9?")}
      >
        Chiedi al chatbot: Che cos'è OpenK9?
      </button>
      <button type="button" onClick={() => chatbotRef.current?.open()}>
        Apri il chatbot
      </button>
      <button type="button" onClick={() => chatbotRef.current?.close()}>
        Chiudi il chatbot
      </button>
      <label>
        <input
          type="checkbox"
          checked={showLauncher}
          onChange={(event) => setShowLauncher(event.currentTarget.checked)}
        />
        Pulsante tondo
      </label>
      <label>
        <input
          type="checkbox"
          checked={isDark}
          onChange={(event) => setIsDark(event.currentTarget.checked)}
        />
        Tema scuro
      </label>
      <label>
        <input
          type="checkbox"
          checked={hasCustomVars}
          onChange={(event) => setHasCustomVars(event.currentTarget.checked)}
        />
        Variabili CSS ridefinite
      </label>
      <label>
        <input
          type="checkbox"
          checked={hasSourcesList}
          onChange={(event) => setHasSourcesList(event.currentTarget.checked)}
        />
        Fonti come elenco di link
      </label>
      <label>
        <input
          type="checkbox"
          checked={hasTimestamps}
          onChange={(event) => setHasTimestamps(event.currentTarget.checked)}
        />
        Orari dei messaggi
      </label>
      <div
        className={[
          "openk9-chatbot",
          isDark && "harness-dark",
          hasCustomVars && "harness-custom-vars",
        ]
          .filter(Boolean)
          .join(" ")}
        style={{ position: "absolute", bottom: "20px", right: "20px" }}
      >
        <Chatbot
          ref={chatbotRef}
          colorScheme={isDark ? "dark" : "light"}
          showLauncher={showLauncher}
          sourcesVariant={hasSourcesList ? "list" : "chips"}
          showTimestamps={hasTimestamps}
          initialMessage="Chiedimi pure qualcosa"
          nameChatbot="Openk9"
          tenant="https://k9-frontend.openk9.io"
          // endpoint del RAG: "chat-tool" (default) lascia decidere all'agente se
          // consultare la knowledge base, "chat" passa sempre dal retrieval
          ragMode="chat-tool"
          // lo slot accetta un nodo qualsiasi, quindi l'informativa puo' avere uno
          // stato suo: qui e' un banner richiudibile. Sta in `bottom`, sotto
          // l'input; spostarlo in `top` lo porta sopra la lista dei messaggi
          aiDisclosure={{ bottom: <AiDisclosureBanner /> }}
          icon={{
            buttonIcon: <Logo size={35} color="white" />,
            userIcon: <User />,
            chatbotIcon: <Logo size={25} color="currentColor" />,
            refreshChatIcon: <TrashIcon />,
            searchIcon: <SearchIcon />,
            logoIcon: <Logo size={35} color="currentColor" />,
            closeIcon: <CloseIcon size="30px" color={"white"} />,
            closeModal: <CloseIcon size="18px" color="currentColor" />,
          }}
          title={
            <h3 className="harness-title">
              Chatta con <span style={{ fontWeight: 700 }}>Openk9</span>
            </h3>
          }
        />
      </div>
    </React.Fragment>
  );
}

export default App;

