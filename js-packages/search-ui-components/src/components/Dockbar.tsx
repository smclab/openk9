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
import { HamburgerIcon } from "../icons/HamburgerIcon";
import { ThemeType } from "../theme";
import { Brand } from "./Brand";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    backgroundColor: theme.digitalLakeMainL2,
    minHeight: 48,
    display: "flex",
    alignItems: "center",
    position: "sticky",
    top: 0,
    zIndex: 999,
  },
  hamburger: {
    padding: [theme.spacingUnit * 1, theme.spacingUnit * 2],
    marginLeft: theme.spacingUnit * 1,
    marginRight: theme.spacingUnit * -1,
    backgroundColor: "transparent",
    border: "none",
  },
  brand: {
    backgroundColor: "white",
    borderRadius: theme.borderRadiusSm,
    height: 38,
    margin: [0, theme.spacingUnit * 2],
    padding: [0, theme.spacingUnit * 2],
    display: "flex",
    alignItems: "center",
    fontSize: 20,
    letterSpacing: "-0.05ch",
    "& span": { fontWeight: "bold" },
  },
  brandLogo: {
    marginRight: 8,
  },
  spacer: { flexGrow: 1 },
  endButtons: {
    display: "flex",
    alignItems: "center",
    margin: [0, theme.spacingUnit * 1],
  },
  notifications: {
    width: 12,
    height: 12,
    borderRadius: "100%",
    backgroundColor: theme.digitalLakePrimary,
    margin: [0, theme.spacingUnit * 1],
  },
  user: {
    width: 26,
    height: 26,
    borderRadius: theme.borderRadius,
    backgroundColor: "white",
    margin: [0, theme.spacingUnit * 1],
  },
}));

/**
 * Common dockbar component to mock the one from Open Square design language.
 * @param onHamburgerAction - called when the user clicks on the three bars hambuger menu, set me to undefined if you don't want an hamburger menu (it may also be a good idea...)
 * @param onLoginAction - called when the user clicks on the upper right login button, set me to undefined if you don't want a login button
 */
export function Dockbar({
  onHamburgerAction,
  onLoginAction,
}: {
  onHamburgerAction?: () => void;
  onLoginAction?: () => void;
}) {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      {onHamburgerAction && (
        <button className={classes.hamburger} onClick={onHamburgerAction}>
          <HamburgerIcon />
        </button>
      )}

      <Brand badge />

      <div className={classes.spacer} />

      <div className={classes.endButtons}>
        {onLoginAction ? (
          <button className="btn btn-secondary" onClick={onLoginAction}>
            Login
          </button>
        ) : (
          <>
            {/* TODO: this is a placeholder to make it look like Open Square, but it serves no function */}
            <div className={classes.notifications} />
            <div className={classes.user} />
          </>
        )}
      </div>
    </div>
  );
}
