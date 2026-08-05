// CRA blocks imports outside `src/` twice: ModuleScopePlugin rejects them, and babel-loader's
// `src/`-anchored include leaves TypeScript untranspiled. Both are opened for `shared/` below.
const path = require("path");
const ModuleScopePlugin = require("react-dev-utils/ModuleScopePlugin");

const sharedDir = path.resolve(__dirname, "../shared");

module.exports = {
	webpack: {
		configure: (webpackConfig) => {
			webpackConfig.resolve.plugins = (webpackConfig.resolve.plugins || []).filter(
				(plugin) => !(plugin instanceof ModuleScopePlugin),
			);

			const oneOfRule = webpackConfig.module.rules.find((rule) => Array.isArray(rule.oneOf));
			if (!oneOfRule) {
				throw new Error("craco.config.js: regola `oneOf` di CRA non trovata, la config va aggiornata");
			}
			const babelRule = oneOfRule.oneOf.find(
				(rule) => rule.loader && rule.loader.includes("babel-loader") && rule.include,
			);
			if (!babelRule) {
				throw new Error("craco.config.js: babel-loader di CRA non trovato, la config va aggiornata");
			}
			babelRule.include = [babelRule.include, sharedDir].flat();

			return webpackConfig;
		},
	},
	jest: {
		configure: (jestConfig) => {
			// Both `roots` and `testMatch` are needed; with `roots` alone shared tests are silently never run.
			jestConfig.roots = [...(jestConfig.roots || []), sharedDir];
			jestConfig.testMatch = [
				...(jestConfig.testMatch || []),
				`${sharedDir}/**/*.{spec,test}.{js,jsx,ts,tsx}`,
			];
			jestConfig.collectCoverageFrom = [...(jestConfig.collectCoverageFrom || []), "../shared/**/*.{ts,tsx}"];
			return jestConfig;
		},
	},
};
