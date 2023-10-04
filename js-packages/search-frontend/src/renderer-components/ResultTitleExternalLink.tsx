import React, { Children } from "react";
import { faExternalLinkAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { ResultTitle } from "./ResultTitle";

type ResultTitleExternalLinkProps = { href: string; children: React.ReactNode };
export function ResultTitleExternalLink({
  href,
  children,
}: ResultTitleExternalLinkProps) {
  return (
    <div style={{ display: "flex", alignItems: "baseline" }}>
      <ResultTitle>
        <a href={href} target="_blank">
          {children}
        </a>
      </ResultTitle>
      <a href={href} target="_blank">
        <FontAwesomeIcon
          icon={faExternalLinkAlt}
          style={{ marginLeft: "0.5em" }}
        />
      </a>
    </div>
  );
}