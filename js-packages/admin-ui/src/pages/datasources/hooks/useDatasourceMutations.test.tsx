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
import { MockedProvider, MockedResponse } from "@apollo/client/testing";
import { act, renderHook } from "@testing-library/react";
import React from "react";
import { beforeEach, describe, expect, test, vi } from "vitest";
import {
  CreateDatasourceConnectionDocument,
  CreateDatasourceConnectionMutationVariables,
  UpdateDatasourceConnectionDocument,
  UpdateDatasourceConnectionMutationVariables,
} from "../../../graphql-generated";
import { useDatasourceMutations } from "./useDatasourceMutations";

const { toast, modal } = vi.hoisted(() => ({ toast: vi.fn(), modal: vi.fn() }));

// the Form barrel never finishes loading in Vitest: keep only what the hook uses
vi.mock("@components/Form", async () => ({
  ...(await vi.importActual<typeof import("../../../components/Form/utils/fromFieldValidators")>(
    "../../../components/Form/utils/fromFieldValidators",
  )),
  useToast: () => toast,
  useModal: () => modal,
}));
vi.mock("react-i18next", () => ({ useTranslation: () => ({ t: (key: string) => key }) }));

const commonVariables = {
  name: "web",
  schedulable: false,
  scheduling: "0 */30 * ? * * *",
  jsonConfig: "{",
  reindexable: false,
  reindexing: "0 0 1 * * ?",
  purgeable: false,
  purging: "0 0 1 * * ?",
  purgeMaxAge: "2d",
};
const createVariables: CreateDatasourceConnectionMutationVariables = {
  ...commonVariables,
  pluginDriverId: 1,
  dataIndex: { name: "web-index", knnIndex: false },
};
const updateVariables: UpdateDatasourceConnectionMutationVariables = {
  ...commonVariables,
  datasourceId: "7",
  dataIndexId: "3",
};

// what the backend answers to a validation error: no GraphQL error, no entity
const refused = {
  __typename: "Response_Datasource",
  entity: null,
  fieldValidators: [{ __typename: "FieldValidator", field: "jsonConfig", message: "not a valid json" }],
};
const accepted = {
  __typename: "Response_Datasource",
  entity: { __typename: "Datasource", id: "7", name: "web" },
  fieldValidators: [],
};

function renderMutations(datasourceId: string, mock: MockedResponse) {
  const navigate = vi.fn();
  const onRefused = vi.fn();
  const { result } = renderHook(() => useDatasourceMutations(datasourceId, navigate, onRefused), {
    wrapper: ({ children }) => <MockedProvider mocks={[mock]}>{children}</MockedProvider>,
  });
  return { result, navigate, onRefused };
}

beforeEach(() => {
  vi.clearAllMocks();
});

describe("a datasource the backend refuses", () => {
  test("is not created: the validation message shows and the form stays", async () => {
    const { result, navigate, onRefused } = renderMutations("new", {
      request: { query: CreateDatasourceConnectionDocument, variables: createVariables },
      result: { data: { createDatasourceConnection: refused } },
    });

    await act(() => result.current.createDatasource({ variables: createVariables }));

    expect(toast).toHaveBeenCalledWith({
      title: "pages.datasources.error-creating-datasource",
      content: "not a valid json",
      displayType: "error",
    });
    expect(onRefused).toHaveBeenCalledOnce();
    expect(navigate).not.toHaveBeenCalled();
  });

  test("is not updated: the validation message shows and the form stays", async () => {
    const { result, navigate, onRefused } = renderMutations("7", {
      request: { query: UpdateDatasourceConnectionDocument, variables: updateVariables },
      result: { data: { updateDatasourceConnection: refused } },
    });

    await act(() => result.current.updateDatasource({ variables: updateVariables }));

    expect(toast).toHaveBeenCalledWith({
      title: "pages.datasources.error-updating-datasource",
      content: "not a valid json",
      displayType: "error",
    });
    expect(modal).not.toHaveBeenCalled();
    expect(onRefused).toHaveBeenCalledOnce();
    expect(navigate).not.toHaveBeenCalled();
  });
});

describe("a datasource the backend accepts", () => {
  test("is created: the success shows and the list opens", async () => {
    const { result, navigate, onRefused } = renderMutations("new", {
      request: { query: CreateDatasourceConnectionDocument, variables: createVariables },
      result: { data: { createDatasourceConnection: accepted } },
    });

    await act(() => result.current.createDatasource({ variables: createVariables }));

    expect(toast).toHaveBeenCalledWith({
      title: "pages.datasources.created-title",
      content: "pages.datasources.created-content",
      displayType: "success",
    });
    expect(navigate).toHaveBeenCalledWith("/data-sources", { replace: true });
    expect(onRefused).not.toHaveBeenCalled();
  });

  test("is updated: the success shows and the list opens", async () => {
    const { result, navigate, onRefused } = renderMutations("7", {
      request: { query: UpdateDatasourceConnectionDocument, variables: updateVariables },
      result: { data: { updateDatasourceConnection: accepted } },
    });

    await act(() => result.current.updateDatasource({ variables: updateVariables }));

    expect(modal).toHaveBeenCalledWith({ title: "pages.datasources.updated-title", displayType: "success" });
    expect(navigate).toHaveBeenCalledWith("/data-sources");
    expect(onRefused).not.toHaveBeenCalled();
  });
});
