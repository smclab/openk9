import React, { useMemo, useState } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  IconButton,
  Box,
  TextField,
  Typography,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
} from "@mui/material";
import { useTheme } from "@mui/material/styles";
import CloseIcon from "@mui/icons-material/Close";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import { gql, useMutation } from "@apollo/client";
import { useSuggestionCategoryQuery, useLanguagesOptionsQuery } from "../../../graphql-generated";
import { ADD_SUGGESTION_CATEGORY_TRANSLATION } from "../gql";

interface TranslationDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSave?: (translationData: TranslationConfig) => void;
  initialData?: Partial<TranslationConfig>;
  suggestionCategoryId: string;
}

interface TranslationConfig {
  language: string;
  name: string;
  description: string;
  translatedLanguages: string[];
  notTranslatedLanguages: string[];
}

const TranslationDialog: React.FC<TranslationDialogProps> = ({
  isOpen,
  onClose,
  onSave,
  initialData,
  suggestionCategoryId,
}) => {
  const theme = useTheme();
  const color = theme.palette.primary.main;

  const { data, refetch } = useSuggestionCategoryQuery({
    variables: { id: suggestionCategoryId },
    skip: !suggestionCategoryId,
    fetchPolicy: "network-only",
  });

  const languagesQuery = useLanguagesOptionsQuery();
  const availableLanguages = React.useMemo(
    () =>
      (languagesQuery.data?.options?.edges || [])
        .map((e) => ({
          code: String(e?.node?.value || "")
            .toLowerCase()
            .replace(/_/g, "-"),
          name: String(e?.node?.name || e?.node?.value || ""),
        }))
        .filter((l) => l.code),
    [languagesQuery.data],
  );

  const norm = (s?: string) => (s || "").toLowerCase().replace(/[-_]/g, "");

  const translatedNameLanguages = useMemo(() => {
    const t = data?.suggestionCategory?.translations || [];
    const langsWithName = new Set<string>();
    t.forEach((tr) => {
      if (tr?.key === "name" && (tr?.value || "").trim() !== "" && tr?.language) {
        langsWithName.add(norm(tr.language));
      }
    });
    return availableLanguages.filter((l) => langsWithName.has(norm(l.code))).map((l) => l.code);
  }, [data, availableLanguages]);

  const notTranslatedNameLanguages = useMemo(() => {
    const setTranslated = new Set(translatedNameLanguages.map((c) => norm(c)));
    return availableLanguages.filter((l) => !setTranslated.has(norm(l.code))).map((l) => l.code);
  }, [translatedNameLanguages, availableLanguages]);

  const [config, setConfig] = useState<TranslationConfig>({
    language: initialData?.language || "it-it",
    name: initialData?.name || "",
    description: initialData?.description || "",
    translatedLanguages: translatedNameLanguages,
    notTranslatedLanguages: notTranslatedNameLanguages,
  });

  React.useEffect(() => {
    setConfig((prev) => ({
      ...prev,
      translatedLanguages: translatedNameLanguages,
      notTranslatedLanguages: notTranslatedNameLanguages,
    }));
  }, [translatedNameLanguages, notTranslatedNameLanguages]);

  const [expandedSections, setExpandedSections] = useState({
    translated: true,
    notTranslated: true,
  });

  const [validation, setValidation] = useState({
    name: "",
    description: "",
  });

  const getTranslatedValue = (k: "name" | "description", langCode: string) => {
    const list = data?.suggestionCategory?.translations || [];
    const match = list.find((tr) => tr?.key === k && norm(tr?.language || undefined) === norm(langCode));
    return (match?.value || "").trim();
  };

  React.useEffect(() => {
    const filledName = getTranslatedValue("name", config.language);
    const filledDescription = getTranslatedValue("description", config.language);
    setConfig((prev) => ({
      ...prev,
      name: filledName,
      description: filledDescription,
    }));
  }, [config.language, data]);

  const handleClose = () => {
    setConfig({
      language: "it-it",
      name: "",
      description: "",
      translatedLanguages: translatedNameLanguages,
      notTranslatedLanguages: notTranslatedNameLanguages,
    });
    setValidation({ name: "", description: "" });
    onClose();
  };

  const handleInputChange = (field: keyof TranslationConfig, value: string) => {
    setConfig((prev) => ({
      ...prev,
      [field]: value,
    }));

    if (field in validation) {
      setValidation((prev) => ({
        ...prev,
        [field]: "",
      }));
    }
  };

  // Hook mutation
  const [addTranslation, { loading: saving }] = useMutation(ADD_SUGGESTION_CATEGORY_TRANSLATION);

  const toBackendLang = (code: string) => {
    const [a, b] = code.split("-");
    if (!b) return code;
    return `${a}_${b}`.toLowerCase().replace(/_.*/, `_${b.toUpperCase()}`);
  };

  const handleSave = async () => {
    const lang = toBackendLang(config.language);

    // name
    await addTranslation({
      variables: {
        suggestionCategoryId,
        language: lang,
        key: "name",
        value: config.name,
      },
    });

    // description
    await addTranslation({
      variables: {
        suggestionCategoryId,
        language: lang,
        key: "description",
        value: config.description,
      },
    });

    await refetch();
    onSave?.(config);
    handleClose();
  };

  const toggleSection = (section: "translated" | "notTranslated") => {
    setExpandedSections((prev) => ({
      ...prev,
      [section]: !prev[section],
    }));
  };

  const getLanguageInfo = (code: string) => {
    return availableLanguages.find((lang) => lang.code === code) || { name: code };
  };

  const renderLanguageChips = (languages: string[]) => {
    return languages.map((langCode) => {
      const langInfo = getLanguageInfo(langCode);
      return (
        <Chip
          key={langCode}
          label={`${langInfo.name} (${langCode})`}
          variant="outlined"
          sx={{
            cursor: "default",
            margin: 0.5,
            "&:hover": { backgroundColor: theme.palette.action.hover },
          }}
        />
      );
    });
  };

  return (
    <Dialog
      onClose={handleClose}
      open={isOpen}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: { borderRadius: "10px" },
      }}
    >
      <DialogTitle
        sx={{
          m: 0,
          p: 2,
          borderRadius: "8px 8px 0 0",
          color: "white",
          bgcolor: color,
          fontSize: "unset",
        }}
      >
        Add Translation - {data?.suggestionCategory?.name ?? ""}
        <IconButton
          aria-label="close"
          onClick={handleClose}
          sx={{
            position: "absolute",
            right: 8,
            top: 8,
            color: "white",
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers sx={{ p: 3 }}>
        <Box display="flex" flexDirection="column" gap={3} minWidth="400px">
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4}>
              <FormControl fullWidth>
                <InputLabel>Language</InputLabel>
                <Select
                  value={config.language}
                  label="Language"
                  onChange={(e) => handleInputChange("language", e.target.value)}
                >
                  {availableLanguages.map((lang) => (
                    <MenuItem key={lang.code} value={lang.code}>
                      <Box display="flex" alignItems="center" gap={1}>
                        <span>{lang.name}</span>
                        <span style={{ opacity: 0.6 }}>({lang.code})</span>
                      </Box>
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={8}>
              <TextField
                fullWidth
                label="Name"
                value={config.name}
                onChange={(e) => handleInputChange("name", e.target.value)}
                error={!!validation.name}
                helperText={validation.name}
                variant="outlined"
              />
            </Grid>
          </Grid>

          <TextField
            fullWidth
            label="Description"
            multiline
            rows={3}
            value={config.description}
            onChange={(e) => handleInputChange("description", e.target.value)}
            error={!!validation.description}
            helperText={validation.description}
            variant="outlined"
          />

          <Box>
            <Box
              display="flex"
              alignItems="center"
              gap={1}
              mb={1}
              sx={{ cursor: "pointer" }}
              onClick={() => toggleSection("translated")}
            >
              <Typography variant="h6" color="primary">
                Add Translation - {data?.suggestionCategory?.name ?? ""}
              </Typography>
              {expandedSections.translated ? <ExpandLessIcon /> : <ExpandMoreIcon />}
            </Box>
            {expandedSections.translated && (
              <Box
                sx={{
                  border: `1px solid ${theme.palette.divider}`,
                  borderRadius: 1,
                  p: 2,
                  minHeight: 60,
                  display: "flex",
                  flexWrap: "wrap",
                  alignItems: "flex-start",
                  gap: 0.5,
                }}
              >
                {config.translatedLanguages.length > 0 ? (
                  renderLanguageChips(config.translatedLanguages)
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    No translated languages
                  </Typography>
                )}
              </Box>
            )}
          </Box>

          <Box>
            <Box
              display="flex"
              alignItems="center"
              gap={1}
              mb={1}
              sx={{ cursor: "pointer" }}
              onClick={() => toggleSection("notTranslated")}
            >
              <Typography variant="h6" color="text.secondary">
                Not translated in
              </Typography>
              {expandedSections.notTranslated ? <ExpandLessIcon /> : <ExpandMoreIcon />}
            </Box>
            {expandedSections.notTranslated && (
              <Box
                sx={{
                  border: `1px solid ${theme.palette.divider}`,
                  borderRadius: 1,
                  p: 2,
                  minHeight: 60,
                  display: "flex",
                  flexWrap: "wrap",
                  alignItems: "flex-start",
                  gap: 0.5,
                }}
              >
                {config.notTranslatedLanguages.length > 0 ? (
                  renderLanguageChips(config.notTranslatedLanguages)
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    All languages are translated
                  </Typography>
                )}
              </Box>
            )}
          </Box>
        </Box>
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        <Button
          variant="outlined"
          onClick={handleSave}
          disabled={saving}
          sx={{
            borderRadius: "10px",
            textTransform: "none",
            fontWeight: 500,
          }}
        >
          SET CHANGES
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default TranslationDialog;
