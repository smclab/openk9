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
import { convertToBackEndFormatData, convertToInputFormat, DateTimeInput, ModalConfirmRadio } from "@components/Form";
import { Box, Button, FormControl, FormControlLabel, Radio, RadioGroup } from "@mui/material";
import React from "react";
import { useReindexMutation } from "../components/Sections/MonitoringTab";
import { BoxArea } from "../components/BoxArea";

type ReindexType = "reindex" | "partial-reindex";

export default function Reindex({ id, data }: { id: string; data: any }) {
  const [areaState, setAreaState] = React.useState<ReindexType>("reindex");
  const [modalHeaderButton, setModalHeaderButton] = React.useState<
    { label: string | null | undefined; action(): void } | null | undefined
  >(undefined);
  const [startData, setStartData] = React.useState(convertToInputFormat(data));
  const reindexMutation = useReindexMutation();

  return (
    <Box sx={{ display: "flex", flexDirection: "column", gap: "20px" }}>
      <RadioGroup
        value={areaState}
        onChange={(e) => {
          setModalHeaderButton({
            label: "Sicuro di voler cambiare la configurazione?",
            action: () => {
              setAreaState(e.target.value as ReindexType);
              setStartData(convertToInputFormat(data));
            },
          });
        }}
      >
        <FormControlLabel value="reindex" control={<Radio />} label="Full Reindex" />
        <FormControlLabel value="partial-reindex" control={<Radio />} label="Partial Reindex"></FormControlLabel>
        <Box sx={{ display: "flex", flexWrap: "wrap" }}>
          <BoxArea isActive={areaState === "partial-reindex"} sx={{ width: "100%" }}>
            <FormControl fullWidth>
              <DateTimeInput
                initialDateTime={startData}
                disabled={areaState !== "partial-reindex"}
                setDateTime={setStartData}
              />
            </FormControl>
          </BoxArea>
        </Box>
      </RadioGroup>
      <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
        <Button
          variant="contained"
          onClick={() => {
            setModalHeaderButton({
              label: " Are you sure you want to reindex it?",
              action: () => {
                reindexMutation.mutate({
                  datasourceId: id,
                  startIngestionDate: convertToBackEndFormatData(startData),
                  reindex: areaState === "reindex",
                });
              },
            });
          }}
        >
          Reindex
        </Button>
      </Box>
      {modalHeaderButton && (
        <ModalConfirmRadio
          title="change configuration"
          message="Are you sure to change Configuration?"
          callbackClose={() => {
            setModalHeaderButton(null);
          }}
          callbackConfirm={() => {
            modalHeaderButton.action();
            setModalHeaderButton(null);
          }}
        />
      )}
    </Box>
  );
}

