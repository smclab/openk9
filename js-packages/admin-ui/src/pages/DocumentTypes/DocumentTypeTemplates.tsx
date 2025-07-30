import { ModalConfirm, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useDeleteDocumentTypeTemplateMutation, useDocumentTypeTemplatesQuery } from "../../graphql-generated";

export function DocumentTypeTemplates() {
  const docTypeTemplatesQuery = useDocumentTypeTemplatesQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteDocumentTypeTemplateMutate] = useDeleteDocumentTypeTemplateMutation({
    refetchQueries: ["DocumentTypeTemplates"],
    onCompleted(data) {
      if (data.deleteDocTypeTemplate?.id) {
        toast({
          title: "Document Type Template Deleted",
          content: "Document Type Template has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Document Type Template",
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
            Document Type Templates
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Document Type Templates. A Document Type Template permits to
            define how to render result in Search Frontend for a specific Document Type.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/document-type-template/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Document Type Templates
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
              label: "View",
              action: (docTypeTemplate) => {
                navigate(`/document-type-template/${docTypeTemplate?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (docTypeTemplate) => {
                docTypeTemplate.id &&
                  navigate(`/document-type-template/${docTypeTemplate?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
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
              header: "Name",
              content: (enrich) => <Box fontWeight="bolder">{enrich?.name}</Box>,
            },
            {
              header: "Description",
              content: (docTypeTemplate) => docTypeTemplate?.description,
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this Document Type Template? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
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
