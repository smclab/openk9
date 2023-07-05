/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { FieldType } from './FieldType';

export type DocTypeFieldDTO = {
    name: string;
    description?: string;
    searchable: boolean;
    sortable: boolean;
    boost?: number;
    fieldType: FieldType;
    exclude?: boolean;
    fieldName: string;
    jsonConfig?: string;
};
