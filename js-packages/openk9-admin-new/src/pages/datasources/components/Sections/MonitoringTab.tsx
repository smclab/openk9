import { useRestClient } from "@components/queryClient";
import RefreshIcon from "@mui/icons-material/Refresh";
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import {
  SchedulerStatus,
  useDataSourceInformationQuery,
  useQDatasourceSchedulersQuery,
} from "../../../../graphql-generated";
import { formatOffsetDateTime } from "../../../../utils/formatOffsetDateTime";
import React from "react";
import { useToast } from "@components/Form";

export function MonitoringTab({ id }: { id: string }) {
  const [schedulingId, setSchedulingId] = React.useState<number>(-1);
  const [modalMessage, setModalMessage] = React.useState<string>("");
  const [modalAction, setModalAction] = React.useState<string>("");
  const [open, setOpen] = React.useState<boolean>(false);
  const [isRefreshing, setIsRefreshing] = React.useState<boolean>(false);
  const [schedulers, setSchedulers] = React.useState<any[]>([]);
  const navigate = useNavigate();

  const dataSourceInformationQuery = useDataSourceInformationQuery({
    variables: { id },
  });
  const dataSourceSchedulers = useQDatasourceSchedulersQuery({
    variables: { id },
    skip: !id || id === "new",
  });

  const [docsCount, setDocsCount] = React.useState<number>(0);
  const [docsDeleted, setDocsDeleted] = React.useState<number>(0);
  const [storeSize, setStoreSize] = React.useState<number>(0);
  const restClient = useRestClient();

  const handleClose = () => {
    setOpen(false);
    setModalMessage("");
  };

  const handleAction = async () => {
    try {
      if (modalAction === "closeScheduling") {
        await restClient.schedulerResource.postApiDatasourceSchedulersCloseScheduling(schedulingId);
      } else if (modalAction === "cancelScheduling") {
        await restClient.schedulerResource.postApiDatasourceSchedulersCancelScheduling(schedulingId);
      } else if (modalAction === "rerouteScheduling") {
        await restClient.schedulerResource.postApiDatasourceSchedulersRerouteScheduling(schedulingId);
      }
      handleClose();
      setIsRefreshing(true);
      await handleRefresh();
    } catch (error) {
      console.error("Error performing action:", error);
    } finally {
      setIsRefreshing(false);
    }
  };

  const renderStatus = (status: SchedulerStatus) => {
    switch (status) {
      case SchedulerStatus.Running:
      case SchedulerStatus.Finished:
        return (
          <Typography variant="body2" color="success.main">
            {status}
          </Typography>
        );
      case SchedulerStatus.Error:
      case SchedulerStatus.Cancelled:
        return (
          <Typography variant="body2" color="error.main">
            {status}
          </Typography>
        );
      default:
        return (
          <Typography variant="body2" color="textSecondary">
            {status}
          </Typography>
        );
    }
  };

  const renderActions = (item: any) => {
    const handleViewInfoClick = () => {
      navigate(`/notificationInfo/${item.node.id}`);
    };

    if (item?.node?.status === SchedulerStatus.Running) {
      return (
        <>
          <Button onClick={() => handleOpen("closeScheduling", item.node.id)}>Close</Button>
          <Button onClick={() => handleOpen("cancelScheduling", item.node.id)}>Cancel</Button>
          <Button onClick={handleViewInfoClick}>View Info</Button>
        </>
      );
    } else if (item?.node?.status === SchedulerStatus.Error) {
      return (
        <>
          <Button onClick={() => handleOpen("closeScheduling", item.node.id)}>Close</Button>
          <Button onClick={() => handleOpen("cancelScheduling", item.node.id)}>Cancel</Button>
          <Button onClick={() => handleOpen("rerouteScheduling", item.node.id)}>Reroute</Button>
          <Button onClick={handleViewInfoClick}>View Info</Button>
        </>
      );
    } else {
      return <Button onClick={handleViewInfoClick}>View Info</Button>;
    }
  };

  const handleOpen = (action: string, id: number) => {
    setSchedulingId(id);
    setModalMessage(`Do you want to ${action.replace("Scheduling", "").toLowerCase()} this scheduling?`);
    dataSourceInformationQuery.refetch();
    dataSourceSchedulers.refetch();
    setModalAction(action);
    setOpen(true);
  };

  const handleRefresh = async () => {
    setIsRefreshing(true);
    await dataSourceInformationQuery.refetch();
    await dataSourceSchedulers.refetch();
    setIsRefreshing(false);
  };

  React.useEffect(() => {
    if (dataSourceInformationQuery.data && dataSourceInformationQuery.data.datasource?.dataIndex?.cat) {
      setDocsCount(Number(dataSourceInformationQuery.data?.datasource?.dataIndex?.cat?.docsCount || "0"));
      setDocsDeleted(Number(dataSourceInformationQuery.data?.datasource?.dataIndex?.cat?.docsDeleted || "0"));
      setStoreSize(Number(dataSourceInformationQuery.data?.datasource?.dataIndex?.cat?.priStoreSize || "0"));
    }
  }, [dataSourceInformationQuery.data]);

  React.useEffect(() => {
    if (dataSourceSchedulers.data && dataSourceSchedulers.data.datasource?.schedulers?.edges) {
      setSchedulers(dataSourceSchedulers.data.datasource.schedulers.edges);
    }
  }, [dataSourceSchedulers.data]);

  if (dataSourceSchedulers.loading || dataSourceInformationQuery.loading) {
    return null;
  }
  return (
    <>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>Confirm Action</DialogTitle>
        <DialogContent>
          <DialogContentText>{modalMessage}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} color="secondary">
            Cancel
          </Button>
          <Button onClick={handleAction} color="primary">
            Apply
          </Button>
        </DialogActions>
      </Dialog>

      <Box sx={{ display: "flex", alignItems: "center", gap: "10px" }}>
        <Typography variant="h2">Your datasource</Typography>
        <Button onClick={handleRefresh} disabled={isRefreshing}>
          {isRefreshing ? <CircularProgress size={24} /> : <RefreshIcon />}
        </Button>
      </Box>

      <div style={{ display: "flex", gap: "16px" }}>
        <Card>
          <CardContent>
            <Typography variant="h6">Document counts</Typography>
            <Typography variant="h2">{docsCount}</Typography>
          </CardContent>
        </Card>
        <Card>
          <CardContent>
            <Typography variant="h6">Document deleted</Typography>
            <Typography variant="h2">{docsDeleted}</Typography>
          </CardContent>
        </Card>
        <Card>
          <CardContent>
            <Typography variant="h6">Store size (MB)</Typography>
            <Typography variant="h2">{storeSize}</Typography>
          </CardContent>
        </Card>
      </div>

      <Typography variant="h5" gutterBottom style={{ marginTop: "24px" }}>
        Data source's activities
      </Typography>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Activity</TableCell>
              <TableCell>Time spent</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {schedulers.length > 0 ? (
              schedulers.map((item, index) => (
                <TableRow key={index}>
                  <TableCell>{item?.node?.__typename}</TableCell>
                  <TableCell>{item?.node?.modifiedDate}</TableCell>
                  <TableCell>{item?.node?.status && renderStatus(item.node.status)}</TableCell>
                  <TableCell>{renderActions(item)}</TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={4} sx={{ py: 5, textAlign: "center" }}>
                  <Box
                    sx={{
                      display: "flex",
                      flexDirection: "column",
                      alignItems: "center",
                      justifyContent: "center",
                      py: 4,
                    }}
                  >
                    <Typography variant="h6" gutterBottom>
                      No activities
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      There are no matching unassociated activities
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}

export function useReindexMutation() {
  const restClient = useRestClient();
  const toast = useToast();
  return useMutation(
    async ({
      datasourceId,
      startIngestionDate,
      reindex,
    }: {
      datasourceId: string;
      startIngestionDate: string;
      reindex: boolean;
    }) => {
      const response = await restClient.triggerWithDateResource?.postApiDatasourceV2Trigger({
        datasourceId: Number(datasourceId),
        reindex,
        startIngestionDate: formatOffsetDateTime(startIngestionDate),
      });
      return { status: response?.status || "unknown" };
    },
    {
      onSuccess: (data: { status: string }) => {
        const status = data.status;
        const isSuccess = status === "ON_SCHEDULING";
        toast({
          title: `Reindex completed with status: ${status} ${!isSuccess ? "view monitoring area" : ""}`,
          content: ``,
          displayType: isSuccess ? "success" : "info",
        });
      },
    },
  );
}

export function useTriggerSchedulerMutation() {
  const restClient = useRestClient();
  return useMutation(async (datasourceId: string) => {
    await restClient.triggerResource.postApiDatasourceV1Trigger({
      datasourceIds: [Number(datasourceId)],
    });
  });
}

export function useGenerateDocumentTypesMutation() {
  const restClient = useRestClient();
  return useMutation(async (datasourceId: string) => {
    await restClient.dataIndexResource.postApiDatasourceV1DataIndexAutoGenerateDocTypes({
      datasourceId: Number(datasourceId),
    });
  });
}
