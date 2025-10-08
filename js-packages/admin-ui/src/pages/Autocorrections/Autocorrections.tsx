import { ModalConfirm, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography } from "@mui/material";
import { useAutocorrectionsOptionsQuery, useDeleteAutocorrectionMutation } from "../../graphql-generated";
import React from "react";
import { Link, useNavigate } from "react-router-dom";

export default function Autocorrections() {
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
          title: "Autocorrection Deleted",
          content: "Autocorrection has been deleted successfully",
          displayType: "success",
        });
      }
      autocorrectionQuery.refetch();
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Autocorrection",
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
                Autocorrections
              </Typography>
              <Typography variant="body1">
                In this section you can create and handle autocorrections to use to define advanced analysis logic when
                you create Data Indices. To go into detail about autocorrections check official Opensearch
                Documentation.
              </Typography>
            </Box>
            <Box>
              <Link to="/autocorrection/new" style={{ textDecoration: "none" }}>
                <Button variant="contained" color="primary" aria-label="create new autocorrection">
                  Create New autocorrection
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
                  label: "View",
                  action: (autocorrection) => {
                    if (autocorrection?.id) navigate(`/autocorrection/${autocorrection?.id}/view`);
                  },
                },
                {
                  label: "Edit",
                  action: (autocorrection) => {
                    if (autocorrection?.id)
                      navigate(`/autocorrection/${autocorrection?.id}`, {
                        replace: true,
                      });
                  },
                },
                {
                  label: "Delete",
                  action: (autocorrection) => {
                    autocorrection?.id && setViewDeleteModal({ view: true, id: autocorrection.id });
                  },
                },
              ]}
              columns={[
                {
                  header: "Name",
                  content: (autocorrection) => <Box fontWeight="bolder">{autocorrection?.name}</Box>,
                },
                {
                  header: "Description",
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
              title="Confirm Deletion"
              body="Are you sure you want to delete this analyzer? This action is irreversible and all associated data will be lost."
              labelConfirm="Delete"
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
