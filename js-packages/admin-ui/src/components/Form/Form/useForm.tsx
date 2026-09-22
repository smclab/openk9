import React from "react";

function singeltonByKey<K, V>(factory: (key: K) => V) {
  const cache = new Map<K, V>();
  return (key: K) => {
    const existing = cache.get(key);
    if (existing) return existing;
    const created = factory(key);
    cache.set(key, created);
    return created;
  };
}

export type FormInputProps<V> = {
  id: string;
  value: V;
  onChange(value: V): void;
  disabled: boolean;
  validationMessages: Array<string>;
  map<M>(mapValue: (value: V) => M, mapOnChange: (value: M) => V): Omit<FormInputProps<M>, "map">;
};

/**
 * Vista in sola lettura di un form, per nome di campo. Qualunque `FormApi<F>`
 * vi e' assegnabile: permette ai componenti generici (recap, wizard) di leggere
 * un campo senza conoscere la forma del form ne' ricorrere a un cast.
 */
export type FormFieldReader = {
  inputProps(field: string): { value: unknown; validationMessages: Array<string> };
};

export type FormApi<F extends object> = {
  submit(): void;
  inputProps<K extends keyof F>(field: K): FormInputProps<F[K]>;
  canSubmit: boolean;
};

export function useForm<F extends object>({
  originalValues,
  initialValues,
  isLoading,
  onSubmit,
  getValidationMessages,
}: {
  originalValues:
    | {
        [K in keyof F]?: F[K] | null | undefined;
      }
    | null
    | undefined;
  initialValues: F;
  isLoading: boolean;
  onSubmit(data: F): void;
  getValidationMessages?(field: keyof F): Array<string>;
}): FormApi<F> {
  const [state, setState] = React.useState({
    values: initialValues,
    info: Object.fromEntries(Object.keys(initialValues).map((key) => [key, { isDirty: false }])) as {
      [K in keyof F]: { isDirty: boolean };
    },
  });
  const getValue = React.useCallback(
    <K extends keyof F>({ values, info }: typeof state, field: K) => {
      return info[field]?.isDirty ? values[field] : originalValues?.[field] ?? initialValues[field];
    },
    [initialValues, originalValues],
  );
  const onChangeByField = React.useMemo(
    () =>
      singeltonByKey(<K extends keyof F>(field: K) => (value: F[K] | ((current: F[K]) => F[K])) => {
        setState((state) => ({
          values: {
            ...state.values,
            [field]: value instanceof Function ? value(getValue(state, field)) : value,
          },
          info: {
            ...state.info,
            [field]: { ...state.info[field], isDirty: true },
          },
        }));
      }),
    [getValue],
  );
  return {
    submit() {
      const fields = Object.keys(initialValues) as Array<keyof F>;
      const values = {} as F;
      fields.forEach((field) => {
        values[field] = getValue(state, field);
      });
      onSubmit(values);
    },
    inputProps<K extends keyof F>(field: K) {
      const id: string = field as string;
      const value: F[K] = getValue(state, field);
      const onChange: (value: F[K]) => void = onChangeByField(field);
      const disabled: boolean = isLoading;
      const validationMessages: Array<string> = getValidationMessages ? getValidationMessages(field) : [];
      return {
        id,
        value,
        onChange,
        disabled,
        validationMessages,
        map<M>(mapValue: (value: F[K]) => M, mapOnChange: (value: M) => F[K]) {
          return {
            id,
            value: mapValue(value),
            onChange: (value: M) => onChange(mapOnChange(value)),
            disabled,
            validationMessages,
          };
        },
      };
    },
    canSubmit: !isLoading,
  };
}
