import React from "react";
import { css, keyframes } from "styled-components/macro";

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
  width?: string;
  height?: string;
  counter?: number;
  circle?: boolean;
  backgroundColor?: string;
  itereitorKey?: string;
  containerMax?: boolean;
}

const CustomSkeleton: React.FC<CustomSkeletonProps> = ({
  width = "100%",
  height = "20px",
  counter = 1,
  circle = false,
  backgroundColor = "red",
  itereitorKey,
  containerMax = false,
}) => {
  return (
    <div
      key={itereitorKey}
      className="custom-container-skeleton"
      css={css`
        display: flex;
        flex-direction: column;
        gap: 5px;
        width: ${containerMax ? "100%" : ""};
      `}
    >
      {Array.from({ length: counter }).map((_, index) => (
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
