import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faRightFromBracket } from "@fortawesome/free-solid-svg-icons/faRightFromBracket";
import { faRightToBracket } from "@fortawesome/free-solid-svg-icons/faRightToBracket";
import { useOpenK9Client } from "./client";
import { useQuery } from "react-query";
import { faUser } from "@fortawesome/free-solid-svg-icons/faUser";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons/faEnvelope";
import { useClickAway } from "./useClickAway";

type LoginInfoProps = {};
function LoginInfoComponent({}: LoginInfoProps) {
  const client = useOpenK9Client();
  const [authenticated, setAuthenticated] = React.useState(false);
  React.useEffect(() => {
    client.authInit.then(setAuthenticated);
  }, []);
  const userProfileQuery = useQuery(["user-profile"], async () => {
    return await client.getUserProfile();
  });
  const [isOpen, setIsOpen] = React.useState(false);
  const dropdownRef = React.useRef<HTMLDivElement | null>(null);
  useClickAway([dropdownRef], () => {
    setIsOpen(false);
  });
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
        &nbsp;Login
      </button>
    );
  } else {
    return (
      <div
        ref={dropdownRef}
        css={css`
          position: relative;
        `}
      >
        <button
          onClick={() => {
            setIsOpen(!isOpen);
          }}
          css={css`
            ${buttonStyle};
          `}
        >
          <FontAwesomeIcon icon={faUser} />
        </button>
        {isOpen && (
          <div
            css={css`
              position: absolute;
              right: 16px;
              background-color: var(
                --openk9-embeddable-search--primary-background-color
              );
              border: 1px solid var(--openk9-embeddable-search--border-color);
              z-index: 1;
              border-radius: 4px;
              width: 300px;
              overflow: hidden;
            `}
          >
            <div
              css={css`
                padding: 8px 16px 8px 16px;
                font-size: 1.5rem;
              `}
            >
                {userProfileQuery.data?.firstName}&nbsp;
                {userProfileQuery.data?.lastName}
            </div>
            <div
              css={css`
                padding: 0px 16px 8px 16px;
              `}
            >
              <FontAwesomeIcon icon={faUser} />
              &nbsp;{userProfileQuery.data?.username}
            </div>
            <div
              css={css`
                padding: 0px 16px 8px 16px;
              `}
            >
              <FontAwesomeIcon icon={faEnvelope} />
              &nbsp;{userProfileQuery.data?.email}
            </div>
            <div
              css={css`
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 8px 16px 8px 16px;
                background-color: var(
                  --openk9-embeddable-search--secondary-background-color
                );
              `}
            >
              <a href="/admin" target="admin" css={css``}>
                Admin
              </a>
              <button
                onClick={() => {
                  client.deauthenticate();
                }}
                css={css`
                  ${buttonStyle};
                `}
              >
                <FontAwesomeIcon icon={faRightFromBracket} />
                &nbsp;Logout
              </button>
            </div>
          </div>
        )}
      </div>
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
  background: var(--openk9-embeddable-search--primary-background-color);
  appearance: none;
  border: 1px solid var(--openk9-embeddable-search--primary-color);
  border-radius: 4px;
  font-family: inherit;
  font-size: inherit;
`;
