import React from "react";
import {
  getUserInfo,
  doLoginRefresh,
  doLogin,
  LoginInfo,
  UserInfo,
} from "@openk9/rest-api";

export type LoginState =
  | { type: "anonymous"; loginInfo?: undefined; userInfo?: undefined }
  | { type: "logging-in"; loginInfo?: undefined; userInfo?: undefined }
  | { type: "login-error"; loginInfo?: undefined; userInfo?: undefined }
  | { type: "logged-in"; loginInfo: LoginInfo; userInfo: UserInfo };

export function useLoginInfo() {
  const [state, setState] = React.useState<LoginState>(() => {
    const persisted = localStorage.getItem("pq-admin-v1");
    if (persisted) {
      try {
        const parsed = JSON.parse(persisted);
        if (parsed.state && parsed.state.loginInfo && parsed.state.userInfo) {
          return {
            type: "logged-in",
            userInfo: parsed.state.userInfo,
            loginInfo: parsed.state.loginInfo,
          };
        }
      } catch (error) {}
    }
    return { type: "anonymous" };
  });
  React.useEffect(() => {
    localStorage.setItem(
      "pq-admin-v1",
      JSON.stringify({
        state: {
          sidebarOpen: true,
          loginInfo: state.loginInfo,
          userInfo: state.userInfo,
        },
        version: 0,
      }),
    );
  }, [state.loginInfo, state.userInfo]);
  const login = React.useCallback(
    async (username: string, password: string) => {
      if (state.type === "anonymous" || state.type === "login-error") {
        setState({ type: "logging-in" });
        try {
          const loginInfoResponse = await doLogin({ username, password });
          if (!loginInfoResponse.ok) throw new Error();
          const userInfoResponse = await getUserInfo(
            loginInfoResponse.response,
          );
          if (!userInfoResponse.ok) throw new Error();
          setState({
            type: "logged-in",
            loginInfo: loginInfoResponse.response,
            userInfo: userInfoResponse.response,
          });
        } catch (error) {
          setState({ type: "anonymous" });
        }
      }
    },
    [state.type],
  );
  const logout = React.useCallback(() => {
    if (state.type === "logged-in") {
      setState({ type: "anonymous" });
      // TODO call logout endpoint
    }
  }, [state.type]);
  React.useEffect(() => {
    if (state.type === "logged-in") {
      const timeoutId = setTimeout(async () => {
        const loginRefreshResponse = await doLoginRefresh({
          refreshToken: state.loginInfo.refresh_token,
        });
        if (!loginRefreshResponse.ok) throw new Error();
        const userInfoResponse = await getUserInfo(
          loginRefreshResponse.response,
        );
        if (!userInfoResponse.ok) throw new Error();
        setState((s) =>
          s === state
            ? {
                type: "logged-in",
                loginInfo: loginRefreshResponse.response,
                userInfo: userInfoResponse.response,
              }
            : s,
        );
      }, state.loginInfo.expires_in * 700);
      return () => {
        clearTimeout(timeoutId);
      };
    }
  }, [state]);
  return { state, login, logout };
}
