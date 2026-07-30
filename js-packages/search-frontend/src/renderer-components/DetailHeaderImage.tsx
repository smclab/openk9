import React from "react";
import { css } from "styled-components";

type DetailHeaderImageProps = {
  src: string;
};
export function DetailHeaderImage({ src }: DetailHeaderImageProps) {
  return (
    <img
      className="openk9-embeddable-detail-header-images"
      src={src}
      alt=""
      css={css`
        max-width: 100%;
        margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
      `}
    />
  );
}
