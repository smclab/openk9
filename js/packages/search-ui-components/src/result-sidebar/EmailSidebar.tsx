import React from "react";
import { createUseStyles } from "react-jss";
import { EmailResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  body: {},
}));

export function EmailSidebar({ result }: { result: EmailResultItem }) {
  const classes = useStyles();
  return (
    <>
      <h3>{result.source.email.subject}</h3>
      <div>
        <strong>Date:</strong>{" "}
        {new Date(result.source.email.date).toLocaleString()}
      </div>
      <div>
        <strong>From:</strong> {result.source.email.from}
      </div>
      <div>
        <strong>To:</strong> {result.source.email.to}
      </div>
      {result.source.email.cc && result.source.email.cc.length > 0 && (
        <div>
          <strong>CC:</strong> {result.source.email.cc}
        </div>
      )}

      <iframe
        frameBorder="0"
        width="100%"
        height="100%"
        srcDoc={result.source.email.htmlBody}
        className={classes.body}
      />
    </>
  );
}
