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

import { Autocomplete as AutocompleteMaterial, Chip, TextField, Typography, Box } from "@mui/material";
import { useCallback, useEffect, useState } from "react";

interface AutocompleteProps {
  defaultChip: string[];
  disabled?: boolean;
  setChips: (value: string[]) => void;
}

export default function Autocomplete({ defaultChip, setChips, disabled }: AutocompleteProps) {
  const [inputValue, setInputValue] = useState("");

  const options = inputValue ? [`${inputValue}`] : [];

  const [internalValue, setInternalValue] = useState(defaultChip || []);

  useEffect(() => {
    setInternalValue(defaultChip);
  }, [defaultChip]);

  const handleBlur = useCallback(() => {
    const trimmedInput = inputValue?.trim();
    if (trimmedInput && trimmedInput.length > 0) {
      const newValue = [...internalValue];
      if (!newValue.includes(trimmedInput)) {
        newValue.push(trimmedInput);
        setChips(newValue);
        setInternalValue(newValue);
      }
    }
  }, [inputValue, internalValue, setChips]);

  return (
    <AutocompleteMaterial<string, true, false, true>
      multiple
      freeSolo
      clearOnBlur={false}
      disabled={disabled}
      options={options}
      value={internalValue}
      inputValue={inputValue}
      onInputChange={(_, newInputValue) => setInputValue(newInputValue)}
      onChange={(_, newValue) => {
        setInternalValue(newValue as string[]);
        setChips(newValue as string[]);
      }}
      onBlur={handleBlur}
      renderTags={(value, getTagProps) =>
        value.map((option, index) => {
          const { key, ...tagProps } = getTagProps({ index });
          return <Chip variant="outlined" label={option} key={key} {...tagProps} />;
        })
      }
      renderOption={(props, option) => {
        const textLength = option.length;
        const isLong = textLength > 15;

        return (
          <Box
            component="li"
            {...props}
            sx={{
              display: "flex",
              flexDirection: isLong ? "column" : "row",
              alignItems: isLong ? "flex-start" : "space-between",
              gap: isLong ? 0.5 : 1,
              py: 1,
            }}
          >
            <Typography variant="body2">{option}</Typography>
            <Typography
              variant="caption"
              color="error"
              sx={{
                fontSize: "0.7rem",
                lineHeight: 1.2,
                mt: isLong ? 0.2 : 0,
              }}
            >
              click here or press enter to confirm
            </Typography>
          </Box>
        );
      }}
      renderInput={(params) => (
        <TextField
          {...params}
          variant="filled"
          onBlur={(event) => {
            const currentText = inputValue?.trim();
            if (currentText && currentText.length > 0) {
              const currentChips = defaultChip || [];
              if (!currentChips.includes(currentText)) {
                setChips([...currentChips, currentText]);
              }
            }
            setInputValue("");
          }}
        />
      )}
    />
  );
}
