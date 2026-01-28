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

