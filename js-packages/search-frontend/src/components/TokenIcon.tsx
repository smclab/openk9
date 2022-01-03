import React from "react";
import {
  faFilePdf,
  faGlobe,
  faMapPin,
  faSitemap,
  faTag,
  faUser,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { css } from "styled-components/macro";
import { AnalysisToken } from "@openk9/rest-api";

type TokenIconProps = { token: AnalysisToken };
export function TokenIcon({ token }: TokenIconProps) {
  return (
    <div
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
