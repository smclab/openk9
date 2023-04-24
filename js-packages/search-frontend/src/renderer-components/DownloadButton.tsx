import React from "react";
import { css } from "styled-components/macro";

type DownloadButtonProps = {
  location: string;
  filename: string;
  children: React.ReactNode;
};
export function DownloadButton({
  location,
  filename,
  children,
}: DownloadButtonProps) {
  return (
    <button
      className="openk9-embeddable-download-button"
      onClick={() => window.open(location)}
      css={css`
        width: 100%;
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
      `}
    >
      {children}
    </button>
  );
}
