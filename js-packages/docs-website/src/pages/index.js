import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import useBaseUrl from "@docusaurus/useBaseUrl";
import CookieConsent from "react-cookie-consent";

import styles from "./styles.module.css";
import { Heading } from "../components/Heading";
import { IconTextItem } from "../components/IconTextItem";
import { ScreenSection } from "../components/ScreenSection";
import { FeatureCard } from "../components/FeatureCard";
import { PricingCard } from "../components/PricingCard";
import { SearchesAnimation } from "../components/SearchesAnimation";

function Home() {
  const context = useDocusaurusContext();
  const { siteConfig = {} } = context;
  return (
    <Layout title="OpenK9">
      <CookieConsent
        buttonStyle={{
          color: "white",
          backgroundColor: "#c22525",
          borderRadius: 16,
          fontSize: "13px",
        }}
      >
        This website or its third-party tools use cookies, which are necessary
        to its functioning and required to achieve the purposes illustrated in
        the <Link to="/privacy">cookie policy</Link>. If you want to know more
        or withdraw your consent to all or some of the cookies, please refer to
        the cookie policy. By closing this banner, scrolling this page, clicking
        a link or continuing to browse otherwise, you agree to the use of
        cookies.
      </CookieConsent>
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
              fits all your needs. Powerful, Modern and Flexible, it employs
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
            <div className={styles.technicalFeatures}>
              <div className={styles.feature}>
                <IconTextItem
                  title="Machine–Learning"
                  iconSrc="img/machine-learning.svg"
                  description={
                    <>
                      Using Machine Learning algorithms, such as Named Entity
                      Recognition and Image Understanding, OpenK9 is able to
                      enrich any kind of document with domain-specific
                      information, useful for your search.
                    </>
                  }
                  align="left"
                />
              </div>

              <div className={styles.feature}>
                <IconTextItem
                  title="Cloud Oriented"
                  iconSrc="img/cloud.svg"
                  description={
                    <>
                      OpenK9 is a Cloud Oriented solution. Flexible, fast and
                      easy to integrate in your K8S IT architecture, it allows
                      you to generate new business opportunities at scale.
                    </>
                  }
                  align="left"
                />
              </div>
              <div className={styles.feature}>
                <IconTextItem
                  title="Autoscaling"
                  iconSrc="img/autoscaling.svg"
                  description={
                    <>
                      Our solution for Enterprise Search Experience is built to
                      scale with ease, both for data and users, easily managing
                      small document databases, complex architectures and
                      everything in between.
                    </>
                  }
                  align="left"
                />
              </div>
              <div className={styles.feature}>
                <IconTextItem
                  title="Fast and Usable"
                  iconSrc="img/fast.svg"
                  description={
                    <>
                      OpenK9 is designed for speed to improve User Experience,
                      giving results in real time as soon as the user starts
                      typing in the search bar.
                    </>
                  }
                  align="left"
                />
              </div>
              {/* <div className={styles.feature}>
                <IconTextItem
                  title="Monitorable"
                  iconSrc="img/monitorable.svg"
                  description={
                    <>
                      OpenK9 allows easy control of server performance. Check
                      directly from the admin panel the server load, how much
                      data it's indexing and get alarm notifications in case of
                      problems.
                    </>
                  }
                  align="left"
                />
              </div> */}
              {/* <div className={styles.feature}>
                <IconTextItem
                  title="Polyglot parsers"
                  iconSrc="img/polyglot.svg"
                  description={
                    <>
                      OpenK9 is truly open: integration plugins can be developed
                      using any programming language. Just choose yours and
                      follow our guidelines.
                    </>
                  }
                  align="left"
                />
              </div> */}
            </div>
          </div>
        </section>

        <section className={styles.sectionLandingPage}>
          <div className={clsx("openK9-wrapper", styles.centering)}>
            <Heading
              title="Architecture"
              subTitle="Enrich and Index your Data"
              description="Our modern and scalable architecture provides a pipeline for data ingestion, enrichment and indexing. Every part of the pipeline can easily scale to handle massive amount of data."
            />
            <div className={styles.archScrollWrapper}>
              <img
                src="img/arch.svg"
                alt="OpenK9 Architecture"
                className={styles.architecture}
              />
            </div>
          </div>
        </section>

        <section className={clsx(styles.sectionLandingPage, styles.keyPoints)}>
          <div className={styles.keyPoint}>
            <div className="openK9-wrapper">
              <ScreenSection
                titl="Multi–source"
                subTitle="Instant Search, everywhere"
                description={
                  <>
                    OpenK9 can handle lots of different data sources with
                    different plugins. Search inside documents, emails,
                    contacts, calendar events and even applications in an
                    instant.
                  </>
                }
                imgSrc="img/search1.png"
                isDxImage={true}
              />
            </div>
          </div>

          <div className={styles.keyPoint}>
            <div className="openK9-wrapper">
              <ScreenSection
                title="Token–based Search"
                subTitle="Structured or Unstructured queries"
                description={
                  <>
                    Search using both fuzzy queries with free text and precise
                    boolean tokens with domain–specific entities. Build your own
                    entities and predicates using plugins.
                  </>
                }
                imgSrc="img/tokens.png"
                isDxImage={false}
              />
            </div>
          </div>

          <div className={styles.keyPoint}>
            <div className="openK9-wrapper">
              <ScreenSection
                title="Admin panel"
                subTitle="Monitor and configure from a single panel"
                description={
                  <>
                    OpenK9 Administration Panel allows you to set up a new
                    search environment in minutes. Tweak and monitor every part
                    of the search system at a glance.
                  </>
                }
                imgSrc="img/admin1.png"
                isDxImage={true}
              />
            </div>
          </div>
        </section>

        <section
          className={clsx(styles.sectionLandingPage, styles.greySection)}
        >
          <div className="openK9-wrapper">
            <Heading
              title="Features"
              subTitle="Powerful. Extensible. Open."
              description={
                <>
                  OpenK9, thanks to its open architecture, offers a set of
                  powerful features to build your own customized search system.
                </>
              }
              alignment="center"
            />
            <div
              className={clsx(
                styles.functionalFeaturesContent,
                styles["gap-20px"],
              )}
            >
              <FeatureCard
                title="Multi-tenant"
                imageSrc="img/multitenant.svg"
                description={
                  <>
                    OpenK9 is a multi-tenant solution. This allows you to
                    consolidate and allocate resources efficiently and access
                    additional capacity when needed. It reduces the need for
                    individual users to manage upgrades and maintenance.
                  </>
                }
                align="left"
              />
              <FeatureCard
                title="Pluggable"
                imageSrc="img/pluggable.svg"
                description={
                  <>
                    Our solution leverages OSGi to build a pluggable
                    architecture to ingest data from multiple sources, enrich
                    data with domain–specific knowledge and build a searchable
                    knowledge graph.
                  </>
                }
                align="left"
              />
              <FeatureCard
                title="Customizable"
                imageSrc="img/customizable.svg"
                description={
                  <>
                    Our Enterprise Search Experience solution is fully
                    extendable and customizable to fit all your needs, through
                    ready-for-use plugins and reusable UI components.
                  </>
                }
                align="left"
              />
              <FeatureCard
                title="Headless"
                imageSrc="img/headless.svg"
                description={
                  <>
                    Using our headless API, OpenK9 allows easy development of
                    every type of custom applications, such as mobile apps,
                    speech interfaces and even analytics and data visualization.
                  </>
                }
                align="left"
              />
              <FeatureCard
                title="Fine UX"
                imageSrc="img/fine-ux.svg"
                description={
                  <>
                    You are going to love OpenK9 from the very first use: enjoy
                    the refined and polished design, the simple and intuitive
                    interface and live a satisfying experience, whether you’re a
                    beginner or an expert user.
                  </>
                }
                align="left"
              />
              <FeatureCard
                title="Polyglot"
                imageSrc="img/polyglot2.svg"
                description={
                  <>
                    Don't like Java? TypeScript or Python fan? We got you
                    covered. With our open architecture you are free to develop
                    plugins in your language of choice, using also our examples
                    as starting point.
                  </>
                }
                align="left"
              />
            </div>
          </div>
        </section>

        {/* <section className={styles.sectionLandingPage}>
          <div className="openK9-wrapper">
            <Heading
              title="Plugins"
              subTitle="Discover all the potential offered with"
              alignment="left"
            />
            <div className={styles.plugins}>
              <div className={styles.plugin}>
                <IconTextItem iconSrc="img/plugins/email.svg" title="E-Mail" />
              </div>
              <div className={styles.plugin}>
                <IconTextItem
                  iconSrc="img/plugins/file-storage.svg"
                  title="File System"
                />
              </div>
              <div className={styles.plugin}>
                <IconTextItem
                  iconSrc="img/plugins/web-crawler.svg"
                  title="Web Crawler"
                />
              </div>
              <div className={styles.plugin}>
                <IconTextItem
                  iconSrc="img/plugins/liferay.svg"
                  title="Liferay"
                />
              </div>
              <div className={styles.plugin}>
                <IconTextItem
                  iconSrc="img/plugins/applications.svg"
                  title="Applications"
                />
              </div>
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
        </section> */}

        <section>
          <div className="openK9-wrapper">
            <Heading
              title="Pricing"
              subTitle="Discover our solutions"
              description="Self–host your own instance of the OpenK9 search engine on–premise for free, using the community edition, or use the incoming enterprise version with support and more features. Don't want to host your own? Stay tuned for our incoming SAAS offer."
              alignment="center"
            />
            <div className={styles.pricingCards}>
              <PricingCard
                type="Community"
                price="Free"
                points={[
                  {
                    name: "Standard Base Product",
                    isChecked: true,
                  },
                  {
                    name: "Online Documentation",
                    isChecked: true,
                  },
                ]}
                isDisabled={false}
              />
              <PricingCard
                type="Enterprise"
                price="?"
                points={[
                  {
                    name: "All community features plus:",
                    isChecked: false,
                  },
                  {
                    name: "Enterprise Support",
                    isChecked: true,
                  },
                  {
                    name: "More plugins",
                    isChecked: true,
                  },
                  {
                    name: "Advanced Machine Learning",
                    isChecked: true,
                  },
                ]}
                isDisabled={true}
              />
              <PricingCard
                type="Saas"
                price="?"
                points={[
                  {
                    name: "All enterprise features plus:",
                    isChecked: false,
                  },
                  {
                    name: "Managed Hosting",
                    isChecked: true,
                  },
                  {
                    name: "Advanced Support",
                    isChecked: true,
                  },
                ]}
                isDisabled={true}
              />
            </div>
            <div className={styles.centering}>
              <Link
                className={clsx("button button--primary button--lg")}
                to="https://www.smc.it/contact-us"
              >
                Contact Us
              </Link>
            </div>
          </div>
        </section>

        {/* TODO: add action subscribe to newsletter */}
        {/* <section>
          <div className="openK9-wrapper">
            <form>
              <div className={styles.newsletter}>
                <div className={styles.newsletterHeader}>
                  <Heading
                    title="Newsletter"
                    subTitle="Subscribe our newsletter and get notifications to stay updated"
                    alignment="left"
                  />
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
        </section> */}
      </main>
    </Layout>
  );
}

export default Home;
