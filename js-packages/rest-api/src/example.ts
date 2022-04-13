import { OpenK9Client } from "./index";

const client = OpenK9Client({ tenant: "https://demo.openk9.io" });

client.doSearch({
  searchQuery: [{ tokenType: "TEXT", values: ["palazzo"], filter: false }],
  range: [0, 2],
});
