import { useState, useEffect, useCallback } from "react";
import { v4 as uuidv4 } from "uuid";
import { useUser } from "./ChatInfoContext";
import { OpenK9Client } from "./client";
import React from "react";
import { useChatContext } from "../context/HistoryChatContext";
import { useTranslation } from "react-i18next";
import { keycloak } from "./keycloak";

type Source = { source?: string; title?: string; url?: string };

export interface Message {
	id?: string;
	question: string;
	answer: string;
	sendTime?: string | null;
	status?: "END" | "CHUNK" | "ERROR";
	sources?: Source[];
	chat_sequence_number: number;
	timestamp?: string;
}

const useGenerateResponse = ({ initialMessages }: { initialMessages: Message[] }) => {
	const [messages, setMessages] = useState<Message[]>(initialMessages);
	const [abortControllers, setAbortControllers] = useState<Map<string, AbortController>>(new Map());
	const [isChatting, setIsChatting] = useState<boolean>(false);
	const [isLoading, setIsLoading] = useState<{ isLoading: boolean; id: string } | null>(null);
	const { userInfo, loading } = useUser();
	const client = React.useMemo(() => OpenK9Client(), []);
	const { language } = useUser();
	const { dispatch } = useChatContext();
	const { t } = useTranslation();
	useEffect(() => {
		setMessages(initialMessages);
		setIsChatting(false);
	}, [initialMessages]);

	const generateResponse = useCallback(
		async (query: string, chatId: string, retrieveFromUploadedDocuments?: boolean) => {
			const id = uuidv4();
			setIsLoading({ id, isLoading: true });

			if (loading || !userInfo) {
				return;
			}
			const timestamp = "" + Date.now();
			const nonLoggedUserId = `anonymous_${uuidv4()}_${timestamp}`;

			const chatHistory = messages.map((msg) => ({
				question: msg.question,
				answer: msg.answer,
				title: "",
				sources: msg.sources || [],
				chat_id: keycloak.authenticated ? chatId : nonLoggedUserId,
				timestamp: msg.timestamp || "",
				chat_sequence_number: msg.chat_sequence_number,
			}));

			setMessages((prevMessages) => {
				const chat_sequence_number = (prevMessages[prevMessages.length - 1]?.chat_sequence_number || 0) + 1;
				const newMessage: Message = {
					id,
					question: query,
					answer: "",
					sendTime: new Date().toISOString(),
					status: "CHUNK",
					sources: [],
					chat_sequence_number,
					timestamp,
				};

				return [...prevMessages, newMessage];
			});

			setIsChatting(true);

			const controller = new AbortController();
			setAbortControllers((prev) => new Map(prev).set(id, controller));

			const url = "/api/rag/chat";

			const searchQuery = keycloak.authenticated
				? {
						searchText: query,
						chatId,
						chatSequenceNumber: messages[messages.length - 1]?.chat_sequence_number + 1 || 1,
						timestamp,
						language,
						retrieveFromUploadedDocuments: !!retrieveFromUploadedDocuments,
				  }
				: {
						searchText: query,
						chatSequenceNumber: messages[messages.length - 1]?.chat_sequence_number + 1 || 1,
						timestamp,
						chatHistory,
						language,
				  };

			try {
				const response = await client.GenerateResponse({ controller, searchQuery: searchQuery, url });
if (response.ok) {
					const reader = response.body?.getReader();
					const decoder = new TextDecoder("utf-8");
					let done = false;
					let buffer = "";

					while (!done && reader) {
						const { value, done: readerDone } = await reader.read();
						done = readerDone;
						buffer += decoder.decode(value, { stream: true });
						let boundaryIndex;

						while ((boundaryIndex = buffer.indexOf("\n")) !== -1) {
							const chunkStr = buffer.slice(0, boundaryIndex);
							buffer = buffer.slice(boundaryIndex + 1);
							if (chunkStr.trim().startsWith("data: ")) {
								const dataStr = chunkStr.trim().slice(6);
								try {
									const data = JSON.parse(dataStr);
									switch (data.type) {
										case "CHUNK":
											setMessages((prev) =>
												prev.map((msg) =>
													msg.id === id
														? {
																...msg,
																answer: msg.answer + data.chunk,
														  }
														: msg,
												),
											);
											break;

										case "DOCUMENT":
											setMessages((prev) =>
												prev.map((msg) =>
													msg.id === id
														? {
																...msg,
																sources: [...(msg?.sources ?? []), data.chunk],
														  }
														: msg,
												),
											);
											break;

										case "START":
											setIsLoading(null);
											break;

										case "END":
											setMessages((prev) =>
												prev.map((msg) =>
													msg.id === id
														? {
																...msg,
																status: "END",
														  }
														: msg,
												),
											);
											setIsChatting(false);
											break;

										case "TITLE":
											dispatch({
												type: "ADD_CHAT",
												payload: { chat_id: chatId, question: query, timestamp: timestamp, title: data.chunk },
											});
											break;
										case "ERROR":
											setMessages((prev) =>
												prev.map((msg) =>
													msg.id === id
														? {
																...msg,
																status: "ERROR",
																answer: data.message || "Si è verificato un errore.",
														  }
														: msg,
												),
											);
											setIsChatting(false);
											setIsLoading(null);
											break;
										default:
											console.warn("Tipo di chunk non riconosciuto:", data.type);
											break;
									}
								} catch (e) {
									console.error("Errore nel parsing del JSON", e);
								}
							}
						}
					}
				} else {
					const errorData = await response.json();
					setMessages((prev) =>
						prev.map((msg) =>
							msg.id === id
								? {
										...msg,
										status: "ERROR",
										answer: errorData?.detail || t("error"),
								  }
								: msg,
						),
					);
					setIsChatting(false);
					setIsLoading(null);
				}
			} catch (error) {
				console.error("Errore durante la richiesta", error);
				setIsChatting(false);
			}

			setAbortControllers((prev) => {
				const updated = new Map(prev);
				updated.delete(id);
				return updated;
			});
		},
		[loading, userInfo, messages, client, language],
	);

	const cancelResponse = (id: string) => {
		const controller = abortControllers.get(id);
		if (controller) {
			controller.abort();
			setIsLoading(null);
			setMessages((prev) =>
				prev.map((msg) =>
					msg.id === id
						? {
								...msg,
								status: "END",
								answer: "La risposta è stata annullata",
						  }
						: msg,
				),
			);
			setAbortControllers((prev) => {
				const updated = new Map(prev);
				updated.delete(id);
				return updated;
			});
			setIsChatting(abortControllers.size > 0);
		} else {
			console.warn(`No AbortController found for id: ${id}`);
		}
	};

	const cancelAllResponses = () => {
		abortControllers.forEach((controller, id) => {
			controller.abort();
		});
		setMessages((prev) =>
			prev.map((msg) =>
				msg.status === "CHUNK"
					? {
							...msg,
							status: "END",
							answer: msg.answer + "... La risposta è stata annullata",
					  }
					: msg,
			),
		);
		setAbortControllers(new Map());
		setIsChatting(false);
		setIsLoading(null);
	};

	return {
		messages,
		generateResponse,
		cancelResponse,
		cancelAllResponses,
		isChatting,
		isLoading,
	};
};

export default useGenerateResponse;
