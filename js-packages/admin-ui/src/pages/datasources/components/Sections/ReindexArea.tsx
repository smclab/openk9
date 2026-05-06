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
import { ModalConfirm } from "@components/Form";
import { AutocompleteDropdownWithOptions } from "@components/Form/Select/AutocompleteDropdown";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import WarningAmberIcon from "@mui/icons-material/WarningAmber";
import { Box, Button, Divider, Typography } from "@mui/material";
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
            title={showDialog.title || "Leave wizard?"}
            body={showDialog.message}
            labelConfirm="Leave and create"
            type="warning"
            actionConfirm={() => {
              showDialog.callbackConfirm();
            }}
            close={() => showDialog.callbackClose()}
          />
        )}
        <Box sx={{ width: "100%", maxWidth: 600 }}>
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
            sx={{ width: "100%" }}
          />
          <Divider sx={{ marginBlock: "20px" }} />
          <Box sx={{ display: "flex", flexDirection: "column", alignItems: "flex-start", gap: "8px" }}>
            <Typography variant="body2" color="text.primary">
              Can't find your Data Index? Go and create a new one.
            </Typography>
            <Button
              variant="outlined"
              color="info"
              disabled={isView}
              aria-label="Shortcut to create a new Data Index (leaves the current wizard)"
              endIcon={<OpenInNewIcon fontSize="small" />}
              onClick={() => {
                setShowDialog({
                  isShow: true,
                  message:
                    "You are about to leave the Datasource wizard to create a new Data Index. The data entered in the previous steps will be lost. Do you want to continue?",
                  title: "Leave wizard?",
                  callbackClose: () => {
                    setShowDialog(defaultModal);
                  },
                  callbackConfirm: () => {
                    navigate("/dataindex/new/mode/edit");
                    setShowDialog(defaultModal);
                  },
                });
              }}
              sx={{ textTransform: "none" }}
            >
              Create new Data Index
            </Button>
            <Box
              sx={{
                display: "flex",
                alignItems: "flex-start",
                gap: "6px",
                marginTop: "4px",
              }}
            >
              <WarningAmberIcon fontSize="small" color="warning" sx={{ marginTop: "2px" }} />
              <Typography variant="caption" color="text.secondary">
                Warning: creating a new Data Index will take you out of this page and the data entered in the previous
                steps will be lost.
              </Typography>
            </Box>
          </Box>
        </Box>
      </div>
      <Box
        sx={{
          marginTop: "10px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <Button
          variant="outlined"
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
            color="primary"
            size="large"
            aria-label="Recap"
            onClick={() => {
              setIsRecap(true);
            }}
            sx={{
              fontWeight: "bold",
              paddingInline: "32px",
              boxShadow: 3,
            }}
          >
            Recap
          </Button>
        )}
      </Box>
    </>
  );
}

