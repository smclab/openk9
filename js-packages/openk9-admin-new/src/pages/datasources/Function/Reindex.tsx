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
