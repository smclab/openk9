import React from "react";
import { css } from "styled-components/macro";

type DetailAttributeProps = {
  label: string;
  children: React.ReactNode;
};
export function DetailAttribute({ label, children }: DetailAttributeProps) {
  return (
    <div
      css={css`
        margin-bottom: 8px;
      `}
    >
      <strong>{label}: </strong>
      {children}
    </div>
  );
}
