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
