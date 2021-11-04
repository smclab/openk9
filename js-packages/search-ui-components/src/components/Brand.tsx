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
import React from "react";
import { createUseStyles } from "react-jss";
import { BrandLogo } from "../icons/BrandLogo";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  brand: {
    display: "flex",
    alignItems: "center",
    fontSize: 20,
    letterSpacing: "-0.05ch",
    "& span": { fontWeight: "bold" },
  },
  brandLogo: {
    marginRight: "0.4em",
    color: theme.digitalLakePrimary,
  },
  badge: {
    backgroundColor: "white",
    borderRadius: theme.borderRadiusSm,
    height: 38,
    margin: [0, theme.spacingUnit * 2],
    padding: [0, theme.spacingUnit * 2],
  },
}));

/**
 * Use this component for OpenK9 branding (logo and title).
 * It's open, it has a dog on it, it's cute!
 */
export function Brand({
  badge,
  className,
  size = 28,
  ...rest
}: { badge?: boolean; size?: number } & React.HTMLAttributes<HTMLDivElement>) {
  const classes = useStyles();
  return (
    <div
      {...rest}
      className={clsx(classes.brand, badge && classes.badge, className)}
      style={{ fontSize: size - 8 }}
    >
      <BrandLogo size={size} className={classes.brandLogo} />
      Open<span>K9</span>
    </div>
  );
}
