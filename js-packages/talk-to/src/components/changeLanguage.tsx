import { Button, Menu, MenuItem } from "@mui/material";
import { useTranslation } from "react-i18next";
import React from "react";
import LanguageIcon from "@mui/icons-material/Language";
import { useQuery } from "react-query";
import { OpenK9Client } from "./client";
import { useUser } from "./ChatInfoContext";

const ChangeLanguage = () => {
	const { i18n } = useTranslation();
	const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
	const { setLanguage, language } = useUser();

	const handleClick = (event: React.MouseEvent<HTMLElement>) => {
		setAnchorEl(event.currentTarget);
	};

	const handleClose = () => {
		setAnchorEl(null);
	};

	const changeLanguage = (lng: string) => {
		i18n.changeLanguage(lng);
		setLanguage?.(lng);
		handleClose();
	};

	const client = OpenK9Client();

	const { data: languagesTest = [] } = useQuery({
		queryKey: ["available-languages"],
		queryFn: client.getAvailableLanguages,
		staleTime: Infinity,
		cacheTime: Infinity,
	});

	useQuery({
		queryKey: ["default-language"],
		queryFn: client.getDefaultLanguage,
		onSuccess: (defaultLang) => {
			console.log("defaultLang", defaultLang, languagesTest);

			if (defaultLang) {
				i18n.changeLanguage(defaultLang.value || "en");
				setLanguage?.(defaultLang.value || "en");
			}
		},
		onError: (error) => {
			console.error("Error fetching default language:", error);
			i18n.changeLanguage("en");
		},
		enabled: !!languagesTest.length,
	});

	const languages: { label: string; value: string }[] = languagesTest
		.map((lang) => {
			if (lang.name && lang.value) {
				return {
					label: lang.name,
					value: lang.value,
				};
			}
			return undefined;
		})
		.filter((val): val is { label: string; value: string } => val !== undefined);

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
				{i18n.language && languageMapped(language)}
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
				{languages?.map((language, index) => (
					<MenuItem key={language?.value || index} onClick={() => changeLanguage(language.value)}>
						{language.label}
					</MenuItem>
				))}
			</Menu>
		</>
	);
};

export const ChangeLanguageMemo = React.memo(ChangeLanguage);

function languageMapped(value: string | undefined) {
	switch (value) {
		case "en_US":
			return "EN";
		case "fr_FR":
			return "FR";
		case "it_IT":
			return "IT";
		case "es_ES":
			return "ES";
		default:
			return "en_US";
	}
}
