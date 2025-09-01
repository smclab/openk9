import {
  CircularProgress,
  List,
  ListItem,
  ListItemButton,
  Typography,
  Paper,
  Divider,
  ListItemIcon,
} from "@mui/material";
import ClearIcon from "@mui/icons-material/Clear";
import React, { useEffect, useRef } from "react";

type Option = {
  value: string;
  label: string;
};

type Props = {
  options: Option[];
  highlightedIndex: number;
  loading: boolean;
  onSelect: (option: Option) => void;
  onScroll: (e: React.UIEvent<HTMLUListElement, UIEvent>) => void;
  clearValue?: string;
};

export function AutocompleteOptionsList({
  options,
  highlightedIndex,
  loading,
  onSelect,
  onScroll,
  clearValue = "__CLEAR__",
}: Props) {
  const itemRefs = useRef<(HTMLDivElement | null)[]>([]);

  useEffect(() => {
    const el = itemRefs.current[highlightedIndex];
    if (el) el.scrollIntoView({ block: "nearest" });
  }, [highlightedIndex]);

  return (
    <Paper
      sx={{
        position: "absolute",
        bottom: "100%",
        left: 0,
        right: 0,
        zIndex: 10,
        maxHeight: 240,
        overflowY: "auto",
        mb: "4px",
      }}
    >
      <List dense onScroll={onScroll} role="listbox" id="doc-type-listbox">
        {options.map((option, index) => {
          const isClear = option.value === clearValue;
          const content = (
            <ListItemButton
              id={`doc-type-option-${index}`}
              ref={(el) => (itemRefs.current[index] = el)}
              selected={index === highlightedIndex}
              role="option"
              aria-selected={index === highlightedIndex}
              onMouseDown={() => onSelect(option)}
              sx={isClear ? { color: "error.main", fontWeight: 700 } : undefined}
            >
              {isClear && (
                <ListItemIcon sx={{ minWidth: 28, color: "inherit" }}>
                  <ClearIcon fontSize="small" />
                </ListItemIcon>
              )}
              {option.label}
            </ListItemButton>
          );

          return (
            <React.Fragment key={`${option.value}-${index}`}>
              <ListItem disablePadding role="none">
                {content}
              </ListItem>
              {isClear && <Divider />}
            </React.Fragment>
          );
        })}

        {loading && (
          <ListItem>
            <CircularProgress size={20} />
          </ListItem>
        )}

        {options.length === 0 && !loading && (
          <ListItem>
            <Typography variant="body2" color="textSecondary">
              Nessun risultato
            </Typography>
          </ListItem>
        )}
      </List>
    </Paper>
  );
}
