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
import { FormControl, Box, Typography, TextField, FormHelperText } from "@mui/material";
import React from "react";
import { BaseInputProps } from "../utils";
import { InformationField } from "../utils/informationField";

export function NumberInput({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  item,
  description,
  isNumber = true,
}: BaseInputProps<number> & { item?: boolean; isNumber?: boolean }) {
  const ref = React.useRef<HTMLInputElement | null>(null);

  React.useLayoutEffect(() => {
    if (ref.current) {
      ref.current.valueAsNumber = value;
    }
  }, [value]);

  return (
    <FormControl fullWidth variant="outlined" error={validationMessages.length > 0} sx={{ marginBottom: 2 }}>
      <Box sx={{ marginBottom: 1 }} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
        <Typography variant="subtitle1" component="label" htmlFor={id}>
          {label}
        </Typography>
        {description && <InformationField description={description} />}
      </Box>
      <TextField
        inputRef={ref}
        type={"number"}
        id={id}
        variant="outlined"
        value={value}
        onChange={(event) => {
          if (!isNaN(Number(event.currentTarget.value))) onChange(Number(event.currentTarget.value));
        }}
        disabled={disabled}
        aria-describedby={`${id}-helper-text`}
        fullWidth
        error={validationMessages.length > 0}
        inputProps={{
          step: "any",
          onWheel: (e) => {
            !isNumber && e.preventDefault();
            !isNumber && e.currentTarget.blur();
          },
        }}
      />

      {validationMessages.length > 0 && (
        <FormHelperText error>
          {validationMessages.map((validationMessage, index) => (
            <div key={index}>{validationMessage}</div>
          ))}
        </FormHelperText>
      )}
    </FormControl>
  );
}

