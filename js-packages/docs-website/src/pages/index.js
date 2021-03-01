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
  alignement: "center",
};

const functionalCaratheristics = [
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
        {functionalCaratheristics && functionalCaratheristics.length > 0 && (
          <section>
            <div className="openK9-wrapper">
              {functionalCaratheristicsTitle && (
                <Heading {...functionalCaratheristicsTitle} />
              )}
              <div className={styles.funtionalFeatures}>
                {functionalCaratheristics.map((props, idx) => (
                  <IconTextItem key={idx} {...props} />
                ))}
              </div>
            </div>
          </section>
        )}

        {keyPoints && keyPoints.length > 0 && (
          <section className={styles.keyPoints}>
            {keyPoints.map((props, idx) => (
              <div className={styles.keyPoint}>
                <div className="openK9-wrapper">
                  <ScreenSection key={idx} {...props} />
                </div>
              </div>
            ))}
          </section>
        )}
      </main>
    </Layout>
  );
}

export default Home;
