import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { ModalAddSingle, ModalConfirm } from "@components/Form";
import { Table } from "../../components/Table/Table";
import {
  useAddTokenFilterToAnalyzerMutation,
  useDeleteTokenFiltersMutation,
  useTokenFiltersQuery,
  useUnboundAnalyzersByTokenFilterQuery,
} from "../../graphql-generated";

export function TokenFilters() {
  const tokenFiltersQuery = useTokenFiltersQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });
  const unboundListAnalyzer = useUnboundAnalyzersByTokenFilterQuery({
    variables: { tokenFilterId: Number(isAdd?.id) },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });
  const toast = useToast();
  const [addMutate] = useAddTokenFilterToAnalyzerMutation();
  const [deleteTokenFiltersMutate] = useDeleteTokenFiltersMutation({
    refetchQueries: ["TokenFilters"],
    onCompleted(data) {
      if (data.deleteTokenFilter?.id) {
        toast({
          title: "Token Filter Deleted",
          content: "Token Filter has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Token Filter",
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
            Token Filters
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Token Filters to use to define advanced analysis logic and bind to
            a custom Analyzer. To go into detail about Token Filters check official Opensearch Documentation.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/token-filter/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Token Filters
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: tokenFiltersQuery,
            field: (data) => data?.tokenFilters,
          }}
          edgesPath="tokenFilters.edges"
          pageInfoPath="tokenFilters.pageInfo"
          onCreatePath="/token-filter/new"
          onDelete={(tokenFilters) => {
            if (tokenFilters?.id)
              deleteTokenFiltersMutate({
                variables: { id: tokenFilters.id },
              });
          }}
          rowActions={[
            {
              label: "Add",
              action: (tokenFilters) => {
                setIsAdd({ id: tokenFilters.id, isVisible: true });
              },
            },
            {
              label: "View",
              action: (tokenFilters) => {
                navigate(`/token-filter/${tokenFilters?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (tokenFilters) => {
                tokenFilters.id &&
                  navigate(`/token-filter/${tokenFilters?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (tokenfilters) => {
                if (tokenfilters?.id) setViewDeleteModal({ view: true, id: tokenfilters.id });
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
          ]}
        />
      </Box>
      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this token filters? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteTokenFiltersMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}

      {isAdd.isVisible && (
        <ModalAddSingle
          id={isAdd.id}
          list={unboundListAnalyzer.data?.unboundAnalyzersByTokenFilter}
          messageSuccess="Token Filter added to Analyzer"
          title="Association to Analyzer"
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
