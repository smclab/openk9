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
        iconSrc: "itg/plugins/email.svg",
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
      }
    ],
  },
  
];

function Plugins() {
  return (
    <Layout title="OpenK9 Plugins">
      <header>
        <div className="openK9-wrapper">
          <h1 className={styles.pageTitle}>
            Welcome to the{" "}
            <span className={clsx(styles.primary, styles.secondRow)}>
              OpenK9 Connectors
            </span>
            <span className={styles.blackText}> Collection </span>
          </h1>
          <p className={styles.pageDescription}>
            In this section are describes available connectors for Openk9. Explore single connector to read more on how to configure and use.
            Every connector is available through source code on Github. 
          </p>
        </div>
      </header>
      <main>
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
      </main>
    </Layout>
  );
}

export default Plugins;
