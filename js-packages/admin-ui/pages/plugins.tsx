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
import Link from "next/link";
import useSWR from "swr";
import ClayIcon from "@clayui/icon";
import ClayButton from "@clayui/button";
import ClayModal, { useModal } from "@clayui/modal";
import { ClayTooltipProvider } from "@clayui/tooltip";
import { pluginLoader, ThemeType } from "@openk9/search-ui-components";
import { getPlugins, PluginInfo } from "@openk9/http-api";
import { Layout } from "../components/Layout";
import { isServer, useLoginCheck, useLoginInfo } from "../state";

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

    "& td": {
      whiteSpace: "normal",
    },
  },
  actions: {
    display: "flex",
    justifyContent: "flex-end",
  },
  dropArea: {
    margin: theme.spacingUnit,
    borderRadius: 16,
    border: `2px dashed ${theme.digitalLakeMainL3}`,
    color: theme.digitalLakeMainL3,
    height: 300,
    display: "flex",
    alignItems: "center",
    textAlign: "center",
    justifyContent: "center",
  },
}));

function AddModal({
  visible,
  handleClose,
}: {
  visible: boolean;
  handleClose(): void;
}) {
  const classes = useStyles();

  const { observer, onClose } = useModal({
    onClose: handleClose,
  });

  if (!visible) return null;

  function handleDrag(e: React.DragEvent) {
    e.preventDefault();
    console.log(e);
    return false;
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    console.log(e);
    return false;
  }

  return (
    <ClayModal observer={observer} size="lg" status="info">
      <ClayModal.Header>Install Plugin</ClayModal.Header>
      <ClayModal.Body>
        <div
          className={classes.dropArea}
          onDragOver={handleDrag}
          onDrop={handleDrop}
        >
          Drop a JAR file here
        </div>
      </ClayModal.Body>
      <ClayModal.Footer
        last={
          <ClayButton.Group spaced>
            <ClayButton displayType="secondary" onClick={onClose}>
              Cancel
            </ClayButton>
          </ClayButton.Group>
        }
      />
    </ClayModal>
  );
}

function Controls({
  searchValue,
  setSearchValue,
}: {
  searchValue: string;
  setSearchValue(s: string): void;
}) {
  const [addDialogVisible, setAddDialogVisible] = useState(false);

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
            <AddModal
              visible={addDialogVisible}
              handleClose={() => setAddDialogVisible(false)}
            />
            <button
              className="nav-btn nav-btn-monospaced btn btn-monospaced btn-primary"
              type="button"
              data-tooltip-align="bottom"
              title="Add Plugin"
              onClick={() => setAddDialogVisible(true)}
            >
              <ClayIcon symbol="plus" />
            </button>
          </div>
        </ClayTooltipProvider>
      </li>
    </ul>
  );
}

function TRow({ pluginInfo }: { pluginInfo: PluginInfo }) {
  const classes = useStyles();

  const plugin = pluginLoader.read(pluginInfo.pluginId);

  return (
    <tr>
      <td className="table-cell-expand">{plugin.displayName}</td>
      <td className="table-cell-expand">{pluginInfo.pluginId}</td>
      <td className="table-cell-expand">
        <p className="table-list-title">{pluginInfo.bundleInfo.symbolicName}</p>
      </td>
      <td>
        {pluginInfo.bundleInfo.state.startsWith("ACTIVE") ? (
          <span className="label label-success">
            <span className="label-item label-item-expand">
              {pluginInfo.bundleInfo.state}
            </span>
          </span>
        ) : (
          <span className="label label-warning">
            <span className="label-item label-item-expand">
              {pluginInfo.bundleInfo.state}
            </span>
          </span>
        )}
      </td>
      <td>
        <div className={classes.actions}>
          <ClayTooltipProvider>
            <div>
              <Link href={`/plugins/${pluginInfo.pluginId}/`} passHref>
                <a
                  className="disabled component-action quick-action-item"
                  role="button"
                  data-tooltip-align="top"
                  title="Delete Plugin"
                >
                  <ClayIcon symbol="trash" />
                </a>
              </Link>
            </div>
          </ClayTooltipProvider>
        </div>
      </td>
    </tr>
  );
}

function TBody({ searchValue }: { searchValue: string }) {
  const loginInfo = useLoginInfo();

  const { data } = useSWR(`/plugins`, () => getPlugins(loginInfo));

  if (!data) {
    return <span className="loading-animation" />;
  }

  const filteredData = data.filter(
    (d) =>
      d.pluginId.includes(searchValue) ||
      d.bundleInfo.symbolicName.includes(searchValue),
  );

  return (
    <tbody>
      {filteredData.map((pluginInfo) =>
        isServer ? null : (
          <Suspense fallback={<span className="loading-animation" />}>
            <TRow key={pluginInfo.pluginId} pluginInfo={pluginInfo} />
          </Suspense>
        ),
      )}
    </tbody>
  );
}

function Plugins() {
  const classes = useStyles();

  const [searchValue, setSearchValue] = useState("");

  const { loginValid } = useLoginCheck();
  if (!loginValid) return <span className="loading-animation" />;

  return (
    <Layout
      breadcrumbsPath={[{ label: "Plugins", path: "/plugins" }]}
      breadcrumbsControls={
        <Controls searchValue={searchValue} setSearchValue={setSearchValue} />
      }
    >
      <div className={classes.root}>
        <table className="table table-autofit table-nowrap">
          <thead>
            <tr>
              <th className="table-cell-expand">Display Name</th>
              <th className="table-cell-expand">Plugin ID</th>
              <th className="table-cell-expand">Symbolic Name</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <TBody searchValue={searchValue} />
        </table>
      </div>
    </Layout>
  );
}

export default Plugins;
