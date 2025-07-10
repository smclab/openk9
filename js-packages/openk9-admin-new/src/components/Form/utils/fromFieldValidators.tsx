type Nullable<T> = T | null | undefined;

export const fromFieldValidators =
  (
    fieldValidators: Nullable<
      Array<
        Nullable<{
          field?: Nullable<string>;
          message?: Nullable<string>;
        }>
      >
    >,
  ) =>
  (field: string) =>
    fieldValidators?.flatMap((entry) =>
      entry?.field === field ? (entry.message ? [entry.message] : []) : [],
    ) ?? [];

type FieldValidator = {
  __typename?: string;
  field?: string | null;
  message?: string | null;
} | null;

export const combineErrorMessages = (
  fieldValidators?: FieldValidator[] | null,
): string => {
  if (!fieldValidators || !Array.isArray(fieldValidators)) return "";

  return fieldValidators
    .filter((validator) => validator?.message)
    .map((validator) => validator!.message!)
    .join("\n");
};
