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
import Markdown, { type Components } from "react-markdown";
import { Translate } from "./Translate";
import { useState } from "react";
import type { Source } from "./client";
import {
  CheckIcon,
  ContentCopyIcon,
  ErrorIcon,
  OpenInFullIcon,
  VisibilityIcon,
} from "./icons";

type Theme = "light" | "dark";

/** how the sources under an answer look */
export type SourcesVariant = "chips" | "list";

// a link in an answer opens in a new tab, so the conversation stays on the page
const markdownComponents: Components = {
  a: ({ href, title, children }) => (
    <a href={href} title={title} target="_blank" rel="noopener noreferrer">
      {children}
    </a>
  ),
};

export function SingleMessage({
  contentMessage,
  timeMessage,
  isChatbot,
  isLoading = false,
  icon,
  nameChatbot,
  status,
  sources,
  numberOfSources = 8,
  sourcesVariant = "chips",
  showTimestamps = true,
  themeInfo = "light",
}: {
  contentMessage: string;
  timeMessage: string;
  isChatbot: boolean;
  isLoading?: boolean;
  icon?: React.ReactNode;
  nameChatbot?: string;
  status?: "END" | "CHUNK" | "ERROR";
  sources?: Source[];
  numberOfSources?: number;
  sourcesVariant?: SourcesVariant;
  showTimestamps?: boolean;
  themeInfo?: Theme;
}) {
  sources = sources || [];

  const [copiedSource, setCopiedSource] = useState<string | null>(null);
  const [showAllSources, setShowAllSources] = useState(false);
  const [expandedChips, setExpandedChips] = useState<Set<string>>(new Set());
  const maxVisibleSources = numberOfSources;
  const INITIAL_VISIBLE_SOURCES = 3;
  // sources kept within the configured cap (the rest is truncated, not reachable)
  const visibleSources = sources.slice(0, maxVisibleSources);
  // collapsed view shows the first few; the toggle reveals the rest up to the cap
  const displayedSources = showAllSources
    ? visibleSources
    : visibleSources.slice(0, INITIAL_VISIBLE_SOURCES);
  const canToggleSources = visibleSources.length > INITIAL_VISIBLE_SOURCES;
  const sourcesCountText = Translate({
    label: visibleSources.length === 1 ? "sourcesCountOne" : "sourcesCount",
  }).replace("{count}", String(visibleSources.length));
  // "show less" carries no `{count}`, so the replace leaves it untouched
  const toggleSourcesText = Translate({
    label: showAllSources ? "showLessSources" : "showMoreSources",
  }).replace(
    "{count}",
    String(visibleSources.length - INITIAL_VISIBLE_SOURCES),
  );

  const copySource = async (source: Source) => {
    // `url` e' opzionale sulla sorgente: senza non c'e' nulla da copiare.
    if (!source.url) return;
    const url = source.url;
    try {
      await navigator.clipboard.writeText(url);
      setCopiedSource(url);
      setTimeout(() => setCopiedSource(null), 2000);
    } catch (err) {
      console.error("Errore durante la copia:", err);
    }
  };

  const toggleChipExpansion = (url: string) => {
    const newSet = new Set(expandedChips);

    if (newSet.has(url)) {
      newSet.delete(url);
    } else {
      newSet.add(url);
    }

    setExpandedChips(newSet);
  };

  const getTypeColor = (source: string) => {
    const baseColor = getStableColor(source, themeInfo);

    const hexToRgb = (hex: string) => {
      const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
      return result
        ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16),
          }
        : { r: 200, g: 200, b: 200 };
    };

    const rgb = hexToRgb(baseColor);

    const backgroundOpacity = themeInfo === "light" ? 0.1 : 0.2;
    const borderOpacity = themeInfo === "light" ? 0.3 : 0.4;

    const lightBackground = `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${backgroundOpacity})`;
    const lightBorder = `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${borderOpacity})`;

    return {
      backgroundColor: lightBackground,
      color: baseColor,
      borderColor: lightBorder,
    };
  };

  const ariaLabel =
    (isChatbot
      ? nameChatbot + " " + Translate({ label: "sendMessage" })
      : Translate({ label: "youSendMessage" })) + timeMessage;

  const openSource = (source: Source) => window.open(source.url, "_blank");

  return (
    <div
      className="openk9-single-message-container"
      data-sender={isChatbot ? "bot" : "user"}
    >
      {status?.toUpperCase() === "ERROR" ? (
        <div className="openk9-message-box">
          {icon && <div className="openk9-chatbot-icon">{icon}</div>}
          <div className="openk9-error-message-container" aria-live="polite">
            <ErrorIcon />
            <div className="openk9-paragraph-message">{contentMessage}</div>
          </div>
        </div>
      ) : (
        <div className="openk9-message-box">
          {icon && <div className="openk9-chatbot-icon">{icon}</div>}
          <div className="openk9-message-content-wrapper">
            <div className="openk9-message-content" aria-live="polite">
              <div
                className="openk9-paragraph-message"
                data-loading={isLoading || undefined}
              >
                {isLoading ? (
                  <div className="openk9-loading-icon-container">
                    <span className="openk9-loader" role="progressbar">
                      <svg viewBox="22 22 44 44">
                        <circle
                          cx="44"
                          cy="44"
                          r="20.2"
                          fill="none"
                          strokeWidth="3.6"
                        />
                      </svg>
                    </span>
                  </div>
                ) : (
                  <section
                    className="openk9-focusable-section"
                    aria-label={ariaLabel}
                    tabIndex={0}
                  >
                    {nameChatbot && (
                      <p className="openk9-paragraph-name">{nameChatbot}</p>
                    )}
                    <Markdown components={markdownComponents}>
                      {contentMessage}
                    </Markdown>
                  </section>
                )}
              </div>
              {maxVisibleSources > 0 && sources.length > 0 && (
                <div className="openk9-sources">
                  <div className="openk9-sources-header">
                    <p className="openk9-sources-count">{sourcesCountText}</p>
                    {canToggleSources && (
                      <button
                        className="openk9-sources-toggle"
                        onClick={() => setShowAllSources((prev) => !prev)}
                      >
                        <VisibilityIcon />
                        <span className="openk9-sources-toggle-label">
                          {toggleSourcesText}
                        </span>
                      </button>
                    )}
                  </div>

                  {sourcesVariant === "list" ? (
                    <ul className="openk9-sources-links">
                      {displayedSources.map((source, index) => (
                        <li key={`${source.url ?? ""}-${index}`}>
                          {source.url ? (
                            <a
                              href={source.url}
                              target="_blank"
                              rel="noopener noreferrer"
                            >
                              {source.title || source.url}
                            </a>
                          ) : (
                            source.title || source.filename || source.source
                          )}
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <div className="openk9-sources-list">
                      {displayedSources.map((source, index) => {
                        const typeColors = getTypeColor(source.url || "");
                        return (
                          // a div, not a <button>, because it holds the copy and
                          // expand buttons: Enter and Space open the source
                          <div
                            key={`${source.url ?? ""}-${index}`}
                            className="openk9-source-chip"
                            role="button"
                            tabIndex={0}
                            onClick={() => openSource(source)}
                            onKeyDown={(event) => {
                              if (event.target !== event.currentTarget) return;
                              if (event.key === " ") event.preventDefault();
                              if (event.key === "Enter") {
                                event.preventDefault();
                                openSource(source);
                              }
                            }}
                            onKeyUp={(event) => {
                              if (event.target !== event.currentTarget) return;
                              if (event.key === " ") openSource(source);
                            }}
                            style={{
                              backgroundColor: typeColors.backgroundColor,
                              color: typeColors.color,
                              borderColor: typeColors.borderColor,
                            }}
                          >
                            <span className="openk9-source-chip-label">
                              <div
                                className="openk9-source-chip-content"
                                data-expanded={
                                  expandedChips.has(source.url || "") ||
                                  undefined
                                }
                              >
                                <div
                                  className="openk9-source-chip-dot"
                                  style={{
                                    backgroundColor: getStableColor(
                                      source.url,
                                      themeInfo,
                                    ),
                                  }}
                                />
                                <span className="openk9-source-chip-title">
                                  {source?.title && source.title}
                                </span>
                                <div>
                                  <button
                                    type="button"
                                    className="openk9-source-chip-button"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      copySource(source);
                                    }}
                                  >
                                    {copiedSource === source.url ? (
                                      <CheckIcon />
                                    ) : (
                                      <ContentCopyIcon />
                                    )}
                                  </button>
                                  <button
                                    type="button"
                                    className="openk9-source-chip-button"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      toggleChipExpansion(source.url || "");
                                    }}
                                  >
                                    <OpenInFullIcon />
                                  </button>
                                </div>
                              </div>
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
      {showTimestamps && <p className="openk9-paragraph-time">{timeMessage}</p>}
    </div>
  );
}

function generateAccessibleColor(theme: Theme): string {
  const MAX_TRIES = 20;
  const bgColor = theme === "light" ? "#ffffff" : "#000000";

  for (let i = 0; i < MAX_TRIES; i++) {
    const hue = Math.floor(Math.random() * 360);
    const saturation = 80;
    const lightness =
      theme === "light" ? 20 + Math.random() * 30 : 60 + Math.random() * 30;

    const hex = hslToHex(hue, saturation, lightness);
    if (hasGoodContrast(hex, bgColor, 6.0)) {
      return hex;
    }
  }

  return theme === "light" ? "#111111" : "#eeeeee";
}

function hexToRgb(hex: string): { r: number; g: number; b: number } {
  hex = hex.replace(/^#/, "");

  if (hex.length === 3) {
    hex = hex
      .split("")
      .map((c) => c + c)
      .join("");
  }

  const num = parseInt(hex, 16);
  return {
    r: (num >> 16) & 255,
    g: (num >> 8) & 255,
    b: num & 255,
  };
}

function luminance(hex: string): number {
  const rgb = hexToRgb(hex);
  const srgb = [rgb.r, rgb.g, rgb.b].map((c) => {
    const c_ = c / 255;
    return c_ <= 0.03928 ? c_ / 12.92 : Math.pow((c_ + 0.055) / 1.055, 2.4);
  });
  return 0.2126 * srgb[0] + 0.7152 * srgb[1] + 0.0722 * srgb[2];
}

function contrastRatio(fg: string, bg: string): number {
  const L1 = luminance(fg);
  const L2 = luminance(bg);
  return (Math.max(L1, L2) + 0.05) / (Math.min(L1, L2) + 0.05);
}

function hasGoodContrast(fg: string, bg: string, minRatio = 6.0): boolean {
  return contrastRatio(fg, bg) >= minRatio;
}

function hslToHex(h: number, s: number, l: number): string {
  l /= 100;
  const a = (s * Math.min(l, 1 - l)) / 100;
  const f = (n: number) => {
    const k = (n + h / 30) % 12;
    const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1);
    return Math.round(255 * color)
      .toString(16)
      .padStart(2, "0");
  };
  return `#${f(0)}${f(8)}${f(4)}`;
}

const sourceColorMap = new Map<string, { light: string; dark: string }>();

function getStableColor(source: string | undefined, theme: Theme): string {
  if (!source) return theme === "light" ? "#666" : "#999";

  // both schemes cached at once: `colorScheme` can change at runtime
  if (!sourceColorMap.has(source)) {
    sourceColorMap.set(source, {
      light: mappingColors(source, "light") ?? generateAccessibleColor("light"),
      dark: mappingColors(source, "dark") ?? generateAccessibleColor("dark"),
    });
  }

  return sourceColorMap.get(source)![theme];
}

function mappingColors(
  source: string | undefined,
  theme: Theme,
): string | undefined {
  switch (source?.toLowerCase()) {
    case "ansa.it":
      return theme === "light" ? "#1976d2" : "#42a5f5";
    case "eurosport.it":
      return theme === "light" ? "#d32f2f" : "#ef5350";
    case "ilpost.it":
      return theme === "light" ? "#388e3c" : "#66bb6a";
    case "corriere.it":
      return theme === "light" ? "#f57c00" : "#ffa726";
    case "techreport.com":
      return theme === "light" ? "#1976d2" : "#42a5f5";
    case "openai.com":
      return theme === "light" ? "#7b1fa2" : "#ab47bc";
    case "meta.ai":
      return theme === "light" ? "#1976d2" : "#42a5f5";
    case "kaggle.com":
      return theme === "light" ? "#f57c00" : "#ffa726";
    case "ethics.ai":
      return theme === "light" ? "#1976d2" : "#42a5f5";
    case "coursera.org":
      return theme === "light" ? "#d32f2f" : "#ef5350";
    case "spotify.com":
      return "#1dd15d";
    case "arxiv.org":
      return theme === "light" ? "#7b1fa2" : "#ab47bc";
    case "github.com":
      return theme === "light" ? "#24292e" : "#f0f6fc";
    case "stackoverflow.com":
      return theme === "light" ? "#f48024" : "#f69c3d";
    case "medium.com":
      return theme === "light" ? "#000000" : "#ffffff";
    case "youtube.com":
      return theme === "light" ? "#ff0000" : "#ff4444";
    case "x.com":
      return theme === "light" ? "#1da1f2" : "#1d9bf0";
    case "linkedin.com":
      return theme === "light" ? "#0077b5" : "#0a66c2";
    case "reddit.com":
      return theme === "light" ? "#ff4500" : "#ff6314";
    case "wikipedia.org":
      return theme === "light" ? "#000000" : "#ffffff";
    default:
      return undefined;
  }
}
