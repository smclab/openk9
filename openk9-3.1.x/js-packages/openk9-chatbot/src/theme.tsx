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
import { createTheme } from "@mui/material/styles";

export const defaultThemeK9 = createTheme({
  breakpoints: {
    values: {
      xs: 0,
      sm: 576,
      md: 768,
      lg: 992,
      xl: 1200,
    },
  },
  palette: {
    primary: {
      main: "#C0272B",
      contrastText: "#FFF",
      dark: "#b10707",
      light: "#e77979",
    },
    text: {
      primary: "#000000",
      secondary: "#666666",
    },
  },
  typography: {
    allVariants: {
      fontFamily: "Titillium Web",
    },
    fontFamily: "Titillium Web",
    body1: {
      fontWeight: 400,
      fontSize: "12px",
      lineHeight: "19px",
    },
    body2: {
      fontFamily: "Roboto",
      font: "Roboto",
      fontWeight: 500,
      fontSize: "10px",
      lineHeight: "12px",
    },
    h5: {
      fontWeight: 400,
      fontSize: "12px",
      lineHeight: "22px",
    },
  },
  spacing: 2,
  shape: { borderRadius: 2 },
});

