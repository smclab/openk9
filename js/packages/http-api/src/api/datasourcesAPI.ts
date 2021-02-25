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

export type SupportedDataSource = {
  name: string;
  active: boolean;
  documentTypes: DocumentType[];
  defaultDocumentType: DocumentType;
};

export async function getSupportedDataSources(): Promise<
  SupportedDataSource[]
> {
  const request = await fetch(`${apiBaseUrl}/supported-datasources`);
  const response: SupportedDataSource[] = await request.json();
  return response;
}
