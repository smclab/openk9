import { NavigateFunction } from "react-router-dom";
import { combineErrorMessages, useModal, useToast } from "@components/Form";
import {
  useCreateDatasourceConnectionMutation,
  useUpdateDatasourceConnectionMutation,
} from "../../../graphql-generated";
import { DataSourceQuery } from "../gql";

export const useDatasourceMutations = (datasourceId: string, navigate: NavigateFunction, onRefused: () => void) => {
  const toast = useToast();
  const modal = useModal();

  const [updateDatasource] = useUpdateDatasourceConnectionMutation({
    refetchQueries: ["DataSources", "EnrichPipelineOptions", DataSourceQuery],
    onCompleted(data) {
      if (data.updateDatasourceConnection?.entity) {
        navigate("/data-sources");
        modal({ title: "update datasource", displayType: "success" });
      } else {
        toast({
          title: "Error Updating Datasource",
          content:
            combineErrorMessages(data.updateDatasourceConnection?.fieldValidators) ||
            "An error occurred while updating the datasource",
          displayType: "error",
        });
        onRefused();
      }
    },
    onError() {
      modal({ title: "errror update datasource", displayType: "error" });
    },
  });

  const [createDatasource] = useCreateDatasourceConnectionMutation({
    refetchQueries: ["DataSources", "EnrichPipelineOptions", DataSourceQuery],
    onCompleted(data) {
      if (data.createDatasourceConnection?.entity) {
        const isNew = datasourceId === "new" ? "created" : "updated";
        toast({
          title: `Datasource ${isNew}`,
          content: `Datasource has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate("/data-sources");
      } else {
        toast({
          title: "Error Creating Datasource",
          content:
            combineErrorMessages(data.createDatasourceConnection?.fieldValidators) ||
            "An error occurred while creating the datasource",
          displayType: "error",
        });
        onRefused();
      }
    },
    onError(error) {
      const isNew = datasourceId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Datasource`,
        displayType: "error",
      });
    },
  });

  return { updateDatasource, createDatasource };
};
