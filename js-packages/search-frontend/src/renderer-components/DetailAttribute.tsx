import React from "react";
import { css } from "styled-components";

type DetailAttributeProps = {
  label: string;
  children: React.ReactNode;
};
export function DetailAttribute({ label, children }: DetailAttributeProps) {
  return (
    <div
      className="openk9-embeddable--detail-attribute"
      css={css`
        margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
      `}
    >
      <strong>{label}: </strong>
      {children}
    </div>
  );
}
