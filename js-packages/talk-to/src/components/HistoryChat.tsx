import React from "react";
import { keyframes, List, ListItem, ListItemButton, ListItemText, Typography } from "@mui/material";
import { OpenK9Client } from "./client";
import { useTranslation } from "react-i18next";
import { jsonObjPost } from "./utils";
import { useChatContext } from "../context/HistoryChatContext";

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

	React.useEffect(() => {
		const fetchData = async () => {
			if (userId) {
				const client = OpenK9Client();
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
			<Typography variant="subtitle1">{t("recents-chat")}</Typography>
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
				{chatHistory?.map((item) => (
					<ListItem sx={{ padding: "4px 0" }} key={item.chat_id}>
						<ListItemButton
							sx={{ borderRadius: "10px", "&:hover": { bgcolor: "rgba(0, 0, 0, 0.1)" }, py: 0.5, zIndex: 10 }}
						>
							<ListItemText
								onClick={() => {
									setChatId({ id: item?.chat_id, isNew: false });
								}}
								primary={item?.title ?? item?.question}
								sx={{
									animation: `${fadeInExpand} 1s ease-out forwards`,
									opacity: 0,
									maxHeight: 0,
									overflow: "hidden",
									transition: "opacity 0.7s ease-in-out, max-height 1s ease-out",
									"& .MuiListItemText-primary": {
										color: "rgba(0, 0, 0, 0.7)",
										fontSize: "14px",
									},
								}}
							/>
						</ListItemButton>
					</ListItem>
				))}
			</List>
		</div>
	);
}
