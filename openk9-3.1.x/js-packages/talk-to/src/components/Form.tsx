import styled from "@emotion/styled";
import { Box, Button } from "@mui/material";

export const SkeletonK9 = styled(Box)(({ theme, color }: { theme: any; color: "primary" | "secondary" }) => {
	const boxK9 = {
		backgroundColor: theme.palette.primary.main,
	};
	const boxSecondaryK9 = {
		backgroundColor: theme.palette.secondary.main,
	};
	return color === "secondary" ? boxK9 : boxSecondaryK9;
});

export const LabelButton = styled(Button)(({ theme }) => ({
	textTransform: "none",
	backgroundColor: "transparent",
	color: "black",
	"&:hover": {
		color: "black",
		textDecoration: "underline",
	},
}));
