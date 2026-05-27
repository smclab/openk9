import { selectablePresets } from "../Step2Security";

describe("selectablePresets", () => {
  it("removes NO_GATEWAY_AUTH and preserves the other presets in order", () => {
    const input = [{ name: "OAUTH2_ADMIN_ONLY" }, { name: "NO_GATEWAY_AUTH" }, { name: "OAUTH2_SEARCH" }];
    expect(selectablePresets(input).map((p) => p.name)).toEqual(["OAUTH2_ADMIN_ONLY", "OAUTH2_SEARCH"]);
  });

  it("returns all presets when NO_GATEWAY_AUTH is absent", () => {
    const input = [{ name: "OAUTH2_ADMIN_ONLY" }, { name: "OAUTH2_SEARCH_WITH_API_KEY" }];
    expect(selectablePresets(input)).toHaveLength(2);
  });
});
