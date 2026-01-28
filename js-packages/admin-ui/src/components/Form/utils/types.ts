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
import React from "react";

export type SelectProps<T> = {
    label: string;
    description?: string;
    disabled?: boolean;
    items: {
        label: string;
        value: string;
    }[];
    setItems: React.Dispatch<
        React.SetStateAction<
            {
                label: string;
                value: string;
            }[]
        >
    >;
};

export type BaseInputProps<T> = {
    id: string;
    label: string;
    value: T;
    onChange(value: T): void;
    disabled: boolean;
    validationMessages: Array<string>;
    description?: string;
    isNotEnum?: boolean;
};

export type TemplateArray = {
    title: string;
    description: string;
    Json: string;
    descriptionAttribute: string;
    visible: string;
  };

export type ExtendedInputProps<T> = BaseInputProps<T> & {
    setTitle?: (value: string) => void;
    type: string;
    setIsCustom: (value: boolean) => void;
    template: TemplateArray[];
    onChangeDescription: (value: string) => void;
    setTemplateChoice: (value: KeyValue) => void;
    onChangeType: (value: string) => void;
  };


export type KeyValue = {
    [key: string]: any;
};

