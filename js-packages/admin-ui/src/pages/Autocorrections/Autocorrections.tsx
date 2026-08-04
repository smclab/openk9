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
import { useAutocorrectionsOptionsQuery, useDeleteAutocorrectionMutation } from "../../graphql-generated";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";

export default function Autocorrections() {
  const { t } = useTranslation();
  const autocorrectionQuery = useAutocorrectionsOptionsQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const toast = useToast();
  const navigate = useNavigate();
  const [deleteAutocorrection] = useDeleteAutocorrectionMutation({
    refetchQueries: ["autocorrections"],
    onCompleted(data) {
      if (data.deleteAutocorrection?.id) {
        toast({
          title: t("pages.autocorrections.deleted-title"),
          content: t("pages.autocorrections.deleted-content"),
          displayType: "success",
        });
      }
      autocorrectionQuery.refetch();
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.autocorrections.delete-error-content"),
        displayType: "error",
      });
    },
  });

  const isLoading = autocorrectionQuery.loading;

  if (isLoading) {
    return null;
  }
  return (
    <>
      <React.Fragment>
        <Container maxWidth="xl">
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Box sx={{ width: "50%", ml: 2 }}>
              <Typography component="h1" variant="h1" fontWeight="600">
                {t("pages.autocorrections.title")}
              </Typography>
              <Typography variant="body1">{t("pages.autocorrections.description")}</Typography>
            </Box>
            <Box>
              <Link to="/autocorrection/new" style={{ textDecoration: "none" }}>
                <Button variant="contained" color="primary" aria-label={t("pages.autocorrections.create-new-aria")}>
                  {t("pages.autocorrections.create-new")}
                </Button>
              </Link>
            </Box>
          </Box>

          <Box display="flex" gap="23px" mt={3}>
            <Table
              data={{
                queryResult: autocorrectionQuery,
                field: (data) => data?.autocorrections,
              }}
              onCreatePath="/autocorrection/new"
              onDelete={(autocorrection) => {
                if (autocorrection?.id)
                  deleteAutocorrection({
                    variables: { id: autocorrection.id },
                  });
              }}
              edgesPath="autocorrections.edges"
              pageInfoPath="autocorrections.pageInfo"
              rowActions={[
                {
                  label: t("common.view"),
                  action: (autocorrection) => {
                    if (autocorrection?.id) navigate(`/autocorrection/${autocorrection?.id}/view`);
                  },
                },
                {
                  label: t("common.edit"),
                  action: (autocorrection) => {
                    if (autocorrection?.id)
                      navigate(`/autocorrection/${autocorrection?.id}`, {
                        replace: true,
                      });
                  },
                },
                {
                  label: t("common.delete"),
                  action: (autocorrection) => {
                    autocorrection?.id && setViewDeleteModal({ view: true, id: autocorrection.id });
                  },
                },
              ]}
              columns={[
                {
                  header: t("common.name"),
                  content: (autocorrection) => <Box fontWeight="bolder">{autocorrection?.name}</Box>,
                },
                {
                  header: t("common.description"),
                  content: (autocorrection) => (
                    <Typography variant="body2" className="pipeline-title">
                      {autocorrection?.description}
                    </Typography>
                  ),
                },
              ]}
            />
          </Box>

          {viewDeleteModal.view && (
            <ModalConfirm
              title={t("modal.confirm-deletion")}
              body={t("pages.autocorrections.delete-body")}
              labelConfirm={t("common.delete")}
              actionConfirm={() => {
                deleteAutocorrection({
                  variables: { id: viewDeleteModal.id || "" },
                });
              }}
              close={() => setViewDeleteModal({ id: undefined, view: false })}
            />
          )}
        </Container>
      </React.Fragment>
    </>
  );
}

