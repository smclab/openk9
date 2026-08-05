import { act, fireEvent, render, screen } from "@testing-library/react";
import { MessageCard } from "./MessageCard";
import { Message } from "./useGenerateResponse";

// Virtual mocks: react-markdown 9 is ESM-only and unresolvable under Jest 27.
jest.mock("react-markdown", () => ({ __esModule: true, default: () => null }), { virtual: true });
jest.mock("remark-gfm", () => ({ __esModule: true, default: () => undefined }), { virtual: true });

const mockOpenPreview = jest.fn();

jest.mock("./DocumentPreview", () => ({
	useDocumentPreview: () => ({
		openPreview: mockOpenPreview,
		showArtifactLive: jest.fn(),
		closePreview: jest.fn(),
		activeArtifactId: null,
	}),
}));

jest.mock("./MarkdownRenderer", () => ({
	ArtifactCard: () => null,
	extractDocumentFromAnswer: () => null,
	richMarkdownComponents: {},
}));

jest.mock("react-i18next", () => ({
	useTranslation: () => ({
		t: (key: string, options?: Record<string, unknown>) =>
			options && "filename" in options ? `${key}:${options.filename}` : key,
	}),
}));

function turn(overrides: Partial<Message> = {}): Message {
	return {
		id: "message-1",
		question: "che pompa e'?",
		answer: "una pompa centrifuga",
		status: "END",
		sources: [],
		chat_sequence_number: 1,
		...overrides,
	};
}

function renderTurn(message: Message) {
	return render(<MessageCard message={message} isGenerateMessage={null} />);
}

describe("MessageCard — immagine della domanda", () => {
	beforeEach(() => {
		mockOpenPreview.mockReset();
	});

	test("la miniatura apre il pannello di anteprima con url e filename", () => {
		renderTurn(
			turn({ questionImage: { url: "blob:http://localhost/abc", filename: "pompa.jpg" } }),
		);

		const thumbnail = screen.getByRole("button", { name: "attached-image:pompa.jpg" });
		fireEvent.click(thumbnail);

		expect(mockOpenPreview).toHaveBeenCalledWith({
			url: "blob:http://localhost/abc",
			filename: "pompa.jpg",
		});
	});

	test("la miniatura e' un vero pulsante, quindi raggiungibile e azionabile da tastiera", () => {
		renderTurn(
			turn({ questionImage: { url: "blob:http://localhost/abc", filename: "pompa.jpg" } }),
		);

		const thumbnail = screen.getByRole("button", { name: "attached-image:pompa.jpg" });
		expect(thumbnail.tagName).toBe("BUTTON");
		expect(thumbnail).not.toHaveAttribute("disabled");

		act(() => thumbnail.focus());
		expect(thumbnail).toHaveFocus();
	});

	test("il nome accessibile sta sul pulsante e non sull'immagine, per non annunciarlo due volte", () => {
		const { container } = renderTurn(
			turn({ questionImage: { url: "blob:http://localhost/abc", filename: "targa.png" } }),
		);

		expect(screen.getByRole("button", { name: "attached-image:targa.png" })).toBeInTheDocument();
		const thumb = container.querySelector('img[src="blob:http://localhost/abc"]');
		expect(thumb).toHaveAttribute("alt", "");
	});

	test("il turno con immagine e testo mostra entrambi", () => {
		renderTurn(
			turn({ question: "che pompa e'?", questionImage: { url: "blob:x", filename: "p.jpg" } }),
		);

		expect(screen.getByText("che pompa e'?")).toBeInTheDocument();
		expect(screen.getByRole("button", { name: "attached-image:p.jpg" })).toBeInTheDocument();
		expect(screen.queryByText("question-with-image")).not.toBeInTheDocument();
	});

	test("il turno di sola immagine appena inviato mostra la miniatura e nessun segnaposto", () => {
		renderTurn(turn({ question: "", questionImage: { url: "blob:x", filename: "p.jpg" } }));

		expect(screen.getByRole("button", { name: "attached-image:p.jpg" })).toBeInTheDocument();
		expect(screen.queryByText("question-with-image")).not.toBeInTheDocument();
	});

	test("il turno di sola immagine ricaricato mostra il segnaposto al posto della riga vuota", () => {
		renderTurn(turn({ question: "", questionImage: undefined }));

		const placeholder = screen.getByText("question-with-image");
		expect(placeholder).toBeInTheDocument();
		expect(placeholder).toHaveStyle({ fontStyle: "italic" });
	});

	test("un turno testuale non mostra ne' miniatura ne' segnaposto", () => {
		renderTurn(turn({ question: "domanda solo testo", questionImage: undefined }));

		expect(screen.getByText("domanda solo testo")).toBeInTheDocument();
		expect(screen.queryByText("question-with-image")).not.toBeInTheDocument();
		expect(screen.queryByRole("button", { name: /attached-image/ })).not.toBeInTheDocument();
	});

	test("se la miniatura non carica si mostra il ripiego tradotto invece di un'immagine rotta", () => {
		const { container } = renderTurn(
			turn({ questionImage: { url: "blob:non-caricabile", filename: "p.jpg" } }),
		);

		const thumb = container.querySelector('img[src="blob:non-caricabile"]');
		fireEvent.error(thumb as Element);

		expect(screen.getByText("image-not-available")).toBeInTheDocument();
		expect(container.querySelector('img[src="blob:non-caricabile"]')).not.toBeInTheDocument();
	});
});
