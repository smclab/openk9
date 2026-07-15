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
import { useDeleteHighlightMutation, useHighlightsQuery } from "../../graphql-generated";

export function Highlights() {
  const highlightsQuery = useHighlightsQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState<{ view: boolean; id: string | undefined }>({
    view: false,
    id: undefined,
  });
  const toast = useToast();
  const navigate = useNavigate();
  const [deleteHighlightMutate] = useDeleteHighlightMutation({
    refetchQueries: ["Highlights"],
    onCompleted(data) {
      if (data.deleteHighlight?.id) {
        toast({
          title: "Highlight Deleted",
          content: "Highlight has been deleted successfully",
          displayType: "success",
        });
      }
      highlightsQuery.refetch();
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Highlight",
        displayType: "error",
      });
    },
  });

  if (highlightsQuery.loading) {
    return null;
  }

  return (
    <React.Fragment>
      <Container maxWidth="xl">
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              Highlights
            </Typography>
            <Typography variant="body1">
              In this section you can create and handle Highlights to configure how search results are highlighted.
              Choose a highlighter type and associate the document type fields to highlight.
            </Typography>
          </Box>
          <Box>
            <Link to="/highlight/new" style={{ textDecoration: "none" }}>
              <Button variant="contained" color="primary" aria-label="create new highlight">
                Create New Highlight
              </Button>
            </Link>
          </Box>
        </Box>

        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: highlightsQuery,
              field: (data) => ({
                edges: (data?.highlights ?? [])
                  .filter((highlight): highlight is NonNullable<typeof highlight> => !!highlight)
                  .map((highlight) => ({ node: highlight })),
                pageInfo: { hasNextPage: false, endCursor: null },
              }),
            }}
            onCreatePath="/highlight/new"
            onDelete={(highlight) => {
              if (highlight?.id)
                deleteHighlightMutate({
                  variables: { id: highlight.id },
                });
            }}
            edgesPath="highlights"
            pageInfoPath="highlights.pageInfo"
            rowActions={[
              {
                label: "View",
                action: (highlight) => {
                  if (highlight?.id) navigate(`/highlight/${highlight?.id}/view`);
                },
              },
              {
                label: "Edit",
                action: (highlight) => {
                  if (highlight?.id)
                    navigate(`/highlight/${highlight?.id}`, {
                      replace: true,
                    });
                },
              },
              {
                label: "Delete",
                action: (highlight) => {
                  highlight?.id && setViewDeleteModal({ view: true, id: highlight.id });
                },
              },
            ]}
            columns={[
              {
                header: "Name",
                content: (highlight) => <Box fontWeight="bolder">{highlight?.name}</Box>,
              },
              {
                header: "Description",
                content: (highlight) => (
                  <Typography variant="body2" className="pipeline-title">
                    {highlight?.description}
                  </Typography>
                ),
              },
              {
                header: "Type",
                content: (highlight) => (
                  <Typography variant="body2" className="pipeline-title">
                    {highlight?.type}
                  </Typography>
                ),
              },
            ]}
          />
        </Box>

        {viewDeleteModal.view && (
          <ModalConfirm
            title="Confirm Deletion"
            body="Are you sure you want to delete this highlight? This action is irreversible and all associated data will be lost."
            labelConfirm="Delete"
            actionConfirm={() => {
              deleteHighlightMutate({
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
