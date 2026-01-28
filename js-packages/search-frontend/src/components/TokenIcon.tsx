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
import React from "react";
import { faFilePdf } from "@fortawesome/free-solid-svg-icons/faFilePdf";
import { faGlobe } from "@fortawesome/free-solid-svg-icons/faGlobe";
import { faMapPin } from "@fortawesome/free-solid-svg-icons/faMapPin";
import { faTag } from "@fortawesome/free-solid-svg-icons/faTag";
import { faUser } from "@fortawesome/free-solid-svg-icons/faUser";
import { faSitemap } from "@fortawesome/free-solid-svg-icons/faSitemap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { css } from "styled-components";
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

