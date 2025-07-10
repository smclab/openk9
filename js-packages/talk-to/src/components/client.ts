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
			userId: string,
			chatId: string,
		): Promise<{ chat_id: string; messages: [{ question: string; answer: string; chat_sequence_number: string }] }> {
			const response = await authFetch(`api/rag/getChat/${userId}/${chatId}`);
			if (!response.ok) throw new Error("Network response was not ok");
			const data = await response.json();
			return data || [];
		},
		async getUserInfo(): Promise<getUserInfo> {
			const response = await fetch(`/api/datasource/buckets/current`);
			if (!response.ok) throw new Error("Network response was not ok");
			const data = await response.json();
			return data;
		},
		async getHistoryChat(searchQuery: jsonObjPost): Promise<{ result: ChatHistory[] }> {
			const response = await authFetch(`/api/rag/user_chats`, {
				method: "POST",
				headers: {
					accept: "application/json",
					"Content-Type": "application/json",
				},
				body: JSON.stringify(searchQuery),
			});

			if (!response.ok) {
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
				chatId: string;
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
	};
}

export interface getUserInfo {
	refreshOnSuggestionCategory: boolean;
	refreshOnTab: boolean;
	refreshOnDate: boolean;
	refreshOnQuery: boolean;
	retrieveType: "MATCH" | "KNN" | "HYBRID";
}
