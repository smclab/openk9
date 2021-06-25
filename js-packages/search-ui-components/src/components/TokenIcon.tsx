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
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import { InputSuggestionToken } from "@openk9/http-api";
import { DXPLogo, EmailIcon, OSLogo } from "../icons";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  icon: {
    marginRight: theme.spacingUnit,
    fill: "currentColor",
  },
}));

// TODO: move these inside plugins as a pluginservice
export function TokenIcon({
  suggestion,
}: {
  suggestion: InputSuggestionToken;
}) {
  const classes = useStyles();

  if (suggestion.kind === "ENTITY") {
    switch (suggestion.type) {
      case "person":
        return <ClayIcon className={classes.icon} symbol="user" />;
      case "organization":
        return <ClayIcon className={classes.icon} symbol="organizations" />;
      case "product":
        return <ClayIcon className={classes.icon} symbol="devices" />;
      case "gpe":
        return <ClayIcon className={classes.icon} symbol="geolocation" />;
      case "loc":
        return <ClayIcon className={classes.icon} symbol="geolocation" />;
      case "date":
        return <ClayIcon className={classes.icon} symbol="calendar" />;
      case "email":
        return <EmailIcon className={classes.icon} size={16} />;
    }
  } else if (suggestion.kind === "PARAM") {
    switch (suggestion.id) {
      case "from":
      case "to":
        return <EmailIcon className={classes.icon} size={16} />;
      default:
        return <ClayIcon className={classes.icon} symbol="filter" />;
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
      case "web":
        return <ClayIcon className={classes.icon} symbol="globe" />;
      case "file":
        return <ClayIcon className={classes.icon} symbol="paperclip" />;
    }
  }

  return <ClayIcon className={classes.icon} symbol="tag" />;
}
