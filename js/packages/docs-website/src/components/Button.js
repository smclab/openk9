import React from "react";

import styles from "./button.module.css";

export function Button({ href, children, target }) {
  return (
    <a className={styles.button} href={href} target={target}>
      {children}
    </a>
  );
}
