import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import styles from "./styles.module.css";
import { PluginCard } from "../components/PluginCard";

const plugins = [
  {
    cards: [
      {
        iconSrc: "img/plugins/sitemap.svg",
        title: "Sitemap",
        pluginHref: "/docs/plugins/sitemap-plugin",
        description: (
          <>
            Crawl data from web sites using information provided by Sitemap's site
          </>
        ),
      },
      {
        iconSrc: "img/plugins/web-crawler.svg",
        title: "Web Crawler",
        pluginHref: "/docs/plugins/web-crawler",
        description: (
          <>
            Get data from web sites crawling urls
          </>
        ),
      },
      {
        iconSrc: "img/plugins/email.svg",
        title: "Imap",
        pluginHref: "/docs/plugins/imap-plugin",
        description: (
          <>
            Extract and handle email and relative attachments from Imap email server
          </>
        ),
      },
      {
        iconSrc: "img/plugins/database.svg",
        title: "Database",
        pluginHref: "/docs/plugins/database-plugin",
        description: (
          <>
            Extract and handle data coming from relational database
          </>
        ),
      },
            {
        iconSrc: "img/plugins/database.svg",
        title: "Minio",
        pluginHref: "/docs/plugins/minio-plugin",
        description: (
          <>
            Extract and handle data coming from S3 Minio storage
          </>
        ),
      },
                  {
        iconSrc: "img/plugins/database.svg",
        title: "Youtube",
        pluginHref: "/docs/plugins/youtube-plugin",
        description: (
          <>
            Extract and handle data coming from Youtube
          </>
        ),
      }
    ],
  },
  
];

function Plugins() {
  return (
    <Layout title="OpenK9 Plugins">
      <header>
        <div className="openK9-wrapper">
          {/* <h1 className={styles.pageTitle}>
            Welcome to the{" "}
            <span className={clsx(styles.primary, styles.secondRow)}>
              OpenK9 Connectors
            </span>
            <span className={styles.blackText}> Collection </span>
          </h1>
          <p className={styles.pageDescription}>
            In this section are describes available connectors for Openk9. Explore single connector to read more on how to configure and use.
            Every connector is available through source code on Github. 
          </p> */}
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
                      <a href="https://github.com/smclab/openk9/tree/main/connectors">GitHub repository</a> for universal connectors available.
                    </p>
        </div>
      </header>
      {/* <main>
        <div className="openK9-wrapper">
          {plugins &&
            plugins.length > 0 &&
            plugins.map((plugin, idx) => (
              <section className={styles.section} key={idx}>
                <h4 className={styles.sectionSubtitle}>{plugin.type}</h4>
                <div className={styles.pluginContainer}>
                  {plugin.cards.map((props, id) => (
                    <PluginCard {...props} key={id} />
                  ))}
                </div>
              </section>
            ))}
        </div>
      </main> */}
    </Layout>
  );
}

export default Plugins;
