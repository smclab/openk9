import React from "react";
import clsx from "clsx";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./iconTextItem.module.css";

export function IconTextItem({ iconSrc, title, description, align }) {
  const iconUrl = useBaseUrl(iconSrc);

  return (
    <div
      className={clsx(
        styles.iconTextItem,
        align === "left"
          ? styles.left
          : align === "right"
          ? styles.right
          : styles.center,
      )}
    >
      {iconUrl && <img alt={title} className={styles.iconItem} src={iconUrl} />}
      {title && <h4 className={styles.titleIconItem}>{title}</h4>}
      {description && (
        <p className={styles.descriptionIconItem}>{description}</p>
      )}
    </div>
  );
}
