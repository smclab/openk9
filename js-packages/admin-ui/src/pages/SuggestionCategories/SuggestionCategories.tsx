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
import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation();
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
          title: t("pages.filters.deleted-title"),
          content: t("pages.filters.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.filters.delete-error-content"),
        displayType: "error",
      });
    },
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.filters.title")}
          </Typography>
          <Typography variant="body1">{t("pages.filters.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/suggestion-category/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.filters.create-new")}
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
              label: t("common.add"),
              action: (datasources) => {
                setIsAdd({ id: datasources.id, isVisible: true });
              },
            },
            {
              label: t("common.add-translation"),
              action: (datasources) => {
                setIsAddTranslation({ id: datasources.id, isVisible: true });
              },
            },
            {
              label: t("common.view"),
              action: (suggestionCategory) => {
                navigate(`/suggestion-category/${suggestionCategory?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (suggestionCategory) => {
                suggestionCategory.id &&
                  navigate(`/suggestion-category/${suggestionCategory?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (suggestionCategory) => {
                suggestionCategory.id && setViewDeleteModal({ view: true, id: suggestionCategory.id });
              },
            },
          ]}
          columns={[
            {
              header: t("common.name"),
              content: (suggestionCategory) => <Box fontWeight="bolder">{suggestionCategory?.name}</Box>,
            },
            {
              header: t("common.description"),
              content: (suggestionCategory) => (
                <Typography variant="body2" className="pipeline-title">
                  {suggestionCategory?.description}
                </Typography>
              ),
            },
            {
              header: t("common.priority"),
              content: (suggestionCategory) => suggestionCategory?.priority,
            },
          ]}
        />
      </Box>

      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.filters.delete-body")}
          labelConfirm={t("common.delete")}
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
          messageSuccess={t("pages.filters.added-to-bucket")}
          title={t("pages.filters.association-title")}
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

