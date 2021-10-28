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

import React, { useEffect, useRef } from "react";
import clsx from "clsx";
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import { ALL_SUGGESTION_CATEGORY_ID, SuggestionResult } from "@openk9/http-api";
import {
  ThemeType,
  useSuggestionCategories,
} from "@openk9/search-ui-components";
import { TokenIcon } from "./TokenIcon";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    zIndex: 98,
    top: "100%",
    left: 0,
    position: "absolute",
    height: 300,
    width: 700,
    borderBottomLeftRadius: theme.borderRadius,
    borderBottomRightRadius: theme.borderRadius,
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    display: "flex",
  },

  menu: {
    width: 160,
    borderRight: `1px solid ${theme.digitalLakeMainL4}`,
    overflow: "auto",
  },
  menuItem: {
    paddingTop: "0.8em",
    paddingBottom: "0.8em",
    paddingLeft: 14,
    paddingRight: 4,
    fontWeight: 500,
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    cursor: "pointer",
    userSelect: "none",

    "&:hover,&:focus": {
      backgroundColor: theme.digitalLakeMainL6,
    },
    // Clay icons have an annoying margin
    "& .lexicon-icon": { marginTop: 0 },
  },
  menuItemActive: {
    backgroundColor: theme.digitalLakeMainL5,
    "&:hover,&:focus": {
      backgroundColor: theme.digitalLakeMainL5,
    },
  },

  tokens: {
    flexGrow: 1,
    overflow: "auto",
  },
  token: {
    padding: theme.spacingUnit,
    paddingLeft: theme.spacingUnit * 2,
    cursor: "pointer",
    userSelect: "none",
  },
  tokenHighlight: {
    backgroundColor: theme.digitalLakePrimaryL3,
  },
}));

/**
 * A menu item for the types selector
 */
function MenuItem({
  active,
  onSelect,
  label,
}: {
  active: boolean;
  onSelect(): void;
  label: string;
}) {
  const classes = useStyles();
  return (
    <div
      role="button"
      className={clsx(classes.menuItem, active && classes.menuItemActive)}
      onClick={onSelect}
    >
      {label} <ClayIcon symbol="angle-right-small" />
    </div>
  );
}

/**
 * A floating menu for adding tokens, such as predicates and entities. To be used with SearchQueryField.
 * @param suggestionsKind - the currently selected token type, AKA the left menu selector state. Please fetch suggestions again at every change.
 * @param highlightToken - the currently selected token, please implement in your focused input field the logic to change this with keyboard.
 */
export function FieldSuggestionBrowser({
  suggestions,
  onAddSuggestion,
  visible,
  activeSuggestionCategoryId,
  onActiveSuggestionCategoryChange,
  highlightToken,
  onHighlightToken,
}: {
  suggestions: SuggestionResult[];
  onAddSuggestion(sugg: SuggestionResult): void;
  visible: boolean;
  activeSuggestionCategoryId: number;
  onActiveSuggestionCategoryChange(k: number): void;
  highlightToken: SuggestionResult | null;
  onHighlightToken(suggestion: SuggestionResult): void;
}) {
  const classes = useStyles();

  function handleToggleActiveSuggestionCategory(id: number) {
    if (activeSuggestionCategoryId === id) {
      onActiveSuggestionCategoryChange(ALL_SUGGESTION_CATEGORY_ID);
    } else {
      onActiveSuggestionCategoryChange(id);
    }
  }

  // This is used to scroll the container correctly when the selected token with keyboard is out of scroll.
  // Every time the selected token changes, it finds which one is highlighted using an ugly DOM operation and scrolls into it.
  // We can't use focus to perform this, since we want the user to be able to write in the input while selecting tokens!
  const scrollRef = useRef<HTMLDivElement | null>(null);
  useEffect(() => {
    if (!scrollRef.current) return;

    const hTok = scrollRef.current.querySelector(
      `div.${classes.tokenHighlight}`,
    ) as HTMLDivElement | null;

    if (!hTok) return;

    const bbRoot = scrollRef.current.getBoundingClientRect();
    const bbTok = hTok.getBoundingClientRect();
    const visibleTok =
      bbTok.y >= bbRoot.top && bbTok.y < bbRoot.height + bbRoot.top;

    if (!visibleTok) hTok.scrollIntoView(false);
  }, [highlightToken]);

  const suggestionCategories = useSuggestionCategories(null);
  return visible ? (
    <div className={classes.root}>
      <div className={classes.menu}>
        {suggestionCategories.data?.map((suggestionCategory) => {
          return (
            <MenuItem
              key={suggestionCategory.suggestionCategoryId}
              active={
                activeSuggestionCategoryId ===
                suggestionCategory.suggestionCategoryId
              }
              onSelect={() =>
                handleToggleActiveSuggestionCategory(
                  suggestionCategory.suggestionCategoryId,
                )
              }
              label={suggestionCategory.name}
            />
          );
        })}
      </div>

      <div className={classes.tokens} ref={scrollRef}>
        {suggestions.map((suggestion, index) => (
          <div
            role="button"
            tabIndex={index}
            className={clsx(
              classes.token,
              highlightToken === suggestion && classes.tokenHighlight,
            )}
            key={index}
            onClick={() => onAddSuggestion(suggestion)}
            onKeyDown={(e) => e.key === "Enter" && onAddSuggestion(suggestion)}
            onMouseMove={() => onHighlightToken(suggestion)}
          >
            {"keywordKey" in suggestion && suggestion.keywordKey && (
              <strong>{suggestion.keywordKey}: </strong>
            )}
            <TokenIcon suggestion={suggestion} />
            {(() => {
              switch (suggestion.tokenType) {
                case "DATASOURCE":
                  return suggestion.value;
                case "DOCTYPE":
                  return suggestion.value;
                case "ENTITY":
                  return suggestion.entityValue;
                case "TEXT":
                  return suggestion.value;
              }
            })()}
          </div>
        ))}
      </div>
    </div>
  ) : null;
}
