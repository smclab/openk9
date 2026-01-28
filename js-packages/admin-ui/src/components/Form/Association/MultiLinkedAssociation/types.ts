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
export type DataFormCardProps = {
  isVisible: boolean;
  onCancel: () => void;
  options: any;
  config: DataFormElementConfig;
  children: React.ReactNode;
  fields: FieldDocType[];
  // setFields: React.Dispatch<React.SetStateAction<Field[]>>;
};

export type DataFormElementConfig = {
  title: string;
  description: string;
  addLabel?: string | "Add";
  resetLabel?: string | "Reset";
};

export type FieldDocType = {
  fieldName: string;
  userField: string;
  docTypeId: string;
  userFieldId: string;
};

export type Field = {
  id: string;
  label: string;
};

export type UserField = "EMAIL" | "NAME" | "NAMESURNAME" | "ROLES" | "SURNAME" | "USERNAME";

export interface DocTypeConfig {
  docTypeId: number;
  userField: UserField;
}

export type SelectedValue = {
  id: string;
  name: string;
};

export type RowInfo = {
  itemLabel?: string;
  ItemId?: string;
  associatedLabel?: string;
  associatedLabelId?: string;
};

