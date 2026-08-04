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
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { ModalConfirm } from "@components/Form";
import { Table } from "../../components/Table/Table";
import { useDeleteEnrichPipelineMutation, useEnrichPipelinesQuery } from "../../graphql-generated";

export function Pipelines() {
  const { t } = useTranslation();
  const pipelinesQuery = useEnrichPipelinesQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteEnrichPipelineMutate] = useDeleteEnrichPipelineMutation({
    refetchQueries: ["EnrichPipelines"],
    onCompleted(data) {
      if (data.deleteEnrichPipeline?.id) {
        toast({
          title: t("pages.pipelines.deleted-title"),
          content: t("pages.pipelines.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.pipelines.delete-error-content"),
        displayType: "error",
      });
    },
  });

  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });

  const isLoading = pipelinesQuery.loading;

  if (isLoading) {
    return null;
  }

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.pipelines.title")}
          </Typography>
          <Typography variant="body1">{t("pages.pipelines.description")}</Typography>
        </Box>
        <Box>
          <Link to="/pipeline/new/mode/edit" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.pipelines.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: pipelinesQuery,
            field: (data) => data?.enrichPipelines,
          }}
          onCreatePath="/pipeline/new/mode/edit"
          edgesPath="pipelines.edges"
          pageInfoPath="pipelines.pageInfo"
          onDelete={(pipelines) => {
            if (pipelines?.id)
              deleteEnrichPipelineMutate({
                variables: { id: pipelines.id },
              });
          }}
          rowActions={[
            {
              label: t("common.view"),
              action: (pipelines) => {
                pipelines.id &&
                  navigate(`/pipeline/${pipelines.id}/mode/view`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.edit"),
              action: (pipelines) => {
                pipelines.id &&
                  navigate(`/pipeline/${pipelines.id}/mode/edit`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (pipeline) => {
                pipeline.id && setViewDeleteModal({ view: true, id: pipeline.id });
              },
            },
          ]}
          columns={[
            {
              header: t("common.name"),
              content: (pipeline) => <Box fontWeight="bolder">{pipeline?.name}</Box>,
            },
            {
              header: t("common.description"),
              content: (pipeline) => (
                <Typography variant="body2" className="pipeline-title">
                  {pipeline?.description}
                </Typography>
              ),
            },
            {
              header: t("common.priority"),
              content: (pipeline: any) => (
                <Typography variant="body2" className="pipeline-title">
                  {pipeline?.priority}
                </Typography>
              ),
            },
          ]}
        />
      </Box>

      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.pipelines.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteEnrichPipelineMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

