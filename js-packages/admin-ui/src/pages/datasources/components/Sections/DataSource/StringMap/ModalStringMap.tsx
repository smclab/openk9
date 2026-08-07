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
import React from "react";
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  TextField,
  Typography,
} from "@mui/material";
import { AutocompleteDropdownWithOptions } from "@components/Form/Select/AutocompleteDropdown";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

const getTestModeOptions = (t: TFunction) => [
  { value: "regex", label: t("pages.datasources.string-map.regex") },
  { value: "xpath", label: t("pages.datasources.string-map.xpath") },
  { value: "jsonpath", label: t("pages.datasources.string-map.jsonpath") },
];

type ModalStringMapProps = {
  open: boolean;
  onClose: () => void;
  testMode: "regex" | "xpath" | "jsonpath";
  setTestMode: (mode: "regex" | "xpath" | "jsonpath") => void;
  selectedIdx: number | null;
  entries: { key: string; value: string }[];
  testText: string;
  setTestText: (text: string) => void;
  testResult: boolean | null;
  setTestResult: (res: boolean | null) => void;
  matchedLines: string[];
  setMatchedLines: (lines: string[]) => void;
  handleTest: () => void;
  value: string;
  setValue: (val: string) => void;
};

export const ModalStringMap: React.FC<ModalStringMapProps> = ({
  open,
  onClose,
  testMode,
  setTestMode,
  selectedIdx,
  entries,
  testText,
  setTestText,
  testResult,
  setTestResult,
  matchedLines,
  setMatchedLines,
  value,
  setValue,
  handleTest,
}) => {
  const { t } = useTranslation();
  const testModeOptions = getTestModeOptions(t);
  return (
  <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
    <DialogTitle>
      <Box display="flex" alignItems="center" justifyContent="space-between" gap={2}>
        <AutocompleteDropdownWithOptions
          label={t("common.type")}
          allowClear={false}
          optionsDefault={testModeOptions}
          value={{
            id: testMode,
            name: testModeOptions.find((o) => o.value === testMode)?.label || testMode,
          }}
          onChange={(val) => setTestMode(val.id as "regex" | "xpath" | "jsonpath")}
          sx={{ minWidth: 160 }}
        />
        <TextField
          size="small"
          label={testMode.charAt(0).toUpperCase() + testMode.slice(1)}
          value={value}
          onChange={(e) => {
            setValue(e.target.value);
          }}
          sx={{ flex: 1 }}
          placeholder={t("pages.datasources.string-map.insert-here", { mode: testMode })}
        />
        <IconButton
          aria-label={t("common.copy")}
          onClick={() => {
            if (selectedIdx !== null) {
              navigator.clipboard.writeText(value || "");
            }
          }}
          size="small"
          sx={{ ml: 1 }}
        >
          <svg width="20" height="20" fill="none" viewBox="0 0 24 24">
            <rect x="9" y="9" width="13" height="13" rx="2" fill="#888" />
            <rect x="3" y="3" width="13" height="13" rx="2" stroke="#888" strokeWidth="2" fill="none" />
          </svg>
        </IconButton>
      </Box>
    </DialogTitle>
    <DialogContent sx={{ display: "flex", gap: 2 }}>
      <Box flex={2}>
        <Typography variant="subtitle2" gutterBottom>
          {t("pages.datasources.string-map.text-to-test")}
        </Typography>
        {testResult !== null ? (
          <>
            <Box
              sx={{
                bgcolor: "#f1f5f9",
                p: 2,
                borderRadius: 1,
                maxHeight: 300,
                overflowY: "auto",
                fontFamily: "monospace",
                whiteSpace: "pre-line",
              }}
            >
              {testMode === "regex" ? (
                testText.split(/\r?\n/).map((line, idx) => {
                  const isMatch = matchedLines.includes(line);
                  return (
                    <Typography
                      key={idx}
                      variant="body2"
                      sx={{
                        color: isMatch ? "success.main" : "error.main",
                        fontWeight: isMatch ? 700 : 400,
                        background: isMatch ? "#e6ffed" : "#ffeaea",
                        px: 1,
                        borderRadius: 0.5,
                        mb: 0.5,
                        display: "block",
                      }}
                    >
                      {line || <span style={{ opacity: 0.5 }}>(empty)</span>}
                    </Typography>
                  );
                })
              ) : (
                <Box>
                  <Typography variant="subtitle2" sx={{ mb: 1 }}>
                    {t("pages.datasources.string-map.results")}
                  </Typography>
                  {matchedLines.length > 0 ? (
                    matchedLines.map((res, idx) => (
                      <Box
                        key={idx}
                        sx={{
                          color: "success.main",
                          background: "#e6ffed",
                          px: 1,
                          borderRadius: 0.5,
                          mb: 1,
                          display: "block",
                          fontFamily: "monospace",
                          fontSize: "0.95em",
                          whiteSpace: "pre-wrap",
                          wordBreak: "break-word",
                        }}
                      >
                        {(() => {
                          try {
                            const parsed = JSON.parse(res);
                            return <pre style={{ margin: 0 }}>{JSON.stringify(parsed, null, 2)}</pre>;
                          } catch {
                            return res;
                          }
                        })()}
                      </Box>
                    ))
                  ) : (
                    <Typography variant="body2" sx={{ color: "error.main" }}>
                      {t("pages.datasources.string-map.no-results")}
                    </Typography>
                  )}
                </Box>
              )}
            </Box>
            <Button
              variant="outlined"
              size="small"
              sx={{ mt: 2 }}
              onClick={() => {
                setTestResult(null);
                setMatchedLines([]);
              }}
            >
              {t("pages.datasources.string-map.edit-text")}
            </Button>
          </>
        ) : (
          <TextField
            multiline
            minRows={6}
            fullWidth
            value={testText}
            onChange={(e) => setTestText(e.target.value)}
            placeholder={t("pages.datasources.string-map.insert-text-here")}
          />
        )}
      </Box>
    </DialogContent>
    <DialogActions sx={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
      <Button onClick={onClose}>{t("common.close")}</Button>
      <Box sx={{ flex: 1, display: "flex", justifyContent: "flex-end" }}>
        <Button
          variant="contained"
          onClick={handleTest}
          disabled={selectedIdx === null || !entries[selectedIdx]?.value}
        >
          {t("common.test")}
        </Button>
      </Box>
    </DialogActions>
  </Dialog>
  );
};
