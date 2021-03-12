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
import { ThemeType } from "@openk9/search-ui-components";

import { ApplicationResultItem } from "./types";

const useStyles = createUseStyles((theme: ThemeType) => ({
  row: {
    display: "flex",
    alignItems: "center",
  },
  iconArea: {
    display: "flex",
    backgroundColor: theme.digitalLakePrimary,
    borderRadius: theme.borderRadius,
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 2,
    fontSize: "24px",
  },
}));

export function ApplicationSidebar({
  result,
}: {
  result: ApplicationResultItem;
}) {
  const classes = useStyles();
  return (
    <>
      <h3 className={classes.row}>
        <div className={classes.iconArea}>
          <img height={32} src={result.source.application.icon} />
        </div>{" "}
        {result.source.application.title}
      </h3>
      {result.source.application.description}
    </>
  );
}
