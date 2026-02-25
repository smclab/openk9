/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { FieldValue } from './FieldValue';
import type { FormFieldType } from './FormFieldType';
import type { FormFieldValidator } from './FormFieldValidator';

export type FormField = {
    info?: string;
    label?: string;
    name?: string;
    type?: FormFieldType;
    size?: number;
    required?: boolean;
    values?: Array<FieldValue>;
    validator?: FormFieldValidator;
};
