import { fileNameFromUrl, isImageUrl } from "./imageUrl";

describe("isImageUrl", () => {
	test("riconosce le estensioni immagine, anche con query string o frammento", () => {
		expect(isImageUrl("https://example.test/foto.jpg")).toBe(true);
		expect(isImageUrl("https://example.test/foto.JPEG")).toBe(true);
		expect(isImageUrl("https://example.test/foto.png?v=2")).toBe(true);
		expect(isImageUrl("https://example.test/foto.webp#anchor")).toBe(true);
	});

	test("non riconosce documenti e URL senza estensione", () => {
		expect(isImageUrl("https://example.test/relazione.pdf")).toBe(false);
		expect(isImageUrl("https://example.test/api/download/12345")).toBe(false);
		expect(isImageUrl(undefined)).toBe(false);
		expect(isImageUrl("")).toBe(false);
	});

	test("un blob: URL non ha estensione, il filename invece si", () => {
		expect(isImageUrl("blob:http://localhost/9f0c-4a1b-8e2d")).toBe(false);
		expect(isImageUrl("pompa.jpg")).toBe(true);
		expect(isImageUrl("pompa.png")).toBe(true);
	});
});

describe("fileNameFromUrl", () => {
	test("prende l'ultimo segmento del percorso", () => {
		expect(fileNameFromUrl("https://example.test/docs/relazione.pdf")).toBe("relazione.pdf");
	});

	test("decodifica i nomi con caratteri percent-encoded", () => {
		expect(fileNameFromUrl("https://example.test/docs/schema%20tecnico.png")).toBe("schema tecnico.png");
	});

	test("senza segmenti nel percorso ripiega sull'host", () => {
		expect(fileNameFromUrl("https://example.test/")).toBe("example.test");
	});

	test("una stringa qualunque risolve come percorso relativo", () => {
		expect(fileNameFromUrl("relazione tecnica.pdf", "fallback")).toBe("relazione tecnica.pdf");
	});
});
