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
import { PricingCard } from "../components/PricingCard";
import { SearchesAnimation } from "../components/SearchesAnimation";

const technicalCharacteristics = [
  {
    title: "Cloud Oriented",
    iconSrc: "img/cloud.svg",
    description: (
      <>
        OpenK9 is Cloud Oriented’s solution. Flexible, fast and easy to
        integrate in your IT achitecture and allow you to generate new business
        opportunity.
      </>
    ),
    align: "left",
  },
  {
    title: "Autoscaling",
    iconSrc: "img/autoscaling.svg",
    description: (
      <>
        Our solutions for Enterprise Search Experience is built for relevance at
        scale, easily able to support small businesses, the largest
        multinationals, and everything in between.
      </>
    ),
    align: "left",
  },
  {
    title: "Monitorable",
    iconSrc: "img/monitorable.svg",
    description: (
      <>
        OpenK9 allows easy control of server performance. Check directly from
        the admin panel the server load, how much data it is indexing and get
        alarm bells in case of problems.
      </>
    ),
    align: "left",
  },
  {
    title: "Polyglot parsers",
    iconSrc: "img/polyglot.svg",
    description: (
      <>
        OpenK9 is able to decode multilingual o mixed input languages, using a
        single unified model. This provides easy, fast research without any
        without any language barrier.
      </>
    ),
    align: "left",
  },
];

const keyPoints = [
  {
    title: "multi–source",
    subTitle: "Instant Search, everywhere",
    description: (
      <>
        OpenK9 can handle lots of different data sources with different{" "}
        <a href="/plugins">plugins</a>. This allows user to search inside
        documents, emails, contacts, calendar events and even applications in an
        instant.
      </>
    ),
    imgSrc: "img/search1.png",
    isDxImage: true,
  },
  {
    title: "Token–based Search",
    subTitle: "Structured or Unstructured queries",
    description: (
      <>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
        ipsum volutpat sem convallis aliquam.
      </>
    ),
    imgSrc: "img/tokens.png",
    isDxImage: false,
  },
  {
    title: "Admin panel",
    subTitle: "Monitor and configure from a single panel",
    description: (
      <>
        OpenK9 Administration Panel allow you to set up a new search environment
        in minutes.
      </>
    ),
    imgSrc: "img/admin1.png",
    isDxImage: true,
  },
];

const functionalCharacteristicsTitle = {
  title: "Features",
  subTitle: "Functional Characteristics",
  description: (
    <>
      Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
      ipsum volutpat sem convallis aliquam. Praesent ut tellus eu risus accumsan
      facilisis. Phasellus vulputate maximus elit sit amet maximus.
    </>
  ),
  alignment: "center",
};

const functionalCharacteristics = [
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
    iconSrc: "img/plugins/web-crawler.svg",
    title: "Web Crawler",
  },
  {
    iconSrc: "img/plugins/liferay.svg",
    title: "Liferay",
  },
  {
    iconSrc: "img/plugins/applications.svg",
    title: "Applications",
  },
];

const pricingTitle = {
  title: "Pricing",
  subTitle: "Discover our solutions",
  description: (
    <>
      Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed porttitor
      ipsum volutpat sem convallis aliquam. Praesent ut tellus eu risus accumsan
      facilisis. Phasellus vulputate maximus elit sit amet maximus.
    </>
  ),
  alignment: "center",
};

const pricing = [
  {
    type: "Community",
    price: "Free",
    points: [
      {
        name: "Standard Base Product",
        isChecked: true,
      },
      {
        name: "Online Documentation",
        isChecked: true,
      },
    ],
    isDisabled: false,
  },
  {
    type: "Enterprise",
    price: "?",
    points: [
      {
        name: "All community features plus:",
        isChecked: false,
      },
      {
        name: "H8x5 Support",
        isChecked: true,
      },
      {
        name: "EE plugins",
        isChecked: true,
      },
    ],
    isDisabled: true,
  },
  {
    type: "Saas",
    price: "?",
    points: [
      {
        name: "All enterprise features plus:",
        isChecked: false,
      },
      {
        name: "H24 Support",
        isChecked: true,
      },
      {
        name: "Advanced Machine Learning features",
        isChecked: true,
      },
      {
        name: "Video & Image search",
        isChecked: true,
      },
    ],
    isDisabled: true,
  },
];

const newsletterTitle = {
  title: "Neswletter",
  subTitle: "Subscribe out newsletter and get notification to stay update",
  alignment: "left",
};

function Home() {
  const context = useDocusaurusContext();
  const { siteConfig = {} } = context;
  return (
    <Layout title="OpenK9" description="Search. Everywhere.">
      <header className={clsx("hero", styles.heroBanner)}>
        <div className={clsx("container", styles.heroRow)}>
          <div className={styles.heroTextContainer}>
            <p className={clsx("hero__subtitle", styles.subtitleHeroBanner)}>
              {siteConfig.tagline}
            </p>
            <h1 className={clsx("hero__title", styles.titleHeroBanner)}>
              The intelligent{" "}
              <span className={clsx(styles.doNotBreak, styles.primary)}>
                Open Source
              </span>{" "}
              Search Engine
            </h1>
            <div className={styles.descriptionHeroBanner}>
              OpenK9 is a complete Cognitive Enterprise Search solution that
              fits all your needs. Powerful, Modern and Flexible, it empowers
              Machine Learning to enrich your data and give the best experience
              possible.
            </div>
            <div className={styles.buttons}>
              <Link
                className={clsx(
                  "button button--outline button--primary button--lg",
                )}
                to={useBaseUrl("docs/")}
              >
                Get Started
              </Link>
            </div>
          </div>
          <div className={styles.bannerAnimation}>
            <SearchesAnimation />
          </div>
        </div>
      </header>
      <main>
        {technicalCharacteristics && technicalCharacteristics.length > 0 && (
          <section
            className={clsx(
              styles.sectionLandingPage,
              styles.technicalCharacteristics,
            )}
          >
            <div
              className={clsx(
                styles.technicalCharacteristicsWrapper,
                "openK9-wrapper",
              )}
            >
              {/* {technicalCharacteristicsTitle && (
                <Heading {...technicalCharacteristicsTitle} />
              )} */}
              <div className={styles.technicalFeatures}>
                {technicalCharacteristics.map((props, idx) => (
                  <div className={styles.feature} key={idx}>
                    <IconTextItem {...props} />
                  </div>
                ))}
              </div>
            </div>
          </section>
        )}

        <section className={styles.sectionLandingPage}>
          <div className={clsx("openK9-wrapper", styles.centering)}>
            <Heading
              title="Architecture"
              subTitle="Enrich and Index your Data"
            />
            <img
              src="img/arch.svg"
              alt="OpenK9 Architecture"
              className={styles.architecture}
            />
          </div>
        </section>

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

        {functionalCharacteristics && functionalCharacteristics.length > 0 && (
          <section
            className={clsx(styles.sectionLandingPage, styles.greySection)}
          >
            <div className="openK9-wrapper">
              {functionalCharacteristicsTitle && (
                <Heading {...functionalCharacteristicsTitle} />
              )}
              <div className={styles.funtionalFeaturesContent}>
                {functionalCharacteristics.map((props, idx) => (
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

              <Link
                className="button button--primary button--lg"
                to={useBaseUrl("/plugins")}
              >
                Show Plugins
                <img
                  src={useBaseUrl("img/arrow_forward.svg")}
                  alt="show plugins button"
                />
              </Link>
            </div>
          </section>
        )}

        {pricing && pricing.length > 0 && (
          <section>
            <div className="openK9-wrapper">
              {pricingTitle && <Heading {...pricingTitle} />}
              <div className={styles.pricingCards}>
                {pricing.map((props, idx) => (
                  <PricingCard key={idx} {...props} />
                ))}
              </div>
            </div>
          </section>
        )}

        {/* TODO: add action subscribe to newsletter */}
        <section>
          <div className="openK9-wrapper">
            <form>
              <div className={styles.newsletter}>
                <div className={styles.newsletterHeader}>
                  {newsletterTitle && <Heading {...newsletterTitle} />}
                </div>
                <div className={styles.newsletterEmail}>
                  <input
                    type="text"
                    placeholder="Enter your email address..."
                    className={styles.inputNewsletter}
                  />
                  <button
                    className={clsx(
                      "button button--primary button--lg",
                      styles.submitNewsletter,
                    )}
                  >
                    <img
                      className={styles.submitNewsletterIcon}
                      src={useBaseUrl("img/paper-plane.svg")}
                      alt="submit newsletter"
                    />
                  </button>
                </div>
              </div>
            </form>
          </div>
        </section>
      </main>
    </Layout>
  );
}

export default Home;
