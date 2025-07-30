/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { FormFieldValidator } from './FormFieldValidator';
import type { FormFieldValue } from './FormFieldValue';
import type { FormType } from './FormType';

export type FormField = {
    info?: string;
    label?: string;
    name?: string;
    type?: FormType;
    size?: number;
    required?: boolean;
    values?: Array<FormFieldValue>;
    validator?: FormFieldValidator;
};
