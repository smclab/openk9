import { sortPresets } from "../Step2Security";

describe("sortPresets", () => {
  it("orders presets by the canonical security configuration order", () => {
    const input = [{ name: "NO_GATEWAY_AUTH" }, { name: "OAUTH2_SEARCH" }, { name: "OAUTH2_ADMIN_ONLY" }];
    expect(sortPresets(input).map((p) => p.name)).toEqual(["OAUTH2_ADMIN_ONLY", "OAUTH2_SEARCH", "NO_GATEWAY_AUTH"]);
  });

  it("produces the same order regardless of input order", () => {
    const canonical = ["OAUTH2_ADMIN_ONLY", "OAUTH2_SEARCH", "OAUTH2_SEARCH_WITH_API_KEY", "OAUTH2_ADMIN_WITH_API_KEY", "NO_GATEWAY_AUTH"];
    const shuffled = [{ name: "OAUTH2_ADMIN_WITH_API_KEY" }, { name: "OAUTH2_ADMIN_ONLY" }, { name: "NO_GATEWAY_AUTH" }, { name: "OAUTH2_SEARCH_WITH_API_KEY" }, { name: "OAUTH2_SEARCH" }];
    expect(sortPresets(shuffled).map((p) => p.name)).toEqual(canonical);
  });

  it("appends unknown keys after known ones, alphabetically", () => {
    const input = [{ name: "ZZZ_UNKNOWN" }, { name: "AAA_UNKNOWN" }, { name: "OAUTH2_ADMIN_ONLY" }];
    expect(sortPresets(input).map((p) => p.name)).toEqual(["OAUTH2_ADMIN_ONLY", "AAA_UNKNOWN", "ZZZ_UNKNOWN"]);
  });

  it("does not mutate the input array", () => {
    const input = [{ name: "NO_GATEWAY_AUTH" }, { name: "OAUTH2_ADMIN_ONLY" }];
    sortPresets(input);
    expect(input.map((p) => p.name)).toEqual(["NO_GATEWAY_AUTH", "OAUTH2_ADMIN_ONLY"]);
  });
});
