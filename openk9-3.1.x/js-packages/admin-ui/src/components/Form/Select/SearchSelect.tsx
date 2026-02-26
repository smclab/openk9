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
import React from "react";
import {
  Button,
  TextField,
  Modal,
  Box,
  Typography,
  ListItem,
  ListItemText,
  IconButton,
  CircularProgress,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import { Virtuoso } from "react-virtuoso";
import { MutationHook, QueryHook } from "../Hook";
import useDebounced from "@components/common/useDebounced";

export function SearchSelect<
  Value,
  Change extends Record<string, any>,
  Remove extends Record<string, any>,
>({
  label,
  value,
  useValueQuery,
  useOptionsQuery,
  useChangeMutation,
  mapValueToMutationVariables,
  useRemoveMutation,
  mapValueToRemoveMutationVariables,
  invalidate,
  description,
  disabled,
}: {
  label: string;
  value: Value | null | undefined;
  disabled: boolean;
  description?: string;
  useValueQuery: QueryHook<
    {
      value?: {
        id?: string | null;
        name?: string | null;
        description?: string | null;
      } | null;
    },
    { id: Value }
  >;
  useOptionsQuery: QueryHook<
    {
      options?: {
        edges?: Array<{
          node?: {
            id?: string | null;
            name?: string | null;
            description?: string | null;
          } | null;
        } | null> | null;
        pageInfo?: { hasNextPage: boolean; endCursor?: string | null } | null;
      } | null;
    },
    { searchText?: string | null; cursor?: string | null }
  >;
  mapValueToMutationVariables(id: string): Change;
  useChangeMutation: MutationHook<any, Change>;
  mapValueToRemoveMutationVariables(): Remove;
  useRemoveMutation: MutationHook<any, Remove>;
  invalidate(): void;
}) {
  const [searchText, setSearchText] = React.useState("");
  const searchTextDebounced = useDebounced(searchText);
  const valueQuery = useValueQuery({
    variables: { id: value as Value },
    skip: !value,
  });
  const optionsQuery = useOptionsQuery({
    variables: { searchText: searchTextDebounced },
  });
  const [changeMutate, changeMutation] = useChangeMutation({});
  const [removeMutate, removeMutation] = useRemoveMutation({});
  const [open, setOpen] = React.useState(false);

  const handleClose = () => setOpen(false);

  return (
    <Box>
      <Typography variant="subtitle1">{label}</Typography>
      {description && <Typography variant="body2">{description}</Typography>}

      <Box display="flex" gap={1} mt={2}>
        <TextField
          fullWidth
          variant="outlined"
          value={valueQuery.data?.value?.name ?? ""}
          InputProps={{ readOnly: true }}
          disabled={!value || disabled}
        />
        <Button
          variant="outlined"
          disabled={disabled}
          onClick={() => setOpen(true)}
        >
          Change
        </Button>
        <Button
          variant="outlined"
          color="error"
          disabled={!valueQuery.data?.value?.name || disabled}
          onClick={() => {
            if (!changeMutation.loading && !removeMutation.loading) {
              removeMutate({
                variables: mapValueToRemoveMutationVariables(),
                onCompleted: () => {
                  invalidate();
                },
              });
            }
          }}
        >
          Remove
        </Button>
      </Box>

      <Modal open={open} onClose={handleClose}>
        <Box>
          <Typography variant="h6" mb={2}>
            {label}
          </Typography>
          <TextField
            fullWidth
            placeholder="Search..."
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
            InputProps={{
              endAdornment: <SearchIcon />,
            }}
          />
          <Box mt={2} height="300px">
            {optionsQuery.loading ? (
              <CircularProgress />
            ) : (
              <Virtuoso
                style={{ height: "300px" }}
                totalCount={optionsQuery.data?.options?.edges?.length || 0}
                itemContent={(index) => {
                  const row = optionsQuery.data?.options?.edges?.[index]?.node;
                  return (
                    <ListItem
                      key={row?.id}
                      secondaryAction={
                        <IconButton
                          edge="end"
                          onClick={() => {
                            if (row?.id) {
                              changeMutate({
                                variables: mapValueToMutationVariables(row.id),
                                onCompleted: () => {
                                  handleClose();
                                },
                              });
                            }
                          }}
                        >
                          <SearchIcon />
                        </IconButton>
                      }
                    >
                      <ListItemText
                        primary={row?.name || "No Name"}
                        secondary={row?.description || ""}
                      />
                    </ListItem>
                  );
                }}
                endReached={() => {
                  if (optionsQuery.data?.options?.pageInfo?.hasNextPage) {
                    optionsQuery.fetchMore({
                      variables: {
                        cursor: optionsQuery.data.options.pageInfo.endCursor,
                      },
                    });
                  }
                }}
              />
            )}
          </Box>
          <Box mt={2} textAlign="right">
            <Button variant="contained" onClick={handleClose}>
              Cancel
            </Button>
          </Box>
        </Box>
      </Modal>
    </Box>
  );
}

