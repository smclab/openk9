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
import { ModalAddSingle, ModalConfirm, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import {
  useAddCharFiltersToAnalyzerMutation,
  useCharfiltersQuery,
  useDeleteCharFiltersMutation,
  useUnboundAnalyzersByCharFilterQuery,
} from "../../graphql-generated";

export function CharFilters() {
  const { t } = useTranslation();
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
          title: t("pages.char-filters.deleted-title"),
          content: t("pages.char-filters.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.char-filters.delete-error-content"),
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
            {t("pages.char-filters.title")}
          </Typography>
          <Typography variant="body1">{t("pages.char-filters.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/char-filter/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary" aria-label={t("pages.char-filters.create-new-aria")}>
              {t("pages.char-filters.create-new")}
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
              label: t("common.add"),
              action: (charFilters) => {
                setIsAdd({ id: charFilters.id, isVisible: true });
              },
            },
            {
              label: t("common.view"),
              action: (charFilters) => {
                navigate(`/char-filter/${charFilters?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (charFilters) => {
                charFilters.id &&
                  navigate(`/char-filter/${charFilters?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
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
              header: t("common.name"),
              content: (pluginDriver) => <Box fontWeight="bolder"> {pluginDriver?.name}</Box>,
            },
            {
              header: t("common.description"),
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
          title={t("modal.confirm-deletion")}
          body={t("pages.char-filters.delete-body")}
          labelConfirm={t("common.delete")}
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
          messageSuccess={t("pages.char-filters.added-to-analyzer")}
          title={t("pages.char-filters.association-title")}
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

