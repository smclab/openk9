/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { FieldType } from './FieldType';
import type { OffsetDateTime } from './OffsetDateTime';

export type DocTypeField = {
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    id?: number;
    name?: string;
    description?: string;
    fieldName?: string;
    searchable?: boolean;
    boost?: number;
    fieldType?: FieldType;
    exclude?: boolean;
    jsonConfig?: string;
    docTypeFieldAndChildren?: Array<DocTypeField>;
    floatBoost?: number;
    keyword?: boolean;
    text?: boolean;
    searchableAndText?: boolean;
    numeric?: boolean;
    date?: boolean;
    boolean?: boolean;
    autocomplete?: boolean;
    defaultExclude?: boolean;
    searchableAndDate?: boolean;
};
