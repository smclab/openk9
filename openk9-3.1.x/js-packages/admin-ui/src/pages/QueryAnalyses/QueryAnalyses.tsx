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
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import { useDeleteQueryAnalysisMutation, useQueryAnalysesQuery } from "../../graphql-generated";

export function QueryAnalyses() {
  const queryAnalysesQuery = useQueryAnalysesQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteQueryAnalysisMutate] = useDeleteQueryAnalysisMutation({
    refetchQueries: ["QueryAnalyses"],
    onCompleted(data) {
      if (data.deleteQueryAnalysis?.id) {
        toast({
          title: "Query Analysis Deleted",
          content: "Query Analysis has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Query Analysis",
        displayType: "error",
      });
    },
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Query Analyses
          </Typography>
          <Typography variant="body1">In this section you can create and configure Query Analysis tool.</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/query-analysis/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Query Analyses
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: queryAnalysesQuery,
            field: (data) => data?.queryAnalyses,
          }}
          edgesPath="queryAnalyses.edges"
          pageInfoPath="queryAnalyses.pageInfo"
          onCreatePath="/query-analyses/new"
          onDelete={(queryAnalyses) => {
            if (queryAnalyses?.id)
              deleteQueryAnalysisMutate({
                variables: { id: queryAnalyses.id },
              });
          }}
          rowActions={[
            {
              label: "View",
              action: (queryAnalyses) => {
                navigate(`/query-analysis/${queryAnalyses?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (queryAnalyses) => {
                queryAnalyses.id &&
                  navigate(`/query-analysis/${queryAnalyses?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (queryAnalyses) => {
                if (queryAnalyses?.id) setViewDeleteModal({ view: true, id: queryAnalyses.id });
              },
            },
          ]}
          columns={[
            {
              header: "Name",
              content: (queryAnalyses) => <Box fontWeight="bolder">{queryAnalyses?.name}</Box>,
            },
            {
              header: "Description",
              content: (queryAnalyses) => (
                <Typography variant="body2" className="pipeline-title">
                  {queryAnalyses?.description}
                </Typography>
              ),
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this query analysys? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteQueryAnalysisMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

