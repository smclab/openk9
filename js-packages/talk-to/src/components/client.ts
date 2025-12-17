import React from "react";
import { keycloakInit } from "./authentication";
import { kc } from "../auth/kc";
import { jsonObjPost } from "./utils";
import { ChatHistory } from "../context/HistoryChatContext";
import { resolveTenantUrl } from "../config/tenant";

export const OpenK9ClientContext = React.createContext<ReturnType<typeof OpenK9Client>>(null as any);

export function OpenK9Client() {
	async function authFetch(route: string, init: RequestInit = {}) {
		await keycloakInit;

		if (kc.authenticated) {
			await kc.updateToken(30);
		}

		const headers = {
			...init.headers,
			...(kc.token ? { Authorization: `Bearer ${kc.token}` } : {}),
		};

		return fetch(resolveTenantUrl(route), { ...init, headers });
	}
	console.log(resolveTenantUrl);
	return {
		authInit: keycloakInit,

		async authenticate() {
			await kc.login();
		},

		async deauthenticate() {
			await kc.logout();
		},

		async getUserProfile(): Promise<{ name?: string } | undefined | null> {
			if (!kc.authenticated) {
				throw new Error("User is not authenticated");
			}
			return kc.loadUserInfo();
		},

		async getInitialMessages(chatId: string) {
			const response = await authFetch(`/api/rag/chat/${chatId}`);
			if (!response.ok) throw new Error("Network response was not ok");
			return response.json();
		},

		async getAvailableLanguages() {
			const res = await fetch(resolveTenantUrl("/api/datasource/buckets/current/availableLanguage"));
			if (!res.ok) throw new Error("Failed to fetch languages");
			return res.json();
		},

		async getDefaultLanguage() {
			const res = await fetch(resolveTenantUrl("/api/datasource/buckets/current/defaultLanguage"));
			if (!res.ok) throw new Error("Failed to fetch default language");
			return res?.statusText !== "No Content" ? res.json() : { value: "en_US" };
		},

		async getUserInfo() {
			const response = await fetch(resolveTenantUrl(`/api/datasource/buckets/current`));
			if (!response.ok) throw new Error("Network response was not ok");
			return response.json();
		},

		async getHistoryChat(searchQuery: jsonObjPost): Promise<{ result: ChatHistory[] }> {
			const response = await authFetch(`/api/rag/user-chats`, {
				method: "POST",
				headers: {
					accept: "application/json",
					"Content-Type": "application/json",
				},
				body: JSON.stringify(searchQuery),
			});

			if (!response.ok) throw new Error("Network response was not ok");
			return response.json();
		},

		async GenerateResponse({ url, searchQuery, controller }: any) {
			return authFetch(url, {
				method: "POST",
				headers: {
					accept: "application/json",
					"Content-Type": "application/json",
				},
				body: JSON.stringify(searchQuery),
				signal: controller.signal,
			});
		},

		async deleteChat(chatId: string) {
			const response = await authFetch(`/api/rag/chat/${chatId}`, {
				method: "DELETE",
				headers: { accept: "application/json" },
			});

			if (!response.ok) {
				throw new Error("Errore durante l'eliminazione della chat");
			}

			return response;
		},

		async renameChat(chatId: string, newTitle: string) {
			const response = await authFetch(`/api/rag/chat/${chatId}`, {
				method: "PATCH",
				headers: {
					accept: "application/json",
					"Content-Type": "application/json",
				},
				body: JSON.stringify({ newTitle }),
			});

			if (!response.ok) {
				throw new Error("Errore durante la rinomina della chat");
			}

			return response;
		},

		async uploadFiles(chatId: string, files: File[]) {
			const formData = new FormData();
			files.forEach((f) => formData.append("files", f));

			const response = await authFetch(`/api/rag/upload-files?chat_id=${encodeURIComponent(chatId)}`, {
				method: "POST",
				body: formData,
			});

			if (!response.ok) {
				const err = await response.text().catch(() => "");
				throw new Error(err || "Upload error");
			}

			return { ok: true };
		},
	};
}
