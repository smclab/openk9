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
import {
  CircularProgress,
  List,
  ListItem,
  ListItemButton,
  Typography,
  Paper,
  Divider,
  ListItemIcon,
  useTheme,
} from "@mui/material";
import ClearIcon from "@mui/icons-material/Clear";
import React, { useEffect, useRef } from "react";

type Option = { value: string; label: string };

type Props = {
  options: Option[];
  highlightedIndex: number;
  loading: boolean;
  onSelect: (option: Option) => void;
  onScroll?: (e: React.UIEvent<HTMLUListElement, UIEvent>) => void;
  clearValue?: string;
  viewClear?: boolean;
};

export function AutocompleteOptionsList({
  options,
  highlightedIndex,
  loading,
  onSelect,
  onScroll,
  clearValue = "__CLEAR__",
  viewClear = true,
}: Props) {
  const theme = useTheme();
  const clearColor = theme.palette.mode === "light" ? theme.palette.error.light : theme.palette.error.main;
  const clearHoverColor = theme.palette.error.main;

  const itemRefs = useRef<(HTMLDivElement | null)[]>([]);

  useEffect(() => {
    itemRefs.current = [];
  }, [options]);

  useEffect(() => {
    const el = itemRefs.current[highlightedIndex];
    if (el) el.scrollIntoView({ block: "nearest" });
  }, [highlightedIndex]);

  const filteredOptions = !viewClear ? options.filter((option) => option.value !== clearValue) : options;

  return (
    <Paper
      sx={{
        position: "absolute",
        left: 0,
        right: 0,
        zIndex: 10,
        mb: "4px",
      }}
      role="presentation"
    >
      <List dense onScroll={onScroll} role="listbox" id="doc-type-listbox" sx={{ maxHeight: 240, overflowY: "auto" }}>
        {filteredOptions.map((option, index) => {
          const isClear = option.value === clearValue;

          const handlePointerDown: React.PointerEventHandler<HTMLDivElement> = (e) => {
            e.preventDefault();
            onSelect(option);
          };

          const handleMouseDown: React.MouseEventHandler<HTMLDivElement> = (e) => {
            e.preventDefault();
            onSelect(option);
          };

          return (
            <React.Fragment key={`${option.value}-${index}`}>
              <ListItem disablePadding role="none">
                <ListItemButton
                  id={`doc-type-option-${index}`}
                  ref={(el) => (itemRefs.current[index] = el)}
                  selected={index === highlightedIndex}
                  role="option"
                  aria-selected={index === highlightedIndex}
                  onPointerDown={handlePointerDown}
                  onMouseDown={handleMouseDown}
                  sx={
                    isClear
                      ? {
                          color: clearColor,
                          fontWeight: 700,
                          "&:hover": {
                            color: clearHoverColor,
                            textDecoration: "underline",
                            backgroundColor: theme.palette.action.hover,
                          },
                        }
                      : undefined
                  }
                >
                  {isClear && (
                    <ListItemIcon sx={{ minWidth: 28, color: "inherit" }}>
                      <ClearIcon fontSize="small" />
                    </ListItemIcon>
                  )}
                  {option.label}
                </ListItemButton>
              </ListItem>
            </React.Fragment>
          );
        })}

        {loading && (
          <ListItem role="status" aria-live="polite">
            <CircularProgress size={20} />
          </ListItem>
        )}

        {options.length === 0 && !loading && (
          <ListItem role="status" aria-live="polite">
            <Typography variant="body2" color="textSecondary">
              No Options
            </Typography>
          </ListItem>
        )}
      </List>
    </Paper>
  );
}

type PropsString = {
  options: string[];
  highlightedIndex: number;
  loading: boolean;
  onSelect: (option: string) => void;
  onScroll?: (e: React.UIEvent<HTMLUListElement, UIEvent>) => void;
  clearValue?: string;
  viewClear?: boolean;
};

export function AutocompleteOptionsListString({
  options,
  highlightedIndex,
  loading,
  onSelect,
  onScroll,
  clearValue = "__CLEAR__",
  viewClear = true,
}: PropsString) {
  const itemRefs = useRef<(HTMLDivElement | null)[]>([]);
  const theme = useTheme();
  const clearColor = theme.palette.mode === "light" ? theme.palette.error.light : theme.palette.error.main;
  const clearHoverColor = theme.palette.error.main;
  useEffect(() => {
    itemRefs.current = [];
  }, [options]);

  useEffect(() => {
    const el = itemRefs.current[highlightedIndex];
    if (el) el.scrollIntoView({ block: "nearest" });
  }, [highlightedIndex]);

  return (
    <Paper
      sx={{
        position: "absolute",
        left: 0,
        right: 0,
        zIndex: 10,
        mb: "4px",
      }}
      role="presentation"
    >
      <List dense onScroll={onScroll} role="listbox" id="doc-type-listbox" sx={{ maxHeight: 240, overflowY: "auto" }}>
        {options.map((option, index) => {
          const isClear = option === clearValue;

          const handlePointerDown: React.PointerEventHandler<HTMLDivElement> = (e) => {
            e.preventDefault();
            onSelect(option);
          };

          const handleMouseDown: React.MouseEventHandler<HTMLDivElement> = (e) => {
            e.preventDefault();
            onSelect(option);
          };

          return (
            <React.Fragment key={`${option}-${index}`}>
              <ListItem disablePadding role="none">
                <ListItemButton
                  id={`doc-type-option-${index}`}
                  ref={(el) => (itemRefs.current[index] = el)}
                  selected={index === highlightedIndex}
                  role="option"
                  aria-selected={index === highlightedIndex}
                  onPointerDown={handlePointerDown}
                  onMouseDown={handleMouseDown}
                  sx={
                    isClear
                      ? {
                          color: clearColor,
                          fontWeight: 700,
                          "&:hover": {
                            color: clearHoverColor,
                            textDecoration: "underline",
                            backgroundColor: theme.palette.action.hover,
                          },
                        }
                      : undefined
                  }
                >
                  {isClear && viewClear && (
                    <ListItemIcon sx={{ minWidth: 28, color: "inherit" }}>
                      <ClearIcon fontSize="small" />
                    </ListItemIcon>
                  )}
                  {option}
                </ListItemButton>
              </ListItem>
              {isClear && <Divider />}
            </React.Fragment>
          );
        })}

        {loading && (
          <ListItem role="status" aria-live="polite">
            <CircularProgress size={20} />
          </ListItem>
        )}

        {options.length === 0 && !loading && (
          <ListItem role="status" aria-live="polite">
            <Typography variant="body2" color="textSecondary">
              No Options
            </Typography>
          </ListItem>
        )}
      </List>
    </Paper>
  );
}

