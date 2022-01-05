import React from "react";
import useSWR from "swr";
import { getItemsInDatasource } from "@openk9/rest-api";
import { useLoginInfo } from "../state";

export function DSItemsCountShow({ datasourceId }: { datasourceId: number }) {
  const loginInfo = useLoginInfo();

  const { data: results } = useSWR(`getItemsInDatasource/${datasourceId}`, () =>
    getItemsInDatasource(datasourceId, loginInfo),
  );

  return <>{results === undefined ? "?" : results.total}</>;
}
