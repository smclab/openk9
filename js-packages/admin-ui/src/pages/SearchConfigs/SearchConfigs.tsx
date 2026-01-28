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
import { useDeleteSearchConfigMutation, useSearchConfigsQuery } from "../../graphql-generated";

export function SearchConfigs() {
  const searchConfigQuery = useSearchConfigsQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteSearchConfigMutate] = useDeleteSearchConfigMutation({
    refetchQueries: ["SearchConfigs"],
    onCompleted(data) {
      if (data.deleteSearchConfig?.id) {
        toast({
          title: "Search Config Deleted",
          content: "Search Config has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Search Config",
        displayType: "error",
      });
    },
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Search Configs
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Search Config. Configuring Search Config you can handle some
            specific aspects on how search is performed and results are returned.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/search-config/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Search Config
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: searchConfigQuery,
            field: (data) => data?.searchConfigs,
          }}
          onCreatePath="/search-configs/new"
          edgesPath="searchConfigs.edges"
          pageInfoPath="searchConfigs.pageInfo"
          onDelete={(searchConfig) => {
            if (searchConfig?.id)
              deleteSearchConfigMutate({
                variables: { id: searchConfig.id },
              });
          }}
          rowActions={[
            {
              label: "View",
              action: (searchConfig) => {
                navigate(`/search-config/${searchConfig?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (searchConfig) => {
                searchConfig.id &&
                  navigate(`/search-config/${searchConfig?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (searchConfig) => {
                if (searchConfig?.id) setViewDeleteModal({ view: true, id: searchConfig.id });
              },
            },
          ]}
          columns={[
            {
              header: "Name",
              content: (searchConfig) => <Box fontWeight="bolder">{searchConfig?.name}</Box>,
            },
            {
              header: "Description",
              content: (searchConfig) => (
                <Typography variant="body2" className="pipeline-title">
                  {searchConfig?.description}
                </Typography>
              ),
            },
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this search config? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteSearchConfigMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

