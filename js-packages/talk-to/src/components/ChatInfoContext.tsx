import React, { createContext, useContext, useState, useEffect } from "react";
import { useQuery } from "react-query";
import { OpenK9Client } from "./client";

/** Mirrors the backend `Bucket.RetrieveType` enum returned by `GET /api/datasource/buckets/current`. */
export type RetrieveType = "KNN" | "HYBRID" | "TEXT";

interface UserInfo {
	retrieveType: RetrieveType;
}

/** Only KNN: the backend rejects an image query on HYBRID and TEXT with 400. */
export function supportsImageQuery(retrieveType?: RetrieveType): boolean {
	return retrieveType === "KNN";
}

interface UserContextType {
	userInfo: UserInfo | null;
	loading: boolean;
	error: any;
	language?: string;
	setLanguage?: (language: string) => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const ChatInfoContext: React.FC<{ children: React.ReactNode }> = ({ children }) => {
	const client = OpenK9Client();
	const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
	const [language, setLanguage] = useState<string>("en_US");

	const { data, isLoading, error } = useQuery(
		"user-info",
		async () => {
			return client.getUserInfo();
		},
		{
			staleTime: Infinity,
			cacheTime: Infinity,
		},
	);

	useEffect(() => {
		if (data) {
			setUserInfo(data);
		}
	}, [data]);

	return (
		<UserContext.Provider value={{ userInfo, loading: isLoading, error, language, setLanguage }}>
			{children}
		</UserContext.Provider>
	);
};

export const useUser = () => {
	const context = useContext(UserContext);
	if (context === undefined) {
		throw new Error("useUser must be used within a UserProvider");
	}
	return context;
};
