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
