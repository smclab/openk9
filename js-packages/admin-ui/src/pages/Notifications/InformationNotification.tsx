import { Box, Button, Container, TextField, Typography } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import { useRestClient } from "@components/queryClient";
import { useSchedulerQuery } from "../../graphql-generated";

const allowedActionsByStatus: Record<string, string[]> = {
  RUNNING: ["CANCEL", "CLOSE"],
  ERROR: ["CANCEL", "CLOSE", "REPROCESS"],
  STALE: ["CANCEL", "CLOSE"],
  FAILURE: [],
  CANCELLED: [],
  FINISHED: [],
};

export const InformationNotification = () => {
  const { notificationId = "" } = useParams();
  const navigate = useNavigate();
  const { loading, data } = useSchedulerQuery({
    variables: { id: notificationId as string },
  });
  const restClient = useRestClient();

  if (loading) return null;

  const info = data?.scheduler;
  const allowedActions = allowedActionsByStatus[info?.status || ""] || [];

  return (
    <Container>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 2,
        }}
      >
        <Typography component="h1" variant="h1" fontWeight="600">
          Notification Information
        </Typography>
        {notificationId && (
          <Box sx={{ display: "flex", gap: 1 }}>
            {allowedActions.includes("CANCEL") && (
              <Button
                variant="outlined"
                color="primary"
                onClick={async () => {
                  await restClient.schedulerResource.postApiDatasourceSchedulersCancelScheduling(
                    Number(notificationId),
                  );
                }}
              >
                Cancel
              </Button>
            )}

            {allowedActions.includes("CLOSE") && (
              <Button
                variant="outlined"
                color="primary"
                onClick={async () => {
                  await restClient.schedulerResource.postApiDatasourceSchedulersCloseScheduling(Number(notificationId));
                }}
              >
                Close
              </Button>
            )}

            {allowedActions.includes("REPROCESS") && (
              <Button
                variant="outlined"
                color="primary"
                onClick={async () => {
                  await restClient.schedulerResource.postApiDatasourceSchedulersRerouteScheduling(
                    Number(notificationId),
                  );
                }}
              >
                Reprocess failed messages
              </Button>
            )}
          </Box>
        )}
      </Box>
      <Typography variant="body1" paragraph>
        Description page
      </Typography>

      <Box component="form" noValidate autoComplete="off">
        <TextField
          label="Schedule ID"
          id="scheduleId"
          fullWidth
          margin="normal"
          value={notificationId}
          InputProps={{ readOnly: true }}
        />
        <TextField
          label="Create Date"
          id="createDate"
          fullWidth
          margin="normal"
          value={info?.createDate || ""}
          InputProps={{ readOnly: true }}
        />
        <TextField
          label="Modified Date"
          id="modifiedDate"
          fullWidth
          margin="normal"
          value={info?.modifiedDate || ""}
          InputProps={{ readOnly: true }}
        />
        <TextField
          label="Last Ingestion Date"
          id="lastIngestionDate"
          fullWidth
          margin="normal"
          value={info?.lastIngestionDate || ""}
          InputProps={{ readOnly: true }}
        />
        <TextField
          label="Status"
          id="status"
          fullWidth
          margin="normal"
          value={info?.status || ""}
          InputProps={{ readOnly: true }}
        />
        <TextField
          label="Error Description"
          id="errorDescription"
          fullWidth
          margin="normal"
          value={info?.errorDescription || ""}
          InputProps={{ readOnly: true }}
          multiline
          minRows={4}
        />
      </Box>

      <Button variant="outlined" color="inherit" onClick={() => navigate(-1)}>
        Back
      </Button>
    </Container>
  );
};
