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
