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
import { useDeleteTokenizerMutation, useTokenizersQuery } from "../../graphql-generated";

export function Tokenizers() {
  const tokenizersQuery = useTokenizersQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const toast = useToast();
  const [deleteTokenizersMutate] = useDeleteTokenizerMutation({
    refetchQueries: ["Tokenizers"],
    onCompleted(data) {
      if (data.deleteTokenizer?.id) {
        toast({
          title: "Tokenizer Deleted",
          content: "Tokenizer has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Tokenizer",
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
            Tokenizers
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Tokenizers to use to define advanced analysis logic and bind to a
            custom Analyzer. To go into detail about Tokenizers check official Opensearch Documentation.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/tokenizer/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Tokenizer
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: tokenizersQuery,
            field: (data) => data?.tokenizers,
          }}
          edgesPath="tokenizers.edges"
          pageInfoPath="tokenizers.pageInfo"
          rowActions={[
            {
              label: "View",
              action: (tokenizer) => {
                navigate(`/tokenizer/${tokenizer?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (tokenizer) => {
                tokenizer.id &&
                  navigate(`/tokenizer/${tokenizer?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (tokenizer) => {
                if (tokenizer?.id) setViewDeleteModal({ view: true, id: tokenizer.id });
              },
            },
          ]}
          onCreatePath="/tokenizers/new"
          onDelete={(tokenizer) => {
            if (tokenizer?.id) deleteTokenizersMutate({ variables: { id: tokenizer.id } });
          }}
          columns={[
            {
              header: "Name",
              content: (pluginDriver) => <Box fontWeight="bolder">{pluginDriver?.name}</Box>,
            },
            {
              header: "Description",
              content: (pluginDriver) => (
                <Typography variant="body2" className="pipeline-title">
                  {pluginDriver?.description}
                </Typography>
              ),
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this tokenizer? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteTokenizersMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

