import React, { CSSProperties, Dispatch, SetStateAction, TableHTMLAttributes, TdHTMLAttributes } from "react";
import { ClayInput, ClaySelect, ClayToggle } from "@clayui/form";
import { ApolloQueryResult, MutationHookOptions, MutationTuple, QueryHookOptions, QueryResult } from "@apollo/client";
import useDebounced from "./useDebounced";
import ClayButton, { ClayButtonWithIcon } from "@clayui/button";
import ClayModal, { useModal } from "@clayui/modal";
import ClayList from "@clayui/list";
import { Virtuoso, Components as VirtuosoComponents, TableVirtuoso } from "react-virtuoso";
import ClayToolbar from "@clayui/toolbar";
import ClayMultiSelect from "@clayui/multi-select";
import ClayIcon from "@clayui/icon";
import { ClayTooltipProvider } from "@clayui/tooltip";
import { AnalyzerQuery, DataSourceQuery, Exact, TokenizerQuery, UserField } from "../graphql-generated";
import { TableRowActions } from "./Table";
import { ClassNameButton } from "../App";
import { Observer } from "@clayui/modal/lib/types";
import { BrandLogo } from "./BrandLogo";
import ClayCard from "@clayui/card";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip as TooltipRecharts, Legend } from "recharts";
import ClayTable from "@clayui/table";

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
    >
  ) =>
  (field: string) =>
    fieldValidators?.flatMap((entry) => (entry?.field === field ? (entry.message ? [entry.message] : []) : [])) ?? [];
export type KeyValue = {
  [key: string]: any;
};
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
  getValidationMessages(field: keyof F): Array<string>;
}) {
  const [state, setState] = React.useState({
    values: initialValues,
    info: Object.fromEntries(Object.keys(initialValues).map((key) => [key, { isDirty: false }])) as {
      [K in keyof F]: { isDirty: boolean };
    },
  });
  const getValue = React.useCallback(
    <K extends keyof F>({ values, info }: typeof state, field: K) =>
      info[field].isDirty ? values[field] : originalValues?.[field] ?? initialValues[field],
    [initialValues, originalValues]
  );
  const onChangeByField = React.useMemo(
    () =>
      singeltonByKey(<K extends keyof F>(field: K) => (value: F[K] | ((value: F[K]) => F[K])) => {
        setState((state) => ({
          values: { ...state.values, [field]: typeof value === "function" ? (value as any)(getValue(state, field)) : value },
          info: { ...state.info, [field]: { ...state.info[field], isDirty: true } },
        }));
      }),
    [getValue]
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
      const validationMessages: Array<string> = getValidationMessages(field);
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

export function useCompoundFormField<T, F extends Record<string, any>>({
  value,
  onChange,
  initialValues,
  serialize,
  deserialize,
  disabled,
}: {
  value: T;
  onChange(value: T | ((v: T) => T)): void;
  initialValues: F;
  serialize(values: F): T;
  deserialize(value: T): F;
  disabled: boolean;
}) {
  const onChangeByField = React.useMemo(
    () =>
      singeltonByKey(<K extends keyof F>(field: K) => (v: F[K] | ((value: F[K]) => F[K])) => {
        onChange((value) => {
          const deserialized = deserialize(value);
          const newValue = typeof v === "function" ? (v as any)(deserialized[field]) : v;
          return serialize({ ...deserialized, [field]: newValue });
        });
      }),
    [deserialize, onChange, serialize]
  );
  return {
    inputProps<K extends keyof F>(
      field: K
    ): { id: string; value: F[K]; onChange(value: F[K]): void; disabled: boolean; validationMessages: Array<string> } {
      return {
        id: field as string,
        value: deserialize(value)[field] ?? initialValues[field],
        onChange: onChangeByField(field),
        disabled,
        validationMessages: [],
      };
    },
  };
}

export type BaseInputProps<T> = {
  id: string;
  label: string;
  value: T;
  onChange(value: T): void;
  disabled: boolean;
  validationMessages: Array<string>;
  description?: string;
};

export function TextInput({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  item,
  description,
}: BaseInputProps<string> & { item?: boolean }) {
  return (
    <div className={`${item ? "form-group-item" : "form-group"} ${validationMessages.length ? "has-warning" : ""}`}>
      <label htmlFor={id}>{label}</label>
      {description && InformationField(description)}
      <div style={{ position: "relative" }}>
        <input
          type="text"
          className="form-control"
          id={id}
          value={value}
          onChange={(event) => onChange(event.currentTarget.value)}
          disabled={disabled}
        ></input>
        {value !== "" && (
          <ClayButtonWithIcon
            aria-label=""
            symbol="times"
            className="component-action"
            onClick={() => {
              onChange("");
            }}
            style={{ position: "absolute", right: "20px", top: "5px" }}
          />
        )}
      </div>
      <div className="form-feedback-group">
        {validationMessages.map((validationMessage, index) => {
          return (
            <div key={index} className="form-feedback-item">
              {validationMessage}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function TextArea({ id, label, value, onChange, disabled, validationMessages, description }: BaseInputProps<string>) {
  return (
    <div className={`form-group ${validationMessages.length ? "has-warning" : ""}`}>
      <label htmlFor={id}>{label}</label>
      {description && InformationField(description)}
      <textarea
        className="form-control"
        id={id}
        value={value}
        onChange={(event) => onChange(event.currentTarget.value)}
        disabled={disabled}
      ></textarea>
      <div className="form-feedback-group">
        {validationMessages.map((validationMessage, index) => {
          return (
            <div key={index} className="form-feedback-item">
              {validationMessage}
            </div>
          );
        })}
      </div>
    </div>
  );
}
export function InformationField(description: string) {
  return (
    <span style={{ marginLeft: "5px" }}>
      <ClayTooltipProvider autoAlign>
        <span title={description}>
          <ClayIcon symbol="info-panel-closed" style={{ cursor: "pointer" }}></ClayIcon>
        </span>
      </ClayTooltipProvider>
    </span>
  );
}
export function StringListInput({ id, label, value, onChange, disabled, validationMessages }: BaseInputProps<Array<string>>) {
  const [newItemText, setNewItemText] = React.useState("");
  return (
    <div className={`form-group ${validationMessages.length ? "has-warning" : ""}`}>
      <label htmlFor={id}>{label}</label>
      <ClayMultiSelect
        value={newItemText}
        onChange={setNewItemText}
        disabled={disabled}
        items={value.map((value) => ({ label: value, value }))}
        onItemsChange={(value: Array<{ label?: string; value?: string }>) => {
          onChange(value.map(({ value }) => value!));
        }}
      />

      <div className="form-feedback-group">
        {validationMessages.map((validationMessage, index) => {
          return (
            <div key={index} className="form-feedback-item">
              {validationMessage}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function EnumSelect<E extends Record<string, any>>({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  dict,
  description,
}: BaseInputProps<E[string]> & { dict: E }) {
  return (
    <div className={`form-group ${validationMessages.length ? "has-warning" : ""}`}>
      <label htmlFor={id}>{label}</label>
      {description && InformationField(description)}
      <select
        className="form-control"
        id={id}
        value={value}
        onChange={(event) => onChange(event.currentTarget.value as E[string])}
        disabled={disabled}
      >
        {Object.entries(dict).map(([label, value]) => {
          return (
            <option key={value} value={value}>
              {label}
            </option>
          );
        })}
      </select>
      <div className="form-feedback-group">
        {validationMessages.map((validationMessage, index) => {
          return (
            <div key={index} className="form-feedback-item">
              {validationMessage}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function EnumSelectSimple<E extends Record<string, any>>({
  id,
  label,
  value,
  onChange,
  dict,
  dimension,
}: BaseInputProps<E[string]> & { dict: E; dimension: number }) {
  return (
    <div className="form-group">
      <label htmlFor={id}>{label}</label>
      <select
        style={{ maxWidth: dimension + "px" }}
        className="form-control"
        id={id}
        value={value}
        onChange={(event) => onChange(event.currentTarget.value as E[string])}
      >
        {Object.entries(dict).map(([label, value]) => {
          return (
            <option key={value} value={value}>
              {label}
            </option>
          );
        })}
      </select>
    </div>
  );
}
export function BooleanInput({ id, label, value, onChange, disabled, validationMessages, description }: BaseInputProps<boolean>) {
  return (
    <div className={`form-group ${validationMessages.length ? "has-warning" : ""}`} style={{ display: "flex" }}>
      <style type="text/css">{StyleToggle}</style>
      <ClayToggle id={id} label={label} toggled={value} onToggle={onChange} disabled={disabled} />
      {description && InformationField(description)}
      <div className="form-feedback-group">
        {validationMessages.map((validationMessage, index) => {
          return (
            <div key={index} className="form-feedback-item">
              {validationMessage}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function NumberInput({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  item,
  description,
}: BaseInputProps<number> & { item?: boolean }) {
  const ref = React.useRef<HTMLInputElement | null>(null);
  React.useLayoutEffect(() => {
    if (ref.current) {
      ref.current.valueAsNumber = value;
    }
  }, [value]);
  return (
    <div className={`${item ? "form-group-item" : "form-group"} ${validationMessages.length ? "has-warning" : ""}`}>
      <label htmlFor={id}>{label}</label>
      {description && InformationField(description)}
      <input
        ref={ref}
        type="number"
        step="any"
        className="form-control"
        id={id}
        onChange={(event) => {
          if (!isNaN(event.currentTarget.valueAsNumber)) onChange(event.currentTarget.valueAsNumber);
        }}
        disabled={disabled}
      ></input>
      <div className="form-feedback-group">
        {validationMessages.map((validationMessage, index) => {
          return (
            <div key={index} className="form-feedback-item">
              {validationMessage}
            </div>
          );
        })}
      </div>
    </div>
  );
}

type QueryHook<Query, QueryVariables extends Record<string, any>> = (
  baseOptions: QueryHookOptions<Query, QueryVariables>
) => QueryResult<Query, QueryVariables>;

type MutationHook<Mutation, MutationVariables extends Record<string, any>> = (
  baseOptions: MutationHookOptions<Mutation, MutationVariables>
) => MutationTuple<Mutation, MutationVariables>;

export function SearchSelect<Value, Change extends Record<string, any>, Remove extends Record<string, any>>({
  label,
  value,
  useValueQuery,
  useOptionsQuery,
  useChangeMutation,
  mapValueToMutationVariables,
  useRemoveMutation,
  mapValueToRemoveMutationVariables,
  invalidate,
  description,
}: {
  label: string;
  value: Value | null | undefined;
  description?: string;
  useValueQuery: QueryHook<{ value?: { id?: string | null; name?: string | null; description?: string | null } | null }, { id: Value }>;
  useOptionsQuery: QueryHook<
    {
      options?: {
        edges?: Array<{ node?: { id?: string | null; name?: string | null; description?: string | null } | null } | null> | null;
        pageInfo?: { hasNextPage: boolean; endCursor?: string | null } | null;
      } | null;
    },
    { searchText?: string | null; cursor?: string | null }
  >;
  mapValueToMutationVariables(id: string): Change;
  useChangeMutation: MutationHook<any, Change>;
  mapValueToRemoveMutationVariables(): Remove;
  useRemoveMutation: MutationHook<any, Remove>;
  invalidate(): void;
}) {
  const [searchText, setSearchText] = React.useState("");
  const searchTextDebounced = useDebounced(searchText);
  const valueQuery = useValueQuery({ variables: { id: value as Value }, skip: !value });
  const optionsQuery = useOptionsQuery({ variables: { searchText: searchTextDebounced } });
  const [changeMutate, changeMutation] = useChangeMutation({});
  const { observer, onOpenChange, open } = useModal();
  const scrollerRef = React.useRef<HTMLElement>();
  const [removeMutate, removeMutation] = useRemoveMutation({});
  return (
    <React.Fragment>
      <CustomFormGroup>
        <label>{label}</label>
        {description && InformationField(description)}
        <ClayInput.Group>
          <ClayInput.GroupItem>
            <ClayInput
              type="text"
              className="form-control"
              style={{ backgroundColor: "#f1f2f5" }}
              readOnly
              disabled={!value}
              value={valueQuery.data?.value?.name ?? ""}
            />
          </ClayInput.GroupItem>
          <ClayInput.GroupItem append shrink>
            <ClayButton.Group>
              <ClayButton
                displayType="secondary"
                style={{
                  border: "1px solid #393B4A",
                  borderRadius: "3px",
                }}
                onClick={() => onOpenChange(true)}
              >
                <span
                  style={{
                    fontFamily: "Helvetica",
                    fontStyle: "normal",
                    fontWeight: "700",
                    fontSize: "15px",
                    color: "#393B4A",
                  }}
                >
                  Change
                </span>
              </ClayButton>
              <ClayButton
                displayType="secondary"
                disabled={typeof valueQuery.data?.value?.name === "string" ? false : true}
                style={{ marginLeft: "10px", border: "1px solid #393B4A", borderRadius: "3px" }}
                onClick={() => {
                  if (!changeMutation.loading && !removeMutation.loading)
                    removeMutate({
                      variables: mapValueToRemoveMutationVariables(),
                      onCompleted() {
                        invalidate();
                      },
                    });
                }}
              >
                <span
                  style={{
                    fontFamily: "Helvetica",
                    fontStyle: "normal",
                    fontWeight: "700",
                    fontSize: "15px",
                    color: "#393B4A",
                  }}
                >
                  Remove
                </span>
              </ClayButton>
            </ClayButton.Group>
          </ClayInput.GroupItem>
        </ClayInput.Group>
      </CustomFormGroup>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>{label}</ClayModal.Header>
          <ClayModal.Body>
            <CustomFormGroup>
              <ClayInput
                type="search"
                placeholder="search"
                value={searchText}
                onChange={(event) => setSearchText(event.currentTarget.value)}
              />
            </CustomFormGroup>
            <Virtuoso
              totalCount={optionsQuery.data?.options?.edges?.length}
              scrollerRef={(element) => (scrollerRef.current = element as any)}
              style={{ height: "400px" }}
              components={ClayListComponents}
              itemContent={(index) => {
                const row = optionsQuery.data?.options?.edges?.[index]?.node ?? undefined;
                return (
                  <React.Fragment>
                    <ClayList.ItemField expand>
                      <ClayList.ItemTitle>{row?.name || "..."}</ClayList.ItemTitle>
                      <ClayList.ItemText>{row?.description || "..."}</ClayList.ItemText>
                    </ClayList.ItemField>
                    <ClayList.ItemField>
                      <ClayList.QuickActionMenu>
                        {!changeMutation.loading && !removeMutation.loading && (
                          <ClayList.QuickActionMenu.Item
                            onClick={() => {
                              if (row?.id) {
                                changeMutate({
                                  variables: mapValueToMutationVariables(row.id),
                                  onCompleted() {
                                    onOpenChange(false);
                                  },
                                });
                              }
                            }}
                            symbol="play"
                          />
                        )}
                      </ClayList.QuickActionMenu>
                    </ClayList.ItemField>
                  </React.Fragment>
                );
              }}
              endReached={() => {
                if (optionsQuery?.data?.options?.pageInfo?.hasNextPage) {
                  optionsQuery.fetchMore({
                    variables: {
                      cursor: optionsQuery?.data?.options?.pageInfo?.endCursor,
                    },
                  });
                }
              }}
              isScrolling={(isScrolling) => {
                if (scrollerRef.current) {
                  if (isScrolling) {
                    scrollerRef.current.style.pointerEvents = "none";
                  } else {
                    scrollerRef.current.style.pointerEvents = "auto";
                  }
                }
              }}
            />
          </ClayModal.Body>
          <ClayModal.Footer
            first={
              <ClayButton displayType="secondary" onClick={() => onOpenChange(false)}>
                Cancel
              </ClayButton>
            }
          />
        </ClayModal>
      )}
    </React.Fragment>
  );
}

export function SearchSelectGraphql<Value, Change extends Record<string, any>, Remove extends Record<string, any>>({
  label,
  value,
  useValueQuery,
  useOptionsQuery,
  useChangeMutation,
  mapValueToMutationVariables,
  useRemoveMutation,
  mapValueToRemoveMutationVariables,
  invalidate,
  description,
  refetch,
}: {
  label: string;
  value: Value | null | undefined;
  description?: string;
  refetch: (variables?: Partial<Exact<{ id: string }>> | undefined) => Promise<ApolloQueryResult<DataSourceQuery>>;
  useValueQuery: QueryHook<{ value?: { id?: string | null; name?: string | null; description?: string | null } | null }, { id: Value }>;
  useOptionsQuery:
    | {
        __typename?: "DefaultConnection_DataIndex" | undefined;
        edges?: Array<{
          __typename?: "DefaultEdge_DataIndex" | undefined;
          node?:
            | {
                __typename?: "DataIndex" | undefined;
                id?: string | null | undefined;
                name?: string | null | undefined;
              }
            | null
            | undefined;
        } | null> | null;
      }
    | null
    | undefined;

  mapValueToMutationVariables(id: string): Change;
  useChangeMutation: MutationHook<any, Change>;
  mapValueToRemoveMutationVariables(): Remove;
  useRemoveMutation: MutationHook<any, Remove>;
  invalidate(): void;
}) {
  const [searchText, setSearchText] = React.useState("");
  const valueQuery = useValueQuery({ variables: { id: value as Value }, skip: !value });
  const [changeMutate, changeMutation] = useChangeMutation({});
  const { observer, onOpenChange, open } = useModal();
  const scrollerRef = React.useRef<HTMLElement>();
  const [removeMutate, removeMutation] = useRemoveMutation({});
  const searchTextDebounced = useDebounced(searchText);
  React.useEffect(() => {
    refetch({ searchText: searchTextDebounced } as any);
  }, [refetch, searchTextDebounced]);
  return (
    <React.Fragment>
      <CustomFormGroup>
        <label>{label}</label>
        {description && InformationField(description)}
        <ClayInput.Group>
          <ClayInput.GroupItem>
            <ClayInput
              type="text"
              className="form-control"
              style={{ backgroundColor: "#f1f2f5" }}
              readOnly
              disabled={!value}
              value={valueQuery.data?.value?.name ?? ""}
            />
          </ClayInput.GroupItem>
          <ClayInput.GroupItem append shrink>
            <ClayButton.Group>
              <ClayButton
                displayType="secondary"
                style={{
                  border: "1px solid #393B4A",
                  borderRadius: "3px",
                }}
                onClick={() => onOpenChange(true)}
              >
                <span
                  style={{
                    fontFamily: "Helvetica",
                    fontStyle: "normal",
                    fontWeight: "700",
                    fontSize: "15px",
                    color: "#393B4A",
                  }}
                >
                  Change
                </span>
              </ClayButton>
              <ClayButton
                displayType="secondary"
                disabled={typeof valueQuery.data?.value?.name === "string" ? false : true}
                style={{ marginLeft: "10px", border: "1px solid #393B4A", borderRadius: "3px" }}
                onClick={() => {
                  if (!changeMutation.loading && !removeMutation.loading)
                    removeMutate({
                      variables: mapValueToRemoveMutationVariables(),
                      onCompleted() {
                        invalidate();
                      },
                    });
                }}
              >
                <span
                  style={{
                    fontFamily: "Helvetica",
                    fontStyle: "normal",
                    fontWeight: "700",
                    fontSize: "15px",
                    color: "#393B4A",
                  }}
                >
                  Remove
                </span>
              </ClayButton>
            </ClayButton.Group>
          </ClayInput.GroupItem>
        </ClayInput.Group>
      </CustomFormGroup>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>{label}</ClayModal.Header>
          <ClayModal.Body>
            <CustomFormGroup>
              <ClayInput
                type="search"
                placeholder="search"
                value={searchText}
                onChange={(event) => setSearchText(event.currentTarget.value)}
              />
            </CustomFormGroup>
            <Virtuoso
              totalCount={useOptionsQuery?.edges?.length}
              scrollerRef={(element) => (scrollerRef.current = element as any)}
              style={{ height: "400px" }}
              components={ClayListComponents}
              itemContent={(index) => {
                const row = useOptionsQuery?.edges?.[index]?.node ?? undefined;
                return (
                  <React.Fragment>
                    <ClayList.ItemField expand>
                      <ClayList.ItemTitle>{row?.name || "..."}</ClayList.ItemTitle>
                      <ClayList.ItemText>{"..."}</ClayList.ItemText>
                    </ClayList.ItemField>
                    <ClayList.ItemField>
                      <ClayList.QuickActionMenu>
                        {!changeMutation.loading && !removeMutation.loading && (
                          <ClayList.QuickActionMenu.Item
                            onClick={() => {
                              if (row?.id) {
                                changeMutate({
                                  variables: mapValueToMutationVariables(row.id),
                                  onCompleted() {
                                    onOpenChange(false);
                                  },
                                });
                              }
                            }}
                            symbol="play"
                          />
                        )}
                      </ClayList.QuickActionMenu>
                    </ClayList.ItemField>
                  </React.Fragment>
                );
              }}
              isScrolling={(isScrolling) => {
                if (scrollerRef.current) {
                  if (isScrolling) {
                    scrollerRef.current.style.pointerEvents = "none";
                  } else {
                    scrollerRef.current.style.pointerEvents = "auto";
                  }
                }
              }}
            />
          </ClayModal.Body>
          <ClayModal.Footer
            first={
              <ClayButton displayType="secondary" onClick={() => onOpenChange(false)}>
                Cancel
              </ClayButton>
            }
          />
        </ClayModal>
      )}
    </React.Fragment>
  );
}
const ClayListComponents: VirtuosoComponents = {
  List: React.forwardRef(({ style, children }, listRef) => (
    <ul className="list-group show-quick-actions-on-hover" style={style} ref={listRef as any}>
      {children}
    </ul>
  )),
  Item: ({ children, ...props }) => (
    <ClayList.Item flex {...props}>
      {children}
    </ClayList.Item>
  ),
};

export function AssociatedEntities<Q>({
  label,
  parentId,
  list: { useListQuery, field },
  useAddMutation,
  useRemoveMutation,
}: {
  label: string;
  parentId: string;
  list: {
    useListQuery: QueryHook<Q, { parentId: string; unassociated: boolean; searchText?: string | null; cursor?: string | null }>;
    field(data: Q | undefined):
      | {
          edges?: Array<{ node?: { id?: string | null; name?: string | null; description?: string | null } | null } | null> | null;
          pageInfo?: { hasNextPage: boolean; endCursor?: string | null } | null;
        }
      | null
      | undefined;
  };
  useAddMutation: MutationHook<any, { parentId: string; childId: string }>;
  useRemoveMutation: MutationHook<any, { parentId: string; childId: string }>;
}) {
  const [searchText, setSearchText] = React.useState("");
  const [modalSearchText, setModalSearchText] = React.useState("");
  const searchTextDebounced = useDebounced(searchText);
  const modalSearchTextDebouced = useDebounced(modalSearchText);
  const associatedListQuery = useListQuery({ variables: { parentId, unassociated: false, searchText: searchTextDebounced } });
  const unassociatedListQuery = useListQuery({ variables: { parentId, unassociated: true, searchText: modalSearchTextDebouced } });
  const [addMutate, addMutation] = useAddMutation({
    onCompleted() {
      associatedListQuery.refetch();
      unassociatedListQuery.refetch();
    },
  });
  unassociatedListQuery.refetch();
  const [removeMutate, removeMutation] = useRemoveMutation({
    onCompleted() {
      associatedListQuery.refetch();
      unassociatedListQuery.refetch();
    },
  });
  const { observer, onOpenChange, open } = useModal();
  const scrollerRef = React.useRef<HTMLElement>();
  const canAct = !addMutation.loading && !removeMutation.loading;
  return (
    <React.Fragment>
      <ClayToolbar light>
        <ContainerFluidWithoutView>
          <ClayToolbar.Nav>
            <ClayToolbar.Item expand>
              <ClayToolbar.Input
                placeholder="Search..."
                sizing="sm"
                value={searchText}
                onChange={(event) => setSearchText(event.currentTarget.value)}
              />
            </ClayToolbar.Item>
            <ClayToolbar.Item>
              <ClayButton className={`${ClassNameButton}  btn-sm`} onClick={() => onOpenChange(true)}>
                <span className="inline-item inline-item-before">
                  <ClayIcon symbol="plus" />
                </span>
                {"Associate"}
              </ClayButton>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluidWithoutView>
      </ClayToolbar>

      <ContainerFluid>
        {(field(associatedListQuery.data)?.edges?.length ?? 0) !== 0 && <MainTitle title={label} />}
        {(field(associatedListQuery.data)?.edges?.length ?? 0) === 0 && !associatedListQuery.loading && (
          <EmptySpace description="There are no matching associated entities" title="No entities" extraClass="c-empty-state-animation" />
        )}
        <Virtuoso
          totalCount={field(associatedListQuery.data)?.edges?.length}
          scrollerRef={(element) => (scrollerRef.current = element as any)}
          style={{ height: "70vh" }}
          components={ClayListComponents}
          itemContent={(index) => {
            const row = field(associatedListQuery.data)?.edges?.[index]?.node ?? undefined;
            return (
              <React.Fragment>
                <ClayList.ItemField expand>
                  <ClayList.ItemTitle>{row?.name || "..."}</ClayList.ItemTitle>
                  <ClayList.ItemText>{row?.description || "..."}</ClayList.ItemText>
                </ClayList.ItemField>
                <ClayList.ItemField>
                  <ClayList.QuickActionMenu>
                    {canAct && (
                      <ClayList.QuickActionMenu.Item
                        onClick={() => {
                          if (row?.id) {
                            removeMutate({ variables: { parentId, childId: row.id } });
                          }
                        }}
                        symbol="chain-broken"
                      />
                    )}
                  </ClayList.QuickActionMenu>
                </ClayList.ItemField>
              </React.Fragment>
            );
          }}
          endReached={() => {
            if (field(associatedListQuery.data)?.pageInfo?.hasNextPage) {
              associatedListQuery.fetchMore({
                variables: {
                  cursor: field(associatedListQuery.data)?.pageInfo?.endCursor,
                },
              });
            }
          }}
          isScrolling={(isScrolling) => {
            if (scrollerRef.current) {
              if (isScrolling) {
                scrollerRef.current.style.pointerEvents = "none";
              } else {
                scrollerRef.current.style.pointerEvents = "auto";
              }
            }
          }}
        />
      </ContainerFluid>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>{label}</ClayModal.Header>
          <ClayModal.Body style={{ minHeight: "465px" }} scrollable={true}>
            <CustomFormGroup>
              <ClayInput
                type="search"
                placeholder="search"
                value={modalSearchText}
                onChange={(event) => setModalSearchText(event.currentTarget.value)}
              />
            </CustomFormGroup>
            {(field(unassociatedListQuery.data)?.edges?.length ?? 0) === 0 && !unassociatedListQuery.loading && (
              <EmptySpace
                description="There are no matching unassociated entities"
                title="No entities"
                extraClass="c-empty-state-animation"
              />
            )}
            {(field(unassociatedListQuery.data)?.edges?.length ?? 0) !== 0 && !unassociatedListQuery.loading && (
              <div
                className={"formContainer"}
                style={{
                  width: "550px",
                  height: "348px",
                  overflowY: (field(unassociatedListQuery.data)?.edges?.length ?? 0) > 4 ? "scroll" : "hidden",
                }}
              >
                <ClayList showQuickActionsOnHover>
                  {
                    field(unassociatedListQuery.data)?.edges?.map((edge) => {
                      return (
                        <ClayList.Item flex key={edge?.node?.id}>
                          <ClayList.ItemField expand>
                            <ClayList.ItemTitle>{edge?.node?.name || "..."}</ClayList.ItemTitle>
                            <ClayList.ItemText>{edge?.node?.description || "..."}</ClayList.ItemText>
                          </ClayList.ItemField>
                          <ClayList.ItemField>
                            <ClayList.QuickActionMenu>
                              {canAct && (
                                <ClayList.QuickActionMenu.Item
                                  onClick={() => {
                                    if (edge?.node?.id) {
                                      addMutate({ variables: { parentId, childId: edge.node.id } });
                                    }
                                  }}
                                  symbol="link"
                                />
                              )}
                            </ClayList.QuickActionMenu>
                          </ClayList.ItemField>
                        </ClayList.Item>
                      );
                    }) as any
                  }
                </ClayList>
              </div>
            )}
          </ClayModal.Body>
        </ClayModal>
      )}
    </React.Fragment>
  );
}

type ButtonType = "button" | "submit" | "reset";

export function CustomButtom({
  customStyle,
  action,
  nameButton,
  canSubmit = true,
  typeSelectet,
}: {
  nameButton: string;
  action?: any;
  customStyle?: any;
  canSubmit?: boolean;
  typeSelectet: ButtonType;
}) {
  return (
    <button className="btn btn-danger" disabled={canSubmit} type={typeSelectet}>
      {nameButton}
    </button>
  );
}

export function MainTitle({ title }: { title: string }) {
  return (
    <li className="list-group-item list-group-item-flex" style={{ overflowAnchor: "none" }}>
      <div className="autofit-col autofit-col-expand" style={{ alignItems: "center" }}>
        <p
          className="navbar-title navbar-text-truncate "
          style={{
            color: "#C22525",
            fontFamily: "Helvetica",
            fontStyle: "normal",
            fontWeight: "700",
            fontSize: "18px",
            lineHeight: "44px",
          }}
        >
          {title}
        </p>
      </div>
    </li>
  );
}

interface CustomTableBodyProps extends React.HTMLAttributes<HTMLTableSectionElement> {
  className?: string;
}

export const CustomTableBody: React.FC<CustomTableBodyProps> = ({ children, className, ...rest }) => {
  const classes = className ? `my-custom-table-body ${className}` : "my-custom-table-body";

  return (
    <tbody {...rest} className={classes}>
      {children}
    </tbody>
  );
};

export const CustomTableHead = (props: any) => {
  const { children, ...rest } = props;

  return (
    <thead className="table-head" {...rest}>
      {children}
    </thead>
  );
};

export const CustomTableRow = (props: any) => {
  const { children, ...rest } = props;

  return (
    <tr className="table-row" {...rest}>
      {children}
    </tr>
  );
};

interface CustomTableCellProps extends React.HTMLAttributes<HTMLTableCellElement> {
  headingCell?: boolean;
  style?: React.CSSProperties;
}

export const CustomTableCell: React.FC<CustomTableCellProps> = (props) => {
  const { headingCell, style, children, ...rest } = props;

  const cellClassName = headingCell ? "table-heading" : "";

  return (
    <td className={cellClassName} style={style} {...rest}>
      {children}
    </td>
  );
};

export function AssociatedEntitiesWithSelect<Q>({
  label,
  parentId,
  list: { useListQuery, field },
  useAddMutation,
  useRemoveMutation,
  remove,
  notSelect,
}: {
  label: string;
  parentId: string;
  notSelect: any;
  remove: any;
  list: {
    useListQuery: QueryHook<Q, { parentId: string; searchText?: string | null; cursor?: string | null }>;
    field(data: Q | undefined): any;
  };
  useAddMutation: any;
  useRemoveMutation: MutationHook<any, { parentId: string; childId: string }>;
}) {
  const [searchText, setSearchText] = React.useState("");
  const [modalSearchText, setModalSearchText] = React.useState("");
  const searchTextDebounced = useDebounced(searchText);
  const searchTextDebouncedInternal = useDebounced(modalSearchText);
  const associatedListQuery = useListQuery({ variables: { parentId, searchText: searchTextDebounced } });
  const unassociatedListQuery = notSelect({ variables: { searchText: searchTextDebouncedInternal } });
  const [addMutate, addMutation] = useAddMutation({
    onCompleted() {
      associatedListQuery.refetch();
      // unassociatedListQuery.refetch();
    },
  });
  const [removeMutate, removeMutation] = useRemoveMutation({
    onCompleted() {
      associatedListQuery.refetch();
      unassociatedListQuery.refetch();
    },
  });
  const { observer, onOpenChange, open } = useModal();
  const scrollerRef = React.useRef<HTMLElement>();
  const canAct = !addMutation.loading && !removeMutation.loading;
  const [userField, setUserField] = React.useState(UserField.Email);
  associatedListQuery.refetch();
  return (
    <React.Fragment>
      <ClayToolbar light>
        <ContainerFluidWithoutView>
          <ClayToolbar.Nav>
            <ClayToolbar.Item expand>
              <ClayToolbar.Input
                placeholder="Search..."
                sizing="sm"
                value={searchText}
                onChange={(event) => setSearchText(event.currentTarget.value)}
              />
            </ClayToolbar.Item>
            <ClayToolbar.Item>
              <ClayButtonWithIcon
                aria-label=""
                className={`${ClassNameButton} btn-sm`}
                symbol="plus"
                small
                onClick={() => onOpenChange(true)}
              />
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluidWithoutView>
      </ClayToolbar>
      <ContainerFluid>
        {(field(associatedListQuery.data)?.edges?.length ?? 0) !== 0 && <MainTitle title={label} />}
        <TableVirtuoso
          totalCount={field(associatedListQuery.data)?.edges?.length}
          scrollerRef={(element) => (scrollerRef.current = element as any)}
          style={{ height: "80vh" }}
          components={{
            Table: (props) => (
              <table
                {...props}
                style={{ ...props.style, tableLayout: "fixed" }}
                className="table table-hover show-quick-actions-on-Hover table-list"
              />
            ),
            TableBody: ClayTable.Body,
            TableHead: ClayTable.Head,
            TableRow: CustomTableRow,
            EmptyPlaceholder: () => (
              <tbody>
                <tr>
                  <td colSpan={field(associatedListQuery.data)?.edges?.length + 3} style={{ backgroundColor: "white" }}>
                    <EmptySpace description="There are no matching entities" title="No entities" extraClass="c-empty-state-animation" />
                  </td>
                </tr>
              </tbody>
            ),
          }}
          fixedHeaderContent={() => (
            <CustomTableRow>
              <CustomTableCell>{<span className="text-truncate">Field Name</span>}</CustomTableCell>
              <CustomTableCell>{<span className="text-truncate">Userfield</span>}</CustomTableCell>
              <CustomTableCell style={{ width: "56px" }} />
            </CustomTableRow>
          )}
          itemContent={(index) => {
            const row = JSON.parse(JSON.stringify(associatedListQuery.data))?.pluginDriver?.aclMappings[index] ?? undefined;
            return (
              <React.Fragment>
                <CustomTableCell>{row?.docTypeField?.name || "..."}</CustomTableCell>
                <CustomTableCell>
                  <EnumSelectSimple
                    dict={UserField}
                    disabled={false}
                    id={""}
                    label=""
                    onChange={(value) => {
                      remove(row?.docTypeField?.id, value);
                    }}
                    value={row?.userField ?? userField}
                    dimension={130}
                    validationMessages={[]}
                    key={""}
                  />
                </CustomTableCell>
                <CustomTableCell>
                  <TableRowActions
                    actions={[
                      {
                        label: "Remove Association",
                        icon: "trash",
                        onClick: () => {
                          if (row?.docTypeField?.id) {
                            removeMutate({ variables: { parentId, childId: row?.docTypeField?.id } });
                          }
                        },
                      },
                    ]}
                  />
                </CustomTableCell>
              </React.Fragment>
            );
          }}
          isScrolling={(isScrolling) => {
            if (scrollerRef.current) {
              if (isScrolling) {
                scrollerRef.current.style.pointerEvents = "none";
              } else {
                scrollerRef.current.style.pointerEvents = "auto";
              }
            }
          }}
        />
      </ContainerFluid>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>{label}</ClayModal.Header>
          <ClayModal.Body>
            <EnumSelectSimple
              dict={UserField}
              disabled={false}
              id={""}
              label="Select User Field"
              onChange={setUserField}
              value={userField}
              dimension={800}
              validationMessages={[]}
              key={""}
            ></EnumSelectSimple>
            <CustomFormGroup>
              <ClayInput
                type="search"
                placeholder="search"
                value={modalSearchText}
                onChange={(event) => setModalSearchText(event.currentTarget.value)}
              />
            </CustomFormGroup>
            {(unassociatedListQuery.data?.docTypeFields.edges?.length ?? 0) === 0 && !unassociatedListQuery.loading && (
              <EmptySpace
                description="There are no matching unassociated entities"
                title="No entities"
                extraClass="c-empty-state-animation"
              />
            )}
            <ClayList showQuickActionsOnHover>
              <div className="formContainer" style={{ width: "550px", height: "275px", overflowY: "scroll" }}>
                {
                  unassociatedListQuery.data?.docTypeFields.edges?.map((edge: any) => {
                    return (
                      <ClayList.Item flex key={edge?.node?.id}>
                        <ClayList.ItemField expand>
                          <ClayList.ItemTitle>{edge?.node?.name || "..."}</ClayList.ItemTitle>
                          <ClayList.ItemText>{edge?.node?.description || "..."}</ClayList.ItemText>
                        </ClayList.ItemField>
                        <ClayList.ItemField>
                          <ClayList.QuickActionMenu>
                            {canAct && (
                              <ClayList.QuickActionMenu.Item
                                onClick={() => {
                                  if (edge?.node?.id) {
                                    addMutate({ variables: { parentId, childId: edge.node.id, userField: userField } });
                                  }
                                }}
                                symbol="link"
                              />
                            )}
                          </ClayList.QuickActionMenu>
                        </ClayList.ItemField>
                      </ClayList.Item>
                    );
                  }) as any
                }
              </div>
            </ClayList>
          </ClayModal.Body>
        </ClayModal>
      )}
    </React.Fragment>
  );
}

export function CronInput(props: BaseInputProps<string>) {
  const { label, value, onChange, validationMessages, disabled, description } = props;
  const scheduling = useCompoundFormField({
    ...props,
    initialValues: {
      second: "0",
      minutes: "0",
      hours: "*",
      daysOfMonth: "?",
      month: "*",
      daysOfWeek: "*",
      year: "*",
    },
    serialize: React.useCallback(({ second, minutes, hours, daysOfMonth, month, daysOfWeek, year }) => {
      return `${second} ${minutes} ${hours} ${daysOfMonth} ${month} ${daysOfWeek} ${year}`;
    }, []),
    deserialize: React.useCallback((value) => {
      const [second, minutes, hours, daysOfMonth, month, daysOfWeek, year] = value.split(" ");
      return { second, minutes, hours, daysOfMonth, month, daysOfWeek, year };
    }, []),
  });
  if (value === "") onChange("0 0 * ? * * *");

  return (
    <React.Fragment>
      <div className="custom-panel panel" style={{ border: "1px solid #0000001a" }}>
        <div className="panel-heading" style={{ marginLeft: "20px", marginTop: "20px" }}>
          <div className="panel-title title">{label}</div>
        </div>
        <div className="panel-body">
          <div className="my-custom-panel-body">
            <fieldset disabled={disabled}>
              <CustomFormGroup>
                <div className="form-group-item">
                  <label>Preset</label>
                  {description && InformationField(description)}
                  <select value={value} onChange={(event) => onChange(event.currentTarget.value)} className="form-control">
                    <option value="">Custom</option>
                    <option value="0 */5 * ? * * *">Every 5 Minutes</option>
                    <option value="0 */30 * ? * * *">Every 30 Minutes</option>
                    <option value="0 0 * ? * * *">Every Hour</option>
                    <option value="0 0 12 * * ? *">Every Day at Midday</option>
                    <option value="0 0 0 * * ? *">Every Day at Midnight</option>
                  </select>
                </div>
              </CustomFormGroup>
              <CustomFormGroup className="form-group-autofit">
                <TextInput
                  item
                  label="Second"
                  {...scheduling.inputProps("second")}
                  value={typeof scheduling.inputProps("second").value !== "undefined" ? scheduling.inputProps("second").value : ""}
                />
                <TextInput
                  item
                  label="Minutes"
                  {...scheduling.inputProps("minutes")}
                  value={typeof scheduling.inputProps("minutes").value !== "undefined" ? scheduling.inputProps("minutes").value : ""}
                />
                <TextInput
                  item
                  label="Hours"
                  {...scheduling.inputProps("hours")}
                  value={typeof scheduling.inputProps("hours").value !== "undefined" ? scheduling.inputProps("hours").value : ""}
                />
                <TextInput
                  item
                  label="Days of Month"
                  {...scheduling.inputProps("daysOfMonth")}
                  value={
                    typeof scheduling.inputProps("daysOfMonth").value !== "undefined" ? scheduling.inputProps("daysOfMonth").value : ""
                  }
                />
                <TextInput
                  item
                  label="Month"
                  {...scheduling.inputProps("month")}
                  value={scheduling.inputProps("month").value !== "undefined" ? scheduling.inputProps("month").value : ""}
                />
                <TextInput
                  item
                  label="Days of Week"
                  {...scheduling.inputProps("daysOfWeek")}
                  value={typeof scheduling.inputProps("daysOfWeek").value !== "undefined" ? scheduling.inputProps("daysOfWeek").value : ""}
                />
                <TextInput
                  item
                  label="Year"
                  {...scheduling.inputProps("year")}
                  value={typeof scheduling.inputProps("year").value !== "undefined" ? scheduling.inputProps("year").value : ""}
                />
              </CustomFormGroup>
            </fieldset>
          </div>
          {validationMessages.length > 0 && (
            <div className="custom-panel-footer panel-footer has-warning">
              <CustomFeedbackGroup>
                {validationMessages.map((validationMessage, index) => {
                  return <CustomFeedbackGroup key={index}>{validationMessage}</CustomFeedbackGroup>;
                })}
              </CustomFeedbackGroup>
            </div>
          )}
        </div>
      </div>
    </React.Fragment>
  );
}

interface CustomPanelProps extends React.HTMLAttributes<HTMLDivElement> {
  displayTitle: string;
  displayType: string;
}

export const CustomPanel: React.FC<CustomPanelProps> = (props) => {
  const { displayTitle, displayType, children, className, ...rest } = props;

  const panelClassName = `custom-panel panel panel-${displayType} ${className || ""}`;

  return (
    <div className={panelClassName} {...rest}>
      {displayTitle && <div className="panel-heading">{displayTitle}</div>}
      <div className="panel-body">{children}</div>
    </div>
  );
};

interface CustomFeedbackGroupProps extends React.HTMLAttributes<HTMLDivElement> {
  className?: string;
}

export const CustomFeedbackGroup: React.FC<CustomFeedbackGroupProps> = (props) => {
  const { children, className, ...rest } = props;

  const combinedClassName = `form-feedback-group ${className || ""}`;

  return (
    <div {...rest} className={combinedClassName}>
      {children}
    </div>
  );
};

export type Filter = {
  title: string;
  description: string;
  Json: string;
  descriptionAttribute: string;
  visible: string;
};

export function CreateField({
  templates,
  templateChoice,
  setTemplateChoice,
}: {
  templates: { title: string; description: string; Json: string; descriptionAttribute: string; visible: string; multiselect?: string }[];
  templateChoice: KeyValue;
  setTemplateChoice: Dispatch<SetStateAction<KeyValue>>;
}) {
  const keyMultiselect: [string, any][] = templates.flatMap((template) => {
    if (template.multiselect) {
      const templateObj = JSON.parse(template.multiselect);
      return Object.entries(templateObj);
    }
    return [];
  });

  const createField = (key: string, value: any, description: string) => {
    // const indexSelect = keyMultiselect.findIndex((singleKey) => {
    //   return singleKey[0] === key;
    // });
    // if (indexSelect !== -1) {
    //   return (
    //     <div className="form-group-item" key={key}>
    //       <label id={key} style={{ paddingTop: "18px" }}>
    //         {key}
    //       </label>
    //       {InformationField(description)}
    //       <ClaySelect
    //         aria-label="Select Label"
    //         id="mySelectId"
    //         onChange={(event) => setTemplateChoice({ ...templateChoice, [key]: event.currentTarget.value })}
    //         defaultValue={templateChoice[key] || ""}
    //       >
    //         {keyMultiselect[indexSelect][1].map((item: any, index: number) => (
    //           <ClaySelect.Option key={"item" + index} label={item} value={item} />
    //         ))}
    //       </ClaySelect>
    //     </div>
    //   );
    // }
    switch (typeof value) {
      case "string":
        return (
          <TextInputSimple
            key={key}
            keyofF={key}
            description={description}
            value={value}
            isNumber={false}
            onChange={(event) => {
              setTemplateChoice({ ...templateChoice, [key]: event.currentTarget.value });
            }}
          />
        );
      case "number":
        return (
          <TextInputSimple
            key={key}
            keyofF={key}
            description={description}
            isNumber={true}
            value={value}
            onChange={(event) => {
              setTemplateChoice({ ...templateChoice, [key]: parseFloat(event.currentTarget.value) });
            }}
          />
        );
      case "boolean":
        return (
          <InputBooleanSimple
            key={key}
            keyofF={key}
            description={description}
            value={value}
            onChange={(event) => {
              setTemplateChoice({ ...templateChoice, [key]: !value });
            }}
          />
        );
      default:
        if (Array.isArray(value)) {
          return (
            <MultiSelectSimple
              key={key}
              keyofF={key}
              description={description}
              items={value.map((value: any) => ({ label: value, value }))}
              onItemchange={(value: Array<{ label?: string; value?: string }>) => {
                setTemplateChoice({ ...templateChoice, [key]: value.map(({ value }) => value!) });
              }}
            />
          );
        }
    }
  };

  return (
    <React.Fragment>
      {templates.map((template: { title: string; description: string; Json: string; descriptionAttribute: string; visible: string }) => {
        if (template.visible === "true") {
          const keysOfFields = Object.keys(JSON.parse(template.Json));

          const descriptionsFields = JSON.parse(template.descriptionAttribute);
          const fields = keysOfFields.reduce((acc: Array<JSX.Element>, key) => {
            if (key !== "type") {
              acc.push(createField(key, templateChoice?.[key], descriptionsFields[key]) || <div></div>);
            }
            return acc;
          }, []);
          return <div key={template.title}>{fields}</div>;
        }
        return null;
      })}
    </React.Fragment>
  );
}

export function CreateFieldDinamically({
  templates,
  templateChoice,
  setTemplateChoice,
}: {
  templates: { title: string; description: string; Json: string; descriptionAttribute: string; visible: string }[];
  templateChoice: KeyValue;
  setTemplateChoice: Dispatch<SetStateAction<KeyValue>>;
}) {
  const createField = (key: string, value: any, description: string) => {
    switch (typeof value) {
      case "string":
        return (
          <TextInputSimple
            key={key}
            keyofF={key}
            description={description}
            value={value}
            isNumber={false}
            onChange={(event) => {
              setTemplateChoice({ ...templateChoice, [key]: event.currentTarget.value });
            }}
          />
        );
      case "number":
        return (
          <TextInputSimple
            key={key}
            keyofF={key}
            description={description}
            isNumber={true}
            value={value}
            onChange={(event) => {
              setTemplateChoice({ ...templateChoice, [key]: parseFloat(event.currentTarget.value) });
            }}
          />
        );
      case "boolean":
        return (
          <InputBooleanSimple
            key={key}
            keyofF={key}
            description={description}
            value={value}
            onChange={(event) => {
              setTemplateChoice({ ...templateChoice, [key]: !value });
            }}
          />
        );
      default:
        if (Array.isArray(value)) {
          return (
            <MultiSelectSimple
              key={key}
              keyofF={key}
              description={description}
              items={value.map((value: any) => ({ label: value, value }))}
              onItemchange={(value: Array<{ label?: string; value?: string }>) => {
                setTemplateChoice({ ...templateChoice, [key]: value.map(({ value }) => value!) });
              }}
            />
          );
        }
    }
  };

  return (
    <React.Fragment>
      {templates.map((template: { title: string; description: string; Json: string; descriptionAttribute: string; visible: string }) => {
        if (template.visible === "true") {
          const keysOfFields = Object.keys(JSON.parse(template.Json));
          const descriptionsFields = JSON.parse(template.descriptionAttribute);
          const fields = keysOfFields.reduce((acc: Array<JSX.Element>, key) => {
            if (key !== "type") {
              acc.push(createField(key, templateChoice?.[key], descriptionsFields[key]) || <div></div>);
            }
            return acc;
          }, []);
          return <div key={template.title}>{fields}</div>;
        }
        return null;
      })}
    </React.Fragment>
  );
}

export interface Template {
  title: string;
  description: string;
  Json: string;
  descriptionAttribute: string;
  visible: string;
  multiselect?: string;
}

export interface InputField {
  id: string;
  value: string;
  multiselect?: string[];
}

export function TemplateQueryComponent({
  TemplateQueryParser,
  type,
  recoveryValue,
  inputFields,
  setInputFields,
  setType,
}: {
  TemplateQueryParser: Template[];
  type: string;
  recoveryValue: string;
  inputFields: InputField[];
  setInputFields: React.Dispatch<React.SetStateAction<InputField[]>>;
  setType: (value: string) => void;
}) {
  const recoveryvalueObject = JSON.parse(recoveryValue);
  const handleTitleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const title = event.target.value;
    setType(title);
  };

  React.useEffect(() => {
    if (type !== "") {
      const template = TemplateQueryParser.find((template) => template.title === type);
      if (template) {
        const jsonAttributes = JSON.parse(template.Json);
        const jsonMultiselect = template?.multiselect ? JSON.parse(template.multiselect) : "";
        const fields = Object.entries(jsonAttributes).map(([id, defaultValue]) => ({
          id,
          value: recoveryvalueObject[id] ? recoveryvalueObject[id] : "" + defaultValue,
          multiselect: jsonMultiselect.hasOwnProperty(id) ? jsonMultiselect[id] : null,
        }));
        setInputFields(fields);
      } else {
        setInputFields([]);
      }
    }
  }, [type]);

  return (
    <div>
      <div className="panelClass custom-panel panel panel-secondary" role="tablist">
        <div className="panel-header">
          <span className="panel-title">Type</span>
        </div>
        <div className="custom-panel-body panel-body">
          <CustomFormGroup>
            <div className="form-group-item">
              <select className="form-control" id="regularSelectElement" value={type} onChange={handleTitleChange}>
                {TemplateQueryParser.map((template) => (
                  <option key={template.title} value={template.title}>
                    {template.title}
                  </option>
                ))}
              </select>
            </div>
          </CustomFormGroup>
        </div>
      </div>
      {inputFields.map((field) => CreateFieldF({ field, setInputFields }))}
    </div>
  );
}
function CreateFieldF({
  field,
  setInputFields,
}: {
  field: InputField;
  setInputFields: React.Dispatch<React.SetStateAction<InputField[]>>;
}) {
  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const id = event.target.id;
    const value = event.target.value;
    setInputFields((prevFields) => prevFields.map((field) => (field.id === id ? { ...field, value } : field)));
  };

  if (field.multiselect) {
    return (
      <div key={field.id} className="form-group">
        <label htmlFor={field.id}>{field.id}</label>
        <select
          className="form-control"
          id="regularSelectElement"
          defaultValue={field.value}
          onChange={(event) => {
            const value = event.target.value;
            setInputFields((prevFields) => prevFields.map((fields) => (fields.id === field.id ? { ...fields, value } : fields)));
          }}
        >
          {field.multiselect.map((value: string, index: number) => (
            <option key={value + index} value={value}>
              {value}
            </option>
          ))}
        </select>
      </div>
    );
  }
  if (field.value === "true" || field.value === "false") {
    const value = field.value === "true" ? "false" : "true";
    return (
      <div style={{ display: "flex", gap: "5px" }} key={field.id}>
        <ClayToggle
          label="Checkbox"
          onToggle={() => {
            setInputFields((prevFields) => prevFields.map((fields) => (fields.id === field.id ? { ...fields, value } : fields)));
          }}
          toggled={field.value === "true" ? true : false}
        />
        <style type="text/css">{StyleToggle}</style>
      </div>
    );
  }
  return (
    <div key={field.id} className="form-group">
      <label htmlFor={field.id}>{field.id}</label>
      <input className="form-control" type="text" id={field.id} value={field.value} onChange={handleInputChange} />
    </div>
  );
}
export function TextInputSimple({
  keyofF,
  description,
  value,
  onChange,
  isNumber,
}: {
  keyofF: string;
  description: string;
  value: string | number;
  onChange(event: any): void;
  isNumber: boolean;
}) {
  return (
    <div className="form-group-item" key={keyofF + "div"}>
      <label id={keyofF + "label"} style={{ paddingTop: "18px" }}>
        {keyofF}
      </label>
      {InformationField(description)}
      <input type={isNumber ? "number" : "text"} id={keyofF + "input"} className="form-control" value={value} onChange={onChange}></input>
    </div>
  );
}

export function NumberInputSimple({
  keyofF,
  description,
  value,
  onChange,
}: {
  keyofF: string;
  description: string;
  value: number;
  onChange(event: any): void;
}) {
  return (
    <div className="form-group-item" key={keyofF + "div"}>
      <label id={keyofF + "label"} style={{ paddingTop: "18px" }}>
        {keyofF}
      </label>
      {InformationField(description)}
      <input type="number" id={keyofF + "input"} className="form-control" value={value} onChange={onChange}></input>
    </div>
  );
}

export function InputBooleanSimple({
  keyofF,
  description,
  value,
  onChange,
}: {
  keyofF: string;
  description: string;
  value: boolean;
  onChange(event: any): void;
}) {
  return (
    <div className="form-group" style={{ paddingTop: "18px" }} key={keyofF}>
      <style type="text/css">{StyleToggle}</style>
      <ClayToggle label={keyofF} id={keyofF} toggled={value} onToggle={onChange} />
      {InformationField(description)}
    </div>
  );
}

export function MultiSelectSimple({
  keyofF,
  description,
  items,
  onItemchange,
}: {
  keyofF: string;
  description: string;
  items: any[];
  onItemchange(event: any): void;
}) {
  const [value, setValue] = React.useState("");

  return (
    <div className="form-group-item" key={keyofF}>
      <label id={keyofF} style={{ paddingTop: "18px" }}>
        {keyofF}
      </label>
      {InformationField(description)}
      <ClayMultiSelect inputName="myInput" items={items} onChange={setValue} onItemsChange={onItemchange} value={value} />
    </div>
  );
}

export function MultiSelectForDinamicFields({
  id,
  setTitle,
  templates,
  onChangeDescription,
  templateChoice,
  setTemplateChoice,
}: {
  setTitle?: (value: string) => void;
  id: string;
  templates: any;
  onChangeDescription: any;
  templateChoice: KeyValue;
  setTemplateChoice: any;
}) {
  return (
    <React.Fragment>
      <div className="panelClass custom-panel panel panel-secondary" role="tablist">
        <div className="panel-header">
          <span className="panel-title">Type</span>
        </div>
        <div className="custom-panel-body panel-body">
          <CustomFormGroup>
            <div className="form-group-item">
              <select
                defaultValue={id === "new" ? "" : templateChoice.type}
                onChange={(event) => {
                  templates.map((element: any) => {
                    element.visible = "false";
                    if (element.title === event.currentTarget.value) {
                      element.visible = "true";
                      setTemplateChoice(JSON.parse(element.Json));
                      return true;
                    }
                  });
                  const dataSelect = templates.find((element: any) => element.title === event.currentTarget.value);
                  if (setTitle) {
                    setTitle(dataSelect.title);
                  }
                  onChangeDescription(dataSelect!.description);
                }}
                className="form-control"
              >
                {templates.map((filter: any, index: number) => (
                  <option key={index} label={filter.title} value={filter.title} />
                ))}
              </select>
            </div>
          </CustomFormGroup>
        </div>
      </div>
    </React.Fragment>
  );
}

interface CustomFormGroupProps extends React.HTMLAttributes<HTMLDivElement> {
  className?: string;
}

export const CustomFormGroup: React.FC<CustomFormGroupProps> = (props) => {
  const { children, className, ...rest } = props;

  const combinedClassName = `form-group ${className || ""}`;

  return (
    <div {...rest} className={combinedClassName}>
      {children}
    </div>
  );
};

export function MultiSelectForDinamicallyFieldsWithoutType({
  id,
  setTitle,
  type,
  setIsCustom,
  template,
  onChangeDescription,
  setTemplateChoice,
  onChangeType,
}: {
  id: string;
  setTitle?: (value: string) => void;
  type: string;
  setIsCustom: any;
  template: any;
  onChangeDescription: any;
  setTemplateChoice: any;
  onChangeType: any;
}) {
  return (
    <React.Fragment>
      <div className="panelClass custom-panel panel panel-secondary" role="tablist">
        <div className="panel-header">
          <span className="panel-title">Type</span>
        </div>
        <div className="custom-panel-body panel-body">
          <CustomFormGroup>
            <div className="form-group-item">
              <select
                defaultValue={id === "new" ? "" : type}
                onChange={(event) => {
                  if (event.currentTarget.value === "custom") {
                    setIsCustom(true);
                  } else {
                    setIsCustom(false);
                  }
                  onChangeType(event.currentTarget.value);
                  template.map((element: any) => {
                    element.visible = "false";
                    if (element.title === event.currentTarget.value) {
                      element.visible = "true";
                      setTemplateChoice(JSON.parse(element.Json));
                      return true;
                    }
                  });
                  const dataSelect = template.find((element: any) => element.title === event.currentTarget.value);
                  if (setTitle) setTitle(dataSelect.title);
                  onChangeDescription(dataSelect!.description);
                }}
                className="form-control"
              >
                {template.map((filter: any, index: number) => (
                  <option key={index} label={filter.title} value={filter.title} />
                ))}
              </select>
            </div>
          </CustomFormGroup>
        </div>
      </div>
    </React.Fragment>
  );
}

export function CreateDinamicallyFieldWithout({
  templates,
  templateChoice,
  setTemplateChoice,
}: {
  templates: any;
  templateChoice: any;
  setTemplateChoice: any;
}) {
  return (
    <React.Fragment>
      {templates.map((template: any) => {
        if (template.visible === "true") {
          const keysOfFields = Object.keys(JSON.parse(template.Json));
          const descriptionsFields = JSON.parse(template.descriptionAttribute);
          let fields: Array<any> = [];
          let i = 0;
          while (i < keysOfFields.length) {
            let t = i;
            if (keysOfFields[i] !== "type" && typeof templateChoice?.[keysOfFields[i]] === "string") {
              fields.push(
                <TextInputSimple
                  key={keysOfFields[i]}
                  keyofF={keysOfFields[i]}
                  isNumber={false}
                  description={descriptionsFields[keysOfFields[i]]}
                  value={templateChoice?.[keysOfFields[i]]}
                  onChange={(event) => {
                    setTemplateChoice({ ...templateChoice, [keysOfFields[t]]: event.currentTarget.value });
                  }}
                />
              );
            }
            if (typeof templateChoice?.[keysOfFields[i]] == "number") {
              fields.push(
                <TextInputSimple
                  key={keysOfFields[i]}
                  keyofF={keysOfFields[i]}
                  isNumber={true}
                  description={descriptionsFields[keysOfFields[i]]}
                  value={templateChoice?.[keysOfFields[i]]}
                  onChange={(event) => {
                    setTemplateChoice({ ...templateChoice, [keysOfFields[t]]: event.currentTarget.value });
                  }}
                />
              );
            }
            if (typeof templateChoice?.[keysOfFields[i]] == "boolean") {
              fields.push(
                <InputBooleanSimple
                  key={keysOfFields[i]}
                  keyofF={keysOfFields[i]}
                  description={descriptionsFields[keysOfFields[i]]}
                  value={templateChoice?.[keysOfFields[i]]}
                  onChange={(event) => {
                    setTemplateChoice({ ...templateChoice, [keysOfFields[t]]: !templateChoice?.[keysOfFields[t]] });
                  }}
                />
              );
            }
            i++;
          }
          return fields;
        }
      })}
    </React.Fragment>
  );
}

export const StyleToggle = `
  .toggle-switch-check:checked ~ .toggle-switch-bar::before {
    background-color: #FFFFFF;
    border-color: #C22525;
  }
  .toggle-switch-check ~ .toggle-switch-bar::before {
    background-color: #ECECEC;
    border-color: #ECECEC;
  }
  .toggle-switch-check:checked ~ .toggle-switch-bar::after {
    background-color: #C22525;
  }
  .toggle-switch-check ~ .toggle-switch-bar::after {
    background-color: #CBCBCB;
  }

  .toggle-switch-check:focus ~ .toggle-switch-bar::before {
    border-color: trasparent;
    box-shadow: 0 0 0 0px rgba(74, 144, 226, 0.5);
  }
`;

interface ParamsFormatString {
  howFormat?: FormatType[];
  words: string;
}

type FormatType = "uppercase" | "withoutSpace" | "upperFirstLetter";

const formatFunctions: { [key in FormatType]: (str: string) => string } = {
  uppercase: (str) => str.toUpperCase(),
  withoutSpace: (str) => str.replace(/\s/g, ""),
  upperFirstLetter: (str) => str.replace(/^\w/, (c) => c.toUpperCase()),
};

export function FormatString({ howFormat = ["uppercase", "withoutSpace", "upperFirstLetter"], words }: ParamsFormatString) {
  howFormat.forEach((singleFormat) => {
    const formatFn = formatFunctions[singleFormat];
    if (formatFn) {
      words = formatFn(words);
    }
  });
  return words;
}

type PropsSimpleModal = {
  observer: Observer;
  description?: string;
  labelContinue?: string;
  labelCancel?: string;
  actionContinue(): void;
  actionCancel(): void;
};

export function SimpleModal({ observer, labelContinue, labelCancel, actionContinue, actionCancel, description }: PropsSimpleModal) {
  return (
    <ClayModal observer={observer} size="sm" status="info" className="custom-modal" center style={{ background: "#f2f2f2" }}>
      <style>{styleModal}</style>
      <div style={{ background: "#ff000026" }}>
        <div style={{ background: "white", margin: "10px" }}>
          <div className="custom-modal__content">
            <div className="custom-modal__header">
              <BrandLogo width={70} height={70} colorFill={"#c22525"} />
              <p className="custom-modal__description">{description}</p>
            </div>
            <div className="custom-modal__footer">
              <ClayButton onClick={actionCancel} displayType="secondary" size="sm" className="custom-modal__cancel">
                {labelCancel}
              </ClayButton>
              <ClayButton onClick={actionContinue} displayType="secondary" size="sm" className="custom-modal__continue">
                {labelContinue}
              </ClayButton>
            </div>
          </div>
        </div>
      </div>
    </ClayModal>
  );
}

export function CreateGraphic({
  data,
  width = 600,
  height = 250,
  labelInformationRigth,
  Information,
}: {
  data: any;
  width: number;
  height: number;
  labelInformationRigth: string;
  Information: string;
}) {
  return (
    <ClayCard style={{ flex: "1", borderRadius: "10px" }}>
      <div style={{ display: "flex", flexDirection: "column", width: "100%" }}>
        <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between" }}>
          <label style={{ marginLeft: "20px", marginTop: "10px", fontSize: "28px" }}>{Information}</label>
          <label style={{ marginRight: "20px", color: "#9C0E10", cursor: "pointer", textDecoration: "underline" }}>
            {labelInformationRigth}
          </label>
        </div>
        <div>
          <label style={{ marginLeft: "20px", marginTop: "3px", color: "#71717A", fontSize: "18px", fontWeight: "400" }}>
            {"Last 7 days"}
          </label>
        </div>
      </div>
      <ClayCard.Body style={{ width: "100%" }}>
        <LineChart width={width} height={height} data={data}>
          <XAxis dataKey="name" />
          <YAxis tickCount={11} />
          <CartesianGrid stroke="#eee" strokeDasharray="5 5" />
          <Line type="monotone" dataKey="query" stroke="#C0272B" strokeWidth={2} dot={false} />
          <TooltipRecharts />
          <Legend />
        </LineChart>
      </ClayCard.Body>
    </ClayCard>
  );
}

interface CustomTableProps extends TableHTMLAttributes<HTMLTableElement> {
  style?: CSSProperties;
}

export const CustomTable: React.FC<CustomTableProps> = (props) => {
  const { children, style, ...rest } = props;

  return (
    <table {...rest} style={{ ...style }} className="table table-list">
      {children}
    </table>
  );
};

export function EmptySpace({ title, description, extraClass = "" }: { title: string; description: string; extraClass?: string }) {
  const classNames = `empty-state ${extraClass}`;
  return (
    <div className={classNames}>
      <h3 className="empty-state-title">{title}</h3>
      <div className="empty-state-description">{description}</div>
    </div>
  );
}

export function ContainerFluid({ children }: { children: React.ReactNode }) {
  return <div className="container-fluid container-view ">{children}</div>;
}

export function ContainerFluidWithoutView({ children }: { children: React.ReactNode }) {
  return <div className="container-fluid  ">{children}</div>;
}

export function DropDownCustom() {
  return (
    <div aria-labelledby="theDropdownToggleId" className="dropdown-menu">
      <ul className="list-unstyled">
        <li>
          <a className="active dropdown-item" href="#1">
            Selected Option
          </a>
        </li>
        <li>
          <a className="dropdown-item" href="#3">
            Normal Option
          </a>
        </li>
        <li>
          <a className="disabled dropdown-item" href="#4">
            Disabled Option
          </a>
        </li>
      </ul>
    </div>
  );
}

export function MultiSelectDynamicField({
  id,
  setTitle,
  templates,
  onChangeDescription,
  templateChoice,
  setTemplateChoice,
  form,
  excludeType,
  query,
  type,
}: {
  setTitle?: (value: string) => void;
  id: string;
  templates: any;
  onChangeDescription: any;
  templateChoice: KeyValue;
  setTemplateChoice: any;
  form: any;
  excludeType: boolean;
  query: QueryResult<
    AnalyzerQuery | TokenizerQuery,
    Exact<{
      id: string;
    }>
  >;
  type?: string;
}) {
  return (
    <React.Fragment>
      <div className="panelClass custom-panel panel panel-secondary" role="tablist">
        <div className="panel-header">
          <span className="panel-title">Type</span>
        </div>
        <div className="custom-panel-body panel-body">
          <CustomFormGroup>
            <div className="form-group-item">
              <select
                defaultValue={id === "new" ? "" : templateChoice.type}
                onChange={(event) => {
                  templates.map((element: any) => {
                    element.visible = "false";
                    if (element.title === event.currentTarget.value) {
                      element.visible = "true";
                      setTemplateChoice(JSON.parse(element.Json));
                      return true;
                    }
                  });
                  const dataSelect = templates.find((element: any) => element.title === event.currentTarget.value);
                  if (setTitle) {
                    setTitle(dataSelect.title);
                  }
                  onChangeDescription(dataSelect!.description);
                }}
                className="form-control"
              >
                {templates.map((filter: any, index: number) => (
                  <option key={index} label={filter.title} value={filter.title} />
                ))}
              </select>
            </div>
          </CustomFormGroup>
        </div>
      </div>
    </React.Fragment>
  );
}

export function LabelNumber({ label, number, unity, icon }: { label: string; number: number; unity?: string; icon?: React.ReactNode }) {
  return (
    <React.Fragment>
      <ClayCard style={{ maxHeight: "307px", maxWidth: "400px", flex: "1", borderRadius: "10px" }}>
        <div style={{ margin: "16px", height: "80px", display: "flex", flexDirection: "column", justifyContent: "center" }}>
          <div
            style={{
              fontSize: "24px",
              lineHeight: "32px",
              fontWeight: "600",
              fontFamily: "bold",
              display: "flex",
              gap: "10px",
              alignItems: "center",
            }}
          >
            {icon && (
              <div
                style={{
                  backgroundColor: "var(--openk9-embeddable-dashboard--secondary-color)",
                  padding: "8px",
                  display: "flex",
                  borderRadius: "100px",
                  color: "#9C0E10",
                  fontSize: "18px",
                }}
              >
                {icon}
              </div>
            )}
            <div>{label}:</div>
          </div>
          <div
            style={{
              color: "#c0272b",
              display: "flex",
              fontSize: "28px",
              lineHeight: "38.19px",
              fontWeight: "800",
              marginTop: "5px",
              fontFamily: "Nunito Sans",
            }}
          >{`${number} ${unity || ""}`}</div>
        </div>
      </ClayCard>
    </React.Fragment>
  );
}

export function CustomButtomClay({
  label,
  action,
  className,
  disabled = false,
  color,
  type = "button",
  ...rest
}: {
  label: React.ReactNode;
  action?: any;
  className?: string;
  disabled?: boolean;
  color?: string;
  type?: "submit" | "reset" | "button";
}) {
  const colorButton = color ? `btn ${color} ` : "btn btn-danger";
  const classes = className ? `${colorButton} ${className}` : ` ${colorButton}`;
  return (
    <button {...rest} disabled={disabled} className={classes} type={type} onClick={action}>
      {label}
    </button>
  );
}

const styleModal = `
    .custom-modal__content {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding:10px;
    }
    
    .custom-modal__header {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
    }
    
    .custom-modal__header svg {
      margin-right: 10px;
    }
    
    .custom-modal__description {
      margin: 0;
      margin-left: 15px;
      flex: 1;
      font-size: 20px;
      font-family: sans-serif;
    }

    .custom-modal__footer {
      display: flex;
      justify-content: flex-end;
      width: 100%;
      margin-right:10px;
    }
    
    .custom-modal__cancel {
      margin-right: 10px;
    }

    :focus-visible {
      outline: none;
  }
  `;
