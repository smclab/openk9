import { ModalAddSingle, ModalConfirm, useToast } from "@components/Form";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import {
  useAddTabToBucketMutation,
  useDeleteTabsMutation,
  useTabsQuery,
  useUnboundBucketsByTabQuery,
} from "../../graphql-generated";
import { TabsQuery } from "./gql";

export function Tabs() {
  const tabsQuery = useTabsQuery();
  const toast = useToast();
  const [deleteTabMutate] = useDeleteTabsMutation({
    refetchQueries: [TabsQuery],
    onCompleted(data) {
      if (data.deleteTab?.id) {
        toast({
          title: "Tab Deleted",
          content: "Tab has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Tab",
        displayType: "error",
      });
    },
  });
  const navigate = useNavigate();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });

  const unboundListEnrichPipeline = useUnboundBucketsByTabQuery({
    variables: { id: Number(isAdd?.id) },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });
  const [addMutate] = useAddTabToBucketMutation({
    refetchQueries: [],
  });
  // const buckets = useBucketsQuery().data?.buckets;

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Tabs
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Tabs to define personalized searches to hookup to tabs in search
            frontend. Add them to Bucket to make it usable.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/tab/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Tab
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: tabsQuery,
            field: (data) => data?.tabs,
          }}
          edgesPath="tabs.edges"
          pageInfoPath="tabs.pageInfo"
          onCreatePath="/suggestion-categories/new"
          onDelete={(tabs) => {
            if (tabs?.id)
              deleteTabMutate({
                variables: { id: tabs.id },
              });
          }}
          rowActions={[
            {
              label: "Add",
              action: (bucket) => {
                setIsAdd({ id: bucket.id, isVisible: true });
              },
            },
            {
              label: "View",
              action: (tabs) => {
                navigate(`/tab/${tabs?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (tabs) => {
                tabs.id &&
                  navigate(`/tab/${tabs?.id}`, {
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
              header: "Description",
              content: (tab) => (
                <Typography variant="body2" className="pipeline-title">
                  {tab?.description}
                </Typography>
              ),
            },
            {
              header: "Priority",
              content: (tab) => (
                <Typography variant="body2" className="pipeline-title">
                  {tab?.priority}
                </Typography>
              ),
            },
          ]}
        />
      </Box>

      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this tab? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteTabMutate({ variables: { id: viewDeleteModal.id || "" } });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}

      {isAdd.isVisible && (
        <ModalAddSingle
          id={isAdd.id}
          list={unboundListEnrichPipeline.data?.unboundBucketsByTab}
          messageSuccess="Tab has been associated successfully"
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
          title="Association to bucket"
          callbackClose={() => {
            setIsAdd({ id: null, isVisible: false });
          }}
        />
      )}
    </Container>
  );
}
