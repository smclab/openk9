import React from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./pluginCard.module.css";

export function PluginCard({ iconSrc, title, description }) {
  const iconUrl = useBaseUrl(iconSrc);

  return (
    <div className={styles.pluginCard}>
      <img className={styles.pluginCardIcon} src={iconUrl} alt={title} />
      <div className={styles.pluginCardText}>
        <h4 className={styles.pluginCardTitle}>{title}</h4>
        <div className={styles.pluginCardDescription}>{description}</div>
      </div>
    </div>
  );
}
