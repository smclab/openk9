import React from "react";
import { Box, Typography, IconButton } from "@mui/material";
import { SvgIconComponent } from "@mui/icons-material";

interface SuggestionsProps {
	name: string;
	Icon: SvgIconComponent;
	onAction?: () => void;
}

const Suggestions: React.FC<SuggestionsProps> = ({ name, Icon, onAction }) => {
	const [isHovered, setIsHovered] = React.useState(false);
	return (
		<Box display="flex" alignItems="center">
			<IconButton color="primary">
				<Icon />
			</IconButton>
			<Typography
				variant="h6"
				onClick={onAction}
				onMouseEnter={() => setIsHovered(true)}
				onMouseLeave={() => setIsHovered(false)}
				sx={{
					cursor: onAction ? "pointer" : "default",
					textDecoration: isHovered && onAction ? "underline" : "",
				}}
			>
				{name}
			</Typography>
		</Box>
	);
};

export default Suggestions;
