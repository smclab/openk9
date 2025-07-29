import { createContext, ReactNode, useContext, useReducer } from "react";

interface Message {
	id: number | string;
	text: string;
	sender: "user" | "bot";
}

interface Chat {
	id: number | string;
	title: string;
	messages: Message[];
}

export interface ChatHistory {
	chat_id: string | null;
	title: string;
	question: string;
	timestamp: string;
}

interface ChatState {
	chatHistory: ChatHistory[];
	chats: Chat[];
	activeChat: number | string | null;
	isLoading: boolean;
}

type ChatAction =
	| { type: "SET_CHATS"; payload: ChatHistory[] }
	| { type: "ADD_CHAT"; payload: ChatHistory }
	| { type: "DELETE_CHAT"; payload: string }
	| { type: "SET_ACTIVE_CHAT"; payload: number | string | null }
	| { type: "ADD_MESSAGE"; payload: { chatId: number | string; message: Omit<Message, "id"> } }
	| { type: "SET_LOADING"; payload: Chat[] }
	| { type: "UPDATE_CHAT_TITLE"; payload: { chatId: string; newTitle: string } };

const initialState: ChatState = {
	chatHistory: [],
	chats: [],
	activeChat: null,
	isLoading: false,
};

function chatReducer(state: ChatState, action: ChatAction): ChatState {
	switch (action.type) {
		case "SET_CHATS":
			return { ...state, chatHistory: action.payload };
		case "ADD_CHAT":
			return { ...state, chatHistory: [action.payload, ...state.chatHistory] };
		case "DELETE_CHAT":
			return {
				...state,
				chatHistory: state.chatHistory.filter((chat) => chat.chat_id !== action.payload),
			};
		case "UPDATE_CHAT_TITLE":
			return {
				...state,
				chatHistory: state.chatHistory.map((chat) =>
					chat.chat_id === action.payload.chatId ? { ...chat, title: action.payload.newTitle } : chat,
				),
			};
		default:
			return state;
	}
}

const ChatContext = createContext<
	| {
			state: ChatState;
			dispatch: React.Dispatch<ChatAction>;
	  }
	| undefined
>(undefined);

export function ChatProvider({ children }: { children: ReactNode }) {
	const [state, dispatch] = useReducer(chatReducer, initialState);

	return <ChatContext.Provider value={{ state, dispatch }}>{children}</ChatContext.Provider>;
}

export function useChatContext() {
	const context = useContext(ChatContext);
	if (context === undefined) {
		throw new Error("useChatContext must be used with a ChatProvider");
	}
	return context;
}
