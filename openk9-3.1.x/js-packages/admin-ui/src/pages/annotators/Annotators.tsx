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
import { Link, useNavigate } from "react-router-dom";
import { useAnnotatorsQuery, useDeleteAnnotatosMutation } from "../../graphql-generated";

export function Annotators() {
  const annotatorsQuery = useAnnotatorsQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const toast = useToast();
  const [deleteAnnotatorMutate] = useDeleteAnnotatosMutation({
    refetchQueries: ["Annotators"],
    onCompleted(data) {
      if (data.deleteAnnotator?.id) {
        toast({
          title: "Annotator Deleted",
          content: "Annotator has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Annotator",
        displayType: "error",
      });
    },
  });

  const navigate = useNavigate();

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Annotators
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Annotators and use them to configure Query Analysis tool.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/annotator/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary" aria-label="new annotator">
              Create New Annotator
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: annotatorsQuery,
            field: (data) => data?.annotators,
          }}
          onCreatePath="/annotators/new"
          onDelete={(annotator) => {
            if (annotator?.id) deleteAnnotatorMutate({ variables: { id: annotator.id } });
          }}
          edgesPath="annotators.edges"
          pageInfoPath="annotators.pageInfo"
          columns={[
            {
              header: "Name",
              content: (annotator) => <Box fontWeight="bolder">{annotator?.name}</Box>,
            },
            {
              header: "Field Name",
              content: (annotator) => (
                <Typography variant="body2" className="pipeline-title">
                  {annotator?.fieldName}
                </Typography>
              ),
            },
            {
              header: "Fuziness",
              content: (annotator) => (
                <Typography variant="body2" className="pipeline-title">
                  {annotator?.fuziness}
                </Typography>
              ),
            },
            {
              header: "Type",
              content: (annotator) => (
                <Typography variant="body2" className="pipeline-title">
                  {annotator?.type}
                </Typography>
              ),
            },
          ]}
          rowActions={[
            {
              label: "View",
              action: (annotator) => {
                navigate(`/annotator/${annotator?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (annotator) => {
                annotator.id &&
                  navigate(`/annotator/${annotator?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (annotator) => {
                if (annotator?.id) setViewDeleteModal({ view: true, id: annotator.id });
              },
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this annotator? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteAnnotatorMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

