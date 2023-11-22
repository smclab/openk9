import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import styles from "./styles.module.css";
import { PluginCard } from "../components/PluginCard";

const plugins = [
  {
    type: "Basic tier",
    cards: [
      {
        iconSrc: "img/plugins/sitemap.svg",
        title: "Sitemap",
        pluginHref: "/docs/plugins/sitemap-plugin",
        description: (
          <>
            Plugin to get and handle data from web site using information provided by Sitemap's site
          </>
        ),
      },
      {
        iconSrc: "img/plugins/web-crawler.svg",
        title: "Web Crawler",
        pluginHref: "/docs/plugins/web-crawler",
        description: (
          <>
            Plugin to get and handle data from web site crawling in generic web
          </>
        ),
      },
      {
        iconSrc: "img/plugins/email.svg",
        title: "Imap",
        pluginHref: "/docs/plugins/imap-plugin",
        description: (
          <>
            Plugin to get and handle email and relative attachments from imap email server
          </>
        ),
      },
      {
        iconSrc: "img/plugins/database.svg",
        title: "Database",
        pluginHref: "/docs/plugins/database-plugin",
        description: (
          <>
            Plugin to get and handle data coming from relational database
          </>
        ),
      },
      {
        iconSrc: "img/plugins/push.svg",
        title: "Push",
        pluginHref: "/docs/plugins/push-plugin",
        description: (
          <>
            Plugin to push data from external
          </>
        ),
      }
    ],
  },
  {
    type: "Advanced tier",
    cards: [
      {
        iconSrc: "img/plugins/gitlab.svg",
        title: "Gitlab",
        pluginHref: "/docs/plugins/gitlab-plugin",
        description: (
          <>
            Plugin to
          </>
        ),
      },
      {
        iconSrc: "img/plugins/github.svg",
        title: "Github",
        pluginHref: "/docs/plugins/github-plugin",
        description: (
          <>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut nisi
            est, laoreet vitae porttitor sed, aliquam et ex.
          </>
        ),
      },
      {
        iconSrc: "img/plugins/liferay.svg",
        title: "Liferay",
        pluginHref: "/docs/plugins/liferay-plugin",
        description: (
          <>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut nisi
            est, laoreet vitae porttitor sed, aliquam et ex.
          </>
        ),
      },
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
              OpenK9 Plugins
            </span>
            <span className={styles.blackText}> Collection </span>
          </h1>
          <p className={styles.pageDescription}>
            In this section are describes available plugins for Openk9
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
