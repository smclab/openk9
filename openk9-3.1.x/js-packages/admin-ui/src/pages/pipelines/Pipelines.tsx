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
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { ModalConfirm } from "@components/Form";
import { Table } from "../../components/Table/Table";
import { useDeleteEnrichPipelineMutation, useEnrichPipelinesQuery } from "../../graphql-generated";

export function Pipelines() {
  const pipelinesQuery = useEnrichPipelinesQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteEnrichPipelineMutate] = useDeleteEnrichPipelineMutation({
    refetchQueries: ["EnrichPipelines"],
    onCompleted(data) {
      if (data.deleteEnrichPipeline?.id) {
        toast({
          title: "Enrich Pipeline Deleted",
          content: "Enrich Pipeline has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Enrich Pipeline",
        displayType: "error",
      });
    },
  });

  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });

  const isLoading = pipelinesQuery.loading;

  if (isLoading) {
    return null;
  }

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Pipelines
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Pipelines. A Pipeline define series of enrichment steps to
            performs to data. You can add and remove Enrich Items to it it and add to DataSource.
          </Typography>
        </Box>
        <Box>
          <Link to="/pipeline/new/mode/edit" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Pipeline
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: pipelinesQuery,
            field: (data) => data?.enrichPipelines,
          }}
          onCreatePath="/pipeline/new/mode/edit"
          edgesPath="pipelines.edges"
          pageInfoPath="pipelines.pageInfo"
          onDelete={(pipelines) => {
            if (pipelines?.id)
              deleteEnrichPipelineMutate({
                variables: { id: pipelines.id },
              });
          }}
          rowActions={[
            {
              label: "View",
              action: (pipelines) => {
                pipelines.id &&
                  navigate(`/pipeline/${pipelines.id}/mode/view`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Edit",
              action: (pipelines) => {
                pipelines.id &&
                  navigate(`/pipeline/${pipelines.id}/mode/edit`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (pipeline) => {
                pipeline.id && setViewDeleteModal({ view: true, id: pipeline.id });
              },
            },
          ]}
          columns={[
            {
              header: "Name",
              content: (pipeline) => <Box fontWeight="bolder">{pipeline?.name}</Box>,
            },
            {
              header: "Description",
              content: (pipeline) => (
                <Typography variant="body2" className="pipeline-title">
                  {pipeline?.description}
                </Typography>
              ),
            },
            {
              header: "Priority",
              content: (pipeline: any) => (
                <Typography variant="body2" className="pipeline-title">
                  {pipeline?.priority}
                </Typography>
              ),
            },
          ]}
        />
      </Box>

      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this pipeline? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteEnrichPipelineMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

