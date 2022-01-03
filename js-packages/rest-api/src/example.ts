import * as restApi from "./index";

restApi.doSearch(
  {
    range: [0, 2],
    searchQuery: [{ tokenType: "TEXT", values: ["palazzo"] }],
  },
  null,
);
