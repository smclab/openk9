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
import { formatDate } from "@components/common";
import { Table } from "@components/Table/Table";
import { Box, Button, Container, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useDataIndicesQuery } from "../../graphql-generated";

export function Dataindices() {
  const dataIndicesQuery = useDataIndicesQuery({ variables: { first: 10 } });
  const navigate = useNavigate();
  const isLoading = dataIndicesQuery.loading;

  if (isLoading) {
    return null;
  }

  return (
    <React.Fragment>
      <Container maxWidth={false}>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box sx={{ width: "50%", ml: 2 }}>
            <Typography component="h1" variant="h1" fontWeight="600">
              Data Indices
            </Typography>
            <Typography variant="body1">
              In this section you can create and handle Data Indices. A Data Index permits to define a mapping for a
              search index.
            </Typography>
          </Box>
          <Box>
            <Link to={"/dataindex/new/mode/edit"} style={{ textDecoration: "none" }}>
              <div style={{ display: "flex" }}>
                <Button variant="contained" color="error" style={{ marginLeft: "auto" }}>
                  Create New Data Indices
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
                label: "View",
                action: (dataIndices) => {
                  navigate(`/dataindex/${dataIndices.id}/mode/view`, {
                    replace: true,
                  });
                },
              },
            ]}
            onCreatePath="/data-indices/new"
            edgesPath="dataIndices.edges"
            pageInfoPath="dataIndices.pageInfo"
            onDelete={(dataIndices) => {}}
            columns={[
              {
                header: "Name",
                content: (dataIndice) => <Box fontWeight="bolder">{dataIndice?.name}</Box>,
              },
              {
                header: "Description",
                content: (dataIndice) => (
                  <Typography variant="body2" className="pipeline-title">
                    {dataIndice?.description}
                  </Typography>
                ),
              },
              {
                header: "Date create",
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

