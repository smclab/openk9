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

import { useLayoutEffect, useRef, useState } from "react";
import clsx from "clsx";
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import ClickAwayListener from "react-click-away-listener";
import {
  InputSuggestionToken,
  readQueryParamToken,
  SearchQuery,
  SearchToken,
  setQueryParamToken,
} from "@openk9/http-api";
import {
  ThemeType,
  MultipleSelectionBar,
  MultipleSelectionBarItem,
  firstOrNull,
  SearchQueryField,
  FieldSuggestionBrowser,
  circularMod,
} from "@openk9/search-ui-components";

import { useSearchQuery, useStore } from "../state";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    backgroundColor: theme.digitalLakeBackground,
    display: "flex",
    flexDirection: "column",
    padding: [theme.spacingUnit * 2, theme.spacingUnit],
    paddingLeft: theme.spacingUnit * 8,
  },
  centering: {
    maxWidth: theme.searchMaxWidth,
    width: "100%",

    "& div .input-group-item": {
      zIndex: 99,
    },
  },
  inputField: {
    minHeight: 32,
    border: "none",
    flexGrow: 1,
    minWidth: 200,
    "&:focus": {
      boxShadow: "none",
    },
  },
}));

export function SearchQueryInput() {
  const classes = useStyles();

  const [searchQuery, setSearchQuery] = useSearchQuery();
  const tenantConfig = useStore((s) => s.tenantConfig);
  const suggestions = useStore((s) => s.suggestions);
  const suggestionsKind = useStore((s) => s.suggestionsKind);
  const setSuggestionsKind = useStore((s) => s.setSuggestionsKind);
  const suggestionsInfo = useStore((s) => s.suggestionsInfo);
  const focusToken = useStore((s) => s.focusToken);
  const setFocusToken = useStore((s) => s.setFocusToken);

  const selectedDataType =
    firstOrNull(readQueryParamToken(searchQuery, "type")) || "any";
  function setSelectedDataType(dataType: string | "any") {
    const values = dataType === "any" ? null : [dataType];
    setSearchQuery(
      setQueryParamToken({
        query: searchQuery,
        keywordKey: "type",
        values,
        tokenType: "DOCTYPE",
      }),
    );
  }

  const focusedInputRef = useRef<HTMLInputElement | null>(null);

  const [searchOpen, setSearchOpen] = useState(false);

  useLayoutEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      // Prevent page back on backspace
      const inputUnfocused =
        !document.activeElement ||
        !document.activeElement.classList.contains("firstFocusInput");
      if (e.key === "Backspace" && inputUnfocused) {
        e.preventDefault();
      }

      // Hide search on ESC or ENTER
      if (e.key === "Escape" || e.key === "Enter") {
        setSearchOpen(false);
      }
    }

    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, []);

  const [highlightToken, setHighlightToken] = useState<string | number | null>(
    null,
  );

  function handleAddSuggestion(sugg: InputSuggestionToken) {
    const soFar = searchQuery.filter((e, i) => i !== focusToken);
    const editingTok = (focusToken !== null && searchQuery[focusToken]) || null;

    if (sugg && sugg.kind === "ENTITY") {
      const tok: SearchToken = {
        tokenType: "ENTITY" as const,
        keywordKey: editingTok?.keywordKey,
        entityType: sugg.type,
        values: [sugg.id],
      };
      setSearchQuery([...soFar, tok]);
    } else if (sugg && sugg.kind === "PARAM") {
      const tok: SearchToken = {
        tokenType: "TEXT" as const,
        keywordKey: sugg.id.toString(),
        values: [""],
      };
      setSearchQuery([...soFar, tok]);
    } else if (sugg && sugg.kind === "TOKEN") {
      const tok: SearchToken = {
        tokenType: sugg.outputTokenType || ("TEXT" as any),
        keywordKey: sugg.outputKeywordKey as any,
        values: [sugg.id as any],
      };
      setSearchQuery([...soFar, tok]);
    }

    setSearchOpen(false);
  }

  function handleInputKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "ArrowDown") {
      e.preventDefault();

      const suggI = suggestions.findIndex((s) => s.id === highlightToken);
      const nextSugg = suggestions[circularMod(suggI + 1, suggestions.length)];
      if (nextSugg) setHighlightToken(nextSugg.id);
    } else if (e.key === "ArrowUp") {
      e.preventDefault();

      const suggI = suggestions.findIndex((s) => s.id === highlightToken);
      const nextSugg = suggestions[circularMod(suggI - 1, suggestions.length)];
      if (nextSugg) setHighlightToken(nextSugg.id);
    } else if (e.key === "Enter") {
      const sugg = suggestions.find((s) => s.id === highlightToken);
      if (sugg) handleAddSuggestion(sugg);
    }
  }

  return (
    <div className={classes.root}>
      <div className={classes.centering}>
        {tenantConfig.querySourceBarShortcuts && (
          <MultipleSelectionBar>
            <MultipleSelectionBarItem
              selected={selectedDataType === "any"}
              onClick={() => setSelectedDataType("any")}
            >
              All
            </MultipleSelectionBarItem>
            {tenantConfig.querySourceBarShortcuts.map((dt) => (
              <MultipleSelectionBarItem
                key={dt.id}
                selected={selectedDataType === dt.id}
                onClick={() => setSelectedDataType(dt.id)}
              >
                {dt.text}
              </MultipleSelectionBarItem>
            ))}
          </MultipleSelectionBar>
        )}

        <ClickAwayListener onClickAway={() => setSearchOpen(false)}>
          <div className="input-group">
            <div className="input-group-item">
              <div className="form-control input-group-inset input-group-inset-before">
                <SearchQueryField
                  ref={focusedInputRef}
                  searchQuery={searchQuery}
                  onSearchQueryChange={(query: SearchQuery) => {
                    setSearchQuery(query);
                    setSearchOpen(true);
                  }}
                  className={clsx(
                    "form-control",
                    "firstFocusInput",
                    classes.inputField,
                  )}
                  aria-label="Search"
                  placeholder="Search..."
                  onClick={() => setSearchOpen(true)}
                  focusToken={focusToken}
                  onFocusToken={setFocusToken}
                  suggestionsInfo={suggestionsInfo}
                  onInputKeyDown={handleInputKeyDown}
                />
              </div>

              <div className="input-group-inset-item input-group-inset-item-before">
                <button className="btn btn-unstyled">
                  <ClayIcon symbol="search" />
                </button>
              </div>
            </div>

            <FieldSuggestionBrowser
              suggestions={suggestions}
              visible={searchOpen}
              suggestionsKind={suggestionsKind}
              setSuggestionsKind={(kind) => {
                setSuggestionsKind(kind);
                focusedInputRef.current && focusedInputRef.current.focus();
              }}
              onAddSuggestion={handleAddSuggestion}
              highlightToken={highlightToken}
              onHighlightToken={setHighlightToken}
            />
          </div>
        </ClickAwayListener>
      </div>
    </div>
  );
}
