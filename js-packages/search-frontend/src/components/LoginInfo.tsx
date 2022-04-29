import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faKey, faUser } from "@fortawesome/free-solid-svg-icons";
import { useClickAway } from "./useClickAway";
import { LoginState } from "./useLogin";

type LoginInfoProps = {
  loginState: LoginState;
  onLogin(username: string, password: string): void;
  onLogout(): void;
};
function LoginInfoComponent({ loginState, onLogin, onLogout }: LoginInfoProps) {
  const [isOpen, setIsOpen] = React.useState(false);
  const [username, setUsername] = React.useState("");
  const [password, setPassword] = React.useState("");
  const clickAwayRef = React.useRef<HTMLDivElement | null>(null);
  useClickAway([clickAwayRef], () => {
    if (isOpen) setIsOpen(false);
  });
  const canLogin =
    loginState.type === "anonymous" || loginState.type === "login-error";
  React.useEffect(() => {
    console.log("mount");
    return () => {
      console.log("unmount");
    };
  }, []);
  return (
    <div
      ref={clickAwayRef}
      css={css`
        position: relative;
      `}
    >
      {(() => {
        switch (loginState.type) {
          case "login-error":
          case "anonymous":
            return (
              <button
                onClick={() => setIsOpen(!isOpen)}
                css={css`
                  padding: 4px 8px;
                  ${buttonStyle};
                `}
              >
                Login
              </button>
            );
          default:
            return (
              <button
                onClick={() => setIsOpen(!isOpen)}
                css={css`
                  padding: 4px 8px;
                  ${buttonStyle};
                `}
              >
                {loginState.userInfo && loginState.userInfo.username}&nbsp;
                <FontAwesomeIcon icon={faUser} />
              </button>
            );
        }
      })()}
      {isOpen && (
        <div
          css={css`
            position: absolute;
            right: 0px;
            background-color: var(
              --openk9-embeddable-search--primary-background-color
            );
            padding: 8px;
            border-radius: 4px;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            z-index: 1;
            width: 300px;
          `}
        >
          {loginState.type !== "logged-in" && (
            <div>
              <Input
                type="text"
                readOnly={!canLogin}
                label="username"
                icon={<FontAwesomeIcon icon={faUser} />}
                value={username}
                onChange={setUsername}
              />
              <Input
                label="password"
                type="password"
                readOnly={!canLogin}
                icon={<FontAwesomeIcon icon={faKey} />}
                value={password}
                onChange={setPassword}
              />
              {loginState.type === "login-error" && (
                <div
                  css={css`
                    margin-bottom: 8px;
                    text-align: center;
                  `}
                >
                  wrong username or password
                </div>
              )}

              <button
                onClick={() => onLogin(username, password)}
                disabled={!canLogin}
                css={css`
                  padding: 8px;
                  width: 100%;
                  ${buttonStyle};
                `}
              >
                login
              </button>
            </div>
          )}
          {loginState.type === "logged-in" && (
            <div>
              {/* <div>
                {loginState.userInfo.name}
                {loginState.userInfo.given_name}
                {loginState.userInfo.family_name}
                {loginState.userInfo.email}
              </div> */}
              <a
                href="/admin"
                target="_blank"
                css={css`
                  padding: 8px;
                  width: 100%;
                  text-align: center;
                  display: block;
                  box-sizing: border-box;
                `}
              >
                admin
              </a>
              <button
                onClick={() => onLogout()}
                css={css`
                  padding: 8px;
                  width: 100%;
                  ${buttonStyle};
                `}
              >
                logout
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export const LoginInfoComponentMemo = React.memo(LoginInfoComponent);

type InputProps = {
  type: string;
  readOnly: boolean;
  label: string;
  icon: React.ReactNode;
  value: string;
  onChange(value: string): void;
};
function Input({ type, label, value, onChange, icon, readOnly }: InputProps) {
  return (
    <div
      css={css`
        display: flex;
        border-radius: 4px;
        margin-bottom: 8px;
        border: 1px solid var(--openk9-embeddable-search--border-color);
        :focus-within {
          border: 1px solid var(--openk9-embeddable-search--active-color);
        }
      `}
    >
      <div
        css={css`
          padding: 8px 0px 8px 8px;
          display: flex;
          justify-content: center;
          align-items: center;
        `}
      >
        {icon}
      </div>
      <input
        type={type}
        value={value}
        onChange={(event) => onChange(event.currentTarget.value)}
        placeholder={label}
        readOnly={readOnly}
        css={css`
          border: none;
          outline: none;
          padding: 8px;
          color: inherit;
          font-size: inherit;
          font-family: inherit;
        `}
      />
    </div>
  );
}

const buttonStyle = css`
  color: inherit;
  :hover {
    color: var(--openk9-embeddable-search--primary-color);
  }
  background: none;
  appearance: none;
  border: 1px solid var(--openk9-embeddable-search--primary-color);
  border-radius: 4px;
  font-family: inherit;
  font-size: inherit;
`;
