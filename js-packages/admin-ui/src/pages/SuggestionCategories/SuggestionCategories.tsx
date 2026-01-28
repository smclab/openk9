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
  useAddSuggestionCategoryToBucketMutation,
  useDeleteSuggestionCategoryMutation,
  useSuggestionCategoriesQuery,
  useUnboundBucketsBySuggestionCategoryQuery,
} from "../../graphql-generated";
import TranslationDialog from "../../components/Form/Modals/translateModal";
import { ADD_SUGGESTION_CATEGORY_TRANSLATION } from "./gql";

export function SuggestionCategories() {
  const suggestionCategoriesQuery = useSuggestionCategoriesQuery();
  const navigate = useNavigate();
  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });
  const [isAddTranslation, setIsAddTranslation] = React.useState({ id: null, isVisible: false });
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const unboundListSuggestionCategory = useUnboundBucketsBySuggestionCategoryQuery({
    variables: { id: Number(isAdd?.id) },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });
  const [addMutate] = useAddSuggestionCategoryToBucketMutation();
  const toast = useToast();
  const [deleteSuggestionCategoryMutate] = useDeleteSuggestionCategoryMutation({
    refetchQueries: ["SuggestionCategories"],
    onCompleted(data) {
      if (data.deleteSuggestionCategory?.id) {
        toast({
          title: "Filter Deleted",
          content: "Filter has been deleted successfully",
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: "Error Delete",
        content: "Impossible to delete Filter",
        displayType: "error",
      });
    },
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            Filters
          </Typography>
          <Typography variant="body1">
            In this section you can create and handle Suggestion Categories to define search filters. Add them to Bucket
            to make it usable.
          </Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/suggestion-category/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              Create New Filter
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: suggestionCategoriesQuery,
            field: (data) => data?.suggestionCategories,
          }}
          edgesPath="suggestionCategories.edges"
          pageInfoPath="suggestionCategories.pageInfo"
          onCreatePath="/suggestion-categories/new"
          onDelete={(suggestionCategory: any) => {
            suggestionCategory?.id && setViewDeleteModal({ view: true, id: suggestionCategory.id });
          }}
          rowActions={[
            {
              label: "Add",
              action: (datasources) => {
                setIsAdd({ id: datasources.id, isVisible: true });
              },
            },
            {
              label: "Add Translation",
              action: (datasources) => {
                setIsAddTranslation({ id: datasources.id, isVisible: true });
              },
            },
            {
              label: "View",
              action: (suggestionCategory) => {
                navigate(`/suggestion-category/${suggestionCategory?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: "Edit",
              action: (suggestionCategory) => {
                suggestionCategory.id &&
                  navigate(`/suggestion-category/${suggestionCategory?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: "Delete",
              action: (suggestionCategory) => {
                suggestionCategory.id && setViewDeleteModal({ view: true, id: suggestionCategory.id });
              },
            },
          ]}
          columns={[
            {
              header: "Name",
              content: (suggestionCategory) => <Box fontWeight="bolder">{suggestionCategory?.name}</Box>,
            },
            {
              header: "Description",
              content: (suggestionCategory) => (
                <Typography variant="body2" className="pipeline-title">
                  {suggestionCategory?.description}
                </Typography>
              ),
            },
            {
              header: "Priority",
              content: (suggestionCategory) => suggestionCategory?.priority,
            },
          ]}
        />
      </Box>

      {viewDeleteModal.view && (
        <ModalConfirm
          title="Confirm Deletion"
          body="Are you sure you want to delete this Filter? This action is irreversible and all associated data will be lost."
          labelConfirm="Delete"
          actionConfirm={() => {
            deleteSuggestionCategoryMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}

      {isAdd.isVisible && (
        <ModalAddSingle
          id={isAdd.id}
          list={unboundListSuggestionCategory.data?.unboundBucketsBySuggestionCategory}
          messageSuccess="Filter added to Bucket"
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
      {isAddTranslation.isVisible && (
        <TranslationDialog
          isOpen={isAddTranslation.isVisible}
          onClose={() => setIsAddTranslation({ id: null, isVisible: false })}
          entityId={String(isAddTranslation.id || "")}
          customMutation={ADD_SUGGESTION_CATEGORY_TRANSLATION}
        />
      )}
    </Container>
  );
}

