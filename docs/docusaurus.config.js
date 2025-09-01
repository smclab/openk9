// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Openk9 site',
  tagline: 'Search. Everywhere. Intelligently.',
  favicon: 'img/favicon.ico',

  // Set the production url of your site here
  url: 'https://your-docusaurus-site.example.com',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'facebook', // Usually your GitHub org/user name.
  projectName: 'docusaurus', // Usually your repo name.

  onBrokenLinks: 'ignore',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/',
        },
        blog: {
          showReadingTime: true,
          feedOptions: {
            type: ['rss', 'atom'],
            xslt: true,
          },
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/',
          // Useful options to enforce blogging best practices
          onInlineTags: 'warn',
          onInlineAuthors: 'warn',
          onUntruncatedBlogPosts: 'warn',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
    [
      'redocusaurus',
      {
        // Plugin Options for loading OpenAPI files
        specs: [
          // Pass it a path to a local OpenAPI YAML file
          {
            // Redocusaurus will automatically bundle your spec into a single file during the build
            id: 'api-searcher',
            spec: 'openapi-yaml/searcher-openapi.yaml',
            route: '/api/searcher',
          },          {
            // Redocusaurus will automatically bundle your spec into a single file during the build
            id: 'api-rag',
            spec: 'openapi-yaml/rag-openapi.json',
            route: '/api/rag',
          },          {
            // Redocusaurus will automatically bundle your spec into a single file during the build
            id: 'api-datasource',
            spec: 'openapi-yaml/datasource-openapi.yaml',
            route: '/api/datasource'
          },
          {
            // Redocusaurus will automatically bundle your spec into a single file during the build
            id: 'api-tenant-manager',
            spec: 'openapi-yaml/tenant-manager-openapi.yaml',
            route: '/api/tenant-manager'
          },
          {
            // Redocusaurus will automatically bundle your spec into a single file during the build
            id: 'api-file-manager',
            spec: 'openapi-yaml/file-manager-openapi.yaml',
            route: '/api/file-manager'
          },
          {
            // Redocusaurus will automatically bundle your spec into a single file during the build
            id: 'api-ingestion',
            spec: 'openapi-yaml/ingestion-openapi.yaml',
            route: '/api/ingestion'
          }
        ],
        // Theme Options for modifying how redoc renders them
        theme: {
          // Change with your site colors
          primaryColor: '#1890ff',
        },
      },
    ]
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      // Replace with your project's social card
      image: 'img/docusaurus-social-card.jpg',
      navbar: {
        title: 'Openk9',
        logo: {
          alt: "OpenK9 Logo",
          src: "img/logo.svg",
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'sidebar',
            position: 'left',
            label: 'Docs',
          },
        {
          to: "docs/architecture",
          activeBasePath: "architecture",
          label: "Architecture",
          position: "left",
        },
          {
            to: "plugins/",
            activeBasePath: "plugins",
            label: "Connectors",
            position: "left",
          },
          {
            to: "roadmap/",
            activeBasePath: "roadmap",
            label: "Roadmap",
            position: "left",
          },
          {
            href: "https://techblog.smc.it/en",
            position: "left",
            label: "Blog",
          },
          {
            href: "https://twitter.com/k9open",
            position: "right",
            className: "header-twitter-link",
            "aria-label": "Twitter profile",
          },
          {
            href: "https://github.com/smclab/openk9",
            position: "right",
            className: "header-github-link",
            "aria-label": "GitHub repository",
          },
          {
            href: 'https://github.com/facebook/docusaurus',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: "dark",
        logo: {
          alt: "SMC Logo",
          src: "img/smc.svg",
          href: "https://smc.it",
        },
        links: [
          {
            title: "Company",
            items: [
              {
                label: "SMC Corporate",
                href: "https://smc.it",
              },
              {
                label: "Liferay Partner of the Year",
                href: "https://liferaypartneritalia.smc.it",
              },
              {
                label: "Tech Blog",
                href: "https://techblog.smc.it",
              },
              {
                label: "Careers",
                href: "https://www.smc.it/lavora-con-noi",
              },
              {
                label: "Contact Us",
                href: "https://www.smc.it/contatta-smc",
              },
            ],
          },
          {
            title: "Product",
            items: [
              {
                label: "Docs",
                to: "/docs",
              },
              {
                label: "Connectors",
                to: "/comingsoon",
              },
            ],
          },
          {
            title: "Community",
            items: [
              {
                label: "GitHub",
                href: "https://github.com/smclab/openk9",
              },
              {
                label: "Twitter",
                href: "https://twitter.com/k9open",
              },
              {
                label: "Privacy Policy",
                to: "/privacy",
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} <a href="https://smc.it/" class="footer__link-item">SMC Treviso s.r.l.</a>`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
    }),
};

export default config;
