import { GenericResultItem } from "@openk9/rest-api";
import React from "react";
import { EmailResultItem } from "./types";

export function EmailSidebar({ result }: { result: GenericResultItem<EmailResultItem> }) {
  return <pre>{JSON.stringify(result, null, 2)}</pre>;
}
