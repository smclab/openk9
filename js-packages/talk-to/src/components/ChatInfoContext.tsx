import React, { createContext, useContext, useState, useEffect } from "react";
import { useQuery } from "react-query";
import { OpenK9Client } from "./client";

interface UserInfo {
	retrieveType: string;
}

interface UserContextType {
	userInfo: UserInfo | null;
	loading: boolean;
	error: any;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const ChatInfoContext: React.FC<{ children: React.ReactNode }> = ({ children }) => {
	const client = OpenK9Client();
	const [userInfo, setUserInfo] = useState<UserInfo | null>(null);

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

	return <UserContext.Provider value={{ userInfo, loading: isLoading, error }}>{children}</UserContext.Provider>;
};

export const useUser = () => {
	const context = useContext(UserContext);
	if (context === undefined) {
		throw new Error("useUser must be used within a UserProvider");
	}
	return context;
};
