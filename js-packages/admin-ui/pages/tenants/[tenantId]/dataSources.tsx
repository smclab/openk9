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

import React, { Suspense, useMemo, useState } from "react";
import { createUseStyles } from "react-jss";
import Link from "next/link";
import { useRouter } from "next/router";
import useSWR, { mutate } from "swr";
import clsx from "clsx";
import ClayIcon from "@clayui/icon";
import { ClayTooltipProvider } from "@clayui/tooltip";
import ClayDropDown from "@clayui/drop-down";

import {
  DataSourceIcon,
  firstOrString,
  pluginLoader,
  SettingsIcon,
  ThemeType,
} from "@openk9/search-ui-components";
import {
  DataSourceInfo,
  DataSourcePlugin,
  deleteDataSource,
  getDataSources,
  getPlugins,
  getSchedulerItems,
  Plugin,
  toggleDataSource,
  triggerReindex,
  triggerScheduler,
} from "@openk9/http-api";
import { Layout } from "../../../components/Layout";
import { isServer, useLoginCheck, useLoginInfo } from "../../../state";
import { ConfirmationModal } from "../../../components/ConfirmationModal";
import { useToast } from "../../_app";
import { DSItemsCountShow } from "../../../components/DSItemsCountShow";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    margin: [theme.spacingUnit * 2, "auto"],
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    width: "100%",
    maxWidth: 1200,
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
  editElement: {
    marginBottom: theme.spacingUnit * 2,
  },
}));

function DSItemRender({
  ds,
  plugin,
  tenantId,
  selected,
  onSelect,
  onToggle,
  setIdToReindex,
  setIdToSchedule,
  setIdToDelete,
}: {
  ds: DataSourceInfo;
  plugin?: Plugin<unknown>;
  tenantId: string;
  selected: boolean;
  onSelect(): void;
  onToggle(): void;
  setIdToReindex(ids: number[]): void;
  setIdToSchedule(ids: number[]): void;
  setIdToDelete(ids: number[]): void;
}) {
  const classes = useStyles();

  const [menuOpen, setMenuOpen] = useState(false);

  const dataSourcePlugin = plugin?.pluginServices.find(
    (ps) =>
      ps.type === "DATASOURCE" && ps.driverServiceName === ds.driverServiceName,
  ) as DataSourcePlugin | null;

  const Icon = dataSourcePlugin?.iconRenderer || DataSourceIcon;

  return (
    <li className="list-group-item list-group-item-flex">
      <div className="autofit-col">
        <div className="custom-control custom-checkbox">
          <label>
            <input
              className="custom-control-input"
              type="checkbox"
              checked={selected}
              onChange={onSelect}
            />
            <span className="custom-control-label"></span>
          </label>
        </div>
      </div>
      <div className="autofit-col">
        <div className="sticker">
          <span className={clsx("inline-item", classes.icon)}>
            <Icon size={32} />
          </span>
        </div>
      </div>
      <div className="autofit-col autofit-col-expand">
        <p className="list-group-title text-truncate">
          <Link
            href={`/tenants/${tenantId}/dataSources/${ds.datasourceId}/settings`}
            passHref
          >
            <a>{ds.name}</a>
          </Link>
        </p>
        <p className="list-group-subtitle text-truncate">
          {plugin?.displayName || ds.driverServiceName}
        </p>
        <div className="list-group-detail">
          {ds.active ? (
            <span className="label label-success">
              <span className="label-item label-item-expand">ENABLED</span>
            </span>
          ) : (
            <span className="label label-warning">
              <span className="label-item label-item-expand">DISABLED</span>
            </span>
          )}
          <span className="label label-inverse-light">
            <span className="label-item label-item-expand">
              <DSItemsCountShow datasourceId={ds.datasourceId} /> items
            </span>
          </span>
        </div>
      </div>
      <div className="autofit-col">
        <div className={classes.actions}>
          <ClayTooltipProvider>
            <div>
              <Link
                href={`/tenants/${tenantId}/dataSources/${ds.datasourceId}/settings`}
                passHref
              >
                <a
                  className="component-action quick-action-item"
                  role="button"
                  data-tooltip-align="top"
                  title="Settings Data Source"
                >
                  <SettingsIcon size={16} />
                </a>
              </Link>
            </div>
          </ClayTooltipProvider>
          <ClayTooltipProvider>
            <div>
              <button
                className="component-action quick-action-item"
                onClick={() => setIdToSchedule([ds.datasourceId])}
                data-tooltip-align="top"
                title="Trigger Scheduler"
              >
                <ClayIcon symbol="reload" />
              </button>
            </div>
          </ClayTooltipProvider>
          <ClayTooltipProvider>
            <div>
              <button
                className="component-action quick-action-item"
                onClick={onToggle}
                data-tooltip-align="top"
                title="Turn on/off"
              >
                <ClayIcon symbol="logout" />
              </button>
            </div>
          </ClayTooltipProvider>

          <ClayDropDown
            trigger={
              <button className="component-action">
                <ClayIcon symbol="ellipsis-v" />
              </button>
            }
            active={menuOpen}
            onActiveChange={setMenuOpen}
            alignmentPosition={3}
          >
            <ClayDropDown.ItemList>
              <ClayDropDown.Item
                onClick={() => setIdToSchedule([ds.datasourceId])}
              >
                Trigger Scheduler
              </ClayDropDown.Item>
              <ClayDropDown.Item
                onClick={() => setIdToReindex([ds.datasourceId])}
              >
                Full Reindex
              </ClayDropDown.Item>
              <ClayDropDown.Divider />
              <ClayDropDown.Item
                onClick={() => setIdToDelete([ds.datasourceId])}
              >
                Delete DataSource
              </ClayDropDown.Item>
            </ClayDropDown.ItemList>
          </ClayDropDown>
        </div>
      </div>
    </li>
  );
}

function Inside({
  tenantId,
  searchValue,
  selectedIds,
  setSelectedIds,
  setIdToReindex,
  setIdToSchedule,
  setIdToDelete,
}: {
  tenantId: string;
  searchValue: string;
  selectedIds: number[];
  setSelectedIds: React.Dispatch<React.SetStateAction<number[]>>;
  setIdToReindex(ids: number[]): void;
  setIdToSchedule(ids: number[]): void;
  setIdToDelete(ids: number[]): void;
}) {
  const classes = useStyles();
  const { pushToast } = useToast();

  const loginInfo = useLoginInfo();

  const { data: pluginInfos } = useSWR(`/api/v1/plugin`, () =>
    getPlugins(loginInfo),
  );

  const { data } = useSWR(`/api/v2/datasource`, () =>
    getDataSources(loginInfo),
  );

  const tenantDSs = useMemo(
    () => data && data.filter((d) => String(d.tenantId) === tenantId),
    [data, tenantId],
  );

  const [menuOpen, setMenuOpen] = useState(false);

  if (!data || !tenantDSs) {
    return <span className="loading-animation" />;
  }

  const filteredData = tenantDSs.filter(
    (d) =>
      d.name.includes(searchValue) ||
      d.datasourceId.toString().includes(searchValue) ||
      d.driverServiceName.includes(searchValue),
  );

  function selectAll() {
    const selectedAll = selectedIds.length === tenantDSs?.length;
    if (selectedAll) {
      setSelectedIds([]);
    } else if (tenantDSs) {
      setSelectedIds(tenantDSs.map((d) => d.datasourceId));
    }
  }

  const someSelected = selectedIds.length > 0;

  async function toggle(ids: number[]) {
    if (!tenantDSs) return;
    await Promise.all(
      ids.map((dsId) =>
        toggleDataSource(
          dsId,
          loginInfo,
          !(
            filteredData.find((ds) => ds.datasourceId === dsId)?.active || false
          ),
        ),
      ),
    );
    mutate(`/api/v2/datasource`);
    const selected = tenantDSs.filter((ds) => ids.includes(ds.datasourceId));
    const targetState =
      selected.map((ds) => ds.active).filter(Boolean).length <=
      selected.length / 2;
    pushToast(`${ids.length} items turned ${targetState ? "on" : "off"}.`);
  }

  return (
    <ul className={clsx("list-group", classes.list)}>
      <li className="list-group-header">
        <div className="custom-control custom-checkbox">
          <label>
            <input
              className="custom-control-input"
              type="checkbox"
              checked={selectedIds.length === tenantDSs.length}
              onChange={selectAll}
            />
            <span className="custom-control-label"></span>
          </label>
        </div>
        <div className={classes.actions}>
          <ClayTooltipProvider>
            <div>
              <button
                className={clsx(
                  "component-action quick-action-item",
                  !someSelected && "disabled",
                )}
                onClick={() => setIdToSchedule(selectedIds)}
                data-tooltip-align="top"
                title="Trigger Scheduler"
              >
                <ClayIcon symbol="reload" />
              </button>
            </div>
          </ClayTooltipProvider>
          <ClayTooltipProvider>
            <div>
              <button
                className={clsx(
                  "component-action quick-action-item",
                  !someSelected && "disabled",
                )}
                onClick={() => toggle(selectedIds)}
                data-tooltip-align="top"
                title="Turn on/off"
              >
                <ClayIcon symbol="logout" />
              </button>
            </div>
          </ClayTooltipProvider>

          <ClayDropDown
            trigger={
              <button
                className={clsx(
                  "component-action",
                  !someSelected && "disabled",
                )}
              >
                <ClayIcon symbol="ellipsis-v" />
              </button>
            }
            active={menuOpen}
            onActiveChange={setMenuOpen}
            alignmentPosition={3}
          >
            <ClayDropDown.ItemList>
              <ClayDropDown.Item onClick={() => setIdToSchedule(selectedIds)}>
                Trigger Scheduler
              </ClayDropDown.Item>
              <ClayDropDown.Item onClick={() => setIdToReindex(selectedIds)}>
                Full Reindex
              </ClayDropDown.Item>
              <ClayDropDown.Divider />
              <ClayDropDown.Item onClick={() => setIdToDelete(selectedIds)}>
                Delete DataSource
              </ClayDropDown.Item>
            </ClayDropDown.ItemList>
          </ClayDropDown>
        </div>
      </li>

      {filteredData.map((ds) => {
        const pluginInfo = (pluginInfos || []).find((p) =>
          ds.driverServiceName.startsWith(p.bundleInfo.symbolicName),
        );
        const plugin = pluginInfo && pluginLoader.read(pluginInfo.pluginId);

        return (
          <DSItemRender
            ds={ds}
            plugin={plugin}
            key={ds.datasourceId}
            tenantId={tenantId}
            selected={selectedIds.includes(ds.datasourceId)}
            onSelect={() =>
              setSelectedIds((ss) =>
                ss.includes(ds.datasourceId)
                  ? ss.filter((s) => s !== ds.datasourceId)
                  : [...ss, ds.datasourceId],
              )
            }
            onToggle={() => toggle([ds.datasourceId])}
            setIdToReindex={setIdToReindex}
            setIdToSchedule={setIdToSchedule}
            setIdToDelete={setIdToDelete}
          />
        );
      })}
    </ul>
  );
}

function Controls({
  tenantId,
  searchValue,
  setSearchValue,
}: {
  tenantId: string;
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
            <Link passHref href={`/tenants/${tenantId}/addDataSource`}>
              <a
                className="nav-btn nav-btn-monospaced btn btn-monospaced btn-primary"
                data-tooltip-align="bottom"
                title="Add Data Source"
              >
                <ClayIcon symbol="plus" />
              </a>
            </Link>
          </div>
        </ClayTooltipProvider>
      </li>
    </ul>
  );
}

function DataSources() {
  const classes = useStyles();
  const { pushToast } = useToast();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);

  const [searchValue, setSearchValue] = useState("");
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  const [idToReindex, setIdToReindex] = useState<number[]>([]);
  const [idToSchedule, setIdToSchedule] = useState<number[]>([]);
  const [idToDelete, setIdToDelete] = useState<number[]>([]);

  const { loginValid, loginInfo } = useLoginCheck();
  if (!loginValid) return <span className="loading-animation" />;

  if (!tenantId) return null;

  async function schedule(ids: number[]) {
    const schedulerItems = await getSchedulerItems(loginInfo);
    const schedulerItemsToRestart = schedulerItems
      .filter((job) => ids.includes(job.datasourceId))
      .map((job) => job.jobName);
    const resp = await triggerScheduler(schedulerItemsToRestart, loginInfo);
    pushToast(`Reindex requested for ${resp.length} item`);
  }

  async function reindex(ids: number[]) {
    const resp = await triggerReindex(ids, loginInfo);
    pushToast(`Full reindex requested for ${resp.length} item`);
  }

  async function doDelete(ids: number[]) {
    const resp = await Promise.all(
      ids.map((id) => deleteDataSource(id, loginInfo)),
    );
    pushToast(`${resp.length} DataSources Deleted`);
    mutate(`/api/v2/datasource`);
  }

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "DataSources", path: `/tenants/${tenantId}/dataSources` },
        ]}
        breadcrumbsControls={
          <Controls
            tenantId={tenantId}
            searchValue={searchValue}
            setSearchValue={setSearchValue}
          />
        }
      >
        <div className={classes.root}>
          {!isServer && (
            <Suspense fallback={<span className="loading-animation" />}>
              <Inside
                tenantId={tenantId}
                searchValue={searchValue}
                selectedIds={selectedIds}
                setSelectedIds={setSelectedIds}
                setIdToReindex={setIdToReindex}
                setIdToSchedule={setIdToSchedule}
                setIdToDelete={setIdToDelete}
              />
            </Suspense>
          )}
        </div>
      </Layout>

      {idToSchedule && idToSchedule.length !== 0 && (
        <ConfirmationModal
          title="Trigger Scheduler"
          message="Are you sure you want to trigger the scheduler for the selected data sources? This will look for new data and may take some time."
          abortText="Abort"
          confirmText="Trigger"
          onCloseModal={() => setIdToSchedule([])}
          onConfirmModal={() => schedule(idToSchedule)}
        />
      )}
      {idToReindex && idToReindex.length !== 0 && (
        <ConfirmationModal
          title="Full Reindex"
          message="Are you sure you want to reindex the selected data sources? This will delete ALL the previously indexed data and may take a long time."
          abortText="Abort"
          confirmText="Reindex"
          onCloseModal={() => setIdToReindex([])}
          onConfirmModal={() => reindex(idToReindex)}
        />
      )}
      {idToDelete && idToDelete.length !== 0 && (
        <ConfirmationModal
          title="Delete"
          message="Are you sure you want to delete the selected data sources? This will delete ALL the previously indexed data."
          abortText="Abort"
          confirmText="Delete"
          onCloseModal={() => setIdToDelete([])}
          onConfirmModal={() => doDelete(idToDelete)}
        />
      )}
    </>
  );
}

export default DataSources;
