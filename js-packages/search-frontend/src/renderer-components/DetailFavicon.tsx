import React from "react";
import { css } from "styled-components";

type DetailFaviconProps = {
  src: string;
};
export function DetailFavicon({ src }: DetailFaviconProps) {
  return (
    <img
      className="openk9-embeddable-detail-favicon"
      src={src}
      alt=""
      css={css`
        margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
      `}
    />
  );
}
