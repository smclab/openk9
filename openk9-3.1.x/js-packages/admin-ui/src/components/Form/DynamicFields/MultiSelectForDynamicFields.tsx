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
import { Box, FormControl, InputLabel, MenuItem, SelectChangeEvent, Select as SelectMaterial } from "@mui/material";
import React from "react";

export function MultiSelectForDinamicFields({
  id,
  setTitle,
  templates,
  onChangeDescription,
  templateChoice,
  setTemplateChoice,
  disabled,
  valueSelect,
  setValueSelect,
}: {
  setTitle?: (value: string) => void;
  id: string;
  templates: any;
  onChangeDescription: any;
  templateChoice: any;
  setTemplateChoice: any;
  disabled?: boolean;
  valueSelect: string | null;
  setValueSelect: React.Dispatch<React.SetStateAction<string | null>>;
}) {
  return (
    <React.Fragment>
      <Box paddingBottom={"20px"} role="tablist">
        {/* <div className="panel-header">
          <span className="panel-title">Type</span>
        </div> */}
        {/* <CustomFormGroup> */}
        <FormControl fullWidth>
          <InputLabel id="type-select-helper-label">Type</InputLabel>
          <SelectMaterial
            label={"Type"}
            labelId="type-select-helper-label"
            value={valueSelect || ""}
            disabled={disabled}
            onChange={(event: SelectChangeEvent) => {
              const selectedValue = event.target.value;
              setValueSelect(selectedValue);
              templates.map((element: any) => {
                element.visible = "false";
                if (element.title === selectedValue) {
                  element.visible = "true";
                  setTemplateChoice(JSON.parse(element.Json));
                  return true;
                }
              });
              const dataSelect = templates.find(
                (element: { title: string; value: string }) => element.title === selectedValue,
              );
              if (setTitle) {
                setTitle(dataSelect.title);
              }
              onChangeDescription(dataSelect!.description);
            }}
          >
            {templates.map((filter: { title: string }, index: number) => (
              <MenuItem key={index} value={filter.title}>
                {filter.title}
              </MenuItem>
            ))}
          </SelectMaterial>
        </FormControl>
        {/* </CustomFormGroup> */}
      </Box>
    </React.Fragment>
  );
}

