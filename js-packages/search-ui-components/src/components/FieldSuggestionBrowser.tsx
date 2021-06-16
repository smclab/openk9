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
import { InputSuggestionToken } from "@openk9/http-api";
import { ThemeType } from "@openk9/search-ui-components";
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

const menuItems = [
  { id: "person", label: "People" },
  { id: "organization", label: "Organizations" },
  { id: "email", label: "Emails" },
  { id: "loc", label: "Locations" },
  { id: "*.topic", label: "Topics" },
  { id: "*.documentType", label: "Document Type" },
  { id: "type", label: "Types" },
  { id: "PARAM", label: "Filters" },
];

export function FieldSuggestionBrowser({
  suggestions,
  onAddSuggestion,
  visible,
  suggestionsKind,
  setSuggestionsKind,
  highlightToken,
  onHighlightToken,
}: {
  suggestions: InputSuggestionToken[];
  onAddSuggestion(sugg: InputSuggestionToken): void;
  visible: boolean;
  suggestionsKind: string | null;
  setSuggestionsKind(k: string | null): void;
  highlightToken: string | number | null;
  onHighlightToken(tok: string | number | null): void;
}) {
  const classes = useStyles();

  function handleToggleKind(kind: string) {
    if (suggestionsKind === kind) {
      setSuggestionsKind(null);
    } else {
      setSuggestionsKind(kind);
    }
  }

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

  return visible ? (
    <div className={classes.root}>
      <div className={classes.menu}>
        <MenuItem
          active={suggestionsKind === null}
          onSelect={() => setSuggestionsKind(null)}
          label="All"
        />
        {menuItems.map((item, i) => (
          <MenuItem
            key={item.id}
            active={suggestionsKind === item.id}
            onSelect={() => handleToggleKind(item.id)}
            {...item}
          />
        ))}
      </div>

      <div className={classes.tokens} ref={scrollRef}>
        {suggestions.map((s, i) => (
          <div
            role="button"
            tabIndex={i}
            className={clsx(
              classes.token,
              highlightToken === s.id && classes.tokenHighlight,
            )}
            key={s.id}
            onClick={() => onAddSuggestion(s)}
            onKeyDown={(e) => e.key === "Enter" && onAddSuggestion(s)}
            onMouseMove={() => onHighlightToken(s.id)}
          >
            <TokenIcon suggestion={s} />
            {s.displayDescription}
          </div>
        ))}
      </div>
    </div>
  ) : null;
}
