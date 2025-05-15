/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { FieldType } from './FieldType';
import type { OffsetDateTime } from './OffsetDateTime';

export type DocTypeField = {
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    id?: number;
    floatBoost?: number;
    keyword?: boolean;
    text?: boolean;
    numeric?: boolean;
    date?: boolean;
    boolean?: boolean;
    i18N?: boolean;
    autocomplete?: boolean;
    defaultExclude?: boolean;
    defaultBoost?: boolean;
    searchableAndAutocomplete?: boolean;
    searchableAndDate?: boolean;
    searchableAndText?: boolean;
    searchableAndI18N?: boolean;
    name?: string;
    description?: string;
    fieldName?: string;
    searchable?: boolean;
    sortable?: boolean;
    boost?: number;
    fieldType?: FieldType;
    exclude?: boolean;
    jsonConfig?: string;
    path?: string;
    children?: Array<DocTypeField>;
    docTypeFieldAndChildren?: Array<DocTypeField>;
};
