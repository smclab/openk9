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
