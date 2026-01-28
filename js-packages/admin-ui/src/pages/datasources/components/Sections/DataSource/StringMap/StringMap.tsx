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
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import SearchIcon from "@mui/icons-material/Search";
import { Box, Button, IconButton, Paper, Stack, TextField, Typography } from "@mui/material";
import React from "react";
import { DEFAULT_JSONPATH_TEST_TEXT, DEFAULT_REGEX_TEST_TEXT, DEFAULT_XPATH_TEST_TEXT } from "./DefaultJson";
import { ModalStringMap } from "./ModalStringMap";
import { testJsonPath, testXPath } from "./TestPath";
import { InformationField } from "@components/Form/utils/informationField";

export type Entry = { key: string; value: string; error?: boolean };
type StringMapInputProps = {
  defaultValue?: Entry[];
  label?: string;
  description?: string;
  onChange: (newMap: Entry[]) => void;
};
export function StringMapInput({ defaultValue, label, description, onChange }: StringMapInputProps) {
  const [entries, setEntries] = React.useState<Entry[]>([...(defaultValue ? defaultValue : [])]);

  const [testModalOpen, setTestModalOpen] = React.useState(false);
  const [testText, setTestText] = React.useState("");
  const [selectedIdx, setSelectedIdx] = React.useState<number | null>(null);
  const [testResult, setTestResult] = React.useState<null | boolean>(null);
  const [testMode, setTestMode] = React.useState<"regex" | "xpath" | "jsonpath">("regex");
  const [matchedLines, setMatchedLines] = React.useState<string[]>([]);
  const [testError, setTestError] = React.useState<string | null>(null);
  const [inputValueModal, setInputValueModal] = React.useState<string | null>(null);

  React.useEffect(() => {
    onChange(entries);
  }, [entries]);

  const handleAdd = () => {
    setEntries([...entries, { key: "", value: "" }]);
  };

  const handleRemove = (idx: number) => {
    setEntries(entries.filter((_, i) => i !== idx));
  };

  const handleChange = (idx: number, field: "key" | "value", val: string) => {
    setEntries((prev) => {
      const updated = [...prev];
      let error = false;
      if (field === "key") {
        error = !!updated.find((e, i) => i !== idx && e.key === val && val.trim() !== "");
      }
      updated[idx] = { ...updated[idx], [field]: val, error };
      return updated;
    });
  };

  const openTestModal = (idx: number, prefill: boolean) => {
    setSelectedIdx(idx);
    setTestResult(null);
    setMatchedLines([]);
    setTestError(null);

    const entry = entries[idx];
    if (entry?.key === "regex" || (testMode === "regex" && entry?.key !== "xpath")) {
      setTestMode("regex");
    } else if (entry?.key === "xpath" || testMode === "xpath") {
      setTestMode("xpath");
    } else if (entry?.key === "jsonpath" || testMode === "jsonpath") {
      setTestMode("jsonpath");
    }

    if (prefill) {
      setTestText(entry?.value || "");
    } else {
      setTestText("");
    }

    setTestModalOpen(true);
  };

  const handleTest = () => {
    if (selectedIdx === null) return;
    const value = entries[selectedIdx].value;

    try {
      if (testMode === "regex") {
        const regex = new RegExp(value);
        const lines = testText.split(/\r?\n/);
        const matched = lines.filter((line) => regex.test(line));
        setMatchedLines(matched);
        setTestResult(matched.length > 0);
        setTestError(null);
      } else if (testMode === "xpath") {
        const res = testXPath(testText, value);
        setMatchedLines(res.results || []);
        setTestResult(res.matched);
        setTestError(res.error || null);
      } else if (testMode === "jsonpath") {
        const res = testJsonPath(testText, value);
        setTestResult(res.matched);
        setMatchedLines(res.results || []);
        setTestError(res.error || null);
      }
    } catch (err) {
      console.error("General Test Error:", err);
      setTestResult(false);
      setMatchedLines([]);
      setTestError("An error occurred during testing. Please check your input.");
    }
  };

  React.useEffect(() => {
    if (!testModalOpen) return;
    if (testMode === "regex") {
      setTestText(DEFAULT_REGEX_TEST_TEXT);
    } else if (testMode === "xpath") {
      setTestText(DEFAULT_XPATH_TEST_TEXT);
    } else if (testMode === "jsonpath") {
      setTestText(DEFAULT_JSONPATH_TEST_TEXT);
    } else {
      setTestText("");
    }
  }, [testMode, testModalOpen]);

  return (
    <Paper elevation={3} sx={{ p: 2, position: "relative", boxShadow: "none" }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Box display={"flex"} alignItems="center" gap={1}>
          <Typography variant="subtitle1">{label}</Typography>
          {description && <InformationField description={description} />}
        </Box>
        <Box>
          <IconButton aria-label="Add" onClick={handleAdd} color="primary" size="small">
            <AddIcon fontSize="small" />
          </IconButton>
        </Box>
      </Box>
      <Stack spacing={1}>
        {entries.map((entry, idx) => (
          <Box key={idx} display="flex" alignItems="center" gap={1}>
            <TextField
              label="Key"
              value={entry.key}
              error={entry.error}
              helperText={entry.error ? "Duplicate key" : ""}
              size="small"
              onChange={(e) => handleChange(idx, "key", e.target.value)}
              sx={{ flex: 1 }}
            />
            <TextField
              label="Value"
              value={entry.value}
              size="small"
              onChange={(e) => handleChange(idx, "value", e.target.value)}
              sx={{ flex: 2 }}
            />
            <IconButton
              aria-label="Test"
              onClick={() => {
                openTestModal(idx, true);
                setInputValueModal(entry.value || "");
              }}
              color="primary"
              size="small"
            >
              <SearchIcon fontSize="small" />
            </IconButton>
            <IconButton aria-label="Remove" onClick={() => handleRemove(idx)} color="error" size="small">
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Box>
        ))}
      </Stack>
      {entries.length === 0 && (
        <Typography variant="subtitle1" color="text.secondary">
          No entries present. Click "Add" to insert the first entry.
        </Typography>
      )}

      {entries.length > 0 && (
        <Box display="flex" justifyContent="flex-end" mt={2}>
          <Button
            variant="contained"
            onClick={() => {
              setInputValueModal("");
              openTestModal(0, false);
            }}
            disabled={entries.length === 0 || entries.every((e) => !e.value)}
          >
            Test
          </Button>
        </Box>
      )}
      <ModalStringMap
        open={testModalOpen}
        onClose={() => setTestModalOpen(false)}
        value={inputValueModal || ""}
        setValue={setInputValueModal}
        testMode={testMode}
        setTestMode={setTestMode}
        selectedIdx={selectedIdx}
        entries={entries}
        testText={testText}
        setTestText={setTestText}
        testResult={testResult}
        setTestResult={setTestResult}
        matchedLines={matchedLines}
        setMatchedLines={setMatchedLines}
        handleTest={handleTest}
      />
    </Paper>
  );
}

