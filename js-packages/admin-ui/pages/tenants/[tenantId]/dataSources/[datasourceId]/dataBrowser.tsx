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

import React from "react";
import { createUseStyles } from "react-jss";
import { useRouter } from "next/router";
import useSWR from "swr";

import { firstOrString, ThemeType } from "@openk9/search-ui-components";
import { getDataSourceInfo } from "@openk9/http-api";

import { DataSourceNavBar } from "../../../../../components/DataSourceNavBar";
import { Layout } from "../../../../../components/Layout";
import { useLoginCheck, useLoginInfo } from "../../../../../state";

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
  settingHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
}));

function Inner({
  tenantId,
  datasourceId,
}: {
  tenantId: number;
  datasourceId: number;
}) {
  const classes = useStyles();

  const loginInfo = useLoginInfo();

  const {
    data: datasource,
    mutate,
  } = useSWR(`/api/v2/datasource/${datasourceId}`, () =>
    getDataSourceInfo(datasourceId, loginInfo),
  );

  if (!datasource) {
    return <span className="loading-animation" />;
  }

  return (
    <>
      <div className={classes.settingHeader}>
        <h2>
          {datasource.datasourceId}: {datasource.name}
        </h2>
      </div>
    </>
  );
}

function DSDataBrowser() {
  const classes = useStyles();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);
  const datasourceId = query.datasourceId && firstOrString(query.datasourceId);
  const dataSourceInt = parseInt(datasourceId || "NaN");

  const { loginValid, loginInfo } = useLoginCheck();
  if (!loginValid) return <span className="loading-animation" />;

  if (isNaN(dataSourceInt) || !tenantId || !datasourceId) {
    return null;
  }

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "DataSources", path: `/tenants/${tenantId}/dataSources` },
          { label: datasourceId },
          {
            label: "DataBrowser",
            path: `/tenants/${tenantId}/dataSources/dataBrowser`,
          },
        ]}
        breadcrumbsControls={
          <DataSourceNavBar
            onReindex={() => {}}
            tenantId={parseInt(tenantId)}
            datasourceId={dataSourceInt}
          />
        }
      >
        <div className={classes.root}>
          <Inner tenantId={parseInt(tenantId)} datasourceId={dataSourceInt} />
        </div>
      </Layout>
    </>
  );
}

export default DSDataBrowser;
