import React from "react";
import { authInit } from "./authentication";
import { getAccessToken, isAuthenticated, loadUserProfile, login, logout } from "../auth/oauth2";
import { jsonObjPost } from "./utils";
import { ChatHistory } from "../context/HistoryChatContext";
import { resolveTenantUrl } from "../config/tenant";

export const OpenK9ClientContext = React.createContext<ReturnType<typeof OpenK9Client>>(null as any);

export function OpenK9Client() {
	async function authFetch(route: string, init: RequestInit = {}) {
		await authInit;

		const token = await getAccessToken();

		const headers = {
			...init.headers,
			...(token ? { Authorization: `Bearer ${token}` } : {}),
		};

		return fetch(resolveTenantUrl(route), { ...init, headers });
	}
	return {
		authInit,

		async authenticate() {
			await authInit;
			return login();
		},

		async deauthenticate() {
			await authInit;
			return logout();
		},

		async getUserProfile() {
			await authInit;
			if (!isAuthenticated()) throw new Error("User is not authenticated");
			return loadUserProfile();
		},

		async getInitialMessages(chatId: string) {
			const response = await authFetch(`/api/rag/chat/${chatId}`);
			if (!response.ok) throw new Error("Network response was not ok");
			return response.json();
		},

		async getAvailableLanguages() {
			const res = await authFetch("/api/datasource/buckets/current/availableLanguage");
			if (!res.ok) throw new Error("Failed to fetch languages");
			return res.json();
		},

		async getDefaultLanguage() {
			const res = await authFetch("/api/datasource/buckets/current/defaultLanguage");
			if (!res.ok) throw new Error("Failed to fetch default language");
			return res?.statusText !== "No Content" ? res.json() : { value: "en_US" };
		},

		async getDatasources(): Promise<{ id: number; name: string }[]> {
			const res = await authFetch("/api/datasource/buckets/current/datasources");
			if (!res.ok) throw new Error("Failed to fetch datasources");
			return res.json();
		},

		async getUserInfo() {
			const response = await authFetch(`/api/datasource/buckets/current`);
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
