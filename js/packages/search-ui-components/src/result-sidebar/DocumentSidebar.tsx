import React from "react";
import { createUseStyles } from "react-jss";
import { DocumentResultItem } from "@openk9/http-api";
import { ImageSlider } from "../components/ImageSlider";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  break: {
    overflow: "hidden",
    maxWidth: "100%",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },
  previews: {
    margin: [theme.spacingUnit * 2, 0],
  },
}));

export function DocumentSidebar({ result }: { result: DocumentResultItem }) {
  const classes = useStyles();

  return (
    <>
      <h3>{result.source.document.title}</h3>
      <div className={classes.break}>
        <strong>File Type:</strong> {result.source.document.contentType}
      </div>
      <div className={classes.break}>
        <strong>Document Type:</strong> {result.source.document.documentType}
      </div>
      {result.source.spaces && (
        <div className={classes.break}>
          <strong>Space:</strong> {result.source.spaces.spaceName} (
          {result.source.spaces.spaceId})
        </div>
      )}
      <div className={classes.break}>
        <strong>Path:</strong> {result.source.file.path}
      </div>
      <div className={classes.break}>
        <strong>Last Edit:</strong>{" "}
        {new Date(result.source.file.lastModifiedDate).toLocaleString()}
      </div>
      <div className={classes.break}>
        <strong>URL:</strong>{" "}
        <a href={result.source.document.URL} target="_blank">
          {result.source.document.URL}
        </a>
      </div>
      <ImageSlider
        key={result.source.id}
        urls={result.source.document.previewURLs}
        className={classes.previews}
      />
      <div>
        <strong>Content:</strong> {result.source.document.content}
      </div>
    </>
  );
}
