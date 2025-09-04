import { ModalConfirm } from "@components/Form";
import { AutocompleteDropdownWithOptions } from "@components/Form/Select/AutocompleteDropdown";
import { Box, Button } from "@mui/material";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { defaultModal } from "../../Function";
import { ConnectionData } from "../../types";

export default function ReindexArea({
  connectionData,
  setConnectionData,
  isView,
  isNew,
  setActiveTab,
  setIsRecap,
}: {
  connectionData: ConnectionData;
  setConnectionData: React.Dispatch<React.SetStateAction<ConnectionData>>;
  isView: boolean;
  isNew: boolean;
  setActiveTab: React.Dispatch<React.SetStateAction<string>>;
  setIsRecap: React.Dispatch<React.SetStateAction<boolean>>;
}) {
  const navigate = useNavigate();
  const [showDialog, setShowDialog] = useState(defaultModal);

  return (
    <>
      <div
        style={{
          display: "flex",
          marginTop: "30px",
          alignItems: "center",
          gap: "10px",
        }}
      >
        {showDialog.isShow && (
          <ModalConfirm
            title="Confirm Change"
            body={showDialog.message}
            labelConfirm="Change"
            actionConfirm={() => {
              showDialog.callbackConfirm();
            }}
            close={() => showDialog.callbackClose()}
          />
        )}
        <Box>
          <Box display={"flex"} flexDirection={"row"} gap={"10px"} alignItems={"end"}>
            <AutocompleteDropdownWithOptions
              label="Data Index"
              value={
                connectionData?.dataIndex?.id
                  ? { id: connectionData.dataIndex.id, name: connectionData.dataIndex.name ?? "" }
                  : undefined
              }
              onChange={(val) => {
                setConnectionData((prev: any) => ({
                  ...prev,
                  dataIndex: { id: val.id, name: val.name },
                }));
              }}
              onClear={() => {
                setConnectionData((prev: any) => ({
                  ...prev,
                  dataIndex: { id: "", name: "" },
                }));
              }}
              disabled={isView}
              optionsDefault={connectionData.optionDataindex.map((item: any) => ({ value: item.id, label: item.name }))}
              description="Select the data index to reindex your data into."
              sx={{ width: 400 }}
            />
            <Button
              variant="contained"
              color="info"
              disabled={isView}
              onClick={() => {
                setShowDialog({
                  isShow: true,
                  message: "Attention, to create new data index, you should leave this page",
                  title: "Area Data Index",
                  callbackClose: () => {
                    setShowDialog(defaultModal);
                  },
                  callbackConfirm: () => {
                    navigate("/dataindex/new/mode/edit");
                    setShowDialog(defaultModal);
                  },
                });
              }}
              sx={{ marginLeft: "auto", minHeight: "56px" }}
            >
              Create Data Index
            </Button>
          </Box>
        </Box>
      </div>
      <Box
        sx={{
          marginTop: "10px",
          display: "flex",
          justifyContent: "space-between",
        }}
      >
        <Button
          variant="contained"
          color="secondary"
          aria-label="Back"
          onClick={() => {
            setActiveTab("pipeline");
          }}
        >
          Back
        </Button>
        {!isView && (
          <Button
            variant="contained"
            aria-label="Recap"
            onClick={() => {
              setIsRecap(true);
            }}
          >
            Recap
          </Button>
        )}
      </Box>
    </>
  );
}
