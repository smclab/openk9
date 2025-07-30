/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

export type DatasourceDTO = {
    name: string;
    description?: string;
    jsonConfig?: string;
    purgeable?: boolean;
    purging?: string;
    purgeMaxAge?: string;
    reindexable?: boolean;
    reindexing?: string;
    schedulable?: boolean;
    scheduling?: string;
};
