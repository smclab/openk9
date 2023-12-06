import React from "react";
import ClayForm, { ClayInput, ClayToggle } from "@clayui/form";
import { MutationHookOptions, MutationTuple, QueryHookOptions, QueryResult } from "@apollo/client";
import useDebounced from "./useDebounced";
import ClayButton, { ClayButtonWithIcon } from "@clayui/button";
import ClayModal, { useModal } from "@clayui/modal";
import ClayList from "@clayui/list";
import ClayEmptyState from "@clayui/empty-state";
import { Virtuoso, Components as VirtuosoComponents } from "react-virtuoso";
import ClayLayout from "@clayui/layout";
import ClayToolbar from "@clayui/toolbar";
import ClayMultiSelect from "@clayui/multi-select";
import ClayIcon from "@clayui/icon";
import { ClayTooltipProvider } from "@clayui/tooltip";

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
};

export function TextInput({
  id,
  label,
  value,
  onChange,
  disabled,
  validationMessages,
  ability,
  item,
}: BaseInputProps<string> & { item?: boolean; ability?: boolean }) {
  return (
    <div className={`${item ? "form-group-item" : "form-group"} ${validationMessages.length ? "has-warning" : ""}`}>
      <label htmlFor={id} style={{ color: ability === undefined || ability ? "black" : "#ECECEC" }}>
        {label}
      </label>
      <input
        type="text"
        className="form-control"
        id={id}
        value={value}
        onChange={(event) => onChange(event.currentTarget.value)}
        disabled={ability === undefined ? false : !ability}
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
`;
export function TextInputWithoutChange({ id, label, value, readOnly }: { id: string; label: string; value: string; readOnly: boolean }) {
  return (
    <div className="form-group">
      <label htmlFor={id}>{label}</label>
      <input
        type="text"
        className={"form-control"}
        id={id}
        value={value}
        readOnly={readOnly}
        style={readOnly ? { cursor: "not-allowed" } : {}}
      ></input>
    </div>
  );
}
export function TextArea({ id, label, value, onChange, disabled, validationMessages }: BaseInputProps<string>) {
  return (
    <div className={`form-group ${validationMessages.length ? "has-warning" : ""}`}>
      <label htmlFor={id}>{label}</label>
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
}: BaseInputProps<E[string]> & { dict: E }) {
  return (
    <div className={`form-group ${validationMessages.length ? "has-warning" : ""}`}>
      <label htmlFor={id}>{label}</label>
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

export function BooleanInput({ id, label, value, onChange, disabled, validationMessages }: BaseInputProps<boolean>) {
  return (
    <div className={`form-group ${validationMessages.length ? "has-warning" : ""}`}>
      <ClayToggle id={id} label={label} toggled={value} onToggle={onChange} disabled={disabled} />
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
}: {
  label: string;
  value: Value | null | undefined;
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
        <ClayInput.Group>
          <ClayInput.GroupItem>
            <ClayInput type="text" readOnly disabled={!value} value={valueQuery.data?.value?.name ?? ""} />
          </ClayInput.GroupItem>
          <ClayInput.GroupItem append shrink>
            <ClayButton.Group>
              <ClayButton displayType="secondary" onClick={() => onOpenChange(true)}>
                Change
              </ClayButton>
              <ClayButton
                displayType="secondary"
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
                Remove
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
  const searchTextDebounced = useDebounced(searchText);
  const associatedListQuery = useListQuery({ variables: { parentId, unassociated: false, searchText: searchTextDebounced } });
  const unassociatedListQuery = useListQuery({ variables: { parentId, unassociated: true, searchText: searchTextDebounced } });
  const [addMutate, addMutation] = useAddMutation({
    onCompleted() {
      associatedListQuery.refetch();
      unassociatedListQuery.refetch();
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
              <ClayButtonWithIcon aria-label="" symbol="plus" small onClick={() => onOpenChange(true)} />
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ClayLayout.ContainerFluid>
      </ClayToolbar>
      <ClayLayout.ContainerFluid view>
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
          <ClayModal.Body>
            <ClayForm.Group>
              <ClayInput
                type="search"
                placeholder="search"
                value={searchText}
                onChange={(event) => setSearchText(event.currentTarget.value)}
              />
            </ClayForm.Group>
            {(field(unassociatedListQuery.data)?.edges?.length ?? 0) === 0 && !unassociatedListQuery.loading && (
              <ClayEmptyState
                description="There are no matching unassociated entities"
                title="No entities"
                className="c-empty-state-animation"
              />
            )}
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
          </ClayModal.Body>
        </ClayModal>
      )}
    </React.Fragment>
  );
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

export function CronInput(props: BaseInputProps<string>) {
  const { label, value, onChange, validationMessages, disabled } = props;
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
    <div className="panelClass custom-panel panel panel-secondary" role="tablist">
      <div className="panel-header">
        <span className="panel-title">{label}</span>
      </div>
      <div className="custom-panel-body panel-body">
        <fieldset disabled={disabled}>
          <ClayForm.Group>
            <div className="form-group-item">
              <label>Preset</label>
              <select value={value} onChange={(event) => onChange(event.currentTarget.value)} className="form-control">
                <option label="Custom" value="" />
                <option label="Every 5 Minutes" value="* */5 0 ? * * *" />
                <option label="Every 30 Minutes" value="* */30 0 ? * * *" />
                <option label="Every Hour" value="* 0 * ? * * *" />
                <option label="Every Day at Midday" value="* 0 0 12 ? * * *" />
                <option label="Every Day at Midnight" value="* 0 0 0 ? * * *" />
              </select>
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
      </div>
      {validationMessages.length > 0 && (
        <div className="form-feedback-group">
          {validationMessages.map((validationMessage, index) => {
            return (
              <div key={index} className="form-feedback-item">
                {validationMessage}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export const ClassNameButton = "btn btn-danger";
