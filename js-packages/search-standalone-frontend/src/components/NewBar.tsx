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

import clsx from "clsx";
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import {
  InputSuggestionToken,
  SearchQuery,
  SearchToken,
} from "@openk9/http-api";
import { ThemeType } from "@openk9/search-ui-components";
import { TokenIcon } from "@openk9/search-ui-components/src/components/InputSuggestionTokensDisplay";

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

    "& .lexicon-icon": { marginTop: 0 },
  },
  menuItemActive: {
    backgroundColor: theme.digitalLakeMainL5,
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
    "&:hover": {
      backgroundColor: theme.digitalLakePrimaryL3,
    },
  },
}));

function MenuItem({
  active,
  onSelect,
  onOver,
  label,
  i,
}: {
  active: boolean;
  onSelect(): void;
  onOver(): void;
  label: string;
  i: number;
}) {
  const classes = useStyles();
  return (
    <div
      role="button"
      className={clsx(classes.menuItem, active && classes.menuItemActive)}
      onClick={onSelect}
      onKeyDown={onSelect}
      // onFocus={onOver}
      // onMouseOver={onOver}
      tabIndex={i}
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
  { id: "istat.topic", label: "Topics" },
  { id: "document.documentType", label: "Document Type" },
  { id: "type", label: "Types" },
  { id: "PARAM", label: "Filters" },
];

export function NewBar({
  suggestions,
  visible,
  searchQuery,
  onSearchQueryChange,
  onClose,
  suggestionsKind,
  setSuggestionsKind,
}: {
  suggestions: InputSuggestionToken[];
  visible: boolean;
  searchQuery: SearchQuery;
  onSearchQueryChange(searchQuery: SearchQuery): void;
  onClose(): void;
  suggestionsKind: string | null;
  setSuggestionsKind(k: string | null): void;
}) {
  const classes = useStyles();

  function handleAddSuggestion(sugg: InputSuggestionToken) {
    const last = searchQuery[searchQuery.length - 1];
    const soFar =
      last && last.tokenType === "TEXT" && !last.keywordKey
        ? searchQuery.slice(0, -1)
        : searchQuery;

    if (sugg && sugg.kind === "ENTITY") {
      const tok: SearchToken = {
        tokenType: "ENTITY" as const,
        // keywordKey: last.keywordKey,
        entityType: sugg.type,
        values: [sugg.id],
      };
      onSearchQueryChange([...soFar, tok]);
    } else if (sugg && sugg.kind === "PARAM") {
      const tok: SearchToken = {
        tokenType: "TEXT" as const,
        keywordKey: sugg.id.toString(),
        values: [""],
      };
      onSearchQueryChange([...soFar, tok]);
    } else if (sugg && sugg.kind === "TOKEN") {
      const tok: SearchToken = {
        tokenType: sugg.outputTokenType || ("TEXT" as any),
        keywordKey: sugg.outputKeywordKey as any,
        values: [sugg.id as any],
      };
      onSearchQueryChange([...soFar, tok]);
    }

    onClose();
  }

  function handleToggleKind(kind: string) {
    if (suggestionsKind === kind) {
      setSuggestionsKind(null);
    } else {
      setSuggestionsKind(kind);
    }
  }

  return visible ? (
    <div className={classes.root}>
      <div className={classes.menu}>
        <MenuItem
          active={suggestionsKind === null}
          onSelect={() => setSuggestionsKind(null)}
          onOver={() => setSuggestionsKind(null)}
          label="All"
          i={0}
        />
        {menuItems.map((item, i) => (
          <MenuItem
            key={item.id}
            active={suggestionsKind === item.id}
            onSelect={() => handleToggleKind(item.id)}
            onOver={() => setSuggestionsKind(item.id)}
            i={i + 1}
            {...item}
          />
        ))}
      </div>

      <div className={classes.tokens}>
        {suggestions.map((s, i) => (
          <div
            role="button"
            tabIndex={i}
            className={classes.token}
            key={s.id}
            onClick={() => handleAddSuggestion(s)}
            onKeyDown={() => handleAddSuggestion(s)}
          >
            <TokenIcon suggestion={s} />
            {s.displayDescription}
          </div>
        ))}
      </div>
    </div>
  ) : null;
}
