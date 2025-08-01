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

export function useForm<F extends Record<string, any>>({
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
}) {
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
      singeltonByKey(<K extends keyof F>(field: K) => (value: F[K] | ((value: F[K]) => F[K])) => {
        setState((state) => ({
          values: {
            ...state.values,
            [field]: typeof value === "function" ? (value as any)(getValue(state, field)) : value,
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
      onSubmit(Object.fromEntries(Object.keys(initialValues).map((key) => [key, getValue(state, key)])) as any);
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
