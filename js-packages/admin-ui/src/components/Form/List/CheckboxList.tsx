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
import {
  Box,
  CircularProgress,
  Checkbox,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  SxProps,
  Theme,
} from "@mui/material";
import { useEffect, useState } from "react";
import { InformationField } from "../utils/informationField";

// --- Definizione dei tipi ---
type OptionRelationOneToOne = {
  value: string;
  label: string;
};

// âœ… Corretta definizione del tipo SelectedValue
type SelectedValue = {
  id: string;
  name: string;
};

type LoadMoreOptions = {
  response: () => Promise<OptionRelationOneToOne[]>;
  hasNextPage: boolean;
};

type CustomCheckboxListProps = {
  options: OptionRelationOneToOne[];
  onChange: (values: SelectedValue[]) => void;
  label?: string;
  description?: string;
  disabled?: boolean;
  loadMoreOptions?: LoadMoreOptions;
  sx?: SxProps<Theme>;
  selectedValues?: SelectedValue[]; // Nuova prop per ricevere valori esterni
};

// --- Componente principale ---
export default function CustomCheckboxList({
  options,
  onChange,
  label,
  description,
  disabled,
  loadMoreOptions,
  sx,
  selectedValues = [],
}: CustomCheckboxListProps) {
  const [currentOptions, setCurrentOptions] = useState<OptionRelationOneToOne[]>(options);
  const [loading, setLoading] = useState(false);
  const [checked, setChecked] = useState<SelectedValue[]>(selectedValues);

  // Aggiorna checked se selectedValues cambia
  useEffect(() => {
    setChecked(selectedValues);
  }, [selectedValues]);

  // Aggiorna currentOptions quando options cambia
  useEffect(() => {
    setCurrentOptions(options);
  }, [options]);

  const handleToggle = (option: OptionRelationOneToOne) => () => {
    const isChecked = checked.some((item) => item.id === option.value);
    let newChecked: SelectedValue[];

    if (isChecked) {
      newChecked = checked.filter((item) => item.id !== option.value);
    } else {
      newChecked = [...checked, { id: option.value, name: option.label }];
    }

    setChecked(newChecked);
    onChange(newChecked);
  };

  const handleScroll = async (event: React.UIEvent<HTMLDivElement>) => {
    const list = event.currentTarget;
    if (loadMoreOptions?.hasNextPage && !loading) {
      if (list.scrollTop + list.clientHeight >= list.scrollHeight) {
        setLoading(true);
        const newOptions = await loadMoreOptions.response();
        setCurrentOptions((prev) => [...prev, ...newOptions]);
        setLoading(false);
      }
    }
  };

  return (
    <Box>
      {label && (
        <Box marginBottom={1} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
          <Typography variant="subtitle1">{label}</Typography>
          {description && <InformationField description={description} />}
        </Box>
      )}
      <List
        component="div"
        sx={{
          width: "100%",
          flex: 1,
          bgcolor: "background.paper",
          overflowY: "auto",
          maxHeight: "19rem",
          ...sx,
        }}
        onScroll={handleScroll}
      >
        {currentOptions.map((option) => {
          const isChecked = checked.some((item) => item.id === option.value);
          const labelId = `checkbox-list-label-${option.value}`;

          return (
            <ListItem key={option.value} disablePadding>
              <ListItemButton role={undefined} onClick={handleToggle(option)} dense disabled={disabled}>
                <ListItemIcon>
                  <Checkbox
                    edge="start"
                    checked={isChecked}
                    tabIndex={-1}
                    disableRipple
                    inputProps={{ "aria-labelledby": labelId }}
                  />
                </ListItemIcon>
                <ListItemText id={labelId} primary={option.label} />
              </ListItemButton>
            </ListItem>
          );
        })}
        {loading && loadMoreOptions?.hasNextPage && (
          <ListItem>
            <ListItemText
              primary={
                <Box display="flex" justifyContent="center" padding={1}>
                  <CircularProgress size={24} />
                </Box>
              }
            />
          </ListItem>
        )}
      </List>
    </Box>
  );
}

