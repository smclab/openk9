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
