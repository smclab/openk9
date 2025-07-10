import React from "react";

export function ContainerFluid({
  children,
  size = "xs",
  style,
  flexColumn = false,
}: {
  children: React.ReactNode;
  size?: "xs" | "md" | "lg";
  style?: React.CSSProperties;
  flexColumn?: boolean;
}) {
  const widthMap = {
    xs: "50%",
    md: "85%",
    lg: "100%",
  };

  const styleFlexColumn: React.CSSProperties = {
    display: "flex",
    flexDirection: "column",
    gap: "10px",
  };

  return (
    <div
      className="container-fluid container-view"
      style={{
        width: widthMap[size],
        marginLeft: "0",
        ...(flexColumn && styleFlexColumn),
        ...style,
      }}
    >
      {children}
    </div>
  );
}
