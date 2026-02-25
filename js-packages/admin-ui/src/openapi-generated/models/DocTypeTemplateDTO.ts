/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { TemplateType } from './TemplateType';

export type DocTypeTemplateDTO = {
    name: string;
    description?: string;
    templateType: TemplateType;
    source: string;
    compiled: string;
};
