import { Box, Button, CssBaseline } from "@mui/material";
import { keyframes, ThemeProvider as MuiThemeProvider } from "@mui/material/styles";
import React from "react";
import { useTranslation } from "react-i18next";
import { useQuery } from "react-query";
import { v4 as uuidv4 } from "uuid";
import "./App.css";
import { getUserProfile } from "./components/authentication";
import { OpenK9Client } from "./components/client";
import { InitialConversation } from "./components/InitialConversation";
import { kc } from "./auth/kc";
import { MessageCard } from "./components/MessageCard";
import Search from "./components/Search";
import Sidebar from "./components/Sidebar";
import { defaultThemeK9 } from "./components/theme";
import useGenerateResponse, { Message } from "./components/useGenerateResponse";
import { Logo } from "./Svg/Logo";

function App() {
	const [chatId, setChatId] = React.useState<chatId>({ id: null, isNew: true });
	const [userId, setUserId] = React.useState<string | undefined | null>();
	const [retrieveFromUploadedDocuments, setRetrieveFromUploadedDocuments] = React.useState<boolean | undefined>(
		undefined,
	);
	const messagesEndRef = React.useRef<HTMLDivElement | null>(null);
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
	const handleSearch = (query: string, retrieveFromUploadedDocumentsParam?: boolean) => {
		const flag = retrieveFromUploadedDocumentsParam ?? retrieveFromUploadedDocuments;
		chatId?.id && generateResponse(query, chatId?.id || "", flag);
	};

	React.useEffect(() => {
		if (messagesEndRef.current) {
			messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
		}
	}, [messages]);

	React.useEffect(() => {
		setRetrieveFromUploadedDocuments(undefined);
	}, [chatId?.id]);

	React.useEffect(() => {
		if (!chatId?.isNew && initialMessages?.retrieveFromUploadedDocuments !== undefined) {
			setRetrieveFromUploadedDocuments(initialMessages.retrieveFromUploadedDocuments);
		}
	}, [chatId?.isNew, initialMessages?.retrieveFromUploadedDocuments]);

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
				<Box
					className="k9-generation-sideNavigation"
					display="flex"
					flexDirection="column"
					minWidth={225}
					maxWidth={245}
					p={2}
					alignItems="flex-start"
					position="relative"
					sx={{
						fontSize: 20,
						color: "#1e1c21",
						background: defaultThemeK9.palette.background.default,
						overflow: "hidden",
						paddingRight: 0,
					}}
				>
					<Sidebar setChatId={setChatId} />
				</Box>

				<Box
					display="flex"
					flexDirection="column"
					flexGrow={1}
					p={2}
					sx={{
						background: "#EEEEEE",
					}}
				>
					<CssBaseline />
					<Box display={"flex"} flexDirection={"column"} flex={1} gap={"14px"}>
						{/* main content */}
						<Box
							component="main"
							flex={1}
							bgcolor="white"
							display="flex"
							flexDirection="column"
							alignItems="center"
							justifyContent={messages.length !== 0 ? "flex-start" : "center"}
							p={2}
							boxSizing="border-box"
							zIndex={2}
							width="100%"
							border={"1px solid rgba(0, 0, 0, 0.12)"}
							borderRadius={"10px"}
						>
							<Box
								component="header"
								width={"100%"}
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
											const newId = kc.authenticated ? `${userId}_${timestamp}` : `anonymous_${uuidv4()}_${timestamp}`;
											setChatId({ id: newId, isNew: true });
										}}
									>
										{t("new-chat", { defaultValue: "New Chat" })}
									</Button>
								)}
							</Box>
							<div
								className="openk9-box-main"
								style={{
									display: "flex",
									flexDirection: "column",
									gap: "50px",
									padding: messages.length !== 0 ? "30px 50px" : "unset",
									width: "100%",
									overflowY: "auto",
									maxHeight: "69vh",
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
									: !isLoadingChat && <InitialConversation handleSearch={handleSearch} />}
								{isLoadingChat && <Loading />}
							</div>
						</Box>
						{/* Search Area */}
						<Box
							height="12vh"
							width="100%"
							display="flex"
							alignItems="center"
							justifyContent="center"
							overflow={"hidden"}
							sx={{
								background: "white",
								borderRadius: "10px",
								border: "1px solid rgba(0, 0, 0, 0.12)",
								padding: "0 16px",
								boxSizing: "border-box",
							}}
						>
							<Search
								handleSearch={handleSearch}
								cancelAllResponses={cancelAllResponses}
								isChatting={isChatting}
								onUploadFiles={async (files) => {
									if (!kc.authenticated || !chatId?.id) throw new Error("Not authenticated or no chat");
									return client.uploadFiles(chatId.id, files);
								}}
								isAuthenticated={!!kc.authenticated}
								retrieveFromUploadedDocuments={retrieveFromUploadedDocuments}
								onSetRetrieveFromUploadedDocuments={(v) => setRetrieveFromUploadedDocuments(v)}
							/>
						</Box>
					</Box>
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
				return { messages: [], retrieve_from_uploaded_documents: undefined };
			}
			return await client.getInitialMessages(chatId?.id || "");
		},
		{
			enabled: !!chatId?.id,
		},
	);

	return {
		initialMessages: {
			recoveryChat: initialMessagesQuery.data?.messages,
			isLoadingChat: initialMessagesQuery.isLoading || false,
			retrieveFromUploadedDocuments: initialMessagesQuery.data?.retrieve_from_uploaded_documents,
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
