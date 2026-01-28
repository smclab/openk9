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
import { gql, useQuery } from "@apollo/client";
import DatasourcesSection from "@components/Form/Dashboard/datasourceCards";
import { useRestClient } from "@components/queryClient";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import VisibilityIcon from "@mui/icons-material/Visibility";
import { Box, Container, Divider, IconButton, Menu, MenuItem, Stack, Typography, useTheme } from "@mui/material";
import { DataSourcesQuery } from "@pages/datasources/gql";
import React from "react";
import { Link } from "react-router-dom";
import { useDataIndexInformationQuery } from "../../graphql-generated";
import DashboardCard from "./DashboardCard";
import DashboardInfoRow from "./DashboardInfoRow";
import { scheduler } from "./gql";
import { DetailGraph } from "./Graph";

export const DataIndexInformation = gql`
  query dataIndexInformation {
    buckets {
      edges {
        node {
          datasources {
            edges {
              node {
                dataIndex {
                  cat {
                    docsCount
                    docsDeleted
                    health
                    index
                    pri
                    priStoreSize
                    rep
                    status
                    storeSize
                    uuid
                  }
                }
              }
            }
          }
        }
      }
    }
  }
`;

export function bytesToMegabytes(bytes: number) {
  const megabytes = bytes / (1024 * 1024);
  return parseFloat(megabytes.toFixed(4));
}

type SchedulerData = {
  schedulers: {
    edges: {
      node: {
        id: string;
        modifiedDate: string;
        errorDescription: string;
        lastIngestionDate: string;
        status: string;
        datasource: {
          id: string;
          name: string;
        };
        newDataIndex: {
          id: string;
          name: string;
        };
      };
    }[];
  };
};

export function DashBoard() {
  const dashboardQuery = useDataIndexInformationQuery();
  const theme = useTheme();
  const { data: datasourcesFetched } = useQuery(DataSourcesQuery, {
    variables: {
      first: 5,
      sortByList: [{ column: "modifiedDate", direction: "DESC" }],
    },
  });
  const { data: schedulerError } = useQuery(scheduler, {
    variables: {
      searchText: "ERROR",
    },
    fetchPolicy: "network-only",
  }) as { data: SchedulerData };
  const { data: schedulerFailure } = useQuery(scheduler, {
    variables: {
      searchText: "FAILURE",
    },
    fetchPolicy: "network-only",
  }) as { data: SchedulerData };
  type OriginType = "ERROR" | "FAILURE";
  const schedulerErrorWithOrigin = (schedulerError?.schedulers?.edges || []).map((item) => ({
    ...item,
    origin: "ERROR" as OriginType,
  }));
  const schedulerFailureWithOrigin = (schedulerFailure?.schedulers?.edges || []).map((item) => ({
    ...item,
    origin: "FAILURE" as OriginType,
  }));
  const scheulerData = [...schedulerErrorWithOrigin, ...schedulerFailureWithOrigin];
  const datasourcesData = datasourcesFetched?.datasources?.edges?.map((edge: any) => edge.node) || [];

  const recoveryDocsDeleted = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.docsDeleted))
    .filter((arr) => arr != null && arr.length > 0);

  const documentDeleted = recoveryDocsDeleted
    ?.flat()
    .reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

  const docsCount = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.docsCount))
    .filter((arr) => arr != null && arr.length > 0);

  const docCount = docsCount?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);

  const recoveryByteCount = dashboardQuery.data?.buckets?.edges
    ?.map((edge) => edge?.node?.datasources?.edges?.map((datasource) => datasource?.node?.dataIndex?.cat?.priStoreSize))
    .filter((arr) => arr != null && arr.length > 0);

  const byteCount = recoveryByteCount?.flat().reduce((acc, singleIndex) => acc + parseFloat(singleIndex ?? "0"), 0);
  const [expandedIndex, setExpandedIndex] = React.useState<number | null>(null);
  const handleExpandClick = (index: number) => {
    setExpandedIndex(expandedIndex === index ? null : index);
  };

  const [actionMenuAnchor, setActionMenuAnchor] = React.useState<null | HTMLElement>(null);
  const [actionMenuIndex, setActionMenuIndex] = React.useState<number | null>(null);
  const restClient = useRestClient();

  const handleActionMenuOpen = (event: React.MouseEvent<HTMLElement>, index: number) => {
    setActionMenuAnchor(event.currentTarget);
    setActionMenuIndex(index);
  };
  const handleActionMenuClose = () => {
    setActionMenuAnchor(null);
    setActionMenuIndex(null);
  };

  const [filter, setFilter] = React.useState<{ ERROR: boolean; FAILURE: boolean }>({
    ERROR: true,
    FAILURE: true,
  });

  const handleLegendClick = (type: "ERROR" | "FAILURE") => {
    setFilter((prev) => ({
      ...prev,
      [type]: !prev[type],
    }));
  };

  const filteredSchedulerData = scheulerData.filter((item) => filter[item.origin]);

  return (
    <Container style={{ padding: 0 }}>
      <DetailGraph
        firstCardNumber={docCount || 0}
        secondCardNumber={documentDeleted || 0}
        thirdCardNumber={bytesToMegabytes(byteCount) || 0}
        firstCardLabel={"Document counts"}
        secondCardLabel={"Document deleted"}
        thirdCardLabel={"Store size megabyte"}
        thirdCardUnity={""}
      />
      <Box display="grid" gridTemplateColumns={{ xs: "1fr", md: "1fr min(360px)" }} gap="24px" marginTop={"2rem"}>
        <DatasourcesSection datasourcesData={datasourcesData} />
        <DashboardCard
          title={
            <Box display={"flex"} flexDirection="row" gap={1} justifyContent={"space-between"} alignItems="center">
              <Typography variant="h4" sx={{ fontWeight: 600, color: "text.primary", mb: 0.5 }}>
                Alert schedulations
              </Typography>
              <Box display="flex" alignItems="center" gap={2}>
                <Box
                  display="flex"
                  alignItems="center"
                  gap={0.5}
                  sx={{
                    cursor: "pointer",
                    opacity: filter.ERROR ? 1 : 0.4,
                  }}
                  onClick={() => handleLegendClick("ERROR")}
                >
                  <ErrorOutlineIcon sx={{ color: theme.palette.error.main, fontSize: 18 }} />
                  <Typography variant="caption" color="error" fontWeight={600}>
                    Error
                  </Typography>
                </Box>
                <Box
                  display="flex"
                  alignItems="center"
                  gap={0.5}
                  sx={{
                    cursor: "pointer",
                    opacity: filter.FAILURE ? 1 : 0.4,
                  }}
                  onClick={() => handleLegendClick("FAILURE")}
                >
                  <ErrorOutlineIcon sx={{ color: theme.palette.warning.main, fontSize: 18 }} />
                  <Typography variant="caption" color="warning.main" fontWeight={600}>
                    Failure
                  </Typography>
                </Box>
              </Box>
            </Box>
          }
          sx={{
            minHeight: "100%",
            display: "flex",
            flexDirection: "column",
            justifyContent: "flex-start",
          }}
        >
          <Stack spacing={2}>
            {filteredSchedulerData.length === 0 && (
              <Box display="flex" flexDirection="column" alignItems="center" justifyContent="center" py={3}>
                <ErrorOutlineIcon sx={{ color: theme.palette.text.disabled, fontSize: 40, mb: 1 }} />
                <Typography variant="body2" color="text.secondary">
                  There is no alert schedule present
                </Typography>
              </Box>
            )}
            {filteredSchedulerData?.map((item, index, arr) => {
              const date = new Date(item.node.modifiedDate);
              const formattedDate = date.toLocaleString("it-IT", {
                day: "2-digit",
                month: "2-digit",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit",
              });
              const isError = item.origin === "ERROR";
              const iconColor = isError ? theme.palette.error.main : theme.palette.warning.main;
              const borderColor = isError ? theme.palette.error.main : theme.palette.warning.main;

              return (
                <React.Fragment key={index}>
                  <Box>
                    <DashboardInfoRow
                      icon={
                        <ErrorOutlineIcon
                          sx={{
                            color: iconColor,
                            fontSize: 26,
                          }}
                        />
                      }
                      label={item.node.id}
                      subtitle={formattedDate}
                      borderColor={borderColor}
                      extraContent={
                        <Stack direction="row" spacing={1} alignItems="center">
                          <Link to={`/notificationInfo/${item.node.id}`}>
                            <IconButton size="small">
                              <VisibilityIcon />
                            </IconButton>
                          </Link>
                          <IconButton size="small" onClick={() => handleExpandClick(index)} aria-label="Show Error">
                            <ArrowDropDownIcon
                              sx={{
                                transform: expandedIndex === index ? "rotate(180deg)" : "rotate(0deg)",
                                transition: "transform 0.2s",
                              }}
                            />
                          </IconButton>
                          {isError && (
                            <IconButton
                              size="small"
                              onClick={(e) => handleActionMenuOpen(e, index)}
                              aria-label="Azioni"
                            >
                              <MoreVertIcon />
                            </IconButton>
                          )}
                          {/* Menu delle azioni */}
                          <Menu
                            anchorEl={actionMenuAnchor}
                            open={actionMenuIndex === index}
                            onClose={handleActionMenuClose}
                            anchorOrigin={{
                              vertical: "bottom",
                              horizontal: "right",
                            }}
                            transformOrigin={{
                              vertical: "top",
                              horizontal: "right",
                            }}
                          >
                            <MenuItem
                              onClick={async () => {
                                await restClient.schedulerResource.postApiDatasourceSchedulersRerouteScheduling(
                                  Number(item.node.id),
                                );
                              }}
                            >
                              Reprocess failed messages
                            </MenuItem>
                            <MenuItem
                              onClick={async () => {
                                await restClient.schedulerResource.postApiDatasourceSchedulersCloseScheduling(
                                  Number(item.node.id),
                                );
                              }}
                            >
                              Close
                            </MenuItem>
                            <MenuItem
                              onClick={async () => {
                                await restClient.schedulerResource.postApiDatasourceSchedulersCancelScheduling(
                                  Number(item.node.id),
                                );
                              }}
                            >
                              Cancel
                            </MenuItem>
                          </Menu>
                        </Stack>
                      }
                    />
                    {expandedIndex === index && (
                      <Box
                        sx={{
                          background: theme.palette.mode === "dark" ? "#2c2a29" : "#f9eaea",
                          color: theme.palette.error.main,
                          borderRadius: 1,
                          mt: 1,
                          mb: 1,
                          px: 2,
                          py: 1,
                          minHeight: 40,
                          display: "flex",
                          alignItems: "center",
                          whiteSpace: "pre-wrap",
                          wordBreak: "break-word",
                          flexWrap: "wrap",
                        }}
                      >
                        <Typography variant="body2" color="error">
                          {item.node.errorDescription || "Nessun errore"}
                        </Typography>
                      </Box>
                    )}
                  </Box>
                  {index < arr.length - 1 && <Divider />}
                </React.Fragment>
              );
            })}
          </Stack>
        </DashboardCard>
      </Box>
    </Container>
  );
}

