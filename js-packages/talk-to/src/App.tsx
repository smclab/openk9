import React from "react";
import { Box, Typography, CssBaseline, Button } from "@mui/material";
import Search from "./components/Search";
import { Logo } from "./Svg/Logo";
import { keyframes, ThemeProvider as MuiThemeProvider } from "@mui/material/styles";
import { defaultThemeK9 } from "./components/theme";
import { Graphic } from "./Svg/Graphic";
import { GraphicTwo } from "./Svg/GraphicsTwo";
import { GraphicThree } from "./Svg/GraphicsThree";
import useGenerateResponse, { Message } from "./components/useGenerateResponse";
import { HistoryChat } from "./components/HistoryChat";
import { InitialConversation } from "./components/InitialConversation";
import { MessageCard } from "./components/MessageCard";
import "./App.css";
import { OpenK9Client } from "./components/client";
import { Login } from "./components/Login";
import { useQuery } from "react-query";
import { getUserProfile } from "./components/authentication";
import ChangeLanguage from "./components/changeLanguage";
import { useTranslation } from "react-i18next";
import { v4 as uuidv4 } from "uuid";
import { keycloak } from "./components/keycloak";

function App() {
	const [chatId, setChatId] = React.useState<chatId>({ id: null, isNew: true });
	const [userId, setUserId] = React.useState<string | undefined | null>();
	const messagesEndRef = React.useRef<HTMLDivElement | null>(null);
	const [authenticated, setAuthenticated] = React.useState(false);
	const { initialMessages } = useChatData(userId || "", chatId);
	const isLoadingChat = !chatId?.isNew && initialMessages.isLoadingChat;
	const { t } = useTranslation();

	const {
		messages,
		generateResponse,
		cancelAllResponses,
		isChatting,
		isLoading: isGenerateMessage,
	} = useGenerateResponse({
		initialMessages: (initialMessages.recoveryChat as Message[]) || [],
	});

	const isNewChat = messages.length === 0;
	const client = OpenK9Client();
	const handleSearch = (query: string) => {
		chatId?.id && generateResponse(query, chatId?.id || "", userId);
	};

	React.useEffect(() => {
		if (messagesEndRef.current) {
			messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
		}
	}, [messages]);

	React.useEffect(() => {
		client.authInit.then(setAuthenticated);
	}, [client]);

	React.useEffect(() => {
		async function fetchUserProfile() {
			try {
				const profile: { sub: string } = await getUserProfile();
				if (profile.sub) {
					const userId = profile.sub + "_" + String(Date.now());
					setUserId(profile.sub);
					setChatId({ id: userId, isNew: true });
				} else {
					const userId = String(Date.now());
					setChatId({ id: userId, isNew: true });
				}
			} catch (error) {
				const userId = String(Date.now());
				setChatId({ id: userId, isNew: true });
			}
		}

		fetchUserProfile();
	}, []);

	return (
		<MuiThemeProvider theme={defaultThemeK9}>
			<Box display="flex" height="100vh">
				{/* sideNavigation */}
				<Box
					className="k9-generation-sideNavigation"
					display="flex"
					flexDirection="column"
					minWidth={225}
					maxWidth={245}
					p={2}
					alignItems="flex-start"
					position="relative"
					gap={2}
					sx={{
						fontSize: 20,
						color: "#1e1c21",
						background: defaultThemeK9.palette.background.default,
						overflow: "hidden",
						paddingRight: 0,
					}}
					// flexGrow={1}
				>
					<Box
						display={"flex"}
						justifyContent={"space-between"}
						alignItems={"center"}
						flexDirection={"column"}
						height={"100vh"}
						width={"100%"}
						sx={{
							background: "white",
							overflow: "hidden",
							borderRadius: "10px",
							position: "relative",
						}}
					>
						<Box sx={{ paddingInline: "10px", marginTop: "10px" }}>
							<Box display="flex" alignItems="center" mb={1} sx={{ justifyContent: "center" }}>
								<Logo size={45} />
								<Typography variant="h6" ml={1}>
									Open
								</Typography>
								<Typography variant="h5" fontWeight={700}>
									K9
								</Typography>
							</Box>
							<Box
								display="flex"
								sx={{ justifyContent: "center", gap: "10px", flexDirection: "column", alignItems: "center" }}
							>
								<Typography variant="body2">{t("version")} 1.6.03</Typography>
								<ChangeLanguage />
							</Box>
							<Box sx={{ display: "flex" }} mt={2}>
								{authenticated && <HistoryChat setChatId={setChatId} userId={userId} />}
							</Box>
						</Box>
						<Box
							display="flex"
							zIndex={3}
							height={"9vh"}
							paddingTop={"5px"}
							borderTop={"1px solid #616161"}
							width={"100%"}
							sx={{ justifyContent: "center", flexDirection: "column", alignItems: "center" }}
						>
							<Login authenticated={authenticated} setAuthenticated={setAuthenticated} />
						</Box>
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
				<Box
					display="flex"
					flexDirection="column"
					flexGrow={1}
					p={2}
					sx={{
						background:
							"linear-gradient(227deg, rgba(175, 175, 175, 0.8), rgba(23, 204, 23, 0) 57%), " +
							"linear-gradient(269deg, rgba(59, 59, 70, 0.97), rgba(0, 0, 255, 0) 70.71%), " +
							"linear-gradient(112deg, #EFEFEF, rgba(0, 0, 255, 0) 70.71%), " +
							"linear-gradient(52deg, #EFEFEF, rgba(0, 0, 255, 0) 70.71%)",
					}}
				>
					<CssBaseline />
					<Box
						component="header"
						p={2}
						bgcolor="background.paper"
						display="flex"
						justifyContent="flex-end"
						alignItems="center"
						padding={0}
						sx={{ borderTopLeftRadius: "10px", borderTopRightRadius: "10px" }}
						zIndex={2}
					>
						{!isNewChat && (
							<Button
								variant="contained"
								sx={{ margin: "10px", borderRadius: "10px" }}
								onClick={() => {
									const timestamp = String(Date.now());
									const newId = keycloak.authenticated
										? `${userId}_${timestamp}`
										: `anonymous_${uuidv4()}_${timestamp}`;
									setChatId({ id: newId, isNew: true });
								}}
							>
								{t("new-chat")}
							</Button>
						)}
					</Box>
					{/* main content */}
					<Box
						component="main"
						flex={1}
						bgcolor="white"
						display="flex"
						flexDirection="column"
						alignItems="center"
						p={2}
						boxSizing="border-box"
						zIndex={2}
						width={"100%"}
						sx={{
							borderTopLeftRadius: isNewChat ? "10px" : "unset",
							borderTopRightRadius: isNewChat ? "10px" : "unset",
						}}
					>
						<div
							className="openk9-box-main"
							style={{
								display: "flex",
								flexDirection: "column",
								gap: "50px",
								padding: messages.length !== 0 ? "30px 50px" : "unset",
								width: "100%",
								overflowY: "auto",
								maxHeight: "71vh",
								justifyContent: "flex-start",
							}}
						>
							{!isNewChat
								? messages.map((message, index) => (
										<React.Fragment key={index}>
											<MessageCard message={message} isGenerateMessage={isGenerateMessage} />
											{index === messages.length - 1 && <div ref={messagesEndRef} />}
										</React.Fragment>
								  ))
								: !isLoadingChat && <InitialConversation />}
							{isLoadingChat && <Loading />}
						</div>
					</Box>
					<Search handleSearch={handleSearch} cancelAllResponses={cancelAllResponses} isChatting={isChatting} />
				</Box>
			</Box>
		</MuiThemeProvider>
	);
}

export default App;

export interface TypeGenerationResponse {
	refreshOnSuggestionCategory: boolean;
	refreshOnTab: boolean;
	refreshOnDate: boolean;
	refreshOnQuery: boolean;
	retrieveType: string;
}

function useChatData(userId: string, chatId: { id: string | null; isNew: boolean } | null) {
	const client = OpenK9Client();

	const initialMessagesQuery = useQuery(
		["initial-message", chatId?.id as string],
		async () => {
			if (chatId?.isNew) {
				return [];
			}
			return (await client.getInitialMessages(chatId?.id || "")).messages;
		},
		{
			enabled: !!chatId?.id,
		},
	);

	return {
		initialMessages: {
			recoveryChat: initialMessagesQuery.data,
			isLoadingChat: initialMessagesQuery.isLoading || false,
		},
		initialMessagesError: initialMessagesQuery.error,
		initialMessagesLoading: initialMessagesQuery.isLoading,
	};
}

type chatId = {
	id: string | null;
	isNew: boolean;
} | null;

const pulseAnimation = keyframes`
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0.5;
  }
`;

function Loading() {
	return (
		<Box
			sx={{
				display: "flex",
				justifyContent: "center",
				alignItems: "center",
				height: "100vh",
			}}
		>
			<Box
				sx={{
					animation: `${pulseAnimation} 1s infinite alternate`,
					boxShadow: "0px 0px 1px 1px #0000001a",
					background: "inherit",
					display: "flex",
					justifyContent: "space-evenly",
					alignItems: "center",
					borderRadius: "20px",
				}}
			>
				<Logo size={150} />
			</Box>
		</Box>
	);
}
