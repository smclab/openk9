import React from "react";
import ReactDOM from "react-dom/client";
import { I18nextProvider } from "react-i18next";
import { QueryClient, QueryClientProvider } from "react-query";
import App from "./App";
import { authInit } from "./components/authentication";
import { ChatInfoContext } from "./components/ChatInfoContext";
import "./i18n";
import i18n from "./i18n";
import { ChatProvider } from "./context/HistoryChatContext";

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);
export const Id = "";
declare global {
	interface Window {
		tenant: string;
		OPENK9_NUMBER_OF_SOURCES?: number;
	}
}

const queryClient = new QueryClient();

authInit.then(() => {
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
