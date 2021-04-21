/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import { promiseTimeoutReject } from "../utilities";
import { apiBaseUrl, authFetch } from "./common";

const AUTH_TIMEOUT = 6000;

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

export type UserInfo = {
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

export async function doLogin(
  payload: {
    username: string;
    password: string;
  },
  timeout = AUTH_TIMEOUT,
): Promise<{ ok: true; response: LoginInfo } | { ok: false; response: any }> {
  async function innerLogin() {
    const request = await fetch(`${apiBaseUrl}/auth/login`, {
      method: "POST",
      body: JSON.stringify(payload),
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

export async function doLogout(
  payload: {
    accessToken: string;
    refreshToken: string;
  },
  loginInfo: LoginInfo,
): Promise<{ ok: boolean; response: any }> {
  try {
    const request = await authFetch(`${apiBaseUrl}/auth/logout`, loginInfo, {
      method: "POST",
      body: JSON.stringify(payload),
    });
    const response = await request.text();
    return { ok: request.ok, response };
  } catch (err) {
    return { ok: false, response: err };
  }
}

export async function doLoginRefresh(
  payload: {
    refreshToken: string;
  },
  timeout = AUTH_TIMEOUT,
): Promise<{ ok: true; response: LoginInfo } | { ok: false; response: any }> {
  async function innerRefresh() {
    const request = await fetch(`${apiBaseUrl}/auth/refresh`, {
      method: "POST",
      body: JSON.stringify(payload),
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

export async function getUserInfo(
  loginInfo: LoginInfo,
): Promise<{ ok: true; response: UserInfo } | { ok: false; response: any }> {
  try {
    const request = await authFetch(`${apiBaseUrl}/auth/user-info`, loginInfo, {
      method: "POST",
      body: "",
    });
    const response: UserInfo = await request.json();
    return { ok: request.ok, response };
  } catch (err) {
    return { ok: false, response: err };
  }
}
