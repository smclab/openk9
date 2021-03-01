import React from "react";
import clsx from "clsx";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./screenSection.module.css";
import { Heading } from "./Heading";

export function ScreenSection({
  title,
  subTitle,
  description,
  imgSrc,
  isDxImage,
}) {
  const props = {
    title,
    subTitle,
    description,
    alignment: isDxImage ? "left" : "right",
  };

  const imageUrl = useBaseUrl(imgSrc);

  return (
    <div
      className={clsx(
        styles.screenSection,
        isDxImage ? styles.rightImage : styles.leftImage,
      )}
    >
      <div className={styles.screenText}>
        <Heading {...props} />
      </div>
      <div
        className={clsx(
          styles.screenImage,
          isDxImage ? styles.imageToLeft : styles.imageToRight,
        )}
        style={{ backgroundImage: `url(${imageUrl})` }}
      ></div>
    </div>
  );
}
