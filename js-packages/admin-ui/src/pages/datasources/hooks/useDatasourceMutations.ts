/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
        const isNew = datasourceId === "new";
        toast({
          title: `Datasource ${isNew ? "Created" : "Updated"}`,
          content: `The datasource has been ${isNew ? "created" : "updated"} successfully.`,
          displayType: "success",
        });
        navigate("/data-sources", { replace: true });
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

