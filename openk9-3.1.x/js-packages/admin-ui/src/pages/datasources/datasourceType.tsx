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
import { useNavigate } from "react-router-dom";
import { CustomForm, useGenerateDocumentTypesMutation } from "./Function";
import { Section } from "./components/Sections/Connectors/ConfigureConnectors";
import { ConnectionData } from "./types";
import { SetStateAction } from "react";

export type HeaderType = {
  landingTabId: string;
  mode: string;
  navigate: ReturnType<typeof useNavigate>;
  datasourceId: string;
  generateDocumentTypes: ReturnType<typeof useGenerateDocumentTypesMutation>;
  setActiveTab: React.Dispatch<SetStateAction<string>>;
};

export type tabsPropsConstructor = {
  tabs: tabsType;
  activeTab: string;
  handleTabChange: (event: React.SyntheticEvent | null, newValue: string) => void;
  setActiveTab: React.Dispatch<React.SetStateAction<string>>;
  areaEnabled: Section;
  formValues: ConnectionData;
  getHealthInfo: (id: number) => Promise<void>;
  getHealthInfoWithoutId: () => Promise<void>;
  isView: boolean;
  setAreaEnabled: React.Dispatch<React.SetStateAction<Section>>;
  setFormValues: React.Dispatch<React.SetStateAction<any>>;
  setShowDialog: React.Dispatch<React.SetStateAction<any>>;
  requestBody: any;
  formCustom: CustomForm[] | undefined;
  setFormCustom: React.Dispatch<React.SetStateAction<any>>;
  datasourceId: string;
  dynamicTemplate: any;
  changeValueTemplate: any;
  dynamicFormJson: any;
  loadingFormCustom: boolean;
  isRecap: boolean;
  setIsRecap: React.Dispatch<React.SetStateAction<boolean>>;
  handleDatasource: () => void;
  isCreated: boolean;
  setExtraFab: (fab: React.ReactNode | null) => void;
};

export type tabsType = (
  | {
      label: string;
      value: string;
      step: number;
      path: string;
      disabled: boolean;
    }
  | {
      label: string;
      value: string;
      path: string;
      step?: undefined;
      disabled?: undefined;
    }
)[];

