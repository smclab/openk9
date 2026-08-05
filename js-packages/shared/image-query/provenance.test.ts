import { existsSync, readFileSync, readdirSync, statSync } from "fs";
import { join } from "path";

const JS_PACKAGES = join(__dirname, "..", "..");

function appDirectories(): string[] {
	return readdirSync(JS_PACKAGES).filter((entry) => {
		if (entry === "shared" || entry === "node_modules") return false;
		const path = join(JS_PACKAGES, entry);
		return statSync(path).isDirectory() && existsSync(join(path, "package.json"));
	});
}

function sourceFiles(dir: string): string[] {
	if (!existsSync(dir)) return [];
	const found: string[] = [];
	for (const entry of readdirSync(dir)) {
		if (entry === "node_modules") continue;
		const path = join(dir, entry);
		if (statSync(path).isDirectory()) {
			found.push(...sourceFiles(path));
		} else if (/\.(ts|tsx)$/.test(entry)) {
			found.push(path);
		}
	}
	return found;
}

describe("provenienza del modulo image-query", () => {
	test("nessun frontend tiene una propria copia di imageQuery", () => {
		const copies: string[] = [];
		for (const app of appDirectories()) {
			for (const file of sourceFiles(join(JS_PACKAGES, app, "src"))) {
				if (/[/\\]imageQuery\.tsx?$/.test(file)) {
					copies.push(file);
				}
			}
		}
		expect(copies).toEqual([]);
	});

	test("chi lo consuma lo importa da shared, non da un percorso locale", () => {
		const importsOfTheModule: Array<{ file: string; specifier: string }> = [];

		for (const app of appDirectories()) {
			for (const file of sourceFiles(join(JS_PACKAGES, app, "src"))) {
				const source = readFileSync(file, "utf8");
				for (const match of source.matchAll(/from\s+"([^"]*imageQuery)"/g)) {
					importsOfTheModule.push({ file, specifier: match[1] });
				}
			}
		}

		for (const { file, specifier } of importsOfTheModule) {
			expect(specifier).toMatch(/shared\/image-query\/imageQuery$/);
			expect(file).not.toContain("imageQuery.ts");
		}
	});

	test("il modulo condiviso non ha dipendenze esterne", () => {
		const source = readFileSync(join(__dirname, "imageQuery.ts"), "utf8");
		const imports = [...source.matchAll(/^\s*import\s.*?from\s+"([^"]+)"/gm)].map((m) => m[1]);
		expect(imports).toEqual([]);
	});

	test("il modulo condiviso non contiene JSX", () => {
		const files = sourceFiles(__dirname);
		expect(files.filter((f) => f.endsWith(".tsx"))).toEqual([]);
	});
});
