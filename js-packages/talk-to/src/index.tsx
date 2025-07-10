import React from "react";
import ReactDOM from "react-dom/client";
import { I18nextProvider } from "react-i18next";
import { QueryClient, QueryClientProvider } from "react-query";
import App from "./App";
import { keycloakInit } from "./components/authentication";
import { ChatInfoContext } from "./components/ChatInfoContext";
import './i18n';
import i18n from "./i18n";
import reportWebVitals from "./reportWebVitals";
import { ChatProvider } from "./context/HistoryChatContext";

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);
export const Id = "";
declare global {
	interface Window {
		KEYCLOAK_URL: string;
		KEYCLOAK_REALM: string;
		KEYCLOAK_CLIENT_ID: string;
	}
}

const queryClient = new QueryClient();

keycloakInit.then(() => {
	root.render(
		<React.StrictMode>
			<I18nextProvider i18n={i18n}>
				<QueryClientProvider client={queryClient}>
					<ChatInfoContext>
						<ChatProvider>
							<App />
						</ChatProvider>
					</ChatInfoContext>
				</QueryClientProvider>
			</I18nextProvider>
		</React.StrictMode>,
	);
});

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
