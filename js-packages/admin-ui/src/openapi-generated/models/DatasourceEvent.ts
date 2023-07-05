/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

export type DatasourceEvent = {
    ingestionId?: string;
    datasourceId?: number;
    contentId?: string;
    parsingDate?: number;
    rawContent?: string;
    tenantId?: string;
    documentTypes?: Array<string>;
    indexName?: string;
    errorMessage?: string;
};
