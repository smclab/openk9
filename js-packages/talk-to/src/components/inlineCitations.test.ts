/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {
	claimPanel,
	parseSourcesEvent,
	remarkStripCitationMarkers,
	stripCitationMarkers,
	releasePanel,
	quotesByOrdinal,
	remarkCitationMarkers,
	sourcesByOrdinal,
	splitAnswerOnMarkers,
} from "./inlineCitations";

describe("splitAnswerOnMarkers", () => {
	it("cuts the text around each marker", () => {
		expect(splitAnswerOnMarkers("SMC opera dal 1995 [2]. Fine.")).toEqual([
			{ kind: "text", text: "SMC opera dal 1995 " },
			{ kind: "marker", ordinal: 2 },
			{ kind: "text", text: ". Fine." },
		]);
	});

	it("keeps every occurrence of the same source", () => {
		// Two sentences from one document are two notes, not a duplicate.
		const pieces = splitAnswerOnMarkers("Uno [2]. Due [2].");
		expect(pieces.filter((p) => p.kind === "marker")).toEqual([
			{ kind: "marker", ordinal: 2 },
			{ kind: "marker", ordinal: 2 },
		]);
	});

	it("handles markers that sit next to each other", () => {
		expect(splitAnswerOnMarkers("Una frase [1][3].")).toEqual([
			{ kind: "text", text: "Una frase " },
			{ kind: "marker", ordinal: 1 },
			{ kind: "marker", ordinal: 3 },
			{ kind: "text", text: "." },
		]);
	});

	it("returns text with no markers unchanged", () => {
		expect(splitAnswerOnMarkers("Nessun marcatore.")).toEqual([{ kind: "text", text: "Nessun marcatore." }]);
	});

	it("returns nothing for an empty answer", () => {
		expect(splitAnswerOnMarkers("")).toEqual([]);
	});

	it("ignores brackets that are not a marker", () => {
		// Four digits, letters, or an empty pair are text the model wrote.
		const text = "vedi [1234], [abc] e [] qui";
		expect(splitAnswerOnMarkers(text)).toEqual([{ kind: "text", text }]);
	});

	it("is not affected by a previous call", () => {
		// The pattern is a module-level /g regexp: forgetting lastIndex would
		// make the second call start half way through the string.
		const text = "Primo [1]. Secondo [2].";
		expect(splitAnswerOnMarkers(text)).toEqual(splitAnswerOnMarkers(text));
	});
});

describe("parseSourcesEvent", () => {
	it("reads the sources the event carries", () => {
		expect(parseSourcesEvent([{ ordinal: 1, document_id: "doc-1", title: "Uno", url: "http://uno" }])).toEqual([
			{ ordinal: 1, documentId: "doc-1", title: "Uno", url: "http://uno" },
		]);
	});

	it("drops an entry with no usable ordinal", () => {
		// Nothing in the answer could point at it.
		expect(
			parseSourcesEvent([
				{ title: "senza numero" },
				{ ordinal: "2", title: "numero come stringa" },
				{ ordinal: 1.5, title: "non intero" },
				{ ordinal: 3, title: "buona" },
			]),
		).toEqual([{ ordinal: 3, documentId: undefined, title: "buona", url: undefined }]);
	});

	it("survives anything that is not the expected shape", () => {
		expect(parseSourcesEvent(undefined)).toEqual([]);
		expect(parseSourcesEvent({})).toEqual([]);
		expect(parseSourcesEvent("SOURCES")).toEqual([]);
		expect(parseSourcesEvent([null, 7])).toEqual([]);
	});

	it("leaves out fields that are blank", () => {
		const [source] = parseSourcesEvent([{ ordinal: 1, title: "   ", url: "" }]);
		expect(source).toEqual({
			ordinal: 1,
			documentId: undefined,
			title: undefined,
			url: undefined,
		});
	});
});

describe("sourcesByOrdinal", () => {
	it("indexes the sources by the number the answer uses", () => {
		const byOrdinal = sourcesByOrdinal([
			{ ordinal: 2, title: "Due" },
			{ ordinal: 5, title: "Cinque" },
		]);

		expect(byOrdinal.get(2)?.title).toBe("Due");
		expect(byOrdinal.get(5)?.title).toBe("Cinque");
		expect(byOrdinal.get(1)).toBeUndefined();
	});
});

describe("remarkCitationMarkers", () => {
	const run = (tree: unknown) => {
		remarkCitationMarkers()(tree);
		return tree;
	};
	const text = (value: string) => ({ type: "text", value });
	const marker = (ordinals: string) => ({
		type: "text",
		value: ordinals,
		data: {
			hName: "span",
			hProperties: { "data-citation-ordinals": ordinals },
		},
	});

	it("turns a marker in a paragraph into a span node", () => {
		const tree = {
			type: "root",
			children: [{ type: "paragraph", children: [text("Opera dal 1995 [2].")] }],
		};

		expect(run(tree)).toEqual({
			type: "root",
			children: [
				{
					type: "paragraph",
					children: [text("Opera dal 1995 "), marker("2"), text(".")],
				},
			],
		});
	});

	it("leaves a marker inside code alone", () => {
		// The node there is `code`, not `text`: an array index in a snippet must
		// not become a footnote.
		const tree = {
			type: "root",
			children: [
				{ type: "code", lang: "python", value: "valori[2] = 7" },
				{ type: "paragraph", children: [{ type: "inlineCode", value: "a[1]" }] },
			],
		};

		expect(run(tree)).toEqual({
			type: "root",
			children: [
				{ type: "code", lang: "python", value: "valori[2] = 7" },
				{ type: "paragraph", children: [{ type: "inlineCode", value: "a[1]" }] },
			],
		});
	});

	it("reaches markers nested inside emphasis and list items", () => {
		const tree = {
			type: "root",
			children: [
				{
					type: "list",
					children: [
						{
							type: "listItem",
							children: [{ type: "emphasis", children: [text("corsivo [3]")] }],
						},
					],
				},
			],
		};
		run(tree);

		const emphasis = (tree.children[0] as any).children[0].children[0];
		expect(emphasis.children).toEqual([text("corsivo "), marker("3")]);
	});

	it("leaves a tree with no markers untouched", () => {
		const tree = {
			type: "root",
			children: [{ type: "paragraph", children: [text("niente qui")] }],
		};

		expect(run(tree)).toEqual({
			type: "root",
			children: [{ type: "paragraph", children: [text("niente qui")] }],
		});
	});

	it("survives a malformed tree", () => {
		expect(() => run(null)).not.toThrow();
		expect(() => run({ type: "root" })).not.toThrow();
		expect(() => run({ type: "root", children: [null, 7] })).not.toThrow();
	});

	it("groups markers written next to each other into one citation", () => {
		// "[1][3]" is one sentence with two sources: one chip, not two.
		const tree = {
			type: "root",
			children: [{ type: "paragraph", children: [text("Una frase [1][3].")] }],
		};

		expect(run(tree)).toEqual({
			type: "root",
			children: [
				{
					type: "paragraph",
					children: [text("Una frase "), marker("1,3"), text(".")],
				},
			],
		});
	});

	it("keeps markers apart when text separates them", () => {
		const tree = {
			type: "root",
			children: [{ type: "paragraph", children: [text("Uno [1]. Due [3].")] }],
		};
		run(tree);

		const children = (tree.children[0] as any).children;
		expect(children).toEqual([text("Uno "), marker("1"), text(". Due "), marker("3"), text(".")]);
	});
});

describe("quotesByOrdinal", () => {
	it("reads the first quote of each cited source", () => {
		const quotes = quotesByOrdinal([
			{ ordinal: 1, citations: [{ quote: "primo passaggio" }] },
			{ ordinal: 2, citations: [{ quote: "secondo" }, { quote: "terzo" }] },
		]);

		expect(quotes.get(1)).toBe("primo passaggio");
		expect(quotes.get(2)).toBe("secondo");
	});

	it("leaves out a source with no surviving quote", () => {
		// Normal: about a quarter of the sources have no passage that could be
		// copied word for word, and the panel then shows the title alone.
		const quotes = quotesByOrdinal([{ ordinal: 1, citations: [] }, { ordinal: 2 }]);

		expect(quotes.size).toBe(0);
	});

	it("survives anything that is not the expected shape", () => {
		expect(quotesByOrdinal(undefined).size).toBe(0);
		expect(quotesByOrdinal([null, 7, { ordinal: "1" }]).size).toBe(0);
		expect(quotesByOrdinal([{ ordinal: 1, citations: [{}, 7] }]).size).toBe(0);
	});
});

describe("claimPanel", () => {
	it("closes the panel that was open when another claims", () => {
		// Panels close on a delay, so moving quickly between two chips would
		// otherwise leave both on screen.
		const closeFirst = jest.fn();
		const closeSecond = jest.fn();

		claimPanel(closeFirst);
		claimPanel(closeSecond);

		expect(closeFirst).toHaveBeenCalledTimes(1);
		expect(closeSecond).not.toHaveBeenCalled();
		releasePanel(closeSecond);
	});

	it("does not close the panel that claims twice", () => {
		const close = jest.fn();

		claimPanel(close);
		claimPanel(close);

		expect(close).not.toHaveBeenCalled();
		releasePanel(close);
	});

	it("releasing lets the next claim pass without closing anything", () => {
		const closeFirst = jest.fn();
		const closeSecond = jest.fn();

		claimPanel(closeFirst);
		releasePanel(closeFirst);
		claimPanel(closeSecond);

		expect(closeFirst).toHaveBeenCalledTimes(0);
		releasePanel(closeSecond);
	});

	it("releasing a panel that no longer holds the claim does nothing", () => {
		const closeFirst = jest.fn();
		const closeSecond = jest.fn();

		claimPanel(closeFirst);
		claimPanel(closeSecond);
		releasePanel(closeFirst);
		const closeThird = jest.fn();
		claimPanel(closeThird);

		expect(closeSecond).toHaveBeenCalledTimes(1);
		releasePanel(closeThird);
	});
});

describe("stripCitationMarkers", () => {
	it("toglie il marcatore e lo spazio che lo precede", () => {
		expect(stripCitationMarkers("in marketing [1].")).toBe("in marketing.");
	});

	it("toglie i marcatori raggruppati", () => {
		expect(stripCitationMarkers("una frase [1][3].")).toBe("una frase.");
	});

	it("lascia intatto il testo senza marcatori", () => {
		expect(stripCitationMarkers("niente qui.")).toBe("niente qui.");
	});

	it("non tocca una parentesi che non e' un marcatore", () => {
		expect(stripCitationMarkers("vedi [1234] e [abc]")).toBe("vedi [1234] e [abc]");
	});
});

describe("remarkStripCitationMarkers", () => {
	it("toglie i marcatori dalla prosa", () => {
		const tree = {
			type: "root",
			children: [{ type: "paragraph", children: [{ type: "text", value: "Uno [1]. Due [2]." }] }],
		};
		remarkStripCitationMarkers()(tree);

		expect((tree.children[0] as any).children).toEqual([{ type: "text", value: "Uno. Due." }]);
	});

	it("lascia stare un indice dentro un blocco di codice", () => {
		const tree = {
			type: "root",
			children: [{ type: "code", lang: "python", value: "valori[2] = 7" }],
		};
		remarkStripCitationMarkers()(tree);

		expect(tree.children[0]).toEqual({
			type: "code",
			lang: "python",
			value: "valori[2] = 7",
		});
	});
});
