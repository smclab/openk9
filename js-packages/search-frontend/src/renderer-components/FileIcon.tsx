import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { DocumentResultItem } from "../renderers/openk9/document/DocumentItem";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileAlt } from "@fortawesome/free-solid-svg-icons/faFileAlt";
import { faFilePdf } from "@fortawesome/free-solid-svg-icons/faFilePdf";
import { faFileExcel } from "@fortawesome/free-solid-svg-icons/faFileExcel";
import { faFileWord } from "@fortawesome/free-solid-svg-icons/faFileWord";
import { faFilePowerpoint } from "@fortawesome/free-solid-svg-icons/faFilePowerpoint";

export function FileIcon({
  result,
}: {
  result: GenericResultItem<DocumentResultItem>;
}) {
  contentTypeSet.add(result.source.document.contentType);
  console.log(contentTypeSet);
  if (result.source.document.contentType === "application/pdf")
    return <FontAwesomeIcon icon={faFilePdf} />;
  if (
    result.source.document.contentType ===
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
  ) {
    return <FontAwesomeIcon icon={faFileExcel} />;
  }
  if (
    result.source.document.contentType ===
    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
  ) {
    return <FontAwesomeIcon icon={faFilePowerpoint} />;
  }
  if (
    result.source.document.contentType ===
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
  ) {
    return <FontAwesomeIcon icon={faFileWord} />;
  }
  if (result.source.document.contentType === "application/msword") {
    return <FontAwesomeIcon icon={faFileWord} />;
  }

  return <FontAwesomeIcon icon={faFileAlt} />;
}

export const contentTypeSet = new Set();
