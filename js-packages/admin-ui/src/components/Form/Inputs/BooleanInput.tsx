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
import { FormControl, FormControlLabel, Checkbox, FormHelperText, Box, SxProps, Theme } from "@mui/material";
import { BaseInputProps } from "../utils";
import { InformationField } from "../utils/informationField";

export function BooleanInput({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  description,
  sxCheckbox,
  sxControl,
  isRequired = false,
}: BaseInputProps<boolean> & {
  sxCheckbox?: SxProps<Theme>;
  sxControl?: SxProps<Theme>;
  isRequired?: boolean;
}) {
  return (
    <div>
      <FormControl component="fieldset" error={validationMessages.length > 0} disabled={disabled}>
        <Box display={"flex"} flexDirection="row" alignItems="center" gap="4px">
          <FormControlLabel
            sx={sxControl}
            control={
              <Checkbox id={id} checked={value} onChange={() => onChange(!value)} disabled={disabled} sx={sxCheckbox} />
            }
            label={label}
          />
          {isRequired && <span style={{ color: "red", marginLeft: "3px" }}>*</span>}
          {description && <InformationField description={description} />}
          {validationMessages.length > 0 && (
            <FormHelperText>
              {validationMessages.map((validationMessage, index) => (
                <div key={index}>{validationMessage}</div>
              ))}
            </FormHelperText>
          )}
        </Box>
      </FormControl>
    </div>
  );
}

