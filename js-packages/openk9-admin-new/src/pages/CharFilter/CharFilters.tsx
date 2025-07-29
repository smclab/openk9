import { ModalAddSingle, ModalConfirm, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  useAddCharFiltersToAnalyzerMutation,
  useCharfiltersQuery,
  useDeleteCharFiltersMutation,
  useUnboundAnalyzersByCharFilterQuery,
} from "../../graphql-generated";

export function CharFilters() {
  const charFiltersQuery = useCharfiltersQuery();
  const toast = useToast();
  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });
  const [addMutate] = useAddCharFiltersToAnalyzerMutation();
  const unboundListAnalyzer = useUnboundAnalyzersByCharFilterQuery({
    variables: { charFilterId: Number(isAdd?.id) },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });
  const [deleteCharFilterMutate] = useDeleteCharFiltersMutation({
    refetchQueries: ["Charfilters"],
    onCompleted(data) {
      if (data.deleteCharFilter?.id) {
        toast({
          title: "Char Filter Deleted",
          content: "Char Filter has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Char Filter",
        displayType: "error",
      });
    },
  });
  const navigate = useNavigate();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Char filters
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Char Filters to use to define advanced analysis logic and bind to
            a custom Analyzer. To go into detail about Char Filters check official Opensearch Documentation.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/char-filter/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary" aria-label="new char filters">
              Create New Char Filters
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: charFiltersQuery,
            field: (data) => data?.charFilters,
          }}
          edgesPath="charFilters.edges"
          pageInfoPath="charFilters.pageInfo"
          rowActions={[
            {
              label: "Add",
              action: (charFilters) => {
                setIsAdd({ id: charFilters.id, isVisible: true });
              },
            },
            {
              label: "View",
              action: (charFilters) => {
                navigate(`/char-filter/${charFilters?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (charFilters) => {
                charFilters.id &&
                  navigate(`/char-filter/${charFilters?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (charFilters) => {
                if (charFilters?.id) setViewDeleteModal({ view: true, id: charFilters.id });
              },
            },
          ]}
          onCreatePath="/char-filters/new"
          onDelete={(charfilter) => {
            if (charfilter?.id) deleteCharFilterMutate({ variables: { id: charfilter.id } });
          }}
          columns={[
            {
              header: "Name",
              content: (pluginDriver) => <Box fontWeight="bolder"> {pluginDriver?.name}</Box>,
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
          body="Are you sure you want to delete this char filter? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteCharFilterMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
      {isAdd.isVisible && (
        <ModalAddSingle
          id={isAdd.id}
          list={unboundListAnalyzer.data?.unboundAnalyzersByCharFilter}
          messageSuccess="Char Filter added to Analyzer"
          title="Char Filter to Analyzer"
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
    </Container>
  );
}
