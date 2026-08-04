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
  useDeleteLargeLanguageModelMutation,
  useEnableLargeLanguageModelMutation,
  useLargeLanguageModelsQuery,
} from "../../graphql-generated";

export function LargeLanguageModels() {
  const { t } = useTranslation();
  const largeLanguageModelsQuery = useLargeLanguageModelsQuery();
  const theme = useTheme();
  const toast = useToast();
  const [deleteTabMutate] = useDeleteLargeLanguageModelMutation({
    refetchQueries: ["LargeLanguageModels"],
    onCompleted(data) {
      if (data.deleteLargeLanguageModel?.id) {
        toast({
          title: t("pages.large-language-models.deleted-title"),
          content: t("pages.large-language-models.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.large-language-models.delete-error-content"),
        displayType: "error",
      });
    },
  });
  const navigate = useNavigate();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });

  const [updateEnableLargeLaguageModel] = useEnableLargeLanguageModelMutation({
    refetchQueries: ["LargeLanguageModels"],
  });

  const [deleteModelsMutate] = useDeleteLargeLanguageModelMutation({
    refetchQueries: ["LargeLanguageModels"],
    onCompleted(data) {
      if (data.deleteLargeLanguageModel?.id) {
      }
    },
    onError(error) {},
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.large-language-models.title")}
          </Typography>
          <Typography variant="body1">{t("pages.large-language-models.description")}</Typography>
        </Box>
        <Box>
          <Link to="/large-language-model/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.large-language-models.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: largeLanguageModelsQuery,
            field: (data) => data?.largeLanguageModels,
          }}
          edgesPath="largeLanguageModels.edges"
          pageInfoPath="largeLanguageModels.pageInfo"
          rowActions={[
            {
              label: t("common.start"),

              isDisabled: (largeLanguageModel) => !largeLanguageModel?.enabled,
              action: (largeLanguage) => {
                if (largeLanguage?.id)
                  updateEnableLargeLaguageModel({
                    variables: { id: largeLanguage.id },
                  });
              },
            },
            {
              label: t("common.view"),
              action: (largeLanguage) => {
                navigate(`/large-language-model/${largeLanguage?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (largeLanguage) => {
                largeLanguage.id &&
                  navigate(`/large-language-model/${largeLanguage?.id}`, {
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
          onCreatePath="large-language-model/new"
          onDelete={(model) => {
            if (model?.id) deleteModelsMutate({ variables: { id: model.id } });
          }}
          columns={[
            {
              header: t("common.name"),
              content: (largeLanguageModel) => <Box fontWeight="bolder">{largeLanguageModel?.name}</Box>,
            },
            {
              header: t("common.description"),
              content: (largeLanguageModel) => largeLanguageModel?.description,
            },
            {
              header: t("common.status"),
              content: (largeLanguage) => {
                const statusText = largeLanguage?.enabled ? t("common.active") : t("common.inactive");
                const backgroundColor = largeLanguage?.enabled ? theme.palette.success.main : theme.palette.grey[500];

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

                    {/* {viewDeleteModal.view && (
                      <ModalConfirm
                        title={t("modal.confirm-deletion")}
                        body={t("pages.large-language-models.delete-body")}
                        labelConfirm={t("common.delete")}
                        actionConfirm={() => {
                          deleteTabMutate({
                            variables: { id: viewDeleteModal.id || "" },
                          });
                        }}
                        close={() => setViewDeleteModal({ id: undefined, view: false })}
                      />
                    )} */}
                  </>
                );
              },
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.large-language-models.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteTabMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

