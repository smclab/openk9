import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import Search from "./Search";

jest.mock("./DatasourceSelect", () => ({
	DatasourceSelectMemo: () => null,
}));

let mockRetrieveType: string | undefined = "KNN";

jest.mock("./ChatInfoContext", () => ({
	useUser: () => ({ userInfo: { retrieveType: mockRetrieveType }, loading: false, language: "en" }),
	supportsImageQuery: (retrieveType?: string) => retrieveType === "KNN",
}));

jest.mock("react-i18next", () => ({
	useTranslation: () => ({
		t: (key: string, options?: Record<string, unknown>) =>
			options && "filename" in options ? `${key}:${options.filename}` : key,
	}),
}));

const mockPrepareQueryImageCached = jest.fn();
const mockForgetQueryImage = jest.fn();

jest.mock("../../../shared/image-query/imageQuery", () => ({
	ALLOWED_CONTENT_TYPES: ["image/jpeg", "image/png", "image/webp", "image/avif", "image/gif"],
	MAX_INPUT_BYTES: 25 * 1024 * 1024,
	ImageQueryError: class extends Error {
		code: string;
		constructor(code: string, message: string) {
			super(message);
			this.code = code;
		}
	},
	prepareQueryImageCached: (...args: unknown[]) => mockPrepareQueryImageCached(...args),
	forgetQueryImage: (...args: unknown[]) => mockForgetQueryImage(...args),
}));

const prepared = { data: "BASE64", contentType: "image/jpeg", width: 1568, height: 1176, bytes: 421_000 };

function renderSearch(props: Partial<React.ComponentProps<typeof Search>> = {}) {
	const handleSearch = jest.fn();
	const utils = render(
		<Search
			handleSearch={handleSearch}
			cancelAllResponses={jest.fn()}
			isChatting={false}
			isAuthenticated
			selectedDatasourceIds={[]}
			onSetSelectedDatasourceIds={jest.fn()}
			{...props}
		/>,
	);
	return { ...utils, handleSearch };
}

function imageInput(container: HTMLElement): HTMLInputElement {
	const input = container.querySelector('input[type="file"][accept*="image/"]');
	if (!input) throw new Error("input immagine non trovato");
	return input as HTMLInputElement;
}

function pick(container: HTMLElement, file: File) {
	fireEvent.change(imageInput(container), { target: { files: [file] } });
}

const pngFile = (name = "pompa.png") => new File(["bytes"], name, { type: "image/png" });

describe("Search — icone del composer", () => {
	beforeEach(() => {
		mockPrepareQueryImageCached.mockReset();
		mockForgetQueryImage.mockReset();
		mockRetrieveType = "KNN";
	});

	test("su un bucket KNN e utente autenticato ci sono entrambe le icone", () => {
		renderSearch();
		expect(screen.getByLabelText("attach-file-aria-label")).toBeInTheDocument();
		expect(screen.getByLabelText("search-by-image")).toBeInTheDocument();
	});

	test.each(["HYBRID", "TEXT"])("su un bucket %s l'icona immagine non c'e'", (retrieveType) => {
		mockRetrieveType = retrieveType;
		renderSearch();
		expect(screen.getByLabelText("attach-file-aria-label")).toBeInTheDocument();
		expect(screen.queryByLabelText("search-by-image")).not.toBeInTheDocument();
	});

	test("nella chat anonima resta l'icona immagine e sparisce la graffetta", () => {
		renderSearch({ isAuthenticated: false });
		expect(screen.queryByLabelText("attach-file-aria-label")).not.toBeInTheDocument();
		expect(screen.getByLabelText("search-by-image")).toBeInTheDocument();
	});

	test("se cadono entrambe le condizioni non si rende nessuna icona", () => {
		mockRetrieveType = "TEXT";
		renderSearch({ isAuthenticated: false });
		expect(screen.queryByLabelText("attach-file-aria-label")).not.toBeInTheDocument();
		expect(screen.queryByLabelText("search-by-image")).not.toBeInTheDocument();
	});

	test("un clic sull'icona apre subito l'input, senza menu intermedi", () => {
		const { container } = renderSearch();
		const input = imageInput(container);
		const click = jest.spyOn(input, "click");
		fireEvent.click(screen.getByLabelText("search-by-image"));
		expect(click).toHaveBeenCalled();
		expect(screen.queryByRole("menu")).not.toBeInTheDocument();
	});
});

describe("Search — allegato immagine", () => {
	beforeEach(() => {
		mockPrepareQueryImageCached.mockReset();
		mockForgetQueryImage.mockReset();
		mockRetrieveType = "KNN";
	});

	test("l'invio e' abilitato con la sola immagine, a campo di testo vuoto", async () => {
		mockPrepareQueryImageCached.mockResolvedValue(prepared);
		const { container } = renderSearch();

		expect(screen.getByLabelText("send-message")).toBeDisabled();
		pick(container, pngFile());

		await waitFor(() => expect(screen.getByLabelText("send-message")).toBeEnabled());
	});

	test("durante la preparazione l'invio resta bloccato", async () => {
		let resolvePrepare: (value: unknown) => void = () => undefined;
		mockPrepareQueryImageCached.mockReturnValue(new Promise((resolve) => (resolvePrepare = resolve)));

		const { container } = renderSearch();
		pick(container, pngFile());

		await waitFor(() => expect(screen.getAllByText("preparing-image")).toHaveLength(2));
		expect(screen.getByLabelText("send-message")).toBeDisabled();

		await act(async () => {
			resolvePrepare(prepared);
		});
		expect(screen.getByLabelText("send-message")).toBeEnabled();
	});

	test("le misure dell'immagine preparata compaiono sotto il nome del file", async () => {
		mockPrepareQueryImageCached.mockResolvedValue(prepared);
		const { container } = renderSearch();
		pick(container, pngFile("targa.png"));

		await waitFor(() => expect(screen.getByText(/1568 × 1176/)).toBeInTheDocument());
		expect(screen.getByText("targa.png")).toBeInTheDocument();
	});

	test("un secondo allegato sostituisce il primo invece di aggiungersi", async () => {
		mockPrepareQueryImageCached.mockResolvedValue(prepared);
		const { container } = renderSearch();

		pick(container, pngFile("prima.png"));
		await waitFor(() => expect(screen.getByText("prima.png")).toBeInTheDocument());

		pick(container, pngFile("seconda.png"));
		await waitFor(() => expect(screen.getByText("seconda.png")).toBeInTheDocument());

		expect(screen.queryByText("prima.png")).not.toBeInTheDocument();
		expect(mockForgetQueryImage).toHaveBeenCalled();
	});

	test("un formato non decodificabile mostra l'errore e non prepara nulla", async () => {
		const { container } = renderSearch();
		pick(container, new File(["bytes"], "scansione.tiff", { type: "image/tiff" }));

		await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent("unsupported-file-type"));
		expect(mockPrepareQueryImageCached).not.toHaveBeenCalled();
		expect(screen.getByLabelText("send-message")).toBeDisabled();
	});

	test("un allegato in errore non blocca l'invio di un testo valido", async () => {
		const { container } = renderSearch();
		pick(container, new File(["bytes"], "scansione.tiff", { type: "image/tiff" }));
		await waitFor(() => expect(screen.getByRole("alert")).toBeInTheDocument());

		fireEvent.change(screen.getByPlaceholderText("write-a-message"), { target: { value: "una domanda" } });
		expect(screen.getByLabelText("send-message")).toBeEnabled();
	});

	test("la rimozione svuota l'allegato e riporta il focus al pulsante immagine", async () => {
		mockPrepareQueryImageCached.mockResolvedValue(prepared);
		const { container } = renderSearch();
		pick(container, pngFile());
		await waitFor(() => expect(screen.getByText("pompa.png")).toBeInTheDocument());

		fireEvent.click(screen.getByLabelText("remove-image"));

		await waitFor(() => expect(screen.queryByText("pompa.png")).not.toBeInTheDocument());
		expect(screen.getByLabelText("search-by-image")).toHaveFocus();
	});

	test("la regione live occupa un pixel, non il 100% del contenitore", () => {
		renderSearch();

		const style = window.getComputedStyle(screen.getByRole("status"));
		expect(style.width).toBe("1px");
		expect(style.height).toBe("1px");
		expect(style.width).not.toBe("100%");
		expect(style.height).not.toBe("100%");
		expect(style.position).toBe("absolute");
		expect(style.margin).toBe("-1px");
	});

	test("lo stato dell'allegato viene annunciato agli screen reader", async () => {
		mockPrepareQueryImageCached.mockResolvedValue(prepared);
		const { container } = renderSearch();

		const status = screen.getByRole("status");
		expect(status).toBeInTheDocument();

		pick(container, pngFile("targa.png"));
		await waitFor(() => expect(status).toHaveTextContent("attached-image:targa.png"));
	});
});

describe("Search — invio", () => {
	beforeEach(() => {
		mockPrepareQueryImageCached.mockReset();
		mockForgetQueryImage.mockReset();
		mockRetrieveType = "KNN";
	});

	test("l'allegato pronto viene passato a handleSearch e il composer si svuota", async () => {
		mockPrepareQueryImageCached.mockResolvedValue(prepared);
		const { container, handleSearch } = renderSearch();

		fireEvent.change(screen.getByPlaceholderText("write-a-message"), { target: { value: "che pompa e'?" } });
		pick(container, pngFile("pompa.png"));
		await waitFor(() => expect(screen.getByLabelText("send-message")).toBeEnabled());

		fireEvent.click(screen.getByLabelText("send-message"));

		expect(handleSearch).toHaveBeenCalledTimes(1);
		const [text, flag, image] = handleSearch.mock.calls[0];
		expect(text).toBe("che pompa e'?");
		expect(flag).toBe(false);
		expect(image).toMatchObject({ filename: "pompa.png" });

		await waitFor(() => expect(screen.queryByText("pompa.png")).not.toBeInTheDocument());
		expect(screen.getByPlaceholderText("write-a-message")).toHaveValue("");
	});

	test("dopo l'invio l'immagine non viaggia col messaggio successivo", async () => {
		mockPrepareQueryImageCached.mockResolvedValue(prepared);
		const { container, handleSearch } = renderSearch();

		pick(container, pngFile());
		await waitFor(() => expect(screen.getByLabelText("send-message")).toBeEnabled());
		fireEvent.click(screen.getByLabelText("send-message"));
		await waitFor(() => expect(handleSearch).toHaveBeenCalledTimes(1));

		fireEvent.change(screen.getByPlaceholderText("write-a-message"), { target: { value: "seconda domanda" } });
		fireEvent.click(screen.getByLabelText("send-message"));

		expect(handleSearch).toHaveBeenCalledTimes(2);
		expect(handleSearch.mock.calls[1][2]).toBeUndefined();
	});

	test("l'object URL di un'immagine inviata non viene revocato: lo usa la miniatura", async () => {
		mockPrepareQueryImageCached.mockResolvedValue(prepared);
		const revoke = jest.spyOn(URL, "revokeObjectURL").mockImplementation(() => undefined);

		const { container } = renderSearch();
		pick(container, pngFile());
		await waitFor(() => expect(screen.getByLabelText("send-message")).toBeEnabled());

		fireEvent.click(screen.getByLabelText("send-message"));

		await waitFor(() => expect(screen.queryByText("pompa.png")).not.toBeInTheDocument());
		expect(revoke).not.toHaveBeenCalled();
		revoke.mockRestore();
	});

	test("l'object URL di un'immagine rimossa viene revocato", async () => {
		mockPrepareQueryImageCached.mockResolvedValue(prepared);
		const revoke = jest.spyOn(URL, "revokeObjectURL").mockImplementation(() => undefined);

		const { container } = renderSearch();
		pick(container, pngFile());
		await waitFor(() => expect(screen.getByText("pompa.png")).toBeInTheDocument());

		fireEvent.click(screen.getByLabelText("remove-image"));

		expect(revoke).toHaveBeenCalled();
		revoke.mockRestore();
	});

	test("mentre lo streaming e' in corso il pulsante interrompe e non e' disabilitato", () => {
		const cancelAllResponses = jest.fn();
		renderSearch({ isChatting: true, cancelAllResponses });

		const stop = screen.getByLabelText("stop-generating");
		expect(stop).toBeEnabled();
		fireEvent.click(stop);
		expect(cancelAllResponses).toHaveBeenCalled();
	});

	test("l'invio e' bloccato finche' la chat non e' pronta", () => {
		renderSearch({ canSend: false });
		fireEvent.change(screen.getByPlaceholderText("write-a-message"), { target: { value: "domanda" } });
		expect(screen.getByLabelText("send-message")).toBeDisabled();
	});
});
