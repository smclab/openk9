import React from "react";
import { Box, TextField, ClickAwayListener } from "@mui/material";
import { AutocompleteOptionsList } from "@components/Form/Select/AutocompleteOptionsList";

type TokenTypeAutocompleteProps<TokenType> = {
  label?: string;
  value: TokenType;
  dict: Record<string, string>;
  onChange: (value: TokenType) => void;
};

export function TokenTypeAutocomplete<TokenType extends string>({
  label = "Token Type",
  value,
  dict,
  onChange,
}: TokenTypeAutocompleteProps<TokenType>) {
  const [open, setOpen] = React.useState(false);
  const [highlightedIndex, setHighlightedIndex] = React.useState(0);

  const options = React.useMemo(
    () =>
      Object.entries(dict).map(([val, lab]) => ({
        value: val,
        label: lab,
      })),
    [dict],
  );

  const selectedOption = options.find((o) => o.value === value) ?? null;

  const handleSelect = (option: { value: string; label: string }) => {
    onChange(option.value as TokenType);
  };

  return (
    <ClickAwayListener onClickAway={() => setOpen(false)}>
      <Box sx={{ position: "relative" }}>
        <TextField
          label={label}
          fullWidth
          value={selectedOption?.label ?? ""}
          onClick={() => setOpen((prev) => !prev)}
          InputProps={{
            readOnly: true,
          }}
        />

        {open && (
          <AutocompleteOptionsList
            options={options}
            highlightedIndex={highlightedIndex}
            loading={false}
            onSelect={(option) => {
              handleSelect(option);
              setOpen(false);
            }}
          />
        )}
      </Box>
    </ClickAwayListener>
  );
}
