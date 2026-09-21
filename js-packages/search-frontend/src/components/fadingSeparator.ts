import { css } from "styled-components";

/**
 * A separator that dies out instead of hitting the panel edge.
 *
 * It is a pseudo-element and not a border because a border cannot carry a
 * gradient. The colour is full strength across the content and fades over
 * `inset`, which callers pass as their own horizontal padding: the line then
 * starts and ends exactly where the text does.
 */
export const fadingSeparator = (color: string, inset: string) => css`
  position: relative;

  &::after {
    content: "";
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 1px;
    background: linear-gradient(
      to right,
      transparent 0,
      ${color} ${inset},
      ${color} calc(100% - ${inset}),
      transparent 100%
    );
  }
`;
