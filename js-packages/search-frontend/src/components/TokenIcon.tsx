import React from "react";
import { faFilePdf } from "@fortawesome/free-solid-svg-icons/faFilePdf";
import { faGlobe } from "@fortawesome/free-solid-svg-icons/faGlobe";
import { faMapPin } from "@fortawesome/free-solid-svg-icons/faMapPin";
import { faTag } from "@fortawesome/free-solid-svg-icons/faTag";
import { faUser } from "@fortawesome/free-solid-svg-icons/faUser";
import { faSitemap } from "@fortawesome/free-solid-svg-icons/faSitemap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { css } from "styled-components/macro";
import { AnalysisToken } from "./client";

type TokenIconProps = { token: AnalysisToken };
export function TokenIcon({ token }: TokenIconProps) {
  return (
    <div
      className="openk9-token-icon"
      css={css`
        width: 16px;
        display: flex;
        margin-right: 8px;
        justify-content: center;
        align-items: center;
      `}
    >
      {(() => {
        switch (token.tokenType) {
          case "DOCTYPE": {
            switch (token.value) {
              case "pdf":
                return <FontAwesomeIcon icon={faFilePdf} />;
              case "web":
                return <FontAwesomeIcon icon={faGlobe} />;
              default:
                return <FontAwesomeIcon icon={faTag} />;
            }
          }
          case "ENTITY": {
            switch (token.entityType) {
              case "person":
                return <FontAwesomeIcon icon={faUser} />;
              case "loc":
                return <FontAwesomeIcon icon={faMapPin} />;
              case "organization":
                return <FontAwesomeIcon icon={faSitemap} />;
              default:
                return <FontAwesomeIcon icon={faTag} />;
            }
          }
          default:
            return <FontAwesomeIcon icon={faTag} />;
        }
      })()}
    </div>
  );
}
