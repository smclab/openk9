import { Box, Typography } from "@mui/material";
import { t } from "i18next";
import React from "react";
import { Graphic } from "../Svg/Graphic";
import { GraphicThree } from "../Svg/GraphicsThree";
import { GraphicTwo } from "../Svg/GraphicsTwo";
import { Logo } from "../Svg/Logo";
import { ChangeLanguageMemo } from "./changeLanguage";
import { OpenK9Client } from "./client";
import { HistoryChat } from "./HistoryChat";
import { Login } from "./Login";
import fetchUserProfile, { chatId } from "./utils/fetchUserProfile";

const Sidebar = ({ setChatId }: { setChatId: React.Dispatch<React.SetStateAction<chatId>> }) => {
	const client = OpenK9Client();
	const [authenticated, setAuthenticated] = React.useState(false);
	const [userId, setUserId] = React.useState<string | undefined | null>();

	React.useEffect(() => {
		client.authInit.then(setAuthenticated);
	}, [client]);

	React.useEffect(() => {
		fetchUserProfile({ setChatId: setChatId, setUserId: setUserId });
	}, []);

	return (
		<Box
			className="k9-generation-sideNavigation"
			display="flex"
			flexDirection="column"
			height="100%"
			width="100%"
			gap={2}
		>
			{/* Main Content Area */}
			<Box
				display="flex"
				flexDirection="column"
				height="calc(100% - 12vh)"
				width="100%"
				sx={{
					background: "white",
					overflow: "hidden",
					borderRadius: "10px",
					position: "relative",
					border: "1px solid rgba(0, 0, 0, 0.12)",
				}}
			>
				<Box sx={{ paddingInline: "10px", marginTop: "10px" }}>
					<Box display="flex" alignItems="center" mb={1} sx={{ justifyContent: "center", paddingBlock: "5px" }}>
						<Logo size={45} />
						<Typography variant="h6" ml={1}>
							Open
						</Typography>
						<Typography variant="h5" fontWeight={700}>
							K9
						</Typography>
					</Box>
					<Box sx={{ display: "flex" }} mt={2}>
						{authenticated && <HistoryChat setChatId={setChatId} userId={userId} />}
					</Box>
				</Box>
			</Box>

			{/* Login Area */}
			<Box
				display="flex"
				width="100%"
				height="12vh"
				borderRadius="10px"
				bgcolor="white"
				overflow="hidden"
				position="relative"
				border="1px solid rgba(0, 0, 0, 0.12)"
				sx={{ flexDirection: "column" }}
			>
				{/* Main Login Area */}
				<Box display="flex" flex={1} alignItems="center" justifyContent="space-between" px={2} zIndex={2}>
					<Box sx={{ transform: "scale(0.8)" }}>
						<ChangeLanguageMemo />
					</Box>
					<Box sx={{ transform: "scale(0.8)" }}>
						<Login authenticated={authenticated} setAuthenticated={setAuthenticated} />
					</Box>
				</Box>

				{/* Version Footer */}
				<Box
					sx={{
						background: "#f5f5f5",
						padding: "4px 16px",
						fontSize: "0.75rem",
						color: "text.secondary",
						borderTop: "1px solid rgba(0, 0, 0, 0.12)",
					}}
				>
					{t("version")} 1.6.03
				</Box>

				{/* Graphics */}
				<Box position="absolute" left={0} bottom={10}>
					<Graphic />
				</Box>
				<Box position="absolute" left={75} bottom={45}>
					<GraphicThree />
				</Box>
				<Box position="absolute" left={80} bottom={0}>
					<GraphicTwo />
				</Box>
			</Box>
		</Box>
	);
};

export default Sidebar;
