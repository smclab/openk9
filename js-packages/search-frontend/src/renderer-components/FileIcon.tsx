import React from "react";
import { GenericResultItem } from "../components/client";
import { DocumentResultItem } from "../renderers/openk9/document/DocumentItem";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileAlt } from "@fortawesome/free-solid-svg-icons/faFileAlt";
import { faFilePdf } from "@fortawesome/free-solid-svg-icons/faFilePdf";
import { faFileExcel } from "@fortawesome/free-solid-svg-icons/faFileExcel";
import { faFileWord } from "@fortawesome/free-solid-svg-icons/faFileWord";
import { faFilePowerpoint } from "@fortawesome/free-solid-svg-icons/faFilePowerpoint";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons/faEnvelope";
import { faImage } from "@fortawesome/free-solid-svg-icons/faImage";

export function FileIcon({
  result,
}: {
  result: GenericResultItem<DocumentResultItem>;
}) {
  contentTypeSet.add(result.source.document.contentType);
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
  if (result.source.document.contentType === "image/png") {
    return <FontAwesomeIcon icon={faImage} />;
  }
  if (result.source.document.contentType === "image/jpeg") {
    return <FontAwesomeIcon icon={faImage} />;
  }
  if (result.source.document.contentType === "image/jpg") {
    return <FontAwesomeIcon icon={faImage} />;
  }
  if (result.source.document.contentType === "message/rfc822") {
    return <FontAwesomeIcon icon={faEnvelope} />;
  }
  if (result.source.document.contentType === "application/mbox") {
    return <FontAwesomeIcon icon={faEnvelope} />;
  }

  return <FontAwesomeIcon icon={faFileAlt} />;
}

export const contentTypeSet = new Set();
