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
