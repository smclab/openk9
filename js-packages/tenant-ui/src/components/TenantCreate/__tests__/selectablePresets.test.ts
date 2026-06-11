import { selectablePresets } from "../Step2Security";

describe("selectablePresets", () => {
  it("removes NO_GATEWAY_AUTH when devMode is off", () => {
    const input = [{ name: "OAUTH2_ADMIN_ONLY" }, { name: "NO_GATEWAY_AUTH" }, { name: "OAUTH2_SEARCH" }];
    expect(selectablePresets(input, false).map((p) => p.name)).toEqual(["OAUTH2_ADMIN_ONLY", "OAUTH2_SEARCH"]);
  });

  it("keeps NO_GATEWAY_AUTH when devMode is on", () => {
    const input = [{ name: "OAUTH2_ADMIN_ONLY" }, { name: "NO_GATEWAY_AUTH" }, { name: "OAUTH2_SEARCH" }];
    expect(selectablePresets(input, true).map((p) => p.name)).toEqual(["OAUTH2_ADMIN_ONLY", "NO_GATEWAY_AUTH", "OAUTH2_SEARCH"]);
  });

  it("returns all presets when NO_GATEWAY_AUTH is absent", () => {
    const input = [{ name: "OAUTH2_ADMIN_ONLY" }, { name: "OAUTH2_SEARCH_WITH_API_KEY" }];
    expect(selectablePresets(input, false)).toHaveLength(2);
  });
});
