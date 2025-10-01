import { ApolloProvider } from "@apollo/client";
import { ModalProvider } from "@components/Form";
import { ToastProvider } from "@components/Form/Form/ToastProvider";
import { useFilteredMenuItems } from "@components/Navigation/menuItems";
import { SideNavigationItem } from "@components/Navigation/SideNavigationItem";
import { SideNavigationContextProvider } from "@components/sideNavigationContext";
import { Clear, KeyboardArrowUp, Search, RateReview } from "@mui/icons-material";
import {
  AppBar,
  Box,
  createTheme,
  CssBaseline,
  Fab,
  IconButton,
  InputAdornment,
  List,
  TextField,
  ThemeProvider,
  Toolbar,
  Typography,
} from "@mui/material";
import { red, yellow } from "@mui/material/colors";
import {
  Analyzers,
  Annotators,
  CharFilters,
  DashBoard,
  Dataindices,
  Datasources,
  DocumentTypeTemplates,
  EmbeddingModels,
  EnrichItems,
  InformationNotification,
  LargeLanguageModels,
  Pipelines,
  PluginDrivers,
  QueryAnalyses,
  Rules,
  SaveAnalyzer,
  SaveAnnotator,
  SaveCharFilter,
  SaveDataindex,
  SaveDatasource,
  SaveDocumentTypeTemplate,
  SaveEmbeddingModel,
  SaveEnrichItem,
  SaveLargeLanguageModel,
  SavePipeline,
  SaveQueryAnalysis,
  SaveSearchConfig,
  SaveSuggestionCategory,
  SaveTab,
  SaveTokenFilter,
  SaveTokenizer,
  SaveTokenTab,
  SearchConfigs,
  SuggestionCategories,
  Tabs,
  TokenFilters,
  Tokenizers,
  TokenTabs,
} from "@pages";
import { Buckets, SaveBucket } from "@pages/buckets";
import DocumentTypes from "@pages/DocumentTypes/DocumentTypes";
import { SaveDocumentType } from "@pages/DocumentTypes/SaveDocumentType";
import { SubDocTypes } from "@pages/DocumentTypes/SubDocTypes";
import { SavePluginnDriverModel } from "@pages/PluginDriver/SavePluginDriver";
import { RagConfigurations, SaveRagConfiguration } from "@pages/RagConfiguration";
import { QueryClientProvider } from "@tanstack/react-query";
import React, { useCallback, useMemo, useState } from "react";
import { BrowserRouter, Outlet, Route, Routes } from "react-router-dom";
import "./app.css";
import { apolloClient } from "./components/apolloClient";
import { Logo } from "./components/common/Logo";
import { NavigationFooter } from "./components/Navigation/NavigationFooter";
import { queryClient } from "./components/queryClient";
import ThemeSwitcher from "./utils/ThemeSwitcher";

export const themeColor = {
  light: {
    primary: "#FFFFFF",
    secondary: "#FAFAFA",
  },
  dark: {
    primary: "#2c2a29",
    secondary: "#303030",
  },
  main: {
    default: red[500],
    lighter: red[100],
    darker: "#7f1818bf",
  },
};

const lightTheme = createTheme({
  shape: { borderRadius: 10 },
  components: {
    MuiTable: {
      styleOverrides: {
        root: { background: themeColor.light.secondary },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.light.secondary, // Colore di sfondo per l'intestazione
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundColor: "#ffffff",
        },
      },
      defaultProps: {
        variant: "outlined",
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          "&:hover": {
            backgroundColor: themeColor.main.lighter,
            color: themeColor.dark.primary,
          },
          "&.active .MuiListItemText-root": {
            backgroundColor: themeColor.main.default,
            color: themeColor.light.primary,
            paddingLeft: "10px",
            borderRadius: "8px",
          },
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: { background: themeColor.light.primary },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { background: "#333", padding: "10px" },
      },
    },
  },
  zIndex: { modal: 500, drawer: 100, snackbar: 510 },
  palette: {
    error: {
      main: red[500],
    },
    success: {
      main: "#2EC071",
    },
    warning: {
      main: "#F39C12",
    },
    info: {
      main: "#2980B9",
    },
    background: {
      paper: themeColor.light.secondary,
      default: themeColor.light.primary,
    },
    primary: {
      light: red[100],
      main: red[500],
      dark: red[800],
      contrastText: themeColor.light.primary,
    },
    secondary: {
      main: themeColor.light.primary,
    },
    mode: "light",
  },
  typography: {
    fontFamily: "Lato, sans-serif",
    h1: {
      fontSize: "2.125rem",
    },
    h2: {
      fontSize: "1.5rem",
      fontWeight: "bold",
    },
    h3: { fontSize: "1.17rem" },
    h4: { fontSize: "1rem" },
    // h5: { fontSize: "0.83rem" },
    // h6: { fontSize: "0.67rem" },
  },
});

const darkTheme = createTheme({
  shape: { borderRadius: 10 },
  components: {
    MuiSelect: {
      styleOverrides: {
        root: { background: themeColor.dark.secondary },
      },
    },
    MuiDialogContent: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.dark.secondary,
        },
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.dark.secondary,
          borderRadius: "0 0 10px 10px",
        },
      },
    },
    MuiTable: {
      styleOverrides: {
        root: {},
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: "1px solid #555", // Colore grigio scuro per le righe della tabella
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.dark.secondary, // Colore di sfondo per l'intestazione
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          backgroundColor: themeColor.dark.secondary,
        },
      },
    },
    MuiTableBody: {
      styleOverrides: {
        root: {},
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundColor: "#3b3b3b",
        },
      },
      defaultProps: {
        variant: "outlined",
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          "&:hover": {
            backgroundColor: themeColor.main.darker,
            color: themeColor.light.primary,
          },
          "&.active .MuiListItemText-root": {
            paddingLeft: "10px",
            color: "#FFFFFF",
            borderRadius: "8px",
          },
          ".MuiListItemText-root": {
            backgrundColor: "transparent",
          },
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: { background: themeColor.dark.primary },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          background: themeColor.dark.secondary,
        },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { background: "#FFFFFF", color: themeColor.dark.secondary, padding: "10px" },
      },
    },
  },
  zIndex: { modal: 500, drawer: 100, snackbar: 510 },
  palette: {
    mode: "dark",
    error: {
      main: red[500],
    },
    success: {
      main: "#2EC071",
    },
    warning: {
      main: "#F39C12",
    },
    info: {
      main: "#2980B9",
    },
    background: {
      paper: "#303030", // Sfondo più scuro
      default: "#2c2a29", // Sfondo generale scuro
    },
    primary: {
      light: red[100],
      main: red[500],
      dark: red[800],
      contrastText: themeColor.light.primary,
    },
    secondary: {
      main: themeColor.light.primary, // Secondario chiaro per contrasto
    },
    text: {
      primary: "#FFFFFF", // Testo chiaro per il tema scuro
      secondary: "#CCC", // Testo secondario meno prominente
      disabled: "#666", // Testo disabilitato chiaro
    },
  },
  typography: {
    fontFamily: "Lato, sans-serif",
    subtitle1: {
      color: "#FFFFFF",
    },
    h1: {
      fontSize: "2.125rem",
      fontWeight: "bold",
      color: "#FFFFFF", // Colore per i titoli principali
    },
    h2: {
      fontSize: "1.5rem",
      fontWeight: "bold",
      color: "#FFFFFF", // Colore per i titoli secondari
    },
    h3: { fontSize: "1.17rem", color: "#FFFFFF" },
    h4: { fontSize: "1rem", color: "#FFFFFF" },
    h5: { color: "#FFFFFF" },
    h6: { color: "#FFFFFF" },
    body1: {
      color: "#FFFFFF", // Colore per il testo del corpo
    },
    body2: {
      color: "#CCC", // Colore per il testo secondario
    },
  },
});

const AppRoutes = () => (
  <Routes>
    <Route path="" element={<DashBoard />} />
    <Route path="/buckets" element={<Buckets />} />
    <Route path="/bucket/:bucketId" element={<SaveBucket />} />
    <Route path="/bucket/:bucketId/:view" element={<SaveBucket />} />
    <Route path="/analyzers" element={<Analyzers />} />
    <Route path="/analyzer/:analyzerId" element={<SaveAnalyzer />} />
    <Route path="/analyzer/:analyzerId/:view" element={<SaveAnalyzer />} />
    <Route path="/suggestion-categories" element={<SuggestionCategories />} />
    <Route path="/suggestion-category/:suggestionCategoryId" element={<SaveSuggestionCategory />} />
    <Route path="/data-sources" element={<Datasources />} />
    <Route path="/data-source/:datasourceId/mode/:mode/landingTab/:landingTabId" element={<SaveDatasource />} />
    <Route path="/document-type/:documentTypeId/:view" element={<SaveDocumentType />} />
    <Route path="/document-types" element={<DocumentTypes />} />
    <Route path="/document-type/:documentTypeId" element={<SaveDocumentType />} />
    <Route path="/sub-document-type/:documentTypeId" element={<SubDocTypes />} />
    <Route path="/document-type-template/:documentTypeTemplateId/:view" element={<SaveDocumentTypeTemplate />} />
    <Route path="/document-type-templates" element={<DocumentTypeTemplates />} />
    <Route path="/document-type-template/:documentTypeTemplateId" element={<SaveDocumentTypeTemplate />} />
    <Route path="/suggestion-category/:suggestionCategoryId/:view" element={<SaveSuggestionCategory />} />
    <Route path="/enrich-item/:enrichItemId" element={<SaveEnrichItem />} />
    <Route path="/enrich-item/:enrichItemId/:view" element={<SaveEnrichItem />} />
    <Route path="/enrich-items" element={<EnrichItems />} />
    <Route path="/pipelines" element={<Pipelines />} />
    <Route path="/pipeline/:pipelineId/mode/:mode" element={<SavePipeline />} />
    <Route path="/tabs" element={<Tabs />} />
    <Route path="/tab/:tabId" element={<SaveTab />} />
    <Route path="/tab/:tabId/:view" element={<SaveTab />} />
    <Route path="/token-tabs" element={<TokenTabs />} />
    <Route path="/token-tab/:tokenTabId" element={<SaveTokenTab />} />
    <Route path="/token-tab/:tokenTabId/:view" element={<SaveTokenTab />} />
    <Route path="/annotators" element={<Annotators />} />
    <Route path="/annotator/:annotatorId" element={<SaveAnnotator />} />
    <Route path="/annotator/:annotatorId/:view" element={<SaveAnnotator />} />
    <Route path="/token-filter/:tokenFilterId" element={<SaveTokenFilter />} />
    <Route path="/token-filter/:tokenFilterId/:view" element={<SaveTokenFilter />} />
    <Route path="/token-filters" element={<TokenFilters />} />
    <Route path="/query-analysis/:queryAnalysisId" element={<SaveQueryAnalysis />} />
    <Route path="/query-analysis/:queryAnalysisId/:view" element={<SaveQueryAnalysis />} />
    <Route path="/query-analyses" element={<QueryAnalyses />} />
    <Route path="/char-filter/:charFilterId" element={<SaveCharFilter />} />
    <Route path="/char-filter/:charFilterId/:view" element={<SaveCharFilter />} />
    <Route path="/tokenizers" element={<Tokenizers />} />
    <Route path="/tokenizer/:tokenizerId" element={<SaveTokenizer />} />
    <Route path="/tokenizer/:tokenizerId/:view" element={<SaveTokenizer />} />
    <Route path="/plugin-drivers" element={<PluginDrivers />} />
    <Route path="/plugin-driver/:pluginDriverId" element={<SavePluginnDriverModel />} />
    <Route path="/plugin-driver/:pluginDriverId/:view" element={<SavePluginnDriverModel />} />
    <Route path="/search-configs" element={<SearchConfigs />} />
    <Route path="/search-config/:searchConfigId" element={<SaveSearchConfig />} />
    <Route path="/search-config/:searchConfigId/:view" element={<SaveSearchConfig />} />
    <Route path="/large-languages-model" element={<LargeLanguageModels />} />
    <Route path="/large-language-model/:LargeLanguageModelId" element={<SaveLargeLanguageModel />} />
    <Route path="large-language-model/:LargeLanguageModelId/:view" element={<SaveLargeLanguageModel />} />
    <Route path="/embedding-models" element={<EmbeddingModels />} />
    <Route path="/embedding-model/:embeddingModelsId" element={<SaveEmbeddingModel />} />
    <Route path="embedding-model/:embeddingModelsId/:view" element={<SaveEmbeddingModel />} />
    <Route path="/char-filters" element={<CharFilters />} />
    <Route path="/dataindices" element={<Dataindices />} />
    <Route path="/dataindex/:dataindexId/mode/:mode" element={<SaveDataindex />} />
    <Route path="/notificationInfo/:notificationId" element={<InformationNotification />} />
    <Route path="rules">
      <Route path="" element={<Rules />} />
      <Route
        path=":ruleId"
        element={
          <React.Fragment>
            <Outlet />
          </React.Fragment>
        }
      />
    </Route>
    <Route path="/rag-configurations" element={<RagConfigurations />} />
    <Route path="/rag-configuration/:ragConfigId" element={<SaveRagConfiguration />} />
    <Route path="/rag-configuration/:ragConfigId/:view" element={<SaveRagConfiguration />} />
  </Routes>
);

export const scrollToTop = () => {
  const mainContent = document.querySelector("main");
  if (mainContent) {
    mainContent.scrollTo({ top: 0, behavior: "smooth" });
  }
};

export default function App() {
  const [isSideMenuOpen, setIsSideMenuOpen] = React.useState(true);
  const [isNotification, setIsNotification] = React.useState(false);
  const savedTheme = localStorage.getItem("isDarkMode");
  const [isDarkMode, setIsDarkMode] = React.useState(savedTheme === "true");
  const memoizedTheme = useMemo(() => (isDarkMode ? darkTheme : lightTheme), [isDarkMode]);
  const toggleTheme = () => {
    const newTheme = !isDarkMode;
    setIsDarkMode(newTheme);
    localStorage.setItem("isDarkMode", newTheme.toString());
  };

  const borderColor = isDarkMode ? "rgba(255, 255, 255, 0.12)" : "rgba(0, 0, 0, 0.12)";

  const [showScrollTop, setShowScrollTop] = React.useState(false);

  // Controlla lo scroll per mostrare/nascondere il pulsante
  React.useEffect(() => {
    const handleScroll = () => {
      const mainContent = document.querySelector("main");
      if (mainContent) {
        setShowScrollTop(mainContent.scrollTop > 100); // Ridotto a 100px per renderlo più visibile
      }
    };

    const mainContent = document.querySelector("main");
    if (mainContent) {
      mainContent.addEventListener("scroll", handleScroll);
      return () => mainContent.removeEventListener("scroll", handleScroll);
    }
  }, []);

  const [searchTerm, setSearchTerm] = useState("");
  const filteredMenuItems = useFilteredMenuItems(searchTerm);

  return (
    <ThemeProvider theme={memoizedTheme}>
      <CssBaseline />
      <QueryClientProvider client={queryClient}>
        <ApolloProvider client={apolloClient}>
          <ToastProvider>
            <ModalProvider>
              <BrowserRouter basename="/admin">
                <SideNavigationContextProvider>
                  <Box
                    sx={{
                      display: "flex",
                      backgroundColor: isDarkMode ? "#1e1e1e" : "#f5f5f5",
                      minHeight: "100vh",
                      p: 1.25, // 10px di padding uniforme
                    }}
                  >
                    <AppBar
                      position="fixed"
                      elevation={0}
                      sx={{
                        zIndex: (theme) => theme.zIndex.drawer + 1,
                        backgroundColor: "background.paper",
                        m: 1.25, // 10px di margine
                        width: "calc(100% - 20px)", // Aggiustato per i margini
                        borderRadius: "10px",
                        border: `1px solid ${borderColor}`,
                      }}
                    >
                      <Toolbar>
                        <Box display="flex" alignItems="center" flexGrow={1}>
                          <Logo size={45} />
                          <Typography variant="h6" ml={1} color="text.primary">
                            Open
                          </Typography>
                          <Typography variant="h5" fontWeight={700} color="text.primary">
                            K9
                          </Typography>
                        </Box>
                        <Box sx={{ display: "flex", alignItems: "center", gap: 0 }}>
                          <IconButton
                            sx={{
                              borderRadius: "10px",
                              "&:hover": {
                                backgroundColor: "rgba(0, 0, 0, 0.04)",
                              },
                            }}
                            component="a"
                            href="https://clearflask.openk9.io/feedback"
                            target="_blank"
                            rel="noopener noreferrer"
                            color="warning"
                          >
                            <RateReview />
                          </IconButton>

                          {/* Vertical divider */}
                          <Box
                            sx={{
                              width: "1px",
                              height: "24px",
                              backgroundColor: borderColor,
                              mx: 1.5,
                            }}
                          />

                          <ThemeSwitcher isDarkMode={isDarkMode} toggleTheme={toggleTheme} />
                        </Box>
                      </Toolbar>
                    </AppBar>

                    <Box
                      sx={{
                        width: 240,
                        flexShrink: 0,
                        mt: "74px", // Aggiustato per i margini
                        mr: 1.25, // 10px di margine
                        height: "calc(100vh - 94px)", // Aggiustato per i margini
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "space-between",
                      }}
                    >
                      <Box
                        sx={{
                          backgroundColor: "background.paper",
                          borderRadius: "10px",
                          boxShadow: "none",
                          border: `1px solid ${borderColor}`,
                          flexGrow: 1,
                          overflow: "auto",
                          mb: 1.25,
                        }}
                      >
                        <TextField
                          fullWidth
                          size="small"
                          placeholder="Cerca sezione..."
                          value={searchTerm}
                          onChange={(e) => setSearchTerm(e.target.value)}
                          sx={{
                            p: 1,
                            "& .MuiInputBase-root": {
                              transition: "padding-left 0.3s ease-in-out",
                              paddingLeft: searchTerm ? "14px" : "40px",
                            },
                          }}
                          autoComplete="off"
                          InputProps={{
                            startAdornment: (
                              <InputAdornment
                                position="start"
                                sx={{
                                  position: "absolute",
                                  left: "14px",
                                  transition: "opacity 0.3s ease-in-out",
                                  opacity: searchTerm ? 0 : 1,
                                  pointerEvents: searchTerm ? "none" : "auto",
                                }}
                              >
                                <Search />
                              </InputAdornment>
                            ),
                            endAdornment: searchTerm && (
                              <InputAdornment position="end">
                                <Clear onClick={() => setSearchTerm("")} sx={{ cursor: "pointer" }} />
                              </InputAdornment>
                            ),
                          }}
                        />
                        <List component="nav" sx={{ p: 1 }}>
                          {filteredMenuItems.map((item, index) => (
                            <SideNavigationItem key={index} item={item} />
                          ))}
                        </List>
                      </Box>
                      <Box
                        sx={{
                          backgroundColor: "background.paper",
                          borderRadius: "10px",
                          boxShadow: "none",
                          border: `1px solid ${borderColor}`,
                          p: 2,
                          display: "flex",
                          justifyContent: "center",
                        }}
                      >
                        <NavigationFooter />
                      </Box>
                    </Box>

                    <Box
                      component="main"
                      sx={{
                        flexGrow: 1,
                        p: 3,
                        mt: "74px",
                        backgroundColor: "background.paper",
                        borderRadius: "10px",
                        boxShadow: "none",
                        border: `1px solid ${borderColor}`,
                        overflow: "auto",
                        height: "calc(100vh - 94px)",
                        position: "relative",
                      }}
                    >
                      <AppRoutes />
                      <Fab
                        color="primary"
                        size="medium"
                        aria-label="scroll back to top"
                        onClick={scrollToTop}
                        sx={{
                          position: "fixed",
                          bottom: 20,
                          right: 30,
                          opacity: showScrollTop ? 0.9 : 0,
                          visibility: showScrollTop ? "visible" : "hidden",
                          transition: "opacity 0.3s, visibility 0.3s",
                          "&:hover": {
                            opacity: 1,
                          },
                        }}
                      >
                        <KeyboardArrowUp fontSize="medium" />
                      </Fab>
                    </Box>
                  </Box>
                </SideNavigationContextProvider>
              </BrowserRouter>
            </ModalProvider>
          </ToastProvider>
        </ApolloProvider>
      </QueryClientProvider>
    </ThemeProvider>
  );
}
