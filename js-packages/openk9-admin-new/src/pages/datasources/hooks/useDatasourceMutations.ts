import { NavigateFunction } from "react-router-dom";
import { useModal, useToast } from "@components/Form";
import {
  useCreateDatasourceConnectionMutation,
  useUpdateDatasourceConnectionMutation,
} from "../../../graphql-generated";
import { DataSourceQuery } from "../gql";

export const useDatasourceMutations = (datasourceId: string, navigate: NavigateFunction) => {
  const toast = useToast();
  const modal = useModal();

  const [updateDatasource] = useUpdateDatasourceConnectionMutation({
    refetchQueries: ["DataSources", "EnrichPipelineOptions", DataSourceQuery],
    onCompleted() {
      navigate("/data-sources");
      modal({ title: "update datasource", displayType: "success" });
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
