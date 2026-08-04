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
import { ModalConfirm, useToast } from "@components/Form";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { Table } from "../../components/Table/Table";
import { useDeleteTokenizerMutation, useTokenizersQuery } from "../../graphql-generated";

export function Tokenizers() {
  const { t } = useTranslation();
  const tokenizersQuery = useTokenizersQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const toast = useToast();
  const [deleteTokenizersMutate] = useDeleteTokenizerMutation({
    refetchQueries: ["Tokenizers"],
    onCompleted(data) {
      if (data.deleteTokenizer?.id) {
        toast({
          title: t("pages.tokenizers.deleted-title"),
          content: t("pages.tokenizers.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.tokenizers.delete-error-content"),
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
            {t("pages.tokenizers.title")}
          </Typography>
          <Typography variant="body1">{t("pages.tokenizers.description")}</Typography>
        </Box>
        <Box display="flex" justifyContent="flex-end" mb={3}>
          <Link to="/tokenizer/new" style={{ textDecoration: "none" }}>
            <Button variant="contained" color="primary">
              {t("pages.tokenizers.create-new")}
            </Button>
          </Link>
        </Box>
      </Box>

      <Box display="flex" gap="23px" mt={3}>
        <Table
          data={{
            queryResult: tokenizersQuery,
            field: (data) => data?.tokenizers,
          }}
          edgesPath="tokenizers.edges"
          pageInfoPath="tokenizers.pageInfo"
          rowActions={[
            {
              label: t("common.view"),
              action: (tokenizer) => {
                navigate(`/tokenizer/${tokenizer?.id}/view`, {
                  replace: true,
                });
              },
            },
            {
              label: t("common.edit"),
              action: (tokenizer) => {
                tokenizer.id &&
                  navigate(`/tokenizer/${tokenizer?.id}`, {
                    replace: true,
                  });
              },
            },
            {
              label: t("common.delete"),
              action: (tokenizer) => {
                if (tokenizer?.id) setViewDeleteModal({ view: true, id: tokenizer.id });
              },
            },
          ]}
          onCreatePath="/tokenizers/new"
          onDelete={(tokenizer) => {
            if (tokenizer?.id) deleteTokenizersMutate({ variables: { id: tokenizer.id } });
          }}
          columns={[
            {
              header: t("common.name"),
              content: (pluginDriver) => <Box fontWeight="bolder">{pluginDriver?.name}</Box>,
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
          body={t("pages.tokenizers.delete-body")}
          labelConfirm={t("common.delete")}
          actionConfirm={() => {
            deleteTokenizersMutate({
              variables: { id: viewDeleteModal.id || "" },
            });
          }}
          close={() => setViewDeleteModal({ id: undefined, view: false })}
        />
      )}
    </Container>
  );
}

