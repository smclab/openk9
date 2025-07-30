import { Autocomplete as AutocompleteMaterial, Chip, TextField } from "@mui/material";

export default function Autocomplete({
  defaultChip,
  setChips,
  disabled,
}: {
  defaultChip: Array<string>;
  disabled?: boolean;
  setChips: (value: Array<string>) => void;
}) {
  return (
    <AutocompleteMaterial<string, true, false, true>
      multiple
      options={[]}
      disabled={disabled}
      defaultValue={defaultChip}
      freeSolo
      onChange={(event, value) => setChips(value as string[])}
      renderTags={(value: readonly string[], getTagProps) =>
        value.map((option: string, index: number) => {
          const { key, ...tagProps } = getTagProps({ index });
          return <Chip variant="outlined" label={option} key={key} {...tagProps} />;
        })
      }
      renderInput={(params) => <TextField {...params} variant="filled" />}
    />
  );
}
