/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
      onChange({ id: "-1", name: "No selection" });
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
          No selection
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

