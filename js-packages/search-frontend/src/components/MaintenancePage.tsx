import React from "react";
import { css } from "styled-components/macro";
import { Logo } from "./Logo";

export function MaintenancePage() {
  return (
    <div
      css={css`
        width: 100vw;
        height: 100vh;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: var(--openk9-embeddable-search--primary-color);
      `}
    >
      <Logo size={128} />
      <h1>The service is undergoing maintenance</h1>
      <p>Contact Daniele Caldarini at +393209443088</p>
    </div>
  );
}
