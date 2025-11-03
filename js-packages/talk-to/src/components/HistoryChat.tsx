import { Check, Close, DeleteOutline, DriveFileRenameOutline, MoreVert } from "@mui/icons-material";
import {
	Box,
	Button,
	Dialog,
	DialogActions,
	DialogContent,
	DialogContentText,
	DialogTitle,
	IconButton,
	keyframes,
	List,
	ListItem,
	ListItemButton,
	ListItemText,
	Menu,
	MenuItem,
	TextField,
	Typography,
} from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { useChatContext } from "../context/HistoryChatContext";
import { OpenK9Client } from "./client";
import { jsonObjPost } from "./utils";

const fadeInExpand = keyframes`
  0% {
    opacity: 0;
    max-height: 0;
  }
  100% {
    opacity: 1;
    max-height: 500px;
  }
`;

export function HistoryChat({
	setChatId,
	userId,
}: {
	setChatId: React.Dispatch<
		React.SetStateAction<{
			id: string | null;
			isNew: boolean;
		} | null>
	>;
	userId: string | null | undefined;
}) {
	const [loading, setLoading] = React.useState(true);
	const { t } = useTranslation();
	const { state, dispatch } = useChatContext();
	const { chatHistory } = state;
	const client = OpenK9Client();
	const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
	const [selectedChatId, setSelectedChatId] = React.useState<string | null>(null);
	const [editingChatId, setEditingChatId] = React.useState<string | null>(null);
	const [newTitle, setNewTitle] = React.useState("");

	const [deleteDialogOpen, setDeleteDialogOpen] = React.useState(false);
	const [chatToDelete, setChatToDelete] = React.useState<string | null>(null);

	React.useEffect(() => {
		const fetchData = async () => {
			if (userId) {
				const json: jsonObjPost = {
					userId,
					paginationFrom: 0,
					paginationSize: 10,
				};
				try {
					const result = await client.getHistoryChat(json);
					dispatch({ type: "SET_CHATS", payload: result?.result });
				} catch (error) {
					console.log(error);
				} finally {
					setLoading(false);
				}
			}
		};
		fetchData();
	}, [userId]);

	const handleDeleteChat = async (chatId: string) => {
		try {
			await client.deleteChat(chatId);
			dispatch({ type: "DELETE_CHAT", payload: chatId });
			if (chatId === state.activeChat) {
				setChatId({ id: null, isNew: true });
			}
		} catch (error) {
			console.error("Errore durante l'eliminazione della chat:", error);
		}
	};

	const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, chatId: string) => {
		event.stopPropagation();
		setAnchorEl(event.currentTarget);
		setSelectedChatId(chatId);
	};

	const handleMenuClose = () => {
		setAnchorEl(null);
		setSelectedChatId(null);
	};

	const handleStartRename = (chatId: string) => {
		setEditingChatId(chatId);
		setNewTitle("");
		handleMenuClose();
	};

	const handleConfirmRename = async (chatId: string) => {
		const trimmedTitle = newTitle.trim();
		if (!trimmedTitle) {
			setEditingChatId(null);
			return;
		}

		try {
			await client.renameChat(chatId, trimmedTitle);
			dispatch({
				type: "UPDATE_CHAT_TITLE",
				payload: { chatId, newTitle: trimmedTitle },
			});
			setEditingChatId(null);
		} catch (error) {
			console.error("Errore durante la rinomina:", error);
		}
	};

	// Gestione del dialog di conferma eliminazione
	const handleDeleteClick = (chatId: string) => {
		setChatToDelete(chatId);
		setDeleteDialogOpen(true);
		handleMenuClose();
	};

	const handleConfirmDelete = async () => {
		if (chatToDelete) {
			await handleDeleteChat(chatToDelete);
		}
		setDeleteDialogOpen(false);
		setChatToDelete(null);
	};

	const handleCancelDelete = () => {
		setDeleteDialogOpen(false);
		setChatToDelete(null);
	};

	const chatToDeleteTitle =
		chatHistory?.find((chat) => chat.chat_id === chatToDelete)?.title ||
		chatHistory?.find((chat) => chat.chat_id === chatToDelete)?.question ||
		"questa chat";

	if (loading) return null;

	return (
		<div
			style={{
				display: "flex",
				flex: 1,
				flexDirection: "column",
				gap: "10px",
				marginTop: "40px",
				maxHeight: "64vh",
				alignItems: "center",
			}}
		>
			<Typography variant="subtitle1">{t("recents-chat", { defaultValue: "Recents chat" })}</Typography>
			<List
				sx={{ px: 1 }}
				style={{
					display: "flex",
					flex: 1,
					flexDirection: "column",
					overflow: "auto",
					alignItems: "flex-start",
				}}
			>
				{!chatHistory || chatHistory.length === 0 ? (
					<Typography sx={{ fontSize: "16px", fontWeight: 500, mb: 1, color: "text.secondary" }}>
						{t("no-chats", { defaultValue: "No chats available" })}
					</Typography>
				) : (
					chatHistory.map((item) => (
						<ListItem sx={{ padding: "4px 0" }} key={item.chat_id}>
							<ListItemButton
								sx={{
									borderRadius: "10px",
									"&:hover": { bgcolor: "rgba(0, 0, 0, 0.1)" },
									py: 0.5,
									px: 2,
									zIndex: 10,
									width: "100%",
								}}
							>
								<ListItemText
									onClick={() => setChatId({ id: item?.chat_id, isNew: false })}
									primary={
										editingChatId === item.chat_id ? (
											<Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
												<TextField
													size="small"
													value={newTitle}
													onChange={(e) => setNewTitle(e.target.value)}
													autoFocus
													placeholder={item?.title ?? item?.question}
													sx={{
														"& .MuiInputBase-input": {
															fontSize: "13px",
															padding: "4px 8px",
															borderRadius: "0",
															lineHeight: "1.2",
															height: "40px",
														},
														"& .MuiOutlinedInput-notchedOutline": {
															border: "none",
															borderBottom: "1px solid rgba(0, 0, 0, 0.42)",
															borderRadius: "0",
														},
														"& .MuiOutlinedInput-root:hover .MuiOutlinedInput-notchedOutline": {
															borderBottom: "1px solid rgba(0, 0, 0, 0.87)",
														},
														"& .MuiOutlinedInput-root.Mui-focused .MuiOutlinedInput-notchedOutline": {
															borderBottom: "1px solid #c0272b",
														},
														flex: 1,
													}}
													onClick={(e) => e.stopPropagation()}
												/>
												<IconButton
													size="small"
													onClick={(e) => {
														e.stopPropagation();
														item.chat_id && handleConfirmRename(item.chat_id);
													}}
												>
													<Check sx={{ fontSize: "16px" }} />
												</IconButton>
												<IconButton
													size="small"
													onClick={(e) => {
														e.stopPropagation();
														setEditingChatId(null);
													}}
												>
													<Close sx={{ fontSize: "16px" }} />
												</IconButton>
											</Box>
										) : (
											<Box
												sx={{
													display: "flex",
													alignItems: "center",
													justifyContent: "space-between",
													"& > span": {
														display: "-webkit-box",
														WebkitLineClamp: "2",
														WebkitBoxOrient: "vertical",
														overflow: "hidden",
														textOverflow: "ellipsis",
													},
												}}
											>
												<span>{item?.title ?? item?.question}</span>
												<IconButton size="small" onClick={(e) => item.chat_id && handleMenuOpen(e, item.chat_id)}>
													<MoreVert sx={{ fontSize: "18px" }} />
												</IconButton>
											</Box>
										)
									}
									sx={{
										animation: `${fadeInExpand} 1s ease-out forwards`,
										opacity: 0,
										maxHeight: 0,
										overflow: "hidden",
										transition: "opacity 0.7s ease-in-out, max-height 1s ease-out",
										"& .MuiListItemText-primary": {
											color: "rgba(0, 0, 0, 0.7)",
											fontSize: "13px",
											display: "-webkit-box",
											WebkitLineClamp: "2",
											WebkitBoxOrient: "vertical",
											overflow: "hidden",
											textOverflow: "ellipsis",
										},
									}}
								/>
							</ListItemButton>
						</ListItem>
					))
				)}
			</List>

			<Menu
				anchorEl={anchorEl}
				open={Boolean(anchorEl)}
				onClose={handleMenuClose}
				sx={{
					"& .MuiMenuItem-root": { fontSize: "13px" },
					"& .MuiPaper-root": {
						boxShadow: "none",
						border: "1px solid rgba(0, 0, 0, 0.12)",
					},
				}}
			>
				<MenuItem
					onClick={() => {
						const selectedChat = chatHistory.find((chat) => chat.chat_id === selectedChatId);
						selectedChatId && selectedChat && handleStartRename(selectedChatId);
					}}
				>
					<DriveFileRenameOutline sx={{ fontSize: "16px", mr: 1 }} />
					Rinomina
				</MenuItem>
				<MenuItem
					onClick={() => {
						selectedChatId && handleDeleteClick(selectedChatId);
					}}
				>
					<DeleteOutline sx={{ fontSize: "16px", mr: 1 }} />
					Elimina
				</MenuItem>
			</Menu>
			<Dialog
				open={deleteDialogOpen}
				onClose={handleCancelDelete}
				aria-labelledby="delete-dialog-title"
				aria-describedby="delete-dialog-description"
			>
				<DialogTitle id="delete-dialog-title">Conferma eliminazione</DialogTitle>
				<DialogContent>
					<DialogContentText id="delete-dialog-description">
						Sei sicuro di voler eliminare <b>{chatToDeleteTitle}</b>? Questa azione non può essere annullata.
					</DialogContentText>
				</DialogContent>
				<DialogActions>
					<Button
						variant="contained"
						sx={{ margin: "10px", borderRadius: "10px" }}
						onClick={handleCancelDelete}
						color="primary"
					>
						Annulla
					</Button>
					<Button
						sx={{ margin: "10px", borderRadius: "10px" }}
						onClick={handleConfirmDelete}
						color="error"
						variant="contained"
					>
						Elimina
					</Button>
				</DialogActions>
			</Dialog>
		</div>
	);
}
