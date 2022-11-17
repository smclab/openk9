import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faRightFromBracket } from "@fortawesome/free-solid-svg-icons/faRightFromBracket";
import { faRightToBracket } from "@fortawesome/free-solid-svg-icons/faRightToBracket";
import { useOpenK9Client } from "./client";

type LoginInfoProps = {};
function LoginInfoComponent({}: LoginInfoProps) {
  const client = useOpenK9Client();
  const [authenticated, setAuthenticated] = React.useState(false);
  React.useEffect(() => {
    client.authInit.then(setAuthenticated);
  }, []);
  if (!authenticated) {
    return (
      <button
        onClick={() => {
          client.authenticate();
        }}
        css={css`
          ${buttonStyle};
        `}
      >
        <FontAwesomeIcon icon={faRightToBracket} />
      </button>
    );
  } else {
    return (
      <button
        onClick={() => {
          client.deauthenticate();
        }}
        css={css`
          ${buttonStyle};
        `}
      >
        <FontAwesomeIcon icon={faRightFromBracket} />
      </button>
    );
  }
}

export const LoginInfoComponentMemo = React.memo(LoginInfoComponent);

const buttonStyle = css`
  padding: 4px 8px;
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
