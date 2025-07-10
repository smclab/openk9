import {
  BooleanInput,
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import { Box, Button, MenuItem, Select, Typography } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  PluginDriverType,
  Provisioning,
  useCreateOrUpdatePluginDriverMutation,
  usePluginDriverQuery,
  usePluginDriversQuery,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { ConfigType, PluginDriverQuery } from "./gql";
import { PluginDriversQuery } from "@pages/datasources/gql";

export const SavePluginnDriverModel = React.forwardRef(
  (
    {
      isConnector,
      customButtonModalStyle,
      onSubmitSuccess,
    }: {
      isConnector?: boolean;
      customButtonModalStyle?: any;
      onSubmitSuccess?: () => void;
    },
    ref: React.Ref<{ submit: () => void }>,
  ) => {
    const pluginDrivers = usePluginDriversQuery();
    const { pluginDriverId = "new", view } = useParams();
    const navigate = useNavigate();
    const { openConfirmModal, ConfirmModal } = useConfirmModal({
      title: "Edit Connector",
      body: "Are you sure you want to edit this Connector?",
      labelConfirm: "Edit",
    });

    const handleEditClick = async () => {
      const confirmed = await openConfirmModal();
      if (confirmed) {
        navigate(`/plugin-driver/${pluginDriverId}`);
      }
    };
    const [page, setPage] = React.useState(0);
    const pluginDriverQuery = usePluginDriverQuery({
      variables: { id: pluginDriverId as string },
      skip: !pluginDriverId || pluginDriverId === "new",
    });
    const toast = useToast();
    const [createOrUpdatePluginDriverMutate, createOrUpdatePluginDriverMutation] =
      useCreateOrUpdatePluginDriverMutation({
        refetchQueries: [PluginDriverQuery, PluginDriversQuery],
        onCompleted(data) {
          if (data.pluginDriver?.entity) {
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
              content: combineErrorMessages(data.pluginDriver?.fieldValidators),
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

    const form = useForm({
      initialValues: React.useMemo(
        () => ({
          name: "",
          description: "",
          type: PluginDriverType.Http,
          jsonConfig: "{}",
          provisioning: Provisioning.User,
        }),
        [],
      ),
      originalValues: {
        name: pluginDriverQuery.data?.pluginDriver?.name || "",
        type: pluginDriverQuery.data?.pluginDriver?.type,
        provisioning: Provisioning.User,
        description: pluginDriverQuery.data?.pluginDriver?.type || "",
        jsonConfig: pluginDriverQuery.data?.pluginDriver?.jsonConfig || "{}",
      },
      isLoading: pluginDriverQuery.loading || createOrUpdatePluginDriverMutation.loading,
      onSubmit(data) {
        createOrUpdatePluginDriverMutate({
          variables: {
            id: pluginDriverId !== "new" ? pluginDriverId : undefined,
            description: data.description,
            name: data.name,
            type: PluginDriverType.Http,
            provisioning: Provisioning.User,
            jsonConfig: JSON.stringify(config),
          },
        }).then(() => {
          if (isConnector && onSubmitSuccess) {
            onSubmitSuccess();
            pluginDrivers.refetch();
          }
        });
      },
      getValidationMessages: fromFieldValidators(
        createOrUpdatePluginDriverMutation.data?.pluginDriver?.fieldValidators,
      ),
    });

    const handleSubmit = () => {
      form.submit();
    };

    // Esponiamo handleSubmit attraverso la ref
    React.useImperativeHandle(ref, () => ({
      submit: () => {
        form.submit();
      },
    }));
    return (
      <ContainerFluid>
        <>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
            <TitleEntity
              nameEntity="Connector"
              description="Create or Edit a Connector and hook up a Openk9 connector."
              id={pluginDriverId}
            />
            {view === "view" && (
              <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
                Edit
              </Button>
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
        </>
        <ConfirmModal />
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
