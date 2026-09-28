import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import viteCompression from "vite-plugin-compression";

export default defineConfig({
	base: "/chat/",
	plugins: [
		{
			name: "redirect-root-to-chat",
			configureServer(server) {
				server.middlewares.use((req, res, next) => {
					if (req.url === "/" || req.url === "") {
						res.writeHead(302, { Location: "/chat/" });
						res.end();
						return;
					}
					next();
				});
			},
		},
		react(),
		viteCompression({
			algorithm: "gzip",
			ext: ".gz",
			threshold: 10240,
		}),
		viteCompression({
			algorithm: "brotliCompress",
			ext: ".br",
			threshold: 10240,
		}),
	],
	build: {
		outDir: "build",
		// nginx.conf serves the bundle from /chat/static/
		assetsDir: "static",
		rollupOptions: {
			output: {
				manualChunks: (id) => {
					if (!id.includes("node_modules")) return;
					const pkg = (name: string) => id.includes(`node_modules/${name}/`);

					// core React runtime
					if (pkg("react") || pkg("react-dom") || pkg("react-is") || pkg("scheduler")) return "react-core";
				},
			},
		},
		chunkSizeWarningLimit: 2000,
	},
	server: {
		port: 3000,
		open: "/chat/",
		proxy: {
			"/api": {
				target: "https://k9-frontend.openk9.io",
				changeOrigin: true,
			},
		},
	},
});
