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
import { Box, TextField, ClickAwayListener } from "@mui/material";
import { AutocompleteOptionsList } from "@components/Form/Select/AutocompleteOptionsList";

type TokenTypeAutocompleteProps<TokenType> = {
  label?: string;
  value: TokenType;
  dict: Record<string, string>;
  onChange: (value: TokenType) => void;
};

export function TokenTypeAutocomplete<TokenType extends string>({
  label = "Token Type",
  value,
  dict,
  onChange,
}: TokenTypeAutocompleteProps<TokenType>) {
  const [open, setOpen] = React.useState(false);
  const [highlightedIndex, setHighlightedIndex] = React.useState(0);

  const options = React.useMemo(
    () =>
      Object.entries(dict).map(([val, lab]) => ({
        value: val,
        label: lab,
      })),
    [dict],
  );

  const selectedOption = options.find((o) => o.value === value) ?? null;

  const handleSelect = (option: { value: string; label: string }) => {
    onChange(option.value as TokenType);
  };

  return (
    <ClickAwayListener onClickAway={() => setOpen(false)}>
      <Box sx={{ position: "relative" }}>
        <TextField
          label={label}
          fullWidth
          value={selectedOption?.label ?? ""}
          onClick={() => setOpen((prev) => !prev)}
          InputProps={{
            readOnly: true,
          }}
        />

        {open && (
          <AutocompleteOptionsList
            options={options}
            highlightedIndex={highlightedIndex}
            loading={false}
            onSelect={(option) => {
              handleSelect(option);
              setOpen(false);
            }}
          />
        )}
      </Box>
    </ClickAwayListener>
  );
}

