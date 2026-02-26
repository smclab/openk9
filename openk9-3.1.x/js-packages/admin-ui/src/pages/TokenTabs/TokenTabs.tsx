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
import { ModalAddSingle, ModalConfirm } from "@components/Form";
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import {
  useAddTokenTabToTabMutation,
  useDeleteTabTokenMutation,
  useTabTokensQuery,
  useUnassociatedTokenTabsInTabQuery,
} from "../../graphql-generated";

export function TokenTabs() {
  const tabTokensQuery = useTabTokensQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteTabMutate] = useDeleteTabTokenMutation({
    refetchQueries: ["TabTokens"],
    onCompleted(data) {
      if (data.deleteTokenTab?.id) {
        toast({
          title: "Token Tab Deleted",
          content: "Token Tab has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Token Tab",
        displayType: "error",
      });
    },
  });
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });
  const [addTokenTabToTabMutate] = useAddTokenTabToTabMutation({
    refetchQueries: ["unassociatedTokenTabsInTab"],
  });

  const { data } = useUnassociatedTokenTabsInTabQuery({
    variables: { id: Number(isAdd.id) ?? -1 },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });

  const unboundTabsByTokenTab = data?.unboundTabsByTokenTab;

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Token Tabs
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Token Tabs to define personalized searches to compose search
            behind Tabs. Add them to Tab to use.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/token-tab/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Token Tab
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: tabTokensQuery,
            field: (data) => data?.totalTokenTabs,
          }}
          onCreatePath="/token-tab/new"
          edgesPath="totalTokenTabs.edges"
          pageInfoPath="totalTokenTabs.pageInfo"
          onDelete={(tabs) => {
            if (tabs?.id)
              deleteTabMutate({
                variables: { id: tabs.id },
              });
          }}
          rowActions={[
            {
              label: "Add",
              action: (datasources) => {
                setIsAdd({ id: datasources.id, isVisible: true });
              },
            },
            {
              label: "View",
              action: (tabs) => {
                navigate(`/token-tab/${tabs?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (tabs) => {
                tabs.id &&
                  navigate(`/token-tab/${tabs?.id}`, {
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
          columns={[
            {
              header: "Name",
              content: (tab) => <Box fontWeight="bolder">{tab?.name}</Box>,
            },
            {
              header: "Token Type",
              content: (tab) => (
                <Typography variant="body2" className="token-type-title">
                  {tab?.tokenType}
                </Typography>
              ),
            },
            {
              header: "value",
              content: (tab) => (
                <Typography variant="body2" className="value-title">
                  {tab?.value}
                </Typography>
              ),
            },
          ]}
        />
      </Box>

      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this token tab? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteTabMutate({ variables: { id: viewDeleteModal.id || "" } });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}

      {isAdd.isVisible && isAdd.id && (
        <ModalAddSingle
          id={isAdd.id}
          messageSuccess="Token Tab has been associated successfully"
          list={unboundTabsByTokenTab}
          association={({ parentId, childId, onSuccessCallback, onErrorCallback }) =>
            addTokenTabToTabMutate({
              variables: { parentId, childId },
              onCompleted: () => {
                onSuccessCallback();
              },
              onError: (error) => {
                onErrorCallback(error);
              },
            })
          }
          title="Association to tabs"
          callbackClose={() => {
            setIsAdd({ id: null, isVisible: false });
          }}
        />
      )}
    </Container>
  );
}

