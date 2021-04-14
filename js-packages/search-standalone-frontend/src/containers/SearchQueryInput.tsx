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

import { useLayoutEffect } from "react";
import clsx from "clsx";
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import { readQueryParamToken, setQueryParamToken } from "@openk9/http-api";

import { useSearchQuery, useStore } from "../state";
import {
  ThemeType,
  MultipleSelectionBar,
  MultipleSelectionBarItem,
  firstOrNull,
  SearchQueryField,
} from "@openk9/search-ui-components";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    backgroundColor: theme.digitalLakeBackground,
    display: "flex",
    flexDirection: "column",
    // alignItems: "center",
    padding: [theme.spacingUnit * 2, theme.spacingUnit],
    paddingLeft: theme.spacingUnit * 8,
    // transition: "padding 0.5s",
  },
  rootOpen: {
    alignItems: "center",
    padding: [theme.spacingUnit * 18, theme.spacingUnit],
  },
  centering: {
    maxWidth: theme.searchMaxWidth,
    width: "100%",
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
  const [searchQuery, setSearchQuery] = useSearchQuery();
  const tenantConfig = useStore((s) => s.tenantConfig);
  const focus = useStore((s) => s.focus);
  const setFocus = useStore((s) => s.setFocus);
  const suggestions = useStore((s) => s.suggestions);
  const focusToken = useStore((s) => s.focusToken);
  const setFocusToken = useStore((s) => s.setFocusToken);
  const open = useStore((s) => s.initial);

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

  // Prevent page back on backspace
  useLayoutEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      const inputUnfocused =
        !document.activeElement ||
        !document.activeElement.classList.contains("firstFocusInput");
      if (e.key === "Backspace" && inputUnfocused) {
        e.preventDefault();
      }
    }
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, []);

  const classes = useStyles();

  return (
    <div className={clsx(classes.root, open && classes.rootOpen)}>
      <div className={classes.centering}>
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

        <div className="input-group">
          <div className={"input-group-item"}>
            <div className="form-control input-group-inset input-group-inset-before">
              <SearchQueryField
                searchQuery={searchQuery}
                onSearchQueryChange={setSearchQuery}
                className={clsx(
                  "form-control",
                  "firstFocusInput",
                  classes.inputField,
                )}
                aria-label="Search"
                placeholder="Search..."
                suggestions={suggestions}
                onCloseSuggestions={() => setFocus("RESULTS")}
                suggestionsVisible={focus === "INPUT"}
                focusToken={focusToken}
                onFocusToken={setFocusToken}
                onFocus={() => setFocus("INPUT")}
                onBlur={() => setFocus("RESULTS")}
                onFocusDown={() => setFocus("RESULTS")}
              />
            </div>

            <div className="input-group-inset-item input-group-inset-item-before">
              <button className="btn btn-unstyled">
                <ClayIcon symbol="search" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
