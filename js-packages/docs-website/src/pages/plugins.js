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
        iconSrc: "img/plugins/email.svg",
        title: "Sitemap",
        pluginHref: "/docs/standalone-app",
        description: (
          <>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut nisi
            est, laoreet vitae porttitor sed, aliquam et ex.
          </>
        ),
      },
      {
        iconSrc: "img/plugins/web-crawler.svg",
        title: "Web Crawler",
        pluginHref: "/docs/standalone-app",
        description: (
          <>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut nisi
            est, laoreet vitae porttitor sed, aliquam et ex.
          </>
        ),
      },
      {
        iconSrc: "img/plugins/email.svg",
        title: "Imap",
        pluginHref: "/docs/plugins/imap-plugin",
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
        pluginHref: "/docs/standalone-app",
        description: (
          <>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut nisi
            est, laoreet vitae porttitor sed, aliquam et ex.
          </>
        ),
      }
    ],
  },
  {
    type: "Advanced tier",
    cards: [
      {
        iconSrc: "img/plugins/email.svg",
        title: "Email",
        pluginHref: "/docs/standalone-app",
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
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut nisi
            est, laoreet vitae porttitor sed, aliquam et ex.
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
