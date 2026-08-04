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
import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation();
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
          title: t("pages.token-filters.deleted-title"),
          content: t("pages.token-filters.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.token-filters.delete-error-content"),
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
            {t("pages.token-filters.title")}
          </Typography>
          <Typography variant="body1">{t("pages.token-filters.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/token-filter/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.token-filters.create-new")}
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
              label: t("common.add"),
              action: (tokenFilters) => {
                setIsAdd({ id: tokenFilters.id, isVisible: true });
              },
            },
            {
              label: t("common.view"),
              action: (tokenFilters) => {
                navigate(`/token-filter/${tokenFilters?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (tokenFilters) => {
                tokenFilters.id &&
                  navigate(`/token-filter/${tokenFilters?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (tokenfilters) => {
                if (tokenfilters?.id) setViewDeleteModal({ view: true, id: tokenfilters.id });
              },
            },
          ]}
          columns={[
            {
              header: t("common.name"),
              content: (tab) => <Box fontWeight="bolder">{tab?.name}</Box>,
            },
            {
              header: t("common.description"),
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
          title={t("modal.confirm-deletion")}
          body={t("pages.token-filters.delete-body")}
          labelConfirm={t("common.delete")}
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
          messageSuccess={t("pages.token-filters.added-to-analyzer")}
          title={t("pages.token-filters.association-title")}
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

