import React from "react";
import clsx from "clsx";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./featureCard.module.css";

export function FeatureCard({ imageSrc, title, description, align }) {
  const imageUrl = useBaseUrl(imageSrc);

  return (
    <div
      className={clsx(
        styles.featureCard,
        align === "left"
          ? styles.left
          : align === "right"
          ? styles.right
          : styles.center,
      )}
    >
      <div
        className={styles.featureCardImage}
        style={{ backgroundImage: `url(${imageUrl})` }}
      ></div>
      <div className={styles.featureCardText}>
        <h4 className={styles.featureCardTitle}>{title}</h4>
        <p className={styles.featureCardDescription}>{description}</p>
      </div>
    </div>
  );
}
