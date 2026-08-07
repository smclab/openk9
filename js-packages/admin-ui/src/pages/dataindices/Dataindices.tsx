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
import { useFormatDate } from "@components/common";
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { useDataIndicesQuery, useDeleteDataIndexMutation } from "../../graphql-generated";
import { useToast } from "@components/Form";

export function Dataindices() {
  const { t } = useTranslation();
  const formatDate = useFormatDate();
  const dataIndicesQuery = useDataIndicesQuery({ variables: { first: 10 } });
  const navigate = useNavigate();
  const toast = useToast();
  const isLoading = dataIndicesQuery.loading;
  const [deleteDataIndex] = useDeleteDataIndexMutation({
    refetchQueries: ["DataIndices"],
    onCompleted(data) {
      if (data.deleteDataIndex?.id) {
        toast({
          title: t("pages.data-indices.deleted-title"),
          content: t("pages.data-indices.deleted-content"),
          displayType: "success",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("common.delete-error"),
        content: t("pages.data-indices.delete-error-content"),
        displayType: "error",
      });
    },
  });
  if (isLoading) {
    return null;
  }

  return (
    <React.Fragment>
      <Container maxWidth={false}>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              {t("pages.data-indices.title")}
            </Typography>
            <Typography variant="body1">{t("pages.data-indices.description")}</Typography>
          </Box>
          <Box>
            <Link to={"/dataindex/new/mode/edit"} style={{ textDecoration: "none" }}>
              <div style={{ display: "flex" }}>
                <Button variant="contained" color="error" style={{ marginLeft: "auto" }}>
                  {t("pages.data-indices.create-new")}
                </Button>
              </div>
            </Link>
          </Box>
        </Box>

        <Box display="flex" gap="23px" mt={3}>
          <Table
            data={{
              queryResult: dataIndicesQuery,
              field: (data) => data?.dataIndices,
            }}
            rowActions={[
              {
                label: t("common.view"),
                action: (dataIndices) => {
                  navigate(`/dataindex/${dataIndices.id}/mode/view`, {
                    replace: true,
                  });
                },
              },
            ]}
            onDelete={() => {}}
            onCreatePath="/data-indices/new"
            edgesPath="dataIndices.edges"
            pageInfoPath="dataIndices.pageInfo"
            deleted={{
              title: t("pages.data-indices.delete-title"),
              messsage: t("pages.data-indices.delete-message"),
              wordConfirm: t("common.delete"),
              actionDeleted: (id: string, name: string) => {
                deleteDataIndex({ variables: { dataIndexId: id, dataIndexName: name } });
              },
            }}
            columns={[
              {
                header: t("common.name"),
                content: (dataIndice) => <Box fontWeight="bolder">{dataIndice?.name}</Box>,
              },
              {
                header: t("common.description"),
                content: (dataIndice) => (
                  <Typography variant="body2" className="pipeline-title">
                    {dataIndice?.description}
                  </Typography>
                ),
              },
              {
                header: t("pages.data-indices.date-create"),
                content: (dataIndice) => (
                  <Typography variant="body2" className="pipeline-title">
                    {formatDate(dataIndice?.createDate)}
                  </Typography>
                ),
              },
            ]}
          />
        </Box>
      </Container>
    </React.Fragment>
  );
}
