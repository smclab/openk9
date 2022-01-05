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
import { GenericResultItem } from "@openk9/rest-api";
import { ThemeType } from "../theme";
import { useEntity } from ".";

const useStyles = createUseStyles((theme: ThemeType) => ({
  entities: {
    display: "flex",
    flexDirection: "row",
    flexWrap: "wrap",
    gap: theme.spacingUnit,
  },
  token: {
    padding: [theme.spacingUnit, theme.spacingUnit * 2],
    borderRadius: theme.borderRadius,
    backgroundColor: theme.digitalLakePrimary,
    color: "white",
  },
}));

/**
 * Display a single entity badge.
 * @param type - the type of the entity, that gets truncated to 3 upper case chars
 * @param id - the id of the entity
 */
export function EntityDisplay({ type, id }: { type: string; id: string }) {
  const classes = useStyles();
  const { data: entity } = useEntity({ type, id });
  return (
    <div className={classes.token}>
      <strong>{type.toUpperCase().substring(0, 3)} </strong>
      {entity?.name ?? id}
    </div>
  );
}

/**
 * Display a list of entity badges, in a row wrapping flex container.
 * @param entities - the entities you want to display
 * @param ignoreTypes - if you want to hide some specific entity types, like documents
 * @param entityLabels - entities does not contain a string label but only a numeric id, so you can provide a label for each id here
 * @param rest - gets spread into the wrapper div
 */
export function EntityListDisplay({
  entities,
  ignoreTypes,
  ...rest
}: {
  entities: GenericResultItem["source"]["entities"];
  ignoreTypes?: string[];
} & React.HTMLAttributes<HTMLDivElement>) {
  const classes = useStyles();

  const entityList = entities?.filter(
    (entity) => !ignoreTypes || !ignoreTypes.includes(entity.entityType),
  );

  return (
    <div className={classes.entities} {...rest}>
      {entityList?.map((e) => (
        <EntityDisplay id={e.id} type={e.entityType} key={e.id} />
      ))}
    </div>
  );
}
