import { TextDecoder, TextEncoder } from "util";
import { renderHook, act, waitFor } from "@testing-library/react";
import useGenerateResponse from "./useGenerateResponse";

(global as any).TextEncoder = (global as any).TextEncoder || TextEncoder;
(global as any).TextDecoder = (global as any).TextDecoder || TextDecoder;

let mockUuidCounter = 0;
jest.mock("uuid", () => ({
	v4: () => `test-uuid-${++mockUuidCounter}`,
}));

jest.mock("./ChatInfoContext", () => ({
	useUser: () => ({
		userInfo: { name: "test-user" },
		loading: false,
		language: "en",
	}),
}));

jest.mock("../context/HistoryChatContext", () => ({
	useChatContext: () => ({ dispatch: jest.fn() }),
}));

// A getter, not a fixed value: the authenticated body has a different shape.
jest.mock("./keycloak", () => ({
	keycloak: {
		get authenticated() {
			return mockAuthenticated;
		},
	},
}));
let mockAuthenticated = false;

jest.mock("react-i18next", () => ({
	useTranslation: () => ({ t: (key: string) => key }),
}));

const mockPrepareQueryImageCached = jest.fn();

jest.mock("../../../shared/image-query/imageQuery", () => ({
	prepareQueryImageCached: (...args: unknown[]) => mockPrepareQueryImageCached(...args),
}));

const mockGenerateResponse = jest.fn();

jest.mock("./client", () => ({
	OpenK9Client: () => ({
		GenerateResponse: (...args: unknown[]) => mockGenerateResponse(...args),
	}),
}));

function createAttachment(filename = "pompa.png") {
	return {
		attachmentId: "attachment-1",
		file: new File(["binary"], filename, { type: "image/png" }),
		previewUrl: "blob:http://localhost/preview-1",
		filename,
	};
}

function sentSearchQuery(): Record<string, unknown> {
	return mockGenerateResponse.mock.calls[0][0].searchQuery;
}

function createSSEResponse(events: Array<Record<string, unknown>>) {
	const encoder = new TextEncoder();
	const lines = events.map((event) => `data: ${JSON.stringify(event)}\n`);
	let index = 0;
	return {
		ok: true,
		body: {
			getReader: () => ({
				read: async () => {
					if (index < lines.length) {
						return { value: encoder.encode(lines[index++]), done: false };
					}
					return { value: undefined, done: true };
				},
			}),
		},
	};
}

const initialMessages: never[] = [];

describe("useGenerateResponse", () => {
	beforeEach(() => {
		mockGenerateResponse.mockReset();
		mockPrepareQueryImageCached.mockReset();
		mockAuthenticated = false;
	});

	test("blocked query shows the guardrail block message", async () => {
		mockGenerateResponse.mockResolvedValue(
			createSSEResponse([
				{ type: "GUARDRAIL", chunk: "Guardrail violation - (CATEGORY)" },
				{ type: "END", chunk: "" },
			]),
		);

		const { result } = renderHook(() => useGenerateResponse({ initialMessages }));

		await act(async () => {
			await result.current.generateResponse("blocked query", "chat-1");
		});

		await waitFor(() => {
			const lastMessage = result.current.messages[result.current.messages.length - 1];
			expect(lastMessage.answer).toBe("guardrail-violation");
			expect(lastMessage.status).toBe("END");
		});
	});

	test("loading and chatting stop on guardrail block even without START event", async () => {
		mockGenerateResponse.mockResolvedValue(
			createSSEResponse([
				{ type: "GUARDRAIL", chunk: "Guardrail violation - (CATEGORY)" },
				{ type: "END", chunk: "" },
			]),
		);

		const { result } = renderHook(() => useGenerateResponse({ initialMessages }));

		await act(async () => {
			await result.current.generateResponse("blocked query", "chat-1");
		});

		await waitFor(() => {
			expect(result.current.isLoading).toBeNull();
			expect(result.current.isChatting).toBe(false);
		});
	});

	test("regular stream still accumulates chunks and completes", async () => {
		mockGenerateResponse.mockResolvedValue(
			createSSEResponse([
				{ type: "START", chunk: "" },
				{ type: "CHUNK", chunk: "Hello " },
				{ type: "CHUNK", chunk: "world" },
				{ type: "END", chunk: "" },
			]),
		);

		const { result } = renderHook(() => useGenerateResponse({ initialMessages }));

		await act(async () => {
			await result.current.generateResponse("normal query", "chat-1");
		});

		await waitFor(() => {
			const lastMessage = result.current.messages[result.current.messages.length - 1];
			expect(lastMessage.answer).toBe("Hello world");
			expect(lastMessage.status).toBe("END");
			expect(result.current.isLoading).toBeNull();
			expect(result.current.isChatting).toBe(false);
		});
	});

	describe("immagine come input della domanda", () => {
		const prepared = {
			data: "BASE64BYTES",
			contentType: "image/jpeg" as const,
			width: 1568,
			height: 1176,
			bytes: 421_000,
		};

		function okStream() {
			return createSSEResponse([
				{ type: "START", chunk: "" },
				{ type: "CHUNK", chunk: "risposta" },
				{ type: "END", chunk: "" },
			]);
		}

		test("il corpo porta media { data, contentType: image/jpeg }", async () => {
			mockPrepareQueryImageCached.mockResolvedValue(prepared);
			mockGenerateResponse.mockResolvedValue(okStream());

			const { result } = renderHook(() => useGenerateResponse({ initialMessages }));
			await act(async () => {
				await result.current.generateResponse("che pompa e'?", "chat-1", undefined, undefined, createAttachment());
			});

			expect(sentSearchQuery().media).toEqual({ data: "BASE64BYTES", contentType: "image/jpeg" });
			expect(String((sentSearchQuery().media as { data: string }).data)).not.toContain("data:");
		});

		test("il messaggio con la sola immagine invia searchText vuoto, non assente", async () => {
			mockPrepareQueryImageCached.mockResolvedValue(prepared);
			mockGenerateResponse.mockResolvedValue(okStream());

			const { result } = renderHook(() => useGenerateResponse({ initialMessages }));
			await act(async () => {
				await result.current.generateResponse("", "chat-1", undefined, undefined, createAttachment());
			});

			const body = sentSearchQuery();
			expect(body).toHaveProperty("searchText");
			expect(body.searchText).toBe("");
			expect(body.searchText).not.toBeNull();
		});

		test("un'immagine allegata esclude il retrieval dai documenti caricati", async () => {
			mockPrepareQueryImageCached.mockResolvedValue(prepared);
			mockGenerateResponse.mockResolvedValue(okStream());
			mockAuthenticated = true;

			const { result } = renderHook(() => useGenerateResponse({ initialMessages }));
			await act(async () => {
				await result.current.generateResponse("domanda", "chat-1", true, undefined, createAttachment());
			});

			expect(sentSearchQuery().retrieveFromUploadedDocuments).toBe(false);
		});

		test("il flag resta invariato quando non c'e' un'immagine", async () => {
			mockGenerateResponse.mockResolvedValue(okStream());
			mockAuthenticated = true;

			const { result } = renderHook(() => useGenerateResponse({ initialMessages }));
			await act(async () => {
				await result.current.generateResponse("domanda", "chat-1", true);
			});

			expect(sentSearchQuery().retrieveFromUploadedDocuments).toBe(true);
			expect(sentSearchQuery()).not.toHaveProperty("media");
		});

		test("media viaggia anche sul corpo anonimo, che porta chatHistory e non chatId", async () => {
			mockPrepareQueryImageCached.mockResolvedValue(prepared);
			mockGenerateResponse.mockResolvedValue(okStream());
			mockAuthenticated = false;

			const { result } = renderHook(() => useGenerateResponse({ initialMessages }));
			await act(async () => {
				await result.current.generateResponse("domanda", "chat-1", undefined, undefined, createAttachment());
			});

			const body = sentSearchQuery();
			expect(body.media).toEqual({ data: "BASE64BYTES", contentType: "image/jpeg" });
			expect(body).toHaveProperty("chatHistory");
			expect(body).not.toHaveProperty("chatId");
		});

		test("l'immagine non entra in chatHistory", async () => {
			mockPrepareQueryImageCached.mockResolvedValue(prepared);
			mockGenerateResponse.mockResolvedValue(okStream());

			const previousTurn = [
				{ question: "prima domanda", answer: "prima risposta", chat_sequence_number: 1, timestamp: "1" },
			];
			const { result } = renderHook(() => useGenerateResponse({ initialMessages: previousTurn }));
			await act(async () => {
				await result.current.generateResponse("seconda", "chat-1", undefined, undefined, createAttachment());
			});

			const history = sentSearchQuery().chatHistory as Array<Record<string, unknown>>;
			expect(history).toHaveLength(1);
			for (const turn of history) {
				expect(turn).not.toHaveProperty("media");
				expect(turn).not.toHaveProperty("questionImage");
			}
		});

		test("il turno porta l'anteprima per la miniatura, senza i byte", async () => {
			mockPrepareQueryImageCached.mockResolvedValue(prepared);
			mockGenerateResponse.mockResolvedValue(okStream());

			const { result } = renderHook(() => useGenerateResponse({ initialMessages }));
			await act(async () => {
				await result.current.generateResponse("domanda", "chat-1", undefined, undefined, createAttachment("targa.png"));
			});

			const lastMessage = result.current.messages[result.current.messages.length - 1];
			expect(lastMessage.questionImage).toEqual({
				url: "blob:http://localhost/preview-1",
				filename: "targa.png",
			});
			expect(JSON.stringify(lastMessage)).not.toContain("BASE64BYTES");
		});

		test("una preparazione fallita marca il turno ERROR invece di lasciare i caricamenti accesi", async () => {
			mockPrepareQueryImageCached.mockRejectedValue(new Error("decode-failed"));
			mockGenerateResponse.mockResolvedValue(okStream());

			const { result } = renderHook(() => useGenerateResponse({ initialMessages }));
			await act(async () => {
				await result.current.generateResponse("domanda", "chat-1", undefined, undefined, createAttachment());
			});

			await waitFor(() => {
				const lastMessage = result.current.messages[result.current.messages.length - 1];
				expect(lastMessage.status).toBe("ERROR");
				expect(lastMessage.answer).toBe("error");
				expect(result.current.isLoading).toBeNull();
				expect(result.current.isChatting).toBe(false);
			});
			expect(mockGenerateResponse).not.toHaveBeenCalled();
		});
	});
});
