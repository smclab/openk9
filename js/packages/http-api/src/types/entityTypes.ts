export type EntityDescription = {
  type: string;
  entityId: string;
  name: string;
};

export interface EntityLookupRequest {
  entityId?: string;
  all?: string;
  type?: string;
}

export type EntityLookupResponse = {
  result: EntityDescription[];
  total: number;
  last: boolean;
};
