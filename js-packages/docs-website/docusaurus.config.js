module.exports = {
  title: "OpenK9",
  tagline: "Search. Everywhere.",
  url: "https://openk9.io",
  baseUrl: "/",
  onBrokenLinks: "warn",
  onBrokenMarkdownLinks: "warn",
  favicon: "img/favicon.ico",
  organizationName: "smc",
  projectName: "openk9",
  themeConfig: {
    colorMode: {
      disableSwitch: true,
    },
    metadata: [
      {
        name: "description",
        content:
          "OpenK9 is a complete Cognitive Enterprise Search solution that fits all your needs. Powerful, Modern and Flexible, it employs Machine Learning to enrich your data and give the best experience possible.",
      },
      {
        name: "keywords",
        content:
          "openk9,open,source,search,enterprise,ai,machine,learning,cognitive,intelligent,enrich",
      },
      { name: "twitter:image", content: "https://openk9.io/img/logo.png" },
      { name: "twitter:image:alt", content: "OpenK9 Logo" },
      { name: "twitter:card", content: "summary" },
      { name: "twitter:site", content: "@K9Open" },
      {
        name: "twitter:description",
        content:
          "OpenK9 is a complete Cognitive Enterprise Search solution that fits all your needs. Powerful, Modern and Flexible, it employs Machine Learning to enrich your data and give the best experience possible.",
      },
      { property: "og:title", content: "OpenK9 Cognitive Enterprise Search" },
      {
        property: "og:description",
        content:
          "OpenK9 is a complete Cognitive Enterprise Search solution that fits all your needs. Powerful, Modern and Flexible, it employs Machine Learning to enrich your data and give the best experience possible.",
      },
      { property: "og:image", content: "https://openk9.io/logo.png" },
      { property: "og:url", content: "https://openk9.io/" },
    ],
    navbar: {
      title: "OpenK9",
      logo: {
        alt: "OpenK9 Logo",
        src: "img/logo.svg",
      },
      items: [
        {
          to: "docs/",
          activeBasePath: "docs",
          label: "Docs",
          position: "left",
        },
        {
          to: "docs/api",
          activeBasePath: "api",
          label: "Api",
          position: "left",
        },
        {
          to: "docs/architecture",
          activeBasePath: "architecture",
          label: "Architecture",
          position: "left",
        },
        {
          to: "comingsoon/",
          activeBasePath: "plugins",
          label: "Plugins",
          position: "left",
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
              label: "API",
              to: "/api",
            },
            {
              label: "Plugins",
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
  },
  presets: [
    "@docusaurus/plugin-google-analytics",
    [
      "@docusaurus/plugin-sitemap",
      {
        cacheTime: 600 * 1000,
        changefreq: "weekly",
        priority: 0.5,
        trailingSlash: false,
      },
    ],
    [
      "@docusaurus/preset-classic",
      {
        docs: {
          sidebarPath: require.resolve("./sidebars.js"),
          editUrl:
            "https://github.com/smclab/openk9/tree/main/js-packages/docs-website/",
        },
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
        googleAnalytics: {
          trackingID: "UA-191444663-1",
          anonymizeIP: true,
        },
      },
    ],
  ],
};
