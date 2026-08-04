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
import { useDeleteSearchConfigMutation, useSearchConfigsQuery } from "../../graphql-generated";

export function SearchConfigs() {
  const { t } = useTranslation();
  const searchConfigQuery = useSearchConfigsQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteSearchConfigMutate] = useDeleteSearchConfigMutation({
    refetchQueries: ["SearchConfigs"],
    onCompleted(data) {
      if (data.deleteSearchConfig?.id) {
        toast({
          title: t("pages.search-configs.deleted-title"),
          content: t("pages.search-configs.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.search-configs.delete-error-content"),
        displayType: "error",
      });
    },
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.search-configs.title")}
          </Typography>
          <Typography variant="body1">{t("pages.search-configs.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/search-config/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.search-configs.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: searchConfigQuery,
            field: (data) => data?.searchConfigs,
          }}
          onCreatePath="/search-configs/new"
          edgesPath="searchConfigs.edges"
          pageInfoPath="searchConfigs.pageInfo"
          onDelete={(searchConfig) => {
            if (searchConfig?.id)
              deleteSearchConfigMutate({
                variables: { id: searchConfig.id },
              });
          }}
          rowActions={[
            {
              label: t("common.view"),
              action: (searchConfig) => {
                navigate(`/search-config/${searchConfig?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (searchConfig) => {
                searchConfig.id &&
                  navigate(`/search-config/${searchConfig?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (searchConfig) => {
                if (searchConfig?.id) setViewDeleteModal({ view: true, id: searchConfig.id });
              },
            },
          ]}
          columns={[
            {
              header: t("common.name"),
              content: (searchConfig) => <Box fontWeight="bolder">{searchConfig?.name}</Box>,
            },
            {
              header: t("common.description"),
              content: (searchConfig) => (
                <Typography variant="body2" className="pipeline-title">
                  {searchConfig?.description}
                </Typography>
              ),
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.search-configs.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteSearchConfigMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

