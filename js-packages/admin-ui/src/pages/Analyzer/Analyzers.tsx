import { ModalConfirm, useToast } from "@components/Form";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import { useAnalyzersQuery, useDeleteAnalyzerMutation } from "../../graphql-generated";

export function Analyzers() {
  const analyzerQuery = useAnalyzersQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const toast = useToast();
  const navigate = useNavigate();
  const [deleteAnalyzerMutate] = useDeleteAnalyzerMutation({
    refetchQueries: ["Analyzers"],
    onCompleted(data) {
      if (data.deleteAnalyzer?.id) {
        toast({
          title: "Analyzer Deleted",
          content: "Analyzer has been deleted successfully",
          displayType: "success",
        });
      }
      analyzerQuery.refetch();
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Analyzer",
        displayType: "error",
      });
    },
  });

  const isLoading = analyzerQuery.loading;

  if (isLoading) {
    return null;
  }

  return (
    <React.Fragment>
      <Container maxWidth="xl">
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              Analyzers
            </Typography>
            <Typography variant="body1">
              In this section you can create and handle Analyzers to use to define advanced analysis logic when you
              create Data Indices. To go into detail about Analyzers check official Opensearch Documentation.
            </Typography>
          </Box>
          <Box>
            <Link to="/analyzer/new" style={{ textDecoration: "none" }}>
              <Button variant="contained" color="primary" aria-label="create new analyzer">
                Create New Analyzer
              </Button>
            </Link>
          </Box>
        </Box>

        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: analyzerQuery,
              field: (data) => data?.analyzers,
            }}
            onCreatePath="/analyzer/new"
            onDelete={(analyzer) => {
              if (analyzer?.id)
                deleteAnalyzerMutate({
                  variables: { id: analyzer.id },
                });
            }}
            edgesPath="analyzers.edges"
            pageInfoPath="analyzers.pageInfo"
            rowActions={[
              // {
              //   label: "Start",
              //   action: (analyzer) => {
              //     if (analyzer?.id)
              //       updateBucketsMutate({
              //         variables: { id: analyzer.id },
              //       });
              //   },
              // },
              {
                label: "View",
                action: (analyzer) => {
                  if (analyzer?.id) navigate(`/analyzer/${analyzer?.id}/view`);
                },
              },
              {
                label: "Edit",
                action: (analyzer) => {
                  if (analyzer?.id)
                    navigate(`/analyzer/${analyzer?.id}`, {
                      replace: true,
                    });
                },
              },
              {
                label: "Delete",
                action: (analyzer) => {
                  analyzer?.id && setViewDeleteModal({ view: true, id: analyzer.id });
                },
              },
            ]}
            columns={[
              {
                header: "Name",
                content: (analyzer) => <Box fontWeight="bolder">{analyzer?.name}</Box>,
              },
              {
                header: "Description",
                content: (analyzer) => (
                  <Typography variant="body2" className="pipeline-title">
                    {analyzer?.description}
                  </Typography>
                ),
              },
              {
                header: "Type",
                content: (analyzer) => (
                  <Typography variant="body2" className="pipeline-title">
                    {analyzer?.type}
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
              deleteAnalyzerMutate({
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
