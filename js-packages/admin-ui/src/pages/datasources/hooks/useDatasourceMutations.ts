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
import { useTranslation } from "react-i18next";
import { NavigateFunction } from "react-router-dom";
import { useModal, useToast } from "@components/Form";
import {
  useCreateDatasourceConnectionMutation,
  useUpdateDatasourceConnectionMutation,
} from "../../../graphql-generated";
import { DataSourceQuery } from "../gql";

export const useDatasourceMutations = (datasourceId: string, navigate: NavigateFunction) => {
  const { t } = useTranslation();
  const toast = useToast();
  const modal = useModal();

  const [updateDatasource] = useUpdateDatasourceConnectionMutation({
    refetchQueries: ["DataSources", "EnrichPipelineOptions", DataSourceQuery],
    onCompleted() {
      navigate("/data-sources");
      modal({ title: t("pages.datasources.updated-title"), displayType: "success" });
    },
    onError() {
      modal({ title: t("pages.datasources.error-updating-datasource"), displayType: "error" });
    },
  });

  const [createDatasource] = useCreateDatasourceConnectionMutation({
    refetchQueries: ["DataSources", "EnrichPipelineOptions", DataSourceQuery],
    onCompleted(data) {
      if (data.createDatasourceConnection?.entity) {
        const isNewDatasource = datasourceId === "new";
        toast({
          title: isNewDatasource ? t("pages.datasources.created-title") : t("pages.datasources.updated-title"),
          content: isNewDatasource ? t("pages.datasources.created-content") : t("pages.datasources.updated-content"),
          displayType: "success",
        });
        navigate("/data-sources");
      }
    },
    onError() {
      const isNewDatasource = datasourceId === "new";
      toast({
        title: isNewDatasource
          ? t("pages.datasources.error-creating-datasource")
          : t("pages.datasources.error-updating-datasource"),
        content: t("common.generic-error"),
        displayType: "error",
      });
    },
  });

  return { updateDatasource, createDatasource };
};

