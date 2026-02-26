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
import * as React from "react";
import { Autocomplete as AutocompleteMaterial, Chip, TextField, Typography } from "@mui/material";

type Props = {
  defaultChip: string[];
  disabled?: boolean;
  setChips: (value: string[]) => void;
};

export default function Autocomplete({ defaultChip, setChips, disabled }: Props) {
  const [chips, setLocalChips] = React.useState<string[]>(defaultChip ?? []);
  const [inputValue, setInputValue] = React.useState("");

  React.useEffect(() => {
    setLocalChips(defaultChip ?? []);
  }, [defaultChip]);

  const commitInputAsChip = React.useCallback(() => {
    const raw = inputValue.trim();
    if (!raw) return;

    if (chips.includes(raw)) {
      setInputValue("");
      return;
    }

    const next = [...chips, raw];
    setLocalChips(next);
    setChips(next);
    setInputValue("");
  }, [chips, inputValue, setChips]);

  return (
    <AutocompleteMaterial<string, true, false, true>
      multiple
      freeSolo
      disabled={disabled}
      options={[]}
      value={chips}
      inputValue={inputValue}
      onInputChange={(_, newInputValue) => setInputValue(newInputValue)}
      onChange={(_, value) => {
        const next = (value as string[]).map((v) => v.trim()).filter(Boolean);
        const dedup = Array.from(new Set(next));

        setLocalChips(dedup);
        setChips(dedup);
      }}
      onBlur={() => {
        commitInputAsChip();
      }}
      renderTags={(value: readonly string[], getTagProps) =>
        value.map((option: string, index: number) => {
          const { key, ...tagProps } = getTagProps({ index });
          return <Chip variant="outlined" label={option} key={key} {...tagProps} />;
        })
      }
      renderInput={(params) => (
        <TextField
          {...params}
          variant="filled"
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              e.preventDefault();
              commitInputAsChip();
            }
          }}
          helperText={
            !disabled && inputValue.trim().length > 0 ? (
              <Typography variant="caption">
                Press <b>Enter</b> or click the suggestion to add it
              </Typography>
            ) : (
              " "
            )
          }
        />
      )}
    />
  );
}
