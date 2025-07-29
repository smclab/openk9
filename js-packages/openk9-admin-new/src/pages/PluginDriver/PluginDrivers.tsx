import { ModalConfirm } from "@components/Form";
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import { useDeletePluginDriverMutation, usePluginDriversInfoQueryQuery } from "../../graphql-generated";

export function PluginDrivers() {
  const pluginDriverQuery = usePluginDriversInfoQueryQuery({
    // refetchQueries: [PluginDriversQuery],
    fetchPolicy: "network-only",
  });

  const navigate = useNavigate();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const toast = useToast();
  const [deletePluginDriverMutate] = useDeletePluginDriverMutation({
    refetchQueries: ["PluginDriversInfoQuery"],
    onCompleted(data) {
      if (data.deletePluginDriver?.id) {
        toast({
          title: "Connector Deleted",
          content: "Connector has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Connector",
        displayType: "error",
      });
    },
  });
  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Connectors
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Connectors. A Connector defines hook up to external Openk9
            connector and how to call it when Openk9 needs to trigger data ingestion.
          </Typography>
        </Box>
        <Box>
          <Link to="/plugin-driver/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Connector
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: pluginDriverQuery,
            field: (data) => data?.pluginDrivers,
          }}
          edgesPath="pluginDrivers.edges"
          pageInfoPath="pluginDrivers.pageInfo"
          rowActions={[
            {
              label: "View",
              action: (pluginDriver) => {
                navigate(`/plugin-driver/${pluginDriver?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (pluginDriver) => {
                pluginDriver.id &&
                  navigate(`/plugin-driver/${pluginDriver?.id}`, {
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
          onCreatePath="plugin-driver/new"
          onDelete={(model) => {
            if (model?.id) deletePluginDriverMutate({ variables: { id: model.id } });
          }}
          columns={[
            {
              header: "Name",
              content: (pluginDriverModel) => <Box fontWeight="bolder">{pluginDriverModel?.name}</Box>,
            },
            {
              header: "Description",
              content: (pluginDriverModel) => pluginDriverModel?.description,
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this Connectors? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deletePluginDriverMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}
