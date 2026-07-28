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

function RealFiltersPanel({
  mobileOpen,
  onClose,
}: {
  mobileOpen: boolean;
  onClose: () => void;
}) {
  const panelRef = React.useRef<HTMLDivElement | null>(null);
  const restoreFocusRef = React.useRef<HTMLElement | null>(null);

  // focus trap: attiva solo quando la modale è realmente presentata (≤1024px).
  // porta il focus dentro, cicla il Tab, chiude con Escape e ripristina il
  // focus al trigger in chiusura.
  React.useEffect(() => {
    if (!mobileOpen) return;
    if (!window.matchMedia("(max-width: 1024px)").matches) return;
    const panel = panelRef.current;
    if (!panel) return;

    restoreFocusRef.current = document.activeElement as HTMLElement | null;

    const getFocusable = () =>
      Array.from(
        panel.querySelectorAll<HTMLElement>(
          'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
        ),
      ).filter((el) => el.offsetParent !== null);

    getFocusable()[0]?.focus();

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        event.stopPropagation();
        onClose();
        return;
      }
      if (event.key !== "Tab") return;
      const items = getFocusable();
      if (items.length === 0) return;
      const first = items[0];
      const last = items[items.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };

    panel.addEventListener("keydown", onKeyDown);
    return () => {
      panel.removeEventListener("keydown", onKeyDown);
      restoreFocusRef.current?.focus?.();
    };
  }, [mobileOpen, onClose]);

  return (
    <>
      {mobileOpen && (
        <div
          aria-hidden="true"
          onClick={onClose}
          css={css`
            display: none;
            @media (max-width: 1024px) {
              display: block;
              position: fixed;
              inset: 0;
              background: rgba(0, 0, 0, 0.35);
              z-index: 1000;
            }
          `}
        />
      )}
      <div
        ref={panelRef}
        className="openk9-mock-filters"
        role={mobileOpen ? "dialog" : undefined}
        aria-modal={mobileOpen ? true : undefined}
        aria-label="Filtri"
        css={css`
          ${panelStyle}
          grid-area: filters;
          position: relative;
          display: flex;
          flex-direction: column;
          overflow: auto;
          /* su tablet/mobile diventa una modale a tutto schermo che sale dal basso */
          @media (max-width: 1024px) {
            position: fixed;
            inset: 0;
            width: 100%;
            max-width: none;
            height: 100%;
            max-height: none;
            margin: 0;
            z-index: 1001;
            border-radius: 0;
            box-shadow: none;
            transform: translateY(${mobileOpen ? "0" : "100%"});
            transition: transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
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
            justify-content: center;
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
          <button
            type="button"
            aria-label="Chiudi i filtri"
            onClick={onClose}
            css={css`
              display: none;
              @media (max-width: 1024px) {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 32px;
                height: 32px;
                border: none;
                background: transparent;
                color: ${MUTED};
                cursor: pointer;
                font-size: 22px;
                line-height: 1;
              }
              &:hover {
                color: ${RED};
              }
            `}
          >
            ×
          </button>
        </div>

        <div
          className="openk9-mock-filters-clear"
          css={css`
            /* desktop: in alto a destra dentro l'header */
            position: absolute;
            top: 16px;
            right: 20px;
            /* mobile: sotto il separatore, prima dei filtri, così non si
             confonde con la × di chiusura in alto a destra */
            @media (max-width: 1024px) {
              position: static;
              display: flex;
              justify-content: flex-end;
              padding: 12px 20px 0;
            }
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
              filtersConfigurable: {
                element,
                haveSearch: true,
                showCount: true,
                selectedAsChips: false,
              },
            })
          }
        />
      </div>
    </>
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
          <span
            aria-hidden="true"
            css={css`
              display: inline-flex;
              color: ${RED};
            `}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path
                fill="currentColor"
                opacity="0.5"
                d="M11 4H6a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h5V4z"
              />
              <path
                fill="currentColor"
                d="M13 4h5a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2h-5V4z"
              />
            </svg>
          </span>{" "}
          Fonti utilizzate
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
              flex: 1;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              gap: 18px;
              padding: 24px 16px;
              text-align: center;
            `}
          >
            <svg
              aria-hidden="true"
              width="150"
              height="120"
              viewBox="0 0 150 120"
              fill="none"
              css={css`
                color: ${RED};
              `}
            >
              {/* documenti */}
              <rect
                x="52"
                y="16"
                width="54"
                height="72"
                rx="8"
                fill="#eef0f2"
              />
              <rect
                x="38"
                y="24"
                width="54"
                height="72"
                rx="8"
                fill="#ffffff"
                stroke="#e5e7eb"
                strokeWidth="2"
              />
              <rect x="46" y="36" width="30" height="4" rx="2" fill="#e5e7eb" />
              <rect x="46" y="46" width="38" height="4" rx="2" fill="#e5e7eb" />
              <rect x="46" y="56" width="22" height="4" rx="2" fill="#e5e7eb" />
              {/* mini bar chart */}
              <rect
                x="46"
                y="80"
                width="5"
                height="8"
                rx="1"
                fill="currentColor"
                opacity="0.55"
              />
              <rect
                x="54"
                y="74"
                width="5"
                height="14"
                rx="1"
                fill="currentColor"
                opacity="0.55"
              />
              <rect
                x="62"
                y="70"
                width="5"
                height="18"
                rx="1"
                fill="currentColor"
                opacity="0.55"
              />
              {/* lente */}
              <circle
                cx="98"
                cy="74"
                r="16"
                fill="#ffffff"
                stroke="currentColor"
                strokeWidth="4"
              />
              <line
                x1="110"
                y1="86"
                x2="122"
                y2="98"
                stroke="currentColor"
                strokeWidth="4"
                strokeLinecap="round"
              />
              {/* sparkle */}
              <path
                d="M120 30 l2 5 5 2 -5 2 -2 5 -2 -5 -5 -2 5 -2 z"
                fill="currentColor"
                opacity="0.4"
              />
            </svg>
            <p
              css={css`
                margin: 0;
                max-width: 240px;
                color: ${MUTED};
                font-size: 13px;
                line-height: 1.5;
              `}
            >
              Le fonti compaiono quando K9 IA genera una risposta.
            </p>
            <div
              css={css`
                display: flex;
                gap: 10px;
                text-align: left;
                background: color-mix(in srgb, ${RED} 7%, #fff);
                border: 1px solid color-mix(in srgb, ${RED} 18%, #fff);
                border-radius: 12px;
                padding: 12px 14px;
              `}
            >
              <span
                aria-hidden="true"
                css={css`
                  flex-shrink: 0;
                  color: ${RED};
                  margin-top: 1px;
                `}
              >
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm0 4.5a1.4 1.4 0 1 1 0 2.8 1.4 1.4 0 0 1 0-2.8zM13.3 18h-2.6v-7h2.6z" />
                </svg>
              </span>
              <div>
                <div
                  css={css`
                    font-weight: 700;
                    font-size: 13px;
                    color: ${RED};
                    margin-bottom: 2px;
                  `}
                >
                  Suggerimento
                </div>
                <div
                  css={css`
                    font-size: 12.5px;
                    line-height: 1.45;
                    color: ${MUTED};
                  `}
                >
                  Più dettagli fornisci nella tua domanda, più precise e
                  rilevanti saranno le risposte e le fonti mostrate.
                </div>
              </div>
            </div>
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
  const { t } = useTranslation();
  return (
    <span
      role="status"
      aria-label={t("copilot-loading") ?? ""}
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
  const [mobileFiltersOpen, setMobileFiltersOpen] = React.useState(false);
  const closeMobileFilters = React.useCallback(
    () => setMobileFiltersOpen(false),
    [],
  );

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
              /* allinea alla colonna filtri (300) − gap (20) così la search
                 parte esattamente sopra la colonna risultati */
              flex: ${view === "ai" ? "1 1 auto" : "0 0 280px"};
              @media (max-width: 1024px) {
                flex: 0 0 auto;
              }
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
              justify-content: flex-end;
              gap: 10px;
              /* stessa larghezza della colonna preview (380) − gap (20),
                 così i controlli sono allineati alla colonna di destra */
              flex: 0 0 360px;
              @media (max-width: 1024px) {
                flex: 1 1 100%;
                margin-left: 0;
              }
            `}
          >
            {view === "results" && (
              <button
                type="button"
                onClick={() => setMobileFiltersOpen(true)}
                aria-label="Apri i filtri"
                css={css`
                  /* solo icona, visibile quando la colonna filtri è nascosta;
                     spinta a sinistra, opposta a lingua/login */
                  display: none;
                  @media (max-width: 1024px) {
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    flex: 0 0 auto;
                    margin-right: auto;
                    width: 42px;
                    height: 40px;
                    border: 1px solid color-mix(in srgb, ${RED} 22%, ${BORDER});
                    border-radius: 10px;
                    background: color-mix(in srgb, ${RED} 8%, #fff);
                    color: ${RED};
                    cursor: pointer;
                  }
                  &:hover {
                    background: color-mix(in srgb, ${RED} 15%, #fff);
                    border-color: ${RED};
                    color: ${RED};
                  }
                `}
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M3 5h18l-7 8v5l-4 2v-7L3 5z" />
                </svg>
              </button>
            )}
            <div
              ref={(element) =>
                openk9.updateConfiguration({ changeLanguage: element })
              }
            />
            <div
              css={css`
                /* login come CTA primaria: pill pieno rosso, testo/icona bianchi */
                .openk9-create-label-container-wrapper {
                  height: 40px;
                  border-radius: 10px;
                  background: ${RED};
                  border: 1px solid ${RED};
                  color: #fff;
                }
                .openk9-create-label-container-wrapper span {
                  color: #fff;
                }
                .openk9-create-label-container-wrapper svg path {
                  fill: #fff;
                }
              `}
              ref={(element) => openk9.updateConfiguration({ login: element })}
            />
          </div>
        </div>

        {/* ---- Colonna sinistra: Filtri reali — nascosti in vista K9 IA ---- */}
        {view === "results" && (
          <RealFiltersPanel
            mobileOpen={mobileFiltersOpen}
            onClose={closeMobileFilters}
          />
        )}

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
  // NB: il setter è rimosso finché la generazione delle CTA è disabilitata
  // (vedi effect commentato più sotto). Ripristinare `, setActionsLoading`
  // quando l'endpoint backend sarà disponibile.
  const [actionsLoading] = React.useState(false);
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
  // sul contesto dell'ultima domanda posta.
  //
  // TEMPORANEAMENTE DISABILITATO: la generazione delle CTA/azioni contestuali
  // è commentata in attesa dell'endpoint backend dedicato. Riattivare questo
  // effect (e la relativa UI) quando l'endpoint sarà disponibile.
  // React.useEffect(() => {
  //   const last = messages[messages.length - 1];
  //   if (!last || last.status !== "END" || !last.answer) return;
  //   let cancelled = false;
  //   setActionsLoading(true);
  //   openk9.client
  //     .getRefinedSearches({ searchText: last.question, language })
  //     .then((result) => {
  //       if (!cancelled) setActions(result);
  //     })
  //     .catch(() => {
  //       if (!cancelled) setActions([]);
  //     })
  //     .finally(() => {
  //       if (!cancelled) setActionsLoading(false);
  //     });
  //   return () => {
  //     cancelled = true;
  //   };
  // }, [messages, language]);

  // autoscroll del thread
  React.useEffect(() => {
    if (threadRef.current) {
      threadRef.current.scrollTop = threadRef.current.scrollHeight;
    }
  }, [messages]);

  // sposta il focus sul campo domanda quando si passa alla vista K9 IA
  React.useEffect(() => {
    if (view === "ai") inputRef.current?.focus();
  }, [view]);

  const submitFollowUp = () => {
    const value = input.trim();
    if (!value || isChatting) return;
    setInput("");
    if (inputRef.current) inputRef.current.style.height = "auto";
    send({ question: value, searchText: value, language });
  };

  // avvia la conversazione da una delle card suggerite nell'empty-state
  const askPrompt = (text: string) => {
    if (isChatting) return;
    send({ question: text, searchText: text, language });
  };

  // prompt di partenza mostrati nell'empty-state (demo: testo IT hardcoded).
  // ogni card ha un'icona SVG, un accento della famiglia rossa e un piccolo
  // offset verticale, così la disposizione risulta armoniosa e non a griglia.
  const starterPrompts: {
    icon: React.ReactNode;
    accent: string;
    text: string;
  }[] = [
    {
      icon: (
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none">
          <g fill="currentColor">
            <rect x="4" y="11" width="3.5" height="8" rx="1" />
            <rect x="10.25" y="7" width="3.5" height="12" rx="1" />
            <rect x="16.5" y="4" width="3.5" height="15" rx="1" />
          </g>
        </svg>
      ),
      accent: RED,
      text: "Quali sono i trend principali emersi dai risultati?",
    },
    {
      icon: (
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none">
          <path
            fill="currentColor"
            d="M12 2a7 7 0 0 0-4 12.74V16a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1v-1.26A7 7 0 0 0 12 2z"
          />
          <rect x="9" y="19" width="6" height="2" rx="1" fill="currentColor" />
        </svg>
      ),
      accent: "#e2555a",
      text: "Quali sono le implicazioni di questi dati?",
    },
    {
      icon: (
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none">
          <path
            fill="currentColor"
            opacity="0.5"
            d="M11 4H6a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h5V4z"
          />
          <path
            fill="currentColor"
            d="M13 4h5a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2h-5V4z"
          />
        </svg>
      ),
      accent: "#b3261e",
      text: "Mostrami le fonti più rilevanti sull'argomento.",
    },
    {
      icon: (
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none">
          <circle
            cx="12"
            cy="12"
            r="8"
            stroke="currentColor"
            strokeWidth="1.8"
          />
          <circle
            cx="12"
            cy="12"
            r="4"
            stroke="currentColor"
            strokeWidth="1.8"
          />
          <circle cx="12" cy="12" r="1.6" fill="currentColor" />
        </svg>
      ),
      accent: "#d1434e",
      text: "Cosa posso approfondire ulteriormente?",
    },
  ];

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
          /* impedisce che il contenuto della chat allarghi la colonna
             (overflow orizzontale su mobile) */
          min-width: 0;
        `}
      >
        {view === "results" ? (
          <>
            {/* header risultati */}
            <div
              css={css`
                ${sectionHeaderRowStyle}
                @media (max-width: 1024px) {
                  flex-wrap: wrap;
                  gap: 8px 12px;
                }
              `}
            >
              <span
                css={css`
                  ${sectionTitleStyle}
                  @media (max-width: 1024px) {
                    font-size: 15px;
                  }
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
              {/* pulsante "Filtra per data": montato dal widget, accanto al
                  conteggio; mostra "dal X al Y" quando un intervallo è attivo */}
              <div
                ref={(element) =>
                  openk9.updateConfiguration({ dateRangeFilter: element })
                }
              />
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
                <span
                  aria-hidden="true"
                  css={css`
                    display: inline-flex;
                    color: ${RED};
                  `}
                >
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                  >
                    <path d="M12 2l1.7 5.3a4 4 0 0 0 2.5 2.5L21.5 11.5l-5.3 1.7a4 4 0 0 0-2.5 2.5L12 21l-1.7-5.3a4 4 0 0 0-2.5-2.5L2.5 11.5l5.3-1.7a4 4 0 0 0 2.5-2.5L12 2z" />
                    <path
                      d="M19 2.5l.6 1.9 1.9.6-1.9.6-.6 1.9-.6-1.9-1.9-.6 1.9-.6z"
                      opacity="0.6"
                    />
                  </svg>
                </span>{" "}
                {t("copilot-toggle")}
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
                AI attiva
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
              role="log"
              aria-live="polite"
              aria-relevant="additions text"
              aria-busy={isChatting}
              css={css`
                flex: 1;
                overflow-y: auto;
                overflow-x: hidden;
                padding: 16px 20px;
                display: flex;
                flex-direction: column;
                gap: 16px;
                min-height: 0;
                min-width: 0;
              `}
            >
              {messages.length === 0 && (
                <div
                  css={css`
                    position: relative;
                    margin: auto;
                    width: 100%;
                    max-width: 760px;
                    padding: 28px 20px;
                    @media (max-width: 620px) {
                      padding: 0px;
                    }
                  `}
                >
                  <div
                    css={css`
                      display: flex;
                      flex-direction: column;
                      align-items: center;
                      text-align: center;
                      gap: 20px;
                    `}
                  >
                    {/* illustrazione: bolla chat con alone e sparkle */}
                    <div
                      aria-hidden="true"
                      css={css`
                        position: relative;
                        width: 150px;
                        height: 120px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: ${RED};
                        &::before {
                          content: "";
                          position: absolute;
                          width: 118px;
                          height: 118px;
                          border-radius: 50%;
                          background: radial-gradient(
                            circle,
                            color-mix(in srgb, ${RED} 14%, transparent) 0%,
                            transparent 68%
                          );
                        }
                      `}
                    >
                      <svg
                        width="150"
                        height="120"
                        viewBox="0 0 150 120"
                        fill="none"
                        css={css`
                          position: relative;
                        `}
                      >
                        <defs>
                          <linearGradient
                            id="k9-bubble-grad"
                            x1="0"
                            y1="0"
                            x2="1"
                            y2="1"
                          >
                            <stop offset="0" stopColor="#ef6b6f" />
                            <stop offset="1" stopColor="#c0272b" />
                          </linearGradient>
                        </defs>
                        <rect
                          x="34"
                          y="24"
                          width="34"
                          height="26"
                          rx="10"
                          fill="currentColor"
                          opacity="0.18"
                        />
                        <rect
                          x="46"
                          y="34"
                          width="74"
                          height="52"
                          rx="16"
                          fill="url(#k9-bubble-grad)"
                        />
                        <path
                          d="M64 82 L64 98 L82 82 Z"
                          fill="url(#k9-bubble-grad)"
                        />
                        <circle cx="70" cy="60" r="4" fill="#fff" />
                        <circle cx="83" cy="60" r="4" fill="#fff" />
                        <circle cx="96" cy="60" r="4" fill="#fff" />
                        <path
                          d="M126 40 l2.5 6 6 2.5 -6 2.5 -2.5 6 -2.5 -6 -6 -2.5 6 -2.5 z"
                          fill="currentColor"
                          opacity="0.55"
                        />
                        <path
                          d="M34 66 l1.8 4.4 4.4 1.8 -4.4 1.8 -1.8 4.4 -1.8 -4.4 -4.4 -1.8 4.4 -1.8 z"
                          fill="currentColor"
                          opacity="0.4"
                        />
                        <circle
                          cx="122"
                          cy="74"
                          r="3"
                          fill="currentColor"
                          opacity="0.35"
                        />
                        <circle
                          cx="30"
                          cy="42"
                          r="2.4"
                          fill="currentColor"
                          opacity="0.3"
                        />
                        <circle
                          cx="112"
                          cy="26"
                          r="2.2"
                          fill="currentColor"
                          opacity="0.3"
                        />
                      </svg>
                    </div>
                    <div>
                      <h3
                        css={css`
                          margin: 0;
                          font-size: 26px;
                          font-weight: 800;
                          letter-spacing: -0.02em;
                          color: #1e1c21;
                        `}
                      >
                        Ciao! Sono{" "}
                        <span
                          css={css`
                            color: ${RED};
                          `}
                        >
                          K9 IA
                        </span>
                      </h3>
                      <p
                        css={css`
                          margin: 8px auto 0;
                          max-width: 460px;
                          font-size: 14px;
                          line-height: 1.5;
                          color: ${MUTED};
                        `}
                      >
                        Chiedi ciò che ti serve: analizzo i risultati e ti
                        fornisco risposte chiare e basate sulle fonti.
                      </p>
                    </div>

                    <div
                      css={css`
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        align-items: start;
                        gap: 14px;
                        width: 100%;
                        max-width: 640px;
                        @media (max-width: 620px) {
                          grid-template-columns: 1fr;
                        }
                      `}
                    >
                      {[
                        [starterPrompts[0], starterPrompts[2]],
                        [starterPrompts[1], starterPrompts[3]],
                      ].map((column, columnIndex) => (
                        <React.Fragment key={columnIndex}>
                          <div
                            css={css`
                              display: flex;
                              flex-direction: column;
                              gap: 14px;
                            `}
                          >
                            {column.map((prompt) => (
                              <button
                                key={prompt.text}
                                type="button"
                                onClick={() => askPrompt(prompt.text)}
                                disabled={isChatting}
                                css={css`
                                  display: flex;
                                  align-items: center;
                                  gap: 12px;
                                  width: 100%;
                                  text-align: left;
                                  background: #fff;
                                  border: 1px solid ${BORDER};
                                  border-radius: 16px;
                                  padding: 12px 14px;
                                  cursor: pointer;
                                  box-shadow: 0 2px 10px -6px rgba(0, 0, 0, 0.15);
                                  transition: transform 140ms ease,
                                    border-color 140ms ease,
                                    box-shadow 140ms ease;
                                  &:hover:not(:disabled) {
                                    transform: translateY(-2px);
                                    border-color: color-mix(
                                      in srgb,
                                      ${prompt.accent} 45%,
                                      ${BORDER}
                                    );
                                    box-shadow: 0 12px 26px -12px color-mix(in
                                          srgb, ${prompt.accent} 55%, transparent);
                                  }
                                  &:disabled {
                                    opacity: 0.5;
                                    cursor: default;
                                  }
                                `}
                              >
                                <span
                                  aria-hidden="true"
                                  css={css`
                                    flex-shrink: 0;
                                    width: 40px;
                                    height: 40px;
                                    border-radius: 12px;
                                    display: inline-flex;
                                    align-items: center;
                                    justify-content: center;
                                    color: ${prompt.accent};
                                    background: color-mix(
                                      in srgb,
                                      ${prompt.accent} 14%,
                                      #fff
                                    );
                                  `}
                                >
                                  {prompt.icon}
                                </span>
                                <span
                                  css={css`
                                    flex: 1;
                                    min-width: 0;
                                    font-size: 13px;
                                    font-weight: 600;
                                    line-height: 1.35;
                                    color: #1e1c21;
                                  `}
                                >
                                  {prompt.text}
                                </span>
                                <span
                                  aria-hidden="true"
                                  css={css`
                                    flex-shrink: 0;
                                    color: ${prompt.accent};
                                    font-size: 16px;
                                  `}
                                >
                                  ›
                                </span>
                              </button>
                            ))}
                          </div>
                        </React.Fragment>
                      ))}
                    </div>
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
                      overflow-wrap: anywhere;
                      word-break: break-word;
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
                      overflow-wrap: anywhere;
                      word-break: break-word;
                      p {
                        margin: 0 0 8px;
                      }
                      p:last-child {
                        margin-bottom: 0;
                      }
                      a {
                        color: ${RED};
                        overflow-wrap: anywhere;
                      }
                      pre {
                        max-width: 100%;
                        overflow-x: auto;
                      }
                      img {
                        max-width: 100%;
                        height: auto;
                      }
                      table {
                        display: block;
                        max-width: 100%;
                        overflow-x: auto;
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
                  border-radius: 16px;
                  background: #fff;
                  box-shadow: 0 8px 24px -12px color-mix(in srgb, ${RED} 35%, transparent);
                  transition: border-color 120ms ease, box-shadow 120ms ease;
                  &:focus-within {
                    border-color: ${RED};
                    box-shadow: 0 0 0 3px
                      color-mix(in srgb, ${RED} 15%, transparent);
                  }
                `}
              >
                <textarea
                  ref={inputRef}
                  id="openk9-copilot-question"
                  name="openk9-copilot-question"
                  rows={1}
                  value={input}
                  aria-label={t("copilot-input-placeholder") ?? ""}
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
                  aria-label={t("copilot-send") ?? ""}
                  title={t("copilot-send") ?? ""}
                  css={css`
                    flex-shrink: 0;
                    width: 38px;
                    height: 38px;
                    border: none;
                    border-radius: 50%;
                    background: ${RED};
                    color: #fff;
                    cursor: pointer;
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    &:disabled {
                      opacity: 0.4;
                      cursor: default;
                    }
                  `}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path
                      fill="#fff"
                      d="M3.4 20.4l17.45-7.48a1 1 0 0 0 0-1.84L3.4 3.6a1 1 0 0 0-1.4.92V9.5c0 .5.37.92.87.98L12 12l-8.13 1.52a1 1 0 0 0-.87.98v4.98a1 1 0 0 0 1.4.92z"
                    />
                  </svg>
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
              <span
                aria-hidden="true"
                css={css`
                  display: inline-flex;
                  vertical-align: -2px;
                  margin-right: 4px;
                `}
              >
                <svg
                  width="13"
                  height="13"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M12 2a5 5 0 0 0-5 5v3H6a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2h-1V7a5 5 0 0 0-5-5zm-3 8V7a3 3 0 0 1 6 0v3H9z" />
                </svg>
              </span>
              Le risposte di K9 IA possono contenere imprecisioni. Verifica
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
