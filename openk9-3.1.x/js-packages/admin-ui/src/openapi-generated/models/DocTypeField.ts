/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { FieldType } from './FieldType';
import type { OffsetDateTime } from './OffsetDateTime';

export type DocTypeField = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
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

