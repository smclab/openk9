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

import { createUseStyles } from "react-jss";
import { ThemeType } from "@openk9/search-ui-components";
import { Layout } from "../components/Layout";
import { useLoginCheck } from "../state";

const useStyles = createUseStyles((theme: ThemeType) => ({
  wrap: {
    display: "flex",
    flexWrap: "wrap",
  },
  card: {
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    borderRadius: theme.borderRadius,
    padding: theme.spacingUnit * 3,
    margin: [theme.spacingUnit * 2, theme.spacingUnit * 4],
    maxWidth: 865,
  },
  statsCard: {
    width: 400,
  },
  rightButton: {
    marginTop: theme.spacingUnit * 2,
    float: "right",
  },
  slCard: {
    flexGrow: 1,
  },
  dataList: {
    flexGrow: 1,
    "& div": {
      marginBottom: "0.2rem",
      display: "flex",
    },
  },
  dataListName: {
    fontWeight: 700,
    flexGrow: 1,
  },
  dataListData: {},
}));

function QuickStart() {
  const classes = useStyles();
  return (
    <div className={classes.card}>
      <h1>Welcome to OpenK9</h1>
      <h3>QuickStart Guide</h3>
      <p>
        Before using the search engine, you need to configure a{" "}
        <strong>Tenant</strong> and some <strong>Data Sources</strong>. Some
        Data Sources plugins are already installed, but you can install more of
        them in the <a href="#">Plugin Manager</a>. You can start with an empty{" "}
        <strong>Enrich Pipeline</strong> or add some pre-made modules.
      </p>
      <p>
        If you want to test the search engine, you can easilly add some sample
        data by using the button below. Refer to the{" "}
        <a href="#">Documentation</a> for more info about how to set up your
        search system.
      </p>
      <button className="btn btn-primary">Load Sample Data</button>
    </div>
  );
}

function Dashboard() {
  const classes = useStyles();

  const { loginValid } = useLoginCheck();
  if (!loginValid) return <span className="loading-animation" />;

  return (
    <Layout breadcrumbsPath={[{ label: "Dashboard", path: "/" }]}>
      <div className={classes.wrap}>
        <QuickStart />
      </div>
      <div className={classes.wrap}>
      </div>
    </Layout>
  );
}

export default Dashboard;
