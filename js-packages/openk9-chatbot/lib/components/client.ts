import React from "react";

export const OpenK9ClientContext = React.createContext<
  ReturnType<typeof OpenK9Client>
>(null as any /* must break app if not provided */);

export default function Client() {
  return null;
}

export function OpenK9Client({
  callbackAuthorization,
}: {
  callbackAuthorization?: () => string | undefined | null;
}) {
  async function authFetch(route: string, init: RequestInit = {}) {
    const authorization = callbackAuthorization && callbackAuthorization();
    // await keycloakInit;
    // if (keycloak.authenticated) {
    // 	await keycloak.updateToken(30);
    // }
    const headers = {
      ...init.headers,
      ...(authorization ? { authorization } : {}),
    };

    return fetch(route, {
      ...init,
      headers,
    });
  }

  return {
    // authInit: keycloakInit,
    // async authenticate() {
    // 	await keycloak.login();
    // },
    // async deauthenticate() {
    // 	await keycloak.logout();
    // },
    // async getUserProfile(): Promise<{ name?: string } | undefined | null> {
    // 	if (!keycloak.authenticated) {
    // 		throw new Error("User is not authenticated");
    // 	}

    // 	try {
    // 		const userInfo = await keycloak.loadUserInfo();
    // 		return userInfo as { name: string };
    // 	} catch (error) {
    // 		throw error;
    // 	}
    // },
    async getInitialMessages(chatId: string): Promise<{
      chat_id: string;
      messages: [
        { question: string; answer: string; chat_sequence_number: string }
      ];
    }> {
      const response = await authFetch(`/api/rag/chat/${chatId}`);
      if (!response.ok) throw new Error("Network response was not ok");
      const data = await response.json();
      return data || [];
    },
    // async getUserInfo(): Promise<getUserInfo> {
    // 	const response = await fetch(`/api/datasource/buckets/current`);
    // 	if (!response.ok) throw new Error("Network response was not ok");
    // 	const data = await response.json();
    // 	return data;
    // },
    async getHistoryChat(
      searchQuery: jsonObjPost
    ): Promise<{ result: ChatHistory[] }> {
      //se refresha pagina browser, la chat riprende dal punto precedente
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
    // async deleteChat(chatId: string) {
    // 	const response = await authFetch(`/api/rag/chat/${chatId}`, {
    // 		method: "DELETE",
    // 		headers: {
    // 			accept: "application/json"
    // 		}
    // 	});

    // 	if (!response.ok) {
    // 		throw new Error("Errore durante l'eliminazione della chat");
    // 	}

    // 	return response;
    // },
    // async renameChat(chatId: string, newTitle: string) {
    // 	const response = await authFetch(`/api/rag/chat/${chatId}`, {
    // 		method: "PATCH",
    // 		headers: {
    // 			accept: "application/json",
    // 			"Content-Type": "application/json",
    // 		},
    // 		body: JSON.stringify({ newTitle })
    // 	});

    // 	if (!response.ok) {
    // 		throw new Error("Errore durante la rinomina della chat");
    // 	}

    // 	return response;
    // }
  };
}

export interface getUserInfo {
  refreshOnSuggestionCategory: boolean;
  refreshOnTab: boolean;
  refreshOnDate: boolean;
  refreshOnQuery: boolean;
  retrieveType: "MATCH" | "KNN" | "HYBRID";
}

export interface ChatHistory {
  chat_id: string | null;
  title: string;
  question: string;
  timestamp: string;
}

export type jsonObjPost = {
  userId: string;
  chatSequenceNumber?: number;
  paginationFrom: number;
  paginationSize: number;
};
