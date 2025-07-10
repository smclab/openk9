import { createTheme } from "@mui/material";

export const defaultThemeK9 = createTheme({
	components: {
		MuiButton: {
			styleOverrides: {
				root: ({ ownerState }) => ({
					minWidth: "20px",
					borderRadius: "50px",
					"&:hover": {
						color: "white",
					},
				}),
			},
		},
	},
	palette: {
		primary: {
			main: "#E6E6E6",
			contrastText: "#636363",
			light: "#fff",
			dark: "#F88078",
		},
		secondary: {
			main: "#fff",
			contrastText: "#d54949",
			light: "#F88078",
			dark: "#9C0E10",
		},
		text: { primary: "#272727" },
		background: {
			default: "linear-gradient(#EEEEEE 100%, #888888 100%)",
		},
	},
	shape: { borderRadius: 16 },
	spacing: 7,
	typography: {
		body1: {
			fontFamily: "sans-serif",
			fontSize: "16px",
			lineHeight: "24px",
			fontWeight: "400",
		},

		h2: {
			lineHeight: "1.5",
			fontFamily: "Roboto, sans-serif",
			fontSize: "24px",
			fontWeight: "700",
		},
		body2: {
			fontFamily: "Open Sans, sans-serif",
			fontWeight: "400",
			fontSize: "14px",
			lineHeight: "20px",
			letterSpacing: "0.15px",
		},
		h5: {
			fontFamily: "sans-serif",
			fontWeight: "500",
			fontSize: "24px",
			lineHeight: "29px",
			alignItems: "center",
		},
		h6: {
			fontFamily: "sans-serif",
			fontWeight: "600",
			fontSize: "20px",
			lineHeight: "32px",
			alignItems: "center",
		},
		subtitle1: {
			fontFamily: "Open Sans, sans-serif",
			fontWeight: "600",
			fontSize: "14px",
			lineHeight: "22px",
			letterSpacing: "0.1px",
		},
	},
});
