import React from "react";

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

export function withAutorization(
  loginInfo: LoginInfo | null,
  headers: HeadersInit,
) {
  if (loginInfo) {
    return { ...headers, Authorization: `Bearer ${loginInfo.access_token}` };
  } else {
    return headers;
  }
}

export type LoginInfo = {
  access_token: string;
  expires_in: number;
  refresh_expires_in: number;
  refresh_token: string;
  token_type: string;
  "not-before-policy": number;
  session_state: string;
  scope: string;
};

const AUTH_TIMEOUT = 6000;

async function doLogin(
  payload: {
    username: string;
    password: string;
  },
  timeout = AUTH_TIMEOUT,
): Promise<{ ok: true; response: LoginInfo } | { ok: false; response: any }> {
  async function innerLogin() {
    const request = await fetch(`/api/searcher/v1/auth/login`, {
      method: "POST",
      body: JSON.stringify(payload),
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });
    return [request, await request.json()] as const;
  }

  try {
    const [request, response] = await promiseTimeoutReject(
      innerLogin(),
      timeout,
    );
    return { ok: request.ok, response };
  } catch (err) {
    return { ok: false, response: err };
  }
}

function promiseTimeoutReject<T>(promise: Promise<T>, ms: number) {
  return Promise.race([promise, abortTimeout<T>(ms)]);
}

function abortTimeout<T>(ms: number) {
  return new Promise<T>((resolve, reject) =>
    setTimeout(() => reject("timeout"), ms),
  );
}

async function doLogout(
  payload: {
    accessToken: string;
    refreshToken: string;
  },
  loginInfo: LoginInfo,
): Promise<{ ok: boolean; response: any }> {
  try {
    const request = await fetch(`/api/searcher/v1/auth/logout`, {
      method: "POST",
      body: JSON.stringify(payload),
      headers: withAutorization(loginInfo, {
        "Content-Type": "application/json",
        Accept: "text/plain",
      }),
    });
    const response = await request.text();
    return { ok: request.ok, response };
  } catch (err) {
    return { ok: false, response: err };
  }
}

async function doLoginRefresh(
  payload: {
    refreshToken: string;
  },
  timeout = AUTH_TIMEOUT,
): Promise<{ ok: true; response: LoginInfo } | { ok: false; response: any }> {
  async function innerRefresh() {
    const request = await fetch(`/api/searcher/v1/auth/refresh`, {
      method: "POST",
      body: JSON.stringify(payload),
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });
    return [request, await request.json()] as const;
  }

  try {
    const [request, response] = await promiseTimeoutReject(
      innerRefresh(),
      timeout,
    );
    return { ok: request.ok, response };
  } catch (err) {
    return { ok: false, response: err };
  }
}

async function getUserInfo(
  loginInfo: LoginInfo,
): Promise<{ ok: true; response: UserInfo } | { ok: false; response: any }> {
  try {
    const request = await fetch(`/api/searcher/v1/auth/user-info`, {
      method: "POST",
      body: "",
      headers: withAutorization(loginInfo, {
        "Content-Type": "text/plain",
        Accept: "application/json",
      }),
    });
    const response: UserInfo = await request.json();
    return { ok: request.ok, response };
  } catch (err) {
    return { ok: false, response: err };
  }
}

type UserInfo = {
  exp: number;
  iat: number;
  jti: string;
  iss: string;
  aud: string;
  sub: string;
  typ: string;
  azp: string;
  session_state: string;
  name: string;
  given_name: string;
  family_name: string;
  preferred_username: string;
  email: string;
  email_verified: boolean;
  acr: string;
  realm_access: { [key: string]: string[] };
  resource_access: { [key: string]: { [key: string]: string[] } };
  scope: string;
  client_id: string;
  username: string;
  active: boolean;
};
