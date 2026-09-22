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
import { useTranslation } from "react-i18next";

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
  const { t } = useTranslation();
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
            title={showDialog.title || t("pages.datasources.reindex.leave-wizard")}
            body={showDialog.message}
            labelConfirm={t("pages.datasources.reindex.leave-and-create")}
            type="warning"
            actionConfirm={() => {
              showDialog.callbackConfirm();
            }}
            close={() => showDialog.callbackClose()}
          />
        )}
        <Box sx={{ width: "100%", maxWidth: 600 }}>
          <AutocompleteDropdownWithOptions
            label={t("pages.datasources.reindex.data-index")}
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
            description={t("pages.datasources.reindex.select-the-data-index-to-reindex-your")}
            sx={{ width: "100%" }}
          />
          <Divider sx={{ marginBlock: "20px" }} />
          <Box sx={{ display: "flex", flexDirection: "column", alignItems: "flex-start", gap: "8px" }}>
            <Typography variant="body2" color="text.primary">
              {t("pages.datasources.reindex.cannot-find-data-index")}
            </Typography>
            <Button
              variant="outlined"
              color="info"
              disabled={isView}
              aria-label={t("pages.datasources.reindex.shortcut-to-create-a-new-data-index")}
              endIcon={<OpenInNewIcon fontSize="small" />}
              onClick={() => {
                setShowDialog({
                  isShow: true,
                  message:
                    "You are about to leave the Datasource wizard to create a new Data Index. The data entered in the previous steps will be lost. Do you want to continue?",
                  title: t("pages.datasources.reindex.leave-wizard"),
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
              {t("pages.datasources.reindex.create-new-data-index")}
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
          aria-label={t("common.back")}
          onClick={() => {
            setActiveTab("pipeline");
          }}
        >
          {t("common.back")}
        </Button>
        {!isView && (
          <Button
            variant="contained"
            color="primary"
            size="large"
            aria-label={t("common.recap")}
            onClick={() => {
              setIsRecap(true);
            }}
            sx={{
              fontWeight: "bold",
              paddingInline: "32px",
              boxShadow: 3,
            }}
          >
            {t("common.recap")}
          </Button>
        )}
      </Box>
    </>
  );
}

