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
import { FormControl, Box, Typography, TextField, FormHelperText, InputAdornment, IconButton } from "@mui/material";
import { BaseInputProps } from "../utils";
import { InformationField } from "../utils/informationField";
import ClearIcon from "@mui/icons-material/Clear";

export function TextInput({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  item,
  description,
  haveReset,
}: BaseInputProps<string> & { item?: boolean; haveReset?: { isVisible: boolean; callback(): void } }) {
  return (
    <FormControl fullWidth variant="outlined" error={validationMessages.length > 0} sx={{ marginBottom: 2 }}>
      <Typography variant="subtitle1" component="label" htmlFor={id}>
        {label}
      </Typography>
      <Box sx={{ marginBottom: 1 }} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
        <TextField
          id={id}
          variant="outlined"
          value={value}
          onChange={(event) => onChange(event.currentTarget.value)}
          disabled={disabled}
          aria-describedby={`${id}-helper-text`}
          fullWidth
          error={validationMessages.length > 0}
          InputProps={{
            endAdornment: haveReset?.isVisible && (
              <InputAdornment position="end">
                <IconButton
                  onClick={() => {
                    onChange("");
                    haveReset?.callback();
                  }}
                  edge="end"
                >
                  <ClearIcon />
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
        {description && <InformationField description={description} />}
        {validationMessages.length > 0 && (
          <FormHelperText error>
            {validationMessages.map((validationMessage, index) => (
              <div key={index}>{validationMessage}</div>
            ))}
          </FormHelperText>
        )}
      </Box>
    </FormControl>
  );
}

