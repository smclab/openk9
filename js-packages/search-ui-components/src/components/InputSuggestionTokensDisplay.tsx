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
import clsx from "clsx";
import ClayIcon from "@clayui/icon";
import { createUseStyles } from "react-jss";

import { ThemeType } from "../theme";
import { InputSuggestionToken } from "@openk9/http-api";
import { DXPLogo, EmailIcon, OSLogo } from "../icons";

const useStyles = createUseStyles((theme: ThemeType) => ({
  popup: {
    position: "absolute",
    top: 0,
    left: 0,
    display: "flex",
    flexDirection: "column",
    "& div:first-child": {
      borderTopLeftRadius: theme.borderRadius,
      borderTopRightRadius: theme.borderRadius,
    },
    "& div:last-child": {
      borderBottomLeftRadius: theme.borderRadius,
      borderBottomRightRadius: theme.borderRadius,
    },
  },
  suggestion: {
    zIndex: 100,
    padding: [theme.spacingUnit, theme.spacingUnit * 2],
    color: theme.digitalLakePrimaryL2,
    border: `1px solid ${theme.digitalLakePrimary}`,
    backgroundColor: "white",
  },
  selected: {
    backgroundColor: theme.digitalLakePrimary,
    color: "white",
  },
  icon: {
    marginRight: theme.spacingUnit,
    fill: "currentColor",
  },
}));

interface Props {
  suggestions: InputSuggestionToken[];
  x: number;
  y: number;
  selected: string | null;
  visible?: boolean;
  onAdd(id: string): void;
}

function TokenIcon({ suggestion }: { suggestion: InputSuggestionToken }) {
  const classes = useStyles();

  if (suggestion.kind === "ENTITY") {
    switch (suggestion.type) {
      case "person":
        return <ClayIcon className={classes.icon} symbol="user" />;
      case "organization":
        return <ClayIcon className={classes.icon} symbol="organizations" />;
      case "product":
        return <ClayIcon className={classes.icon} symbol="devices" />;
      case "product":
        return <ClayIcon className={classes.icon} symbol="geolocation" />;
      case "email":
        return <EmailIcon className={classes.icon} size={16} />;
    }
  } else if (suggestion.kind === "PARAM") {
    switch (suggestion.id) {
      case "from":
      case "to":
        return <EmailIcon className={classes.icon} size={16} />;
    }
  } else if (suggestion.kind === "TOKEN") {
    switch (suggestion.id) {
      case "spaces":
        return <OSLogo className={classes.icon} size={16} />;
      case "liferay":
        return <DXPLogo className={classes.icon} size={16} />;
      case "email":
        return <EmailIcon className={classes.icon} size={16} />;
      case "application":
        return <ClayIcon className={classes.icon} symbol="desktop" />;
      case "document":
        return <ClayIcon className={classes.icon} symbol="document" />;
      case "office-word":
        return <ClayIcon className={classes.icon} symbol="document-text" />;
      case "office-powerpoint":
        return (
          <ClayIcon className={classes.icon} symbol="document-presentation" />
        );
      case "office-excel":
        return <ClayIcon className={classes.icon} symbol="document-table" />;
      case "pdf":
        return <ClayIcon className={classes.icon} symbol="document-pdf" />;
      case "calendar":
        return <ClayIcon className={classes.icon} symbol="calendar" />;
      case "user":
        return <ClayIcon className={classes.icon} symbol="user" />;
    }
  }

  return null;
}

export function InputSuggestionTokenDisplay({
  suggestion,
  selected,
  onClick,
}: {
  suggestion: InputSuggestionToken;
  selected: boolean;
  onClick(): void;
}) {
  const classes = useStyles();

  return (
    <div
      className={clsx(classes.suggestion, selected && classes.selected)}
      onClick={onClick}
    >
      <TokenIcon suggestion={suggestion} />
      {suggestion.displayDescription}
      {selected ? " ↩︎" : "  "}
    </div>
  );
}

export function InputSuggestionTokensDisplay({
  suggestions,
  x,
  y,
  className,
  selected,
  onAdd,
  visible = true,
  ...rest
}: Props & React.HTMLAttributes<HTMLDivElement>) {
  const classes = useStyles();

  return (
    <div
      className={clsx(className, classes.popup)}
      style={{ left: x, top: y, visibility: visible ? "visible" : "hidden" }}
      {...rest}
    >
      {suggestions.map((s, i) => (
        <InputSuggestionTokenDisplay
          onClick={() => onAdd(s.id)}
          key={s.id + "-" + i}
          suggestion={s}
          selected={selected === s.id}
        />
      ))}
    </div>
  );
}
