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
import { useFormatDate } from "@components/common";
import { ModalAddSingle, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography, useTheme } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import {
  useAddDataSourceToBucketMutation,
  useDataSourcesQuery,
  useDeleteDataSourceMutation,
  useUnboundBucketsByDatasourceQuery,
} from "../../graphql-generated";

export function Datasources() {
  const { t } = useTranslation();
  const formatDate = useFormatDate();
  const datasourcesQuery = useDataSourcesQuery();
  const theme = useTheme();
  const navigate = useNavigate();
  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });
  const unboundListBuckets = useUnboundBucketsByDatasourceQuery({
    variables: { datasourceId: Number(isAdd?.id) },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });
  const [addMutate] = useAddDataSourceToBucketMutation();
  const toast = useToast();
  const [deleteDataSource] = useDeleteDataSourceMutation({
    refetchQueries: ["DataSources"],
    onCompleted(data) {
      if (data.deleteDatasource?.id) {
        toast({
          title: t("pages.datasources.deleted-title"),
          content: t("pages.datasources.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.datasources.delete-error-content"),
        displayType: "error",
      });
    },
  });

  return (
    <React.Fragment>
      <Container maxWidth="xl">
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              {t("pages.datasources.title")}
            </Typography>
            <Typography variant="body1">{t("pages.datasources.description")}</Typography>
          </Box>
          <Box>
            <Link to="/data-source/new/mode/create/landingTab/0" style={{ textDecoration: "none" }}>
              <Button variant="contained" color="primary">
                {t("pages.datasources.create-new")}
              </Button>
            </Link>
          </Box>
        </Box>

        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: datasourcesQuery,
              field: (data) => data?.datasources,
            }}
            onCreatePath="/data-source/new/mode/edit"
            maxVisibleActions={2}
            onDelete={(datasources) => {}}
            edgesPath="datasources.edges"
            pageInfoPath="datasources.pageInfo"
            deleted={{
              title: t("pages.datasources.delete-title"),
              messsage: t("pages.datasources.delete-message"),
              wordConfirm: t("common.delete"),
              actionDeleted: (id: string, name: string) => {
                deleteDataSource({ variables: { id, datasourceName: name } });
              },
            }}
            rowActions={[
              {
                label: t("common.add"),
                action: (datasources) => {
                  setIsAdd({ id: datasources.id, isVisible: true });
                },
              },
              {
                label: t("common.view"),
                action: (datasources) => {
                  navigate(`/data-source/${datasources?.id}/mode/view/landingTab/monitoring`, {
                    replace: true,
                  });
                },
              },
              {
                label: t("common.edit"),
                action: (datasources) => {
                  datasources.id &&
                    navigate(`/data-source/${datasources?.id}/mode/edit/landingTab/datasource`, {
                      replace: true,
                    });
                },
              },
            ]}
            columns={[
              {
                header: t("common.name"),
                content: (datasource) => <Box fontWeight="bolder">{datasource?.name}</Box>,
              },
              {
                header: t("pages.datasources.last-ingestion-date"),
                content: (datasource) => (
                  <Typography variant="body2" className="datasource-title">
                    {formatDate(datasource?.lastIngestionDate)}
                  </Typography>
                ),
              },
              {
                header: t("pages.datasources.schedulable-reindexable"),
                content: (datasource) => {
                  const isScheduled = datasource?.schedulable;
                  const isReindex = datasource?.reindexable;

                  return (
                    <Box display="flex" gap={0.5} flexDirection={"column"} width={"fit-content"}>
                      <Typography
                        variant="body2"
                        color={theme.palette.background.paper}
                        sx={{
                          borderRadius: "8px",
                          background: isScheduled ? theme.palette.success.main : theme.palette.grey[500],
                          padding: "4px 10px",
                          fontSize: "0.75rem",
                          fontWeight: 600,
                          whiteSpace: "nowrap",
                        }}
                      >
                        {isScheduled
                          ? t("pages.datasources.schedulable-active")
                          : t("pages.datasources.schedulable-idle")}
                      </Typography>
                      <Typography
                        variant="body2"
                        color={theme.palette.background.paper}
                        sx={{
                          borderRadius: "8px",
                          background: isReindex ? theme.palette.info.main : theme.palette.grey[500],
                          padding: "4px 10px",
                          fontSize: "0.75rem",
                          fontWeight: 600,
                        }}
                      >
                        {isReindex
                          ? t("pages.datasources.reindexable-active")
                          : t("pages.datasources.reindexable-idle")}
                      </Typography>
                    </Box>
                  );
                },
              },
            ]}
          />
        </Box>
      </Container>
      {isAdd.isVisible && (
        <ModalAddSingle
          id={isAdd.id}
          list={unboundListBuckets.data?.unboundBucketsByDatasource}
          messageSuccess={t("pages.datasources.added-to-bucket")}
          title={t("pages.datasources.association-title")}
          association={({ parentId, childId, onSuccessCallback, onErrorCallback }) => {
            addMutate({
              variables: { parentId, childId },
              onCompleted: () => {
                onSuccessCallback();
              },
              onError: (error) => {
                onErrorCallback(error);
              },
            });
          }}
          callbackClose={() => {
            setIsAdd({ id: null, isVisible: false });
          }}
        />
      )}
    </React.Fragment>
  );
}
