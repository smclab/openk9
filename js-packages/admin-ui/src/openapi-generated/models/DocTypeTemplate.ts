/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';
import type { TemplateType } from './TemplateType';

export type DocTypeTemplate = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    name?: string;
    description?: string;
    templateType?: TemplateType;
    source?: string;
    compiled?: string;
};
