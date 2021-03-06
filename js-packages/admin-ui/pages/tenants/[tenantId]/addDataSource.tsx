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

import React, { Suspense, useState } from "react";
import { createUseStyles } from "react-jss";
import { useRouter } from "next/router";
import { mutate } from "swr";
import { firstOrString, ThemeType } from "@openk9/search-ui-components";
import { DataSourceInfo, postDataSource } from "@openk9/http-api";
import { isServer, useLoginCheck } from "../../../state";
import { Layout } from "../../../components/Layout";
import { EditDataSource } from "../../../components/EditDataSource";

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
}));

function AddDataSource() {
  const classes = useStyles();

  const { query, push } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);

  const [editingDataSource, setEditingDataSource] = useState<
    Omit<DataSourceInfo, "datasourceId" | "lastIngestionDate" | "tenantId">
  >({
    active: true,
    description: "",
    jsonConfig: "{}",
    name: "",
    scheduling: "0 */30 * ? * *",
    driverServiceName: "",
  });

  const { loginValid, loginInfo } = useLoginCheck();
  if (!loginValid) return <span className="loading-animation" />;

  if (!tenantId) {
    return null;
  }

  const fullDataSourceInfo: DataSourceInfo = {
    ...editingDataSource,
    datasourceId: -1,
    lastIngestionDate: 0,
    tenantId: parseInt(tenantId),
  };

  async function handleSave() {
    await postDataSource(fullDataSourceInfo, loginInfo);
    mutate(`/api/v2/datasource`);
    push(`/tenants/${tenantId}/dataSources`);
  }

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "DataSources", path: `/tenants/${tenantId}/dataSources` },
          { label: "Add New" },
        ]}
      >
        <div className={classes.root}>
          {!isServer && (
            <Suspense fallback={<span className="loading-animation" />}>
              <EditDataSource
                editingDataSource={fullDataSourceInfo}
                onChange={setEditingDataSource}
                onAbort={() => push(`/tenants/${tenantId}/dataSources`)}
                onSave={handleSave}
              />
            </Suspense>
          )}
        </div>
      </Layout>
    </>
  );
}

export default AddDataSource;
