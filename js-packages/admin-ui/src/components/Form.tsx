import React, { Dispatch, SetStateAction } from "react";
import ClayForm, { ClayInput, ClaySelect, ClayToggle } from "@clayui/form";
import { MutationHookOptions, MutationTuple, QueryHookOptions, QueryResult } from "@apollo/client";
import useDebounced from "./useDebounced";
import ClayTable from "@clayui/table";
import ClayButton, { ClayButtonWithIcon } from "@clayui/button";
import ClayModal, { useModal } from "@clayui/modal";
import ClayList from "@clayui/list";
import ClayEmptyState from "@clayui/empty-state";
import { Virtuoso, Components as VirtuosoComponents, TableVirtuoso } from "react-virtuoso";
import ClayLayout from "@clayui/layout";
import ClayToolbar from "@clayui/toolbar";
import ClayPanel from "@clayui/panel";
import ClayMultiSelect from "@clayui/multi-select";
import ClayIcon from "@clayui/icon";
import { ClayTooltipProvider } from "@clayui/tooltip";
import { UserField } from "../graphql-generated";
import { TableRowActions } from "./Table";
import { ClassNameButton } from "../App";
import { Observer } from "@clayui/modal/lib/types";
import { BrandLogo } from "./BrandLogo";
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
      <ClayForm.Group>
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
      </ClayForm.Group>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>{label}</ClayModal.Header>
          <ClayModal.Body>
            <ClayForm.Group>
              <ClayInput
                type="search"
                placeholder="search"
                value={searchText}
                onChange={(event) => setSearchText(event.currentTarget.value)}
              />
            </ClayForm.Group>
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
        <ClayLayout.ContainerFluid>
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
        </ClayLayout.ContainerFluid>
      </ClayToolbar>

      <ClayLayout.ContainerFluid view>
        {(field(associatedListQuery.data)?.edges?.length ?? 0) !== 0 && <MainTitle title={label} />}
        {(field(associatedListQuery.data)?.edges?.length ?? 0) === 0 && !associatedListQuery.loading && (
          <ClayEmptyState description="There are no matching associated entities" title="No entities" className="c-empty-state-animation" />
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
      </ClayLayout.ContainerFluid>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>{label}</ClayModal.Header>
          <ClayModal.Body style={{ minHeight: "465px" }} scrollable={true}>
            <ClayForm.Group>
              <ClayInput
                type="search"
                placeholder="search"
                value={modalSearchText}
                onChange={(event) => setModalSearchText(event.currentTarget.value)}
              />
            </ClayForm.Group>
            {(field(unassociatedListQuery.data)?.edges?.length ?? 0) === 0 && !unassociatedListQuery.loading && (
              <ClayEmptyState
                description="There are no matching unassociated entities"
                title="No entities"
                className="c-empty-state-animation"
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
        <ClayLayout.ContainerFluid>
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
        </ClayLayout.ContainerFluid>
      </ClayToolbar>
      <ClayLayout.ContainerFluid view>
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
            TableRow: ClayTable.Row,
            EmptyPlaceholder: () => (
              <tbody>
                <tr>
                  <td colSpan={field(associatedListQuery.data)?.edges?.length + 3} style={{ backgroundColor: "white" }}>
                    <ClayEmptyState description="There are no matching entities" title="No entities" className="c-empty-state-animation" />
                  </td>
                </tr>
              </tbody>
            ),
          }}
          fixedHeaderContent={() => (
            <ClayTable.Row>
              <ClayTable.Cell headingCell headingTitle>
                {<span className="text-truncate">Field Name</span>}
              </ClayTable.Cell>
              <ClayTable.Cell headingCell headingTitle>
                {<span className="text-truncate">Userfield</span>}
              </ClayTable.Cell>
              <ClayTable.Cell headingCell style={{ width: "56px" }} />
            </ClayTable.Row>
          )}
          itemContent={(index) => {
            const row = JSON.parse(JSON.stringify(associatedListQuery.data))?.pluginDriver?.aclMappings[index] ?? undefined;
            return (
              <React.Fragment>
                <ClayTable.Cell>{row?.docTypeField?.name || "..."}</ClayTable.Cell>
                <ClayTable.Cell>
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
                </ClayTable.Cell>
                <ClayTable.Cell>
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
                </ClayTable.Cell>
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
      </ClayLayout.ContainerFluid>
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
            <ClayForm.Group>
              <ClayInput
                type="search"
                placeholder="search"
                value={modalSearchText}
                onChange={(event) => setModalSearchText(event.currentTarget.value)}
              />
            </ClayForm.Group>
            {(unassociatedListQuery.data?.docTypeFields.edges?.length ?? 0) === 0 && !unassociatedListQuery.loading && (
              <ClayEmptyState
                description="There are no matching unassociated entities"
                title="No entities"
                className="c-empty-state-animation"
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
      second: "",
      minutes: "",
      hours: "",
      daysOfMonth: "",
      month: "",
      daysOfWeek: "",
      year: "",
    },
    serialize: React.useCallback(({ second, minutes, hours, daysOfMonth, month, daysOfWeek, year }) => {
      return `${second} ${minutes} ${hours} ${daysOfMonth} ${month} ${daysOfWeek} ${year}`;
    }, []),
    deserialize: React.useCallback((value) => {
      const [second, minutes, hours, daysOfMonth, month, daysOfWeek, year] = value.split(" ");
      return { second, minutes, hours, daysOfMonth, month, daysOfWeek, year };
    }, []),
  });
  return (
    <React.Fragment>
      <ClayPanel displayTitle={label} displayType="secondary">
        <ClayPanel.Body>
          <fieldset disabled={disabled}>
            <ClayForm.Group>
              <div className="form-group-item">
                <label>Preset</label>
                {description && InformationField(description)}
                <ClaySelect value={value} onChange={(event) => onChange(event.currentTarget.value)}>
                  <ClaySelect.Option label="Custom" value="" />
                  <ClaySelect.Option label="Every 5 Minutes" value="0 */5 * ? * * *" />
                  <ClaySelect.Option label="Every 30 Minutes" value="0 */30 * ? * * *" />
                  <ClaySelect.Option label="Every Hour" value="0 0 * ? * * *" />
                  <ClaySelect.Option label="Every Day at Midday" value="0 0 12 * * ? *" />
                  <ClaySelect.Option label="Every Day at Midnight" value="0 0 0 * * ? *" />
                </ClaySelect>
              </div>
            </ClayForm.Group>
            <ClayForm.Group className="form-group-autofit">
              <TextInput item label="Second" {...scheduling.inputProps("second")} />
              <TextInput item label="Minutes" {...scheduling.inputProps("minutes")} />
              <TextInput item label="Hours" {...scheduling.inputProps("hours")} />
              <TextInput item label="Days of Month" {...scheduling.inputProps("daysOfMonth")} />
              <TextInput item label="Month" {...scheduling.inputProps("month")} />
              <TextInput item label="Days of Week" {...scheduling.inputProps("daysOfWeek")} />
              <TextInput item label="Year" {...scheduling.inputProps("year")} />
            </ClayForm.Group>
          </fieldset>
        </ClayPanel.Body>
        {validationMessages.length > 0 && (
          <ClayPanel.Footer className="has-warning">
            <ClayForm.FeedbackGroup>
              {validationMessages.map((validationMessage, index) => {
                return <ClayForm.FeedbackItem key={index}>{validationMessage}</ClayForm.FeedbackItem>;
              })}
            </ClayForm.FeedbackGroup>
          </ClayPanel.Footer>
        )}
      </ClayPanel>
    </React.Fragment>
  );
}

export type Filter = {
  title: string;
  description: string;
  Json: string;
  descriptionAttribute: string;
  visible: string;
};

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
      <ClayPanel displayTitle="Type" displayType="secondary">
        <ClayPanel.Body>
          <ClayForm.Group>
            <div className="form-group-item">
              <ClaySelect
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
              >
                {templates.map((filter: any, index: number) => (
                  <ClaySelect.Option key={index} label={filter.title} value={filter.title} />
                ))}
              </ClaySelect>
            </div>
          </ClayForm.Group>
        </ClayPanel.Body>
      </ClayPanel>
    </React.Fragment>
  );
}

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
      <ClayPanel displayTitle="Type" displayType="secondary">
        <ClayPanel.Body>
          <ClayForm.Group>
            <div className="form-group-item">
              <ClaySelect
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
              >
                {template.map((filter: any, index: number) => (
                  <ClaySelect.Option key={index} label={filter.title} value={filter.title} />
                ))}
              </ClaySelect>
            </div>
          </ClayForm.Group>
        </ClayPanel.Body>
      </ClayPanel>
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

export const StyleToggleDisabled = `
.toggle-switch-check:checked ~ .toggle-switch-bar::before {
  background-color: black;
  border-color: black;
}
.toggle-switch-check ~ .toggle-switch-bar::before {
  background-color: black;
  border-color: black;
}
.toggle-switch-check:checked ~ .toggle-switch-bar::after {
  background-color: black;
}
.toggle-switch-check ~ .toggle-switch-bar::after {
  background-color: black;
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
              <BrandLogo size={70} colorFill={"#c22525"} />
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
  `;
