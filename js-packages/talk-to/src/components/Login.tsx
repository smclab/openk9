import LoginIcon from "@mui/icons-material/Login";
import LogoutIcon from "@mui/icons-material/Logout";
import { Button } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { useQueryClient } from "react-query";
import { OpenK9Client } from "./client";

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
		<Button
			variant="contained"
			size="small"
			onClick={() => {
				if (authenticated) {
					handleLogout();
				} else {
					handleLogin();
				}
			}}
			sx={{
				minWidth: "auto",
				px: 2,
				py: 0.5,
				fontSize: "0.875rem",
				borderRadius: "10px",
				boxShadow: "none",
				border: "1px solid rgba(0, 0, 0, 0.12)",
				"&:hover": {
					boxShadow: "none",
					borderColor: "primary.main",
					backgroundColor: "#C0272B",
				},
			}}
		>
			{authenticated ? (
				<>
					<LogoutIcon />
					{t("logout")}
				</>
			) : (
				<>
					<LoginIcon />
					{t("login")}
				</>
			)}
		</Button>
	);
}
