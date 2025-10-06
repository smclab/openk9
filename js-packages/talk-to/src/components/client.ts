import React from "react";
import { keycloakInit } from "./authentication";
import { keycloak } from "./keycloak";
import { jsonObjPost } from "./utils";
import { ChatHistory } from "../context/HistoryChatContext";

export const OpenK9ClientContext = React.createContext<ReturnType<typeof OpenK9Client>>(
	null as any /* must break app if not provided */,
);

export default function Client() {
	return null;
}

export function OpenK9Client() {
	async function authFetch(route: string, init: RequestInit = {}) {
		await keycloakInit;
		if (keycloak.authenticated) {
			await keycloak.updateToken(30);
		}
		const headers = {
			...init.headers,
			...(keycloak.token ? { Authorization: `Bearer ${keycloak.token}` } : {}),
		};

		return fetch(route, {
			...init,
			headers,
		});
	}

	return {
		authInit: keycloakInit,
		async authenticate() {
			await keycloak.login();
		},
		async deauthenticate() {
			await keycloak.logout();
		},
		async getUserProfile(): Promise<{ name?: string } | undefined | null> {
			if (!keycloak.authenticated) {
				throw new Error("User is not authenticated");
			}

			try {
				const userInfo = await keycloak.loadUserInfo();
				return userInfo as { name: string };
			} catch (error) {
				throw error;
			}
		},
		async getInitialMessages(
			chatId: string,
		): Promise<{ chat_id: string; messages: [{ question: string; answer: string; chat_sequence_number: string }] }> {
			const response = await authFetch(`/api/rag/chat/${chatId}`);
			if (!response.ok) throw new Error("Network response was not ok");
			const data = await response.json();
			return data || [];
		},
		async getAvailableLanguages(): Promise<language[]> {
			const res = await fetch("/api/datasource/buckets/current/availableLanguage");
			if (!res.ok) throw new Error("Failed to fetch languages");
			return res.json();
		},

		async getDefaultLanguage(): Promise<language | null> {
			const res = await fetch("/api/datasource/buckets/current/defaultLanguage");
			if (!res.ok) throw new Error("Failed to fetch default language");
			return res?.statusText !== "No Content" ? res.json() : { value: "en_US" };
		},
		async getUserInfo(): Promise<getUserInfo> {
			const response = await fetch(`/api/datasource/buckets/current`);
			if (!response.ok) throw new Error("Network response was not ok");
			const data = await response.json();
			return data;
		},
		async getHistoryChat(searchQuery: jsonObjPost): Promise<{ result: ChatHistory[] }> {
			console.log("Calling getHistoryChat with URL:", "/api/rag/user-chats");
			const response = await authFetch(`/api/rag/user-chats`, {
				method: "POST",
				headers: {
					accept: "application/json",
					"Content-Type": "application/json",
				},
				body: JSON.stringify(searchQuery),
			});

			if (!response.ok) {
				console.error("Response not ok:", response.status, response.statusText);
				throw new Error("Network response was not ok");
			}
			const result = await response.json();
			return result;
		},
		async GenerateResponse({
			url,
			searchQuery,
			controller,
		}: {
			url: string;
			searchQuery: {
				searchText: string;
				chatId?: string;
				chatSequenceNumber: number;
				timestamp: string;
				chatHistory?: Array<{
					question: string;
					answer: string;
					title: string;
					sources: Array<any>;
					chat_id: string;
					timestamp: string;
					chat_sequence_number: number;
				}>;
				retrieveFromUploadedDocuments?: boolean;
			};
			controller: AbortController;
		}) {
			const response = await authFetch(url, {
				method: "POST",
				headers: {
					accept: "application/json",
					"Content-Type": "application/json",
				},
				body: JSON.stringify(searchQuery),
				signal: controller.signal,
			});

			return response;
		},
		async deleteChat(chatId: string) {
			const response = await authFetch(`/api/rag/chat/${chatId}`, {
				method: "DELETE",
				headers: {
					accept: "application/json",
				},
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

		async uploadFiles(chatId: string, files: File[]): Promise<{ ok: boolean }> {
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

export interface getUserInfo {
	refreshOnSuggestionCategory: boolean;
	refreshOnTab: boolean;
	refreshOnDate: boolean;
	refreshOnQuery: boolean;
	retrieveType: "MATCH" | "KNN" | "HYBRID";
}

type language = {
	createDate?: string;
	id?: number;
	modifiedDate?: string;
	name?: string;
	value?: string;
};
