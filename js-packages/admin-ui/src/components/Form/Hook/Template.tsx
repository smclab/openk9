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
import { Box, Button } from "@mui/material";
import { TemplateType, TemplateValue } from "@pages/Analyzer/gql";
import React, { useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { KeyValue } from "../utils";

const useTemplate = ({
  templateSelected,
  jsonConfig,
  type,
}: {
  templateSelected: TemplateType[];
  jsonConfig?: string | null | undefined;
  type?: string | null | undefined;
}) => {
  const [typeSelected, setTypeSelected] = useState<string | null>(null);
  const [template, setTemplate] = useState<TemplateType | null | undefined>(null);

  React.useEffect(() => {
    if (type && jsonConfig && (typeSelected === null || typeSelected === type)) {
      const defaultValue = templateSelected.find((template) => template.title === type);
      const jsonRecoveryData: KeyValue = jsonConfig ? JSON.parse(jsonConfig) : null;
      const singleElement = defaultValue?.value;
      const constructValue: TemplateValue[] = singleElement
        ? singleElement.map((element) => ({
            name: element.name,
            value: jsonRecoveryData[element.name],
            type: element.type,
            description: element.description,
            options: element.options,
          }))
        : [];

      const initialTemplate: TemplateType | undefined =
        (typeSelected === null || type === typeSelected) && jsonRecoveryData
          ? defaultValue && {
              title: defaultValue.title || "",
              description: defaultValue.description || "",
              type: defaultValue.type || "",
              value: constructValue,
            }
          : defaultValue;

      setTypeSelected(type);
      setTemplate(initialTemplate);
    }
  }, [type, jsonConfig]);

  const changeValueKey = useCallback(
    (key: string, value: string | number | Array<string> | boolean) => {
      setTemplate((pre: TemplateType | null | undefined) =>
        pre ? { ...pre, value: [...pre.value].map((el) => (el.name === key ? { ...el, value } : el)) } : pre,
      );
    },
    [setTemplate, template],
  );
  const changeType = useCallback(
    (type: string) => {
      setTypeSelected(type);
      const find = templateSelected.find((template) => template.title === type);
      setTemplate(find);
    },
    [setTemplate, templateSelected],
  );

  return { template, changeValueKey, changeType, typeSelected: typeSelected || "", setTypeSelected };
};

export default useTemplate;

export function createJsonString({
  template,
  type,
}: {
  template?: Array<TemplateValue> | null | undefined;
  type?: string | null | undefined;
}) {
  if (!template) {
    console.error("input error.");
    return "{}";
  }

  const result: Record<string, any> = {};
  if (type) {
    result.type = type;
  }
  template.forEach((temp) => {
    result[temp.name] = temp.value;
  });
  return JSON.stringify(result);
}

