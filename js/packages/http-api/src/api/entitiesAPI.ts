import { EntityLookupRequest, EntityLookupResponse } from "../types";
import { apiBaseUrl } from "./common";

export async function doSearchEntities(
  query: EntityLookupRequest,
): Promise<EntityLookupResponse> {
  const request = await fetch(`${apiBaseUrl}/entity`, {
    method: "POST",
    body: JSON.stringify(query),
  });
  return await request.json();
}
