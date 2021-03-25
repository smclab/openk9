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

import React, { Suspense, useEffect, useState } from "react";
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import { ClayTooltipProvider } from "@clayui/tooltip";
import { useRouter } from "next/router";
import { firstOrString, ThemeType } from "@openk9/search-ui-components";
import { Layout } from "../../../components/Layout";
import { isServer } from "../../../state";
import { getTenant, putTenant } from "@openk9/http-api";
import useSWR, { mutate } from "swr";
import ClayForm, { ClayInput } from "@clayui/form";
import ClayButton from "@clayui/button";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    margin: [theme.spacingUnit * 2, "auto"],
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    width: "100%",
    maxWidth: 1000,
    borderRadius: theme.borderRadius,
    overflow: "auto",

    "& thead": {
      position: "sticky",
      top: 0,
      borderTopLeftRadius: theme.borderRadius,
      borderTopRightRadius: theme.borderRadius,
      zIndex: 1000,
    },
  },
  actions: {
    display: "flex",
    justifyContent: "flex-end",
  },
  list: {
    marginBottom: 0,
  },
  icon: {
    fill: "currentColor",
    fontSize: 30,
  },
  alert: {
    "& .alert-autofit-row": {
      alignItems: "center",
    },
  },
}));

function Controls({
  searchValue,
  setSearchValue,
}: {
  searchValue: string;
  setSearchValue(s: string): void;
}) {
  return (
    <ul className="navbar-nav" style={{ marginRight: 16 }}>
      <div className="navbar-form navbar-form-autofit navbar-overlay navbar-overlay-sm-down">
        <div className="container-fluid container-fluid-max-xl">
          <div className="input-group">
            <div className="input-group-item">
              <input
                className="form-control form-control input-group-inset input-group-inset-after"
                type="text"
                value={searchValue}
                onChange={(e) => setSearchValue(e.target.value)}
              />
              <span className="input-group-inset-item input-group-inset-item-after">
                {searchValue && searchValue.length > 0 && (
                  <button
                    className="navbar-breakpoint-d-none btn btn-monospaced btn-unstyled"
                    type="button"
                    onClick={() => setSearchValue("")}
                  >
                    <ClayIcon symbol="times" />
                  </button>
                )}
                <button
                  className="btn btn-monospaced btn-unstyled"
                  type="submit"
                >
                  <ClayIcon symbol="search" />
                </button>
              </span>
            </div>
          </div>
        </div>
      </div>
      <li className="nav-item">
        <ClayTooltipProvider>
          <div>
            <button
              className="nav-btn nav-btn-monospaced btn btn-monospaced btn-primary"
              type="button"
              data-tooltip-align="bottom"
              title="Add Data Source"
            >
              <ClayIcon symbol="plus" />
            </button>
          </div>
        </ClayTooltipProvider>
      </li>
    </ul>
  );
}

function Inside({ tenantId }) {
  console.log(tenantId);

  const { data } = useSWR(`/api/v2/tenant/${tenantId}`);

  console.log(data);

  const [tenant, setTenant] = useState({ ...data });

  useEffect(() => {
    if (data) {
      setTenant({ ...data });
    }
  }, [data, setTenant]);

  if (!data) {
    return <span className="loading-animation" />;
  }

  const handleChange = (e) => {
    const value = e.target.value;
    const id = e.target.id;
    setTenant((cs) => ({
      ...cs,
      [id]: value,
    }));
  };

  const handleSave = async () => {
    await putTenant(tenant);
    mutate(`/api/v2/tenant`);
  };

  return (
    <>
      {
        <div className="sheet">
          <div className="sheet-section">
            <div className="form-group-autofit">
              <div className="form-group-item">
                <label>Name</label>
                <input
                  className="form-control"
                  id="name"
                  placeholder="Name"
                  type="text"
                  value={tenant.name}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group-item">
                <label>VirtuaHost</label>
                <input
                  className="form-control"
                  id="virtualHost"
                  placeholder="VirtualHost"
                  type="text"
                  value={tenant.virtualHost}
                  onChange={handleChange}
                />
              </div>
            </div>
          </div>
          <div className="sheet-footer sheet-footer-btn-block-sm-down">
            <div className="btn-group">
              <div className="btn-group-item">
                <button
                  className="btn btn-primary"
                  type="button"
                  onClick={handleSave}
                >
                  {"Save"}
                </button>
              </div>
            </div>
          </div>
        </div>
      }
    </>
  );
}

function Settings() {
  const classes = useStyles();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "Tenant Settings", path: `/tenants/${tenantId}/settings` },
        ]}
      >
        <div className={classes.root}>
          {!isServer && (
            <Suspense fallback={<span className="loading-animation" />}>
              <Inside
                tenantId={tenantId}
                /*onPerformAction={(label) =>
                  setToastItems((tt) => [
                    ...tt,
                    { label, key: Math.random().toFixed(5) },
                  ])
                }*/
              />
            </Suspense>
          )}
        </div>
      </Layout>
    </>
  );
}

export default Settings;
