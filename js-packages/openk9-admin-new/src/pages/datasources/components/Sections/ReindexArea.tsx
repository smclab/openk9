import { Box, Button, CircularProgress, FormControl, MenuItem, Select } from "@mui/material";
import { ConnectionData } from "../../types";
import React, { useState } from "react";
import { useQuery } from "@apollo/client";
import { DataIndicesQuery } from "@pages/dataindices/gql";
import { useNavigate } from "react-router-dom";
import { defaultModal } from "../../Function";
import { ModalConfirm } from "@components/Form";

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
          <label>Data Index</label>
          <Box display={"flex"} flexDirection={"row"} gap={"10px"} alignItems={"center"}>
            <InfiniteScrollSelect
              connectionData={connectionData}
              isNew={isNew}
              isView={isView}
              setConnectionData={setConnectionData}
              defaultValue={{ id: connectionData?.dataIndex?.id || "", name: connectionData?.dataIndex?.name || "" }}
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
              sx={{ marginLeft: "auto" }}
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

type InfiniteScrollSelectProps = {
  connectionData: any;
  setConnectionData: React.Dispatch<React.SetStateAction<any>>;
  isView: boolean;
  isNew: boolean;
  defaultValue: {
    id: string;
    name: string;
  };
};
const InfiniteScrollSelect: React.FC<InfiniteScrollSelectProps> = ({
  connectionData,
  setConnectionData,
  isView,
  isNew,
  defaultValue,
}) => {
  const [options] = useState<Option[]>(connectionData.optionDataindex);

  return (
    <>
      <FormControl fullWidth>
        <Select
          value={connectionData?.dataIndex?.id || ""}
          sx={{ width: "100%", minWidth: "350px" }}
          disabled={isView}
          onChange={(event) => {
            const id = event.target.value;
            const name = options.find((item) => item.id === id)?.name || "";
            setConnectionData((prevData: any) => ({
              ...prevData,
              dataIndex: { id, name },
            }));
          }}
          displayEmpty
        >
          <MenuItem value={defaultValue.id}>{defaultValue.name}</MenuItem>
          {options.map(
            (item) =>
              defaultValue.id !== item.id && (
                <MenuItem key={item.id} value={item.id}>
                  {item.name}
                </MenuItem>
              ),
          )}
        </Select>
      </FormControl>
    </>
  );
};

type Option = {
  id: string;
  name: string;
};
