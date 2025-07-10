import { FormControl, Typography, MenuItem, FormHelperText, Select as SelectMaterial, Box } from "@mui/material";
import { BaseInputProps, TemplateArray } from "../utils";
import { useState } from "react";
import { InformationField } from "../utils/informationField";

export function CustomSelect<E extends Record<string, any>>({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  dict,
  isNotEnum,
  description,
}: BaseInputProps<E[string]> & { dict: E }) {
  const [savedDescription, setSavedDescription] = useState<string>("");
  return (
    <FormControl fullWidth error={validationMessages.length > 0} disabled={disabled}>
      <Box sx={{ marginBottom: 1 }} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
        <Typography
          color={disabled ? "text.disabled" : "text.primary"}
          variant="subtitle1"
          component="label"
          htmlFor={id + label}
        >
          {label}
        </Typography>
        {description && <InformationField description={description} />}
      </Box>
      <SelectMaterial
        id={id + label}
        value={value}
        onChange={(event) => onChange(event.target.value as E[string])}
        displayEmpty
      >
        {isNotEnum
          ? dict.map((filter: TemplateArray) => (
              <MenuItem
                key={filter.title}
                value={filter.title}
                onClick={() => setSavedDescription(filter?.description || "")}
              >
                {filter.title}
              </MenuItem>
            ))
          : Object.entries(dict).map(([label, value]) => (
              <MenuItem key={value} value={value}>
                {label}
              </MenuItem>
            ))}
      </SelectMaterial>
      {savedDescription && <FormHelperText>{savedDescription}</FormHelperText>}
      {validationMessages.length > 0 && (
        <FormHelperText>
          {validationMessages.map((validationMessage, index) => (
            <div key={index}>{validationMessage}</div>
          ))}
        </FormHelperText>
      )}
    </FormControl>
  );
}
