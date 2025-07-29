import React from "react";
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography,
} from "@mui/material";

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
}) => (
  <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
    <DialogTitle>
      <Box display="flex" alignItems="center" justifyContent="space-between" gap={2}>
        <FormControl size="small" sx={{ minWidth: 120 }}>
          <InputLabel id="test-mode-label">Type</InputLabel>
          <Select
            labelId="test-mode-label"
            value={testMode}
            label="Type"
            onChange={(e) => setTestMode(e.target.value as any)}
          >
            <MenuItem value="regex">Regex</MenuItem>
            <MenuItem value="xpath">XPath</MenuItem>
            <MenuItem value="jsonpath">JsonPath</MenuItem>
          </Select>
        </FormControl>
        <TextField
          size="small"
          label={testMode.charAt(0).toUpperCase() + testMode.slice(1)}
          value={value}
          onChange={(e) => {
            setValue(e.target.value);
          }}
          sx={{ flex: 1, bgcolor: "#fff" }}
          placeholder={`Insert ${testMode} here...`}
        />
        <IconButton
          aria-label="Copy"
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
          Text to test
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
                    Results:
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
                      Nessun risultato trovato.
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
              Edit text
            </Button>
          </>
        ) : (
          <TextField
            multiline
            minRows={6}
            fullWidth
            value={testText}
            onChange={(e) => setTestText(e.target.value)}
            placeholder="Insert text here..."
            sx={{ bgcolor: "#fff" }}
          />
        )}
      </Box>
    </DialogContent>
    <DialogActions sx={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
      <Button onClick={onClose}>Close</Button>
      <Box sx={{ flex: 1, display: "flex", justifyContent: "flex-end" }}>
        <Button
          variant="contained"
          onClick={handleTest}
          disabled={selectedIdx === null || !entries[selectedIdx]?.value}
        >
          Test
        </Button>
      </Box>
    </DialogActions>
  </Dialog>
);
