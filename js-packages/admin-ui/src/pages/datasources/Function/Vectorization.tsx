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
import { CodeInput, ModalConfirmRadio } from "@components/Form";
import { Box, Button, FormControlLabel, Radio, RadioGroup, TextField, Typography } from "@mui/material";
import React from "react";
import { CustomForm } from "../components/Sections/DataSource/ConfigureDatasource";
import { BoxArea } from "../components/BoxArea";
import { ConnectionData } from "../types";

export const VectorizationModal = {
  isShow: false,
  message: "",
  title: "",
  callbackConfirm: () => {},
  callbackClose: () => {},
};

type vectorRadioType = "default" | "configuration";
type VectorizationType = {
  dataDatasource: ConnectionData;
  setDataDatasource: React.Dispatch<React.SetStateAction<ConnectionData>>;
  setActiveTab: (value: React.SetStateAction<string>) => void;
  disabled: boolean;
  isRecap: boolean;
  setIsRecap: React.Dispatch<React.SetStateAction<boolean>>;
  formCustom: CustomForm[] | undefined;
  callbackBack(): void;
};
// export function Vectorization({
//   dataDatasource,
//   setDataDatasource,
//   setActiveTab,
//   disabled,
//   isRecap,
//   setIsRecap,
//   formCustom,
//   callbackBack,
// }: VectorizationType) {
//   const [showDialog, setShowDialog] = React.useState(VectorizationModal);
//   const [areaState, setAreaState] = React.useState<{
//     radioVectorization: vectorRadioType | null;
//   }>({
//     radioVectorization: dataDatasource.vectorIndex ? "configuration" : "default",
//   });
//   const isEdit = !disabled && areaState.radioVectorization !== "configuration";

//   return (
//     <>
//       {showDialog.isShow && (
//         <ModalConfirmRadio
//           title="Change Configuration vectorization"
//           callbackConfirm={() => {
//             showDialog.callbackConfirm();
//           }}
//           message={showDialog.message}
//           callbackClose={() => {
//             showDialog.callbackClose();
//           }}
//         />
//       )}
//       <RadioGroup
//         value={areaState.radioVectorization}
//         onChange={(e) => {
//           setShowDialog({
//             isShow: true,
//             message:
//               "Sei sicuro di voler cambiare il radio? Perderesti tutte le altre modifiche precedentemente selezionate.",
//             title: "Vectorization",
//             callbackClose: () => {
//               setShowDialog(VectorizationModal);
//             },
//             callbackConfirm: () => {
//               setAreaState({
//                 radioVectorization: e.target.value as vectorRadioType,
//               });
//               setShowDialog(VectorizationModal);
//             },
//           });
//         }}
//       >
//         <FormControlLabel
//           value="default"
//           control={<Radio color={disabled ? "default" : "primary"} disabled={disabled} />}
//           label="Default"
//         />
//         <Box sx={{ display: "flex", flexWrap: "wrap" }}>
//           <BoxArea isActive={areaState.radioVectorization === "default"} sx={{ width: "100%" }}>
//             <Typography variant="body1">Not Configure vectorization</Typography>
//           </BoxArea>
//         </Box>

//         <FormControlLabel
//           value="configuration"
//           control={<Radio color={disabled ? "default" : "primary"} disabled={disabled} />}
//           label="Configure Vectorization"
//         />
//         <BoxArea
//           isActive={areaState.radioVectorization === "configuration"}
//           sx={{ display: "flex", flexDirection: "column", gap: "20px" }}
//         >
//           <TextField
//             name="Title"
//             label="Title"
//             disabled={disabled}
//             value={dataDatasource?.vectorIndex?.titleField}
//             onChange={(event) =>
//               setDataDatasource((pre) => ({
//                 ...pre,
//                 vectorIndex: {
//                   ...pre.vectorIndex,
//                   titleField: event?.currentTarget?.value,
//                 },
//               }))
//             }
//           />
//           <TextField
//             name="urlField"
//             label="Url Field"
//             disabled={disabled}
//             value={dataDatasource?.vectorIndex?.urlField}
//             onChange={(event) =>
//               setDataDatasource((pre) => ({
//                 ...pre,
//                 vectorIndex: {
//                   ...pre.vectorIndex,
//                   urlField: event?.currentTarget?.value,
//                 },
//               }))
//             }
//           />
//           <TextField
//             name="textEmbeddingField"
//             label="Text Embedding Field"
//             disabled={disabled}
//             value={dataDatasource?.vectorIndex?.textEmbeddingField}
//             onChange={(event) =>
//               setDataDatasource((pre) => ({
//                 ...pre,
//                 vectorIndex: {
//                   ...pre.vectorIndex,
//                   textEmbeddingField: event?.currentTarget?.value,
//                 },
//               }))
//             }
//           />
//           <TextField
//             name="metadataMapping"
//             label="Meta data mapping"
//             disabled={disabled}
//             value={dataDatasource?.vectorIndex?.metadataMapping}
//             onChange={(event) =>
//               setDataDatasource((pre) => ({
//                 ...pre,
//                 vectorIndex: {
//                   ...pre.vectorIndex,
//                   metadataMapping: event?.currentTarget?.value,
//                 },
//               }))
//             }
//           />
//           <TextField
//             name="chunkWindowSize"
//             label="Chunk window size"
//             disabled={disabled}
//             value={dataDatasource?.vectorIndex?.chunkWindowSize}
//             onChange={(event) => {
//               setDataDatasource((pre) => ({
//                 ...pre,
//                 vectorIndex: {
//                   ...pre.vectorIndex,
//                   chunkWindowSize: Number(event?.currentTarget?.value),
//                 },
//               }));
//             }}
//           />
//           <CodeInput
//             label="Chunk window size"
//             readonly={disabled || isEdit}
//             disabled={disabled}
//             value={dataDatasource?.vectorIndex?.jsonConfig || ""}
//             id="chunk-window-size-label"
//             language="json"
//             onChange={(e) => {
//               setDataDatasource((pre) => ({
//                 ...pre,
//                 vectorIndex: {
//                   ...pre.vectorIndex,
//                   jsonConfig: e,
//                 },
//               }));
//             }}
//             validationMessages={[]}
//           />
//         </BoxArea>
//       </RadioGroup>
//       <Box sx={{ display: "flex", justifyContent: "space-between" }}>
//         {
//           <Button
//             variant="contained"
//             onClick={() => {
//               if (isRecap) {
//                 setIsRecap(false);
//                 callbackBack();
//               } else setActiveTab("datasource");
//             }}
//           >
//             Back
//           </Button>
//         }
//         <Button
//           variant="contained"
//           onClick={() => {
//             setIsRecap(true);
//           }}
//         >
//           Recap
//         </Button>
//       </Box>
//     </>
//   );
// }

