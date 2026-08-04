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
import { useDeleteDocumentTypeTemplateMutation, useDocumentTypeTemplatesQuery } from "../../graphql-generated";

export function DocumentTypeTemplates() {
  const { t } = useTranslation();
  const docTypeTemplatesQuery = useDocumentTypeTemplatesQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteDocumentTypeTemplateMutate] = useDeleteDocumentTypeTemplateMutation({
    refetchQueries: ["DocumentTypeTemplates"],
    onCompleted(data) {
      if (data.deleteDocTypeTemplate?.id) {
        toast({
          title: t("pages.document-type-templates.deleted-title"),
          content: t("pages.document-type-templates.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.document-type-templates.delete-error-content"),
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
            {t("pages.document-type-templates.title")}
          </Typography>
          <Typography variant="body1">{t("pages.document-type-templates.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/document-type-template/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.document-type-templates.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: docTypeTemplatesQuery,
            field: (data) => data?.docTypeTemplates,
          }}
          onCreatePath="/document-type-template/new"
          rowActions={[
            {
              label: t("common.view"),
              action: (docTypeTemplate) => {
                navigate(`/document-type-template/${docTypeTemplate?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (docTypeTemplate) => {
                docTypeTemplate.id &&
                  navigate(`/document-type-template/${docTypeTemplate?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (docTypeTemplate) => {
                docTypeTemplate.id && setViewDeleteModal({ view: true, id: docTypeTemplate.id });
              },
            },
          ]}
          onDelete={(docTypeTemplate) => {
            if (docTypeTemplate?.id)
              deleteDocumentTypeTemplateMutate({
                variables: { id: docTypeTemplate.id },
              });
          }}
          columns={[
            {
              header: t("common.name"),
              content: (enrich) => <Box fontWeight="bolder">{enrich?.name}</Box>,
            },
            {
              header: t("common.description"),
              content: (docTypeTemplate) => docTypeTemplate?.description,
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.document-type-templates.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteDocumentTypeTemplateMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

