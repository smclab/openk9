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
import { Box } from "@mui/material";
import { useEffect, useState } from "react";
import DataCard from "./DataCard";
import DataFormCard from "./DataFormCard";
import { DataFormElementConfig, FieldDocType, Field, RowInfo } from "./types";

export type defaultActions = {
  label?: "Edit" | "Delete";
  icon?: React.ReactNode;
  action?: (item?: RowInfo, index?: number) => void;
};

export type customActions = {
  label?: string;
  icon?: React.ReactNode;
  action: (id?: string) => void;
};

export type RowItemBase = {
  itemLabel: string;
  itemLabelId: string;
  actions?: defaultActions[];
  customActions?: customActions[];
};

export type RowItemWithAssociated = RowItemBase & {
  associatedLabel: string;
  associatedLabelId: string;
};

export type RowItemWithoutAssociated = RowItemBase & {
  associatedLabel?: undefined;
  associatedLabelId?: undefined;
};

export type RowItem = RowItemWithAssociated | RowItemWithoutAssociated;

export type row = RowItem[];

export default function DataCardManager({
  config,
  children,
  onReset,
  onAddField,
  row,
  options,
  isCreateButtonVisible,
  onInit,
}: {
  config: DataFormElementConfig;
  children: React.ReactNode;
  onReset?: () => void;
  onAddField?: () => void;
  row: row;
  options: any;
  isCreateButtonVisible?: boolean;
  onInit?: (methods: { openForm: () => void }) => void;
}) {
  const [showForm, setShowForm] = useState(false);

  const handleAddField = () => {
    onAddField && onAddField();
    setShowForm(false);
  };

  useEffect(() => {
    if (onInit) {
      onInit({
        openForm: () => setShowForm(true),
      });
    }
  }, [onInit]);

  return (
    <Box sx={{ display: "flex", minHeight: "600px", gap: 2, marginTop: "16px" }}>
      <Box sx={{ minHeight: "100%" }}>
        <DataCard
          onCreateClick={() => setShowForm(true)}
          config={config}
          row={row}
          options={options}
          isCreateButtonVisible={isCreateButtonVisible}
          // onEdit={(field) => handleEdit(field, 0)} 
          // onRemove={removeField}
        />
      </Box>

      <Box sx={{ flexGrow: 1 }}>
        <DataFormCard
          isVisible={showForm}
          onCancel={() => setShowForm(false)}
          config={config}
          onReset={onReset}
          onAddField={handleAddField}
        >
          {children}
        </DataFormCard>
      </Box>
    </Box>
  );
}

