import { Button, Menu, MenuItem } from "@mui/material";
import { useTranslation } from "react-i18next";
import React from "react";
import LanguageIcon from "@mui/icons-material/Language";

const ChangeLanguage = () => {
	const { i18n } = useTranslation();
	const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

	const handleClick = (event: React.MouseEvent<HTMLElement>) => {
		setAnchorEl(event.currentTarget);
	};

	const handleClose = () => {
		setAnchorEl(null);
	};

	const changeLanguage = (lng: string) => {
		i18n.changeLanguage(lng);
		handleClose();
	};

	const languages: { value: string; label: string }[] = [
		{ value: "it", label: "Italiano" },
		{ value: "en", label: "English" },
		{ value: "fr", label: "Français" },
		{ value: "es", label: "Español" },
	];

	return (
		<>
			<Button
				onClick={handleClick}
				startIcon={<LanguageIcon />}
				size="small"
				variant="outlined"
				sx={{
					color: "#333",
					minWidth: "auto",
					px: 1,
					py: 0.5,
					fontSize: "0.875rem",
					position: "relative",
					zIndex: 2,
					borderRadius: "10px",
					borderColor: "rgba(0, 0, 0, 0.12)",
					"&:hover": {
						borderColor: "primary.main",
						backgroundColor: "#C0272B",
					},
				}}
			>
				{i18n.language.toUpperCase()}
			</Button>
			<Menu
				anchorEl={anchorEl}
				open={Boolean(anchorEl)}
				onClose={handleClose}
				slotProps={{
					paper: {
						sx: {
							boxShadow: "none",
							borderRadius: "10px",
							border: "1px solid rgba(0, 0, 0, 0.25)",
						},
					},
				}}
				anchorOrigin={{
					vertical: "top",
					horizontal: "center",
				}}
				transformOrigin={{
					vertical: "bottom",
					horizontal: "center",
				}}
			>
				{languages.map((language) => (
					<MenuItem key={language.value} onClick={() => changeLanguage(language.value)}>
						{language.label}
					</MenuItem>
				))}
			</Menu>
		</>
	);
};

export default ChangeLanguage;
