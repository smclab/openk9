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
  useAddTokenTabToTabMutation,
  useDeleteTabTokenMutation,
  useTabTokensQuery,
  useUnassociatedTokenTabsInTabQuery,
} from "../../graphql-generated";
import { evictTokenTabAssociationLists } from "./gql";

export function TokenTabs() {
  const { t } = useTranslation();
  const tabTokensQuery = useTabTokensQuery();
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteTabMutate] = useDeleteTabTokenMutation({
    refetchQueries: ["TabTokens"],
    update(cache, { data }) {
      if (data?.deleteTokenTab?.id) {
        evictTokenTabAssociationLists(cache);
      }
    },
    onCompleted(data) {
      if (data.deleteTokenTab?.id) {
        toast({
          title: t("pages.token-tabs.deleted-title"),
          content: t("pages.token-tabs.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.token-tabs.delete-error-content"),
        displayType: "error",
      });
    },
  });
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const [isAdd, setIsAdd] = React.useState({ id: null, isVisible: false });
  const [addTokenTabToTabMutate] = useAddTokenTabToTabMutation({
    refetchQueries: ["unassociatedTokenTabsInTab"],
  });

  const { data } = useUnassociatedTokenTabsInTabQuery({
    variables: { id: Number(isAdd.id) ?? -1 },
    skip: !isAdd.id,
    fetchPolicy: "network-only",
  });

  const unboundTabsByTokenTab = data?.unboundTabsByTokenTab;

  return (
    <Container maxWidth="xl">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box sx={{ width: "50%", ml: 2 }}>
          <Typography component="h1" variant="h1" fontWeight="600">
            {t("pages.token-tabs.title")}
          </Typography>
          <Typography variant="body1">{t("pages.token-tabs.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/token-tab/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.token-tabs.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: tabTokensQuery,
            field: (data) => data?.totalTokenTabs,
          }}
          onCreatePath="/token-tab/new"
          edgesPath="totalTokenTabs.edges"
          pageInfoPath="totalTokenTabs.pageInfo"
          onDelete={(tabs) => {
            if (tabs?.id)
              deleteTabMutate({
                variables: { id: tabs.id },
              });
          }}
          rowActions={[
            {
              label: t("common.add"),
              action: (datasources) => {
                setIsAdd({ id: datasources.id, isVisible: true });
              },
            },
            {
              label: t("common.view"),
              action: (tabs) => {
                navigate(`/token-tab/${tabs?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (tabs) => {
                tabs.id &&
                  navigate(`/token-tab/${tabs?.id}`, {
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
              header: t("pages.token-tabs.token-type"),
              content: (tab) => (
                <Typography variant="body2" className="token-type-title">
                  {tab?.tokenType}
                </Typography>
              ),
            },
            {
              header: t("pages.token-tabs.value"),
              content: (tab) => (
                <Typography variant="body2" className="value-title">
                  {tab?.value}
                </Typography>
              ),
            },
          ]}
        />
      </Box>

      {viewDeleteModal.view && (
        <ModalConfirm
          title={t("modal.confirm-deletion")}
          body={t("pages.token-tabs.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteTabMutate({ variables: { id: viewDeleteModal.id || "" } });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}

      {isAdd.isVisible && isAdd.id && (
        <ModalAddSingle
          id={isAdd.id}
          messageSuccess={t("pages.token-tabs.associated")}
          list={unboundTabsByTokenTab}
          association={({ parentId, childId, onSuccessCallback, onErrorCallback }) =>
            addTokenTabToTabMutate({
              variables: { parentId, childId },
              onCompleted: () => {
                onSuccessCallback();
              },
              onError: (error) => {
                onErrorCallback(error);
              },
            })
          }
          title={t("pages.token-tabs.association-title")}
          callbackClose={() => {
            setIsAdd({ id: null, isVisible: false });
          }}
        />
      )}
    </Container>
  );
}

