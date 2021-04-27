/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import React, { useState } from "react";
import useSWR from "swr";
import { createUseStyles } from "react-jss";
import { useRouter } from "next/router";
import ReactFlow from "react-flow-renderer";
import { firstOrString, ThemeType } from "@openk9/search-ui-components";
import {
  getDataSourceInfo,
  getEnrichItem,
  getEnrichPipeline,
  EnrichItem,
} from "@openk9/http-api";
import { Layout } from "../../../../../components/Layout";
import { useLoginCheck, useLoginInfo } from "../../../../../state";
import { DataSourceNavBar } from "../../../../../components/DataSourceNavBar";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    margin: [theme.spacingUnit * 2, "auto"],
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    width: "100%",
    maxWidth: 1000,
    borderRadius: theme.borderRadius,
    overflow: "auto",
    padding: theme.spacingUnit * 2,
  },
  navActionButton: {
    marginLeft: theme.spacingUnit,
  },
  navMenu: {
    backgroundColor: "transparent",
  },
  reactFlowContainer: {
    height: "200px",
    borderRadius: theme.borderRadius,
    backgroundColor: theme.digitalLakeGrayLighter,
    marginTop: "4px",
  },
  title: {
    marginTop: "24px",
  },
  position: {
    position: "absolute",
    top: "4px",
    left: "8px",
  },
  nameEnrich: {
    marginBottom: "0",
  },
  alertWarning: {
    marginTop: "16px",
  },
  json: {
    marginTop: "0.2rem",
    backgroundColor: theme.digitalLakeMainL2,
    color: "white",
    padding: theme.spacingUnit * 2,
    borderRadius: theme.borderRadius,
  },
}));

function getElementsReactFlow(dsEnrichItems: any, classes: any) {
  let count = 0;
  const elements = dsEnrichItems && [
    {
      id: "0",
      type: "input",
      data: { label: "INPUT" },
      sourcePosition: "right",
      position: { x: 20, y: 80 },
    },
    ...dsEnrichItems
      .sort((a: any, b: any) => a._position - b._position)
      .map((item: any) => {
        count++;
        return {
          id: `${count}`,
          sourcePosition: "right",
          targetPosition: "left",
          type: "default",
          data: {
            label: (
              <div>
                <small className={classes.position}>{item._position}</small>
                <h6 className={classes.nameEnrich}>{item.name}</h6>
              </div>
            ),
            enrichItemId: item.enrichItemId,
          },
          position: { x: count * 200 + 20, y: 79 },
        };
      }),
    {
      id: `${count + 1}`,
      sourcePosition: "right",
      targetPosition: "left",
      type: "output",
      data: { label: "OUTPUT" },
      position: { x: (count + 1) * 200 + 20, y: 80 },
    },
  ];

  if (elements && elements.length !== 0) {
    for (let i = 1; i <= count + 1; i++) {
      elements.push({
        id: `line-${i - 1}-${i}`,
        source: `${i - 1}`,
        target: `${i}`,
        type: "smoothstep",
      });
    }
  }

  if (!dsEnrichItems) {
    return [];
  }
  return elements;
}

function Inner({ datasourceId }: { datasourceId: number }) {
  const classes = useStyles();
  const [enrichItemToView, setEnrichItemToView] = useState<EnrichItem>();

  const loginInfo = useLoginInfo();

  const { data: datasource } = useSWR(
    `/api/v2/datasource/${datasourceId}`,
    () => getDataSourceInfo(datasourceId, loginInfo),
  );

  const { data: enrichPipelines } = useSWR(`/api/v2/enrichPipeline`, () =>
    getEnrichPipeline(loginInfo),
  );

  const { data: enrichItem } = useSWR(`/api/v2/enrichItem`, () =>
    getEnrichItem(loginInfo),
  );

  const dsEnrichPipeline =
    enrichPipelines &&
    enrichPipelines.filter((e) => e.datasourceId === datasourceId)[0];

  const dsEnrichItems =
    enrichItem &&
    dsEnrichPipeline &&
    enrichItem.filter(
      (e) => e.enrichPipelineId === dsEnrichPipeline.enrichPipelineId,
    );

  const elements = getElementsReactFlow(dsEnrichItems, classes);

  function onElementClick(event: any, element: any) {
    if (!dsEnrichItems) return;
    const id = element.data.enrichItemId && element.data.enrichItemId;
    const item = dsEnrichItems.filter((item) => item.enrichItemId === id)[0];
    setEnrichItemToView(item);
  }

  if (!datasource) {
    return <span className="loading-animation" />;
  }

  if (!dsEnrichPipeline) {
    return (
      <>
        <h2>
          {datasource.datasourceId}: {datasource.name}
        </h2>
        {/* <ClayAlert
          displayType="warning"
          className={clsx(classes.alert, classes.alertWarning)}
        >
          {"There aren't items in Enrich Pipeline for this Data Source"}
        </ClayAlert> */}
      </>
    );
  }

  return (
    <>
      <h2>
        {datasource.datasourceId}: {datasource.name}
      </h2>
      <h5 className={classes.title}>Enrich Pipeline</h5>
      <div className={classes.reactFlowContainer}>
        <ReactFlow elements={elements} onElementClick={onElementClick} />
      </div>

      {enrichItemToView && Object.entries(enrichItemToView).length !== 0 && (
        <>
          <h5 className={classes.title}>{enrichItemToView.name}</h5>
          <div>
            <strong>Status:</strong>{" "}
            {enrichItemToView.active ? (
              <span className="label label-success">
                <span className="label-item label-item-expand">ENABLED</span>
              </span>
            ) : (
              <span className="label label-warning">
                <span className="label-item label-item-expand">DISABLED</span>
              </span>
            )}
          </div>
          <div>
            <strong>Position:</strong> {enrichItemToView._position}
          </div>
          <div>
            <strong>Service Name:</strong> {enrichItemToView.serviceName}
          </div>
          <div>
            <strong>JSON Configuration</strong>
            <pre className={classes.json}>
              {JSON.stringify(JSON.parse(enrichItemToView.jsonConfig), null, 4)}
            </pre>
          </div>
        </>
      )}
    </>
  );
}

function DSEnrich() {
  const classes = useStyles();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);
  const datasourceId = query.datasourceId && firstOrString(query.datasourceId);

  const { loginValid, loginInfo } = useLoginCheck();
  if (!loginValid) return <span className="loading-animation" />;

  if (!tenantId || !datasourceId) return null;

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "DataSources", path: `/tenants/${tenantId}/dataSources` },
          { label: datasourceId },
          { label: "Enrich", path: `/tenants/${tenantId}/dataSources/enrich` },
        ]}
        breadcrumbsControls={
          <DataSourceNavBar
            onReindex={() => {}}
            tenantId={parseInt(tenantId)}
            datasourceId={parseInt(datasourceId)}
          />
        }
      >
        <div className={classes.root}>
          <Inner datasourceId={parseInt(datasourceId)} />
        </div>
      </Layout>
    </>
  );
}

export default DSEnrich;
