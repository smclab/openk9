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

import { useState } from "react";
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import { ClayTooltipProvider } from "@clayui/tooltip";
import Link from "next/link";
import useSWR from "swr";
import {
  DataSourceIcon,
  SettingsIcon,
  ThemeType,
  UsersIcon,
} from "@openk9/search-ui-components";
import { getTenants } from "@openk9/http-api";
import { Layout } from "../components/Layout";
import ClayButton from "@clayui/button";
import ClayModal, { useModal } from "@clayui/modal";

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
}));

function TBody({ searchValue }: { searchValue: string }) {
  const classes = useStyles();

  const { data } = useSWR(`/api/v2/tenant`, getTenants);

  if (!data) {
    return <span className="loading-animation" />;
  }

  const filteredData = data.filter(
    (d) =>
      d.name.includes(searchValue) ||
      d.tenantId.toString().includes(searchValue) ||
      d.virtualHost.includes(searchValue),
  );

  return (
    <tbody>
      {filteredData.map((ten) => (
        <tr key={ten.tenantId}>
          <td>{ten.tenantId}</td>
          <td className="table-cell-expand ">
            <p className="table-list-title">{ten.name}</p>
          </td>
          <td className="table-cell-expand">{ten.virtualHost}</td>
          <td>
            <div className={classes.actions}>
              <ClayTooltipProvider>
                <div>
                  <Link href={`/tenants/${ten.tenantId}/settings/`} passHref>
                    <a
                      className="component-action quick-action-item"
                      role="button"
                      data-tooltip-align="top"
                      title="Tenant Settings"
                    >
                      <SettingsIcon size={16} />
                    </a>
                  </Link>
                </div>
              </ClayTooltipProvider>
              <ClayTooltipProvider>
                <div>
                  <Link href={`/tenants/${ten.tenantId}/users/`} passHref>
                    <a
                      className="component-action quick-action-item"
                      role="button"
                      data-tooltip-align="top"
                      title="Users"
                    >
                      <UsersIcon size={16} />
                    </a>
                  </Link>
                </div>
              </ClayTooltipProvider>
              <ClayTooltipProvider>
                <div>
                  <Link href={`/tenants/${ten.tenantId}/dataSources/`} passHref>
                    <a
                      className="component-action quick-action-item"
                      role="button"
                      data-tooltip-align="top"
                      title="Data Sources"
                    >
                      <DataSourceIcon size={16} />
                    </a>
                  </Link>
                </div>
              </ClayTooltipProvider>
              <a className="component-action" role="button">
                <ClayIcon symbol="ellipsis-v" />
              </a>
            </div>
          </td>
        </tr>
      ))}
    </tbody>
  );
}

function Controls({
  searchValue,
  setSearchValue,
}: {
  searchValue: string;
  setSearchValue(s: string): void;
}) {
  const [visible, setVisible] = useState(false);

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
        <AddModal visible={visible} handleClose={() => setVisible(false)} />
            <button
              className="nav-btn nav-btn-monospaced btn btn-monospaced btn-primary"
              type="button"
              data-tooltip-align="bottom"
              title="Add Tenant"
              onClick={() => setVisible(true)}
            >
              <ClayIcon symbol="plus" />
            </button>
          </div>
        </ClayTooltipProvider>
      </li>
    </ul>
  );
}

function Tenants() {
  const classes = useStyles();

  const [searchValue, setSearchValue] = useState("");

  return (
    <Layout
      breadcrumbsPath={[{ label: "Tenants", path: "/tenants" }]}
      breadcrumbsControls={
        <Controls searchValue={searchValue} setSearchValue={setSearchValue} />
      }
    >
      <div className={classes.root}>
        <table className="table table-autofit table-nowrap">
          <thead>
            <tr>
              <th>ID</th>
              <th className="table-cell-expand">Name</th>
              <th className="table-cell-expand">VirtualHost</th>
              <th></th>
            </tr>
          </thead>

          <TBody searchValue={searchValue} />
        </table>
      </div>
    </Layout>
  );
}

function AddModal({ visible, handleClose }) {
  const { observer, onClose } = useModal({
    onClose: handleClose,
  });

  return (
    <>
      {visible && (
        <ClayModal observer={observer} size="lg" status="info">
          <ClayModal.Header>{"Title"}</ClayModal.Header>
          <ClayModal.Body>
            <h1>{"Hello world!"}</h1>
          </ClayModal.Body>
          <ClayModal.Footer
            first={
              <ClayButton.Group spaced>
                <ClayButton displayType="secondary">{"Cancel"}</ClayButton>
              </ClayButton.Group>
            }
            last={<ClayButton onClick={onClose}>{"Save"}</ClayButton>}
          />
        </ClayModal>
      )}
    </>
  );
}

export default Tenants;
