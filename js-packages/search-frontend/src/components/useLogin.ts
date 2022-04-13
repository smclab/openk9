import React from "react";
import {
  LoginInfo,
  UserInfo,
  ClientAuthenticationState,
} from "@openk9/rest-api";
import { client, clientAuthenticationChangeListeners } from "./client";

export type LoginState =
  | { type: "anonymous"; loginInfo?: undefined; userInfo?: undefined }
  | { type: "logging-in"; loginInfo?: undefined; userInfo?: undefined }
  | { type: "login-error"; loginInfo?: undefined; userInfo?: undefined }
  | {
      type: "logged-in";
      loginInfo: LoginInfo;
      userInfo: UserInfo;
    };

export function useLoginInfo() {
  const [state, setState] = React.useState<LoginState>({ type: "anonymous" });

  React.useEffect(() => {
    const persisted = load();
    if (persisted) {
      client.authenticate(persisted.loginInfo);
    }
  }, []);

  React.useEffect(() => {
    if (state.type === "logged-in") {
      const { loginInfo, userInfo } = state;
      store({ loginInfo, userInfo });
    }
  }, [state]);

  React.useEffect(() => {
    const onAuthenticationStateChange = (state: ClientAuthenticationState) => {
      if (state === null) {
        setState({ type: "anonymous" });
      } else {
        setState({
          type: "logged-in",
          loginInfo: state.loginInfo,
          userInfo: state.userInfo,
        });
      }
    };
    clientAuthenticationChangeListeners.add(onAuthenticationStateChange);
    return () => {
      clientAuthenticationChangeListeners.add(onAuthenticationStateChange);
    };
  }, []);

  const login = React.useCallback(
    async (username: string, password: string) => {
      if (state.type === "anonymous" || state.type === "login-error") {
        setState({ type: "logging-in" });
        try {
          const loginInfoResponse = await client.getLoginInfoByUsernamePassword(
            {
              username,
              password,
            },
          );
          if (!loginInfoResponse.ok) throw new Error();
          const loginInfo = loginInfoResponse.response;
          await client.authenticate(loginInfo);
        } catch (error) {
          setState({ type: "login-error" });
        }
      }
    },
    [state.type],
  );

  const logout = React.useCallback(() => {
    if (state.type === "logged-in") {
      client.deauthenticate();
    }
  }, [state.type]);

  return { state, login, logout };
}

function store(state: ClientAuthenticationState) {
  localStorage.setItem(
    "pq-admin-v1",
    JSON.stringify({
      state: {
        sidebarOpen: true,
        loginInfo: state?.loginInfo,
        userInfo: state?.userInfo,
      },
      version: 0,
    }),
  );
}

function load(): ClientAuthenticationState {
  const persisted = localStorage.getItem("pq-admin-v1");
  if (persisted) {
    try {
      const parsed = JSON.parse(persisted);
      if (parsed.state && parsed.state.loginInfo && parsed.state.userInfo) {
        return {
          userInfo: parsed.state.userInfo,
          loginInfo: parsed.state.loginInfo,
        };
      }
    } catch (error) {}
  }
  return null;
}
