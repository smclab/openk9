import {
  Box,
  CircularProgress,
  MenuItem,
  SelectChangeEvent,
  Select as SelectMaterial,
  SxProps,
  Theme,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { InformationField } from "../utils/informationField";

type OptionRelationOneToOne = {
  value: string;
  label: string;
};

type SelectedValue = {
  id: string;
  name: string;
};

type CustomSelectProps = {
  options: OptionRelationOneToOne[];
  onChange: (value: SelectedValue) => void;
  label: string;
  value?: SelectedValue;
  description?: string;
  disabled?: boolean;
  loadMoreOptions?: { response: () => Promise<OptionRelationOneToOne[]>; hasNextPage: boolean };
  sx?: SxProps<Theme>;
};

export function CustomSelectRelationsOneToOne({
  options,
  onChange,
  label,
  value,
  disabled,
  description,
  loadMoreOptions,
  sx,
}: CustomSelectProps) {
  const [currentOptions, setCurrentOptions] = useState<OptionRelationOneToOne[]>(options);
  const [loading, setLoading] = useState(false);

  const handleChange = (event: SelectChangeEvent<string>) => {
    const selectedOption = currentOptions.find((option) => option.value === event.target.value);
    if (selectedOption) {
      onChange({ id: selectedOption.value, name: selectedOption.label });
    } else {
      onChange({ id: "-1", name: "Nessuna selezione" });
    }
  };

  const handleScroll = async (event: React.UIEvent<HTMLUListElement, UIEvent>) => {
    const list = event.currentTarget;
    if (loadMoreOptions?.hasNextPage)
      if (list.scrollTop + list.clientHeight >= list.scrollHeight && !loading) {
        setLoading(true);
        const newOptions = loadMoreOptions && (await loadMoreOptions.response());
        newOptions && setCurrentOptions((prev) => [...prev, ...newOptions]);
        setLoading(false);
      }
  };

  useEffect(() => {
    setCurrentOptions(options);
  }, [options]);

  const selectedOptionNotInOptions = value && !currentOptions.some((option) => option.value === value.id);

  return (
    <Box>
      <Box marginBottom={1} display={"flex"} flexDirection="row" alignItems="center" gap="4px" sx={sx}>
        <Typography variant="subtitle1" component="label" htmlFor={label + "label"}>
          {label}
        </Typography>
        {description && <InformationField description={description} />}
      </Box>
      <SelectMaterial
        fullWidth
        value={value?.id || "-1"}
        onChange={handleChange}
        disabled={disabled}
        MenuProps={{
          PaperProps: {
            onScroll: handleScroll,
          },
        }}
      >
        <MenuItem key="-1" value="-1">
          Nessuna selezione
        </MenuItem>
        {selectedOptionNotInOptions && Number(value.id) > 0 && (
          <MenuItem key={value.id} value={value.id}>
            {value.name}
          </MenuItem>
        )}
        {currentOptions.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
        {loading && loadMoreOptions?.hasNextPage && (
          <MenuItem disabled>
            <CircularProgress size={24} />
          </MenuItem>
        )}
      </SelectMaterial>
    </Box>
  );
}
