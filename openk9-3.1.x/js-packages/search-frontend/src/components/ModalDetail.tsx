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
import { css } from "styled-components";
import { GenericResultItem, DetailRendererProps } from "./client";
import { useRenderers } from "./useRenderers";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { DetailMemo } from "./Detail";

export function ModalDetail({
  content,
  padding = "8px",
  background = "#89878794",
}: {
  content: React.ReactNode;
  padding?: string;
  background?: string;
}) {
  return (
    <div
      className="openk9-modal-mobile openk9-modal"
      css={css`
        position: relative;
      `}
    >
      <div
        className="openk9-wrapper-modal openk9-container-modal"
        css={css`
          padding: ${padding};
          z-index: 500;
          position: fixed;
          top: 0px;
          left: 0px;
          right: 0px;
          bottom: 0px;
          background: ${background};
          overflow: auto;
        `}
      >
        {content}
      </div>
    </div>
  );
}

