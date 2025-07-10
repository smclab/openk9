import { Box, Button, Container, Typography, useTheme } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { ModalConfirm, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { useBucketsQuery, useDeleteBucketMutation, useEnableBucketMutation } from "../../graphql-generated";
import { BucketsQuery } from "./gql";

export function Buckets() {
  const bucketsQuery = useBucketsQuery();
  const theme = useTheme();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const [updateBucketsMutate] = useEnableBucketMutation({
    refetchQueries: [BucketsQuery],
  });
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteBucketMutate] = useDeleteBucketMutation({
    refetchQueries: [BucketsQuery],
    onCompleted(data) {
      if (data.deleteBucket?.id) {
        toast({
          title: "Bucket Deleted",
          content: "Bucket has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Bucket",
        displayType: "error",
      });
    },
  });

  const isLoading = bucketsQuery.loading;

  if (isLoading) {
    return null;
  }
  return (
    <React.Fragment>
      <Container maxWidth="xl">
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              Buckets
            </Typography>
            <Typography variant="body1">
              In this section you can create and handle Buckets. A Bucket define runtime objects search enging use. You
              can add and remove to it datasources, filters or tabs you want to make searchable. You can also configure
              details like search configuration aspects, language or query analysis to use.
            </Typography>
          </Box>
          <Box>
            <Link to="/bucket/new" style={{ textDecoration: "none" }}>
              <Button variant="contained" color="primary" aria-label="create new bucket">
                Create New Bucket
              </Button>
            </Link>
          </Box>
        </Box>

        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: bucketsQuery,
              field: (data) => data?.buckets,
            }}
            onCreatePath="/bucket/new"
            edgesPath="buckets.edges"
            pageInfoPath="buckets.pageInfo"
            onDelete={(bucket) => {
              if (bucket?.id)
                deleteBucketMutate({
                  variables: { id: bucket.id },
                });
            }}
            rowActions={[
              {
                label: "Start",
                isDisabled: (bucket) => !bucket?.enabled,
                action: (bucket) => {
                  if (bucket?.id)
                    updateBucketsMutate({
                      variables: { id: bucket.id },
                    });
                },
              },
              {
                label: "View",
                action: (bucket) => {
                  if (bucket?.id)
                    navigate(`/bucket/${bucket?.id}/view`, {
                      replace: true,
                    });
                },
              },
              {
                label: "Edit",
                action: (bucket) => {
                  if (bucket?.id)
                    navigate(`/bucket/${bucket?.id}`, {
                      replace: true,
                    });
                },
              },
              {
                label: "Delete",
                action: (bucket) => {
                  if (bucket?.id) setViewDeleteModal({ view: true, id: bucket.id });
                },
              },
            ]}
            columns={[
              {
                header: "Name",
                content: (bucket) => <Box fontWeight="bolder">{bucket?.name}</Box>,
              },
              {
                header: "Description",
                content: (bucket) => (
                  <Typography variant="body2" className="pipeline-title">
                    {bucket?.description}
                  </Typography>
                ),
              },
              {
                header: "Status",
                content: (bucket) => {
                  const statusText = bucket?.enabled ? "Active" : "Inactive";
                  const backgroundColor = bucket?.enabled ? theme.palette.success.main : theme.palette.grey[500];

                  return (
                    <Typography
                      variant="body2"
                      color={theme.palette.background.paper}
                      sx={{
                        borderRadius: "8px",
                        background: backgroundColor,
                        padding: "8px",
                        maxWidth: "150px",
                      }}
                    >
                      {statusText}
                    </Typography>
                  );
                },
              },
            ]}
          />
        </Box>

        {viewDeleteModal.view && (
          <ModalConfirm
            title="Confirm Deletion"
            body="Are you sure you want to delete this bucket? This action is irreversible and all associated data will be lost."
            labelConfirm="Delete"
            actionConfirm={() => {
              deleteBucketMutate({
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
