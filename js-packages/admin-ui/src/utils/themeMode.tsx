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

/**
 * The theme preference, shared between the AppBar switcher and the Admin
 * Settings page.
 *
 * A change applies and persists in one step, so both entry points behave the
 * same way and the choice survives a reload without any explicit save.
 *
 * The preference lives in the `isDarkMode` key the switcher has always used, so
 * there is nothing to migrate.
 */
import React from "react";

export type ThemeMode = "light" | "dark";

const STORAGE_KEY = "isDarkMode";

function readMode(): ThemeMode {
  return localStorage.getItem(STORAGE_KEY) === "true" ? "dark" : "light";
}

function writeMode(mode: ThemeMode): void {
  localStorage.setItem(STORAGE_KEY, String(mode === "dark"));
}

export type ThemeModeContextValue = {
  mode: ThemeMode;
  isDarkMode: boolean;
  /** Applies and persists. */
  setMode(next: ThemeMode): void;
  /** Flips light/dark, for the AppBar switcher. */
  toggleTheme(): void;
};

const ThemeModeContext = React.createContext<ThemeModeContextValue | null>(null);

export const ThemeModeContextProvider = ThemeModeContext.Provider;

export function useThemeMode(): ThemeModeContextValue {
  const value = React.useContext(ThemeModeContext);
  if (value === null) {
    throw new Error("useThemeMode must be used inside a ThemeModeContextProvider");
  }
  return value;
}

/**
 * Owns the preference. Called once by `App`, which both feeds the context and
 * picks the MUI theme out of it. The returned value is memoized on the mode:
 * `App` re-renders on scroll and on every keystroke in the nav search box, and
 * consumers must not re-render with it.
 */
export function useThemeModeState(): ThemeModeContextValue {
  const [mode, setModeState] = React.useState<ThemeMode>(readMode);

  const setMode = React.useCallback((next: ThemeMode) => {
    writeMode(next);
    setModeState(next);
  }, []);

  const toggleTheme = React.useCallback(() => setMode(mode === "dark" ? "light" : "dark"), [mode, setMode]);

  return React.useMemo(
    () => ({ mode, isDarkMode: mode === "dark", setMode, toggleTheme }),
    [mode, setMode, toggleTheme],
  );
}
