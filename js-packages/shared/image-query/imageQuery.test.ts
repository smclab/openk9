
import {
	ALLOWED_CONTENT_TYPES,
	MAX_BYTES,
	MAX_EDGE_PX,
	QUALITY_LADDER,
	computeTargetSize,
} from "./imageQuery";

describe("computeTargetSize", () => {
	it("non ingrandisce mai un'immagine piu' piccola del limite", () => {
		expect(computeTargetSize(800, 600, MAX_EDGE_PX)).toEqual({
			width: 800,
			height: 600,
			scaled: false,
		});
	});

	it("lascia intatta un'immagine esattamente al limite", () => {
		expect(computeTargetSize(MAX_EDGE_PX, 1000, MAX_EDGE_PX)).toEqual({
			width: MAX_EDGE_PX,
			height: 1000,
			scaled: false,
		});
	});

	it("riduce sul lato lungo quando l'immagine e' orizzontale", () => {
		const target = computeTargetSize(4032, 3024, MAX_EDGE_PX);
		expect(target.width).toBe(MAX_EDGE_PX);
		expect(target.height).toBe(1176);
		expect(target.scaled).toBe(true);
	});

	it("riduce sul lato lungo anche quando l'immagine e' verticale", () => {
		const target = computeTargetSize(3024, 4032, MAX_EDGE_PX);
		expect(target.height).toBe(MAX_EDGE_PX);
		expect(target.width).toBe(1176);
		expect(target.scaled).toBe(true);
	});

	it("conserva il rapporto d'aspetto entro un pixel", () => {
		const source = { width: 4032, height: 3024 };
		const target = computeTargetSize(source.width, source.height, MAX_EDGE_PX);
		const sourceRatio = source.width / source.height;
		const targetRatio = target.width / target.height;
		expect(Math.abs(sourceRatio - targetRatio)).toBeLessThan(0.01);
	});

	it("non produce mai un lato a zero, nemmeno su strisce estreme", () => {
		const target = computeTargetSize(20000, 1, MAX_EDGE_PX);
		expect(target.width).toBe(MAX_EDGE_PX);
		expect(target.height).toBeGreaterThanOrEqual(1);
	});

	it("tiene il lato lungo entro il limite qualunque sia l'orientamento", () => {
		const cases: Array<[number, number]> = [
			[4032, 3024],
			[3024, 4032],
			[6000, 6000],
			[12000, 900],
			[900, 12000],
		];
		for (const [width, height] of cases) {
			const target = computeTargetSize(width, height, MAX_EDGE_PX);
			expect(Math.max(target.width, target.height)).toBeLessThanOrEqual(MAX_EDGE_PX);
		}
	});
});

describe("costanti di contratto", () => {
	it("il tetto e' i 2 MiB sui byte decodificati attesi dal searcher", () => {
		expect(MAX_BYTES).toBe(2 * 1024 * 1024);
	});

	it("il lato lungo e' 1568 px", () => {
		expect(MAX_EDGE_PX).toBe(1568);
	});

	it("la scala di qualita' e' decrescente e dentro i limiti di canvas.toBlob", () => {
		expect(QUALITY_LADDER.length).toBeGreaterThan(0);
		for (const quality of QUALITY_LADDER) {
			expect(quality).toBeGreaterThan(0);
			expect(quality).toBeLessThanOrEqual(1);
		}
		const descending = [...QUALITY_LADDER].sort((a, b) => b - a);
		expect(QUALITY_LADDER).toEqual(descending);
	});

	it("l'allowlist e' esplicita e non contiene formati che il browser non decodifica", () => {
		expect(ALLOWED_CONTENT_TYPES).toContain("image/jpeg");
		expect(ALLOWED_CONTENT_TYPES).toContain("image/png");
		expect(ALLOWED_CONTENT_TYPES).not.toContain("image/heic");
		expect(ALLOWED_CONTENT_TYPES).not.toContain("image/heif");
	});
});
