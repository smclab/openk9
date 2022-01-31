import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { myTheme } from "./myTheme";
import { useClickAway } from "./useClickAway";
import { LoginState } from "./useLogin";

type LoginInfoProps = {
  loginState: LoginState;
  onLogin(username: string, password: string): void;
  onLogout(): void;
};
export function LoginInfoComponent({
  loginState,
  onLogin,
  onLogout,
}: LoginInfoProps) {
  const [isOpen, setIsOpen] = React.useState(false);
  const [username, setUsername] = React.useState("");
  const [password, setPassword] = React.useState("");
  const clickAwayRef = React.useRef<HTMLDivElement | null>(null);
  useClickAway([clickAwayRef], () => {
    if (isOpen) setIsOpen(false);
  });
  const canLogin =
    loginState.type === "anonymous" || loginState.type === "login-error";
  return (
    <div
      ref={clickAwayRef}
      css={css`
        position: relative;
      `}
    >
      {(() => {
        switch (loginState.type) {
          case "anonymous":
            return <button onClick={() => setIsOpen(!isOpen)}>login</button>;
          default:
            return (
              <div onClick={() => setIsOpen(!isOpen)}>
                {loginState.userInfo && loginState.userInfo.name}
                <button>
                  <FontAwesomeIcon icon={faUser} />
                </button>
              </div>
            );
        }
      })()}
      {isOpen && (
        <div
          css={css`
            position: absolute;
            right: 0px;
            background-color: ${myTheme.backgroundColor1};
            padding: 8px 16px;
            border-radius: 4px;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            z-index: 1;
          `}
        >
          {canLogin && (
            <div>
              username:{" "}
              <input
                type="text"
                value={username}
                onChange={(event) => setUsername(event.currentTarget.value)}
              />
              password:{" "}
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.currentTarget.value)}
              />
              <button
                onClick={() => onLogin(username, password)}
                disabled={!canLogin}
              >
                login
              </button>
            </div>
          )}
          {loginState.type === "logged-in" && (
            <div>
              <div>
                <button onClick={() => onLogout()}>logout</button>
              </div>
              <div>
                <a href="/admin">admin</a>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
