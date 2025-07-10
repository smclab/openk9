import { Box, TextField, Typography, IconButton, InputAdornment } from "@mui/material";
import { Visibility, VisibilityOff } from "@mui/icons-material";
import { InformationField } from "../utils/informationField";
import { useState } from "react";

export function TextInputSimple({
  label,
  description,
  value,
  onChange,
  disabled,
  type = "text",
}: {
  label?: string;
  description?: string;
  value: string | number;
  onChange(event: any): void;
  disabled?: boolean;
  type?: "email" | "text" | "number" | "password" | "search" | "tel" | "url" | "date" | "time";
}) {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <Box key={label + "div"} display={"flex"} flexDirection="column" paddingBottom={"20px"}>
      <Box marginBottom={1} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
        <Typography variant="subtitle1" component="label">
          {label}
        </Typography>
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
