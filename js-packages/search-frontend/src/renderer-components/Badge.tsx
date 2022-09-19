import React from "react";

type BadgeProps = {
  children: React.ReactNode;
};
export function Badge({ children }: BadgeProps) {
  return <div className="openk9-embeddable-search--result-badge">{children}</div>;
}
