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
import { Box, Button, Container, Typography, useTheme } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { ModalConfirm, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { useRagConfigurationsQuery, useDeleteRagConfigurationMutation } from "../../graphql-generated";

export function RagConfigurations() {
  const ragConfigurationsQuery = useRagConfigurationsQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteRagConfigurationMutate] = useDeleteRagConfigurationMutation({
    refetchQueries: ["RagConfigurations"],
    onCompleted(data) {
      if (data.deleteRAGConfiguration?.id) {
        toast({
          title: "RAG Configuration Deleted",
          content: "RAG Configuration has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete RAG Configuration",
        displayType: "error",
      });
    },
  });

  return (
    <React.Fragment>
      <Container maxWidth={false}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              RAG Configurations
            </Typography>
            <Typography variant="body1">
              In this section you can create and handle the RAG configurations. A RAG configuration can be used to
              customize the retrieved data behaviours.
            </Typography>
          </Box>
          <Box>
            <Link to="/rag-configuration/new">
              <Button variant="contained" color="primary">
                Create new RAG Configuration
              </Button>
            </Link>
          </Box>
        </Box>
        {/* <Box display="flex" justifyContent="space-between" alignItems="center" mt={3} mb={3}>
          <Typography variant="h4" component="h1">
            RAG Configurations
          </Typography>
          <Link to="/rag-configuration/new">
            <Button variant="contained" color="primary">
              Create RAG Configuration
            </Button>
          </Link>
        </Box> */}
        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: ragConfigurationsQuery,
              field: (data) => data?.ragConfigurations,
            }}
            edgesPath="ragConfigurations.edges"
            pageInfoPath="ragConfigurations.pageInfo"
            onCreatePath="/rag-configuration/new"
            onDelete={(ragConfig) => {
              if (ragConfig?.id)
                deleteRagConfigurationMutate({
                  variables: { id: ragConfig.id },
                });
            }}
            rowActions={[
              {
                label: "View",
                action: (ragConfig) => {
                  if (ragConfig?.id)
                    navigate(`/rag-configuration/${ragConfig?.id}/view`, {
                      replace: true,
                    });
                },
              },
              {
                label: "Edit",
                action: (ragConfig) => {
                  if (ragConfig?.id)
                    navigate(`/rag-configuration/${ragConfig?.id}`, {
                      replace: true,
                    });
                },
              },
              {
                label: "Delete",
                action: (ragConfig) => {
                  if (ragConfig?.id) setViewDeleteModal({ view: true, id: ragConfig.id });
                },
              },
            ]}
            columns={[
              {
                header: "Name",
                content: (ragConfig) => <Box fontWeight="bolder">{ragConfig?.name}</Box>,
              },
              {
                header: "Description",
                content: (ragConfig) => (
                  <Typography variant="body2" className="pipeline-title">
                    {ragConfig?.description}
                  </Typography>
                ),
              },
              {
                header: "Type",
                content: (ragConfig) => <Typography variant="body2">{ragConfig?.type}</Typography>,
              },
            ]}
          />
        </Box>

        {viewDeleteModal.view && (
          <ModalConfirm
            title="Confirm Deletion"
            body="Are you sure you want to delete this RAG Configuration? This action is irreversible and all associated data will be lost."
            labelConfirm="Delete"
            actionConfirm={() => {
              deleteRagConfigurationMutate({
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

