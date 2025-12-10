import React from "react";
import { SxProps } from "@mui/material";
import { useDocTypeFieldsOptions } from "./useDocTypeFieldsOptions";
import { AutocompleteDropdown } from "./AutocompleateOptionList";

type Value = { id: string; name: string };

type Props = {
  label?: string;
  disabled?: boolean;
  value?: Value;
  onChange: (value: Value) => void;
  onClear?: () => void;
  sx?: SxProps;
  suggestionCategoryId?: number | null;
};

export const DocTypeFieldAutocompleteDropdown: React.FC<Props> = ({
  label = "Search Config",
  disabled,
  value,
  onChange,
  onClear,
  sx,
  suggestionCategoryId,
}) => {
  return (
    <AutocompleteDropdown
      label={label}
      disabled={disabled}
      value={value}
      onChange={onChange}
      onClear={onClear}
      useOptions={useDocTypeFieldsOptions}
      extraVariables={{ suggestionCategoryId: suggestionCategoryId ?? 0 }}
      sx={sx}
    />
  );
};
