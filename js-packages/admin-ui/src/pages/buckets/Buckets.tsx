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
import { Box, Button, Container, Typography, useTheme } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { ModalConfirm, useToast } from "@components/Form";
import { Table } from "@components/Table/Table";
import { useBucketsQuery, useDeleteBucketMutation, useEnableBucketMutation } from "../../graphql-generated";

export function Buckets() {
  const { t } = useTranslation();
  const bucketsQuery = useBucketsQuery();
  const theme = useTheme();
  const [viewDeleteModal, setViewDeleteModal] = React.useState({
    view: false,
    id: undefined,
  });
  const [updateBucketsMutate] = useEnableBucketMutation({
    refetchQueries: ["Buckets"],
  });
  const navigate = useNavigate();
  const toast = useToast();
  const [deleteBucketMutate] = useDeleteBucketMutation({
    refetchQueries: ["Buckets"],
    onCompleted(data) {
      if (data.deleteBucket?.id) {
        toast({
          title: t("pages.buckets.deleted-title"),
          content: t("pages.buckets.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.buckets.delete-error-content"),
        displayType: "error",
      });
    },
  });

  const isLoading = bucketsQuery.loading;

  if (isLoading) {
    return null;
  }
  return (
    <React.Fragment>
      <Container maxWidth="xl">
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              {t("pages.buckets.title")}
            </Typography>
            <Typography variant="body1">{t("pages.buckets.description")}</Typography>
          </Box>
          <Box>
            <Link to="/bucket/new" style={{ textDecoration: "none" }}>
              <Button variant="contained" color="primary" aria-label={t("pages.buckets.create-new-aria")}>
                {t("pages.buckets.create-new")}
              </Button>
            </Link>
          </Box>
        </Box>

        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: bucketsQuery,
              field: (data) => data?.buckets,
            }}
            onCreatePath="/bucket/new"
            edgesPath="buckets.edges"
            pageInfoPath="buckets.pageInfo"
            onDelete={(bucket) => {
              if (bucket?.id)
                deleteBucketMutate({
                  variables: { id: bucket.id },
                });
            }}
            rowActions={[
              {
                label: t("common.start"),
                isDisabled: (bucket) => !bucket?.enabled,
                action: (bucket) => {
                  if (bucket?.id)
                    updateBucketsMutate({
                      variables: { id: bucket.id },
                    });
                },
              },
              {
                label: t("common.view"),
                action: (bucket) => {
                  if (bucket?.id)
                    navigate(`/bucket/${bucket?.id}/view`, {
                      replace: true,
                    });
                },
              },
              {
                label: t("common.edit"),
                action: (bucket) => {
                  if (bucket?.id)
                    navigate(`/bucket/${bucket?.id}`, {
                      replace: true,
                    });
                },
              },
              {
                label: t("common.delete"),
                action: (bucket) => {
                  if (bucket?.id) setViewDeleteModal({ view: true, id: bucket.id });
                },
              },
            ]}
            columns={[
              {
                header: t("common.name"),
                content: (bucket) => <Box fontWeight="bolder">{bucket?.name}</Box>,
              },
              {
                header: t("common.description"),
                content: (bucket) => (
                  <Typography variant="body2" className="pipeline-title">
                    {bucket?.description}
                  </Typography>
                ),
              },
              {
                header: t("common.status"),
                content: (bucket) => {
                  const statusText = bucket?.enabled ? t("common.active") : t("common.inactive");
                  const backgroundColor = bucket?.enabled ? theme.palette.success.main : theme.palette.grey[500];

                  return (
                    <Typography
                      variant="body2"
                      color={theme.palette.background.paper}
                      sx={{
                        borderRadius: "8px",
                        background: backgroundColor,
                        padding: "8px",
                        maxWidth: "150px",
                      }}
                    >
                      {statusText}
                    </Typography>
                  );
                },
              },
            ]}
          />
        </Box>

        {viewDeleteModal.view && (
          <ModalConfirm
            title={t("modal.confirm-deletion")}
            body={t("pages.buckets.delete-body")}
            labelConfirm={t("common.delete")}
            actionConfirm={() => {
              deleteBucketMutate({
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

