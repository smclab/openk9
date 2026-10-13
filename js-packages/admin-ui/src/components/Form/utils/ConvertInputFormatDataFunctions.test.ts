import { convertToBackEndFormatData, convertToInputFormat } from "./ConvertInputFormatDataFunctions";

// Jest cannot change the timezone from inside a test file, so these checks
// hold in any timezone: before the fix they failed in every zone but UTC.
describe("the partial reindex date field works in the browser's local time", () => {
  test("sending the proposed value back sends the last ingestion", () => {
    expect(convertToBackEndFormatData(convertToInputFormat("2026-09-29T15:25:17.252Z"))).toBe(
      "2026-09-29T15:25:00.000Z",
    );
  });

  test("in winter too", () => {
    expect(convertToBackEndFormatData(convertToInputFormat("2026-01-15T09:30:45.000Z"))).toBe(
      "2026-01-15T09:30:00.000Z",
    );
  });

  test("a datasource never ingested proposes the epoch and can still be sent", () => {
    expect(convertToBackEndFormatData(convertToInputFormat(null))).toBe("1970-01-01T00:00:00.000Z");
  });
});
