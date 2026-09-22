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
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import {
  useAddEnrichItemToEnrichPipelineMutation,
  useDeleteEnrichItemMutation,
  useEnrichItemsQuery,
  useUnboundEnrichPipelinesQuery,
} from "../../graphql-generated";

export function EnrichItems() {
  const { t } = useTranslation();
  const enrichItemsQuery = useEnrichItemsQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [addMutate] = useAddEnrichItemToEnrichPipelineMutation({
    refetchQueries: [],
  });
  const [deleteEnrichItemMutate] = useDeleteEnrichItemMutation({
    refetchQueries: ["EnrichItems"],
    onCompleted(data) {
      if (data.deleteEnrichItem?.id) {
        toast({
          title: t("pages.enrich-items.deleted-title"),
          content: t("pages.enrich-items.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.enrich-items.delete-error-content"),
        displayType: "error",
      });
    },
  });

  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });
  const [viewDeleteModal, setViewDeleteModal] = React.useState<{
    view: boolean;
    id: string | undefined;
  }>({
    view: false,
    id: undefined,
  });
  const unboundListEnrichPipeline = useUnboundEnrichPipelinesQuery({
    variables: { itemId: Number(isAdd?.id) },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.enrich-items.title")}
          </Typography>
          <Typography variant="body1">{t("pages.enrich-items.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/enrich-item/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.enrich-items.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: enrichItemsQuery,
            field: (data) => data?.enrichItems,
          }}
          onCreatePath="/enrich-items/new"
          edgesPath="enrichItems.edges"
          pageInfoPath="enrichItems.pageInfo"
          rowActions={[
            {
              label: t("common.add"),
              action: (enrichItem) => {
                setIsAdd({ id: enrichItem?.id, isVisible: true });
              },
            },
            {
              label: t("common.view"),
              action: (enrichItem) => {
                navigate(`/enrich-item/${enrichItem?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (enrichItem) => {
                enrichItem.id &&
                  navigate(`/enrich-item/${enrichItem?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (enrichItem) => {
                enrichItem.id && setViewDeleteModal({ view: true, id: enrichItem.id });
              },
            },
          ]}
          onDelete={(enrichItem) => {
            if (enrichItem?.id) deleteEnrichItemMutate({ variables: { id: enrichItem.id } });
          }}
          columns={[
            {
              header: t("common.name"),
              content: (enrich) => <Box fontWeight="bolder">{enrich?.name}</Box>,
            },
            {
              header: t("common.description"),
              content: (enrichItem) => enrichItem?.description,
            },
          ]}
        />
      </Box>

      {isAdd.isVisible && (
        <ModalAddSingle
          id={isAdd.id}
          callbackClose={() => setIsAdd({ id: null, isVisible: false })}
          title={t("pages.enrich-items.association-title")}
          messageSuccess={t("pages.enrich-items.added-to-pipeline")}
          list={unboundListEnrichPipeline.data?.unboundEnrichPipelines}
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
        />
      )}

      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.enrich-items.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteEnrichItemMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

