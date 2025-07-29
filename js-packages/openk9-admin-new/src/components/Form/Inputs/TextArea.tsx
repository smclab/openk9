import { Box, FormControl, FormHelperText, TextField, Typography } from "@mui/material";
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
  return (
    <FormControl fullWidth variant="outlined" error={validationMessages.length > 0} sx={{ marginBottom: 2 }}>
      <Box sx={{ marginBottom: 1 }}>
        <Typography variant="subtitle1" component="label" htmlFor={id}>
          {label}
        </Typography>
      </Box>
      <TextField
        id={id}
        value={value}
        onChange={(event) => onChange(event.currentTarget.value)}
        disabled={disabled}
        aria-describedby={`${id}-helper-text`}
        minRows={5}
        maxRows={5}
        multiline
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
