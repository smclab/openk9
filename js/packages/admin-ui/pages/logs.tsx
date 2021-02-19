import { createUseStyles } from "react-jss";
import { ThemeType } from "@openk9/search-ui-components";
import ClayIcon from "@clayui/icon";
import Link from "next/link";
import useSWR from "swr";
import { Layout } from "../components/Layout";

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
}));

function TBody() {
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

  return (
    <tbody>
      {data.map((container) => (
        <tr key={container.ID}>
          <td>{container.ID}</td>
          <td className="table-cell-expand">
            <p className="table-list-title">{container.Names}</p>
          </td>
          <td className="table-cell-expand">{container.Image}</td>
          <td>
            {container.Status.startsWith("Up") ? (
              <span className="label label-success">
                <span className="label-item label-item-expand">
                  {container.Status}
                </span>
              </span>
            ) : (
              <span className="label label-warning">
                <span className="label-item label-item-expand">
                  {container.Status}
                </span>
              </span>
            )}
          </td>
          <td>
            <div className={classes.actions}>
              <Link href={`/logs/${container.ID}/`} passHref>
                <a className="component-action quick-action-item" role="button">
                  <ClayIcon symbol="forms" />
                </a>
              </Link>
            </div>
          </td>
        </tr>
      ))}
    </tbody>
  );
}

function Logs() {
  const classes = useStyles();
  return (
    <Layout breadcrumbsPath={[{ label: "Logs", path: "/logs" }]}>
      <div className={classes.root}>
        <table className="table table-autofit table-nowrap">
          <thead>
            <tr>
              <th>ID</th>
              <th className="table-cell-expand">Name</th>
              <th className="table-cell-expand">Image</th>
              <th>Status</th>
              <th>Logs</th>
            </tr>
          </thead>
          <TBody />
        </table>
      </div>
    </Layout>
  );
}

export default Logs;
