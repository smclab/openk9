/**
 * The marker the answer writes, as the reader sees it: a link to the source
 * it points at, next to the sentence it belongs to.
 *
 * The chips are rendered directly rather than through react-markdown: that
 * package is ESM only, and this app is still built by Create React App,
 * whose Jest does not transform `node_modules`. What the remark plugin puts
 * in the tree - the `data-citation-ordinals` span this renders by hand - is
 * covered in `inlineCitations.test.ts`, code blocks included.
 */
import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
// user-event is at v13 here: no setup(), and the helpers are synchronous.
import userEvent from "@testing-library/user-event";
import React from "react";

import { type CitedSource } from "./inlineCitations";
import { markdownComponentsWithCitations } from "./CitationChip";

const SOURCES: CitedSource[] = [
	{ ordinal: 1, title: "Primo documento", url: "https://smc.it/uno" },
	{ ordinal: 2, title: "Secondo documento", url: "https://smc.it/due" },
];

/**
 * Render one chip per group of ordinals, the way the remark plugin hands
 * them over: "1,2" is one citation resting on two sources.
 */
function renderChips(groups: string[], sources = SOURCES, quotes?: Map<number, string>) {
	const components = markdownComponentsWithCitations(sources, quotes);
	const Citation = components.span as React.ComponentType<Record<string, unknown>>;
	return render(
		<p>
			{groups.map((ordinals) => (
				<Citation key={ordinals} data-citation-ordinals={ordinals}>
					{ordinals}
				</Citation>
			))}
		</p>,
	);
}

describe("il chip che cita la fonte", () => {
	it("mostra il titolo della fonte, non il numero", () => {
		// Su un corpus a dominio unico il nome del sito non distinguerebbe
		// nulla: e' il titolo del documento a dire quale fonte e'.
		renderChips(["2"]);

		expect(screen.getByText("Secondo documento")).toBeTruthy();
		expect(screen.queryByText("[2]")).toBeNull();
	});

	it("accorcia un titolo troppo lungo per stare in riga", () => {
		renderChips(
			["1"],
			[
				{
					ordinal: 1,
					title: "Un titolo molto lungo che non sta dentro una riga di prosa",
					url: "https://smc.it/uno",
				},
			],
		);

		const chip = screen.getByText(/^Un titolo molto lungo/);
		expect(chip.textContent?.endsWith("…")).toBe(true);
		expect((chip.textContent ?? "").length).toBeLessThanOrEqual(28);
	});

	it("riunisce in un chip solo le fonti della stessa frase", () => {
		// "[1][2]" e' una citazione con due fonti, non due citazioni.
		renderChips(["1,2"]);

		expect(screen.getByText("Primo documento")).toBeTruthy();
		expect(screen.getByText("+1")).toBeTruthy();
		expect(screen.queryByText("Secondo documento")).toBeNull();
	});

	it("apre il pannello col dominio e il link al passaggio del mouse", async () => {
		renderChips(["1"]);

		expect(screen.queryByRole("group")).toBeNull();
		userEvent.hover(screen.getByText("Primo documento"));

		const panel = screen.getByRole("group");
		expect(within(panel).getByText("smc.it")).toBeTruthy();
		const link = within(panel).getByRole("link", { name: "Primo documento" });
		expect(link).toHaveAttribute("href", "https://smc.it/uno");
		expect(link).toHaveAttribute("target", "_blank");
		expect(link).toHaveAttribute("rel", "noreferrer");
	});

	it("mostra nel pannello la citazione testuale quando c'e'", async () => {
		renderChips(["1"], SOURCES, new Map([[1, "un passaggio copiato"]]));

		userEvent.hover(screen.getByText("Primo documento"));

		expect(within(screen.getByRole("group")).getByText(/un passaggio copiato/)).toBeTruthy();
	});

	it("mostra il pannello anche senza citazione testuale", async () => {
		// Circa un quarto delle fonti non ne ha una: resta il titolo.
		renderChips(["1"]);

		userEvent.hover(screen.getByText("Primo documento"));

		expect(within(screen.getByRole("group")).getByRole("link")).toBeTruthy();
	});

	it("elenca tutte le fonti raggruppate nel pannello", async () => {
		renderChips(["1,2"]);

		userEvent.hover(screen.getByText("Primo documento"));

		const panel = screen.getByRole("group");
		expect(within(panel).getByRole("link", { name: "Primo documento" })).toBeTruthy();
		expect(within(panel).getByRole("link", { name: "Secondo documento" })).toBeTruthy();
	});

	it("il pannello resta aperto quando il puntatore ci si sposta sopra", async () => {
		// Il difetto: fra chip e pannello c'era un margine che non
		// apparteneva a nessuno dei due, e attraversarlo chiudeva tutto proprio
		// mentre si andava a cliccare il link.
		renderChips(["1"]);

		userEvent.hover(screen.getByText("Primo documento"));
		const panel = screen.getByRole("group");
		userEvent.hover(within(panel).getByRole("link"));

		expect(screen.getByRole("group")).toBeTruthy();
	});

	it("il pannello si chiude lasciando sia chip sia pannello", async () => {
		renderChips(["1"]);

		userEvent.hover(screen.getByText("Primo documento"));
		expect(screen.getByRole("group")).toBeTruthy();

		userEvent.unhover(screen.getByRole("group"));
		await waitFor(() => expect(screen.queryByRole("group")).toBeNull());
	});

	it("ne resta aperto uno solo passando in fretta da un chip all'altro", async () => {
		// La chiusura e' ritardata per lasciare raggiungere il link: senza un
		// registro, il chip che si lascia e' ancora nel suo margine di
		// tolleranza quando la successiva apre, e si vedono due pannelli.
		renderChips(["1", "2"]);

		userEvent.hover(screen.getByText("Primo documento"));
		userEvent.hover(screen.getByText("Secondo documento"));

		expect(screen.getAllByRole("group")).toHaveLength(1);
	});

	it("lascia il numero visibile se nessuna fonte lo risolve", () => {
		// Il server non sopprime il marcatore proprio perche' degradi in
		// qualcosa di leggibile: resta testo, non un chip con un pannello vuoto.
		renderChips(["9"]);

		expect(screen.getByText("[9]")).toBeTruthy();
		expect(screen.queryByRole("button")).toBeNull();
	});

	it("tiene nel chip solo le fonti risolte e lascia il numero delle altre", () => {
		renderChips(["1,9"]);

		expect(screen.getByRole("button", { name: "Primo documento" })).toBeTruthy();
		expect(screen.queryByText("+1")).toBeNull();
		expect(screen.getByText("[9]")).toBeTruthy();
	});

	it("da tastiera si arriva al link del pannello senza che si chiuda", async () => {
		// Il blur del chip e il focus del link si annullano: prima il blur
		// avviava la chiusura e dopo 200 ms il pannello spariva sotto il focus.
		renderChips(["1"]);

		const chip = screen.getByRole("button", { name: "Primo documento" });
		fireEvent.focus(chip);
		expect(chip.getAttribute("aria-expanded")).toBe("true");
		const link = within(screen.getByRole("group")).getByRole("link");
		fireEvent.blur(chip);
		fireEvent.focus(link);
		await act(() => new Promise((resolve) => setTimeout(resolve, 300)));

		expect(screen.getByRole("group")).toBeTruthy();
	});

	it("Esc chiude il pannello e riporta il focus sul chip", () => {
		renderChips(["1"]);

		const chip = screen.getByRole("button", { name: "Primo documento" });
		act(() => chip.focus());
		const link = within(screen.getByRole("group")).getByRole("link");
		act(() => link.focus());
		fireEvent.keyDown(link, { key: "Escape" });

		expect(screen.queryByRole("group")).toBeNull();
		expect(chip).toHaveFocus();
		expect(chip.getAttribute("aria-expanded")).toBe("false");
	});
});
