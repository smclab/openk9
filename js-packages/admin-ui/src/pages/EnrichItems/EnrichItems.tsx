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
import { ModalAddSingle, ModalConfirm, useToast } from "@components/Form";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import {
  useAddEnrichItemToEnrichPipelineMutation,
  useDeleteEnrichItemMutation,
  useEnrichItemsQuery,
  useUnboundEnrichPipelinesQuery,
} from "../../graphql-generated";

export function EnrichItems() {
  const enrichItemsQuery = useEnrichItemsQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [addMutate] = useAddEnrichItemToEnrichPipelineMutation({
    refetchQueries: [],
  });
  const [deleteEnrichItemMutate] = useDeleteEnrichItemMutation({
    refetchQueries: ["EnrichItems"],
    onCompleted(data) {
      if (data.deleteEnrichItem?.id) {
        toast({
          title: "Enrich Item Deleted",
          content: "Enrich Item has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Enrich Item",
        displayType: "error",
      });
    },
  });

  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });
  const [viewDeleteModal, setViewDeleteModal] = React.useState<{
    view: boolean;
    id: string | undefined;
  }>({
    view: false,
    id: undefined,
  });
  const unboundListEnrichPipeline = useUnboundEnrichPipelinesQuery({
    variables: { itemId: Number(isAdd?.id) },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Enrich Items
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Enrich Items. Add them to Enrich Pipelines to perform enrich
            activities on data.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/enrich-item/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Enrich Item
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: enrichItemsQuery,
            field: (data) => data?.enrichItems,
          }}
          onCreatePath="/enrich-items/new"
          edgesPath="enrichItems.edges"
          pageInfoPath="enrichItems.pageInfo"
          rowActions={[
            {
              label: "Add",
              action: (enrichItem) => {
                setIsAdd({ id: enrichItem?.id, isVisible: true });
              },
            },
            {
              label: "View",
              action: (enrichItem) => {
                navigate(`/enrich-item/${enrichItem?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (enrichItem) => {
                enrichItem.id &&
                  navigate(`/enrich-item/${enrichItem?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (enrichItem) => {
                enrichItem.id && setViewDeleteModal({ view: true, id: enrichItem.id });
              },
            },
          ]}
          onDelete={(enrichItem) => {
            if (enrichItem?.id) deleteEnrichItemMutate({ variables: { id: enrichItem.id } });
          }}
          columns={[
            {
              header: "Name",
              content: (enrich) => <Box fontWeight="bolder">{enrich?.name}</Box>,
            },
            {
              header: "Description",
              content: (enrichItem) => enrichItem?.description,
            },
          ]}
        />
      </Box>

      {isAdd.isVisible && (
        <ModalAddSingle
          id={isAdd.id}
          callbackClose={() => setIsAdd({ id: null, isVisible: false })}
          title="Associate To Pipeline"
          messageSuccess={"Enrich Item added to Pipeline"}
          list={unboundListEnrichPipeline.data?.unboundEnrichPipelines}
          association={({ parentId, childId, onSuccessCallback, onErrorCallback }) => {
            addMutate({
              variables: { parentId, childId },
              onCompleted: () => {
                onSuccessCallback();
              },
              onError: (error) => {
                onErrorCallback(error);
              },
            });
          }}
        />
      )}

      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this Enrich Item? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteEnrichItemMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

