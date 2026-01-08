import {
  BooleanInput,
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  CustomSelectRelationsOneToOne,
  fromFieldValidators,
  ModalConfirm,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import DataCardManager from "@components/Form/Association/MultiLinkedAssociation/DataCardManager";
import { FieldDocType, SelectedValue } from "@components/Form/Association/MultiLinkedAssociation/types";
import CheckboxList from "@components/Form/List/CheckboxList";
import { useRestClient } from "@components/queryClient";
import FiberManualRecordIcon from "@mui/icons-material/FiberManualRecord";
import { Box, Button, MenuItem, Select, Typography } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  DocTypeUserDtoInput,
  InputMaybe,
  PluginDriverType,
  Provisioning,
  useDocumentTypeFieldsForPluginQuery,
  usePluginDriverQuery,
  usePluginDriversQuery,
  usePluginDriverWithDocTypeMutation,
  UserField,
} from "../../graphql-generated";
import { PluginDriverType as OpenApiPluginDriverType } from "../../openapi-generated/models/PluginDriverType";
import useOptions from "../../utils/getOptions";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { ConfigType } from "./gql";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export const aclOption: { value: UserField; label: UserField }[] = [
  { value: "EMAIL" as UserField, label: "EMAIL" as UserField },
  { value: "NAME" as UserField, label: "NAME" as UserField },
  { value: "NAME_SURNAME" as UserField, label: "NAMESURNAME" as UserField },
  { value: "ROLES" as UserField, label: "ROLES" as UserField },
  { value: "SURNAME" as UserField, label: "SURNAME" as UserField },
  { value: "USERNAME" as UserField, label: "USERNAME" as UserField },
];

export const SavePluginnDriverModel = React.forwardRef(
  (
    {
      isConnector,
      customButtonModalStyle,
      onSubmitSuccess,
      setExtraFab,
    }: {
      isConnector?: boolean;
      customButtonModalStyle?: any;
      onSubmitSuccess?: () => void;
      setExtraFab: (fab: React.ReactNode | null) => void;
    },
    ref: React.Ref<{ submit: () => void }>,
  ) => {
    const pluginDrivers = usePluginDriversQuery();
    const { pluginDriverId = "new", view } = useParams();
    const restClient = useRestClient();
    const navigate = useNavigate();
    const { openConfirmModal, ConfirmModal } = useConfirmModal({
      title: "Edit Connector",
      body: "Are you sure you want to edit this Connector?",
      labelConfirm: "Edit",
    });
    const [viewDeleteModal, setViewDeleteModal] = React.useState<{ view: boolean | undefined; id: number | undefined }>(
      {
        view: false,
        id: undefined,
      },
    );
    const { OptionQuery: userFieldsOptions } = useOptions({
      queryKeyPath: "docTypeFields.edges",
      useQuery: useDocumentTypeFieldsForPluginQuery,
      accessKey: "node",
    });

    const handleEditClick = async () => {
      const confirmed = await openConfirmModal();
      if (confirmed) {
        navigate(`/plugin-driver/${pluginDriverId}`);
      }
    };
    const [page, setPage] = React.useState(0);
    const isRecap = page === 1;
    const isNew = pluginDriverId === "new";
    const pluginDriverQuery = usePluginDriverQuery({
      variables: { id: pluginDriverId as string },
      skip: !pluginDriverId || pluginDriverId === "new",
    });

    const [selectedItems, setSelectedItems] = React.useState<SelectedValue[]>([]);
    const [fields, setFields] = React.useState<FieldDocType[]>([]);
    const [testResult, setTestResult] = React.useState<"success" | "error" | null>(null);

    React.useEffect(() => {
      const mappings = pluginDriverQuery.data?.pluginDriver?.aclMappings;
      if (!mappings) return;

      const initialFields: FieldDocType[] = mappings.map((field) => ({
        docTypeId: field?.docTypeField?.id ?? "",
        userField: field?.userField ?? "",
        userFieldId: field?.userField ?? "",
        fieldName: field?.docTypeField?.name ?? "",
      }));

      setFields(initialFields);
    }, [pluginDriverQuery.data?.pluginDriver?.aclMappings]);
    const isDuplicate = (newField: FieldDocType) => {
      return fields.some((field) => field.userField === newField.userField && field.fieldName === newField.fieldName);
    };

    const handleAddField = () => {
      const duplicates: { fieldName: string; userField: string }[] = [];
      const newFields = selectedItems
        .filter((item) => {
          const isDup = isDuplicate({
            fieldName: item.name,
            userField: form.inputProps("userFieldsSelectedOptions").value.name,
            docTypeId: item.id,
            userFieldId: form.inputProps("userFieldsSelectedOptions").value.id,
          });
          if (isDup) {
            duplicates.push({
              fieldName: item.name,
              userField: form.inputProps("userFieldsSelectedOptions").value.name,
            });
          }
          return !isDup;
        })
        .map((item) => ({
          fieldName: item.name,
          userField: form.inputProps("userFieldsSelectedOptions").value.name,
          docTypeId: item.id,
          userFieldId: form.inputProps("userFieldsSelectedOptions").value.id,
        }));

      setFields((prev) => [...prev, ...newFields]);

      if (duplicates.length > 0) {
        toast({
          title: "Duplicate associations",
          content: `The following associations could not be added because they already exist: ${duplicates
            .map((d) => `"${d.userField}" - "${d.fieldName}"`)
            .join(", ")}`,
          displayType: "warning",
        });
      }
      setSelectedItems([]);
    };

    const handleReset = () => {
      setSelectedItems([]);
    };

    const toast = useToast();
    const [pluginDriverWithDocType, pluginDriverWithDocTypeMutation] = usePluginDriverWithDocTypeMutation({
      refetchQueries: ["PluginDriver", "PluginDrivers", "DataSource"],
      onCompleted(data) {
        if (data.pluginDriverWithDocType?.entity) {
          const isNew = pluginDriverId === "new" ? "created" : "updated";
          toast({
            title: `Connector ${isNew}`,
            content: `Connector has been ${isNew} successfully`,
            displayType: "success",
          });
          !isConnector && navigate(`/plugin-drivers/`, { replace: true });
        } else {
          toast({
            title: `Error`,
            content: combineErrorMessages(data.pluginDriverWithDocType?.fieldValidators),
            displayType: "error",
          });
        }
      },
      onError(error) {
        console.log(error);
        const isNew = pluginDriverId === "new" ? "create" : "update";
        toast({
          title: `Error ${isNew}`,
          content: `Impossible to ${isNew} Connector`,
          displayType: "error",
        });
      },
    });

    const [config, setConfig] = React.useState<ConfigType | null>(null);

    React.useEffect(() => {
      if (pluginDriverQuery.data?.pluginDriver?.jsonConfig) {
        const parsedConfig = DesctructuringJsonConfig(pluginDriverQuery.data?.pluginDriver?.jsonConfig);
        setConfig(parsedConfig);
      }
    }, [pluginDriverQuery.data?.pluginDriver?.jsonConfig]);

    React.useEffect(() => {
      setTestResult(null);
    }, [config?.baseUri, config?.path, config?.method, config?.secure]);

    const form = useForm({
      initialValues: React.useMemo(
        () => ({
          name: "",
          description: "",
          type: PluginDriverType.Http,
          jsonConfig: "{}",
          provisioning: Provisioning.User,
          userFieldsSelectedOptions: { id: "", name: "" },
          docTypeFieldsSelectedOptions: { id: "", name: "" },
          docTypeUserDTOSet: [] as DocTypeUserDtoInput[],
        }),
        [],
      ),
      originalValues: {
        name: pluginDriverQuery.data?.pluginDriver?.name || "",
        type: pluginDriverQuery.data?.pluginDriver?.type,
        provisioning: Provisioning.User,
        description: pluginDriverQuery.data?.pluginDriver?.type || "",
        jsonConfig: pluginDriverQuery.data?.pluginDriver?.jsonConfig || "{}",
        docTypeUserDTOSet:
          pluginDriverQuery.data?.pluginDriver?.aclMappings?.map((field) => ({
            docTypeId: Number(field?.docTypeField?.id),
            userField: field?.userField as InputMaybe<UserField> | undefined,
          })) || [],
      },
      isLoading: pluginDriverQuery.loading || pluginDriverWithDocTypeMutation.loading,
      onSubmit(data) {
        pluginDriverWithDocType({
          variables: {
            id: pluginDriverId !== "new" ? pluginDriverId : undefined,
            description: data.description,
            name: data.name,
            type: PluginDriverType.Http,
            provisioning: Provisioning.User,
            jsonConfig: JSON.stringify(config),
            docTypeUserDTOSet:
              fields?.map((field) => ({
                docTypeId: Number(field.docTypeId),
                userField: field.userFieldId as InputMaybe<UserField> | undefined,
              })) || [],
          },
        }).then(() => {
          if (isConnector && onSubmitSuccess) {
            onSubmitSuccess();
            pluginDrivers.refetch();
          }
        });
      },
      getValidationMessages: fromFieldValidators(
        pluginDriverWithDocTypeMutation.data?.pluginDriverWithDocType?.fieldValidators,
      ),
    });

    const handleSubmit = () => {
      form.submit();
    };

    React.useImperativeHandle(ref, () => ({
      submit: () => {
        form.submit();
      },
    }));

    const recapSections = mappingCardRecap({
      form: form as any,
      sections: [
        {
          cell: [
            { key: "name" },
            { key: "description" },
            { key: "type" },
            { key: "jsonConfig", label: "JSON Config" },
            { key: "provisioning" },
            { key: "docTypeUserDTOSet", label: "Document Types User DTO Set" },
          ],
          label: "Recap Connector",
        },
      ],
    });

    return (
      <ContainerFluid size="md">
        <>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
            <TitleEntity
              nameEntity="Connector"
              description="Create or Edit a Connector and hook up a Openk9 connector."
              id={pluginDriverId}
            />
            {view === "view" && (
              <Box display={"flex"} gap={1}>
                <Button
                  variant="contained"
                  onClick={() => {
                    setViewDeleteModal({ id: Number(pluginDriverId) || 0, view: true });
                  }}
                  sx={{ height: "fit-content" }}
                >
                  Generate document types
                </Button>
                <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
                  Edit
                </Button>
              </Box>
            )}
          </Box>
          <form style={{ borderStyle: "unset", padding: "0 16px" }}>
            <CreateDataEntity
              id={pluginDriverId}
              form={form}
              page={page}
              pathBack="/plugin-drivers/"
              setPage={setPage}
              isFooterButton={!isConnector}
              haveConfirmButton={view ? false : true}
              informationSuggestion={[
                {
                  content: (
                    <Box>
                      <TextInput label="Name" {...form.inputProps("name")} />
                      <TextArea label="Description" {...form.inputProps("description")} />
                      <CustomSelect label="Type" dict={PluginDriverType} {...form.inputProps("type")} />
                      <TextInput
                        label="Base Uri"
                        value={config?.baseUri || ""}
                        validationMessages={[]}
                        onChange={(e) =>
                          setConfig((config) => (config ? { ...config, baseUri: e } : ({ baseUri: e } as ConfigType)))
                        }
                        id={pluginDriverId}
                        disabled={false}
                      />
                      <BooleanInput
                        label="Secure"
                        description="If the host is exposed is secure way or not"
                        id={pluginDriverId}
                        value={config?.secure ? true : false}
                        onChange={(e) =>
                          setConfig((config) => (config ? { ...config, secure: e } : ({ secure: e } as ConfigType)))
                        }
                        disabled={false}
                        validationMessages={[]}
                      />
                      <TextInput
                        label="Path"
                        description="Api call used to trigger data extraction"
                        id={pluginDriverId}
                        value={config?.path || ""}
                        onChange={(e) =>
                          setConfig((config) => (config ? { ...config, path: e } : ({ path: e } as ConfigType)))
                        }
                        disabled={false}
                        validationMessages={[]}
                      />
                      <Typography variant="subtitle1" component="label" htmlFor={pluginDriverId + "method"}>
                        {"Method"}
                      </Typography>
                      <Select
                        value={config?.method || ""}
                        onChange={(e) =>
                          setConfig((config) =>
                            config ? { ...config, method: e.target.value } : ({ method: e.target.value } as ConfigType),
                          )
                        }
                        id={pluginDriverId + "method"}
                        displayEmpty
                        fullWidth
                      >
                        <MenuItem value="GET">GET</MenuItem>
                        <MenuItem value="POST">POST</MenuItem>
                        <MenuItem value="PUT">PUT</MenuItem>
                        <MenuItem value="DELETE">DELETE</MenuItem>
                        <MenuItem value="PATCH">PATCH</MenuItem>
                      </Select>
                      <Box sx={{ display: "flex", marginBlock: 2, alignItems: "center" }}>
                        <Button
                          onClick={async () => {
                            try {
                              const res = await restClient.pluginDriverResource.postApiDatasourcePluginDriversHealth({
                                name: form.inputProps("name").value,
                                type: OpenApiPluginDriverType.HTTP,
                                jsonConfig: JSON.stringify(config),
                              });
                              setTestResult(res ? "success" : "error");
                            } catch {
                              setTestResult("error");
                            }
                          }}
                          variant="outlined"
                        >
                          Test Connector
                        </Button>
                      </Box>
                      <Box sx={{ mt: 1, display: "flex", alignItems: "center", gap: 1 }}>
                        {testResult === null && (
                          <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{ display: "flex", alignItems: "center", gap: 1 }}
                          >
                            <FiberManualRecordIcon sx={{ color: "text.secondary", fontSize: 18 }} />
                            Status: Waiting for test
                          </Typography>
                        )}
                        {testResult === "success" && (
                          <Typography
                            variant="body2"
                            color="success.main"
                            sx={{ display: "flex", alignItems: "center", gap: 1 }}
                          >
                            <FiberManualRecordIcon sx={{ color: "success.main", fontSize: 18 }} />
                            Connection successful
                          </Typography>
                        )}
                        {testResult === "error" && (
                          <Typography
                            variant="body2"
                            color="error.main"
                            sx={{ display: "flex", alignItems: "center", gap: 1 }}
                          >
                            <FiberManualRecordIcon sx={{ color: "error.main", fontSize: 18 }} />
                            Endpoint unreachable
                          </Typography>
                        )}
                      </Box>
                      {/* <ConnectorManager /> */}
                      <DataCardManager
                        options={userFieldsOptions}
                        config={{
                          title: "Associate Acl mappings",
                          description: "Associate user fields with document types to manage access control.",
                          addLabel: "Add",
                        }}
                        isCreateButtonVisible={page !== 1 && !view}
                        onAddField={handleAddField}
                        onReset={handleReset}
                        row={fields.map((field) => ({
                          itemLabel: field.userField,
                          itemLabelId: field.userFieldId,
                          associatedLabel: field.fieldName,
                          associatedLabelId: field.docTypeId,
                          actions: [
                            {
                              action(item, index) {
                                setFields((prev) =>
                                  prev.map((field, i) => {
                                    return i === index
                                      ? {
                                          ...field,
                                          ...(item?.itemLabel ? { userField: item.itemLabel } : {}),
                                          ...(item?.ItemId ? { userFieldId: item.ItemId } : {}),
                                          ...(item?.associatedLabel ? { fieldName: item.associatedLabel } : {}),
                                          ...(item?.associatedLabelId ? { docTypeId: item.associatedLabelId } : {}),
                                        }
                                      : field;
                                  }),
                                );
                              },
                              label: "Edit",
                            },
                            {
                              action(item, index) {
                                setFields((prev) => prev.filter((_, i) => i !== index));
                              },
                              label: "Delete",
                            },
                          ],
                        }))}
                      >
                        <Box sx={{ width: "100%", display: "grid", gridColumn: "span 2" }}>
                          <CustomSelectRelationsOneToOne
                            options={aclOption}
                            label="UserFieldsOptions"
                            onChange={(val) =>
                              form.inputProps("userFieldsSelectedOptions").onChange({ id: val.id, name: val.name })
                            }
                            value={{
                              id: form.inputProps("userFieldsSelectedOptions").value.id,
                              name: form.inputProps("userFieldsSelectedOptions").value.name || "",
                            }}
                            disabled={page === 1}
                          />
                          <CheckboxList
                            options={userFieldsOptions}
                            onChange={setSelectedItems}
                            selectedValues={selectedItems}
                          />
                        </Box>
                      </DataCardManager>
                    </Box>
                  ),
                  page: 0,
                  validation: view ? true : false,
                },
                {
                  validation: true,
                },
              ]}
              fieldsControll={["name"]}
            />
          </form>
          <ConfirmModal />
          <Recap
            recapData={recapSections}
            setExtraFab={setExtraFab}
            forceFullScreen={isRecap}
            actions={{
              onBack: () => setPage(0),
              onSubmit: () => form.submit(),
              submitLabel: isNew ? "Create entity" : "Update entity",
              backLabel: "Back",
            }}
          />
        </>
        {viewDeleteModal.view && (
          <ModalConfirm
            title="Generate document types"
            body="are you sure you want to regenerate the document types?"
            labelConfirm="Generate"
            actionConfirm={async () => {
              try {
                const result = await restClient.pluginDriverResource.postApiDatasourcePluginDriversDocumentTypes(
                  Number(pluginDriverId),
                );
                toast({
                  title: "Document types generation",
                  content: result ? "Document types generated successfully" : "Error generating document types",
                  displayType: result ? "success" : "error",
                });
              } catch (error) {
                toast({
                  title: "Document types generation",
                  content: "Error generating document types",
                  displayType: "error",
                });
              }
            }}
            close={() => setViewDeleteModal({ id: undefined, view: false })}
          />
        )}
      </ContainerFluid>
    );
  },
);

function DesctructuringJsonConfig(data: string) {
  try {
    const { baseUri, secure, path, method } = JSON.parse(data);
    return { baseUri, secure, path, method };
  } catch (error) {
    console.error("Invalid JSON string:", error);
    return null;
  }
}
