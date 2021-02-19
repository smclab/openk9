export type DSItem = {
  datasourceId: number;
  active: boolean;
  description: string;
  jsonConfig: string;
  lastIngestionDate: number;
  name: string;
  tenantId: number;
  scheduling: string;
  driverServiceName: string;
};
