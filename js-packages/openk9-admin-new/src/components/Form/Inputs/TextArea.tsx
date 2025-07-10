import { FormControl, Box, Typography, TextareaAutosize, FormHelperText, useTheme } from "@mui/material";
import { BaseInputProps } from "../utils";

export function TextArea({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  description,
}: BaseInputProps<string>) {
  const theme = useTheme();
  const isDark = theme.palette.mode === "dark";
  const borderColor = isDark ? "rgba(255, 255, 255, 0.12)" : "rgba(0, 0, 0, 0.12)";
  const textColor = theme.palette.mode === "dark" ? "#FFFFFF" : "#000000";
  return (
    <FormControl fullWidth variant="outlined" error={validationMessages.length > 0} sx={{ marginBottom: 2 }}>
      <Box sx={{ marginBottom: 1 }}>
        <Typography variant="subtitle1" component="label" htmlFor={id}>
          {label}
        </Typography>
      </Box>
      <TextareaAutosize
        id={id}
        value={value}
        onChange={(event) => onChange(event.currentTarget.value)}
        disabled={disabled}
        aria-describedby={`${id}-helper-text`}
        minRows={5}
        style={{
          width: "100%",
          color: textColor,
          backgroundColor: theme.palette.background.paper,
          border: `2px solid ${borderColor}`,
          borderRadius: "8px",
          padding: "8px",
          outline: "none",
          transition: "border-color 0.3s",
        }}
        onFocus={(e) => (e.currentTarget.style.borderColor = theme.palette.primary.main)}
        onBlur={(e) => (e.currentTarget.style.borderColor = "#ccc")}
      />
      {description && <FormHelperText id={`${id}-helper-text`}>{description}</FormHelperText>}
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
