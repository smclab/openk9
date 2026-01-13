import { ModalConfirm, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAutocompletesQuery, useDeleteAutocorrectionMutation } from "../../graphql-generated";

export function Autocompletes() {
  const autocompletesQuery = useAutocompletesQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteAutocompleteMutate] = useDeleteAutocorrectionMutation({
    refetchQueries: ["Autocompletes"],
    onCompleted(data) {
      if (data.deleteAutocorrection?.id) {
        toast({
          title: "Autocomplete Deleted",
          content: "Autocompletes has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Autocomplete",
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
            Autcomplete
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Autocomplete. A Document Type Template permits to define how to
            render result in Search Frontend for a specific Document Type.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/autocomplete/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Autocomplete
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
              label: "View",
              action: (autocomplete) => {
                navigate(`/document-type-template/${autocomplete?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (autocomplete) => {
                autocomplete.id &&
                  navigate(`/document-type-template/${autocomplete?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
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
              header: "Name",
              content: (enrich) => <Box fontWeight="bolder">{enrich?.name}</Box>,
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this Autocomplete? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
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
