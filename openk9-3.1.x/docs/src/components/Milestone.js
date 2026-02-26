import React from "react";
import clsx from "clsx";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./milestone.module.css";

export function Milestone({
  imageSrc,
  completed,
  title,
  notes,
  releaseUrl,
  description,
  date,
  leftImage,
}) {
  const imageUrl = useBaseUrl(imageSrc);

  return (
    <div className={styles.milestone}>
      {/* Immagine */}
      <div className={styles.imageContainer}>
        {imageUrl && <img src={imageUrl} alt={title} height={100} />}
      </div>

      {/* Timeline */}
      <div className={styles.timelineContainer}>
        <div
          className={clsx(
            styles.timelineTopLine,
            completed && styles.timelineTopLineCompleted,
          )}
        ></div>
        <div
          className={clsx(
            styles.timelinePoint,
            completed && styles.timelinePointCompleted,
          )}
        ></div>
        <div
          className={clsx(
            styles.timelineBottomLine,
            completed && styles.timelineBottomLineCompleted,
          )}
        ></div>
      </div>

      {/* Milestone */}
      <div className={styles.descriptionContainer}>
        <h2>{title} <h5>{date}</h5></h2>
        {notes && <p>{notes}</p>}
        <p>{description}</p>
        {releaseUrl && <p>Check out <a href={releaseUrl}>release </a> or <a href={releaseUrl}>migration guide </a> on Github for more details.</p>}.
      </div>
    </div>
  );
}
