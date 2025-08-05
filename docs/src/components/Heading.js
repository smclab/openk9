import React from "react";
import clsx from "clsx";
import styles from "./heading.module.css";

export function Heading({ title, subTitle, description, alignment }) {
  return (
    <div
      className={clsx(
        styles.heading,
        alignment === "left"
          ? styles.left
          : alignment === "right"
          ? styles.right
          : styles.center,
      )}
    >
      {title && <h3 className={styles.titleHeading}>{title}</h3>}
      {subTitle && <h2 className={styles.subtitleHeading}>{subTitle}</h2>}
      {description && (
        <p className={styles.descriptionHeading}>{description}</p>
      )}
    </div>
  );
}
