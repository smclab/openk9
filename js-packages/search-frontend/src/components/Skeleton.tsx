import React from "react";
import { css, keyframes } from "styled-components";

const pulse = keyframes`
  0% {
    opacity: 0.6;
  }
  50% {
    opacity: 0.9;
  }
  100% {
    opacity: 0.6;
  }
`;

interface CustomSkeletonProps {
  width?: string | null | undefined | null | undefined;
  height?: string | null | undefined;
  counter?: number | null | undefined;
  circle?: boolean | null | undefined;
  backgroundColor?: string | null | undefined;
  itereitorKey?: string | null | undefined;
  containerMax?: boolean | null | undefined;
  position?: "column" | "row";
  gap?: string | null | undefined;
}

const CustomSkeleton: React.FC<CustomSkeletonProps> = ({
  width = "100%",
  height = "20px",
  counter = 1,
  circle = false,
  backgroundColor = "red",
  itereitorKey,
  containerMax = false,
  position = "column",
  gap = "5px",
}) => {
  return (
    <div
      key={itereitorKey}
      className="custom-container-skeleton"
      css={css`
        display: flex;
        flex-direction: ${position};
        gap: ${gap};
        width: ${containerMax ? "100%" : ""};
      `}
    >
      {Array.from({ length: counter || 1 }).map((_, index) => (
        <div
          key={index}
          className="custom-skeleton"
          css={css`
            background-color: ${backgroundColor};
            border-radius: ${circle ? "50%" : "5px"};
            width: ${width};
            min-height: ${height};
            animation: ${pulse} 1.5s ease-in-out infinite;
          `}
        ></div>
      ))}
    </div>
  );
};

export default CustomSkeleton;
