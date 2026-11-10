import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import App from "./App";

let mockAuthEnabled = true;
let mockAuthenticated = false;
const mockGenerateResponse = vi.fn();
const mockAuthenticate = vi.fn();

vi.mock("./auth/oauth2", () => ({
	isAuthEnabled: () => mockAuthEnabled,
	isAuthenticated: () => mockAuthenticated,
}));

vi.mock("./components/authentication", () => ({
	getUserProfile: () => Promise.resolve(null),
}));

vi.mock("./components/client", () => ({
	OpenK9Client: () => ({ authenticate: mockAuthenticate, uploadFiles: vi.fn(), getInitialMessages: vi.fn() }),
}));

vi.mock("./components/useGenerateResponse", () => ({
	default: () => ({
		messages: [],
		generateResponse: mockGenerateResponse,
		cancelAllResponses: vi.fn(),
		isChatting: false,
		isLoading: null,
		isReady: true,
	}),
}));

vi.mock("./components/Sidebar", () => ({ default: () => null }));
vi.mock("./components/MessageCard", () => ({ MessageCard: () => null }));
vi.mock("./components/DocumentPreview", () => ({
	DocumentPreviewProvider: ({ children }: { children: React.ReactNode }) => children,
}));
vi.mock("./components/DatasourceSelect", () => ({ DatasourceSelectMemo: () => null }));
vi.mock("./components/ChatInfoContext", () => ({
	useUser: () => ({ userInfo: { retrieveType: "TEXT" }, loading: false, language: "en" }),
	supportsImageQuery: () => false,
}));
vi.mock("react-i18next", () => ({
	useTranslation: () => ({ t: (key: string) => key }),
}));

async function renderApp() {
	const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
	render(
		<QueryClientProvider client={queryClient}>
			<App />
		</QueryClientProvider>,
	);
	// the chat id comes from a promise and remounts the composer: let it land before typing
	await act(async () => {});
}

async function typeMessage(text: string) {
	fireEvent.change(screen.getByPlaceholderText("write-a-message"), { target: { value: text } });
	// the button is enabled once the chat id is set
	await waitFor(() => expect(screen.getByLabelText("send-message")).toBeEnabled());
}

describe("App — accesso richiesto per usare la chat", () => {
	beforeEach(() => {
		mockAuthEnabled = true;
		mockAuthenticated = false;
		window.OPENK9_REQUIRE_LOGIN = true;
	});

	afterEach(() => {
		delete window.OPENK9_REQUIRE_LOGIN;
	});

	test("con la freccia l'utente non loggato vede l'avviso, il messaggio non parte e resta nell'input", async () => {
		await renderApp();
		await typeMessage("una domanda");

		fireEvent.click(screen.getByLabelText("send-message"));

		expect(screen.getByRole("alert")).toHaveTextContent("login-required");
		expect(mockGenerateResponse).not.toHaveBeenCalled();
		expect(screen.getByPlaceholderText("write-a-message")).toHaveValue("una domanda");
	});

	test("con Invio vale lo stesso: avviso, nessun invio, testo conservato", async () => {
		await renderApp();
		await typeMessage("una domanda");

		const form = screen.getByPlaceholderText("write-a-message").closest("form");
		if (!form) throw new Error("form del composer non trovato");
		fireEvent.submit(form);

		expect(screen.getByRole("alert")).toHaveTextContent("login-required");
		expect(mockGenerateResponse).not.toHaveBeenCalled();
		expect(screen.getByPlaceholderText("write-a-message")).toHaveValue("una domanda");
	});

	test("un prompt suggerito mostra l'avviso e non avvia la conversazione", async () => {
		await renderApp();
		fireEvent.click(screen.getByText("Natural language conversation"));

		expect(screen.getByRole("alert")).toHaveTextContent("login-required");
		expect(mockGenerateResponse).not.toHaveBeenCalled();
	});

	test("Accedi nell'avviso avvia il login", async () => {
		await renderApp();
		await typeMessage("una domanda");
		fireEvent.click(screen.getByLabelText("send-message"));

		fireEvent.click(screen.getByRole("button", { name: "login" }));

		expect(mockAuthenticate).toHaveBeenCalledTimes(1);
	});

	test("l'avviso si chiude e ricompare al tentativo successivo", async () => {
		await renderApp();
		await typeMessage("una domanda");
		fireEvent.click(screen.getByLabelText("send-message"));

		fireEvent.click(screen.getByLabelText("close"));
		expect(screen.queryByRole("alert")).not.toBeInTheDocument();

		fireEvent.click(screen.getByLabelText("send-message"));
		expect(screen.getByRole("alert")).toHaveTextContent("login-required");
	});

	test("dopo l'accesso senza ricaricare la pagina, l'invio parte e l'avviso si chiude", async () => {
		await renderApp();
		await typeMessage("una domanda");
		fireEvent.click(screen.getByLabelText("send-message"));
		expect(screen.getByRole("alert")).toHaveTextContent("login-required");

		mockAuthenticated = true;
		fireEvent.click(screen.getByLabelText("send-message"));

		expect(mockGenerateResponse).toHaveBeenCalledTimes(1);
		expect(screen.queryByRole("alert")).not.toBeInTheDocument();
	});

	test("l'utente loggato chatta come oggi", async () => {
		mockAuthenticated = true;
		await renderApp();
		await typeMessage("una domanda");

		fireEvent.click(screen.getByLabelText("send-message"));

		expect(mockGenerateResponse).toHaveBeenCalledTimes(1);
		expect(mockGenerateResponse.mock.calls[0][0]).toBe("una domanda");
		expect(screen.queryByRole("alert")).not.toBeInTheDocument();
	});

	test.each([
		["il flag e' assente", undefined],
		["il flag e' false", false],
	])("se %s la chat anonima funziona come oggi", async (_, flag) => {
		window.OPENK9_REQUIRE_LOGIN = flag;
		await renderApp();
		await typeMessage("una domanda");

		fireEvent.click(screen.getByLabelText("send-message"));

		expect(mockGenerateResponse).toHaveBeenCalledTimes(1);
		expect(screen.queryByRole("alert")).not.toBeInTheDocument();
	});

	test("senza autenticazione configurata per il tenant il flag non blocca nessuno", async () => {
		mockAuthEnabled = false;
		await renderApp();
		await typeMessage("una domanda");

		fireEvent.click(screen.getByLabelText("send-message"));

		expect(mockGenerateResponse).toHaveBeenCalledTimes(1);
		expect(screen.queryByRole("alert")).not.toBeInTheDocument();
	});
});
