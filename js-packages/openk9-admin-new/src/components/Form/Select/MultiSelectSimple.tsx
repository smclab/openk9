import { Autocomplete, TextField } from "@mui/material";
import React from "react";

export function MultiSelectSimple({
  keyofF, description, items, disabled, onItemchange,
}: {
  keyofF: string;
  description: string;
  disabled?: boolean;
  items: any[];
  onItemchange(event: any): void;
}) {
  const [value, setValue] = React.useState<string | null>("");

  return (
    <div key={keyofF} style={{ paddingBottom: "20px" }}>
      <Autocomplete
        multiple
        id={keyofF + "-select"}
        options={items}
        getOptionLabel={(option) => option.label || ""}
        value={items.filter((item) => value?.includes(item.value))}
        onChange={(event, newValue) => {
          setValue(newValue.map((item) => item.value).join(","));
          onItemchange(newValue);
        }}
        disabled={disabled}
        renderInput={(params) => <TextField {...params} label={keyofF} />} />
    </div>
  );
}
