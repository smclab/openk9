export const apiGroupLabel: Record<string, string> = {
  ADMINISTRATION: "Administration",
  PUBLIC: "Public",
  SEARCH: "Search",
  INGESTION: "Ingestion",
};

export const apiGroupDescription: Record<string, string> = {
  ADMINISTRATION: "Admin GraphQL APIs (/api/datasource/graphql)",
  PUBLIC: "Public datasource APIs (/api/datasource/buckets/current/**)",
  SEARCH: "Search and RAG APIs (/api/searcher/**, /api/rag/**)",
  INGESTION: "Ingestion APIs (/api/ingestion/**)",
};

export type ApiKeyDisplayStatus = "ACTIVE" | "EXPIRED" | "REVOKED";

export function deriveDisplayStatus(
  status: string | null | undefined,
  expirationDate: string | null | undefined
): ApiKeyDisplayStatus {
  if (status === "REVOKED") return "REVOKED";
  if (expirationDate && new Date(expirationDate) < new Date()) return "EXPIRED";
  return "ACTIVE";
}
