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
import { useDeleteDocumentTypeMutation, useDocumentTypesQuery } from "../../graphql-generated";

export default function DocumentTypes() {
  const { t } = useTranslation();
  const documentTypeQuery = useDocumentTypesQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteDocumentTypeMutate] = useDeleteDocumentTypeMutation({
    refetchQueries: ["DocumentTypes"],
    onCompleted(data) {
      if (data.deleteDocType?.id) {
        toast({
          title: t("pages.document-types.deleted-title"),
          content: t("pages.document-types.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.document-types.delete-error-content"),
        displayType: "error",
      });
    },
  });

  const isLoading = documentTypeQuery.loading;

  if (isLoading) {
    return null;
  }
  return (
    <React.Fragment>
      <Container maxWidth="xl">
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              {t("pages.document-types.title")}
            </Typography>
            <Typography variant="body1"></Typography>
          </Box>
          <Box>
            <Link to="/document-type/new" style={{ textDecoration: "none" }}>
              <Button variant="contained" color="primary" aria-label={t("pages.document-types.create-new-aria")}>
                {t("pages.document-types.create-new")}
              </Button>
            </Link>
          </Box>
        </Box>

        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: documentTypeQuery,
              field: (data) => data?.docTypes,
            }}
            onCreatePath="/document-type/new"
            edgesPath="documentTypes.edges"
            pageInfoPath="documentTypes.pageInfo"
            onDelete={(documentType) => {
              if (documentType?.id) {
              }
            }}
            deleted={{
              title: t("pages.document-types.delete-title"),
              messsage: t("pages.document-types.delete-message"),
              wordConfirm: t("common.delete"),
              actionDeleted: (id: string, name: string) => {
                deleteDocumentTypeMutate({ variables: { id, docTypeName: name } });
              },
            }}
            rowActions={[
              {
                label: t("common.view"),
                action: (documentType) => {
                  if (documentType?.id)
                    navigate(`/document-type/${documentType?.id}/view`, {
                      replace: true,
                    });
                },
              },
              {
                label: t("common.edit"),
                action: (documentType) => {
                  if (documentType?.id)
                    navigate(`/document-type/${documentType?.id}`, {
                      replace: true,
                    });
                },
              },
              {
                label: t("pages.document-types.handle-fields"),
                action: (documentType) => {
                  if (documentType?.id)
                    navigate(`/sub-document-type/${documentType?.id}`, {
                      replace: true,
                    });
                },
              },
            ]}
            columns={[
              {
                header: t("common.name"),
                content: (documentType) => <Box fontWeight="bolder">{documentType?.name}</Box>,
              },
              {
                header: t("common.description"),
                content: (documentType) => (
                  <Typography variant="body2" className="pipeline-title">
                    {documentType?.description}
                  </Typography>
                ),
              },
            ]}
          />
        </Box>

        {viewDeleteModal.view && (
          <ModalConfirm
            title={t("modal.confirm-deletion")}
            body={t("pages.document-types.delete-body")}
            labelConfirm={t("common.delete")}
            actionConfirm={() => {
              deleteDocumentTypeMutate({
                variables: { id: viewDeleteModal.id || "" },
              });
            }}
            close={() => setViewDeleteModal({ id: undefined, view: false })}
          />
        )}
      </Container>
    </React.Fragment>
  );
}

