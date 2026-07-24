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

import { debounce } from "lodash";
import React from "react";
import { useTranslation } from "react-i18next";
import Markdown from "react-markdown";
import { css, keyframes } from "styled-components";
import "./app.css";
import "./index.css";
import "./ScrollBar.css";
import { Logo } from "./components/Logo";
// il demo consuma la stessa superficie pubblica di un embedder reale: hook e tipi
// arrivano dall'entry del package, non da percorsi interni `./components/*`
import { type ChatSource } from "./components/client";
import { OpenK9 } from "./embeddable/entry";

const isOAuth2Enabled = import.meta.env.VITE_OAUTH2_ENABLED !== "false";
const isGenerativeEnabled = import.meta.env.VITE_GENERATIVE_ENABLED === "true";

export const openk9 = new OpenK9({
  enabled: true,
  searchAutoselect: false,
  searchReplaceText: false,
  memoryResults: false,
  useGenerativeApi: isGenerativeEnabled,
  useOAuth2: isOAuth2Enabled,
  queryStringMap: { filters: "filtri" },
  useQueryAnalysis: false,
  showSyntax: false,
  autocompleteEnabled: true,
});

const RED = "var(--openk9-embeddable-search--primary-color, #c0272b)";
const PAGE_BG = "#f5f6f8";
const BORDER = "#e5e7eb";
const MUTED = "#6b7280";

// ---- stile condiviso delle sezioni ---------------------------------------
// un unico sistema visivo per tutte le aree (navbar, filtri, risultati, K9 IA,
// anteprima, fonti): stesso contenitore (bordo/raggio/ombra) e stessi header e
// titoli, così le colonne risultano uniformi tra loro.
const panelStyle = css`
  background: #fff;
  border: 1px solid ${BORDER};
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
`;

const sectionHeaderStyle = css`
  padding: 16px 20px;
  border-bottom: 1px solid ${BORDER};
`;

const sectionHeaderRowStyle = css`
  ${sectionHeaderStyle}
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const sectionTitleStyle = css`
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1e1c21;
`;

const sectionSubtitleStyle = css`
  margin: 6px 0 0;
  font-size: 13px;
  color: ${MUTED};
`;

// il backend risponde con questo testo quando non trova nulla in knowledge base:
// in quel caso non c'è una risposta utile, quindi non mostriamo la CTA verso K9 IA
const NO_ANSWER_PATTERN = /no information found in the knowledge base/i;

// ---- pannello Filtri (reali, stile del mock) -----------------------------
// I filtri veri di OpenK9 vengono montati via `filtersConfigurable` /
// `removeFiltersConfigurable`; il markup iniettato dal widget viene ristilizzato
// per combaciare con il pannello mockato tramite selettori annidati (scoped
// dalla classe wrapper, quindi vincono per specificità senza `!important`).

function RealFiltersPanel() {
  return (
    <div
      className="openk9-mock-filters"
      css={css`
        ${panelStyle}
        grid-area: filters;
        display: flex;
        flex-direction: column;
        overflow: auto;
        @media (max-width: 1024px) {
          display: none;
        }

        /* --- ristilizzazione dei filtri reali per matchare il mock --- */
        /* la search per-categoria si apre con animazione dalla lente */
        .openk9-filter-category-container-search {
          margin: 2px 0 12px;
        }
        .openk9-filter-category-container {
          /* separatore delicato, sempre presente, sotto le suggestion */
          border-bottom: 1px solid #eef0f2;
          padding: 14px 0;
          margin-bottom: 0;
        }
        .openk9-filter-category-title {
          margin-left: 0;
          padding: 0 0 8px;
          /* separatore persistente tra nome categoria e ricerca */
          border-bottom: 1px solid #eef0f2;
        }
        .openk9-filter-category-title strong,
        .name-category-filter {
          text-transform: uppercase;
          font-size: 13px;
          font-weight: 700;
          letter-spacing: 0.3px;
          color: ${MUTED};
        }
        /* pulsanti lente/chevron senza box, così non competono coi bordi */
        .openk9-toggle-search-button,
        .openk9-collapsable-filters,
        .openk9-mobile-collapsable-filters {
          border: none;
          background: transparent;
          padding: 4px;
          color: ${MUTED};
        }
        /* la search filtri (riga a tutta larghezza) usa lo stile del componente */
        .openk9-filter-form-check-container {
          padding-left: 0;
          gap: 10px;
          margin-top: 12px;
        }
        .form-check {
          align-items: center;
        }
        /* checkbox nativo con accent rosso, come nel mock (non il quadro custom) */
        .form-check-input {
          appearance: auto;
          -webkit-appearance: auto;
          accent-color: ${RED};
          width: 16px;
          height: 16px;
          min-width: 16px;
          min-height: 16px;
          border: none;
          border-radius: 0;
          background-color: initial;
          margin-right: 10px;
          cursor: pointer;
        }
        .form-check-label {
          font-weight: 400;
          color: #1e1c21;
          font-size: 14px;
          line-height: 1.4;
        }
        .openk9-container-load-more {
          justify-content: flex-start;
          margin-left: 0;
        }
        .openk9-load-more-button {
          color: ${RED};
          font-weight: 600;
        }
      `}
    >
      <div
        css={css`
          ${sectionHeaderRowStyle}
        `}
      >
        <span
          css={css`
            ${sectionTitleStyle}
          `}
        >
          Filtri
        </span>
        <div
          className="openk9-mock-filters-clear"
          css={css`
            /* il mock aveva solo il testo rosso, senza icona */
            svg,
            .fa,
            [class*="icon"] {
              display: none !important;
            }
            button {
              border: none;
              background: none;
              color: ${RED};
              font-size: 13px;
              font-weight: 600;
              cursor: pointer;
              padding: 0;
              display: inline-flex;
              align-items: center;
              gap: 0;
            }
          `}
          ref={(element) =>
            openk9.updateConfiguration({
              removeFiltersConfigurable: {
                element,
                itemsRemove: ["filters"],
                label: "Cancella tutto",
              },
            })
          }
        />
      </div>

      <div
        css={css`
          flex: 1;
          box-sizing: border-box;
          overflow-x: hidden;
          overflow-y: auto;
          padding-block: 0 16px;
          padding-inline: 8px;
        `}
        ref={(element) =>
          openk9.updateConfiguration({
            filtersConfigurable: { element, haveSearch: true, showCount: true },
          })
        }
      />
    </div>
  );
}

// ---- colonna Fonti utilizzate (dati reali: eventi DOCUMENT della risposta) --

function SourcesColumn({ sources }: { sources: Array<ChatSource> }) {
  return (
    <div
      css={css`
        ${panelStyle}
        grid-area: detail;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        @media (max-width: 1024px) {
          display: none;
        }
      `}
    >
      <div
        css={css`
          ${sectionHeaderStyle}
        `}
      >
        <span
          css={css`
            ${sectionTitleStyle}
          `}
        >
          <span aria-hidden="true">📖</span> Fonti utilizzate
        </span>
        <p
          css={css`
            ${sectionSubtitleStyle}
          `}
        >
          Le fonti che hanno contribuito alla risposta generata da K9 IA.
        </p>
      </div>
      <div
        css={css`
          flex: 1;
          overflow: auto;
          padding: 12px 16px;
          display: flex;
          flex-direction: column;
          gap: 12px;
        `}
      >
        {sources.length === 0 && (
          <div
            css={css`
              color: ${MUTED};
              font-size: 13px;
              padding: 24px 16px;
              text-align: center;
            `}
          >
            Le fonti compaiono quando K9 IA genera una risposta.
          </div>
        )}
        {sources.map((source, index) => {
          const title = source.title || source.source || source.url || "Fonte";
          return (
            <div
              key={(source.url ?? "") + index}
              css={css`
                border: 1px solid ${BORDER};
                border-radius: 10px;
                padding: 14px;
              `}
            >
              <div
                css={css`
                  display: flex;
                  justify-content: space-between;
                  gap: 10px;
                  align-items: flex-start;
                `}
              >
                <div
                  css={css`
                    display: flex;
                    gap: 10px;
                    align-items: flex-start;
                  `}
                >
                  <span
                    aria-hidden="true"
                    css={css`
                      flex-shrink: 0;
                      width: 34px;
                      height: 34px;
                      background: ${RED};
                      color: #fff;
                      border-radius: 8px;
                      display: inline-flex;
                      align-items: center;
                      justify-content: center;
                      font-size: 15px;
                      font-weight: 800;
                    `}
                  >
                    {title.charAt(0).toUpperCase()}
                  </span>
                  <span
                    css={css`
                      font-size: 14px;
                      font-weight: 700;
                      color: #1e1c21;
                      line-height: 1.3;
                    `}
                  >
                    {title}
                  </span>
                </div>
                <span
                  css={css`
                    flex-shrink: 0;
                    font-size: 11px;
                    font-weight: 600;
                    color: #16a34a;
                    background: #dcfce7;
                    border-radius: 999px;
                    padding: 3px 8px;
                    white-space: nowrap;
                  `}
                >
                  Usata nella risposta
                </span>
              </div>
              {source.url && (
                <a
                  href={source.url}
                  target="_blank"
                  rel="noreferrer"
                  css={css`
                    display: block;
                    margin: 8px 0 0;
                    font-size: 12px;
                    color: #2563eb;
                    word-break: break-all;
                  `}
                >
                  {source.url}
                </a>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ---- indicatore "sta scrivendo" ------------------------------------------

const blink = keyframes`
  0%, 80%, 100% { opacity: 0.2; }
  40% { opacity: 1; }
`;

function TypingDots() {
  return (
    <span
      css={css`
        display: inline-flex;
        gap: 4px;
        span {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: ${MUTED};
          animation: ${blink} 1.2s infinite ease-in-out both;
        }
        span:nth-of-type(2) {
          animation-delay: 0.2s;
        }
        span:nth-of-type(3) {
          animation-delay: 0.4s;
        }
      `}
    >
      <span />
      <span />
      <span />
    </span>
  );
}

// ---- App -----------------------------------------------------------------

export function App() {
  return <AppInner />;
}

function AppInner() {
  const [view, setView] = React.useState<"results" | "ai">("results");

  return (
    <>
      <div
        id="openk9-body"
        className="openk9-body"
        css={css`
          background: ${PAGE_BG};
          width: 100vw;
          height: 100vh;
          box-sizing: border-box;
          display: grid;
          gap: 20px;
          padding: 20px;
          grid-template-columns: ${view === "ai"
            ? "1fr 380px"
            : "300px 1fr 380px"};
          grid-template-rows: auto 1fr;
          grid-template-areas: ${view === "ai"
            ? '"dockbar dockbar" "dialog detail"'
            : '"dockbar dockbar dockbar" "filters dialog detail"'};

          @media (max-width: 1024px) {
            grid-template-columns: 1fr;
            grid-template-rows: auto 1fr;
            grid-template-areas:
              "dockbar"
              "dialog";
          }
        `}
      >
        {/* ---- Navbar: logo + search reale + login/lingua reali ---- */}
        <div
          css={css`
            ${panelStyle}
            grid-area: dockbar;
            padding: 10px 20px;
            display: flex;
            align-items: center;
            gap: 20px;
            @media (max-width: 768.98px) {
              flex-wrap: wrap;
            }
          `}
        >
          <div
            css={css`
              display: flex;
              align-items: center;
              font-size: 20px;
              color: #1e1c21;
              flex-shrink: 0;
            `}
          >
            <span
              css={css`
                color: ${RED};
                margin-right: 8px;
              `}
            >
              <Logo size={32} />
            </span>
            <span>Open</span>
            <span
              css={css`
                font-weight: 700;
              `}
            >
              K9
            </span>
          </div>
          <div
            css={css`
              flex: 1;
              min-width: 240px;
              /* in vista K9 IA l'input di ricerca sparisce (c'è già la chat) */
              display: ${view === "ai" ? "none" : "block"};
            `}
            ref={(element) =>
              openk9.updateConfiguration({ searchWithButton: element })
            }
          />
          <div
            css={css`
              display: flex;
              align-items: center;
              gap: 10px;
              flex-shrink: 0;
              /* i pulsanti restano a destra anche quando la search è nascosta */
              margin-left: auto;
            `}
          >
            <div
              ref={(element) =>
                openk9.updateConfiguration({ changeLanguage: element })
              }
            />
            <div
              ref={(element) => openk9.updateConfiguration({ login: element })}
            />
          </div>
        </div>

        {/* ---- Colonna sinistra: Filtri reali — nascosti in vista K9 IA ---- */}
        {view === "results" && <RealFiltersPanel />}

        {/* ---- Colonne centrale + destra: gestite dal componente ad hoc ---- */}
        <K9Copilot view={view} setView={setView} />
      </div>
      {/* target del preview mobile: il widget vi monta l'overlay ModalDetail
          aperto dal pill "Anteprima" sui risultati sotto i 1024px */}
      <div
        ref={(element) => openk9.updateConfiguration({ detailMobile: element })}
      />
    </>
  );
}

type K9CopilotProps = {
  view: "results" | "ai";
  setView: (view: "results" | "ai") => void;
};

// componente ad hoc (non esportato): possiede tutta la gestione del Copilot —
// stato, effetti, motore chat — e rende conversazione, CTA e colonna fonti.
// Il client arriva dall'oggetto `openk9` (nessun provider di context necessario).
function K9Copilot({ view, setView }: K9CopilotProps) {
  const { t, i18n } = useTranslation();
  const [searchText, setSearchText] = React.useState("");
  const [numberOfResults, setNumberOfResults] = React.useState(0);
  const [language, setLanguage] = React.useState(i18n.language);
  const [input, setInput] = React.useState("");
  const [pending, setPending] = React.useState<string | null>(null);
  // azioni K9 IA generate dinamicamente dal contesto cercato
  const [actions, setActions] = React.useState<Array<string>>([]);
  const [actionsLoading, setActionsLoading] = React.useState(false);
  const lastQueryRef = React.useRef<string | null>(null);
  const threadRef = React.useRef<HTMLDivElement | null>(null);
  const inputRef = React.useRef<HTMLTextAreaElement | null>(null);

  // motore reale del Copilot: l'hook è esposto sull'oggetto openk9 e usa il suo
  // client (niente provider, niente import interni — è la superficie di embedding)
  const { messages, isChatting, send, reset } = openk9.useCopilotChat();
  const preview = messages[0];
  const currentSources = messages[messages.length - 1]?.sources ?? [];
  // la risposta è "vuota" quando il backend non ha trovato nulla in KB
  const previewHasNoAnswer =
    preview?.status === "END" && NO_ANSWER_PATTERN.test(preview.answer ?? "");

  // lingua reale del bucket per allineare l'AI ai risultati
  React.useEffect(() => {
    openk9.client
      .getLanguageDefault()
      .then((data) => setLanguage(data.value))
      .catch(() => {});
  }, []);

  // testo di ricerca + numero risultati dagli eventi openk9
  React.useEffect(() => {
    const debounced = debounce(
      (queryState: {
        numberOfResults: number;
        searchTokens: Array<{ values?: Array<string> }>;
      }) => {
        setSearchText(queryState.searchTokens?.[0]?.values?.[0] ?? "");
        setNumberOfResults(queryState.numberOfResults);
      },
      250,
    );
    openk9.addEventListener("queryStateChange", debounced);
    return () => openk9.removeEventListener("queryStateChange", debounced);
  }, []);

  // a ogni nuova ricerca: azzera la conversazione e prepara la prima domanda
  React.useEffect(() => {
    const query = searchText.trim();
    if (!query) {
      lastQueryRef.current = null;
      setPending(null);
      reset();
      return;
    }
    if (lastQueryRef.current === query) return;
    lastQueryRef.current = query;
    reset();
    setPending(query);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchText]);

  // quando la conversazione è stata azzerata, invia la prima domanda (history vuota)
  React.useEffect(() => {
    if (pending && messages.length === 0 && !isChatting) {
      send({ question: pending, searchText: pending, language });
      setPending(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pending, messages.length, isChatting]);

  // le azioni contestuali si azzerano appena parte una nuova interazione
  React.useEffect(() => {
    if (isChatting) setActions([]);
  }, [isChatting]);

  // ...e vengono rigenerate ad ogni interazione, appena la risposta è completa,
  // sul contesto dell'ultima domanda posta
  React.useEffect(() => {
    const last = messages[messages.length - 1];
    if (!last || last.status !== "END" || !last.answer) return;
    let cancelled = false;
    setActionsLoading(true);
    openk9.client
      .getRefinedSearches({ searchText: last.question, language })
      .then((result) => {
        if (!cancelled) setActions(result);
      })
      .catch(() => {
        if (!cancelled) setActions([]);
      })
      .finally(() => {
        if (!cancelled) setActionsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [messages, language]);

  // autoscroll del thread
  React.useEffect(() => {
    if (threadRef.current) {
      threadRef.current.scrollTop = threadRef.current.scrollHeight;
    }
  }, [messages]);

  const submitFollowUp = () => {
    const value = input.trim();
    if (!value || isChatting) return;
    setInput("");
    if (inputRef.current) inputRef.current.style.height = "auto";
    send({ question: value, searchText: value, language });
  };

  return (
    <>
      {/* ---- Colonna centrale: risultati oppure conversazione K9 IA ---- */}
      <div
        css={css`
          ${panelStyle}
          grid-area: dialog;
          display: flex;
          flex-direction: column;
          overflow: hidden;
          min-height: 0;
        `}
      >
        {view === "results" ? (
          <>
            {/* header risultati */}
            <div
              css={css`
                ${sectionHeaderRowStyle}
              `}
            >
              <span
                css={css`
                  ${sectionTitleStyle}
                `}
              >
                {t("number-of-results")}
                <span
                  css={css`
                    color: ${RED};
                    margin-left: 6px;
                  `}
                >
                  {numberOfResults}
                </span>
              </span>
            </div>

            {/* micro-anteprima della risposta AI + CTA "Vai a K9 IA":
                nascosta quando il backend non ha una risposta utile */}
            {!previewHasNoAnswer && (
              <div
                css={css`
                  display: flex;
                  align-items: center;
                  gap: 14px;
                  margin: 14px 20px 0;
                  padding: 12px 14px;
                  border: 1px solid ${BORDER};
                  border-left: 3px solid ${RED};
                  border-radius: 10px;
                  background: #fafafa;
                `}
              >
                <span aria-hidden="true" style={{ fontSize: 20 }}>
                  ✨
                </span>
                <div
                  css={css`
                    flex: 1;
                    min-width: 0;
                    font-size: 13px;
                    line-height: 1.4;
                    color: ${MUTED};
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    -webkit-box-orient: vertical;
                    overflow: hidden;
                  `}
                >
                  {preview ? (
                    preview.answer ? (
                      preview.answer
                    ) : (
                      <TypingDots />
                    )
                  ) : (
                    "Chiedi a K9 IA di aiutarti con questi risultati."
                  )}
                </div>
                <button
                  type="button"
                  onClick={() => setView("ai")}
                  css={css`
                    flex-shrink: 0;
                    display: inline-flex;
                    align-items: center;
                    gap: 6px;
                    border: 1px solid ${RED};
                    background: ${RED};
                    color: #fff;
                    border-radius: 8px;
                    padding: 8px 14px;
                    font-size: 13px;
                    font-weight: 700;
                    cursor: pointer;
                    white-space: nowrap;
                  `}
                >
                  Vai a K9 IA →
                </button>
              </div>
            )}

            {/* risultati reali */}
            <div
              className="openk9-results-container openk9-box"
              ref={(element) =>
                openk9.updateConfiguration({
                  resultList: {
                    element,
                    changeOnOver: true,
                  },
                })
              }
              css={css`
                flex: 1;
                overflow: auto;
                /* sfondo lista uguale a quello di pagina: le card bianche
                   risaltano invece di confondersi col pannello */
                background: ${PAGE_BG};
                /* padding-block generoso + scroll-padding così il bordo/raggio
                   della card non viene tagliato di netto contro il bordo dello
                   scroll: entra/esce sfumando (mask) invece di troncarsi */
                padding: 16px 20px;
                scroll-padding-block: 16px;
                display: flex;
                flex-direction: column;
                gap: 10px;
                mask-image: linear-gradient(
                  to bottom,
                  transparent 0,
                  #000 16px,
                  #000 calc(100% - 16px),
                  transparent 100%
                );
                -webkit-mask-image: linear-gradient(
                  to bottom,
                  transparent 0,
                  #000 16px,
                  #000 calc(100% - 16px),
                  transparent 100%
                );
                /* il pill "Anteprima" iniettato dal widget su tablet/mobile:
                   lo avviciniamo alla card e lo allineiamo a destra */
                .openk9-wrapper-button-mobile {
                  display: flex;
                  justify-content: flex-end;
                  margin-top: -6px;
                }
              `}
            />
          </>
        ) : (
          <>
            {/* header conversazione */}
            <div
              css={css`
                ${sectionHeaderRowStyle}
              `}
            >
              <span
                css={css`
                  ${sectionTitleStyle}
                `}
              >
                <span aria-hidden="true">✨</span> {t("copilot-toggle")}
              </span>
              <span
                css={css`
                  display: inline-flex;
                  align-items: center;
                  gap: 6px;
                  font-size: 13px;
                  font-weight: 600;
                  color: #16a34a;
                  background: #dcfce7;
                  border-radius: 999px;
                  padding: 5px 12px;
                `}
              >
                AI attiva ▾
              </span>
            </div>

            <div
              css={css`
                padding: 12px 20px;
                border-bottom: 1px solid ${BORDER};
              `}
            >
              <button
                type="button"
                onClick={() => setView("results")}
                css={css`
                  display: inline-flex;
                  align-items: center;
                  gap: 8px;
                  border: 1px solid ${BORDER};
                  background: #fff;
                  border-radius: 8px;
                  padding: 8px 14px;
                  font-size: 14px;
                  font-weight: 600;
                  color: #1e1c21;
                  cursor: pointer;
                  &:hover {
                    border-color: ${RED};
                    color: ${RED};
                  }
                `}
              >
                ← Torna ai risultati
              </button>
            </div>

            {/* thread */}
            <div
              ref={threadRef}
              css={css`
                flex: 1;
                overflow-y: auto;
                padding: 16px 20px;
                display: flex;
                flex-direction: column;
                gap: 16px;
                min-height: 0;
              `}
            >
              {messages.length === 0 && (
                <div
                  css={css`
                    margin: auto;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    text-align: center;
                    gap: 16px;
                    padding: 24px;
                    max-width: 640px;
                  `}
                >
                  <div
                    aria-hidden="true"
                    css={css`
                      width: 72px;
                      height: 72px;
                      border-radius: 50%;
                      background: #fdeaea;
                      color: ${RED};
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      font-size: 30px;
                    `}
                  >
                    💬
                  </div>
                  <div>
                    <h3
                      css={css`
                        margin: 0;
                        font-size: 18px;
                        color: #1e1c21;
                      `}
                    >
                      Fai una domanda sui risultati
                    </h3>
                    <p
                      css={css`
                        margin: 6px 0 0;
                        font-size: 14px;
                        color: ${MUTED};
                      `}
                    >
                      K9 IA analizza i risultati trovati e ti fornisce risposte
                      chiare e basate sulle fonti.
                    </p>
                  </div>
                </div>
              )}
              {messages.map((message, index) => (
                <div
                  key={index}
                  css={css`
                    display: flex;
                    flex-direction: column;
                    gap: 8px;
                  `}
                >
                  <div
                    css={css`
                      align-self: flex-end;
                      background: ${RED};
                      color: #fff;
                      border-radius: 14px 14px 2px 14px;
                      padding: 8px 12px;
                      max-width: 85%;
                      line-height: 1.4;
                    `}
                  >
                    {message.question}
                  </div>
                  <div
                    css={css`
                      align-self: flex-start;
                      background: #fbfbfc;
                      border: 1px solid ${BORDER};
                      border-radius: 14px 14px 14px 2px;
                      padding: 10px 14px;
                      max-width: 90%;
                      line-height: 1.5;
                      p {
                        margin: 0 0 8px;
                      }
                      p:last-child {
                        margin-bottom: 0;
                      }
                      a {
                        color: ${RED};
                      }
                    `}
                  >
                    {message.status === "CHUNK" && message.answer === "" ? (
                      <TypingDots />
                    ) : (
                      <Markdown>{message.answer}</Markdown>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* pulsanti azione K9 IA generati dinamicamente dal contesto cercato */}
            {(actionsLoading || actions.length > 0) && (
              <div
                css={css`
                  display: flex;
                  flex-wrap: wrap;
                  align-items: center;
                  gap: 10px;
                  padding: 12px 20px;
                  border-top: 1px solid ${BORDER};
                `}
              >
                {actionsLoading && actions.length === 0 && (
                  <span
                    css={css`
                      display: inline-flex;
                      align-items: center;
                      gap: 8px;
                      font-size: 13px;
                      color: ${MUTED};
                    `}
                  >
                    <TypingDots /> Azioni contestuali…
                  </span>
                )}
                {actions.map((action) => (
                  <button
                    key={action}
                    type="button"
                    disabled={isChatting}
                    onClick={() =>
                      send({ question: action, searchText: action, language })
                    }
                    css={css`
                      border: 1px solid ${BORDER};
                      background: #fff;
                      border-radius: 8px;
                      padding: 8px 14px;
                      font-size: 14px;
                      font-weight: 600;
                      color: #1e1c21;
                      cursor: pointer;
                      text-align: left;
                      &:hover {
                        border-color: ${RED};
                        color: ${RED};
                      }
                      &:disabled {
                        opacity: 0.5;
                        cursor: default;
                      }
                    `}
                  >
                    {action}
                  </button>
                ))}
              </div>
            )}

            {/* input follow-up (textarea) */}
            <div
              css={css`
                padding: 12px 20px 8px;
              `}
            >
              <div
                css={css`
                  display: flex;
                  align-items: flex-end;
                  gap: 8px;
                  padding: 8px 8px 8px 14px;
                  border: 1px solid ${BORDER};
                  border-radius: 14px;
                  background: #fff;
                  transition: border-color 120ms ease, box-shadow 120ms ease;
                  &:focus-within {
                    border-color: ${RED};
                    box-shadow: 0 0 0 3px rgba(192, 39, 43, 0.12);
                  }
                `}
              >
                <textarea
                  ref={inputRef}
                  rows={1}
                  value={input}
                  placeholder={
                    t("copilot-input-placeholder") ?? "Fai una domanda…"
                  }
                  onChange={(event) => {
                    setInput(event.target.value);
                    const el = event.target;
                    el.style.height = "auto";
                    el.style.height = `${Math.min(el.scrollHeight, 120)}px`;
                  }}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" && !event.shiftKey) {
                      event.preventDefault();
                      submitFollowUp();
                    }
                  }}
                  css={css`
                    flex: 1;
                    border: none;
                    outline: none;
                    resize: none;
                    background: transparent;
                    font-family: inherit;
                    font-size: 14px;
                    line-height: 1.5;
                    max-height: 120px;
                    padding: 6px 0;
                    color: #1e1c21;
                  `}
                />
                <button
                  type="button"
                  onClick={submitFollowUp}
                  disabled={isChatting || input.trim() === ""}
                  css={css`
                    flex-shrink: 0;
                    width: 36px;
                    height: 36px;
                    border: none;
                    border-radius: 50%;
                    background: ${RED};
                    color: #fff;
                    cursor: pointer;
                    font-size: 15px;
                    &:disabled {
                      opacity: 0.4;
                      cursor: default;
                    }
                  `}
                >
                  ➤
                </button>
              </div>
            </div>

            <div
              css={css`
                padding: 0 20px 14px;
                text-align: center;
                font-size: 12px;
                color: ${MUTED};
              `}
            >
              🔒 Le risposte di K9 IA possono contenere imprecisioni. Verifica
              sempre le informazioni importanti.
            </div>
          </>
        )}
      </div>

      {/* ---- Colonna destra: anteprima (risultati) o fonti (K9 IA) ---- */}
      {view === "ai" ? (
        <SourcesColumn sources={currentSources} />
      ) : (
        <div
          className="openk9-preview-container openk9-box"
          ref={(element) => openk9.updateConfiguration({ details: element })}
          css={css`
            ${panelStyle}
            grid-area: detail;
            overflow-y: auto;
            overflow-x: hidden;
            @media (max-width: 1024px) {
              display: none;
            }
          `}
        />
      )}
    </>
  );
}
