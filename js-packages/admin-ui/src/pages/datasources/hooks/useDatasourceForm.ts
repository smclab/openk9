import { QueryResult } from "@apollo/client";
import { DataSourceQuery, Exact, InputMaybe, Provisioning, Scalars } from "../../../graphql-generated";
import { useEffect, useState } from "react";
import { parseCronString } from "../Function";
import { ConnectionData } from "../types";

export const initializeConnectionData = (datasourceId: string | undefined | null): ConnectionData => ({
  datasourceId: datasourceId || "new",
  name: "",
  optionDataindex: [],
  description: "",
  dataIndices: [],
  dataIndex: {},
  enrichPipeline: { id: "", name: "" },
  jsonConfig: "",
  lastIngestionDate: null,
  schedulable: false,
  scheduling: "",
  schedulingPreset: { label: "", value: "" },
  schedulingMinute: "",
  schedulingHour: "",
  schedulingDayOfMonth: "",
  schedulingDayOfWeek: "",
  schedulingMonth: "",
  isCronSectionreindex: false,
  isCronSectionpurge: false,
  isCronSectionscheduling: false,
  startAtCreation: false,
  cronExpression: "",
  linkedEnrichItems: [],
  pipeline: {},
  pluginDriverSelect: { id: null },
  bodyTag: "",
  maxLenght: "",
  pageCount: "",
  titleCount: "",
  titleTag: "",
  documentTypeExtension: "",
  enrichPipelineCustom: { id: "", name: "", linkedEnrichItems: [] },
  vectorIndex: {
    textEmbeddingField: "",
    titleField: "",
    urlField: "",
    chunkWindowSize: 0,
    chunkType: null,
    embeddingDocTypeFieldId: { id: "", name: "" },
    embeddingJsonConfig: "",
    docTypeIds: [],
  },
});

export const useDatasourceForm = (
  datasourceId: string,
  datasourceQuery: QueryResult<
    DataSourceQuery,
    Exact<{
      id: Scalars["ID"];
      searchText?: InputMaybe<Scalars["String"]>;
    }>
  >,
) => {
  const [formValues, setFormValues] = useState<ConnectionData>(initializeConnectionData(datasourceId));

  useEffect(() => {
    if (datasourceQuery.data?.datasource) {
      const parsedScheduling = parseCronString(datasourceQuery?.data?.datasource?.scheduling || "");

      const parsedReindexing = parseCronString(datasourceQuery?.data?.datasource?.reindexing || "");
      const parsedPurge = parseCronString(datasourceQuery?.data?.datasource?.purging || "");
      const purgeMaxAge = datasourceQuery?.data?.datasource?.purgeMaxAge || "2d";

      const { day = null, hour = null, minute = null, months = null } = parsedScheduling || {};
      const {
        day: dayReindex = null,
        hour: hourReindex = null,
        minute: minuteReindex = null,
        months: monthsReindex = null,
      } = parsedReindexing || {};

      const {
        day: dayPurge = null,
        hour: hourPurge = null,
        minute: minutePurge = null,
        months: monthsPurge = null,
      } = parsedPurge || {};

      setFormValues((prevValues) => ({
        ...prevValues,
        ...datasourceQuery?.data?.datasource,
        optionDataindex:
          datasourceQuery?.data?.datasource?.dataIndexes?.edges?.map((dat) => ({
            id: dat?.node?.id || "",
            name: dat?.node?.name || "",
          })) || [],
        scheduling: datasourceQuery?.data?.datasource?.scheduling,
        schedulingDayOfMonth: day,
        schedulingHour: hour,
        schedulingMinute: minute,
        schedulingMonth: months,
        reindexDayOfMonth: dayReindex,
        reindexHour: hourReindex,
        reindexMinute: minuteReindex,
        reindexMonth: monthsReindex,
        purgeDayOfMonth: dayPurge,
        purgeHour: hourPurge,
        purgeMinute: minutePurge,
        purgeMonth: monthsPurge,
        purgeMaxAge: purgeMaxAge,
        isCronSectionpurge: datasourceQuery?.data?.datasource?.purgeable,
        purging: datasourceQuery?.data?.datasource?.purging,
        isCronSectionreindex: datasourceQuery?.data?.datasource?.reindexable,
        isCronSectionscheduling: datasourceQuery?.data?.datasource?.schedulable,
        dataIndex: {
          id: datasourceQuery?.data?.datasource?.dataIndex?.id || "",
          name: datasourceQuery?.data?.datasource?.dataIndex?.name || "",
          knnIndex: datasourceQuery?.data?.datasource?.dataIndex?.knnIndex || false,
        },
        enrichPipeline: {
          id: datasourceQuery?.data?.datasource?.enrichPipeline?.id || "",
          name: datasourceQuery?.data?.datasource?.enrichPipeline?.name,
        },
        pluginDriverSelect: {
          ...prevValues?.pluginDriverSelect,
          ...datasourceQuery?.data?.datasource?.pluginDriver,
          json: datasourceQuery?.data?.datasource?.pluginDriver?.jsonConfig || "",
          provisioning: datasourceQuery?.data?.datasource?.pluginDriver?.provisioning || Provisioning.System,
          nameConnectors: datasourceQuery?.data?.datasource?.pluginDriver?.name || "",
        },
      }));
    }
  }, [datasourceQuery.data, datasourceId]);

  return { formValues, setFormValues };
};
