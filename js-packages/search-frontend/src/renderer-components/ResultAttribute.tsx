import React from "react";
import { css } from "styled-components";

type ResultAttributeProps = {
  label: string;
  children: React.ReactNode;
};
export function ResultAttribute({ label, children }: ResultAttributeProps) {
  return (
    <div
      className="openk9-embeddable-result-attribute--container"
      css={css`
        margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
      `}
    >
      <strong>{label}: </strong>
      {children}
    </div>
  );
}
