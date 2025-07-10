import { formatDate } from "@components/common";
import { ModalAddSingle, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography, useTheme } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  useAddDataSourceToBucketMutation,
  useDataSourcesQuery,
  useDeleteDataSourceMutation,
  useUnboundBucketsByDatasourceQuery,
} from "../../graphql-generated";
import { DataSourcesQuery } from "./gql";

export function Datasources() {
  const datasourcesQuery = useDataSourcesQuery();
  const theme = useTheme();
  const navigate = useNavigate();
  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });
  const unboundListBuckets = useUnboundBucketsByDatasourceQuery({
    variables: { datasourceId: Number(isAdd?.id) },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });
  const [addMutate] = useAddDataSourceToBucketMutation();
  const toast = useToast();
  const [deleteDataSource] = useDeleteDataSourceMutation({
    refetchQueries: [DataSourcesQuery],
    onCompleted(data) {
      if (data.deleteDatasource?.id) {
        toast({
          title: "Datasource Deleted",
          content: "Datasource has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Datasource",
        displayType: "error",
      });
    },
  });

  return (
    <React.Fragment>
      <Container maxWidth="xl">
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              Datasources
            </Typography>
            <Typography variant="body1">
              This is your datasources list, lorem Ipsum has been the industry's standard dummy text ever since the,
              when an unknown printer took a galley of type and scrambled it to make a type specimen hook.
            </Typography>
          </Box>
          <Box>
            <Link to="/data-source/new/mode/create/landingTab/0" style={{ textDecoration: "none" }}>
              <Button variant="contained" color="primary">
                Create New Datasource
              </Button>
            </Link>
          </Box>
        </Box>

        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: datasourcesQuery,
              field: (data) => data?.datasources,
            }}
            onCreatePath="/data-source/new/mode/edit"
            onDelete={(datasources) => {}}
            edgesPath="datasources.edges"
            pageInfoPath="datasources.pageInfo"
            deleted={{
              title: "Datasource",
              messsage: "Deleting the datasource will remove all entities and related indexes.",
              wordConfirm: "Delete",
              actionDeleted: (id: string, name: string) => {
                deleteDataSource({ variables: { id, datasourceName: name } });
              },
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
                action: (datasources) => {
                  navigate(`/data-source/${datasources?.id}/mode/view/landingTab/monitoring`, {
                    replace: true,
                  });
                },
              },
              {
                label: "Edit",
                action: (datasources) => {
                  datasources.id &&
                    navigate(`/data-source/${datasources?.id}/mode/edit/landingTab/datasource`, {
                      replace: true,
                    });
                },
              },
            ]}
            columns={[
              {
                header: "Name",
                content: (datasource) => <Box fontWeight="bolder">{datasource?.name}</Box>,
              },
              {
                header: "Last Ingestion Date",
                content: (datasource) => (
                  <Typography variant="body2" className="datasource-title">
                    {formatDate(datasource?.lastIngestionDate)}
                  </Typography>
                ),
              },
              {
                header: "Schedulable",
                content: (datasource) => {
                  const statusText = datasource?.schedulable ? "Active" : "Idle";
                  const backgroundColor = datasource?.schedulable
                    ? theme.palette.success.main
                    : theme.palette.grey[500];

                  return (
                    <Typography variant="body2" className="datasource-title">
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
                    </Typography>
                  );
                },
              },
            ]}
          />
        </Box>
      </Container>
      {isAdd.isVisible && (
        <ModalAddSingle
          id={isAdd.id}
          list={unboundListBuckets.data?.unboundBucketsByDatasource}
          messageSuccess="Datasource added to Bucket"
          title="Association to Bucket"
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
          callbackClose={() => {
            setIsAdd({ id: null, isVisible: false });
          }}
        />
      )}
    </React.Fragment>
  );
}
