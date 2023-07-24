import React from "react";
import { BrowserRouter, Routes, Route, Outlet, NavLink } from "react-router-dom";
import { SideNavigation } from "./components/SideNavigation";
import "@clayui/css/lib/css/atlas.css";
import { Provider } from "@clayui/core";
import spritemap from "@clayui/css/lib/images/icons/icons.svg";
import { DataSources } from "./components/DataSources";
import { EnrichPipelines } from "./components/EnrichPipelines";
import { SuggestionCategories } from "./components/SuggestionCategories";
import { PluginDrivers } from "./components/PluginDrivers";
import { EnrichItems } from "./components/EnrichItems";
import { DocumentTypes } from "./components/DocumentTypes";
import { PluginDriver } from "./components/PluginDriver";
import { EnrichItem } from "./components/EnrichItem";
import { EnrichPipeline } from "./components/EnrichPipeline";
import { DataSource } from "./components/DataSource";
import { SuggestionCategory } from "./components/SuggestionCategory";
import { DocumentType } from "./components/DocumentType";
import { DocumentTypeFields } from "./components/DocumentTypeFields";
import { DocumentTypeField } from "./components/DocumentTypeField";
import { SuggestionCategoryDocumentTypeFields } from "./components/SuggestionCategoryDocumentTypeFields";
import { QueryAnalyses } from "./components/QueryAnalyses";
import { QueryAnalysis } from "./components/QueryAnalysis";
import { Rule } from "./components/Rule";
import { Rules } from "./components/Rules";
import { Annotators } from "./components/Annotators";
import { Annotator } from "./components/Annotator";
import { EnrichPipelineEnrichItems } from "./components/EnrichPipelineEnrichItems";
import { ApplicationBar } from "./components/ApplicationBar";
import "./index.css";
import "./app.css";
import { QueryAnalysesRules } from "./components/QueryAnalysesRules";
import { DashBoard } from "./components/Dashboard";
import { WebCrawlerWizard } from "./wizards/WebCrawlerWizard";
import { QueryAnalysesAnnotators } from "./components/QueryAnalysesAnnotator";
import { Wizards } from "./wizards/Wizards";
import { DatabaseWizard } from "./wizards/DatabaseWizard";
import { ServerEmailWizard } from "./wizards/ServerEmailWizard";
import { GitLabWizard } from "./wizards/GitLabWizard";
import { LiferayWizard } from "./wizards/LiferayWizard";
import { GoogleWizard } from "./wizards/GoogleWizard";
import { GitHubWizard } from "./wizards/GitHubWizard";
import { SiteMapWizard } from "./wizards/SiteMapWizard";
import { Tabs } from "./components/Tabs";
import { Tab } from "./components/Tab";
import { TabTokenTabs } from "./components/TabTokens";
import { TabToken } from "./components/TabToken";
import { ToastProvider } from "./components/ToastProvider";
import { QueryClientProvider } from "@tanstack/react-query";
import { DocumentTypeMappings } from "./components/DocumentTypeMappings";
import { MonitoringEvents } from "./components/MonitoringEvents";
import { SearchConfigs } from "./components/SearchConfigs";
import { SearchConfig } from "./components/SearchConfig";
import { QueryParserConfig } from "./components/QueryParser";
import { QueryParsers } from "./components/QueryParsers";
import { DocumentTypeTemplates } from "./components/DocumentTypeTemplates";
import { DocumentTypeTemplate } from "./components/DocumentTypeTemplate";
import { Logs } from "./components/Logs";
import { PodsStatus } from "./components/PodsStatus";
import { Tokenizers } from "./components/Tokenizers";
import { Tokenizer } from "./components/Tokenizer";
import { TokenFilters } from "./components/TokenFilters";
import { TokenFilter } from "./components/TokenFIlter";
import { CharFilters } from "./components/CharFilters";
import { CharFilter } from "./components/CharFilter";
import { Analyzers } from "./components/Analyzers";
import { Analyzer } from "./components/Analyzer";
import { AnalyzerTokenFilters } from "./components/AnalyzerTokenFilters";
import { AnalyzerCharFilters } from "./components/AnalyzerCharFilters";
import { apolloClient } from "./components/apolloClient";
import { ApolloProvider } from "@apollo/client";
import { queryClient } from "./components/queryClient";
import { AuthenticationProvider } from "./components/authentication";
import { Buckets } from "./components/Buckets";
import { Bucket } from "./components/Bucket";
import { BucketDataSources } from "./components/BucketDataSource";
import { BucketTabs } from "./components/BucketTabs";
import { BucketSuggestionCategories } from "./components/BucketSuggestionCategories";
import { DropBoxWizard } from "./wizards/DropBoxWizard";
import { DocumentTypesSettings } from "./components/DocumentTypesSettings";
import { SubFieldsDocumentType } from "./components/SubFieldsDocumentType";
import { MachingLearning } from "./components/MachingLearning";
import { HuggingFaceCard } from "./components/HuggingFaceCards";
import { HuggingFace } from "./components/HuggingFace";
import { PluginDriverToAcl } from "./components/PluginDriverToAcl";
import { InformationBuckets } from "./components/InformationBuckets";
import { InformationDataSource } from "./components/InformationDataSource";
import { TabTokenTabsAssociation } from "./components/TabTokenTabs";
import { Languages } from "./components/Languages";
import { BucketLanguage } from "./components/BucketLanguage";
import { Language } from "./components/Language";

export default function App() {
  const [isSideMenuOpen, setIsSideMenuOpen] = React.useState(true);
  return (
    <AuthenticationProvider>
      <QueryClientProvider client={queryClient}>
        <ApolloProvider client={apolloClient}>
          <Provider spritemap={spritemap}>
            <ToastProvider>
              <BrowserRouter basename="/admin">
                <SideNavigation isSideMenuOpen={isSideMenuOpen} />
                <ApplicationBar isSideMenuOpen={isSideMenuOpen} onSideMenuToggle={setIsSideMenuOpen} />
                <div style={{ paddingLeft: isSideMenuOpen ? "319px" : "0px" }}>
                  <Routes>
                    <Route path="" element={<DashBoard />} />
                    <Route path="data-sources" element={<DataSources />} />
                    <Route path="data-sources/:datasourceId" element={<DataSource />} />
                    <Route path="data-sources">
                      <Route path="" element={<DataSources />} />
                      <Route path="new" element={<DataSource />} />
                      <Route
                        path=":datasourceId"
                        element={
                          <React.Fragment>
                            <NavTabs
                              tabs={[
                                { label: "Attributes", path: "" },
                                {
                                  label: "Label Metrics",
                                  path: "label-metrics-data-source",
                                },
                              ]}
                            />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<DataSource />} />
                        <Route path="label-metrics-data-source" element={<InformationDataSource />} />
                      </Route>
                    </Route>
                    <Route path="plugin-drivers">
                      <Route path="" element={<PluginDrivers />} />
                      <Route path="new" element={<PluginDriver />} />
                      <Route
                        path=":pluginDriverId"
                        element={
                          <React.Fragment>
                            <NavTabs
                              tabs={[
                                { label: "Attributes", path: "" },
                                {
                                  label: "associated acl",
                                  path: "associated-acl",
                                },
                              ]}
                            />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<PluginDriver />} />
                        <Route path="associated-acl" element={<PluginDriverToAcl />} />
                      </Route>
                    </Route>
                    <Route path="enrich-pipelines" element={<EnrichPipelines />} />
                    <Route
                      path="enrich-pipelines/:enrichPipelineId"
                      element={
                        <React.Fragment>
                          <EnrichPipeline />
                          <EnrichPipelineEnrichItems />
                        </React.Fragment>
                      }
                    />
                    <Route path="buckets">
                      <Route path="" element={<Buckets />} />
                      <Route path="new" element={<Bucket />} />
                      <Route
                        path=":bucketId"
                        element={
                          <React.Fragment>
                            <NavTabs
                              tabs={[
                                { label: "Attributes", path: "" },
                                {
                                  label: "Associated Data Sources",
                                  path: "data-sources",
                                },
                                {
                                  label: "Associated Suggestion Categories",
                                  path: "suggestion-categories",
                                },
                                {
                                  label: "Associated Languages",
                                  path: "languages",
                                },
                                { label: "Associated Tabs", path: "tabs" },
                                { label: "Label Metrics", path: "label-metrics-buckets" },
                              ]}
                            />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<Bucket />} />
                        <Route path="data-sources" element={<BucketDataSources />} />
                        <Route path="suggestion-categories" element={<BucketSuggestionCategories />} />
                        <Route path="tabs" element={<BucketTabs />} />
                        <Route path="label-metrics-buckets" element={<InformationBuckets />} />
                        <Route path="languages" element={<BucketLanguage />} />
                      </Route>
                    </Route>
                    <Route path="enrich-items" element={<EnrichItems />} />
                    <Route path="enrich-items/:enrichItemId" element={<EnrichItem />} />
                    <Route path="suggestion-categories">
                      <Route path="" element={<SuggestionCategories />} />
                      <Route path="new" element={<SuggestionCategory />} />
                      <Route
                        path=":suggestionCategoryId"
                        element={
                          <React.Fragment>
                            <NavTabs
                              tabs={[
                                { label: "Attributes", path: "" },
                                {
                                  label: "Document Type Fields",
                                  path: "document-type-fields",
                                },
                              ]}
                            />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<SuggestionCategory />} />
                        <Route path="document-type-fields" element={<SuggestionCategoryDocumentTypeFields />} />
                      </Route>
                    </Route>
                    <Route path="analyzers">
                      <Route path="" element={<Analyzers />} />
                      <Route path="new" element={<Analyzer />} />
                      <Route
                        path=":analyzerId"
                        element={
                          <React.Fragment>
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<Analyzer />} />
                        <Route path="char-filters" element={<AnalyzerCharFilters />} />
                        <Route path="token-filters" element={<AnalyzerTokenFilters />} />
                      </Route>
                    </Route>
                    <Route path="document-types">
                      <Route path="" element={<DocumentTypes />} />
                      <Route
                        path=":documentTypeId/document-type-fields/:documentTypeFieldId/:ParentId/:SubFieldId"
                        element={<SubFieldsDocumentType />}
                      />
                      <Route path=":documentTypeId/document-type-fields/:documentTypeFieldId" element={<DocumentTypeField />} />
                      <Route path="new" element={<DocumentType />} />
                      <Route path="settings" element={<DocumentTypesSettings />} />
                      <Route
                        path=":documentTypeId"
                        element={
                          <React.Fragment>
                            <NavTabs
                              tabs={[
                                { label: "Attributes", path: "" },
                                {
                                  label: "Document Type Fields",
                                  path: "document-type-fields",
                                },
                                { label: "Mappings", path: "mappings" },
                              ]}
                            />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<DocumentType />} />
                        <Route path="mappings" element={<DocumentTypeMappings />} />
                        <Route path="document-type-fields" element={<DocumentTypeFields />} />
                      </Route>
                    </Route>
                    <Route path="search-configs">
                      <Route path="" element={<SearchConfigs />} />
                      <Route path=":searchConfigId/query-parsers/:queryParserConfigId" element={<QueryParserConfig />} />
                      <Route path="new" element={<SearchConfig />} />
                      <Route
                        path=":searchConfigId"
                        element={
                          <React.Fragment>
                            <NavTabs
                              tabs={[
                                { label: "Attributes", path: "" },
                                {
                                  label: "Query Parsers",
                                  path: "query-parsers",
                                },
                              ]}
                            />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<SearchConfig />} />
                        <Route path="query-parsers" element={<QueryParsers />} />
                      </Route>
                    </Route>
                    <Route path="tabs">
                      <Route path="" element={<Tabs />} />
                      <Route path=":tabId/tab-tokens/:tabTokenId" element={<TabToken />} />
                      <Route path="new" element={<Tab />} />
                      <Route
                        path=":tabId"
                        element={
                          <React.Fragment>
                            <NavTabs
                              tabs={[
                                { label: "Attributes", path: "" },
                                {
                                  label: "Token Tabs ",
                                  path: "token-tabs",
                                },
                              ]}
                            />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<Tab />} />
                        <Route path="token-tabs" element={<TabTokenTabsAssociation />} />
                      </Route>
                    </Route>
                    <Route path="token-tabs">
                      <Route path="" element={<TabTokenTabs />} />
                      <Route path=":tabId/tab-tokens/:tabTokenId" element={<TabToken />} />
                      <Route path="new" element={<TabToken />} />
                      <Route
                        path=":tabTokenId"
                        element={
                          <React.Fragment>
                            <NavTabs tabs={[{ label: "Attributes", path: "" }]} />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<TabToken />} />
                      </Route>
                    </Route>
                    {/* <Route path="tab-tokens" element={<TabTokenTabs />} /> */}
                    <Route path="query-analyses">
                      <Route path="" element={<QueryAnalyses />} />
                      <Route path="new" element={<QueryAnalysis />} />
                      <Route
                        path=":queryAnalysisId"
                        element={
                          <React.Fragment>
                            <NavTabs
                              tabs={[
                                { label: "Attributes", path: "" },
                                { label: "Rules", path: "rules" },
                                { label: "Annotators", path: "annotators" },
                              ]}
                            />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<QueryAnalysis />} />
                        <Route path="Rules" element={<QueryAnalysesRules />} />
                        <Route path="Annotators" element={<QueryAnalysesAnnotators />} />
                      </Route>
                    </Route>
                    <Route path="rules">
                      <Route path="" element={<Rules />} />
                      <Route path="new" element={<Rule />} />
                      <Route
                        path=":ruleId"
                        element={
                          <React.Fragment>
                            <NavTabs tabs={[{ label: "Attributes", path: "" }]} />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<Rule />} />
                      </Route>
                    </Route>
                    <Route path="tokenizers">
                      <Route path="" element={<Tokenizers />} />
                      <Route path="new" element={<Tokenizer />} />
                      <Route
                        path=":tokenizerId"
                        element={
                          <React.Fragment>
                            <NavTabs tabs={[{ label: "Attributes", path: "" }]} />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<Tokenizer />} />
                      </Route>
                    </Route>
                    <Route path="token-filters">
                      <Route path="" element={<TokenFilters />} />
                      <Route path="new" element={<TokenFilter />} />
                      <Route
                        path=":tokenFilterId"
                        element={
                          <React.Fragment>
                            <NavTabs tabs={[{ label: "Attributes", path: "" }]} />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<TokenFilter />} />
                      </Route>
                    </Route>
                    <Route path="char-filters">
                      <Route path="" element={<CharFilters />} />
                      <Route path="new" element={<CharFilter />} />
                      <Route
                        path=":charFilterId"
                        element={
                          <React.Fragment>
                            <NavTabs tabs={[{ label: "Attributes", path: "" }]} />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<CharFilter />} />
                      </Route>
                    </Route>
                    <Route path="maching-learning">
                      <Route path="" element={<MachingLearning />} />
                      <Route path="hugging-face-view">
                        <Route path="" element={<HuggingFaceCard />} />
                        <Route path="configure-hugging-face" element={<HuggingFace />} />
                        <Route path="enrich-item/:name" element={<EnrichItem />} />
                      </Route>
                    </Route>
                    <Route path="annotators">
                      <Route path="" element={<Annotators />} />
                      <Route path="new" element={<Annotator />} />
                      <Route
                        path=":annotatorId"
                        element={
                          <React.Fragment>
                            <NavTabs tabs={[{ label: "Attributes", path: "" }]} />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<Annotator />} />
                      </Route>
                    </Route>
                    <Route path="languages">
                      <Route path="" element={<Languages />} />
                      <Route path="new" element={<Language />} />
                      <Route
                        path=":languageId"
                        element={
                          <React.Fragment>
                            <NavTabs tabs={[{ label: "Attributes", path: "" }]} />
                            <Outlet />
                          </React.Fragment>
                        }
                      >
                        <Route path="" element={<Language />} />
                      </Route>
                    </Route>
                    <Route path="wizard">
                      <Route path="web-crawler" element={<WebCrawlerWizard />} />
                    </Route>
                    <Route path="wizards">
                      <Route path="" element={<Wizards />} />
                      <Route path="web-crawler" element={<WebCrawlerWizard />} />
                      <Route path="sitemap" element={<SiteMapWizard />} />
                      <Route path="server-email" element={<ServerEmailWizard />} />
                      <Route path="github" element={<GitHubWizard />} />
                      <Route path="gitlab" element={<GitLabWizard />} />
                      <Route path="liferay" element={<LiferayWizard />} />
                      <Route path="database" element={<DatabaseWizard />} />
                      <Route path="google-drive" element={<GoogleWizard />} />
                      <Route path="dropbox" element={<DropBoxWizard />} />
                    </Route>
                    <Route path="monitoring-events" element={<MonitoringEvents />} />
                    <Route path="document-type-templates" element={<DocumentTypeTemplates />} />
                    <Route path="document-type-templates/:documentTypeTemplateId" element={<DocumentTypeTemplate />} />
                    <Route path="logs/:podName" element={<Logs />} />
                    <Route path="logs" element={<PodsStatus />} />
                    <Route path="*" element={<h1>Not Found</h1>} />
                  </Routes>
                </div>
              </BrowserRouter>
            </ToastProvider>
          </Provider>
        </ApolloProvider>
      </QueryClientProvider>
    </AuthenticationProvider>
  );
}

function NavTabs({ tabs }: { tabs: Array<{ label: string; path: string }> }) {
  return (
    <React.Fragment>
      <div
        className="navbar navbar-underline navigation-bar navigation-bar-secondary navbar-expand-md"
        style={{
          position: "sticky",
          backgroundColor: "white",
          boxShadow: "rgb(194 177 177 / 61%) 3px 4px 3px",
          top: "59px",
          zIndex: "2",
          borderTop: "1px solid #00000024",
        }}
      >
        <div className="container-fluid container-fluid-max-xl">
          <ul className="navbar-nav">
            {tabs.map(({ label, path }, index) => {
              return (
                <li className="nav-item" key={index}>
                  <NavLink
                    style={{ outline: "none", boxShadow: "none" }}
                    className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
                    to={path}
                    end={true}
                  >
                    <span className="navbar-text-truncate">{label}</span>
                  </NavLink>
                </li>
              );
            })}
          </ul>
        </div>
      </div>

      <style type="text/css">
        {`
        .navbar-underline.navbar-expand-md .navbar-nav .nav-link.active:after{
          background-color: red;
        }
        .navigation-bar-secondary .navbar-nav .nav-link.active {
          color: black;
      }
      .navigation-bar-secondary .navbar-nav .nav-link:hover {
        color: black;
    }
    `}
      </style>
    </React.Fragment>
  );
}

export const ClassNameButton = "btn btn-danger";
