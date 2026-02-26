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

export enum FieldType {
    NULL = 'NULL',
    BINARY = 'BINARY',
    BOOLEAN = 'BOOLEAN',
    KEYWORD = 'KEYWORD',
    CONSTANT_KEYWORD = 'CONSTANT_KEYWORD',
    WILDCARD = 'WILDCARD',
    DATE = 'DATE',
    DATE_NANOS = 'DATE_NANOS',
    LONG = 'LONG',
    INTEGER = 'INTEGER',
    SHORT = 'SHORT',
    BYTE = 'BYTE',
    DOUBLE = 'DOUBLE',
    FLOAT = 'FLOAT',
    HALF_FLOAT = 'HALF_FLOAT',
    SCALED_FLOAT = 'SCALED_FLOAT',
    UNSIGNED_LONG = 'UNSIGNED_LONG',
    OBJECT = 'OBJECT',
    FLATTENED = 'FLATTENED',
    NESTED = 'NESTED',
    JOIN = 'JOIN',
    LONG_RANGE = 'LONG_RANGE',
    DOUBLE_RANGE = 'DOUBLE_RANGE',
    DATE_RANGE = 'DATE_RANGE',
    IP_RANGE = 'IP_RANGE',
    IP = 'IP',
    VERSION = 'VERSION',
    MURMUR3 = 'MURMUR3',
    HISTOGRAM = 'HISTOGRAM',
    TEXT = 'TEXT',
    ANNOTATED_TEXT = 'ANNOTATED_TEXT',
    COMPLETION = 'COMPLETION',
    SEARCH_AS_YOU_TYPE = 'SEARCH_AS_YOU_TYPE',
    TOKEN_COUNT = 'TOKEN_COUNT',
    DENSE_VECTOR = 'DENSE_VECTOR',
    KNN_VECTOR = 'KNN_VECTOR',
    SPARSE_VECTOR = 'SPARSE_VECTOR',
    RANK_FEATURE = 'RANK_FEATURE',
    RANK_FEATURES = 'RANK_FEATURES',
    GEO_POINT = 'GEO_POINT',
    GEO_SHAPE = 'GEO_SHAPE',
    POINT = 'POINT',
    SHAPE = 'SHAPE',
    PERCOLATOR = 'PERCOLATOR',
    I18N = 'I18N',
}

