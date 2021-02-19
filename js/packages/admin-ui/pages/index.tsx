import { createUseStyles } from "react-jss";
import clsx from "clsx";
import { add, format } from "date-fns";
import Link from "next/link";
import useSWR from "swr";
import { ThemeType } from "@openk9/search-ui-components";
import { Layout } from "../components/Layout";

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

function ServiceStatusInside() {
  const classes = useStyles();

  const { data } = useSWR(`/logs/status`, async () => {
    const req = await fetch(`/logs/status`);
    const data: {
      ID: string;
      Image: string;
      Names: string;
      Status: string;
    }[] = await req.json();
    return data;
  });

  if (!data) {
    return <span className="loading-animation" />;
  }

  const services = [
    { id: "elasticsearch", label: "ElasticSearch" },
    { id: "projectq-karaf", label: "Backend" },
    { id: "search-standalone-frontend", label: "Frontend" },
    { id: "postgres", label: "Postgres" },
    { id: "rabbitmq", label: "Queue Service" },
    { id: "docker_reverse-proxy_1", label: "Reverse Proxy" },
  ];

  return (
    <div className={classes.dataList}>
      {services.map((s) => {
        const record = data.find((d) => d.Names === s.id);
        const status = record?.Status || "Down";
        return (
          <div key={s.id}>
            <div className={classes.dataListName}>{s.label}</div>
            <div className={classes.dataListData}>
              <Link href={`/logs/${record?.ID}/`} passHref>
                {status.startsWith("Up") ? (
                  <a className="label label-success">
                    <span className="label-item label-item-expand">
                      {status}
                    </span>
                  </a>
                ) : (
                  <a className="label label-danger">
                    <span className="label-item label-item-expand">
                      {status}
                    </span>
                  </a>
                )}
              </Link>
            </div>
          </div>
        );
      })}

      <Link passHref href="/logs">
        <a className={clsx("btn btn-primary", classes.rightButton)}>
          View Logs
        </a>
      </Link>
    </div>
  );
}

function ServiceStatus() {
  const classes = useStyles();
  return (
    <div className={clsx(classes.card, classes.statsCard)}>
      <h3>Status</h3>
      <ServiceStatusInside />
    </div>
  );
}

function GlobalStats() {
  const classes = useStyles();
  return (
    <div className={clsx(classes.card, classes.statsCard)}>
      <h3>Global Stats</h3>
      <div className={classes.dataList}>
        <div>
          <div className={classes.dataListName}>Indexed Results</div>
          <div className={classes.dataListData}>3425</div>
        </div>
        <div>
          <div className={classes.dataListName}>Last Index Time</div>
          <div className={classes.dataListData}>
            {format(new Date().setMinutes(0), "dd/MM/yyyy, HH:mm")}
          </div>
        </div>
        <div>
          <div className={classes.dataListName}>Next Index Time</div>
          <div className={classes.dataListData}>
            {format(add(new Date(), { days: 2 }), "dd/MM/yyyy, HH:mm")}
          </div>
        </div>
        <div>
          <div className={classes.dataListName}>Found Entities</div>
          <div className={classes.dataListData}>235369</div>
        </div>
        <div>
          <div className={classes.dataListName}>Disk Usage</div>
          <div className={classes.dataListData}>58.23% (81 GB/120GB)</div>
        </div>
        <div>
          <div className={classes.dataListName}>RAM Usage</div>
          <div className={classes.dataListData}>81.00% (12.9 GB/16GB)</div>
        </div>
      </div>
      <a className={clsx("btn btn-primary", classes.rightButton)}>See More</a>
    </div>
  );
}

function SystemLoadChart() {
  const classes = useStyles();
  return (
    <div className={clsx(classes.card, classes.slCard)}>
      <h3>System Load</h3>
    </div>
  );
}

function Dashboard() {
  const classes = useStyles();
  return (
    <Layout breadcrumbsPath={[{ label: "Dashboard", path: "/" }]}>
      <div className={classes.wrap}>
        <QuickStart />
      </div>
      <div className={classes.wrap}>
        <GlobalStats />
        <ServiceStatus />
        {/* <SystemLoadChart /> */}
      </div>
    </Layout>
  );
}

export default Dashboard;
