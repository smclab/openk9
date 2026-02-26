import React from "react";
import { GenericResultItem } from "../components/client";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faForumbee } from "@fortawesome/free-brands-svg-icons";
import { faCalendar } from "@fortawesome/free-solid-svg-icons";
import { faFileAlt } from "@fortawesome/free-solid-svg-icons/faFileAlt";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { faSitemap } from "@fortawesome/free-solid-svg-icons";
import { faWikipediaW } from "@fortawesome/free-brands-svg-icons";

export function CustomFontAwesomeIcon({
  result,
}: {
  result: GenericResultItem;
}) {
	const documentTypesSet : string[] = result.source.documentTypes;
  if (documentTypesSet.includes("forum")) {
    return <FontAwesomeIcon icon={faForumbee} />;
  }
  if (documentTypesSet.includes("calendar")) {
	  return <FontAwesomeIcon icon={faCalendar} />;
	}
  if (documentTypesSet.includes("user")) {
 	  return <FontAwesomeIcon icon={faUser} />;
 	}
   if (documentTypesSet.includes("site")) {
  	  return <FontAwesomeIcon icon={faSitemap} />;
  	}
   if (documentTypesSet.includes("wiki")) {
   	  return <FontAwesomeIcon icon={faWikipediaW} />;
   	}
  return <FontAwesomeIcon icon={faFileAlt} />;
}