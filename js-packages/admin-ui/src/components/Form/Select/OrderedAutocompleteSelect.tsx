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
import { Box, IconButton, List, ListItem, ListItemText, Typography } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import React from "react";
import { UseOptionsHook } from "utils/RelationOneToOne";
import { AutocompleteDropdown, SelectedValue } from "./AutocompleteDropdown";
import { addToOrderedSelection, removeFromOrderedSelection } from "./orderedSelection";

type Props = {
  label: string;
  description?: string;
  addLabel: string;
  removeLabel: string;
  emptyLabel: string;
  value: SelectedValue[];
  onChange: (value: SelectedValue[]) => void;
  useOptions: UseOptionsHook;
  disabled?: boolean;
};

/**
 * Chooses an ordered list of values: each choice is appended at the end, and
 * the list shows the values numbered in the order they were chosen.
 */
export function OrderedAutocompleteSelect({
  label,
  description,
  addLabel,
  removeLabel,
  emptyLabel,
  value,
  onChange,
  useOptions,
  disabled,
}: Props) {
  return (
    <Box>
      <Typography variant="subtitle1">{label}</Typography>
      {description && (
        <Typography variant="body2" color="text.secondary">
          {description}
        </Typography>
      )}
      {value.length === 0 ? (
        <Typography variant="body2" sx={{ marginTop: 1 }}>
          {emptyLabel}
        </Typography>
      ) : (
        <List dense component="ol" aria-label={label}>
          {value.map((item, index) => (
            <ListItem
              key={item.id}
              secondaryAction={
                !disabled && (
                  <IconButton
                    edge="end"
                    aria-label={`${removeLabel} ${item.name}`}
                    onClick={() => onChange(removeFromOrderedSelection(value, item.id))}
                  >
                    <CloseIcon fontSize="small" />
                  </IconButton>
                )
              }
            >
              <ListItemText primary={`${index + 1}. ${item.name}`} />
            </ListItem>
          ))}
        </List>
      )}
      {!disabled && (
        <AutocompleteDropdown
          label={addLabel}
          value={{ id: "", name: "" }}
          allowClear={false}
          onChange={(selected) => onChange(addToOrderedSelection(value, selected))}
          useOptions={useOptions}
        />
      )}
    </Box>
  );
}
