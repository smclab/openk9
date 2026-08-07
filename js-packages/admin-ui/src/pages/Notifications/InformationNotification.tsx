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
import { Box, Button, Container, TextField, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation();
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
          {t("pages.notifications.title")}
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
                {t("common.cancel")}
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
                {t("common.close")}
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
                {t("pages.datasources.monitoring.reprocess-failed-messages")}
              </Button>
            )}
          </Box>
        )}
      </Box>
      <Typography variant="body1" paragraph>
        {t("pages.notifications.description")}
      </Typography>

      <Box component="form" noValidate autoComplete="off">
        <TextField
          label={t("pages.notifications.schedule-id")}
          id="scheduleId"
          fullWidth
          margin="normal"
          value={info?.scheduleId || ""}
          InputProps={{ readOnly: true }}
        />
        <TextField
          label={t("pages.notifications.create-date")}
          id="createDate"
          fullWidth
          margin="normal"
          value={info?.createDate || ""}
          InputProps={{ readOnly: true }}
        />
        <TextField
          label={t("pages.datasources.monitoring.modified-date")}
          id="modifiedDate"
          fullWidth
          margin="normal"
          value={info?.modifiedDate || ""}
          InputProps={{ readOnly: true }}
        />
        <TextField
          label={t("pages.datasources.last-ingestion-date")}
          id="lastIngestionDate"
          fullWidth
          margin="normal"
          value={info?.lastIngestionDate || ""}
          InputProps={{ readOnly: true }}
        />
        <TextField
          label={t("common.status")}
          id="status"
          fullWidth
          margin="normal"
          value={info?.status || ""}
          InputProps={{ readOnly: true }}
        />
        {(info?.status === "ERROR" || info?.status === "FAILURE") && (
          <TextField
            label={t("pages.notifications.error-description")}
            id="errorDescription"
            fullWidth
            margin="normal"
            value={info?.errorDescription || ""}
            InputProps={{ readOnly: true }}
            multiline
            minRows={4}
          />
        )}
      </Box>

      <Button variant="outlined" color="inherit" onClick={() => navigate(-1)}>
        {t("common.back")}
      </Button>
    </Container>
  );
};

