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

import { useState } from "react";
import clsx from "clsx";
import { createUseStyles } from "react-jss";
import ClayForm, { ClayInput } from "@clayui/form";
import { client } from "../components/client";
import { ThemeType } from "../components/theme";
import { Brand } from "../components/Brand";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    height: "100%",
    color: theme.digitalLakeMain,
    backgroundColor: theme.digitalLakeBackground,
  },
  card: {
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    borderRadius: theme.borderRadius,
    padding: theme.spacingUnit * 3,
    width: "100%",
    maxWidth: 400,
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    textAlign: "center",
  },
  logo: {
    marginBottom: "0.5em",
  },
}));

function Login() {
  const classes = useStyles();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(false);

    try {
      const loginInfoResponse = await client.getLoginInfoByUsernamePassword({
        username,
        password,
      });
      if (!loginInfoResponse.ok) throw new Error();
      const loginInfo = loginInfoResponse.response;
      await client.authenticate(loginInfo);
      setLoading(false);
      setError(false);
    } catch (error) {
      setLoading(false);
      setError(true);
    }
  }

  return (
    <div className={classes.root}>
      <div className={classes.card}>
        <Brand className={classes.logo} size={40} />

        <ClayForm onSubmit={handleSubmit} style={{ alignSelf: "stretch" }}>
          <ClayForm.Group className={clsx(error && "has-error")}>
            <label htmlFor="username">Username</label>
            <ClayInput
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </ClayForm.Group>

          <ClayForm.Group className={clsx(error && "has-error")}>
            <label htmlFor="password">Password</label>
            <ClayInput
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </ClayForm.Group>

          {error && (
            <ClayForm.Group className="has-error">
              <ClayForm.FeedbackGroup>
                <ClayForm.FeedbackItem>
                  Invalid username or password
                </ClayForm.FeedbackItem>
              </ClayForm.FeedbackGroup>
            </ClayForm.Group>
          )}

          {loading ? (
            <span className="loading-animation" />
          ) : (
            <button type="submit" className="btn btn-primary">
              Login
            </button>
          )}
        </ClayForm>
      </div>
    </div>
  );
}

export default Login;
