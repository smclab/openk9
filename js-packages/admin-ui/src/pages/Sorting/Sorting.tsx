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
import { ModalConfirm } from "@components/Form";
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import { useDeleteSortingMutation, useSortingsQuery } from "../../graphql-generated";

export function Sortings() {
  const { t } = useTranslation();
  const sortingsQuery = useSortingsQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteSortingMutate] = useDeleteSortingMutation({
    refetchQueries: ["Sortings"],
    onCompleted(data) {
      if (data.deleteSorting?.id) {
        toast({
          title: t("pages.sortings.deleted-title"),
          content: t("pages.sortings.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.sortings.delete-error-content"),
        displayType: "error",
      });
    },
  });
  const [viewDeleteModal, setViewDeleteModal] = React.useState<{ view: boolean; id: string | undefined }>({
    view: false,
    id: undefined,
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.sortings.title")}
          </Typography>
          <Typography variant="body1">{t("pages.sortings.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/sorting/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.sortings.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: sortingsQuery,
            field: (data) => data?.totalSortings,
          }}
          onCreatePath="/sorting/new"
          edgesPath="totalSortings.edges"
          pageInfoPath="totalSortings.pageInfo"
          onDelete={(sorting) => {
            if (sorting?.id)
              deleteSortingMutate({
                variables: { id: sorting.id },
              });
          }}
          rowActions={[
            {
              label: t("common.view"),
              action: (sorting) => {
                navigate(`/sorting/${sorting?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (sorting) => {
                sorting.id &&
                  navigate(`/sorting/${sorting?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (sorting) => {
                sorting.id && setViewDeleteModal({ view: true, id: sorting.id });
              },
            },
          ]}
          columns={[
            {
              header: t("common.name"),
              content: (sorting) => <Box fontWeight="bolder">{sorting?.name}</Box>,
            },
            {
              header: t("common.type"),
              content: (sorting) => (
                <Typography variant="body2" className="sorting-type-title">
                  {sorting?.type}
                </Typography>
              ),
            },
            {
              header: t("common.priority"),
              content: (sorting) => (
                <Typography variant="body2" className="sorting-priority-title">
                  {sorting?.priority}
                </Typography>
              ),
            },
          ]}
        />
      </Box>

      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.sortings.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteSortingMutate({ variables: { id: viewDeleteModal.id || "" } });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}
