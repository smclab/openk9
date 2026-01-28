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
import { Box, TextField, Typography, IconButton, InputAdornment, SxProps } from "@mui/material";
import { Visibility, VisibilityOff } from "@mui/icons-material";
import { InformationField } from "../utils/informationField";
import { useState } from "react";
import { Theme } from "@emotion/react";

export function TextInputSimple({
  label,
  description,
  value,
  onChange,
  disabled,
  type = "text",
  sx = {},
  isRequired = false,
}: {
  label?: string;
  description?: string;
  value: string | number;
  onChange(event: any): void;
  disabled?: boolean;
  isRequired?: boolean;
  sx?: SxProps<Theme> | undefined;
  type?: "email" | "text" | "number" | "password" | "search" | "tel" | "url" | "date" | "time";
}) {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <Box key={label + "div"} display={"flex"} flexDirection="column" sx={{ paddingBottom: "20px", ...sx }}>
      <Box marginBottom={1} display={"flex"} flexDirection="row" alignItems="center" justifyContent={"space-between"}>
        <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
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
        variant="outlined"
        type={type === "password" && showPassword ? "text" : type}
        value={value}
        onChange={onChange}
        disabled={disabled}
        fullWidth
        InputProps={{
          endAdornment: type === "password" && (
            <InputAdornment position="end">
              <IconButton onClick={() => setShowPassword((prev) => !prev)} edge="end">
                {showPassword ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          ),
        }}
      />
    </Box>
  );
}

