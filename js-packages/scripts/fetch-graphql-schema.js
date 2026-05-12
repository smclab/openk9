#!/usr/bin/env node
const { spawnSync } = require('node:child_process');
const path = require('node:path');

const mavenModule = process.argv[2];
if (!mavenModule) {
	console.error('Usage: fetch-graphql-schema.js <maven-module> (e.g. app/datasource)');
	process.exit(2);
}

const coreDir = path.resolve(__dirname, '..', '..', 'core');
const isWindows = process.platform === 'win32';
const wrapper = path.join(coreDir, isWindows ? 'mvnw.cmd' : 'mvnw');

const args = [
	'-pl', mavenModule,
	'-am',
	'-P', '!validate,!format,graphql-schema',
	'-q',
	'process-classes',
];

// shell: true is required on Windows so .cmd batch files are dispatched correctly
const result = spawnSync(wrapper, args, {
	cwd: coreDir,
	stdio: 'inherit',
	shell: isWindows,
});

if (result.error) {
	console.error(result.error);
	process.exit(1);
}
process.exit(result.status ?? 1);
