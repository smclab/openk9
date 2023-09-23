import React, { Children } from "react";
import { faExternalLinkAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

type DetailTitleExternalLinkProps = { href: string; children: React.ReactNode };
export function DetailTitleExternalLink({
  href,
  children,
}: DetailTitleExternalLinkProps) {
  return (
    <div style={{ display: "flex", alignItems: "baseline" }}>
      <DetailTitle>
        <a href={href} target="_blank">
          {children}
        </a>
      </DetailTitle>
      <a href={href} target="_blank">
        <FontAwesomeIcon
          icon={faExternalLinkAlt}
          style={{ marginLeft: "0.5em" }}
        />
      </a>
    </div>
  );
}