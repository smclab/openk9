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
import { Box, SxProps, TextField, Theme, Typography } from "@mui/material";
import { InformationField } from "../utils/informationField";

export function NumberInputSimple({
  label,
  description,
  value,
  onChange,
  disabled,
  isRequired = false,
  setStyles,
}: {
  label: string;
  description?: string;
  isRequired?: boolean;
  value: number;
  disabled?: boolean;
  onChange(event: any): void;
  setStyles?: SxProps<Theme> | undefined;
}) {
  return (
    <Box key={label + "div"} display={"flex"} flexDirection="column" paddingBottom={"20px"} sx={{ ...setStyles }}>
      <Box
        marginBottom={1}
        display={"flex"}
        flexDirection="row"
        alignItems="center"
        gap="4px"
        justifyContent={"space-between"}
      >
        <div style={{ display: "flex", gap: "10px" }}>
          <Typography variant="subtitle1" component="label">
            {label}
          </Typography>
          {isRequired && (
            <Typography color="error" ml={"3px"}>
              *
            </Typography>
          )}
        </div>
        {description && <InformationField description={description} />}
      </Box>
      <TextField
        id={label + "input"}
        disabled={disabled}
        variant="outlined"
        type="number"
        value={value}
        onChange={onChange}
        fullWidth
      />
    </Box>
  );
}

