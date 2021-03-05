import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import styles from "./styles.module.css";

function ComingSoon() {
  return (
    <Layout title="OpenK9">
      <header>
        <div className="openK9-wrapper">
          <h1 className={styles.pageTitle}>
            This Page is{" "}
            <span className={clsx(styles.primary, styles.secondRow)}>
              Coming Soon
            </span>
          </h1>
          <p className={styles.pageDescription}>
            We are working hard to build great docs to help you quickstart your
            search engine.
          </p>
          <p className={styles.pageDescription}>
            In the meantime, you can check out{" "}
            <a href="https://github.com/smclab.openk9">GitHub repository</a>.
          </p>
        </div>
      </header>
    </Layout>
  );
}

export default ComingSoon;
