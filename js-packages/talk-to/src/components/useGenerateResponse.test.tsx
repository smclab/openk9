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

jest.mock("./keycloak", () => ({
	keycloak: { authenticated: false },
}));

jest.mock("react-i18next", () => ({
	useTranslation: () => ({ t: (key: string) => key }),
}));

const mockGenerateResponse = jest.fn();

jest.mock("./client", () => ({
	OpenK9Client: () => ({
		GenerateResponse: (...args: unknown[]) => mockGenerateResponse(...args),
	}),
}));

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
});
