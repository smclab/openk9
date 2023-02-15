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
import { LoginLogo } from "./LogoLogin";

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
          css={css`
            ${buttonStyle};
            background: ${"var(--openk9-embeddable-search--secondary-active-color)"};
            cursor: pointer;
          `}
          style={{
            background:
              "var(--openk9-embeddable-search--secondary-active-color)",
          }}
          onClick={() => {
            setIsOpen(!isOpen);
          }}
        >
          <LoginLogo />
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
              width: 245px;
              height: 95px;
              overflow: hidden;
            `}
          >
            <div
              css={css`
                padding: 3px 16px 0px 16px;
                border-bottom: 1px solid
                  var(--openk9-embeddable-search--border-color);
              `}
            >
              <FontAwesomeIcon icon={faUser} />
              &nbsp;
              <span
                css={css`
                  font-family: "Helvetica";
                  font-style: normal;
                  font-weight: 400;
                  font-size: 14px;
                  line-height: 44px;
                  margin-left: 10px;
                  /* or 314% */

                  align-items: center;

                  color: #2e2f39;
                `}
              >
                {userProfileQuery.data?.preferred_username}
              </span>
            </div>
            <div
              css={css`
                padding: 3px 16px 0px 16px;
                border-bottom: 1px solid
                  var(--openk9-embeddable-search--border-color);
                cursor: pointer;
              `}
              onClick={() => {
                client.deauthenticate();
              }}
            >
              <FontAwesomeIcon icon={faRightFromBracket} />
              &nbsp;
              <span
                css={css`
                  font-family: "Helvetica";
                  font-style: normal;
                  font-weight: 700;
                  font-size: 14px;
                  line-height: 44px;
                  margin-left: 10px;
                  /* or 314% */
                  align-items: center;
                  color: #2e2f39;
                `}
              >
                Logout
              </span>
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
