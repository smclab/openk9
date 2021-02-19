import { apiBaseUrl } from "./common";

export interface SearchKeyword {
  keyword: string;
  fieldBoost: { [key: string]: number };
}

export type DocumentType = {
  name: string;
  icon: string;
  searchKeywords: SearchKeyword[];
};

export type DataSource = {
  name: string;
  active: boolean;
  documentTypes: DocumentType[];
  defaultDocumentType: DocumentType;
};

export async function getDataSources(): Promise<DataSource[]> {
  const request = await fetch(`${apiBaseUrl}/supported-datasources`);
  const response: DataSource[] = await request.json();
  return response;
}
