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
import AddIcon from "@mui/icons-material/Add";
import CheckIcon from "@mui/icons-material/Check";
import CloseIcon from "@mui/icons-material/Close";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import InsertDriveFileOutlinedIcon from "@mui/icons-material/InsertDriveFileOutlined";
import SearchIcon from "@mui/icons-material/Search";
import {
  Autocomplete,
  Box,
  Button,
  Card,
  CardContent,
  FormControl,
  IconButton,
  List,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { aclOption } from "@pages";
import React, { useState } from "react";
import { defaultActions, row, RowItem } from "./DataCardManager";
import { DataFormElementConfig, RowInfo } from "./types";

type DataCardProps = {
  onCreateClick: () => void;
  config: DataFormElementConfig;
  row: row;
  options: any;
  isSearcheable?: boolean;
  isCreateButtonVisible?: boolean;
};

const FieldItem = ({
  field,
  callbackSaveData,
  closeFieldItem,
  doctypeOptions,
  position,
}: {
  field: RowInfo;
  callbackSaveData?(row: RowInfo | null | undefined, position: number): void;
  closeFieldItem?(): void;
  doctypeOptions: any;
  position: number;
}) => {
  const [tempFields, setTempFields] = useState<RowInfo | null>();
  return (
    <ListItem>
      <Box sx={{ width: "100%" }}>
        <Stack spacing={2}>
          <FormControl fullWidth size="small">
            <Autocomplete
              size="small"
              options={aclOption}
              getOptionLabel={(option) => option.label}
              value={
                aclOption.find((opt) => opt.label === tempFields?.itemLabel) ||
                aclOption.find((opt) => opt.label === field?.itemLabel) ||
                null
              }
              onChange={(_, newValue) => {
                setTempFields({
                  ...tempFields,
                  itemLabel: newValue?.label || "",
                  ItemId: newValue?.value || "",
                });
              }}
              renderInput={(params) => <TextField {...params} label="User Field" placeholder="Select user field..." />}
            />
          </FormControl>

          <FormControl fullWidth size="small">
            <Autocomplete
              size="small"
              options={doctypeOptions}
              getOptionLabel={(option) => option.label}
              value={
                doctypeOptions.find((opt: any) => opt.label === tempFields?.associatedLabel) ||
                doctypeOptions.find((opt: any) => opt.label === field?.associatedLabel) ||
                null
              }
              onChange={(_, newValue) => {
                setTempFields({
                  ...tempFields,
                  associatedLabelId: newValue?.value || "",
                  associatedLabel: newValue?.label || "",
                });
              }}
              renderInput={(params) => (
                <TextField {...params} label="Document Type Field" placeholder="Select document type..." />
              )}
            />
          </FormControl>

          <Stack direction="row" spacing={1} justifyContent="flex-end">
            <Button size="small" startIcon={<CloseIcon />} onClick={closeFieldItem}>
              Cancel
            </Button>
            <Button
              size="small"
              variant="contained"
              startIcon={<CheckIcon />}
              onClick={() => {
                callbackSaveData && callbackSaveData(tempFields, position);
                closeFieldItem && closeFieldItem();
              }}
              // disabled={!tempFields.fieldName || !tempFields.userField}
            >
              Save
            </Button>
          </Stack>
        </Stack>
      </Box>
    </ListItem>
  );
};

const RowItemComponent = ({ item, options, position }: { item: RowItem; options: any; position: number }) => {
  const [isEdit, setIsEdit] = React.useState(false);
  const Edit = item?.actions?.find((it) => it.label === "Edit");
  return (
    <>
      {isEdit ? (
        <FieldItem
          field={{
            associatedLabel: item.associatedLabel || "",
            associatedLabelId: item.associatedLabelId || "",
            ItemId: item.itemLabelId,
            itemLabel: item.itemLabel,
          }}
          callbackSaveData={Edit?.action}
          closeFieldItem={() => setIsEdit(false)}
          doctypeOptions={options}
          position={position}
        />
      ) : (
        <ListItem key={item.itemLabelId} divider>
          <ListItemText primary={item.itemLabel} secondary={item.associatedLabel && `â†’ ${item.associatedLabel}`} />
          <ListItemSecondaryAction>
            {item.actions &&
              item.actions?.map((action) => (
                <FactoryButton
                  actions={{ ...action, action: action.label === "Edit" ? () => setIsEdit(true) : action.action }}
                  infoRow={{
                    itemLabel: item?.itemLabel || "",
                    ItemId: item?.itemLabelId,
                    associatedLabel: item?.associatedLabel || "",
                    associatedLabelId: item?.associatedLabelId || "",
                  }}
                  position={position}
                />
                // <IconButton
                //   key={action.label}
                //   onClick={() => action?.action && action?.action(item)}
                //   title={action.label}
                // >
                //   {action.icon}
                // </IconButton>
              ))}
            {item.customActions &&
              item.customActions?.map((custom, index) => (
                <Button
                  key={index}
                  size="small"
                  onClick={() => custom.action(item.itemLabelId)}
                  sx={{ ml: 1, gap: "5px" }}
                >
                  {custom.icon}
                  {custom.label}
                </Button>
              ))}
          </ListItemSecondaryAction>
        </ListItem>
      )}
    </>
  );
};

export default function DataCard({
  onCreateClick,
  config,
  row,
  options,
  isSearcheable,
  isCreateButtonVisible,
}: DataCardProps) {
  return (
    <Card
      sx={{
        height: "100%",
        width: 320,
        display: "flex",
        flexDirection: "column",
      }}
    >
      <CardContent
        sx={{
          borderBottom: "1px solid rgba(139, 139, 139, 0.55)",
          pb: 2,
        }}
      >
        <Typography variant="h6" mb={2}>
          {config.title}
          {row.length > 0 && (
            <Typography component="span" variant="body2" color="text.secondary" sx={{ ml: 1 }}>
              ({row.length})
            </Typography>
          )}
        </Typography>
        {isSearcheable && (
          <Box sx={{ position: "relative", mb: 2 }}>
            <TextField
              variant="outlined"
              placeholder={`Search ${config.title}`}
              fullWidth
              size="small"
              InputProps={{
                startAdornment: <SearchIcon sx={{ color: "action.active" }} />,
              }}
              sx={{
                input: {
                  pl: 1,
                  py: 1.2,
                },
                "& .MuiOutlinedInput-root": {
                  borderRadius: "10px",
                  fontSize: "0.9rem",
                },
              }}
            />
          </Box>
        )}
        {isCreateButtonVisible && (
          <Button
            variant="contained"
            fullWidth
            startIcon={<AddIcon />}
            onClick={onCreateClick}
            size="small"
            sx={{
              boxShadow: "none",
              textTransform: "none",
              fontSize: "1rem",
              fontWeight: 500,
              borderRadius: "10px",
            }}
          >
            {config.title}
          </Button>
        )}
      </CardContent>

      <Box sx={{ flexGrow: 1, overflowY: "auto" }}>
        {row.length === 0 ? (
          <Box sx={{ textAlign: "center", p: 4, color: "#757575" }}>
            <InsertDriveFileOutlinedIcon sx={{ fontSize: 64, color: "text.disabled", mb: 2 }} />
            <Typography variant="body1" sx={{ mb: 1 }}>
              No {config.title} found
            </Typography>
            <Typography variant="body2">Create your first {config.title} by clicking on the button above</Typography>
          </Box>
        ) : (
          <List disablePadding>
            {row.map((rowItem, idx) => (
              <RowItemComponent key={idx} item={rowItem} options={options} position={idx} />
            ))}
          </List>
        )}
      </Box>
    </Card>
  );
}

function FactoryButton({
  actions,
  infoRow,
  position,
}: {
  actions: defaultActions;
  infoRow: RowInfo;
  position: number;
}) {
  switch (actions.label) {
    case "Edit":
      return (
        <IconButton
          onClick={() => {
            actions.action && actions.action();
          }}
        >
          <EditIcon fontSize="small" />
        </IconButton>
      );
    case "Delete":
      return (
        <IconButton
          onClick={() => {
            actions.action && actions.action(infoRow, position);
          }}
        >
          <DeleteIcon fontSize="small" color="error" />
        </IconButton>
      );
    default:
      return <></>;
  }
}

