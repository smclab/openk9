import { ModalConfirm, useToast } from "@components/Form";
import { Box, Button, Container, Typography, useTheme } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import {
  useDeleteEmbeddingModelMutation,
  useEmbeddingModelsQuery,
  useEnableEmbeddingModelMutation,
} from "../../graphql-generated";
import { EmbeddingModelsQuery } from "./gql";

export function EmbeddingModels() {
  const embeddingModelsQuery = useEmbeddingModelsQuery();
  const theme = useTheme();
  const toast = useToast();
  const [deleteEmbeddingMutate] = useDeleteEmbeddingModelMutation({
    refetchQueries: [EmbeddingModelsQuery],
    onCompleted(data) {
      if (data.deleteEmbeddingModel?.id) {
        toast({
          title: "Embedding Model Deleted",
          content: "Embedding Model has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Embedding Model",
        displayType: "error",
      });
    },
  });
  const navigate = useNavigate();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });

  const [updateEnableLargeLaguageModel] = useEnableEmbeddingModelMutation({
    refetchQueries: [EmbeddingModelsQuery],
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Embedding models
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Embedding Model. An Embedding Model can be used to vectorize data
            and enable Semantic Search features.
          </Typography>
        </Box>
        <Box>
          <Link to="/embedding-model/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Embedding Model
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: embeddingModelsQuery,
            field: (data) => data?.embeddingModels,
          }}
          edgesPath="embeddingModels.edges"
          pageInfoPath="embeddingModels.pageInfo"
          rowActions={[
            {
              label: "Start",
              isDisabled: (embeddingModels) => !embeddingModels?.enabled,
              action: (embeddingModel) => {
                if (embeddingModel?.id)
                  updateEnableLargeLaguageModel({
                    variables: { id: embeddingModel.id },
                  });
              },
            },
            {
              label: "View",
              action: (embeddingModels) => {
                navigate(`/embedding-model/${embeddingModels?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (embeddingModels) => {
                embeddingModels.id &&
                  navigate(`/embedding-model/${embeddingModels?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (tab) => {
                tab.id && setViewDeleteModal({ view: true, id: tab.id });
              },
            },
          ]}
          onCreatePath="new/"
          onDelete={(model) => {
            if (model?.id) deleteEmbeddingMutate({ variables: { id: model.id } });
          }}
          columns={[
            {
              header: "Name",
              content: (embeddingModel) => <Box fontWeight="bolder">{embeddingModel?.name}</Box>,
            },
            {
              header: "Description",
              content: (embeddingModel) => embeddingModel?.description,
            },
            {
              header: "Status",
              content: (embeddingModel) => {
                const statusText = embeddingModel?.enabled ? "Active" : "Inactive";
                const backgroundColor = embeddingModel?.enabled ? theme.palette.success.main : theme.palette.grey[500];

                return (
                  <>
                    <Typography
                      variant="body2"
                      color={theme.palette.background.paper}
                      sx={{
                        borderRadius: "8px",
                        background: backgroundColor,
                        padding: "8px",
                        maxWidth: "150px",
                      }}
                    >
                      {statusText}
                    </Typography>

                    {viewDeleteModal.view && (
                      <ModalConfirm
                        title="Confirm Deletion"
                        body="Are you sure you want to delete this embedding models? This action is irreversible and all associated data will be lost."
                        labelConfirm="Delete"
                        actionConfirm={() => {
                          deleteEmbeddingMutate({
                            variables: { id: viewDeleteModal.id || "" },
                          });
                        }}
                        close={() => setViewDeleteModal({ id: undefined, view: false })}
                      />
                    )}
                  </>
                );
              },
            },
          ]}
        />
      </Box>
    </Container>
  );
}
