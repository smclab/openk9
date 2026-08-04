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
import { ModalConfirm, useToast } from "@components/Form";
import { Box, Button, Container, Typography, useTheme } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import {
  useDeleteEmbeddingModelMutation,
  useEmbeddingModelsQuery,
  useEnableEmbeddingModelMutation,
} from "../../graphql-generated";

export function EmbeddingModels() {
  const { t } = useTranslation();
  const embeddingModelsQuery = useEmbeddingModelsQuery();
  const theme = useTheme();
  const toast = useToast();
  const [deleteEmbeddingMutate] = useDeleteEmbeddingModelMutation({
    refetchQueries: ["EmbeddingModels"],
    onCompleted(data) {
      if (data.deleteEmbeddingModel?.id) {
        toast({
          title: t("pages.embedding-models.deleted-title"),
          content: t("pages.embedding-models.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.embedding-models.delete-error-content"),
        displayType: "error",
      });
    },
  });
  const navigate = useNavigate();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });

  const [updateEnableLargeLaguageModel] = useEnableEmbeddingModelMutation({
    refetchQueries: ["EmbeddingModels"],
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.embedding-models.title")}
          </Typography>
          <Typography variant="body1">{t("pages.embedding-models.description")}</Typography>
        </Box>
        <Box>
          <Link to="/embedding-model/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.embedding-models.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: embeddingModelsQuery,
            field: (data) => data?.embeddingModels,
          }}
          edgesPath="embeddingModels.edges"
          pageInfoPath="embeddingModels.pageInfo"
          rowActions={[
            {
              label: t("common.start"),
              isDisabled: (embeddingModels) => !embeddingModels?.enabled,
              action: (embeddingModel) => {
                if (embeddingModel?.id)
                  updateEnableLargeLaguageModel({
                    variables: { id: embeddingModel.id },
                  });
              },
            },
            {
              label: t("common.view"),
              action: (embeddingModels) => {
                navigate(`/embedding-model/${embeddingModels?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (embeddingModels) => {
                embeddingModels.id &&
                  navigate(`/embedding-model/${embeddingModels?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (tab) => {
                tab.id && setViewDeleteModal({ view: true, id: tab.id });
              },
            },
          ]}
          onCreatePath="new/"
          onDelete={(model) => {
            if (model?.id) deleteEmbeddingMutate({ variables: { id: model.id } });
          }}
          columns={[
            {
              header: t("common.name"),
              content: (embeddingModel) => <Box fontWeight="bolder">{embeddingModel?.name}</Box>,
            },
            {
              header: t("common.description"),
              content: (embeddingModel) => embeddingModel?.description,
            },
            {
              header: t("common.status"),
              content: (embeddingModel) => {
                const statusText = embeddingModel?.enabled ? t("common.active") : t("common.inactive");
                const backgroundColor = embeddingModel?.enabled ? theme.palette.success.main : theme.palette.grey[500];

                return (
                  <>
                    <Typography
                      variant="body2"
                      color={theme.palette.background.paper}
                      sx={{
                        borderRadius: "8px",
                        background: backgroundColor,
                        padding: "8px",
                        maxWidth: "150px",
                      }}
                    >
                      {statusText}
                    </Typography>

                    {viewDeleteModal.view && (
                      <ModalConfirm
                        title={t("modal.confirm-deletion")}
                        body={t("pages.embedding-models.delete-body")}
                        labelConfirm={t("common.delete")}
                        actionConfirm={() => {
                          deleteEmbeddingMutate({
                            variables: { id: viewDeleteModal.id || "" },
                          });
                        }}
                        close={() => setViewDeleteModal({ id: undefined, view: false })}
                      />
                    )}
                  </>
                );
              },
            },
          ]}
        />
      </Box>
    </Container>
  );
}

