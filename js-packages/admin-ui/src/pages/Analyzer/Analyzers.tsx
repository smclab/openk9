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
import { useAnalyzersQuery, useDeleteAnalyzerMutation } from "../../graphql-generated";

export function Analyzers() {
  const { t } = useTranslation();
  const analyzerQuery = useAnalyzersQuery();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const toast = useToast();
  const navigate = useNavigate();
  const [deleteAnalyzerMutate] = useDeleteAnalyzerMutation({
    refetchQueries: ["Analyzers"],
    onCompleted(data) {
      if (data.deleteAnalyzer?.id) {
        toast({
          title: t("pages.analyzers.deleted-title"),
          content: t("pages.analyzers.deleted-content"),
          displayType: "success",
        });
      }
      analyzerQuery.refetch();
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.analyzers.delete-error-content"),
        displayType: "error",
      });
    },
  });

  const isLoading = analyzerQuery.loading;

  if (isLoading) {
    return null;
  }

  return (
    <React.Fragment>
      <Container maxWidth="xl">
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              {t("pages.analyzers.title")}
            </Typography>
            <Typography variant="body1">{t("pages.analyzers.description")}</Typography>
          </Box>
          <Box>
            <Link to="/analyzer/new" style={{ textDecoration: "none" }}>
              <Button variant="contained" color="primary" aria-label={t("pages.analyzers.create-new-aria")}>
                {t("pages.analyzers.create-new")}
              </Button>
            </Link>
          </Box>
        </Box>

        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: analyzerQuery,
              field: (data) => data?.analyzers,
            }}
            onCreatePath="/analyzer/new"
            onDelete={(analyzer) => {
              if (analyzer?.id)
                deleteAnalyzerMutate({
                  variables: { id: analyzer.id },
                });
            }}
            edgesPath="analyzers.edges"
            pageInfoPath="analyzers.pageInfo"
            rowActions={[
              // {
              //   label: t("common.start"),
              //   action: (analyzer) => {
              //     if (analyzer?.id)
              //       updateBucketsMutate({
              //         variables: { id: analyzer.id },
              //       });
              //   },
              // },
              {
                label: t("common.view"),
                action: (analyzer) => {
                  if (analyzer?.id) navigate(`/analyzer/${analyzer?.id}/view`);
                },
              },
              {
                label: t("common.edit"),
                action: (analyzer) => {
                  if (analyzer?.id)
                    navigate(`/analyzer/${analyzer?.id}`, {
                      replace: true,
                    });
                },
              },
              {
                label: t("common.delete"),
                action: (analyzer) => {
                  analyzer?.id && setViewDeleteModal({ view: true, id: analyzer.id });
                },
              },
            ]}
            columns={[
              {
                header: t("common.name"),
                content: (analyzer) => <Box fontWeight="bolder">{analyzer?.name}</Box>,
              },
              {
                header: t("common.description"),
                content: (analyzer) => (
                  <Typography variant="body2" className="pipeline-title">
                    {analyzer?.description}
                  </Typography>
                ),
              },
              {
                header: t("common.type"),
                content: (analyzer) => (
                  <Typography variant="body2" className="pipeline-title">
                    {analyzer?.type}
                  </Typography>
                ),
              },
            ]}
          />
        </Box>

        {viewDeleteModal.view && (
          <ModalConfirm
            title={t("modal.confirm-deletion")}
            body={t("pages.analyzers.delete-body")}
            labelConfirm={t("common.delete")}
            actionConfirm={() => {
              deleteAnalyzerMutate({
                variables: { id: viewDeleteModal.id || "" },
              });
            }}
            close={() => setViewDeleteModal({ id: undefined, view: false })}
          />
        )}
      </Container>
    </React.Fragment>
  );
}

