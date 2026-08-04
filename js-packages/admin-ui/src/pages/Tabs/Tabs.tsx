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
import TranslationDialog from "../../components/Form/Modals/translateModal";
import { ADD_TAB_TRANSLATION } from "./gql";
import {
  useAddTabToBucketMutation,
  useDeleteTabsMutation,
  useTabsQuery,
  useUnboundBucketsByTabQuery,
} from "../../graphql-generated";

export function Tabs() {
  const { t } = useTranslation();
  const tabsQuery = useTabsQuery();
  const toast = useToast();
  const [deleteTabMutate] = useDeleteTabsMutation({
    refetchQueries: ["Tabs"],
    onCompleted(data) {
      if (data.deleteTab?.id) {
        toast({
          title: t("pages.tabs.deleted-title"),
          content: t("pages.tabs.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.tabs.delete-error-content"),
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
  const [isAddTranslation, setIsAddTranslation] = React.useState({ id: null, isVisible: false });

  const unboundListEnrichPipeline = useUnboundBucketsByTabQuery({
    variables: { id: Number(isAdd?.id) },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });
  const [addMutate] = useAddTabToBucketMutation({
    refetchQueries: [],
  });

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.tabs.title")}
          </Typography>
          <Typography variant="body1">{t("pages.tabs.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/tab/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.tabs.create-new")}
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
          maxVisibleActions={2}
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
              label: t("common.add"),
              action: (bucket) => {
                setIsAdd({ id: bucket.id, isVisible: true });
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
              action: (tabs) => {
                navigate(`/tab/${tabs?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (tabs) => {
                tabs.id &&
                  navigate(`/tab/${tabs?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (tab) => {
                tab.id && setViewDeleteModal({ view: true, id: tab.id });
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
            {
              header: t("common.priority"),
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
          title={t("modal.confirm-deletion")}
          body={t("pages.tabs.delete-body")}
          labelConfirm={t("common.delete")}
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
          messageSuccess={t("pages.tabs.associated")}
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
          title={t("pages.tabs.association-title")}
          callbackClose={() => {
            setIsAdd({ id: null, isVisible: false });
          }}
        />
      )}

      {isAddTranslation.isVisible && (
        <TranslationDialog
          isOpen={isAddTranslation.isVisible}
          onClose={() => setIsAddTranslation({ id: null, isVisible: false })}
          entityType="tab"
          entityId={String(isAddTranslation.id || "")}
          customMutation={ADD_TAB_TRANSLATION}
        />
      )}
    </Container>
  );
}
