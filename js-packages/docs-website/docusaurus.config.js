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
    metadatas: [{ name: "twitter:card", content: "summary" }],
    announcementBar: {
      id: "beta",
      content:
        "We are still in alpha phase, everything you see may change at every time. Feel free to try our product right now!",
      backgroundColor: "#f9f9f9",
      textColor: "#505050",
      isCloseable: true,
    },
    navbar: {
      title: "OpenK9",
      logo: {
        alt: "OpenK9 Logo",
        src: "img/logo.svg",
      },
      items: [
        { to: "features", label: "Features", position: "left" },
        { to: "pricing", label: "Pricing", position: "left" },
        {
          to: "docs/",
          activeBasePath: "docs",
          label: "Docs",
          position: "left",
        },
        {
          to: "api/",
          activeBasePath: "api",
          label: "API",
          position: "left",
        },
        { to: "plugins", label: "Plugins", position: "left" },
        {
          href: "https://github.com/smclab/openk9",
          label: "GitHub",
          position: "right",
        },
      ],
    },
    footer: {
      style: "dark",
      links: [
        {
          title: "Docs",
          items: [
            {
              label: "Style Guide",
              to: "docs/",
            },
            {
              label: "Second Doc",
              to: "docs/doc2/",
            },
          ],
        },
        {
          title: "More",
          items: [
            {
              label: "Blog",
              to: "blog",
            },
            {
              label: "GitHub",
              href: "https://github.com/smclab/openk9",
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} SMC Treviso s.r.l.`,
    },
  },
  presets: [
    [
      "@docusaurus/preset-classic",
      {
        docs: {
          sidebarPath: require.resolve("./sidebars.js"),
          editUrl: "https://github.com/smclab/openk9/edit/master/website/",
        },
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      },
    ],
  ],
};
