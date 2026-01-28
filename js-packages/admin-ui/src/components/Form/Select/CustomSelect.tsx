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
import { FormControl, Typography, MenuItem, FormHelperText, Select as SelectMaterial, Box } from "@mui/material";
import { BaseInputProps, TemplateArray } from "../utils";
import { useState } from "react";
import { InformationField } from "../utils/informationField";

export function CustomSelect<E extends Record<string, any>>({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  dict,
  isNotEnum,
  description,
}: BaseInputProps<E[string]> & { dict: E }) {
  const [savedDescription, setSavedDescription] = useState<string>("");
  return (
    <FormControl fullWidth error={validationMessages.length > 0} disabled={disabled}>
      <Box sx={{ marginBottom: 1 }} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
        <Typography
          color={disabled ? "text.disabled" : "text.primary"}
          variant="subtitle1"
          component="label"
          htmlFor={id + label}
        >
          {label}
        </Typography>
        {description && <InformationField description={description} />}
      </Box>
      <SelectMaterial
        id={id + label}
        value={value}
        onChange={(event) => onChange(event.target.value as E[string])}
        displayEmpty
      >
        {isNotEnum
          ? dict.map((filter: TemplateArray) => (
              <MenuItem
                key={filter.title}
                value={filter.title}
                onClick={() => setSavedDescription(filter?.description || "")}
              >
                {filter.title}
              </MenuItem>
            ))
          : Object.entries(dict).map(([label, value]) => (
              <MenuItem key={value} value={value}>
                {label}
              </MenuItem>
            ))}
      </SelectMaterial>
      {savedDescription && <FormHelperText>{savedDescription}</FormHelperText>}
      {validationMessages.length > 0 && (
        <FormHelperText>
          {validationMessages.map((validationMessage, index) => (
            <div key={index}>{validationMessage}</div>
          ))}
        </FormHelperText>
      )}
    </FormControl>
  );
}

