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
import { useAnnotatorsQuery, useDeleteAnnotatosMutation } from "../../graphql-generated";

export function Annotators() {
  const { t } = useTranslation();
  const annotatorsQuery = useAnnotatorsQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const toast = useToast();
  const [deleteAnnotatorMutate] = useDeleteAnnotatosMutation({
    refetchQueries: ["Annotators"],
    onCompleted(data) {
      if (data.deleteAnnotator?.id) {
        toast({
          title: t("pages.annotators.deleted-title"),
          content: t("pages.annotators.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.annotators.delete-error-content"),
        displayType: "error",
      });
    },
  });

  const navigate = useNavigate();

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.annotators.title")}
          </Typography>
          <Typography variant="body1">{t("pages.annotators.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/annotator/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary" aria-label={t("pages.annotators.create-new-aria")}>
              {t("pages.annotators.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: annotatorsQuery,
            field: (data) => data?.annotators,
          }}
          onCreatePath="/annotators/new"
          onDelete={(annotator) => {
            if (annotator?.id) deleteAnnotatorMutate({ variables: { id: annotator.id } });
          }}
          edgesPath="annotators.edges"
          pageInfoPath="annotators.pageInfo"
          columns={[
            {
              header: t("common.name"),
              content: (annotator) => <Box fontWeight="bolder">{annotator?.name}</Box>,
            },
            {
              header: t("pages.annotators.field-name"),
              content: (annotator) => (
                <Typography variant="body2" className="pipeline-title">
                  {annotator?.fieldName}
                </Typography>
              ),
            },
            {
              header: t("pages.annotators.fuziness"),
              content: (annotator) => (
                <Typography variant="body2" className="pipeline-title">
                  {annotator?.fuziness}
                </Typography>
              ),
            },
            {
              header: t("common.type"),
              content: (annotator) => (
                <Typography variant="body2" className="pipeline-title">
                  {annotator?.type}
                </Typography>
              ),
            },
          ]}
          rowActions={[
            {
              label: t("common.view"),
              action: (annotator) => {
                navigate(`/annotator/${annotator?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (annotator) => {
                annotator.id &&
                  navigate(`/annotator/${annotator?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (annotator) => {
                if (annotator?.id) setViewDeleteModal({ view: true, id: annotator.id });
              },
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.annotators.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteAnnotatorMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

