import React from "react";
import { useQueryClient } from "react-query";
import { Box } from "@mui/material";
import { OpenK9Client } from "./client";
import { LabelButton } from "./Form";
import LoginIcon from "@mui/icons-material/Login";
import LogoutIcon from "@mui/icons-material/Logout";
import { useTranslation } from "react-i18next";

export function Login({
	authenticated,
	setAuthenticated,
}: {
	authenticated: boolean;
	setAuthenticated: React.Dispatch<React.SetStateAction<boolean>>;
}) {
	const client = OpenK9Client();
	const queryClient = useQueryClient();
	const { t } = useTranslation();

	const handleLogin = async () => {
		await client.authenticate();
		setAuthenticated(true);
		queryClient.invalidateQueries(["user-profile"]);
	};

	const handleLogout = async () => {
		await client.deauthenticate();
		setAuthenticated(false);
		queryClient.invalidateQueries(["user-profile"]);
	};

	return (
		<Box sx={{ marginRight: "10px" }}>
			{authenticated ? (
				<LabelButton onClick={handleLogout}>
					<LogoutIcon />
					<span style={{ marginLeft: "10px" }}>{t("logout")}</span>
				</LabelButton>
			) : (
				<LabelButton onClick={handleLogin}>
					<LoginIcon />
					<span style={{ marginLeft: "10px" }}>{t("login")}</span>
				</LabelButton>
			)}
		</Box>
	);
}
