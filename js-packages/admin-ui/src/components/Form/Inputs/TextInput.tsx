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
