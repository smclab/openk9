import React from "react";
import clsx from "clsx";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./pricingCard.module.css";

export function PricingCard({ type, price, points, isDisabled }) {
  const imageUrl = useBaseUrl("img/check-mark.svg");
  return (
    <div
      className={clsx(styles.pricingCard, isDisabled && styles.disabledCard)}
    >
      <h4 className={clsx(styles.type, isDisabled && styles.disabledType)}>
        {type}
      </h4>
      <div className={styles.price}>
        <h3 className={styles.amount}>{price}</h3>
        {price !== "Free" && <span className={styles.period}>per month</span>}
      </div>
      <div className={styles.points}>
        {points &&
          points.length > 0 &&
          points.map((point, idx) => (
            <div key={idx}>
              {point.isChecked && (
                <img
                  className={styles.iconCheck}
                  src={imageUrl}
                  alt={`Option price {type}`}
                />
              )}
              <span>{point.name}</span>
            </div>
          ))}
      </div>
    </div>
  );
}
