import { ModalConfirm, useToast } from "@components/Form";
import { Box, Button, Container, Typography, useTheme } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import {
  useDeleteLargeLanguageModelMutation,
  useEnableLargeLanguageModelMutation,
  useLargeLanguageModelsQuery,
} from "../../graphql-generated";

export function LargeLanguageModels() {
  const largeLanguageModelsQuery = useLargeLanguageModelsQuery();
  const theme = useTheme();
  const toast = useToast();
  const [deleteTabMutate] = useDeleteLargeLanguageModelMutation({
    refetchQueries: ["LargeLanguageModels"],
    onCompleted(data) {
      if (data.deleteLargeLanguageModel?.id) {
        toast({
          title: "Large Language Model Deleted",
          content: "Large Language Model has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Large Language Model",
        displayType: "error",
      });
    },
  });
  const navigate = useNavigate();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });

  const [updateEnableLargeLaguageModel] = useEnableLargeLanguageModelMutation({
    refetchQueries: ["LargeLanguageModels"],
  });

  const [deleteModelsMutate] = useDeleteLargeLanguageModelMutation({
    refetchQueries: ["LargeLanguageModels"],
    onCompleted(data) {
      if (data.deleteLargeLanguageModel?.id) {
      }
    },
    onError(error) {},
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Large Language Models
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Large Language Model. An Large Language Model can be used to
            enable Rag and Chat feature on Openk9.
          </Typography>
        </Box>
        <Box>
          <Link to="/large-language-model/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Large Language Model
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: largeLanguageModelsQuery,
            field: (data) => data?.largeLanguageModels,
          }}
          edgesPath="largeLanguageModels.edges"
          pageInfoPath="largeLanguageModels.pageInfo"
          rowActions={[
            {
              label: "Start",

              isDisabled: (largeLanguageModel) => !largeLanguageModel?.enabled,
              action: (largeLanguage) => {
                if (largeLanguage?.id)
                  updateEnableLargeLaguageModel({
                    variables: { id: largeLanguage.id },
                  });
              },
            },
            {
              label: "View",
              action: (largeLanguage) => {
                navigate(`/large-language-model/${largeLanguage?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (largeLanguage) => {
                largeLanguage.id &&
                  navigate(`/large-language-model/${largeLanguage?.id}`, {
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
          onCreatePath="large-language-model/new"
          onDelete={(model) => {
            if (model?.id) deleteModelsMutate({ variables: { id: model.id } });
          }}
          columns={[
            {
              header: "Name",
              content: (largeLanguageModel) => <Box fontWeight="bolder">{largeLanguageModel?.name}</Box>,
            },
            {
              header: "Description",
              content: (largeLanguageModel) => largeLanguageModel?.description,
            },
            {
              header: "Status",
              content: (largeLanguage) => {
                const statusText = largeLanguage?.enabled ? "Active" : "Inactive";
                const backgroundColor = largeLanguage?.enabled ? theme.palette.success.main : theme.palette.grey[500];

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

                    {/* {viewDeleteModal.view && (
                      <ModalConfirm
                        title="Confirm Deletion"
                        body="Are you sure you want to delete this large language models? This action is irreversible and all associated data will be lost."
                        labelConfirm="Delete"
                        actionConfirm={() => {
                          deleteTabMutate({
                            variables: { id: viewDeleteModal.id || "" },
                          });
                        }}
                        close={() => setViewDeleteModal({ id: undefined, view: false })}
                      />
                    )} */}
                  </>
                );
              },
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this large language models? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteTabMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}
