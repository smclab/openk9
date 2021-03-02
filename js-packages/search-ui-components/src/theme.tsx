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

export const defaultTheme = {
  digitalLakeMain: "#1d2124",
  digitalLakeMainL1: "#252b2f",
  digitalLakeMainL2: "#2f363b",
  digitalLakeMainL3: "#62696e",
  digitalLakeMainL4: "#9ba6af",
  digitalLakeMainL5: "#c0c7cc",
  digitalLakeMainL6: "#dadee1",
  digitalLakePrimary: "#0073e6",
  digitalLakePrimaryD2: "#005ab3",
  digitalLakePrimaryD1: "#0066cd",
  digitalLakePrimaryL1: "#5caeff",
  digitalLakePrimaryL2: "#94caff",
  digitalLakePrimaryL3: "#cde6ff",
  digitalLakeError: "#b51f2e",
  digitalLakeErrorL1: "#e87a85",
  digitalLakeErrorL2: "#f7ced2",
  digitalLakeWarningD1: "#0f0d0a",
  digitalLakeWarning: "#ff8800",
  digitalLakeWarningL1: "#ffb866",
  digitalLakeWarningL2: "#fff3e6",
  digitalLakeSuccess: "#007e33",
  digitalLakeSuccessL1: "#0eff6f",
  digitalLakeSuccessL2: "#bdffd7",
  digitalLakeInfo: "#0063cc",
  digitalLakeInfoL1: "#66a3ff",
  digitalLakeInfoL2: "#dbeaff",
  digitalLakeBackground: "#f1f2f5",
  digitalLakeBackgroundL1: "#f7f8f9",
  digitalLakeGrayLighter: "#f6f6f6",
  digitalLakeGrayLight: "#c0c7cc",

  spacingUnit: 8,

  searchMaxWidth: 700,
  pageMaxWidth: 1000,
  adminSidebarWidth: 240,

  borderRadius: 4,
  borderRadiusLg: 6,
  borderRadiusSm: 2,

  baseBoxShadow: "1px 20px 30px 0px rgba(56,77,104,0.14)",
  navigationShadow: "1px 4px 15px 0px rgba(56,77,104,0.14)",
};

export type ThemeType = typeof defaultTheme;
