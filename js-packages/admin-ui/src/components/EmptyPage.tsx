import React from "react";
import { css } from "styled-components/macro";
import { PlusLogo } from "../wizards/Logo/Plus";

export function EmptyPage({ message, link }: { message: string; link: string }) {
  return (
    <div
      css={css`
        width: 80vw;
        height: 70vh;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
      `}
    >
      <h1>{message}</h1>
      <PlusLogo link={link} />
    </div>
  );
}
