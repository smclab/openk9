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

import { ContactResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";
import { ResultCard, Highlight } from "../components";
import { ChatIcon } from "../icons";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    "&:focus, &:hover h4": {
      textDecoration: "underline",
    },
  },
  avatar: {
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 3,
    width: 64,
    height: 64,
    fontSize: 64,
    flexShrink: 0,
    display: "flex",
    justifyContent: "center",
    color: theme.digitalLakeMainL2,
    borderRadius: "100%",
    overflow: "hidden",
  },
  nameLine: {
    display: "flex",
    alignItems: "center",
    marginBottom: 0,
  },
  name: {
    marginRight: theme.spacingUnit,
  },
  actionButton: {
    fill: theme.digitalLakePrimary,
    color: theme.digitalLakePrimary,
    fillOpacity: 1,
    margin: [0, theme.spacingUnit * 0.5],
    fontSize: 16,
  },
  title: {
    fontSize: 14,
    fontWeight: 600,
    color: theme.digitalLakeMainL3,
  },
}));

export function ContactResultCard({
  data,
  className,
  ...rest
}: {
  data: ContactResultItem;
  onSelect?: () => void;
} & React.AnchorHTMLAttributes<HTMLAnchorElement>): JSX.Element {
  const classes = useStyles();

  return (
    <ResultCard className={classes.root} {...rest}>
      <div className={classes.avatar}>
        {data.source.user.portrait_preview ? (
          <img src={data.source.user.portrait_preview} />
        ) : (
          <ClayIcon symbol="user" />
        )}
      </div>
      <div style={{ minWidth: 0 }}>
        <h4 className={classes.nameLine}>
          <div className={classes.name}>
            <Highlight
              text={data.source.user.firstName}
              highlight={data.highlight["user.firstName"]}
              inline
            />{" "}
            <Highlight
              text={data.source.user.middleName}
              highlight={data.highlight["user.middleName"]}
              inline
            />{" "}
            <Highlight
              text={data.source.user.lastName}
              highlight={data.highlight["user.lastName"]}
              inline
            />
          </div>
          <a href="#" className={classes.actionButton}>
            <ChatIcon />
          </a>
          <a href="#" className={classes.actionButton}>
            <ClayIcon symbol="phone" />
          </a>
        </h4>
        <div className={classes.title}>
          <Highlight
            text={data.source.user.jobTitle}
            highlight={data.highlight["user.jobTitle"]}
          />
        </div>
        <div className={classes.title}>
          <Highlight
            text={data.source.user.emailAddress}
            highlight={data.highlight["user.emailAddress"]}
          />
        </div>
        {data.source.user.phoneNumber && (
          <div className={classes.title}>
            <Highlight
              text={data.source.user.phoneNumber}
              highlight={data.highlight["user.phoneNumber"]}
            />
          </div>
        )}
      </div>
    </ResultCard>
  );
}
