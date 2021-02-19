import React, { useMemo, useState } from "react";
import { createUseStyles } from "react-jss";
import clsx from "clsx";
import ClayIcon from "@clayui/icon";
import ClayAlert from "@clayui/alert";
import Link from "next/link";
import { useRouter } from "next/router";
import useSWR from "swr";
import {
  DXPLogo,
  EmailIcon,
  firstOrString,
  OSLogo,
  SettingsIcon,
  ThemeType,
} from "@openk9/search-ui-components";
import { Layout } from "../../../components/Layout";
import { DSItem } from "../../../types";

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
  alert: {
    "& .alert-autofit-row": {
      alignItems: "center",
    },
  },
}));

const dsConfig = {
  "it.rios.projectq.plugins.email.driver.EmailPluginDriver": {
    name: "Email Plugin",
    icon: <EmailIcon size={30} />,
  },
  "it.rios.projectq.plugins.spaces.driver.SpacesPluginDriver": {
    name: "Spaces Plugin",
    icon: <OSLogo size={30} />,
  },
  "it.rios.projectq.plugins.applications.driver.ApplicationPluginDriver": {
    name: "Applications Plugin",
    icon: <ClayIcon symbol="desktop" />,
  },
  "it.rios.projectq.plugins.liferay.driver.LiferayPluginDriver": {
    name: "Liferay Plugin",
    icon: <DXPLogo size={32} />,
  },
};

function DSItemRender({
  ds,
  tenantId,
  selected,
  onSelect,
  onReindex,
  onToggle,
}: {
  ds: DSItem;
  tenantId: string;
  selected: boolean;
  onSelect(): void;
  onReindex(): void;
  onToggle(): void;
}) {
  const classes = useStyles();

  const dsInfo = dsConfig[ds.driverServiceName] || {
    name: ds.driverServiceName,
    icon: "",
  };

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
            {dsInfo.icon}
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
        <p className="list-group-subtitle text-truncate">{dsInfo.name}</p>
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
        </div>
      </div>
      <div className="autofit-col">
        <div className={classes.actions}>
          <Link
            href={`/tenants/${tenantId}/dataSources/${ds.datasourceId}/settings`}
            passHref
          >
            <a className="component-action quick-action-item" role="button">
              <SettingsIcon size={16} />
            </a>
          </Link>
          <button
            className="component-action quick-action-item"
            onClick={onReindex}
          >
            <ClayIcon symbol="reload" />
          </button>
          <button
            className="component-action quick-action-item"
            onClick={onToggle}
          >
            <ClayIcon symbol="logout" />
          </button>
          <button className="component-action">
            <ClayIcon symbol="ellipsis-v" />
          </button>
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
  onPerformAction,
}: {
  tenantId: string;
  searchValue: string;
  selectedIds: number[];
  setSelectedIds: React.Dispatch<React.SetStateAction<number[]>>;
  onPerformAction(s: string): void;
}) {
  const classes = useStyles();

  const { data } = useSWR(`/api/v2/datasource`, async () => {
    const req = await fetch(`/api/v2/datasource`);
    const data: DSItem[] = await req.json();
    return data;
  });

  const tenantDSs = useMemo(
    () => data && data.filter((d) => String(d.tenantId) === tenantId),
    [data, tenantId],
  );

  if (!data) {
    return <span className="loading-animation" />;
  }

  const filteredData = tenantDSs.filter(
    (d) =>
      d.name.includes(searchValue) ||
      d.tenantId.toString().includes(searchValue) ||
      d.driverServiceName.includes(searchValue),
  );

  function selectAll() {
    const selectedAll = selectedIds.length === tenantDSs.length;
    if (selectedAll) {
      setSelectedIds([]);
    } else {
      setSelectedIds(tenantDSs.map((d) => d.datasourceId));
    }
  }

  const someSelected = selectedIds.length > 0;
  // async function reindex(ids: number[]) {
  //   // TODO: Can this be done without this call, using the ids directly?
  //   const schedulerJobsReq = await fetch(`/api/v1/scheduler`);
  //   const schedulerJobsResp: {
  //     jobName: string;
  //     datasourceId: number;
  //     scheduling: string;
  //     datasourceName: string;
  //   }[] = await schedulerJobsReq.json();
  //   const schedulerJobs = schedulerJobsResp
  //     .filter((job) => ids.includes(job.datasourceId))
  //     .map((job) => job.jobName);

  //   const req = await fetch(`/api/v1/scheduler/trigger`, {
  //     method: "POST",
  //     headers: { ContentType: "application/json" },
  //     body: JSON.stringify(schedulerJobs),
  //   });
  //   const resp = await req.json();
  //   console.log(resp);
  //   onPerformAction(`Reindex requested for ${ids.length} items.`);
  // }
  async function reindex(ids: number[]) {
    const req = await fetch(`/api/v1/index/reindex`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ datasourceIds: ids }),
    });
    const resp = await req.text();
    console.log(resp);
    onPerformAction(`Reindex requested for ${ids.length} items.`);
  }

  async function toggle(ids: number[]) {
    const selected = tenantDSs.filter((ds) => ids.includes(ds.datasourceId));
    const targetState =
      selected.map((ds) => ds.active).filter(Boolean).length <=
      selected.length / 2;
    onPerformAction(
      `${ids.length} items turned ${targetState ? "on" : "off"}.`,
    );
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
          <button
            className={clsx(
              "component-action quick-action-item",
              !someSelected && "disabled",
            )}
            onClick={() => reindex(selectedIds)}
          >
            <ClayIcon symbol="reload" />
          </button>
          <button
            className={clsx(
              "component-action quick-action-item",
              !someSelected && "disabled",
            )}
            onClick={() => toggle(selectedIds)}
          >
            <ClayIcon symbol="logout" />
          </button>
          <button
            className={clsx("component-action", !someSelected && "disabled")}
          >
            <ClayIcon symbol="ellipsis-v" />
          </button>
        </div>
      </li>

      {filteredData.map((ds) => (
        <DSItemRender
          ds={ds}
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
          onReindex={() => reindex([ds.datasourceId])}
          onToggle={() => toggle([ds.datasourceId])}
        />
      ))}
    </ul>
  );
}

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
        <button
          className="nav-btn nav-btn-monospaced btn btn-monospaced btn-primary"
          type="button"
        >
          <ClayIcon symbol="plus" />
        </button>
      </li>
    </ul>
  );
}

function DataSources() {
  const classes = useStyles();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);

  const [searchValue, setSearchValue] = useState("");
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [toastItems, setToastItems] = useState<
    { label: string; key: string }[]
  >([]);

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "DataSources", path: `/tenants/${tenantId}/dataSources` },
        ]}
        breadcrumbsControls={
          <Controls searchValue={searchValue} setSearchValue={setSearchValue} />
        }
      >
        <div className={classes.root}>
          <Inside
            tenantId={tenantId}
            searchValue={searchValue}
            selectedIds={selectedIds}
            setSelectedIds={setSelectedIds}
            onPerformAction={(label) =>
              setToastItems((tt) => [
                ...tt,
                { label, key: Math.random().toFixed(5) },
              ])
            }
          />
        </div>
      </Layout>

      <ClayAlert.ToastContainer>
        {toastItems.map((value) => (
          <ClayAlert
            displayType="success"
            className={classes.alert}
            autoClose={5000}
            key={value.key}
            onClose={() => {
              setToastItems((prevItems) =>
                prevItems.filter((item) => item.key !== value.key),
              );
            }}
          >
            {value.label}
          </ClayAlert>
        ))}
      </ClayAlert.ToastContainer>
    </>
  );
}

export default DataSources;
