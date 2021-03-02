import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./styles.module.css";
import { Heading } from "../components/Heading";
import { IconTextItem } from "../components/IconTextItem";
import { ScreenSection } from "../components/ScreenSection";
import { FeatureCard } from "../components/FeatureCard";
import { Button } from "../components/Button";

const tecnicalCaratheristicsTitle = {
  title: "Features",
  subTitle: "Tecnical Caratheristics",
  description: (
    <>
      Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
      ipsum volutpat sem convallis aliquam. Praesent ut tellus eu risus accumsan
      facilisis. Phasellus vulputate maximus elit sit amet maximus.
    </>
  ),
  alignment: "center",
};

const tecnicalCaratheristics = [
  {
    title: "Cloud Oriented",
    iconSrc: "img/cloud.svg",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    align: "left",
  },
  {
    title: "Autoscaling",
    iconSrc: "img/cloud.svg",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    align: "left",
  },
  {
    title: "Monitorable",
    iconSrc: "img/cloud.svg",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    align: "left",
  },
  {
    title: "Polyglot parsers",
    iconSrc: "img/cloud.svg",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    align: "left",
  },
];

const keyPoints = [
  {
    title: "Title",
    subTitle: "Insert here the subtitle",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    imgSrc: "img/calendar.png",
    isDxImage: true,
  },
  {
    title: "Title 2",
    subTitle: "Insert here the subtitle 2",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    imgSrc: "img/calendar.png",
    isDxImage: false,
  },
  {
    title: "Title 3",
    subTitle: "Insert here the subtitle 3",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    imgSrc: "img/calendar.png",
    isDxImage: true,
  },
];

const functionalCaratheristicsTitle = {
  title: "Features",
  subTitle: "Functional Caratheristics",
  description: (
    <>
      Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
      ipsum volutpat sem convallis aliquam. Praesent ut tellus eu risus accumsan
      facilisis. Phasellus vulputate maximus elit sit amet maximus.
    </>
  ),
  alignment: "center",
};

const functionalCaratheristics = [
  {
    title: "Multi-tenant",
    imageSrc: "img/placeholder.png",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    align: "left",
  },
  {
    title: "Pluggable",
    imageSrc: "img/placeholder.png",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    align: "left",
  },
  {
    title: "Customizable",
    imageSrc: "img/placeholder.png",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    align: "left",
  },
  {
    title: "Headless",
    imageSrc: "img/placeholder.png",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    align: "left",
  },
  {
    title: "Fine UX",
    imageSrc: "img/placeholder.png",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    align: "left",
  },
];

const pluginsTitle = {
  title: "Plugins",
  subTitle: "Discover all the potential offered with",
  alignment: "left",
};

const plugins = [
  {
    iconSrc: "img/plugins/email.svg",
    title: "E-Mail",
  },
  {
    iconSrc: "img/plugins/file-storage.svg",
    title: "File System",
  },
  {
    iconSrc: "img/plugins/webcrawler.svg",
    title: "Web Crawler",
  },
  {
    iconSrc: "img/plugins/liferay.png",
    title: "Liferay",
  },
  {
    iconSrc: "img/plugins/applications.svg",
    title: "Applications",
  },
];

function Home() {
  const context = useDocusaurusContext();
  const { siteConfig = {} } = context;
  return (
    <Layout title="OpenK9" description="Search. Everywhere.">
      <header className={clsx("hero hero--primary", styles.heroBanner)}>
        <div className="container">
          <h1 className="hero__title">{siteConfig.title}</h1>
          <p className="hero__subtitle">{siteConfig.tagline}</p>
          <div className={styles.buttons}>
            <Link
              className={clsx(
                "button button--outline button--secondary button--lg",
                styles.getStarted,
              )}
              to={useBaseUrl("docs/")}
            >
              Get Started
            </Link>
          </div>
        </div>
      </header>
      <main>
        {tecnicalCaratheristics && tecnicalCaratheristics.length > 0 && (
          <section className={styles.sectionLandingPage}>
            <div className="openK9-wrapper">
              {/* {tecnicalCaratheristicsTitle && (
                <Heading {...tecnicalCaratheristicsTitle} />
              )} */}
              <div className={styles.tecnicalFeatures}>
                {tecnicalCaratheristics.map((props, idx) => (
                  <div className={styles.feature} key={idx}>
                    <IconTextItem {...props} />
                  </div>
                ))}
              </div>
            </div>
          </section>
        )}

        {keyPoints && keyPoints.length > 0 && (
          <section
            className={clsx(styles.sectionLandingPage, styles.keyPoints)}
          >
            {keyPoints.map((props, idx) => (
              <div key={idx} className={styles.keyPoint}>
                <div className="openK9-wrapper">
                  <ScreenSection key={idx} {...props} />
                </div>
              </div>
            ))}
          </section>
        )}

        {functionalCaratheristics && functionalCaratheristics.length > 0 && (
          <section
            className={clsx(styles.sectionLandingPage, styles.greySection)}
          >
            <div className="openK9-wrapper">
              {functionalCaratheristicsTitle && (
                <Heading {...functionalCaratheristicsTitle} />
              )}
              <div className={styles.funtionalFeaturesContent}>
                {functionalCaratheristics.map((props, idx) => (
                  <FeatureCard key={idx} {...props} />
                ))}
              </div>
            </div>
          </section>
        )}

        {plugins && plugins.length > 0 && (
          <section className={styles.sectionLandingPage}>
            <div className="openK9-wrapper">
              {pluginsTitle && <Heading {...pluginsTitle} />}
              <div className={styles.plugins}>
                {plugins.map((props, idx) => (
                  <div className={styles.plugin} key={idx}>
                    <IconTextItem {...props} />
                  </div>
                ))}
              </div>
              <Button
                href="/plugins"
                iconSrc="img/arrow_forward.svg"
                target="_blank"
              >
                Show Plugins
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  enable-background="new 0 0 24 24"
                  height="24"
                  viewBox="0 0 24 24"
                  width="24"
                >
                  <path
                    fill="#fff"
                    d="M5,13h11.17l-4.88,4.88c-0.39,0.39-0.39,1.03,0,1.42l0,0c0.39,0.39,1.02,0.39,1.41,0l6.59-6.59 c0.39-0.39,0.39-1.02,0-1.41l-6.58-6.6c-0.39-0.39-1.02-0.39-1.41,0l0,0c-0.39,0.39-0.39,1.02,0,1.41L16.17,11H5 c-0.55,0-1,0.45-1,1l0,0C4,12.55,4.45,13,5,13z"
                  />
                </svg>
              </Button>
            </div>
          </section>
        )}
      </main>
    </Layout>
  );
}

export default Home;
