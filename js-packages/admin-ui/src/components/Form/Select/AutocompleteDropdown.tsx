import useDebounced from "@components/common/useDebounced";
import { Box, SxProps, TextField, Theme, Typography } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { UseOptionsHook } from "utils/RelationOneToOne";
import { InformationField } from "../utils/informationField";
import { AutocompleteOptionsList } from "./AutocompleteOptionsList";

export type Option = { value: string; label: string };
export type SelectedValue = { id: string; name: string };
export type UseOptionsResult = {
  options: Option[];
  loading: boolean;
  hasNextPage?: boolean;
  loadMore?: () => Promise<void>;
};

type Props = {
  onChange: (value: SelectedValue) => void;
  onClear?: () => void;
  allowClear?: boolean;
  clearLabel?: string;
  label: string;
  value?: SelectedValue;
  description?: string;
  disabled?: boolean;
  useOptions: UseOptionsHook;
  sx?: SxProps<Theme>;
  extraVariables?: Record<string, any>;
};

type PropsWithOptions = {
  onChange: (value: SelectedValue) => void;
  onClear?: () => void;
  allowClear?: boolean;
  clearLabel?: string;
  label: string;
  value?: SelectedValue;
  description?: string;
  disabled?: boolean;
  optionsDefault: Option[];
  sx?: SxProps<Theme>;
};

export function AutocompleteDropdown({
  onChange,
  onClear,
  allowClear = true,
  clearLabel = "Clear selection",
  label,
  value,
  disabled,
  description,
  useOptions,
  extraVariables = {},
  sx,
}: Props) {
  const [open, setOpen] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const [loadingMore, setLoadingMore] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [inputValue, setInputValue] = useState("");
  const justClearedRef = useRef(false);

  const debouncedText = useDebounced(inputValue, 300);

  const { options, loading, hasNextPage, loadMore } = useOptions(debouncedText, { ...extraVariables });

  const CLEAR_OPTION: Option = useMemo(() => ({ value: "__CLEAR__", label: clearLabel }), [clearLabel]);

  const showClear = allowClear && !!value;
  const visibleOptions = useMemo<Option[]>(
    () => (showClear ? [CLEAR_OPTION, ...options] : options),
    [showClear, CLEAR_OPTION, options],
  );

  const clampIndex = (idx: number, opts: Option[]) => (opts.length ? Math.max(0, Math.min(idx, opts.length - 1)) : -1);

  const openWithReset = () => {
    setOpen(true);
    setHighlightedIndex(visibleOptions.length ? 0 : -1);
  };

  useEffect(() => {
    if (justClearedRef.current) return;
    setInputValue(value?.name || "");
  }, [value]);

  useEffect(() => {
    setHighlightedIndex((prev) => clampIndex(prev, visibleOptions));
  }, [visibleOptions.length]);

  const validateAndClose = useCallback(() => {
    setOpen(false);

    if (justClearedRef.current) {
      justClearedRef.current = false;
      setInputValue("");
      return;
    }

    const matched = options.find((o) => o.label === inputValue);
    if (matched) {
      if (value?.id !== matched.value) {
        onChange({ id: matched.value, name: matched.label });
      }
      if (inputValue !== matched.label) setInputValue(matched.label);
      return;
    }

    if (value && inputValue !== value.name) setInputValue(value.name);
    else if (!value && inputValue !== "") setInputValue("");
  }, [options, inputValue, onChange, value]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        validateAndClose();
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [validateAndClose]);

  useEffect(() => {
    if (!open || highlightedIndex < 0) return;
    const el = document.getElementById(`doc-type-option-${highlightedIndex}`);
    if (el) el.scrollIntoView({ block: "nearest" });
  }, [highlightedIndex, open]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
    setOpen(true);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (!open && (e.key === "ArrowDown" || e.key === "ArrowUp")) {
      setOpen(true);
      setHighlightedIndex(visibleOptions.length ? 0 : -1);
      return;
    }

    if (e.key === "ArrowDown") {
      e.preventDefault();
      setHighlightedIndex((i) => clampIndex((i < 0 ? -1 : i) + 1, visibleOptions));
      return;
    }

    if (e.key === "ArrowUp") {
      e.preventDefault();
      setHighlightedIndex((i) => clampIndex((i < 0 ? 0 : i) - 1, visibleOptions));
      return;
    }

    if (e.key === "Home") {
      e.preventDefault();
      setHighlightedIndex(visibleOptions.length ? 0 : -1);
      return;
    }

    if (e.key === "End") {
      e.preventDefault();
      setHighlightedIndex(visibleOptions.length ? visibleOptions.length - 1 : -1);
      return;
    }

    if (e.key === "PageDown") {
      e.preventDefault();
      setHighlightedIndex((i) => clampIndex((i < 0 ? -1 : i) + 5, visibleOptions));
      return;
    }

    if (e.key === "PageUp") {
      e.preventDefault();
      setHighlightedIndex((i) => clampIndex((i < 0 ? 0 : i) - 5, visibleOptions));
      return;
    }

    if (e.key === "Enter") {
      if ((e.nativeEvent as any)?.isComposing || highlightedIndex < 0) return;
      const selected = visibleOptions[highlightedIndex];
      if (!selected) return;

      if (selected.value === "__CLEAR__") {
        justClearedRef.current = true;
        onClear?.();
        setInputValue("");
        setOpen(false);
        return;
      }

      onChange({ id: selected.value, name: selected.label });
      setInputValue(selected.label);
      setOpen(false);
      return;
    }

    if (e.key === "Escape") {
      e.preventDefault();
      validateAndClose();
      return;
    }

    if (e.key === "Tab") {
      validateAndClose();
      return;
    }
  };

  const handleScroll = async (e: React.UIEvent<HTMLUListElement>) => {
    if (!hasNextPage || !loadMore || loadingMore) return;
    const list = e.currentTarget;
    const nearBottom = list.scrollTop + list.clientHeight >= list.scrollHeight - 10;
    if (!nearBottom) return;
    setLoadingMore(true);
    try {
      await loadMore();
    } finally {
      setLoadingMore(false);
    }
  };

  const handleSelect = (option: Option) => {
    if (option.value === "__CLEAR__") {
      justClearedRef.current = true;
      onClear?.();
      setInputValue("");
      setOpen(false);
      return;
    }
    onChange({ id: option.value, name: option.label });
    setInputValue(option.label);
    setOpen(false);
  };

  return (
    <Box ref={containerRef} sx={{ position: "relative", ...sx }}>
      <Box marginBottom={1} display="flex" flexDirection="row" alignItems="center" gap="4px">
        <Typography variant="subtitle1" component="label">
          {label}
        </Typography>
        {description && <InformationField description={description} />}
      </Box>

      <Box sx={{ position: "relative" }}>
        <TextField
          fullWidth
          disabled={disabled}
          value={inputValue}
          onChange={handleInputChange}
          onFocus={openWithReset}
          onClick={openWithReset}
          onBlur={validateAndClose}
          onKeyDown={handleKeyDown}
          placeholder="Select..."
          inputProps={{
            autoComplete: "off",
            role: "combobox",
            "aria-expanded": open,
            "aria-controls": open ? "doc-type-listbox" : undefined,
            "aria-activedescendant": open && highlightedIndex >= 0 ? `doc-type-option-${highlightedIndex}` : undefined,
          }}
        />
        {inputValue.length > 0 && allowClear && (
          <Box
            sx={{
              position: "absolute",
              right: 8,
              top: "50%",
              transform: "translateY(-50%)",
              cursor: "pointer",
              zIndex: 2,
              color: "#888",
              display: disabled ? "none" : "flex",
              alignItems: "center",
            }}
            onClick={() => {
              justClearedRef.current = true;
              onClear?.();
              setInputValue("");
              setOpen(false);
            }}
            aria-label={clearLabel}
          >
            <CloseIcon fontSize="small" />
          </Box>
        )}
      </Box>

      {open && (
        <AutocompleteOptionsList
          options={visibleOptions}
          highlightedIndex={highlightedIndex}
          loading={loading || loadingMore}
          onSelect={handleSelect}
          onScroll={handleScroll}
          clearValue="__CLEAR__"
          viewClear={inputValue.length > 0}
        />
      )}
    </Box>
  );
}

export function AutocompleteDropdownWithOptions({
  onChange,
  onClear,
  allowClear = true,
  clearLabel = "Clear selection",
  label,
  value,
  disabled,
  optionsDefault,
  description,
  sx,
}: PropsWithOptions) {
  const [open, setOpen] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [inputValue, setInputValue] = useState("");
  const justClearedRef = useRef(false);

  const debouncedText = useDebounced(inputValue, 300);

  const CLEAR_OPTION: Option = useMemo(() => ({ value: "__CLEAR__", label: clearLabel }), [clearLabel]);
  const options = optionsDefault.filter((option) => option.label.toLowerCase().includes(debouncedText.toLowerCase()));
  const showClear = allowClear && !!value;
  const visibleOptions = useMemo<Option[]>(
    () => (showClear ? [CLEAR_OPTION, ...options] : options),
    [showClear, CLEAR_OPTION, options],
  );

  const clampIndex = (idx: number, opts: Option[]) => (opts.length ? Math.max(0, Math.min(idx, opts.length - 1)) : -1);

  const openWithReset = () => {
    setOpen(true);
    setHighlightedIndex(visibleOptions.length ? 0 : -1);
  };

  useEffect(() => {
    if (justClearedRef.current) return;
    setInputValue(value?.name || "");
  }, [value]);

  useEffect(() => {
    setHighlightedIndex((prev) => clampIndex(prev, visibleOptions));
  }, [visibleOptions.length]);

  const validateAndClose = useCallback(() => {
    setOpen(false);

    if (justClearedRef.current) {
      justClearedRef.current = false;
      setInputValue("");
      return;
    }

    const matched = options.find((o) => o.label === inputValue);
    if (matched) {
      if (value?.id !== matched.value) {
        onChange({ id: matched.value, name: matched.label });
      }
      if (inputValue !== matched.label) setInputValue(matched.label);
      return;
    }

    if (value && inputValue !== value.name) setInputValue(value.name);
    else if (!value && inputValue !== "") setInputValue("");
  }, [options, inputValue, onChange, value]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        validateAndClose();
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [validateAndClose]);

  useEffect(() => {
    if (!open || highlightedIndex < 0) return;
    const el = document.getElementById(`doc-type-option-${highlightedIndex}`);
    if (el) el.scrollIntoView({ block: "nearest" });
  }, [highlightedIndex, open]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
    setOpen(true);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (!open && (e.key === "ArrowDown" || e.key === "ArrowUp")) {
      setOpen(true);
      setHighlightedIndex(visibleOptions.length ? 0 : -1);
      return;
    }

    if (e.key === "ArrowDown") {
      e.preventDefault();
      setHighlightedIndex((i) => clampIndex((i < 0 ? -1 : i) + 1, visibleOptions));
      return;
    }

    if (e.key === "ArrowUp") {
      e.preventDefault();
      setHighlightedIndex((i) => clampIndex((i < 0 ? 0 : i) - 1, visibleOptions));
      return;
    }

    if (e.key === "Home") {
      e.preventDefault();
      setHighlightedIndex(visibleOptions.length ? 0 : -1);
      return;
    }

    if (e.key === "End") {
      e.preventDefault();
      setHighlightedIndex(visibleOptions.length ? visibleOptions.length - 1 : -1);
      return;
    }

    if (e.key === "PageDown") {
      e.preventDefault();
      setHighlightedIndex((i) => clampIndex((i < 0 ? -1 : i) + 5, visibleOptions));
      return;
    }

    if (e.key === "PageUp") {
      e.preventDefault();
      setHighlightedIndex((i) => clampIndex((i < 0 ? 0 : i) - 5, visibleOptions));
      return;
    }

    if (e.key === "Enter") {
      if ((e.nativeEvent as any)?.isComposing || highlightedIndex < 0) return;
      const selected = visibleOptions[highlightedIndex];
      if (!selected) return;

      if (selected.value === "__CLEAR__") {
        justClearedRef.current = true;
        onClear?.();
        setInputValue("");
        setOpen(false);
        return;
      }

      onChange({ id: selected.value, name: selected.label });
      setInputValue(selected.label);
      setOpen(false);
      return;
    }

    if (e.key === "Escape") {
      e.preventDefault();
      validateAndClose();
      return;
    }

    if (e.key === "Tab") {
      validateAndClose();
      return;
    }
  };

  const handleSelect = (option: Option) => {
    if (option.value === "__CLEAR__") {
      justClearedRef.current = true;
      onClear?.();
      setInputValue("");
      setOpen(false);
      return;
    }
    onChange({ id: option.value, name: option.label });
    setInputValue(option.label);
    setOpen(false);
  };

  return (
    <Box ref={containerRef} sx={{ position: "relative", ...sx }}>
      <Box marginBottom={1} display="flex" flexDirection="row" alignItems="center" gap="4px">
        <Typography variant="subtitle1" component="label">
          {label}
        </Typography>
        {description && <InformationField description={description} />}
      </Box>

      <Box sx={{ position: "relative" }}>
        <TextField
          fullWidth
          disabled={disabled}
          value={inputValue}
          onChange={handleInputChange}
          onFocus={openWithReset}
          onClick={openWithReset}
          onBlur={validateAndClose}
          onKeyDown={handleKeyDown}
          placeholder="Select..."
          inputProps={{
            autoComplete: "off",
            role: "combobox",
            "aria-expanded": open,
            "aria-controls": open ? "doc-type-listbox" : undefined,
            "aria-activedescendant": open && highlightedIndex >= 0 ? `doc-type-option-${highlightedIndex}` : undefined,
          }}
        />
        {inputValue.length > 0 && allowClear && (
          <Box
            sx={{
              position: "absolute",
              right: 8,
              top: "50%",
              transform: "translateY(-50%)",
              cursor: "pointer",
              zIndex: 2,
              color: "#888",
              display: disabled ? "none" : "flex",
              alignItems: "center",
            }}
            onClick={() => {
              justClearedRef.current = true;
              onClear?.();
              setInputValue("");
              setOpen(false);
            }}
            aria-label={clearLabel}
          >
            <CloseIcon fontSize="small" />
          </Box>
        )}
      </Box>

      {open && (
        <AutocompleteOptionsList
          options={visibleOptions}
          highlightedIndex={highlightedIndex}
          loading={false}
          onSelect={handleSelect}
          clearValue="__CLEAR__"
          viewClear={inputValue.length > 0}
        />
      )}
    </Box>
  );
}
