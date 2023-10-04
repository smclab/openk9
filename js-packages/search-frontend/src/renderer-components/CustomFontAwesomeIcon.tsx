import React from "react";
import { GenericResultItem } from "../components/client";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faForumbee } from "@fortawesome/free-brands-svg-icons";
import { faCalendar } from "@fortawesome/free-solid-svg-icons";
import { faFileAlt } from "@fortawesome/free-solid-svg-icons/faFileAlt";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { faSitemap } from "@fortawesome/free-solid-svg-icons";
import { faWikipediaW } from "@fortawesome/free-brands-svg-icons";

export function CustomFontAwesomeIcon<E>({
  result
}: {
  result: GenericResultItem<E>;
}) {
  const realResult = result as any
  if (realResult.source.documentTypes.includes("forum")) {
    return <FontAwesomeIcon icon={faForumbee} />;
  }
  if (realResult.source.documentTypes.includes("calendar")) {
	  return <FontAwesomeIcon icon={faCalendar} />;
	}
  if (realResult.source.documentTypes.includes("user")) {
 	  return <FontAwesomeIcon icon={faUser} />;
 	}
   if (realResult.source.documentTypes.includes("site")) {
  	  return <FontAwesomeIcon icon={faSitemap} />;
  	}
   if (realResult.source.documentTypes.includes("wiki")) {
   	  return <FontAwesomeIcon icon={faWikipediaW} />;
   	}
  return <FontAwesomeIcon icon={faFileAlt} />;
}
