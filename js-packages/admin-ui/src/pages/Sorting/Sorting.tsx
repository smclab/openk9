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
import { ModalConfirm } from "@components/Form";
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import { useDeleteSortingMutation, useSortingsQuery } from "../../graphql-generated";

export function Sortings() {
  const sortingsQuery = useSortingsQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteSortingMutate] = useDeleteSortingMutation({
    refetchQueries: ["Sortings"],
    onCompleted(data) {
      if (data.deleteSorting?.id) {
        toast({
          title: "Sorting Deleted",
          content: "Sorting has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Sorting",
        displayType: "error",
      });
    },
  });
  const [viewDeleteModal, setViewDeleteModal] = React.useState<{ view: boolean; id: string | undefined }>({
    view: false,
    id: undefined,
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Sortings
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Sortings to define how search results are ordered. Add them to Tab
            to use.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/sorting/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Sorting
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: sortingsQuery,
            field: (data) => data?.totalSortings,
          }}
          onCreatePath="/sorting/new"
          edgesPath="totalSortings.edges"
          pageInfoPath="totalSortings.pageInfo"
          onDelete={(sorting) => {
            if (sorting?.id)
              deleteSortingMutate({
                variables: { id: sorting.id },
              });
          }}
          rowActions={[
            {
              label: "View",
              action: (sorting) => {
                navigate(`/sorting/${sorting?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (sorting) => {
                sorting.id &&
                  navigate(`/sorting/${sorting?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (sorting) => {
                sorting.id && setViewDeleteModal({ view: true, id: sorting.id });
              },
            },
          ]}
          columns={[
            {
              header: "Name",
              content: (sorting) => <Box fontWeight="bolder">{sorting?.name}</Box>,
            },
            {
              header: "Type",
              content: (sorting) => (
                <Typography variant="body2" className="sorting-type-title">
                  {sorting?.type}
                </Typography>
              ),
            },
            {
              header: "Priority",
              content: (sorting) => (
                <Typography variant="body2" className="sorting-priority-title">
                  {sorting?.priority}
                </Typography>
              ),
            },
          ]}
        />
      </Box>

      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this sorting? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteSortingMutate({ variables: { id: viewDeleteModal.id || "" } });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}
