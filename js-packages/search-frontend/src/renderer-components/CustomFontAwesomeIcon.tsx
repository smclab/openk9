import React from "react";
import { GenericResultItem } from "../components/client";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faForumbee } from "@fortawesome/free-brands-svg-icons";
import { faCalendar } from "@fortawesome/free-solid-svg-icons";
import { faFileAlt } from "@fortawesome/free-solid-svg-icons/faFileAlt";

export function CustomFontAwesomeIcon({
  result,
}: {
  result: GenericResultItem<E>;
}) {
  contentTypeSet.add(result.source.document.contentType);
  if (result.source.documentTypes.includes("forum")) {
    return <FontAwesomeIcon icon={faForumbee} />;
  }
  if (result.source.documentTypes.includes("calendar")) {
	  return <FontAwesomeIcon icon={faCalendar} />;
	}

  return <FontAwesomeIcon icon={faFileAlt} />;
}

export const contentTypeSet = new Set();
