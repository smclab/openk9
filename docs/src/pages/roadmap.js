import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import styles from "./styles.module.css";
import { Milestone } from "../components/Milestone";

const milestones = [  
  {
    completed: true,
    title: "v1.7.1",
    date: "Jun 20, 2024",
    imageSrc: "",
    releaseUrl: "https://github.com/smclab/openk9/releases/tag/v1.7.1",
    notes: "",
    description: (
      <ul>
        <li>I18n support</li>
        <li>Support to deploy connectors using ArgoCD</li>
        <li>Added autocomplete to Query Analysis</li>
        <li>New components for Search Frontend library</li>
      </ul>
    ),
  },
  {
    completed: true,
    title: "v2.0.0",
    date: "Oct 22, 2024",
    imageSrc: "",
    releaseUrl: "https://github.com/smclab/openk9/releases/tag/v2.0.0",
    notes: "",
    description: (
      <ul>
        <li>
          Migrating from Elasticsearch to Opensearch
        </li>
        <li>Support for Vector and Hybrid search</li>
        <li>Retrieval Augmented Generation integration</li>
      </ul>
    ),
  },
  {
    completed: false,
    title: "v3.0.0",
    date: "will be released approximately at the end of April 2025",
    imageSrc: "",
    notes: "Following aspects ",
    description: (
      <ul>
        <li>New Admin UI</li>
        <li>New frontend for RAG Generative AI integration</li>
        <li>Migration from quarkus 2.x to Quarkus 3.x</li>
        <li>Optimizations for vector data ingestion</li>
      </ul>
    ),
  },
  {
    completed: false,
    title: "v4.0.0",
    date: "will be released approximately at the end of November 2025",
    imageSrc: "",
    notes: "It will be packed with new features as it will contain approximately 6 months of work, whereas our usual previews contain 2 month of work.",
    description: (
      <p></p>
    ),
  },
];

function Roadmap() {
  return (
    <Layout title="OpenK9">
      <header>
        <div className="openK9-wrapper">
          <h1 className={styles.pageTitle}>Roadmap</h1>
            Here you can find our product roadmap.
            We work realising LTS version approximately every 4/6 months. 
            During period between two LTS versions we release also preview versions (approximately every 2 months).<br></br><br></br>
            <p>To check previous versions view <a href="https://github.com/smclab/openk9/releases">Releases</a> on Github Repository.
            <br></br>
            For every release is possibile to search on Github Wiki for release detailed changelog, migration guide and compatibility matrix.
        <p>End of life information are also provided.</p>
        </p>
        </div>
      </header>
      <main>
        <div className="openK9-wrapper">
          {milestones &&
            milestones.length > 0 &&
            milestones.map((milestone, idx) => (
              <Milestone key={idx} {...milestone} leftImage={false} />
            ))}
        </div>
      </main>
      <footer>
      <div className="openK9-wrapper">
          <h2>Questions?
          </h2>
          <div className={styles.pageDescription}>
          If you have any questions about this plan, feel free to ask in the comments of this blog post or on GitHub Discussions.
          </div>
          <br></br>
          <h2>Come Join Us</h2>
          <div className={styles.pageDescription}>
          We value your feedback a lot so please report bugs, ask for improvements…​ Let’s build something great together!
          If you are a Openk9 user or just curious, don’t be shy and join our welcoming community:
          <br></br>          <br></br>

          <ul>
        <li>provide feedback on GitHub.</li>
        <li>ask your questions on Stack Overflow.</li>
      </ul>
          </div>
        </div>
        </footer>
    </Layout>
  );
}

export default Roadmap;
