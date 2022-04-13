import React from "react";
import useSWR from "swr";
import { client } from "./client";

export function DSItemsCountShow({ datasourceId }: { datasourceId: number }) {
  const { data: results } = useSWR(`getItemsInDatasource/${datasourceId}`, () =>
    client.doSearchDatasource({ range: [0, 0], searchQuery: [] }, datasourceId),
  );

  return <>{results === undefined ? "?" : results.total}</>;
}
