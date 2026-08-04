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
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { useAutocompletesQuery, useDeleteAutocompleteMutation } from "../../graphql-generated";

export function Autocompletes() {
  const { t } = useTranslation();
  const autocompletesQuery = useAutocompletesQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteAutocompleteMutate] = useDeleteAutocompleteMutation({
    refetchQueries: ["Autocompletes"],
    onCompleted(data) {
      if (data.deleteAutocomplete?.id) {
        toast({
          title: t("pages.autocompletes.deleted-title"),
          content: t("pages.autocompletes.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.autocompletes.delete-error-content"),
        displayType: "error",
      });
    },
  });
  const [viewDeleteModal, setViewDeleteModal] = React.useState<{
    view: boolean;
    id: string | undefined;
  }>({
    view: false,
    id: undefined,
  });
  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.autocompletes.title")}
          </Typography>
          <Typography variant="body1">{t("pages.autocompletes.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/autocomplete/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.autocompletes.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: autocompletesQuery,
            field: (data) => data?.autocompletes,
          }}
          onCreatePath="/autocomplete/new"
          edgesPath="autocompletes.edges"
          pageInfoPath="autocompletes.pageInfo"
          rowActions={[
            {
              label: t("common.view"),
              action: (autocomplete) => {
                navigate(`/autocomplete/${autocomplete?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (autocomplete) => {
                autocomplete.id &&
                  navigate(`/autocomplete/${autocomplete?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (autocomplete) => {
                autocomplete.id && setViewDeleteModal({ view: true, id: autocomplete.id });
              },
            },
          ]}
          onDelete={(autocomplete) => {
            if (autocomplete?.id)
              deleteAutocompleteMutate({
                variables: { id: autocomplete.id },
              });
          }}
          columns={[
            {
              header: t("common.name"),
              content: (enrich) => <Box fontWeight="bolder">{enrich?.name}</Box>,
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.autocompletes.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteAutocompleteMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

