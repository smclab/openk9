import { gql } from '@apollo/client';
import * as Apollo from '@apollo/client';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
const defaultOptions = {} as const;
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
  BigDecimal: any;
  BigInteger: any;
  DateTime: any;
  FormConfigurations: any;
};

export type AclMapping = {
  __typename?: 'AclMapping';
  docTypeField?: Maybe<DocTypeField>;
  key?: Maybe<PluginDriverDocTypeFieldKey>;
  pluginDriver?: Maybe<PluginDriver>;
  userField?: Maybe<UserField>;
};

export type Analyzer = {
  __typename?: 'Analyzer';
  charFilters?: Maybe<Connection_CharFilter>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  tokenFilters?: Maybe<Connection_TokenFilter>;
  tokenizer?: Maybe<Tokenizer>;
  type?: Maybe<Scalars['String']>;
};


export type AnalyzerCharFiltersArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


export type AnalyzerTokenFiltersArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type AnalyzerDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  type: Scalars['String'];
};

export type AnalyzerWithListsDtoInput = {
  charFilterIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  tokenFilterIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
  tokenizerId?: InputMaybe<Scalars['BigInteger']>;
  type: Scalars['String'];
};

export type Annotator = {
  __typename?: 'Annotator';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  docTypeField?: Maybe<DocTypeField>;
  docTypeFieldNotInAnnotator?: Maybe<Connection_DocTypeField>;
  extraParams?: Maybe<Scalars['String']>;
  fieldName?: Maybe<Scalars['String']>;
  fuziness?: Maybe<Fuzziness>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  size?: Maybe<Scalars['Int']>;
  type?: Maybe<AnnotatorType>;
};


export type AnnotatorDocTypeFieldNotInAnnotatorArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type AnnotatorDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  extraParams?: InputMaybe<Scalars['String']>;
  fieldName: Scalars['String'];
  fuziness: Fuzziness;
  name: Scalars['String'];
  size?: InputMaybe<Scalars['Int']>;
  type: AnnotatorType;
};

export enum AnnotatorType {
  Aggregator = 'AGGREGATOR',
  Autocomplete = 'AUTOCOMPLETE',
  Autocorrect = 'AUTOCORRECT',
  Doctype = 'DOCTYPE',
  Keyword = 'KEYWORD',
  KeywordAutocomplete = 'KEYWORD_AUTOCOMPLETE',
  Ner = 'NER',
  NerAutocomplete = 'NER_AUTOCOMPLETE',
  Stopword = 'STOPWORD',
  Token = 'TOKEN'
}

export type AnnotatorWithDocTypeFieldDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  /** The docTypeField to be associated. (optional) */
  docTypeFieldId?: InputMaybe<Scalars['BigInteger']>;
  extraParams?: InputMaybe<Scalars['String']>;
  fieldName: Scalars['String'];
  fuziness: Fuzziness;
  name: Scalars['String'];
  size?: InputMaybe<Scalars['Int']>;
  type: AnnotatorType;
};

export enum BehaviorMergeType {
  Merge = 'MERGE',
  Replace = 'REPLACE'
}

export enum BehaviorOnError {
  Fail = 'FAIL',
  Reject = 'REJECT',
  Skip = 'SKIP'
}

export type Bucket = {
  __typename?: 'Bucket';
  catIndices?: Maybe<Array<Maybe<CatResponse>>>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  datasources?: Maybe<Connection_Datasource>;
  description?: Maybe<Scalars['String']>;
  docCount?: Maybe<Scalars['BigInteger']>;
  enabled: Scalars['Boolean'];
  id?: Maybe<Scalars['ID']>;
  indexCount?: Maybe<Scalars['BigInteger']>;
  language?: Maybe<Language>;
  languages?: Maybe<Connection_Language>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  queryAnalysis?: Maybe<QueryAnalysis>;
  ragConfigurationChat?: Maybe<RagConfiguration>;
  ragConfigurationChatTool?: Maybe<RagConfiguration>;
  ragConfigurationSimpleGenerate?: Maybe<RagConfiguration>;
  refreshOnDate?: Maybe<Scalars['Boolean']>;
  refreshOnQuery?: Maybe<Scalars['Boolean']>;
  refreshOnSuggestionCategory?: Maybe<Scalars['Boolean']>;
  refreshOnTab?: Maybe<Scalars['Boolean']>;
  retrieveType?: Maybe<RetrieveType>;
  searchConfig?: Maybe<SearchConfig>;
  sortings?: Maybe<Connection_Sorting>;
  suggestionCategories?: Maybe<Connection_SuggestionCategory>;
  tabs?: Maybe<Connection_Tab>;
};


export type BucketDatasourcesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


export type BucketLanguagesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


export type BucketSortingsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


export type BucketSuggestionCategoriesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


export type BucketTabsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type BucketDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  refreshOnDate: Scalars['Boolean'];
  refreshOnQuery: Scalars['Boolean'];
  refreshOnSuggestionCategory: Scalars['Boolean'];
  refreshOnTab: Scalars['Boolean'];
  retrieveType: RetrieveType;
};

export type BucketWithListsDtoInput = {
  datasourceIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
  defaultLanguageId?: InputMaybe<Scalars['BigInteger']>;
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  queryAnalysisId?: InputMaybe<Scalars['BigInteger']>;
  ragConfigurationChat?: InputMaybe<Scalars['BigInteger']>;
  ragConfigurationChatTool?: InputMaybe<Scalars['BigInteger']>;
  ragConfigurationSimpleGenerate?: InputMaybe<Scalars['BigInteger']>;
  refreshOnDate: Scalars['Boolean'];
  refreshOnQuery: Scalars['Boolean'];
  refreshOnSuggestionCategory: Scalars['Boolean'];
  refreshOnTab: Scalars['Boolean'];
  retrieveType: RetrieveType;
  searchConfigId?: InputMaybe<Scalars['BigInteger']>;
  suggestionCategoryIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
  tabIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
};

export type CatResponse = {
  __typename?: 'CatResponse';
  docsCount?: Maybe<Scalars['String']>;
  docsDeleted?: Maybe<Scalars['String']>;
  health?: Maybe<Scalars['String']>;
  index?: Maybe<Scalars['String']>;
  pri?: Maybe<Scalars['String']>;
  priStoreSize: Scalars['BigInteger'];
  rep?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  storeSize: Scalars['BigInteger'];
  uuid?: Maybe<Scalars['String']>;
};

export type CharFilter = {
  __typename?: 'CharFilter';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
};

export type CharFilterDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  type: Scalars['String'];
};

export enum ChunkType {
  ChunkTypeCharacterTextSplitter = 'CHUNK_TYPE_CHARACTER_TEXT_SPLITTER',
  ChunkTypeDefault = 'CHUNK_TYPE_DEFAULT',
  ChunkTypeSemanticSplitter = 'CHUNK_TYPE_SEMANTIC_SPLITTER',
  ChunkTypeTextSplitter = 'CHUNK_TYPE_TEXT_SPLITTER',
  ChunkTypeTokenTextSplitter = 'CHUNK_TYPE_TOKEN_TEXT_SPLITTER',
  Unrecognized = 'UNRECOGNIZED'
}

/** A connection to a list of items. */
export type Connection_Analyzer = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Analyzer>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Annotator = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Annotator>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Bucket = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Bucket>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_CharFilter = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_CharFilter>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_DataIndex = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_DataIndex>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Datasource = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Datasource>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_DocType = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_DocType>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_DocTypeField = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_DocTypeField>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_DocTypeTemplate = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_DocTypeTemplate>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_EmbeddingModel = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_EmbeddingModel>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_EnrichItem = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_EnrichItem>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_EnrichPipeline = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_EnrichPipeline>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Language = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Language>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_LargeLanguageModel = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_LargeLanguageModel>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_PluginDriver = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_PluginDriver>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_QueryAnalysis = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_QueryAnalysis>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_QueryParserConfig = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_QueryParserConfig>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_RagConfiguration = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_RagConfiguration>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Rule = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Rule>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Scheduler = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Scheduler>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_SearchConfig = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_SearchConfig>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_SearchTokenDto = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_SearchTokenDto>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Sorting = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Sorting>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_SuggestionCategory = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_SuggestionCategory>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Tab = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Tab>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_TokenFilter = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_TokenFilter>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_TokenTab = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_TokenTab>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Tokenizer = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Tokenizer>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

export type CreateDatasourceDtoInput = {
  /** Configurations used to create the dataIndex */
  dataIndex: DataIndexDtoInput;
  description?: InputMaybe<Scalars['String']>;
  /** Json configuration with custom fields for datasource */
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  /** Pipeline to be created and associated (optional) */
  pipeline?: InputMaybe<PipelineWithItemsDtoInput>;
  /** Pipeline to be associated (optional) */
  pipelineId?: InputMaybe<Scalars['BigInteger']>;
  /** PluginDriver to be associated */
  pluginDriverId: Scalars['BigInteger'];
  /** The duration to identify orphaned Dataindex. */
  purgeMaxAge?: InputMaybe<Scalars['String']>;
  /** If true set active the purge job scheduling */
  purgeable?: InputMaybe<Scalars['Boolean']>;
  /** Cron quartz expression to define purging for this datasource */
  purging?: InputMaybe<Scalars['String']>;
  /** If true datasource is reindexed based on defined scheduling expression */
  reindexable?: InputMaybe<Scalars['Boolean']>;
  /** Cron quartz expression to define reindexing of datasource */
  reindexing?: InputMaybe<Scalars['String']>;
  /** If true datasource is scheduled based on defined scheduling expression */
  schedulable?: InputMaybe<Scalars['Boolean']>;
  /** Cron quartz expression to define scheduling of datasource */
  scheduling?: InputMaybe<Scalars['String']>;
};

export type CreateRagConfigurationDtoInput = {
  /**
   * Controls context window merging behavior for chunk processing:
   * 0: Disables chunk merging.
   * > 0: Enables merging with specified window size.
   *
   */
  chunkWindow?: InputMaybe<Scalars['Int']>;
  description?: InputMaybe<Scalars['String']>;
  /** A JSON that can be used to add additional configurations to the EmbeddingModel. */
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  /** Main prompt template used for RAG. */
  prompt?: InputMaybe<Scalars['String']>;
  /**
   * Prompt template used specifically in RAG-as-tool configurations when the RAG
   * tool is available but not invoked by the LLM.
   *
   */
  promptNoRag?: InputMaybe<Scalars['String']>;
  /**
   * Description of the RAG tool's capabilities, used in RAG-as-tool implementations
   * to help the LLM decide when to invoke it.
   *
   */
  ragToolDescription?: InputMaybe<Scalars['String']>;
  /**
   * Boolean flag that controls whether a large language model should reformulate
   * the input prompt before processing it using rephrasePrompt.
   *
   */
  reformulate?: InputMaybe<Scalars['Boolean']>;
  /** Prompt template used if reformulate is set to true. */
  rephrasePrompt?: InputMaybe<Scalars['String']>;
  type: RagType;
};

export type DataIndex = {
  __typename?: 'DataIndex';
  cat?: Maybe<CatResponse>;
  chunkType?: Maybe<ChunkType>;
  chunkWindowSize?: Maybe<Scalars['Int']>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  datasource?: Maybe<Datasource>;
  description?: Maybe<Scalars['String']>;
  docCount?: Maybe<Scalars['BigInteger']>;
  docTypes?: Maybe<Connection_DocType>;
  embeddingDocTypeField?: Maybe<DocTypeField>;
  embeddingJsonConfig?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  knnIndex?: Maybe<Scalars['Boolean']>;
  mappings?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  settings?: Maybe<Scalars['String']>;
};


export type DataIndexDocTypesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type DataIndexDtoInput = {
  /** The chunk strategy to apply. */
  chunkType?: InputMaybe<ChunkType>;
  /** The number of chunks before and after every chunk. */
  chunkWindowSize?: InputMaybe<Scalars['Int']>;
  description?: InputMaybe<Scalars['String']>;
  /** The list of documentType ids that the dataIndex is composed of. */
  docTypeIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
  /**
   * The field used during the text embedding,
   * must be a valid docTypeFieldId.
   */
  embeddingDocTypeFieldId?: InputMaybe<Scalars['BigInteger']>;
  /** The configurations used by the embedding model, if needed. */
  embeddingJsonConfig?: InputMaybe<Scalars['String']>;
  /**
   * Define if this index is a knn index, this property enables
   * vector similarity search features on this DataIndex.
   */
  knnIndex?: InputMaybe<Scalars['Boolean']>;
  name: Scalars['String'];
  /** The settings that will be used when the associated indexTemplate is created. */
  settings?: InputMaybe<Scalars['String']>;
};

export type Datasource = {
  __typename?: 'Datasource';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  dataIndex?: Maybe<DataIndex>;
  dataIndexes?: Maybe<Connection_DataIndex>;
  description?: Maybe<Scalars['String']>;
  enrichPipeline?: Maybe<EnrichPipeline>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** Last ingestion date of data for current datasource (ISO-8601) */
  lastIngestionDate?: Maybe<Scalars['DateTime']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  pluginDriver?: Maybe<PluginDriver>;
  /** The duration to identify orphaned Dataindex. */
  purgeMaxAge?: Maybe<Scalars['String']>;
  /** If true set active the purge job scheduling */
  purgeable?: Maybe<Scalars['Boolean']>;
  /** Chron quartz expression to define purging for this datasource */
  purging?: Maybe<Scalars['String']>;
  /** If true set datasource as reindexable */
  reindexable?: Maybe<Scalars['Boolean']>;
  /** Chron quartz expression to define reindexing of datasource */
  reindexing?: Maybe<Scalars['String']>;
  /** If true set datasource as schedulable */
  schedulable?: Maybe<Scalars['Boolean']>;
  schedulers?: Maybe<Connection_Scheduler>;
  /** Chron quartz expression to define scheduling of datasource */
  scheduling?: Maybe<Scalars['String']>;
};


export type DatasourceDataIndexesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


export type DatasourceSchedulersArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type DatasourceDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  /** Json configuration with custom fields for datasource */
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  /** The duration to identify orphaned Dataindex. */
  purgeMaxAge?: InputMaybe<Scalars['String']>;
  /** If true set active the purge job scheduling */
  purgeable?: InputMaybe<Scalars['Boolean']>;
  /** Cron quartz expression to define purging for this datasource */
  purging?: InputMaybe<Scalars['String']>;
  /** If true datasource is reindexed based on defined scheduling expression */
  reindexable?: InputMaybe<Scalars['Boolean']>;
  /** Cron quartz expression to define reindexing of datasource */
  reindexing?: InputMaybe<Scalars['String']>;
  /** If true datasource is scheduled based on defined scheduling expression */
  schedulable?: InputMaybe<Scalars['Boolean']>;
  /** Cron quartz expression to define scheduling of datasource */
  scheduling?: InputMaybe<Scalars['String']>;
};

export type DefaultConnection_Analyzer = Connection_Analyzer & {
  __typename?: 'DefaultConnection_Analyzer';
  edges?: Maybe<Array<Maybe<Edge_Analyzer>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Annotator = Connection_Annotator & {
  __typename?: 'DefaultConnection_Annotator';
  edges?: Maybe<Array<Maybe<Edge_Annotator>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Bucket = Connection_Bucket & {
  __typename?: 'DefaultConnection_Bucket';
  edges?: Maybe<Array<Maybe<Edge_Bucket>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_CharFilter = Connection_CharFilter & {
  __typename?: 'DefaultConnection_CharFilter';
  edges?: Maybe<Array<Maybe<Edge_CharFilter>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_DataIndex = Connection_DataIndex & {
  __typename?: 'DefaultConnection_DataIndex';
  edges?: Maybe<Array<Maybe<Edge_DataIndex>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Datasource = Connection_Datasource & {
  __typename?: 'DefaultConnection_Datasource';
  edges?: Maybe<Array<Maybe<Edge_Datasource>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_DocType = Connection_DocType & {
  __typename?: 'DefaultConnection_DocType';
  edges?: Maybe<Array<Maybe<Edge_DocType>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_DocTypeField = Connection_DocTypeField & {
  __typename?: 'DefaultConnection_DocTypeField';
  edges?: Maybe<Array<Maybe<Edge_DocTypeField>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_DocTypeTemplate = Connection_DocTypeTemplate & {
  __typename?: 'DefaultConnection_DocTypeTemplate';
  edges?: Maybe<Array<Maybe<Edge_DocTypeTemplate>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_EmbeddingModel = Connection_EmbeddingModel & {
  __typename?: 'DefaultConnection_EmbeddingModel';
  edges?: Maybe<Array<Maybe<Edge_EmbeddingModel>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_EnrichItem = Connection_EnrichItem & {
  __typename?: 'DefaultConnection_EnrichItem';
  edges?: Maybe<Array<Maybe<Edge_EnrichItem>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_EnrichPipeline = Connection_EnrichPipeline & {
  __typename?: 'DefaultConnection_EnrichPipeline';
  edges?: Maybe<Array<Maybe<Edge_EnrichPipeline>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Language = Connection_Language & {
  __typename?: 'DefaultConnection_Language';
  edges?: Maybe<Array<Maybe<Edge_Language>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_LargeLanguageModel = Connection_LargeLanguageModel & {
  __typename?: 'DefaultConnection_LargeLanguageModel';
  edges?: Maybe<Array<Maybe<Edge_LargeLanguageModel>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_PluginDriver = Connection_PluginDriver & {
  __typename?: 'DefaultConnection_PluginDriver';
  edges?: Maybe<Array<Maybe<Edge_PluginDriver>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_QueryAnalysis = Connection_QueryAnalysis & {
  __typename?: 'DefaultConnection_QueryAnalysis';
  edges?: Maybe<Array<Maybe<Edge_QueryAnalysis>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_QueryParserConfig = Connection_QueryParserConfig & {
  __typename?: 'DefaultConnection_QueryParserConfig';
  edges?: Maybe<Array<Maybe<Edge_QueryParserConfig>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_RagConfiguration = Connection_RagConfiguration & {
  __typename?: 'DefaultConnection_RAGConfiguration';
  edges?: Maybe<Array<Maybe<Edge_RagConfiguration>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Rule = Connection_Rule & {
  __typename?: 'DefaultConnection_Rule';
  edges?: Maybe<Array<Maybe<Edge_Rule>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Scheduler = Connection_Scheduler & {
  __typename?: 'DefaultConnection_Scheduler';
  edges?: Maybe<Array<Maybe<Edge_Scheduler>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_SearchConfig = Connection_SearchConfig & {
  __typename?: 'DefaultConnection_SearchConfig';
  edges?: Maybe<Array<Maybe<Edge_SearchConfig>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_SearchTokenDto = Connection_SearchTokenDto & {
  __typename?: 'DefaultConnection_SearchTokenDto';
  edges?: Maybe<Array<Maybe<Edge_SearchTokenDto>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Sorting = Connection_Sorting & {
  __typename?: 'DefaultConnection_Sorting';
  edges?: Maybe<Array<Maybe<Edge_Sorting>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_SuggestionCategory = Connection_SuggestionCategory & {
  __typename?: 'DefaultConnection_SuggestionCategory';
  edges?: Maybe<Array<Maybe<Edge_SuggestionCategory>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Tab = Connection_Tab & {
  __typename?: 'DefaultConnection_Tab';
  edges?: Maybe<Array<Maybe<Edge_Tab>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_TokenFilter = Connection_TokenFilter & {
  __typename?: 'DefaultConnection_TokenFilter';
  edges?: Maybe<Array<Maybe<Edge_TokenFilter>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_TokenTab = Connection_TokenTab & {
  __typename?: 'DefaultConnection_TokenTab';
  edges?: Maybe<Array<Maybe<Edge_TokenTab>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Tokenizer = Connection_Tokenizer & {
  __typename?: 'DefaultConnection_Tokenizer';
  edges?: Maybe<Array<Maybe<Edge_Tokenizer>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultEdge_Analyzer = Edge_Analyzer & {
  __typename?: 'DefaultEdge_Analyzer';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Analyzer>;
};

export type DefaultEdge_Annotator = Edge_Annotator & {
  __typename?: 'DefaultEdge_Annotator';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Annotator>;
};

export type DefaultEdge_Bucket = Edge_Bucket & {
  __typename?: 'DefaultEdge_Bucket';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Bucket>;
};

export type DefaultEdge_CharFilter = Edge_CharFilter & {
  __typename?: 'DefaultEdge_CharFilter';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<CharFilter>;
};

export type DefaultEdge_DataIndex = Edge_DataIndex & {
  __typename?: 'DefaultEdge_DataIndex';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<DataIndex>;
};

export type DefaultEdge_Datasource = Edge_Datasource & {
  __typename?: 'DefaultEdge_Datasource';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Datasource>;
};

export type DefaultEdge_DocType = Edge_DocType & {
  __typename?: 'DefaultEdge_DocType';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<DocType>;
};

export type DefaultEdge_DocTypeField = Edge_DocTypeField & {
  __typename?: 'DefaultEdge_DocTypeField';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<DocTypeField>;
};

export type DefaultEdge_DocTypeTemplate = Edge_DocTypeTemplate & {
  __typename?: 'DefaultEdge_DocTypeTemplate';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<DocTypeTemplate>;
};

export type DefaultEdge_EmbeddingModel = Edge_EmbeddingModel & {
  __typename?: 'DefaultEdge_EmbeddingModel';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<EmbeddingModel>;
};

export type DefaultEdge_EnrichItem = Edge_EnrichItem & {
  __typename?: 'DefaultEdge_EnrichItem';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<EnrichItem>;
};

export type DefaultEdge_EnrichPipeline = Edge_EnrichPipeline & {
  __typename?: 'DefaultEdge_EnrichPipeline';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<EnrichPipeline>;
};

export type DefaultEdge_Language = Edge_Language & {
  __typename?: 'DefaultEdge_Language';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Language>;
};

export type DefaultEdge_LargeLanguageModel = Edge_LargeLanguageModel & {
  __typename?: 'DefaultEdge_LargeLanguageModel';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<LargeLanguageModel>;
};

export type DefaultEdge_PluginDriver = Edge_PluginDriver & {
  __typename?: 'DefaultEdge_PluginDriver';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<PluginDriver>;
};

export type DefaultEdge_QueryAnalysis = Edge_QueryAnalysis & {
  __typename?: 'DefaultEdge_QueryAnalysis';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<QueryAnalysis>;
};

export type DefaultEdge_QueryParserConfig = Edge_QueryParserConfig & {
  __typename?: 'DefaultEdge_QueryParserConfig';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<QueryParserConfig>;
};

export type DefaultEdge_RagConfiguration = Edge_RagConfiguration & {
  __typename?: 'DefaultEdge_RAGConfiguration';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<RagConfiguration>;
};

export type DefaultEdge_Rule = Edge_Rule & {
  __typename?: 'DefaultEdge_Rule';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Rule>;
};

export type DefaultEdge_Scheduler = Edge_Scheduler & {
  __typename?: 'DefaultEdge_Scheduler';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Scheduler>;
};

export type DefaultEdge_SearchConfig = Edge_SearchConfig & {
  __typename?: 'DefaultEdge_SearchConfig';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<SearchConfig>;
};

export type DefaultEdge_SearchTokenDto = Edge_SearchTokenDto & {
  __typename?: 'DefaultEdge_SearchTokenDto';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<SearchTokenDto>;
};

export type DefaultEdge_Sorting = Edge_Sorting & {
  __typename?: 'DefaultEdge_Sorting';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Sorting>;
};

export type DefaultEdge_SuggestionCategory = Edge_SuggestionCategory & {
  __typename?: 'DefaultEdge_SuggestionCategory';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<SuggestionCategory>;
};

export type DefaultEdge_Tab = Edge_Tab & {
  __typename?: 'DefaultEdge_Tab';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Tab>;
};

export type DefaultEdge_TokenFilter = Edge_TokenFilter & {
  __typename?: 'DefaultEdge_TokenFilter';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<TokenFilter>;
};

export type DefaultEdge_TokenTab = Edge_TokenTab & {
  __typename?: 'DefaultEdge_TokenTab';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<TokenTab>;
};

export type DefaultEdge_Tokenizer = Edge_Tokenizer & {
  __typename?: 'DefaultEdge_Tokenizer';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Tokenizer>;
};

export type DefaultPageInfo = PageInfo & {
  __typename?: 'DefaultPageInfo';
  endCursor?: Maybe<Scalars['String']>;
  hasNextPage: Scalars['Boolean'];
  hasPreviousPage: Scalars['Boolean'];
  startCursor?: Maybe<Scalars['String']>;
};

export enum Direction {
  Asc = 'ASC',
  Desc = 'DESC'
}

export type DocType = {
  __typename?: 'DocType';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  docTypeFields?: Maybe<Connection_DocTypeField>;
  docTypeTemplate?: Maybe<DocTypeTemplate>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
};


export type DocTypeDocTypeFieldsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type DocTypeDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
};

export type DocTypeField = {
  __typename?: 'DocTypeField';
  analyzer?: Maybe<Analyzer>;
  autocomplete: Scalars['Boolean'];
  boolean: Scalars['Boolean'];
  boost?: Maybe<Scalars['Float']>;
  children?: Maybe<Array<Maybe<DocTypeField>>>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  date: Scalars['Boolean'];
  defaultBoost: Scalars['Boolean'];
  defaultExclude: Scalars['Boolean'];
  description?: Maybe<Scalars['String']>;
  docType?: Maybe<DocType>;
  docTypeFieldAndChildren?: Maybe<Array<Maybe<DocTypeField>>>;
  exclude?: Maybe<Scalars['Boolean']>;
  fieldName?: Maybe<Scalars['String']>;
  fieldType?: Maybe<FieldType>;
  floatBoost?: Maybe<Scalars['Float']>;
  i18N: Scalars['Boolean'];
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  keyword: Scalars['Boolean'];
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  numeric: Scalars['Boolean'];
  parent?: Maybe<DocTypeField>;
  path?: Maybe<Scalars['String']>;
  searchable?: Maybe<Scalars['Boolean']>;
  searchableAndAutocomplete: Scalars['Boolean'];
  searchableAndDate: Scalars['Boolean'];
  searchableAndI18N: Scalars['Boolean'];
  searchableAndText: Scalars['Boolean'];
  sortable?: Maybe<Scalars['Boolean']>;
  subFields?: Maybe<Connection_DocTypeField>;
  text: Scalars['Boolean'];
  translations?: Maybe<Array<Maybe<TranslationDto>>>;
};


export type DocTypeFieldSubFieldsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type DocTypeFieldDtoInput = {
  /** Value to define boost on score in case of matches on current field */
  boost?: InputMaybe<Scalars['Float']>;
  description?: InputMaybe<Scalars['String']>;
  /** If true field is not returned by search */
  exclude?: InputMaybe<Scalars['Boolean']>;
  fieldName: Scalars['String'];
  /** Define type used to map field in index */
  fieldType: FieldType;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  /** If true field is used for matches during search */
  searchable: Scalars['Boolean'];
  /** If true field is used for sorting during search */
  sortable: Scalars['Boolean'];
};

export type DocTypeFieldWithAnalyzerDtoInput = {
  /** The analyzerId used to analyze the query string in full-text query. (optional) */
  analyzerId?: InputMaybe<Scalars['BigInteger']>;
  /** Value to define boost on score in case of matches on current field */
  boost?: InputMaybe<Scalars['Float']>;
  description?: InputMaybe<Scalars['String']>;
  /** If true field is not returned by search */
  exclude?: InputMaybe<Scalars['Boolean']>;
  fieldName: Scalars['String'];
  /** Define type used to map field in index */
  fieldType: FieldType;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  /** If true field is used for matches during search */
  searchable: Scalars['Boolean'];
  /** If true field is used for sorting during search */
  sortable: Scalars['Boolean'];
};

export type DocTypeTemplate = {
  __typename?: 'DocTypeTemplate';
  compiled?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  source?: Maybe<Scalars['String']>;
  templateType?: Maybe<TemplateType>;
};

export type DocTypeTemplateDtoInput = {
  compiled: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  source: Scalars['String'];
  templateType: TemplateType;
};

export type DocTypeUserDtoInput = {
  docTypeId: Scalars['BigInteger'];
  userField?: InputMaybe<UserField>;
};

export type DocTypeWithTemplateDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  /** Rendering template to be associated. (optional) */
  docTypeTemplateId?: InputMaybe<Scalars['BigInteger']>;
  name: Scalars['String'];
};

/** An edge in a connection */
export type Edge_Analyzer = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Analyzer>;
};

/** An edge in a connection */
export type Edge_Annotator = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Annotator>;
};

/** An edge in a connection */
export type Edge_Bucket = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Bucket>;
};

/** An edge in a connection */
export type Edge_CharFilter = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<CharFilter>;
};

/** An edge in a connection */
export type Edge_DataIndex = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<DataIndex>;
};

/** An edge in a connection */
export type Edge_Datasource = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Datasource>;
};

/** An edge in a connection */
export type Edge_DocType = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<DocType>;
};

/** An edge in a connection */
export type Edge_DocTypeField = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<DocTypeField>;
};

/** An edge in a connection */
export type Edge_DocTypeTemplate = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<DocTypeTemplate>;
};

/** An edge in a connection */
export type Edge_EmbeddingModel = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<EmbeddingModel>;
};

/** An edge in a connection */
export type Edge_EnrichItem = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<EnrichItem>;
};

/** An edge in a connection */
export type Edge_EnrichPipeline = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<EnrichPipeline>;
};

/** An edge in a connection */
export type Edge_Language = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Language>;
};

/** An edge in a connection */
export type Edge_LargeLanguageModel = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<LargeLanguageModel>;
};

/** An edge in a connection */
export type Edge_PluginDriver = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<PluginDriver>;
};

/** An edge in a connection */
export type Edge_QueryAnalysis = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<QueryAnalysis>;
};

/** An edge in a connection */
export type Edge_QueryParserConfig = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<QueryParserConfig>;
};

/** An edge in a connection */
export type Edge_RagConfiguration = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<RagConfiguration>;
};

/** An edge in a connection */
export type Edge_Rule = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Rule>;
};

/** An edge in a connection */
export type Edge_Scheduler = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Scheduler>;
};

/** An edge in a connection */
export type Edge_SearchConfig = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<SearchConfig>;
};

/** An edge in a connection */
export type Edge_SearchTokenDto = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<SearchTokenDto>;
};

/** An edge in a connection */
export type Edge_Sorting = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Sorting>;
};

/** An edge in a connection */
export type Edge_SuggestionCategory = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<SuggestionCategory>;
};

/** An edge in a connection */
export type Edge_Tab = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Tab>;
};

/** An edge in a connection */
export type Edge_TokenFilter = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<TokenFilter>;
};

/** An edge in a connection */
export type Edge_TokenTab = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<TokenTab>;
};

/** An edge in a connection */
export type Edge_Tokenizer = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Tokenizer>;
};

export type EmbeddingModel = {
  __typename?: 'EmbeddingModel';
  apiKey?: Maybe<Scalars['String']>;
  apiUrl?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  enabled: Scalars['Boolean'];
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  providerModel?: Maybe<ProviderModel>;
  vectorSize?: Maybe<Scalars['Int']>;
};

export type EmbeddingModelDtoInput = {
  /**
   * Authentication API key required for accessing the embedding model's service.
   * Necessary for providers that require authentication to use their embedding API.
   * Ensure this key is kept confidential.
   *
   */
  apiKey?: InputMaybe<Scalars['String']>;
  /**
   * The API URL for the embedding model's endpoint.
   * Required only when using a custom embedding service or a model hosted
   * on a private/internal network.
   *
   */
  apiUrl?: InputMaybe<Scalars['String']>;
  description?: InputMaybe<Scalars['String']>;
  /** A JSON that can be used to add additional configurations to the EmbeddingModel. */
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  providerModel: ProviderModelDtoInput;
  /**
   * Dimensionality of the embedding vectors produced by the model.
   * This critical technical parameter determines the storage and processing requirements
   * for vector representations.
   *
   * Most Common Dimensions:
   *
   * 384 dimensions: Good for lightweight applications
   * 768 dimensions: Standard for many BERT-based models
   * 1,024 dimensions: High-performance models
   * 1,536 dimensions: Ultra-high performance models
   *
   */
  vectorSize: Scalars['Int'];
};

export type EnrichItem = {
  __typename?: 'EnrichItem';
  behaviorMergeType?: Maybe<BehaviorMergeType>;
  behaviorOnError?: Maybe<BehaviorOnError>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  jsonPath?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  requestTimeout?: Maybe<Scalars['BigInteger']>;
  script?: Maybe<Scalars['String']>;
  serviceName?: Maybe<Scalars['String']>;
  type?: Maybe<EnrichItemType>;
};

export type EnrichItemDtoInput = {
  behaviorMergeType: BehaviorMergeType;
  behaviorOnError: BehaviorOnError;
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  jsonPath: Scalars['String'];
  name: Scalars['String'];
  requestTimeout: Scalars['BigInteger'];
  script?: InputMaybe<Scalars['String']>;
  serviceName: Scalars['String'];
  type: EnrichItemType;
};

export enum EnrichItemType {
  GroovyScript = 'GROOVY_SCRIPT',
  HttpAsync = 'HTTP_ASYNC',
  HttpSync = 'HTTP_SYNC'
}

export type EnrichPipeline = {
  __typename?: 'EnrichPipeline';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  enrichItems?: Maybe<Connection_EnrichItem>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
};


export type EnrichPipelineEnrichItemsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  not?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type EnrichPipelineDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
};

export type Event = {
  __typename?: 'Event';
  className?: Maybe<Scalars['String']>;
  classPK?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  created?: Maybe<Scalars['DateTime']>;
  data?: Maybe<Scalars['String']>;
  groupKey?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  parsingDate?: Maybe<Scalars['DateTime']>;
  size?: Maybe<Scalars['Int']>;
  type?: Maybe<Scalars['String']>;
  version?: Maybe<Scalars['Int']>;
};

export type EventOption = {
  __typename?: 'EventOption';
  className?: Maybe<Scalars['String']>;
  classPK?: Maybe<Scalars['String']>;
  groupKey?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
};

export enum EventSortable {
  ClassName = 'CLASS_NAME',
  ClassPk = 'CLASS_PK',
  Created = 'CREATED',
  GroupKey = 'GROUP_KEY',
  ParsingDate = 'PARSING_DATE',
  Size = 'SIZE',
  Type = 'TYPE',
  Version = 'VERSION'
}

export enum FieldType {
  AnnotatedText = 'ANNOTATED_TEXT',
  Binary = 'BINARY',
  Boolean = 'BOOLEAN',
  Byte = 'BYTE',
  Completion = 'COMPLETION',
  ConstantKeyword = 'CONSTANT_KEYWORD',
  Date = 'DATE',
  DateNanos = 'DATE_NANOS',
  DateRange = 'DATE_RANGE',
  DenseVector = 'DENSE_VECTOR',
  Double = 'DOUBLE',
  DoubleRange = 'DOUBLE_RANGE',
  Flattened = 'FLATTENED',
  Float = 'FLOAT',
  GeoPoint = 'GEO_POINT',
  GeoShape = 'GEO_SHAPE',
  HalfFloat = 'HALF_FLOAT',
  Histogram = 'HISTOGRAM',
  I18N = 'I18N',
  Integer = 'INTEGER',
  Ip = 'IP',
  IpRange = 'IP_RANGE',
  Join = 'JOIN',
  Keyword = 'KEYWORD',
  KnnVector = 'KNN_VECTOR',
  Long = 'LONG',
  LongRange = 'LONG_RANGE',
  Murmur3 = 'MURMUR3',
  Nested = 'NESTED',
  Null = 'NULL',
  Object = 'OBJECT',
  Percolator = 'PERCOLATOR',
  Point = 'POINT',
  RankFeature = 'RANK_FEATURE',
  RankFeatures = 'RANK_FEATURES',
  ScaledFloat = 'SCALED_FLOAT',
  SearchAsYouType = 'SEARCH_AS_YOU_TYPE',
  Shape = 'SHAPE',
  Short = 'SHORT',
  SparseVector = 'SPARSE_VECTOR',
  Text = 'TEXT',
  TokenCount = 'TOKEN_COUNT',
  UnsignedLong = 'UNSIGNED_LONG',
  Version = 'VERSION',
  Wildcard = 'WILDCARD'
}

export type FieldValidator = {
  __typename?: 'FieldValidator';
  field?: Maybe<Scalars['String']>;
  message?: Maybe<Scalars['String']>;
};

export type FilterFieldInput = {
  fieldName?: InputMaybe<Scalars['String']>;
  not?: InputMaybe<Scalars['Boolean']>;
  operator?: InputMaybe<Operator>;
  value?: InputMaybe<Scalars['String']>;
};

export type FilterInput = {
  andOperator?: InputMaybe<Scalars['Boolean']>;
  filterFields?: InputMaybe<Array<InputMaybe<FilterFieldInput>>>;
};

export enum Fuzziness {
  Auto = 'AUTO',
  One = 'ONE',
  Two = 'TWO',
  Zero = 'ZERO'
}

export type ItemDtoInput = {
  enrichItemId: Scalars['BigInteger'];
  weight: Scalars['Float'];
};

export enum K9Column {
  CreateDate = 'createDate',
  Description = 'description',
  Id = 'id',
  ModifiedDate = 'modifiedDate',
  Name = 'name'
}

export type Language = {
  __typename?: 'Language';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  value?: Maybe<Scalars['String']>;
};

export type LanguageDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  value?: InputMaybe<Scalars['String']>;
};

export type LargeLanguageModel = {
  __typename?: 'LargeLanguageModel';
  apiKey?: Maybe<Scalars['String']>;
  apiUrl?: Maybe<Scalars['String']>;
  contextWindow?: Maybe<Scalars['Int']>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  enabled: Scalars['Boolean'];
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  providerModel?: Maybe<ProviderModel>;
  retrieveCitations?: Maybe<Scalars['Boolean']>;
};

export type LargeLanguageModelDtoInput = {
  /** It is the API key that you have to provide in order to make the authentication. */
  apiKey?: InputMaybe<Scalars['String']>;
  /** It is the API url of the model that you want to use. */
  apiUrl: Scalars['String'];
  /** It is the context window size. */
  contextWindow?: InputMaybe<Scalars['Int']>;
  description?: InputMaybe<Scalars['String']>;
  /** It is a JSON that can be used to add additional configurations to the LargeLanguageModel. */
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  providerModel: ProviderModelDtoInput;
  /** It indicates whether the LargeLanguageModel retrieves citations. */
  retrieveCitations?: InputMaybe<Scalars['Boolean']>;
};

/** Mutation root */
export type Mutation = {
  __typename?: 'Mutation';
  addAnnotatorToQueryAnalysis?: Maybe<Tuple2_QueryAnalysis_Annotator>;
  addCharFilterToAnalyzer?: Maybe<Tuple2_Analyzer_CharFilter>;
  addDatasourceToBucket?: Maybe<Tuple2_Bucket_Datasource>;
  addDocTypeFieldToPluginDriver?: Maybe<Tuple2_PluginDriver_DocTypeField>;
  addDocTypeFieldToSuggestionCategory?: Maybe<Tuple2_SuggestionCategory_DocTypeField>;
  addDocTypeFieldTranslation?: Maybe<Tuple2_String_String>;
  addDocTypeToDataIndex?: Maybe<Tuple2_DataIndex_DocType>;
  addEnrichItemToEnrichPipeline?: Maybe<Tuple2_EnrichPipeline_EnrichItem>;
  addLanguageToBucket?: Maybe<Tuple2_Bucket_Language>;
  addRuleToQueryAnalysis?: Maybe<Tuple2_QueryAnalysis_Rule>;
  addSortingToBucket?: Maybe<Tuple2_Bucket_Sorting>;
  addSortingToTab?: Maybe<Tuple2_Tab_Sorting>;
  addSortingTranslation?: Maybe<Tuple2_String_String>;
  addSuggestionCategoryToBucket?: Maybe<Tuple2_Bucket_SuggestionCategory>;
  addSuggestionCategoryTranslation?: Maybe<Tuple2_String_String>;
  addTabToBucket?: Maybe<Tuple2_Bucket_Tab>;
  addTabTranslation?: Maybe<Tuple2_String_String>;
  addTokenFilterToAnalyzer?: Maybe<Tuple2_Analyzer_TokenFilter>;
  addTokenTabToTab?: Maybe<Tuple2_Tab_TokenTab>;
  analyzer?: Maybe<Response_Analyzer>;
  analyzerWithLists?: Maybe<Response_Analyzer>;
  annotator?: Maybe<Response_Annotator>;
  annotatorWithDocTypeField?: Maybe<Response_Annotator>;
  bindAnalyzerToDocTypeField?: Maybe<Tuple2_DocTypeField_Analyzer>;
  bindAnnotatorToDocTypeField?: Maybe<Tuple2_Annotator_DocTypeField>;
  bindDataIndexToDatasource?: Maybe<Tuple2_Datasource_DataIndex>;
  bindDocTypeFieldToSorting?: Maybe<Tuple2_Sorting_DocTypeField>;
  bindDocTypeFieldToTokenTab?: Maybe<Tuple2_TokenTab_DocTypeField>;
  bindDocTypeToDocTypeTemplate?: Maybe<Tuple2_DocType_DocTypeTemplate>;
  bindEnrichPipelineToDatasource?: Maybe<Tuple2_Datasource_EnrichPipeline>;
  bindLanguageToBucket?: Maybe<Tuple2_Bucket_Language>;
  bindPluginDriverToDatasource?: Maybe<Tuple2_Datasource_PluginDriver>;
  bindQueryAnalysisToBucket?: Maybe<Tuple2_Bucket_QueryAnalysis>;
  /**
   * Binds an existing RAGConfiguration to a specified Bucket.
   *
   * This mutation links a RAGConfiguration to a Bucket.
   * It use the field RAGType of RAGConfiguration to specify the type of binding.
   *
   * Arguments:
   * - `bucketId` (ID!): The ID of the Bucket to bind the RAGConfiguration to.
   * - `ragConfigurationId` (ID!): The ID of the RAGConfiguration to be bound.
   *
   * Returns:
   * - A tuple containing:
   *   - `bucket`: The updated Bucket with the linked RAGConfiguration.
   *   - `ragConfiguration`: The linked RAGConfiguration.
   *
   */
  bindRAGConfigurationToBucket?: Maybe<Tuple2_Bucket_RagConfiguration>;
  bindSearchConfigToBucket?: Maybe<Tuple2_Bucket_SearchConfig>;
  bindTokenizerToAnalyzer?: Maybe<Tuple2_Analyzer_Tokenizer>;
  bucket?: Maybe<Response_Bucket>;
  bucketWithLists?: Maybe<Response_Bucket>;
  charFilter?: Maybe<Response_CharFilter>;
  createDatasourceAndAddPluginDriver?: Maybe<Tuple2_Datasource_PluginDriver>;
  createDatasourceConnection?: Maybe<Response_Datasource>;
  /**
   * Create a RAGConfiguration entity based on the provided input.
   *
   * Arguments:
   * - `createRAGConfigurationDTO` (RAGConfigurationDTO!): The input object with data for creation or update.
   *
   * Returns:
   * - The RAGConfiguration entity created.
   *
   */
  createRAGConfiguration?: Maybe<Response_RagConfiguration>;
  createSubField?: Maybe<Response_DocTypeField>;
  dataIndex?: Maybe<Response_DataIndex>;
  datasource?: Maybe<Response_Datasource>;
  deleteAnalyzer?: Maybe<Analyzer>;
  deleteAnnotator?: Maybe<Annotator>;
  deleteBucket?: Maybe<Bucket>;
  deleteCharFilter?: Maybe<CharFilter>;
  deleteDataIndex?: Maybe<DataIndex>;
  deleteDatasource?: Maybe<Datasource>;
  /**
   * Deletes a DocType entity by its ID after validating the provided name matches the entity.
   * Requires both the docTypeId and docTypeName (as a confirmation mechanism) to prevent
   * accidental deletions.
   *
   */
  deleteDocType?: Maybe<DocType>;
  deleteDocTypeFieldTranslation?: Maybe<Tuple2_String_String>;
  deleteDocTypeTemplate?: Maybe<DocTypeTemplate>;
  deleteEmbeddingModel?: Maybe<EmbeddingModel>;
  deleteEnrichItem?: Maybe<EnrichItem>;
  deleteEnrichPipeline?: Maybe<EnrichPipeline>;
  deleteLanguage?: Maybe<Language>;
  deleteLargeLanguageModel?: Maybe<LargeLanguageModel>;
  deletePluginDriver?: Maybe<PluginDriver>;
  deleteQueryAnalysis?: Maybe<QueryAnalysis>;
  deleteRAGConfiguration?: Maybe<RagConfiguration>;
  deleteRule?: Maybe<Rule>;
  deleteSearchConfig?: Maybe<SearchConfig>;
  deleteSortingTranslation?: Maybe<Tuple2_String_String>;
  deleteSuggestionCategory?: Maybe<SuggestionCategory>;
  deleteSuggestionCategoryTranslation?: Maybe<Tuple2_String_String>;
  deleteTab?: Maybe<Tab>;
  deleteTabTranslation?: Maybe<Tuple2_String_String>;
  deleteTokenFilter?: Maybe<TokenFilter>;
  deleteTokenTab?: Maybe<TokenTab>;
  deleteTokenizer?: Maybe<Tokenizer>;
  docType?: Maybe<Response_DocType>;
  docTypeField?: Maybe<Response_DocTypeField>;
  docTypeFieldWithAnalyzer?: Maybe<Response_DocTypeField>;
  docTypeTemplate?: Maybe<Response_DocTypeTemplate>;
  docTypeWithTemplate?: Maybe<Response_DocType>;
  embeddingModel?: Maybe<Response_EmbeddingModel>;
  enableBucket?: Maybe<Bucket>;
  enableEmbeddingModel?: Maybe<EmbeddingModel>;
  enableLargeLanguageModel?: Maybe<LargeLanguageModel>;
  enrichItem?: Maybe<Response_EnrichItem>;
  enrichPipeline?: Maybe<Response_EnrichPipeline>;
  enrichPipelineWithEnrichItems?: Maybe<Response_EnrichPipeline>;
  language?: Maybe<Response_Language>;
  largeLanguageModel?: Maybe<Response_LargeLanguageModel>;
  multiSelect?: Maybe<SuggestionCategory>;
  pluginDriver?: Maybe<Response_PluginDriver>;
  pluginDriverWithDocType?: Maybe<Response_PluginDriver>;
  queryAnalysis?: Maybe<Response_QueryAnalysis>;
  queryAnalysisWithLists?: Maybe<Response_QueryAnalysis>;
  queryParserConfig?: Maybe<Response_QueryParserConfig>;
  removeAnnotatorFromQueryAnalysis?: Maybe<Tuple2_QueryAnalysis_Annotator>;
  removeCharFilterFromAnalyzer?: Maybe<Tuple2_Analyzer_CharFilter>;
  removeCharFilterListFromAnalyzer?: Maybe<Analyzer>;
  removeDatasourceFromBucket?: Maybe<Tuple2_Bucket_Datasource>;
  removeDocTypeField?: Maybe<Tuple2_DocType_BigInteger>;
  removeDocTypeFieldFromPluginDriver?: Maybe<Tuple2_PluginDriver_DocTypeField>;
  /** This mutation is deprecated. Use `unbindDocTypeFieldFromSuggestionCategory` instead. Deprecation introduced in version 3.0.0. No significant impacts from usage, but transitioning to the new method is recommended. */
  removeDocTypeFieldFromSuggestionCategory?: Maybe<Tuple2_SuggestionCategory_DocTypeField>;
  removeDocTypeFromDataIndex?: Maybe<Tuple2_DataIndex_DocType>;
  removeEnrichItemFromEnrichPipeline?: Maybe<Tuple2_EnrichPipeline_EnrichItem>;
  removeLanguageFromBucket?: Maybe<Tuple2_Bucket_Language>;
  removeQueryParserConfig?: Maybe<Tuple2_SearchConfig_BigInteger>;
  removeRuleFromQueryAnalysis?: Maybe<Tuple2_QueryAnalysis_Rule>;
  removeSortingFromBucket?: Maybe<Tuple2_Bucket_Sorting>;
  removeSortingToTab?: Maybe<Tuple2_Tab_Sorting>;
  removeSuggestionCategoryFromBucket?: Maybe<Tuple2_Bucket_SuggestionCategory>;
  removeTabFromBucket?: Maybe<Tuple2_Bucket_Tab>;
  removeTokenFilterFromAnalyzer?: Maybe<Tuple2_Analyzer_TokenFilter>;
  removeTokenFilterListFromAnalyzer?: Maybe<Analyzer>;
  removeTokenTabToTab?: Maybe<Tuple2_Tab_TokenTab>;
  rule?: Maybe<Response_Rule>;
  searchConfig?: Maybe<Response_SearchConfig>;
  searchConfigWithQueryParsers?: Maybe<Response_SearchConfig>;
  sortEnrichItems?: Maybe<EnrichPipeline>;
  sorting?: Maybe<Response_Sorting>;
  suggestionCategory?: Maybe<Response_SuggestionCategory>;
  suggestionCategoryWithDocTypeField?: Maybe<Response_SuggestionCategory>;
  tab?: Maybe<Response_Tab>;
  tabWithTokenTabs?: Maybe<Response_Tab>;
  tokenFilter?: Maybe<Response_TokenFilter>;
  tokenTab?: Maybe<Response_TokenTab>;
  /** API to create, patch or update tokenTab with the possibility to link a docTypeField  */
  tokenTabWithDocTypeField?: Maybe<Response_TokenTab>;
  tokenizer?: Maybe<Response_Tokenizer>;
  unbindAnalyzerFromDocTypeField?: Maybe<Tuple2_DocTypeField_Analyzer>;
  unbindAnnotatorFromDocTypeField?: Maybe<Tuple2_Annotator_DocTypeField>;
  unbindDataIndexFromDatasource?: Maybe<Datasource>;
  unbindDocTypeFieldFromSorting?: Maybe<Tuple2_Sorting_DocTypeField>;
  /** This mutation replaces `removeDocTypeFieldFromSuggestionCategory`. It does not require the `docTypeFieldId` parameter and provides a more efficient implementation. */
  unbindDocTypeFieldFromSuggestionCategory?: Maybe<SuggestionCategory>;
  unbindDocTypeFieldFromTokenTab?: Maybe<Tuple2_TokenTab_DocTypeField>;
  unbindDocTypeTemplateFromDocType?: Maybe<DocType>;
  unbindEnrichPipelineToDatasource?: Maybe<Datasource>;
  unbindLanguageFromBucket?: Maybe<Tuple2_Bucket_Language>;
  unbindPluginDriverToDatasource?: Maybe<Datasource>;
  unbindQueryAnalysisFromBucket?: Maybe<Tuple2_Bucket_QueryAnalysis>;
  /**
   * Unbinds the RAGConfiguration from a specified Bucket according to the provided ragType.
   *
   * This mutation removes the link between a RAGConfiguration and a Bucket.
   * It uses the ragType argument to specify the type of binding to remove.
   *
   * Arguments:
   * - `bucketId` (ID!): The ID of the Bucket from which the RAGConfiguration will be unbound.
   * - `ragType` (RAGType!): The type of binding to remove.
   *
   * Returns:
   * - A tuple containing:
   *   - `bucket`: The updated Bucket after unbinding the RAGConfiguration.
   *   - `ragConfiguration`: Always null.
   *
   */
  unbindRAGConfigurationFromBucket?: Maybe<Tuple2_Bucket_RagConfiguration>;
  unbindSearchConfigFromBucket?: Maybe<Tuple2_Bucket_SearchConfig>;
  unbindTokenizerFromAnalyzer?: Maybe<Tuple2_Analyzer_Tokenizer>;
  updateDatasourceConnection?: Maybe<Response_Datasource>;
  /**
   * Update or patch a RAGConfiguration entity based on the provided input.
   *
   * Arguments:
   * - `id` (Long): The ID of the RAGConfiguration to update.
   * - `ragConfigurationDTO` (RAGConfigurationDTO!): The input object with data for creation or update.
   * - `patch` (Boolean): Whether to perform a partial update. Defaults to false.
   *
   * Returns:
   * - The RAGConfiguration entity created.
   *
   */
  updateRAGConfiguration?: Maybe<Response_RagConfiguration>;
  userField?: Maybe<AclMapping>;
};


/** Mutation root */
export type MutationAddAnnotatorToQueryAnalysisArgs = {
  annotatorId: Scalars['ID'];
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationAddCharFilterToAnalyzerArgs = {
  charFilterId: Scalars['ID'];
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationAddDatasourceToBucketArgs = {
  bucketId: Scalars['ID'];
  datasourceId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddDocTypeFieldToPluginDriverArgs = {
  docTypeFieldId: Scalars['ID'];
  pluginDriverId: Scalars['ID'];
  userField?: InputMaybe<UserField>;
};


/** Mutation root */
export type MutationAddDocTypeFieldToSuggestionCategoryArgs = {
  docTypeFieldId: Scalars['ID'];
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddDocTypeFieldTranslationArgs = {
  docTypeFieldId: Scalars['ID'];
  key?: InputMaybe<Scalars['String']>;
  language?: InputMaybe<Scalars['String']>;
  value?: InputMaybe<Scalars['String']>;
};


/** Mutation root */
export type MutationAddDocTypeToDataIndexArgs = {
  dataIndexId: Scalars['ID'];
  docTypeId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddEnrichItemToEnrichPipelineArgs = {
  enrichItemId: Scalars['ID'];
  enrichPipelineId: Scalars['ID'];
  tail?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationAddLanguageToBucketArgs = {
  bucketId: Scalars['ID'];
  languageId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddRuleToQueryAnalysisArgs = {
  id: Scalars['ID'];
  ruleId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddSortingToBucketArgs = {
  id: Scalars['ID'];
  sortingId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddSortingToTabArgs = {
  id: Scalars['ID'];
  sortingId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddSortingTranslationArgs = {
  key?: InputMaybe<Scalars['String']>;
  language?: InputMaybe<Scalars['String']>;
  sortingId: Scalars['ID'];
  value?: InputMaybe<Scalars['String']>;
};


/** Mutation root */
export type MutationAddSuggestionCategoryToBucketArgs = {
  bucketId: Scalars['ID'];
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddSuggestionCategoryTranslationArgs = {
  key?: InputMaybe<Scalars['String']>;
  language?: InputMaybe<Scalars['String']>;
  suggestionCategoryId: Scalars['ID'];
  value?: InputMaybe<Scalars['String']>;
};


/** Mutation root */
export type MutationAddTabToBucketArgs = {
  id: Scalars['ID'];
  tabId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddTabTranslationArgs = {
  key?: InputMaybe<Scalars['String']>;
  language?: InputMaybe<Scalars['String']>;
  tabId: Scalars['ID'];
  value?: InputMaybe<Scalars['String']>;
};


/** Mutation root */
export type MutationAddTokenFilterToAnalyzerArgs = {
  id: Scalars['ID'];
  tokenFilterId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddTokenTabToTabArgs = {
  id: Scalars['ID'];
  tokenTabId: Scalars['ID'];
};


/** Mutation root */
export type MutationAnalyzerArgs = {
  analyzerDTO?: InputMaybe<AnalyzerDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationAnalyzerWithListsArgs = {
  analyzerWithListsDTO?: InputMaybe<AnalyzerWithListsDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationAnnotatorArgs = {
  annotatorDTO?: InputMaybe<AnnotatorDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationAnnotatorWithDocTypeFieldArgs = {
  annotatorDTO?: InputMaybe<AnnotatorWithDocTypeFieldDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationBindAnalyzerToDocTypeFieldArgs = {
  analyzerId: Scalars['ID'];
  docTypeFieldId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindAnnotatorToDocTypeFieldArgs = {
  docTypeFieldId: Scalars['ID'];
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationBindDataIndexToDatasourceArgs = {
  dataIndexId: Scalars['ID'];
  datasourceId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindDocTypeFieldToSortingArgs = {
  docTypeFieldId: Scalars['ID'];
  sortingId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindDocTypeFieldToTokenTabArgs = {
  docTypeFieldId: Scalars['ID'];
  tokenTabId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindDocTypeToDocTypeTemplateArgs = {
  docTypeId: Scalars['ID'];
  docTypeTemplateId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindEnrichPipelineToDatasourceArgs = {
  datasourceId: Scalars['ID'];
  enrichPipelineId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindLanguageToBucketArgs = {
  bucketId: Scalars['ID'];
  languageId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindPluginDriverToDatasourceArgs = {
  datasourceId: Scalars['ID'];
  pluginDriverId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindQueryAnalysisToBucketArgs = {
  bucketId: Scalars['ID'];
  queryAnalysisId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindRagConfigurationToBucketArgs = {
  bucketId: Scalars['ID'];
  ragConfigurationId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindSearchConfigToBucketArgs = {
  bucketId: Scalars['ID'];
  searchConfigId: Scalars['ID'];
};


/** Mutation root */
export type MutationBindTokenizerToAnalyzerArgs = {
  analyzerId: Scalars['ID'];
  tokenizerId: Scalars['ID'];
};


/** Mutation root */
export type MutationBucketArgs = {
  bucketDTO?: InputMaybe<BucketDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationBucketWithListsArgs = {
  bucketWithListsDTO?: InputMaybe<BucketWithListsDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationCharFilterArgs = {
  charFilterDTO?: InputMaybe<CharFilterDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationCreateDatasourceAndAddPluginDriverArgs = {
  datasourceDTO?: InputMaybe<DatasourceDtoInput>;
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationCreateDatasourceConnectionArgs = {
  datasourceConnection?: InputMaybe<CreateDatasourceDtoInput>;
};


/** Mutation root */
export type MutationCreateRagConfigurationArgs = {
  createRAGConfigurationDTO?: InputMaybe<CreateRagConfigurationDtoInput>;
};


/** Mutation root */
export type MutationCreateSubFieldArgs = {
  docTypeFieldDTO?: InputMaybe<DocTypeFieldDtoInput>;
  parentDocTypeFieldId: Scalars['ID'];
};


/** Mutation root */
export type MutationDataIndexArgs = {
  dataIndexDTO?: InputMaybe<DataIndexDtoInput>;
  datasourceId: Scalars['ID'];
};


/** Mutation root */
export type MutationDatasourceArgs = {
  datasourceDTO?: InputMaybe<DatasourceDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationDeleteAnalyzerArgs = {
  analyzerId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteAnnotatorArgs = {
  annotatorId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteBucketArgs = {
  bucketId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteCharFilterArgs = {
  charFilterId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteDataIndexArgs = {
  dataIndexId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteDatasourceArgs = {
  datasourceId: Scalars['ID'];
  datasourceName?: InputMaybe<Scalars['String']>;
};


/** Mutation root */
export type MutationDeleteDocTypeArgs = {
  docTypeId: Scalars['ID'];
  docTypeName?: InputMaybe<Scalars['String']>;
};


/** Mutation root */
export type MutationDeleteDocTypeFieldTranslationArgs = {
  docTypeFieldId: Scalars['ID'];
  key?: InputMaybe<Scalars['String']>;
  language?: InputMaybe<Scalars['String']>;
};


/** Mutation root */
export type MutationDeleteDocTypeTemplateArgs = {
  docTypeTemplateId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteEmbeddingModelArgs = {
  embeddingModelId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteEnrichItemArgs = {
  enrichItemId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteEnrichPipelineArgs = {
  enrichPipelineId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteLanguageArgs = {
  languageId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteLargeLanguageModelArgs = {
  largeLanguageModelId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeletePluginDriverArgs = {
  pluginDriverId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteQueryAnalysisArgs = {
  queryAnalysisId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteRagConfigurationArgs = {
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteRuleArgs = {
  ruleId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteSearchConfigArgs = {
  searchConfigId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteSortingTranslationArgs = {
  key?: InputMaybe<Scalars['String']>;
  language?: InputMaybe<Scalars['String']>;
  sortingId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteSuggestionCategoryArgs = {
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteSuggestionCategoryTranslationArgs = {
  key?: InputMaybe<Scalars['String']>;
  language?: InputMaybe<Scalars['String']>;
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteTabArgs = {
  tabId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteTabTranslationArgs = {
  key?: InputMaybe<Scalars['String']>;
  language?: InputMaybe<Scalars['String']>;
  tabId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteTokenFilterArgs = {
  tokenFilterId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteTokenTabArgs = {
  tokenTabId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteTokenizerArgs = {
  tokenizerId: Scalars['ID'];
};


/** Mutation root */
export type MutationDocTypeArgs = {
  docTypeDTO?: InputMaybe<DocTypeDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationDocTypeFieldArgs = {
  docTypeFieldDTO?: InputMaybe<DocTypeFieldDtoInput>;
  docTypeFieldId?: InputMaybe<Scalars['ID']>;
  docTypeId: Scalars['ID'];
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationDocTypeFieldWithAnalyzerArgs = {
  docTypeFieldId?: InputMaybe<Scalars['ID']>;
  docTypeFieldWithAnalyzerDTO?: InputMaybe<DocTypeFieldWithAnalyzerDtoInput>;
  docTypeId: Scalars['ID'];
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationDocTypeTemplateArgs = {
  docTypeTemplateDTO?: InputMaybe<DocTypeTemplateDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationDocTypeWithTemplateArgs = {
  docTypeWithTemplateDTO?: InputMaybe<DocTypeWithTemplateDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationEmbeddingModelArgs = {
  embeddingModelDTO?: InputMaybe<EmbeddingModelDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationEnableBucketArgs = {
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationEnableEmbeddingModelArgs = {
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationEnableLargeLanguageModelArgs = {
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationEnrichItemArgs = {
  enrichItemDTO?: InputMaybe<EnrichItemDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationEnrichPipelineArgs = {
  enrichPipelineDTO?: InputMaybe<EnrichPipelineDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationEnrichPipelineWithEnrichItemsArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  pipelineWithItemsDTO?: InputMaybe<PipelineWithItemsDtoInput>;
};


/** Mutation root */
export type MutationLanguageArgs = {
  id?: InputMaybe<Scalars['ID']>;
  languageDTO?: InputMaybe<LanguageDtoInput>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationLargeLanguageModelArgs = {
  id?: InputMaybe<Scalars['ID']>;
  largeLanguageModelDTO?: InputMaybe<LargeLanguageModelDtoInput>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationMultiSelectArgs = {
  multiSelect: Scalars['Boolean'];
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationPluginDriverArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  pluginDriverDTO?: InputMaybe<PluginDriverDtoInput>;
};


/** Mutation root */
export type MutationPluginDriverWithDocTypeArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  pluginWithDocTypeDTO?: InputMaybe<PluginWithDocTypeDtoInput>;
};


/** Mutation root */
export type MutationQueryAnalysisArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  queryAnalysisDTO?: InputMaybe<QueryAnalysisDtoInput>;
};


/** Mutation root */
export type MutationQueryAnalysisWithListsArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  queryAnalysisWithListsDTO?: InputMaybe<QueryAnalysisWithListsDtoInput>;
};


/** Mutation root */
export type MutationQueryParserConfigArgs = {
  patch?: InputMaybe<Scalars['Boolean']>;
  queryParserConfigDTO?: InputMaybe<QueryParserConfigDtoInput>;
  queryParserConfigId?: InputMaybe<Scalars['ID']>;
  searchConfigId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveAnnotatorFromQueryAnalysisArgs = {
  annotatorId: Scalars['ID'];
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveCharFilterFromAnalyzerArgs = {
  charFilterId: Scalars['ID'];
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveCharFilterListFromAnalyzerArgs = {
  analyzerId: Scalars['BigInteger'];
};


/** Mutation root */
export type MutationRemoveDatasourceFromBucketArgs = {
  bucketId: Scalars['ID'];
  datasourceId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveDocTypeFieldArgs = {
  docTypeFieldId: Scalars['ID'];
  docTypeId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveDocTypeFieldFromPluginDriverArgs = {
  docTypeFieldId: Scalars['ID'];
  pluginDriverId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveDocTypeFieldFromSuggestionCategoryArgs = {
  docTypeFieldId: Scalars['ID'];
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveDocTypeFromDataIndexArgs = {
  dataIndexId: Scalars['ID'];
  docTypeId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveEnrichItemFromEnrichPipelineArgs = {
  enrichItemId: Scalars['ID'];
  enrichPipelineId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveLanguageFromBucketArgs = {
  bucketId: Scalars['ID'];
  languageId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveQueryParserConfigArgs = {
  queryParserConfigId: Scalars['ID'];
  searchConfigId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveRuleFromQueryAnalysisArgs = {
  id: Scalars['ID'];
  ruleId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveSortingFromBucketArgs = {
  id: Scalars['ID'];
  sortingId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveSortingToTabArgs = {
  id: Scalars['ID'];
  sortingId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveSuggestionCategoryFromBucketArgs = {
  bucketId: Scalars['ID'];
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveTabFromBucketArgs = {
  id: Scalars['ID'];
  tabId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveTokenFilterFromAnalyzerArgs = {
  id: Scalars['ID'];
  tokenFilterId: Scalars['ID'];
};


/** Mutation root */
export type MutationRemoveTokenFilterListFromAnalyzerArgs = {
  analyzerId: Scalars['BigInteger'];
};


/** Mutation root */
export type MutationRemoveTokenTabToTabArgs = {
  id: Scalars['ID'];
  tokenTabId: Scalars['ID'];
};


/** Mutation root */
export type MutationRuleArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  ruleDTO?: InputMaybe<RuleDtoInput>;
};


/** Mutation root */
export type MutationSearchConfigArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  searchConfigDTO?: InputMaybe<SearchConfigDtoInput>;
};


/** Mutation root */
export type MutationSearchConfigWithQueryParsersArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  searchConfigWithQueryParsersDTO?: InputMaybe<SearchConfigWithQueryParsersDtoInput>;
};


/** Mutation root */
export type MutationSortEnrichItemsArgs = {
  enrichItemIdList?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
  enrichPipelineId: Scalars['ID'];
};


/** Mutation root */
export type MutationSortingArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  sortingDTO?: InputMaybe<SortingDtoInput>;
};


/** Mutation root */
export type MutationSuggestionCategoryArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  suggestionCategoryDTO?: InputMaybe<SuggestionCategoryDtoInput>;
};


/** Mutation root */
export type MutationSuggestionCategoryWithDocTypeFieldArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  suggestionCategoryWithDocTypeFieldDTO?: InputMaybe<SuggestionCategoryWithDocTypeFieldDtoInput>;
};


/** Mutation root */
export type MutationTabArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  tabDTO?: InputMaybe<TabDtoInput>;
};


/** Mutation root */
export type MutationTabWithTokenTabsArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  tabWithTokenTabsDTO?: InputMaybe<TabWithTokenTabsDtoInput>;
};


/** Mutation root */
export type MutationTokenFilterArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  tokenFilterDTO?: InputMaybe<TokenFilterDtoInput>;
};


/** Mutation root */
export type MutationTokenTabArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  tokenTabDTO?: InputMaybe<TokenTabDtoInput>;
};


/** Mutation root */
export type MutationTokenTabWithDocTypeFieldArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  tokenTabWithDocTypeFieldDTO?: InputMaybe<TokenTabWithDocTypeFieldDtoInput>;
};


/** Mutation root */
export type MutationTokenizerArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  tokenizerDTO?: InputMaybe<TokenizerDtoInput>;
};


/** Mutation root */
export type MutationUnbindAnalyzerFromDocTypeFieldArgs = {
  docTypeFieldId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindAnnotatorFromDocTypeFieldArgs = {
  docTypeFieldId: Scalars['ID'];
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindDataIndexFromDatasourceArgs = {
  datasourceId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindDocTypeFieldFromSortingArgs = {
  docTypeFieldId: Scalars['ID'];
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindDocTypeFieldFromSuggestionCategoryArgs = {
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindDocTypeFieldFromTokenTabArgs = {
  docTypeFieldId: Scalars['ID'];
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindDocTypeTemplateFromDocTypeArgs = {
  docTypeId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindEnrichPipelineToDatasourceArgs = {
  datasourceId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindLanguageFromBucketArgs = {
  bucketId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindPluginDriverToDatasourceArgs = {
  datasourceId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindQueryAnalysisFromBucketArgs = {
  bucketId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindRagConfigurationFromBucketArgs = {
  bucketId: Scalars['ID'];
  ragType: RagType;
};


/** Mutation root */
export type MutationUnbindSearchConfigFromBucketArgs = {
  bucketId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindTokenizerFromAnalyzerArgs = {
  analyzerId: Scalars['ID'];
};


/** Mutation root */
export type MutationUpdateDatasourceConnectionArgs = {
  datasourceConnection?: InputMaybe<UpdateDatasourceDtoInput>;
};


/** Mutation root */
export type MutationUpdateRagConfigurationArgs = {
  id: Scalars['ID'];
  patch?: InputMaybe<Scalars['Boolean']>;
  ragConfigurationDTO?: InputMaybe<RagConfigurationDtoInput>;
};


/** Mutation root */
export type MutationUserFieldArgs = {
  docTypeFieldId: Scalars['ID'];
  pluginDriverId: Scalars['ID'];
  userField?: InputMaybe<UserField>;
};

export enum Operator {
  Contains = 'contains',
  EndsWith = 'endsWith',
  Equals = 'equals',
  GreaterThan = 'greaterThan',
  GreaterThanOrEqualTo = 'greaterThanOrEqualTo',
  LessThan = 'lessThan',
  LessThenOrEqualTo = 'lessThenOrEqualTo',
  StartsWith = 'startsWith'
}

/** Information about pagination in a connection. */
export type PageInfo = {
  /** When paginating forwards, the cursor to continue. */
  endCursor?: Maybe<Scalars['String']>;
  /** When paginating forwards, are there more items? */
  hasNextPage: Scalars['Boolean'];
  /** When paginating backwards, are there more items? */
  hasPreviousPage: Scalars['Boolean'];
  /** When paginating backwards, the cursor to continue. */
  startCursor?: Maybe<Scalars['String']>;
};

export type Page_PluginDriver = {
  __typename?: 'Page_PluginDriver';
  afterId: Scalars['BigInteger'];
  beforeId: Scalars['BigInteger'];
  content?: Maybe<Array<Maybe<PluginDriver>>>;
  count: Scalars['BigInteger'];
  limit: Scalars['Int'];
};

export type PageableInput = {
  afterId?: InputMaybe<Scalars['BigInteger']>;
  beforeId?: InputMaybe<Scalars['BigInteger']>;
  limit?: InputMaybe<Scalars['Int']>;
  sortBy?: InputMaybe<K9Column>;
};

export type PipelineWithItemsDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  items?: InputMaybe<Array<InputMaybe<ItemDtoInput>>>;
  name: Scalars['String'];
};

export type PluginDriver = {
  __typename?: 'PluginDriver';
  aclMappings?: Maybe<Array<Maybe<PluginDriverAclMapping>>>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  docTypeFields?: Maybe<Connection_DocTypeField>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  provisioning?: Maybe<Provisioning>;
  type?: Maybe<PluginDriverType>;
};


export type PluginDriverDocTypeFieldsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  not?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type PluginDriverAclMapping = {
  __typename?: 'PluginDriverAclMapping';
  docTypeField?: Maybe<DocTypeField>;
  userField?: Maybe<UserField>;
};

export type PluginDriverDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  provisioning?: InputMaybe<Provisioning>;
  type: PluginDriverType;
};

export type PluginDriverDocTypeFieldKey = {
  __typename?: 'PluginDriverDocTypeFieldKey';
  docTypeFieldId?: Maybe<Scalars['BigInteger']>;
  pluginDriverId?: Maybe<Scalars['BigInteger']>;
};

export enum PluginDriverType {
  Http = 'HTTP'
}

export type PluginWithDocTypeDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  docTypeUserDTOSet?: InputMaybe<Array<InputMaybe<DocTypeUserDtoInput>>>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  provisioning?: InputMaybe<Provisioning>;
  type: PluginDriverType;
};

export type ProviderModel = {
  __typename?: 'ProviderModel';
  model?: Maybe<Scalars['String']>;
  provider?: Maybe<Scalars['String']>;
};

export type ProviderModelDtoInput = {
  /** The specific model like: GPT-4, LLaMA 2, Mistral 7B. */
  model: Scalars['String'];
  /** The model provider like: OpenAI, Meta, Mistral. */
  provider: Scalars['String'];
};

export enum Provisioning {
  System = 'SYSTEM',
  User = 'USER'
}

/** Query root */
export type Query = {
  __typename?: 'Query';
  analyzer?: Maybe<Analyzer>;
  analyzers?: Maybe<Connection_Analyzer>;
  annotator?: Maybe<Annotator>;
  annotators?: Maybe<Connection_Annotator>;
  bucket?: Maybe<Bucket>;
  buckets?: Maybe<Connection_Bucket>;
  charFilter?: Maybe<CharFilter>;
  charFilters?: Maybe<Connection_CharFilter>;
  dataIndex?: Maybe<DataIndex>;
  dataIndices?: Maybe<Connection_DataIndex>;
  datasource?: Maybe<Datasource>;
  datasources?: Maybe<Connection_Datasource>;
  docType?: Maybe<DocType>;
  docTypeField?: Maybe<DocTypeField>;
  docTypeFieldNotInAnnotator?: Maybe<Connection_DocTypeField>;
  docTypeFields?: Maybe<Connection_DocTypeField>;
  docTypeFieldsByParent?: Maybe<Connection_DocTypeField>;
  docTypeFieldsFromDocType?: Maybe<Connection_DocTypeField>;
  docTypeFieldsFromDocTypeByParent?: Maybe<Connection_DocTypeField>;
  docTypeFieldsNotInSorting?: Maybe<Connection_DocTypeField>;
  docTypeFieldsNotInTokenTab?: Maybe<Connection_DocTypeField>;
  docTypeTemplate?: Maybe<DocTypeTemplate>;
  docTypeTemplates?: Maybe<Connection_DocTypeTemplate>;
  docTypes?: Maybe<Connection_DocType>;
  embeddingModel?: Maybe<EmbeddingModel>;
  embeddingModels?: Maybe<Connection_EmbeddingModel>;
  enabledBucket?: Maybe<Bucket>;
  enrichItem?: Maybe<EnrichItem>;
  enrichItems?: Maybe<Connection_EnrichItem>;
  enrichPipeline?: Maybe<EnrichPipeline>;
  enrichPipelines?: Maybe<Connection_EnrichPipeline>;
  /** Returns the list of events */
  event?: Maybe<Array<Maybe<Event>>>;
  /** Get event data */
  eventData?: Maybe<Scalars['String']>;
  /** Returns the list of available options for the event */
  eventOptions?: Maybe<Array<Maybe<EventOption>>>;
  language?: Maybe<Language>;
  languages?: Maybe<Connection_Language>;
  largeLanguageModel?: Maybe<LargeLanguageModel>;
  largeLanguageModels?: Maybe<Connection_LargeLanguageModel>;
  pluginDriver?: Maybe<PluginDriver>;
  pluginDrivers?: Maybe<Connection_PluginDriver>;
  pluginDriversPageFilter?: Maybe<Page_PluginDriver>;
  queryAnalyses?: Maybe<Connection_QueryAnalysis>;
  queryAnalysis?: Maybe<QueryAnalysis>;
  queryParserConfig?: Maybe<QueryParserConfig>;
  /** Retrieves all available form configurations from the system. */
  queryParserConfigFormConfigurations?: Maybe<Scalars['FormConfigurations']>;
  queryParserConfigs?: Maybe<Connection_QueryParserConfig>;
  ragConfiguration?: Maybe<RagConfiguration>;
  ragConfigurations?: Maybe<Connection_RagConfiguration>;
  rule?: Maybe<Rule>;
  rules?: Maybe<Connection_Rule>;
  scheduler?: Maybe<Scheduler>;
  schedulers?: Maybe<Connection_Scheduler>;
  searchConfig?: Maybe<SearchConfig>;
  searchConfigs?: Maybe<Connection_SearchConfig>;
  sorting?: Maybe<Sorting>;
  sortings?: Maybe<Connection_Sorting>;
  suggestionCategories?: Maybe<Connection_SuggestionCategory>;
  suggestionCategory?: Maybe<SuggestionCategory>;
  tab?: Maybe<Tab>;
  tabs?: Maybe<Connection_Tab>;
  tokenFilter?: Maybe<TokenFilter>;
  tokenFilters?: Maybe<Connection_TokenFilter>;
  tokenTab?: Maybe<TokenTab>;
  tokenTabs?: Maybe<Connection_TokenTab>;
  tokenizer?: Maybe<Tokenizer>;
  tokenizers?: Maybe<Connection_Tokenizer>;
  totalSortings?: Maybe<Connection_Sorting>;
  totalTokenTabs?: Maybe<Connection_TokenTab>;
  unboundAnalyzersByCharFilter?: Maybe<Array<Maybe<Analyzer>>>;
  unboundAnalyzersByTokenFilter?: Maybe<Array<Maybe<Analyzer>>>;
  unboundBucketsByDatasource?: Maybe<Array<Maybe<Bucket>>>;
  unboundBucketsBySuggestionCategory?: Maybe<Array<Maybe<Bucket>>>;
  unboundBucketsByTab?: Maybe<Array<Maybe<Bucket>>>;
  /** Fetches DocTypeFields unbound to the provided SuggestionCategory ID with FieldType KEYWORD or I18N. */
  unboundDocTypeFieldsBySuggestionCategory?: Maybe<Array<Maybe<DocTypeField>>>;
  unboundEnrichPipelines?: Maybe<Array<Maybe<EnrichPipeline>>>;
  /**
   * Retrieves a list of RAGConfiguration entities of the specified RAGType
   * that are not yet associated with the given Bucket.
   *
   * This query returns all RAGConfiguration instances that:
   * - Have the specified RAGType.
   * - Are not currently linked to the provided bucketId.
   *
   * Arguments:
   * - `bucketId` (ID!): The ID of the Bucket for which to retrieve unbound RAGConfiguration entities.
   * - `ragType` (RAGType!): The type of RAGConfiguration to filter by.
   *
   * Returns:
   * - A list of RAGConfiguration entities that match the criteria.
   *
   */
  unboundRAGConfigurationByBucket?: Maybe<Array<Maybe<RagConfiguration>>>;
  unboundTabsByTokenTab?: Maybe<Array<Maybe<Tab>>>;
};


/** Query root */
export type QueryAnalyzerArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryAnalyzersArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryAnnotatorArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryAnnotatorsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryBucketArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryBucketsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryCharFilterArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryCharFiltersArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryDataIndexArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryDataIndicesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryDatasourceArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryDatasourcesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryDocTypeArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryDocTypeFieldArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryDocTypeFieldNotInAnnotatorArgs = {
  after?: InputMaybe<Scalars['String']>;
  annotatorId: Scalars['ID'];
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryDocTypeFieldsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryDocTypeFieldsByParentArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  parentId: Scalars['BigInteger'];
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryDocTypeFieldsFromDocTypeArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  docTypeId: Scalars['ID'];
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryDocTypeFieldsFromDocTypeByParentArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  docTypeId: Scalars['ID'];
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  parentId: Scalars['BigInteger'];
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryDocTypeFieldsNotInSortingArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
  sortingId: Scalars['ID'];
};


/** Query root */
export type QueryDocTypeFieldsNotInTokenTabArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
  tokenTabId: Scalars['ID'];
};


/** Query root */
export type QueryDocTypeTemplateArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryDocTypeTemplatesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryDocTypesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryEmbeddingModelArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryEmbeddingModelsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryEnrichItemArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryEnrichItemsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryEnrichPipelineArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryEnrichPipelinesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryEventArgs = {
  className?: InputMaybe<Scalars['String']>;
  classPK?: InputMaybe<Scalars['String']>;
  from?: InputMaybe<Scalars['Int']>;
  groupKey?: InputMaybe<Scalars['String']>;
  gte?: InputMaybe<Scalars['DateTime']>;
  id?: InputMaybe<Scalars['String']>;
  lte?: InputMaybe<Scalars['DateTime']>;
  size?: InputMaybe<Scalars['Int']>;
  sortBy?: InputMaybe<EventSortable>;
  sortType?: InputMaybe<Scalars['String']>;
  type?: InputMaybe<Scalars['String']>;
};


/** Query root */
export type QueryEventDataArgs = {
  id?: InputMaybe<Scalars['String']>;
};


/** Query root */
export type QueryEventOptionsArgs = {
  from?: InputMaybe<Scalars['Int']>;
  size?: InputMaybe<Scalars['Int']>;
  sortType?: InputMaybe<Scalars['String']>;
  sortable?: InputMaybe<Scalars['Boolean']>;
};


/** Query root */
export type QueryLanguageArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryLanguagesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryLargeLanguageModelArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryLargeLanguageModelsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryPluginDriverArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryPluginDriversArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryPluginDriversPageFilterArgs = {
  filter?: InputMaybe<FilterInput>;
  pageable?: InputMaybe<PageableInput>;
};


/** Query root */
export type QueryQueryAnalysesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryQueryAnalysisArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryQueryParserConfigArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryQueryParserConfigsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchConfigId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryRagConfigurationArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryRagConfigurationsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryRuleArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryRulesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QuerySchedulerArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QuerySchedulersArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QuerySearchConfigArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QuerySearchConfigsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QuerySortingArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QuerySortingsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
  tabId: Scalars['ID'];
};


/** Query root */
export type QuerySuggestionCategoriesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QuerySuggestionCategoryArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryTabArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryTabsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryTokenFilterArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryTokenFiltersArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryTokenTabArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryTokenTabsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
  tabId: Scalars['ID'];
};


/** Query root */
export type QueryTokenizerArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryTokenizersArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryTotalSortingsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryTotalTokenTabsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryUnboundAnalyzersByCharFilterArgs = {
  charFilterId: Scalars['BigInteger'];
};


/** Query root */
export type QueryUnboundAnalyzersByTokenFilterArgs = {
  tokenFilterId: Scalars['BigInteger'];
};


/** Query root */
export type QueryUnboundBucketsByDatasourceArgs = {
  datasourceId: Scalars['BigInteger'];
};


/** Query root */
export type QueryUnboundBucketsBySuggestionCategoryArgs = {
  suggestionCategoryId: Scalars['BigInteger'];
};


/** Query root */
export type QueryUnboundBucketsByTabArgs = {
  tabId: Scalars['BigInteger'];
};


/** Query root */
export type QueryUnboundDocTypeFieldsBySuggestionCategoryArgs = {
  suggestionCategoryId: Scalars['BigInteger'];
};


/** Query root */
export type QueryUnboundEnrichPipelinesArgs = {
  itemId: Scalars['BigInteger'];
};


/** Query root */
export type QueryUnboundRagConfigurationByBucketArgs = {
  bucketId: Scalars['ID'];
  ragType: RagType;
};


/** Query root */
export type QueryUnboundTabsByTokenTabArgs = {
  tokenTabId: Scalars['BigInteger'];
};

export type QueryAnalysis = {
  __typename?: 'QueryAnalysis';
  annotators?: Maybe<Connection_Annotator>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  rules?: Maybe<Connection_Rule>;
  stopWords?: Maybe<Scalars['String']>;
  stopWordsList?: Maybe<Array<Maybe<Scalars['String']>>>;
};


export type QueryAnalysisAnnotatorsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


export type QueryAnalysisRulesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type QueryAnalysisDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  stopWords?: InputMaybe<Scalars['String']>;
};

export type QueryAnalysisWithListsDtoInput = {
  annotatorsIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  rulesIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
  stopWords?: InputMaybe<Scalars['String']>;
};

export type QueryParserConfig = {
  __typename?: 'QueryParserConfig';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  type?: Maybe<QueryParserType>;
};

export type QueryParserConfigDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  type: Scalars['String'];
};

export enum QueryParserType {
  Acl = 'ACL',
  Autocomplete = 'AUTOCOMPLETE',
  Datasource = 'DATASOURCE',
  Date = 'DATE',
  DateOrder = 'DATE_ORDER',
  Doctype = 'DOCTYPE',
  Entity = 'ENTITY',
  Filter = 'FILTER',
  Hybrid = 'HYBRID',
  Knn = 'KNN',
  Text = 'TEXT'
}

export type RagConfiguration = {
  __typename?: 'RAGConfiguration';
  chunkWindow?: Maybe<Scalars['Int']>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  prompt?: Maybe<Scalars['String']>;
  promptNoRag?: Maybe<Scalars['String']>;
  ragToolDescription?: Maybe<Scalars['String']>;
  reformulate?: Maybe<Scalars['Boolean']>;
  rephrasePrompt?: Maybe<Scalars['String']>;
  type?: Maybe<RagType>;
};

export type RagConfigurationDtoInput = {
  /**
   * Controls context window merging behavior for chunk processing:
   * 0: Disables chunk merging.
   * > 0: Enables merging with specified window size.
   *
   */
  chunkWindow?: InputMaybe<Scalars['Int']>;
  description?: InputMaybe<Scalars['String']>;
  /** A JSON that can be used to add additional configurations to the EmbeddingModel. */
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  /** Main prompt template used for RAG. */
  prompt?: InputMaybe<Scalars['String']>;
  /**
   * Prompt template used specifically in RAG-as-tool configurations when the RAG
   * tool is available but not invoked by the LLM.
   *
   */
  promptNoRag?: InputMaybe<Scalars['String']>;
  /**
   * Description of the RAG tool's capabilities, used in RAG-as-tool implementations
   * to help the LLM decide when to invoke it.
   *
   */
  ragToolDescription?: InputMaybe<Scalars['String']>;
  /**
   * Boolean flag that controls whether a large language model should reformulate
   * the input prompt before processing it using rephrasePrompt.
   *
   */
  reformulate?: InputMaybe<Scalars['Boolean']>;
  /** Prompt template used if reformulate is set to true. */
  rephrasePrompt?: InputMaybe<Scalars['String']>;
};

export enum RagType {
  ChatRag = 'CHAT_RAG',
  ChatRagTool = 'CHAT_RAG_TOOL',
  SimpleGenerate = 'SIMPLE_GENERATE'
}

export type Response_Analyzer = {
  __typename?: 'Response_Analyzer';
  entity?: Maybe<Analyzer>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_Annotator = {
  __typename?: 'Response_Annotator';
  entity?: Maybe<Annotator>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_Bucket = {
  __typename?: 'Response_Bucket';
  entity?: Maybe<Bucket>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_CharFilter = {
  __typename?: 'Response_CharFilter';
  entity?: Maybe<CharFilter>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_DataIndex = {
  __typename?: 'Response_DataIndex';
  entity?: Maybe<DataIndex>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_Datasource = {
  __typename?: 'Response_Datasource';
  entity?: Maybe<Datasource>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_DocType = {
  __typename?: 'Response_DocType';
  entity?: Maybe<DocType>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_DocTypeField = {
  __typename?: 'Response_DocTypeField';
  entity?: Maybe<DocTypeField>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_DocTypeTemplate = {
  __typename?: 'Response_DocTypeTemplate';
  entity?: Maybe<DocTypeTemplate>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_EmbeddingModel = {
  __typename?: 'Response_EmbeddingModel';
  entity?: Maybe<EmbeddingModel>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_EnrichItem = {
  __typename?: 'Response_EnrichItem';
  entity?: Maybe<EnrichItem>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_EnrichPipeline = {
  __typename?: 'Response_EnrichPipeline';
  entity?: Maybe<EnrichPipeline>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_Language = {
  __typename?: 'Response_Language';
  entity?: Maybe<Language>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_LargeLanguageModel = {
  __typename?: 'Response_LargeLanguageModel';
  entity?: Maybe<LargeLanguageModel>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_PluginDriver = {
  __typename?: 'Response_PluginDriver';
  entity?: Maybe<PluginDriver>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_QueryAnalysis = {
  __typename?: 'Response_QueryAnalysis';
  entity?: Maybe<QueryAnalysis>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_QueryParserConfig = {
  __typename?: 'Response_QueryParserConfig';
  entity?: Maybe<QueryParserConfig>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_RagConfiguration = {
  __typename?: 'Response_RAGConfiguration';
  entity?: Maybe<RagConfiguration>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_Rule = {
  __typename?: 'Response_Rule';
  entity?: Maybe<Rule>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_SearchConfig = {
  __typename?: 'Response_SearchConfig';
  entity?: Maybe<SearchConfig>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_Sorting = {
  __typename?: 'Response_Sorting';
  entity?: Maybe<Sorting>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_SuggestionCategory = {
  __typename?: 'Response_SuggestionCategory';
  entity?: Maybe<SuggestionCategory>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_Tab = {
  __typename?: 'Response_Tab';
  entity?: Maybe<Tab>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_TokenFilter = {
  __typename?: 'Response_TokenFilter';
  entity?: Maybe<TokenFilter>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_TokenTab = {
  __typename?: 'Response_TokenTab';
  entity?: Maybe<TokenTab>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type Response_Tokenizer = {
  __typename?: 'Response_Tokenizer';
  entity?: Maybe<Tokenizer>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export enum RetrieveType {
  Hybrid = 'HYBRID',
  Knn = 'KNN',
  Text = 'TEXT'
}

export type Rule = {
  __typename?: 'Rule';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  lhs?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  rhs?: Maybe<Scalars['String']>;
};

export type RuleDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  lhs: Scalars['String'];
  name: Scalars['String'];
  rhs: Scalars['String'];
};

export type Scheduler = {
  __typename?: 'Scheduler';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  dataIndex?: Maybe<DataIndex>;
  datasource?: Maybe<Datasource>;
  errorDescription?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  lastIngestionDate?: Maybe<Scalars['DateTime']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  newDataIndex?: Maybe<DataIndex>;
  oldDataIndex?: Maybe<DataIndex>;
  scheduleId?: Maybe<Scalars['String']>;
  status?: Maybe<SchedulerStatus>;
};

export enum SchedulerStatus {
  Cancelled = 'CANCELLED',
  Error = 'ERROR',
  Failure = 'FAILURE',
  Finished = 'FINISHED',
  Running = 'RUNNING',
  Stale = 'STALE'
}

export type SearchConfig = {
  __typename?: 'SearchConfig';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  minScore?: Maybe<Scalars['Float']>;
  minScoreSearch: Scalars['Boolean'];
  minScoreSuggestions: Scalars['Boolean'];
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  queryParserConfigs?: Maybe<Connection_QueryParserConfig>;
};


export type SearchConfigQueryParserConfigsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type SearchConfigDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  minScore: Scalars['Float'];
  minScoreSearch: Scalars['Boolean'];
  minScoreSuggestions: Scalars['Boolean'];
  name: Scalars['String'];
};

export type SearchConfigWithQueryParsersDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  minScore: Scalars['Float'];
  minScoreSearch: Scalars['Boolean'];
  minScoreSuggestions: Scalars['Boolean'];
  name: Scalars['String'];
  queryParsers?: InputMaybe<Array<InputMaybe<QueryParserConfigDtoInput>>>;
};

export type SearchTokenDto = {
  __typename?: 'SearchTokenDto';
  filter?: Maybe<Scalars['Boolean']>;
  keywordKey?: Maybe<Scalars['String']>;
  tokenType?: Maybe<Scalars['String']>;
  values?: Maybe<Array<Maybe<Scalars['String']>>>;
};

export type SortByInput = {
  column?: InputMaybe<Scalars['String']>;
  direction?: InputMaybe<Direction>;
};

export type Sorting = {
  __typename?: 'Sorting';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  defaultSort: Scalars['Boolean'];
  description?: Maybe<Scalars['String']>;
  docTypeField?: Maybe<DocTypeField>;
  docTypeFieldsNotInSorting?: Maybe<Connection_DocTypeField>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  priority?: Maybe<Scalars['Float']>;
  type?: Maybe<SortingType>;
};


export type SortingDocTypeFieldsNotInSortingArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type SortingDtoInput = {
  defaultSort: Scalars['Boolean'];
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  priority: Scalars['Float'];
  type: SortingType;
};

export enum SortingType {
  Asc = 'ASC',
  Desc = 'DESC'
}

/** Subscription root */
export type Subscription = {
  __typename?: 'Subscription';
  analyzerCreated?: Maybe<Analyzer>;
  analyzerDeleted?: Maybe<Analyzer>;
  analyzerUpdated?: Maybe<Analyzer>;
  annotatorCreated?: Maybe<Annotator>;
  annotatorDeleted?: Maybe<Annotator>;
  annotatorUpdated?: Maybe<Annotator>;
  bucketCreated?: Maybe<Bucket>;
  bucketDeleted?: Maybe<Bucket>;
  bucketUpdated?: Maybe<Bucket>;
  charFilterCreated?: Maybe<CharFilter>;
  charFilterDeleted?: Maybe<CharFilter>;
  charFilterUpdated?: Maybe<CharFilter>;
  dataIndexCreated?: Maybe<DataIndex>;
  dataIndexDeleted?: Maybe<DataIndex>;
  dataIndexUpdated?: Maybe<DataIndex>;
  datasourceCreated?: Maybe<Datasource>;
  datasourceDeleted?: Maybe<Datasource>;
  datasourceUpdated?: Maybe<Datasource>;
  docTypeCreated?: Maybe<DocType>;
  docTypeDeleted?: Maybe<DocType>;
  docTypeTemplateCreated?: Maybe<DocTypeTemplate>;
  docTypeTemplateDeleted?: Maybe<DocTypeTemplate>;
  docTypeTemplateUpdated?: Maybe<DocTypeTemplate>;
  docTypeUpdated?: Maybe<DocType>;
  embeddingModelCreated?: Maybe<EmbeddingModel>;
  embeddingModelDeleted?: Maybe<EmbeddingModel>;
  embeddingModelUpdated?: Maybe<EmbeddingModel>;
  enrichItemCreated?: Maybe<EnrichItem>;
  enrichItemDeleted?: Maybe<EnrichItem>;
  enrichItemUpdated?: Maybe<EnrichItem>;
  enrichPipelineCreated?: Maybe<EnrichPipeline>;
  enrichPipelineDeleted?: Maybe<EnrichPipeline>;
  enrichPipelineUpdated?: Maybe<EnrichPipeline>;
  largeLanguageModelCreated?: Maybe<LargeLanguageModel>;
  largeLanguageModelDeleted?: Maybe<LargeLanguageModel>;
  largeLanguageModelUpdated?: Maybe<LargeLanguageModel>;
  pluginDriverCreated?: Maybe<PluginDriver>;
  pluginDriverDeleted?: Maybe<PluginDriver>;
  pluginDriverUpdated?: Maybe<PluginDriver>;
  queryAnalysisCreated?: Maybe<QueryAnalysis>;
  queryAnalysisDeleted?: Maybe<QueryAnalysis>;
  queryAnalysisUpdated?: Maybe<QueryAnalysis>;
  ruleCreated?: Maybe<Rule>;
  ruleDeleted?: Maybe<Rule>;
  ruleUpdated?: Maybe<Rule>;
  searchConfigCreated?: Maybe<SearchConfig>;
  searchConfigDeleted?: Maybe<SearchConfig>;
  searchConfigUpdated?: Maybe<SearchConfig>;
  suggestionCategoryCreated?: Maybe<SuggestionCategory>;
  suggestionCategoryDeleted?: Maybe<SuggestionCategory>;
  suggestionCategoryUpdated?: Maybe<SuggestionCategory>;
  tabCreated?: Maybe<Tab>;
  tabDeleted?: Maybe<Tab>;
  tabUpdated?: Maybe<Tab>;
  tokenFilterCreated?: Maybe<TokenFilter>;
  tokenFilterDeleted?: Maybe<TokenFilter>;
  tokenFilterUpdated?: Maybe<TokenFilter>;
  tokenizerCreated?: Maybe<Tokenizer>;
  tokenizerDeleted?: Maybe<Tokenizer>;
  tokenizerUpdated?: Maybe<Tokenizer>;
};

export type SuggestionCategory = {
  __typename?: 'SuggestionCategory';
  buckets?: Maybe<Connection_Bucket>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  docTypeField?: Maybe<DocTypeField>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  multiSelect: Scalars['Boolean'];
  name?: Maybe<Scalars['String']>;
  priority?: Maybe<Scalars['Float']>;
  translations?: Maybe<Array<Maybe<TranslationDto>>>;
};


export type SuggestionCategoryBucketsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type SuggestionCategoryDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  multiSelect: Scalars['Boolean'];
  name: Scalars['String'];
  priority: Scalars['Float'];
};

export type SuggestionCategoryWithDocTypeFieldDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  docTypeFieldId?: InputMaybe<Scalars['BigInteger']>;
  multiSelect: Scalars['Boolean'];
  name: Scalars['String'];
  priority: Scalars['Float'];
};

export type Tab = {
  __typename?: 'Tab';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  priority?: Maybe<Scalars['Int']>;
  searchTokens?: Maybe<Connection_SearchTokenDto>;
  sortings?: Maybe<Connection_Sorting>;
  tokenTabs?: Maybe<Connection_TokenTab>;
  translations?: Maybe<Array<Maybe<TranslationDto>>>;
};


export type TabSearchTokensArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


export type TabSortingsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


export type TabTokenTabsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  notEqual?: InputMaybe<Scalars['Boolean']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type TabDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  priority: Scalars['Int'];
};

export type TabWithTokenTabsDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  priority: Scalars['Int'];
  tokenTabIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
};

export enum TemplateType {
  JavascriptCompiled = 'JAVASCRIPT_COMPILED',
  JavascriptSource = 'JAVASCRIPT_SOURCE',
  TypescriptSource = 'TYPESCRIPT_SOURCE'
}

export type TokenFilter = {
  __typename?: 'TokenFilter';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
};

export type TokenFilterDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  type: Scalars['String'];
};

export type TokenTab = {
  __typename?: 'TokenTab';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  docTypeField?: Maybe<DocTypeField>;
  docTypeFieldsNotInTokenTab?: Maybe<Connection_DocTypeField>;
  extraParams?: Maybe<Scalars['String']>;
  filter?: Maybe<Scalars['Boolean']>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  tokenType?: Maybe<TokenType>;
  value?: Maybe<Scalars['String']>;
};


export type TokenTabDocTypeFieldsNotInTokenTabArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type TokenTabDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  extraParams?: InputMaybe<Scalars['String']>;
  filter: Scalars['Boolean'];
  name: Scalars['String'];
  tokenType: TokenType;
  value: Scalars['String'];
};

export type TokenTabWithDocTypeFieldDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  docTypeFieldId?: InputMaybe<Scalars['BigInteger']>;
  extraParams?: InputMaybe<Scalars['String']>;
  filter: Scalars['Boolean'];
  name: Scalars['String'];
  tokenType: TokenType;
  value: Scalars['String'];
};

export enum TokenType {
  Autocomplete = 'AUTOCOMPLETE',
  Date = 'DATE',
  DateOrder = 'DATE_ORDER',
  Doctype = 'DOCTYPE',
  Entity = 'ENTITY',
  Filter = 'FILTER',
  Text = 'TEXT'
}

export type Tokenizer = {
  __typename?: 'Tokenizer';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
};

export type TokenizerDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  type: Scalars['String'];
};

export type TranslationDto = {
  __typename?: 'TranslationDTO';
  description?: Maybe<Scalars['String']>;
  key?: Maybe<Scalars['String']>;
  language?: Maybe<Scalars['String']>;
  name: Scalars['String'];
  value?: Maybe<Scalars['String']>;
};

export type Tuple2_Analyzer_CharFilter = {
  __typename?: 'Tuple2_Analyzer_CharFilter';
  left?: Maybe<Analyzer>;
  right?: Maybe<CharFilter>;
};

export type Tuple2_Analyzer_TokenFilter = {
  __typename?: 'Tuple2_Analyzer_TokenFilter';
  left?: Maybe<Analyzer>;
  right?: Maybe<TokenFilter>;
};

export type Tuple2_Analyzer_Tokenizer = {
  __typename?: 'Tuple2_Analyzer_Tokenizer';
  left?: Maybe<Analyzer>;
  right?: Maybe<Tokenizer>;
};

export type Tuple2_Annotator_DocTypeField = {
  __typename?: 'Tuple2_Annotator_DocTypeField';
  left?: Maybe<Annotator>;
  right?: Maybe<DocTypeField>;
};

export type Tuple2_Bucket_Datasource = {
  __typename?: 'Tuple2_Bucket_Datasource';
  left?: Maybe<Bucket>;
  right?: Maybe<Datasource>;
};

export type Tuple2_Bucket_Language = {
  __typename?: 'Tuple2_Bucket_Language';
  left?: Maybe<Bucket>;
  right?: Maybe<Language>;
};

export type Tuple2_Bucket_QueryAnalysis = {
  __typename?: 'Tuple2_Bucket_QueryAnalysis';
  left?: Maybe<Bucket>;
  right?: Maybe<QueryAnalysis>;
};

export type Tuple2_Bucket_RagConfiguration = {
  __typename?: 'Tuple2_Bucket_RAGConfiguration';
  left?: Maybe<Bucket>;
  right?: Maybe<RagConfiguration>;
};

export type Tuple2_Bucket_SearchConfig = {
  __typename?: 'Tuple2_Bucket_SearchConfig';
  left?: Maybe<Bucket>;
  right?: Maybe<SearchConfig>;
};

export type Tuple2_Bucket_Sorting = {
  __typename?: 'Tuple2_Bucket_Sorting';
  left?: Maybe<Bucket>;
  right?: Maybe<Sorting>;
};

export type Tuple2_Bucket_SuggestionCategory = {
  __typename?: 'Tuple2_Bucket_SuggestionCategory';
  left?: Maybe<Bucket>;
  right?: Maybe<SuggestionCategory>;
};

export type Tuple2_Bucket_Tab = {
  __typename?: 'Tuple2_Bucket_Tab';
  left?: Maybe<Bucket>;
  right?: Maybe<Tab>;
};

export type Tuple2_DataIndex_DocType = {
  __typename?: 'Tuple2_DataIndex_DocType';
  left?: Maybe<DataIndex>;
  right?: Maybe<DocType>;
};

export type Tuple2_Datasource_DataIndex = {
  __typename?: 'Tuple2_Datasource_DataIndex';
  left?: Maybe<Datasource>;
  right?: Maybe<DataIndex>;
};

export type Tuple2_Datasource_EnrichPipeline = {
  __typename?: 'Tuple2_Datasource_EnrichPipeline';
  left?: Maybe<Datasource>;
  right?: Maybe<EnrichPipeline>;
};

export type Tuple2_Datasource_PluginDriver = {
  __typename?: 'Tuple2_Datasource_PluginDriver';
  left?: Maybe<Datasource>;
  right?: Maybe<PluginDriver>;
};

export type Tuple2_DocTypeField_Analyzer = {
  __typename?: 'Tuple2_DocTypeField_Analyzer';
  left?: Maybe<DocTypeField>;
  right?: Maybe<Analyzer>;
};

export type Tuple2_DocType_BigInteger = {
  __typename?: 'Tuple2_DocType_BigInteger';
  left?: Maybe<DocType>;
  right?: Maybe<Scalars['BigInteger']>;
};

export type Tuple2_DocType_DocTypeTemplate = {
  __typename?: 'Tuple2_DocType_DocTypeTemplate';
  left?: Maybe<DocType>;
  right?: Maybe<DocTypeTemplate>;
};

export type Tuple2_EnrichPipeline_EnrichItem = {
  __typename?: 'Tuple2_EnrichPipeline_EnrichItem';
  left?: Maybe<EnrichPipeline>;
  right?: Maybe<EnrichItem>;
};

export type Tuple2_PluginDriver_DocTypeField = {
  __typename?: 'Tuple2_PluginDriver_DocTypeField';
  left?: Maybe<PluginDriver>;
  right?: Maybe<DocTypeField>;
};

export type Tuple2_QueryAnalysis_Annotator = {
  __typename?: 'Tuple2_QueryAnalysis_Annotator';
  left?: Maybe<QueryAnalysis>;
  right?: Maybe<Annotator>;
};

export type Tuple2_QueryAnalysis_Rule = {
  __typename?: 'Tuple2_QueryAnalysis_Rule';
  left?: Maybe<QueryAnalysis>;
  right?: Maybe<Rule>;
};

export type Tuple2_SearchConfig_BigInteger = {
  __typename?: 'Tuple2_SearchConfig_BigInteger';
  left?: Maybe<SearchConfig>;
  right?: Maybe<Scalars['BigInteger']>;
};

export type Tuple2_Sorting_DocTypeField = {
  __typename?: 'Tuple2_Sorting_DocTypeField';
  left?: Maybe<Sorting>;
  right?: Maybe<DocTypeField>;
};

export type Tuple2_String_String = {
  __typename?: 'Tuple2_String_String';
  left?: Maybe<Scalars['String']>;
  right?: Maybe<Scalars['String']>;
};

export type Tuple2_SuggestionCategory_DocTypeField = {
  __typename?: 'Tuple2_SuggestionCategory_DocTypeField';
  left?: Maybe<SuggestionCategory>;
  right?: Maybe<DocTypeField>;
};

export type Tuple2_Tab_Sorting = {
  __typename?: 'Tuple2_Tab_Sorting';
  left?: Maybe<Tab>;
  right?: Maybe<Sorting>;
};

export type Tuple2_Tab_TokenTab = {
  __typename?: 'Tuple2_Tab_TokenTab';
  left?: Maybe<Tab>;
  right?: Maybe<TokenTab>;
};

export type Tuple2_TokenTab_DocTypeField = {
  __typename?: 'Tuple2_TokenTab_DocTypeField';
  left?: Maybe<TokenTab>;
  right?: Maybe<DocTypeField>;
};

export type UpdateDatasourceDtoInput = {
  /** The dataIndex's id related to this datasource */
  dataIndexId: Scalars['BigInteger'];
  /** The datasource's id that needs to be updated */
  datasourceId: Scalars['BigInteger'];
  description?: InputMaybe<Scalars['String']>;
  /** Json configuration with custom fields for datasource */
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  /** Pipeline to be created and associated (optional) */
  pipeline?: InputMaybe<PipelineWithItemsDtoInput>;
  /** Pipeline to be associated (optional) */
  pipelineId?: InputMaybe<Scalars['BigInteger']>;
  /** The duration to identify orphaned Dataindex. */
  purgeMaxAge?: InputMaybe<Scalars['String']>;
  /** If true set active the purge job scheduling */
  purgeable?: InputMaybe<Scalars['Boolean']>;
  /** Cron quartz expression to define purging for this datasource */
  purging?: InputMaybe<Scalars['String']>;
  /** If true datasource is reindexed based on defined scheduling expression */
  reindexable?: InputMaybe<Scalars['Boolean']>;
  /** Cron quartz expression to define reindexing of datasource */
  reindexing?: InputMaybe<Scalars['String']>;
  /** If true datasource is scheduled based on defined scheduling expression */
  schedulable?: InputMaybe<Scalars['Boolean']>;
  /** Cron quartz expression to define scheduling of datasource */
  scheduling?: InputMaybe<Scalars['String']>;
};

export enum UserField {
  Email = 'EMAIL',
  Name = 'NAME',
  NameSurname = 'NAME_SURNAME',
  Roles = 'ROLES',
  Surname = 'SURNAME',
  Username = 'USERNAME'
}

export type LanguagesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type LanguagesQuery = { __typename?: 'Query', languages?: { __typename?: 'DefaultConnection_Language', edges?: Array<{ __typename?: 'DefaultEdge_Language', node?: { __typename?: 'Language', id?: string | null, name?: string | null, value?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type AnalyzersQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type AnalyzersQuery = { __typename?: 'Query', analyzers?: { __typename?: 'DefaultConnection_Analyzer', edges?: Array<{ __typename?: 'DefaultEdge_Analyzer', node?: { __typename?: 'Analyzer', id?: string | null, name?: string | null, description?: string | null, type?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type AnalyzerOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type AnalyzerOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_Analyzer', edges?: Array<{ __typename?: 'DefaultEdge_Analyzer', node?: { __typename?: 'Analyzer', name?: string | null, description?: string | null, type?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type AnalyzerValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type AnalyzerValueQuery = { __typename?: 'Query', value?: { __typename?: 'Analyzer', id?: string | null, name?: string | null, description?: string | null, type?: string | null } | null };

export type BindAnalyzerToDocTypeFieldMutationVariables = Exact<{
  analyzerId: Scalars['ID'];
  docTypeFieldId: Scalars['ID'];
}>;


export type BindAnalyzerToDocTypeFieldMutation = { __typename?: 'Mutation', bindAnalyzerToDocTypeField?: { __typename?: 'Tuple2_DocTypeField_Analyzer', left?: { __typename?: 'DocTypeField', id?: string | null, docType?: { __typename?: 'DocType', id?: string | null } | null } | null, right?: { __typename?: 'Analyzer', id?: string | null } | null } | null };

export type UnbindQueryAnalysisFromDocTypeFieldMutationVariables = Exact<{
  docTypeFieldId: Scalars['ID'];
}>;


export type UnbindQueryAnalysisFromDocTypeFieldMutation = { __typename?: 'Mutation', unbindAnalyzerFromDocTypeField?: { __typename?: 'Tuple2_DocTypeField_Analyzer', right?: { __typename?: 'Analyzer', id?: string | null } | null } | null };

export type SearchConfigOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type SearchConfigOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_SearchConfig', edges?: Array<{ __typename?: 'DefaultEdge_SearchConfig', node?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type SearchConfigValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type SearchConfigValueQuery = { __typename?: 'Query', value?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, description?: string | null } | null };

export type BindTokenizerToAnalyzerMutationVariables = Exact<{
  analyzerId: Scalars['ID'];
  tokenizerId: Scalars['ID'];
}>;


export type BindTokenizerToAnalyzerMutation = { __typename?: 'Mutation', bindTokenizerToAnalyzer?: { __typename?: 'Tuple2_Analyzer_Tokenizer', left?: { __typename?: 'Analyzer', id?: string | null, tokenizer?: { __typename?: 'Tokenizer', id?: string | null } | null } | null, right?: { __typename?: 'Tokenizer', id?: string | null } | null } | null };

export type UnbindTokenizerFromAnalyzerMutationVariables = Exact<{
  analyzerId: Scalars['ID'];
}>;


export type UnbindTokenizerFromAnalyzerMutation = { __typename?: 'Mutation', unbindTokenizerFromAnalyzer?: { __typename?: 'Tuple2_Analyzer_Tokenizer', right?: { __typename?: 'Tokenizer', id?: string | null } | null } | null };

export type LanguagesOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type LanguagesOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_Language', edges?: Array<{ __typename?: 'DefaultEdge_Language', node?: { __typename?: 'Language', id?: string | null, name?: string | null, value?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type LanguageValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type LanguageValueQuery = { __typename?: 'Query', value?: { __typename?: 'Language', id?: string | null, name?: string | null, value?: string | null } | null };

export type AnalyzerQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type AnalyzerQuery = { __typename?: 'Query', analyzer?: { __typename?: 'Analyzer', id?: string | null, name?: string | null, description?: string | null, type?: string | null, jsonConfig?: string | null, tokenizer?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null } | null } | null };

export type DeleteAnalyzerMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteAnalyzerMutation = { __typename?: 'Mutation', deleteAnalyzer?: { __typename?: 'Analyzer', id?: string | null, name?: string | null } | null };

export type CreateOrUpdateAnalyzerMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: Scalars['String'];
  tokenFilterIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
  charFilterIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
  tokenizerId?: InputMaybe<Scalars['BigInteger']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateAnalyzerMutation = { __typename?: 'Mutation', analyzerWithLists?: { __typename?: 'Response_Analyzer', entity?: { __typename?: 'Analyzer', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type AnalyzersAssociationsQueryVariables = Exact<{
  parentId: Scalars['ID'];
  unassociated: Scalars['Boolean'];
}>;


export type AnalyzersAssociationsQuery = { __typename?: 'Query', analyzer?: { __typename?: 'Analyzer', id?: string | null, charFilters?: { __typename?: 'DefaultConnection_CharFilter', edges?: Array<{ __typename?: 'DefaultEdge_CharFilter', node?: { __typename?: 'CharFilter', id?: string | null, name?: string | null } | null } | null> | null } | null, tokenFilters?: { __typename?: 'DefaultConnection_TokenFilter', edges?: Array<{ __typename?: 'DefaultEdge_TokenFilter', node?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null } | null } | null> | null } | null } | null };

export type CharfiltersQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type CharfiltersQuery = { __typename?: 'Query', charFilters?: { __typename?: 'DefaultConnection_CharFilter', edges?: Array<{ __typename?: 'DefaultEdge_CharFilter', node?: { __typename?: 'CharFilter', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteCharFiltersMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteCharFiltersMutation = { __typename?: 'Mutation', deleteCharFilter?: { __typename?: 'CharFilter', id?: string | null, name?: string | null } | null };

export type UnboundAnalyzersByCharFilterQueryVariables = Exact<{
  charFilterId: Scalars['BigInteger'];
}>;


export type UnboundAnalyzersByCharFilterQuery = { __typename?: 'Query', unboundAnalyzersByCharFilter?: Array<{ __typename?: 'Analyzer', name?: string | null, id?: string | null } | null> | null };

export type AddCharFiltersToAnalyzerMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddCharFiltersToAnalyzerMutation = { __typename?: 'Mutation', addCharFilterToAnalyzer?: { __typename?: 'Tuple2_Analyzer_CharFilter', left?: { __typename?: 'Analyzer', id?: string | null } | null, right?: { __typename?: 'CharFilter', id?: string | null } | null } | null };

export type CharFilterQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type CharFilterQuery = { __typename?: 'Query', charFilter?: { __typename?: 'CharFilter', id?: string | null, name?: string | null, description?: string | null, jsonConfig?: string | null, type?: string | null } | null };

export type CreateOrUpdateCharFilterMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  type: Scalars['String'];
}>;


export type CreateOrUpdateCharFilterMutation = { __typename?: 'Mutation', charFilter?: { __typename?: 'Response_CharFilter', entity?: { __typename?: 'CharFilter', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DataIndexInformationQueryVariables = Exact<{ [key: string]: never; }>;


export type DataIndexInformationQuery = { __typename?: 'Query', buckets?: { __typename?: 'DefaultConnection_Bucket', edges?: Array<{ __typename?: 'DefaultEdge_Bucket', node?: { __typename?: 'Bucket', datasources?: { __typename?: 'DefaultConnection_Datasource', edges?: Array<{ __typename?: 'DefaultEdge_Datasource', node?: { __typename?: 'Datasource', dataIndex?: { __typename?: 'DataIndex', cat?: { __typename?: 'CatResponse', docsCount?: string | null, docsDeleted?: string | null, health?: string | null, index?: string | null, pri?: string | null, priStoreSize: any, rep?: string | null, status?: string | null, storeSize: any, uuid?: string | null } | null } | null } | null } | null> | null } | null } | null } | null> | null } | null };

export type SchedulersFaiulureQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
}>;


export type SchedulersFaiulureQuery = { __typename?: 'Query', schedulers?: { __typename?: 'DefaultConnection_Scheduler', edges?: Array<{ __typename?: 'DefaultEdge_Scheduler', node?: { __typename?: 'Scheduler', id?: string | null, modifiedDate?: any | null, errorDescription?: string | null, lastIngestionDate?: any | null, status?: SchedulerStatus | null, datasource?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null, newDataIndex?: { __typename?: 'DataIndex', id?: string | null, name?: string | null } | null } | null } | null> | null } | null };

export type DocumentTypeTemplatesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type DocumentTypeTemplatesQuery = { __typename?: 'Query', docTypeTemplates?: { __typename?: 'DefaultConnection_DocTypeTemplate', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeTemplate', node?: { __typename?: 'DocTypeTemplate', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteDocumentTypeTemplateMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteDocumentTypeTemplateMutation = { __typename?: 'Mutation', deleteDocTypeTemplate?: { __typename?: 'DocTypeTemplate', id?: string | null, name?: string | null } | null };

export type DocumentTypesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type DocumentTypesQuery = { __typename?: 'Query', docTypes?: { __typename?: 'DefaultConnection_DocType', edges?: Array<{ __typename?: 'DefaultEdge_DocType', node?: { __typename?: 'DocType', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DocumentTypeTemplateQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DocumentTypeTemplateQuery = { __typename?: 'Query', docTypeTemplate?: { __typename?: 'DocTypeTemplate', id?: string | null, name?: string | null, description?: string | null, templateType?: TemplateType | null, source?: string | null, compiled?: string | null } | null };

export type CreateOrUpdateDocumentTypeTemplateMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  templateType: TemplateType;
  source: Scalars['String'];
  compiled: Scalars['String'];
}>;


export type CreateOrUpdateDocumentTypeTemplateMutation = { __typename?: 'Mutation', docTypeTemplate?: { __typename?: 'Response_DocTypeTemplate', entity?: { __typename?: 'DocTypeTemplate', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DocumentTypeFieldQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DocumentTypeFieldQuery = { __typename?: 'Query', docTypeField?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null, fieldType?: FieldType | null, boost?: number | null, searchable?: boolean | null, exclude?: boolean | null, fieldName?: string | null, jsonConfig?: string | null, sortable?: boolean | null, analyzer?: { __typename?: 'Analyzer', id?: string | null, name?: string | null } | null, translations?: Array<{ __typename?: 'TranslationDTO', key?: string | null, language?: string | null, value?: string | null, description?: string | null } | null> | null } | null };

export type CreateOrUpdateDocumentTypeSubFieldsMutationVariables = Exact<{
  parentDocTypeFieldId: Scalars['ID'];
  name: Scalars['String'];
  fieldName: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
  searchable: Scalars['Boolean'];
  boost?: InputMaybe<Scalars['Float']>;
  fieldType: FieldType;
  sortable: Scalars['Boolean'];
}>;


export type CreateOrUpdateDocumentTypeSubFieldsMutation = { __typename?: 'Mutation', createSubField?: { __typename?: 'Response_DocTypeField', entity?: { __typename?: 'DocTypeField', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type CreateOrUpdateDocumentTypeMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateDocumentTypeMutation = { __typename?: 'Mutation', docType?: { __typename?: 'Response_DocType', entity?: { __typename?: 'DocType', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DocumentTypeQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DocumentTypeQuery = { __typename?: 'Query', docType?: { __typename?: 'DocType', id?: string | null, name?: string | null, description?: string | null, docTypeTemplate?: { __typename?: 'DocTypeTemplate', id?: string | null, name?: string | null } | null } | null };

export type DocTypeFieldsByParentQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  parentId: Scalars['BigInteger'];
  docTypeId: Scalars['ID'];
}>;


export type DocTypeFieldsByParentQuery = { __typename?: 'Query', docTypeFieldsFromDocTypeByParent?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null, fieldType?: FieldType | null, boost?: number | null, searchable?: boolean | null, exclude?: boolean | null, fieldName?: string | null, jsonConfig?: string | null, sortable?: boolean | null, parent?: { __typename?: 'DocTypeField', id?: string | null, fieldName?: string | null } | null } | null } | null> | null } | null };

export type CreateOrUpdateDocumentTypeFieldMutationVariables = Exact<{
  documentTypeId: Scalars['ID'];
  documentTypeFieldId?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  fieldName: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  fieldType: FieldType;
  boost?: InputMaybe<Scalars['Float']>;
  searchable: Scalars['Boolean'];
  exclude?: InputMaybe<Scalars['Boolean']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  sortable: Scalars['Boolean'];
  analyzerId?: InputMaybe<Scalars['BigInteger']>;
}>;


export type CreateOrUpdateDocumentTypeFieldMutation = { __typename?: 'Mutation', docTypeFieldWithAnalyzer?: { __typename?: 'Response_DocTypeField', entity?: { __typename?: 'DocTypeField', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DeleteDocumentTypeMutationVariables = Exact<{
  id: Scalars['ID'];
  docTypeName?: InputMaybe<Scalars['String']>;
}>;


export type DeleteDocumentTypeMutation = { __typename?: 'Mutation', deleteDocType?: { __typename?: 'DocType', id?: string | null } | null };

export type DocTypeTemplateListQueryVariables = Exact<{ [key: string]: never; }>;


export type DocTypeTemplateListQuery = { __typename?: 'Query', docTypeTemplates?: { __typename?: 'DefaultConnection_DocTypeTemplate', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeTemplate', node?: { __typename?: 'DocTypeTemplate', name?: string | null, id?: string | null } | null } | null> | null } | null };

export type CreateOrUpdateDocTypeWithTemplateMutationVariables = Exact<{
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  docTypeTemplateId?: InputMaybe<Scalars['BigInteger']>;
  id?: InputMaybe<Scalars['ID']>;
}>;


export type CreateOrUpdateDocTypeWithTemplateMutation = { __typename?: 'Mutation', docTypeWithTemplate?: { __typename?: 'Response_DocType', entity?: { __typename?: 'DocType', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type UnboundAnalyzersQueryVariables = Exact<{ [key: string]: never; }>;


export type UnboundAnalyzersQuery = { __typename?: 'Query', analyzers?: { __typename?: 'DefaultConnection_Analyzer', edges?: Array<{ __typename?: 'DefaultEdge_Analyzer', node?: { __typename?: 'Analyzer', id?: string | null, name?: string | null } | null } | null> | null } | null };

export type EmbeddingModelsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type EmbeddingModelsQuery = { __typename?: 'Query', embeddingModels?: { __typename?: 'DefaultConnection_EmbeddingModel', edges?: Array<{ __typename?: 'DefaultEdge_EmbeddingModel', node?: { __typename?: 'EmbeddingModel', id?: string | null, name?: string | null, description?: string | null, enabled: boolean } | null } | null> | null } | null };

export type EnableEmbeddingModelMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EnableEmbeddingModelMutation = { __typename?: 'Mutation', enableEmbeddingModel?: { __typename?: 'EmbeddingModel', id?: string | null, name?: string | null } | null };

export type DeleteEmbeddingModelMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteEmbeddingModelMutation = { __typename?: 'Mutation', deleteEmbeddingModel?: { __typename?: 'EmbeddingModel', id?: string | null, name?: string | null } | null };

export type EmbeddingModelQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EmbeddingModelQuery = { __typename?: 'Query', embeddingModel?: { __typename?: 'EmbeddingModel', name?: string | null, description?: string | null, apiUrl?: string | null, apiKey?: string | null, vectorSize?: number | null, jsonConfig?: string | null, providerModel?: { __typename?: 'ProviderModel', provider?: string | null, model?: string | null } | null } | null };

export type CreateOrUpdateEmbeddingModelMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  apiKey?: InputMaybe<Scalars['String']>;
  apiUrl: Scalars['String'];
  description: Scalars['String'];
  name: Scalars['String'];
  vectorSize: Scalars['Int'];
  providerModel: ProviderModelDtoInput;
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateEmbeddingModelMutation = { __typename?: 'Mutation', embeddingModel?: { __typename?: 'Response_EmbeddingModel', entity?: { __typename?: 'EmbeddingModel', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type UnboundEnrichPipelinesQueryVariables = Exact<{
  itemId: Scalars['BigInteger'];
}>;


export type UnboundEnrichPipelinesQuery = { __typename?: 'Query', unboundEnrichPipelines?: Array<{ __typename?: 'EnrichPipeline', name?: string | null, id?: string | null } | null> | null };

export type DeleteEnrichItemMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteEnrichItemMutation = { __typename?: 'Mutation', deleteEnrichItem?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null } | null };

export type EnrichItemQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EnrichItemQuery = { __typename?: 'Query', enrichItem?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null, description?: string | null, type?: EnrichItemType | null, serviceName?: string | null, jsonConfig?: string | null, script?: string | null, behaviorMergeType?: BehaviorMergeType | null, jsonPath?: string | null, behaviorOnError?: BehaviorOnError | null, requestTimeout?: any | null } | null };

export type CreateOrUpdateEnrichItemMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: EnrichItemType;
  serviceName: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
  script?: InputMaybe<Scalars['String']>;
  behaviorMergeType: BehaviorMergeType;
  jsonPath: Scalars['String'];
  behaviorOnError: BehaviorOnError;
  requestTimeout: Scalars['BigInteger'];
}>;


export type CreateOrUpdateEnrichItemMutation = { __typename?: 'Mutation', enrichItem?: { __typename?: 'Response_EnrichItem', entity?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type LargeLanguageModelsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type LargeLanguageModelsQuery = { __typename?: 'Query', largeLanguageModels?: { __typename?: 'DefaultConnection_LargeLanguageModel', edges?: Array<{ __typename?: 'DefaultEdge_LargeLanguageModel', node?: { __typename?: 'LargeLanguageModel', id?: string | null, name?: string | null, description?: string | null, enabled: boolean, providerModel?: { __typename?: 'ProviderModel', provider?: string | null, model?: string | null } | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type EnableLargeLanguageModelMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EnableLargeLanguageModelMutation = { __typename?: 'Mutation', enableLargeLanguageModel?: { __typename?: 'LargeLanguageModel', id?: string | null, name?: string | null } | null };

export type DeleteLargeLanguageModelMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteLargeLanguageModelMutation = { __typename?: 'Mutation', deleteLargeLanguageModel?: { __typename?: 'LargeLanguageModel', id?: string | null, name?: string | null } | null };

export type CreateOrUpdateLargeLanguageModelMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  apiKey?: InputMaybe<Scalars['String']>;
  apiUrl: Scalars['String'];
  description: Scalars['String'];
  name: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
  providerModel: ProviderModelDtoInput;
  contextWindow?: InputMaybe<Scalars['Int']>;
  retrieveCitations?: InputMaybe<Scalars['Boolean']>;
}>;


export type CreateOrUpdateLargeLanguageModelMutation = { __typename?: 'Mutation', largeLanguageModel?: { __typename?: 'Response_LargeLanguageModel', entity?: { __typename?: 'LargeLanguageModel', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type LargeLanguageModelQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type LargeLanguageModelQuery = { __typename?: 'Query', largeLanguageModel?: { __typename?: 'LargeLanguageModel', name?: string | null, description?: string | null, apiUrl?: string | null, apiKey?: string | null, jsonConfig?: string | null, contextWindow?: number | null, retrieveCitations?: boolean | null, providerModel?: { __typename?: 'ProviderModel', provider?: string | null, model?: string | null } | null } | null };

export type SchedulersQueryVariables = Exact<{ [key: string]: never; }>;


export type SchedulersQuery = { __typename?: 'Query', schedulers?: { __typename?: 'DefaultConnection_Scheduler', edges?: Array<{ __typename?: 'DefaultEdge_Scheduler', node?: { __typename?: 'Scheduler', scheduleId?: string | null, status?: SchedulerStatus | null, datasource?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null } | null } | null> | null } | null };

export type SchedulersErrorQueryVariables = Exact<{ [key: string]: never; }>;


export type SchedulersErrorQuery = { __typename?: 'Query', schedulers?: { __typename?: 'DefaultConnection_Scheduler', edges?: Array<{ __typename?: 'DefaultEdge_Scheduler', node?: { __typename?: 'Scheduler', scheduleId?: string | null, createDate?: any | null, status?: SchedulerStatus | null, datasource?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null } | null } | null> | null } | null };

export type SchedulerQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type SchedulerQuery = { __typename?: 'Query', scheduler?: { __typename?: 'Scheduler', scheduleId?: string | null, createDate?: any | null, modifiedDate?: any | null, lastIngestionDate?: any | null, status?: SchedulerStatus | null, errorDescription?: string | null } | null };

export type PluginDriverQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type PluginDriverQuery = { __typename?: 'Query', pluginDriver?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null, description?: string | null, type?: PluginDriverType | null, jsonConfig?: string | null, provisioning?: Provisioning | null, aclMappings?: Array<{ __typename?: 'PluginDriverAclMapping', userField?: UserField | null, docTypeField?: { __typename?: 'DocTypeField', name?: string | null, id?: string | null, fieldName?: string | null } | null } | null> | null } | null };

export type CreateOrUpdatePluginDriverMutationMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: PluginDriverType;
  jsonConfig?: InputMaybe<Scalars['String']>;
  provisioning: Provisioning;
}>;


export type CreateOrUpdatePluginDriverMutationMutation = { __typename?: 'Mutation', pluginDriver?: { __typename?: 'Response_PluginDriver', entity?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type PluginDriverByNameQueryVariables = Exact<{
  name?: InputMaybe<Scalars['String']>;
}>;


export type PluginDriverByNameQuery = { __typename?: 'Query', pluginDrivers?: { __typename?: 'DefaultConnection_PluginDriver', edges?: Array<{ __typename?: 'DefaultEdge_PluginDriver', node?: { __typename?: 'PluginDriver', id?: string | null } | null } | null> | null } | null };

export type PluginDriversInfoQueryQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type PluginDriversInfoQueryQuery = { __typename?: 'Query', pluginDrivers?: { __typename?: 'DefaultConnection_PluginDriver', edges?: Array<{ __typename?: 'DefaultEdge_PluginDriver', node?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null, description?: string | null, type?: PluginDriverType | null, aclMappings?: Array<{ __typename?: 'PluginDriverAclMapping', userField?: UserField | null, docTypeField?: { __typename?: 'DocTypeField', fieldName?: string | null } | null } | null> | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeletePluginDriverMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeletePluginDriverMutation = { __typename?: 'Mutation', deletePluginDriver?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null } | null };

export type PluginDriverWithDocTypeMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: PluginDriverType;
  jsonConfig: Scalars['String'];
  provisioning: Provisioning;
  docTypeUserDTOSet?: InputMaybe<Array<InputMaybe<DocTypeUserDtoInput>> | InputMaybe<DocTypeUserDtoInput>>;
}>;


export type PluginDriverWithDocTypeMutation = { __typename?: 'Mutation', pluginDriverWithDocType?: { __typename?: 'Response_PluginDriver', entity?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type PluginDriverToDocumentTypeFieldsQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type PluginDriverToDocumentTypeFieldsQuery = { __typename?: 'Query', pluginDriver?: { __typename?: 'PluginDriver', id?: string | null, aclMappings?: Array<{ __typename?: 'PluginDriverAclMapping', userField?: UserField | null, docTypeField?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null } | null } | null> | null, docTypeFields?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null, docType?: { __typename?: 'DocType', id?: string | null } | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type DocumentTypeFieldsForPluginQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
}>;


export type DocumentTypeFieldsForPluginQuery = { __typename?: 'Query', docTypeFields?: { __typename: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename: 'DefaultEdge_DocTypeField', node?: { __typename: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null } | null };

export type QueryAnalysesRulesQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type QueryAnalysesRulesQuery = { __typename?: 'Query', queryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null, rules?: { __typename?: 'DefaultConnection_Rule', edges?: Array<{ __typename?: 'DefaultEdge_Rule', node?: { __typename?: 'Rule', id?: string | null, name?: string | null, lhs?: string | null, rhs?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type AddRulesToQueryAnalysesMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddRulesToQueryAnalysesMutation = { __typename?: 'Mutation', addRuleToQueryAnalysis?: { __typename?: 'Tuple2_QueryAnalysis_Rule', left?: { __typename?: 'QueryAnalysis', id?: string | null } | null, right?: { __typename?: 'Rule', id?: string | null } | null } | null };

export type RemoveRuleFromQueryAnalysesMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveRuleFromQueryAnalysesMutation = { __typename?: 'Mutation', removeRuleFromQueryAnalysis?: { __typename?: 'Tuple2_QueryAnalysis_Rule', left?: { __typename?: 'QueryAnalysis', id?: string | null } | null, right?: { __typename?: 'Rule', id?: string | null } | null } | null };

export type QueryAnalysisQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type QueryAnalysisQuery = { __typename?: 'Query', queryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null, description?: string | null, stopWords?: string | null, annotators?: { __typename?: 'DefaultConnection_Annotator', edges?: Array<{ __typename?: 'DefaultEdge_Annotator', node?: { __typename?: 'Annotator', id?: string | null, name?: string | null } | null } | null> | null } | null, rules?: { __typename?: 'DefaultConnection_Rule', edges?: Array<{ __typename?: 'DefaultEdge_Rule', node?: { __typename?: 'Rule', id?: string | null, name?: string | null } | null } | null> | null } | null } | null };

export type QueryAnalysesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type QueryAnalysesQuery = { __typename?: 'Query', queryAnalyses?: { __typename?: 'DefaultConnection_QueryAnalysis', edges?: Array<{ __typename?: 'DefaultEdge_QueryAnalysis', node?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteQueryAnalysisMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteQueryAnalysisMutation = { __typename?: 'Mutation', deleteQueryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null } | null };

export type CreateOrUpdateQueryAnalysisMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  stopWords?: InputMaybe<Scalars['String']>;
  annotatorsIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
  rulesIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
}>;


export type CreateOrUpdateQueryAnalysisMutation = { __typename?: 'Mutation', queryAnalysisWithLists?: { __typename?: 'Response_QueryAnalysis', entity?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type QueryAnalysisAssociationsQueryVariables = Exact<{
  parentId: Scalars['ID'];
  unassociated: Scalars['Boolean'];
}>;


export type QueryAnalysisAssociationsQuery = { __typename?: 'Query', queryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null, annotators?: { __typename?: 'DefaultConnection_Annotator', edges?: Array<{ __typename?: 'DefaultEdge_Annotator', node?: { __typename?: 'Annotator', id?: string | null, name?: string | null } | null } | null> | null } | null, rules?: { __typename?: 'DefaultConnection_Rule', edges?: Array<{ __typename?: 'DefaultEdge_Rule', node?: { __typename?: 'Rule', id?: string | null, name?: string | null } | null } | null> | null } | null } | null };

export type RagConfigurationsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type RagConfigurationsQuery = { __typename?: 'Query', ragConfigurations?: { __typename?: 'DefaultConnection_RAGConfiguration', edges?: Array<{ __typename?: 'DefaultEdge_RAGConfiguration', node?: { __typename?: 'RAGConfiguration', id?: string | null, name?: string | null, description?: string | null, type?: RagType | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type RagConfigurationQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type RagConfigurationQuery = { __typename?: 'Query', ragConfiguration?: { __typename?: 'RAGConfiguration', id?: string | null, name?: string | null, description?: string | null, type?: RagType | null, reformulate?: boolean | null, chunkWindow?: number | null, rephrasePrompt?: string | null, prompt?: string | null, jsonConfig?: string | null, ragToolDescription?: string | null, promptNoRag?: string | null } | null };

export type CreateRagConfigMutationVariables = Exact<{
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: RagType;
  reformulate?: InputMaybe<Scalars['Boolean']>;
  chunkWindow?: InputMaybe<Scalars['Int']>;
  rephrasePrompt?: InputMaybe<Scalars['String']>;
  prompt?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  ragToolDescription?: InputMaybe<Scalars['String']>;
  promptNoRag?: InputMaybe<Scalars['String']>;
}>;


export type CreateRagConfigMutation = { __typename?: 'Mutation', createRAGConfiguration?: { __typename?: 'Response_RAGConfiguration', entity?: { __typename?: 'RAGConfiguration', id?: string | null, name?: string | null, type?: RagType | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type UpdateRagConfigurationMutationVariables = Exact<{
  id: Scalars['ID'];
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  reformulate?: InputMaybe<Scalars['Boolean']>;
  chunkWindow?: InputMaybe<Scalars['Int']>;
  rephrasePrompt?: InputMaybe<Scalars['String']>;
  prompt?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  ragToolDescription?: InputMaybe<Scalars['String']>;
  promptNoRag?: InputMaybe<Scalars['String']>;
  patch?: InputMaybe<Scalars['Boolean']>;
}>;


export type UpdateRagConfigurationMutation = { __typename?: 'Mutation', updateRAGConfiguration?: { __typename?: 'Response_RAGConfiguration', entity?: { __typename?: 'RAGConfiguration', id?: string | null, name?: string | null, type?: RagType | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DeleteRagConfigurationMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteRagConfigurationMutation = { __typename?: 'Mutation', deleteRAGConfiguration?: { __typename?: 'RAGConfiguration', id?: string | null } | null };

export type UnboundRagConfigurationsByBucketQueryVariables = Exact<{
  bucketId: Scalars['ID'];
  ragType: RagType;
}>;


export type UnboundRagConfigurationsByBucketQuery = { __typename?: 'Query', unboundRAGConfigurationByBucket?: Array<{ __typename?: 'RAGConfiguration', id?: string | null, name?: string | null } | null> | null };

export type RulesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type RulesQuery = { __typename?: 'Query', rules?: { __typename?: 'DefaultConnection_Rule', edges?: Array<{ __typename?: 'DefaultEdge_Rule', node?: { __typename?: 'Rule', id?: string | null, name?: string | null, lhs?: string | null, rhs?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteRulesMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteRulesMutation = { __typename?: 'Mutation', deleteRule?: { __typename?: 'Rule', id?: string | null, name?: string | null } | null };

export type RuleQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type RuleQuery = { __typename?: 'Query', rule?: { __typename?: 'Rule', id?: string | null, name?: string | null, description?: string | null, lhs?: string | null, rhs?: string | null } | null };

export type CreateOrUpdateRuleQueryMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  lhs: Scalars['String'];
  rhs: Scalars['String'];
}>;


export type CreateOrUpdateRuleQueryMutation = { __typename?: 'Mutation', rule?: { __typename?: 'Response_Rule', entity?: { __typename?: 'Rule', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type SearchConfigQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type SearchConfigQuery = { __typename?: 'Query', searchConfig?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, description?: string | null, minScore?: number | null, minScoreSuggestions: boolean, minScoreSearch: boolean, queryParserConfigs?: { __typename?: 'DefaultConnection_QueryParserConfig', edges?: Array<{ __typename?: 'DefaultEdge_QueryParserConfig', node?: { __typename?: 'QueryParserConfig', id?: string | null, name?: string | null, type?: QueryParserType | null, jsonConfig?: string | null } | null } | null> | null } | null } | null };

export type CreateOrUpdateSearchConfigMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  minScore: Scalars['Float'];
  minScoreSuggestions: Scalars['Boolean'];
  minScoreSearch: Scalars['Boolean'];
  queryParsersConfig?: InputMaybe<Array<InputMaybe<QueryParserConfigDtoInput>> | InputMaybe<QueryParserConfigDtoInput>>;
}>;


export type CreateOrUpdateSearchConfigMutation = { __typename?: 'Mutation', searchConfigWithQueryParsers?: { __typename?: 'Response_SearchConfig', entity?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, minScore?: number | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type SearchConfigsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type SearchConfigsQuery = { __typename?: 'Query', searchConfigs?: { __typename?: 'DefaultConnection_SearchConfig', edges?: Array<{ __typename?: 'DefaultEdge_SearchConfig', node?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, description?: string | null, minScore?: number | null, minScoreSuggestions: boolean, minScoreSearch: boolean } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteSearchConfigMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteSearchConfigMutation = { __typename?: 'Mutation', deleteSearchConfig?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null } | null };

export type QueryParserConfigQueryVariables = Exact<{ [key: string]: never; }>;


export type QueryParserConfigQuery = { __typename?: 'Query', queryParserConfigFormConfigurations?: any | null };

export type AddSuggestionCategoryTranslationMutationVariables = Exact<{
  suggestionCategoryId: Scalars['ID'];
  language: Scalars['String'];
  key?: InputMaybe<Scalars['String']>;
  value: Scalars['String'];
}>;


export type AddSuggestionCategoryTranslationMutation = { __typename?: 'Mutation', addSuggestionCategoryTranslation?: { __typename?: 'Tuple2_String_String', left?: string | null, right?: string | null } | null };

export type UnboundBucketsBySuggestionCategoryQueryVariables = Exact<{
  id: Scalars['BigInteger'];
}>;


export type UnboundBucketsBySuggestionCategoryQuery = { __typename?: 'Query', unboundBucketsBySuggestionCategory?: Array<{ __typename?: 'Bucket', name?: string | null, id?: string | null } | null> | null };

export type SuggestionCategoriesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type SuggestionCategoriesQuery = { __typename?: 'Query', suggestionCategories?: { __typename?: 'DefaultConnection_SuggestionCategory', edges?: Array<{ __typename?: 'DefaultEdge_SuggestionCategory', node?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null, description?: string | null, priority?: number | null, multiSelect: boolean } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteSuggestionCategoryMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteSuggestionCategoryMutation = { __typename?: 'Mutation', deleteSuggestionCategory?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null } | null };

export type SuggestionCategoryDocumentTypeFieldsQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type SuggestionCategoryDocumentTypeFieldsQuery = { __typename?: 'Query', suggestionCategory?: { __typename?: 'SuggestionCategory', id?: string | null, docTypeField?: { __typename?: 'DocTypeField', name?: string | null, subFields?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null, docType?: { __typename?: 'DocType', id?: string | null } | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null } | null };

export type AddDocumentTypeFieldToSuggestionCategoryMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddDocumentTypeFieldToSuggestionCategoryMutation = { __typename?: 'Mutation', addDocTypeFieldToSuggestionCategory?: { __typename?: 'Tuple2_SuggestionCategory_DocTypeField', left?: { __typename?: 'SuggestionCategory', id?: string | null } | null, right?: { __typename?: 'DocTypeField', id?: string | null } | null } | null };

export type SuggestionCategoryQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type SuggestionCategoryQuery = { __typename?: 'Query', suggestionCategory?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null, description?: string | null, priority?: number | null, multiSelect: boolean, docTypeField?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null } | null, translations?: Array<{ __typename?: 'TranslationDTO', key?: string | null, language?: string | null, value?: string | null, description?: string | null } | null> | null } | null };

export type UnboundDocTypeFieldsBySuggestionCategoryQueryVariables = Exact<{
  suggestionCategoryId: Scalars['BigInteger'];
}>;


export type UnboundDocTypeFieldsBySuggestionCategoryQuery = { __typename?: 'Query', unboundDocTypeFieldsBySuggestionCategory?: Array<{ __typename?: 'DocTypeField', id?: string | null, name?: string | null } | null> | null };

export type DocTypeFieldsQueryVariables = Exact<{
  after?: InputMaybe<Scalars['String']>;
}>;


export type DocTypeFieldsQuery = { __typename?: 'Query', docTypeFields?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type CreateOrUpdateSuggestionCategoryMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  priority: Scalars['Float'];
  multiSelect: Scalars['Boolean'];
  docTypeFieldId?: InputMaybe<Scalars['BigInteger']>;
}>;


export type CreateOrUpdateSuggestionCategoryMutation = { __typename?: 'Mutation', suggestionCategoryWithDocTypeField?: { __typename?: 'Response_SuggestionCategory', entity?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type TabsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type TabsQuery = { __typename?: 'Query', tabs?: { __typename?: 'DefaultConnection_Tab', edges?: Array<{ __typename?: 'DefaultEdge_Tab', node?: { __typename?: 'Tab', id?: string | null, name?: string | null, description?: string | null, priority?: number | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteTabsMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteTabsMutation = { __typename?: 'Mutation', deleteTab?: { __typename?: 'Tab', id?: string | null, name?: string | null } | null };

export type UnboundBucketsByTabQueryVariables = Exact<{
  id: Scalars['BigInteger'];
}>;


export type UnboundBucketsByTabQuery = { __typename?: 'Query', unboundBucketsByTab?: Array<{ __typename?: 'Bucket', name?: string | null, id?: string | null } | null> | null };

export type TabQueryVariables = Exact<{
  id: Scalars['ID'];
  unasociated?: InputMaybe<Scalars['Boolean']>;
}>;


export type TabQuery = { __typename?: 'Query', tab?: { __typename?: 'Tab', id?: string | null, name?: string | null, description?: string | null, priority?: number | null, tokenTabs?: { __typename?: 'DefaultConnection_TokenTab', edges?: Array<{ __typename?: 'DefaultEdge_TokenTab', node?: { __typename?: 'TokenTab', name?: string | null, id?: string | null } | null } | null> | null } | null, translations?: Array<{ __typename?: 'TranslationDTO', key?: string | null, language?: string | null, value?: string | null, description?: string | null } | null> | null } | null };

export type CreateOrUpdateTabMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  priority: Scalars['Int'];
  tokenTabIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
}>;


export type CreateOrUpdateTabMutation = { __typename?: 'Mutation', tabWithTokenTabs?: { __typename?: 'Response_Tab', entity?: { __typename?: 'Tab', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type TabTokenTabsQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
}>;


export type TabTokenTabsQuery = { __typename?: 'Query', tab?: { __typename?: 'Tab', id?: string | null, tokenTabs?: { __typename?: 'DefaultConnection_TokenTab', edges?: Array<{ __typename?: 'DefaultEdge_TokenTab', node?: { __typename?: 'TokenTab', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type AddTokenTabToTabMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddTokenTabToTabMutation = { __typename?: 'Mutation', addTokenTabToTab?: { __typename?: 'Tuple2_Tab_TokenTab', left?: { __typename?: 'Tab', id?: string | null } | null, right?: { __typename?: 'TokenTab', id?: string | null } | null } | null };

export type RemoveTokenTabToTabMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveTokenTabToTabMutation = { __typename?: 'Mutation', removeTokenTabToTab?: { __typename?: 'Tuple2_Tab_TokenTab', left?: { __typename?: 'Tab', id?: string | null } | null, right?: { __typename?: 'TokenTab', id?: string | null } | null } | null };

export type TabTokensQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type TabTokensQuery = { __typename?: 'Query', totalTokenTabs?: { __typename?: 'DefaultConnection_TokenTab', edges?: Array<{ __typename?: 'DefaultEdge_TokenTab', node?: { __typename?: 'TokenTab', id?: string | null, name?: string | null, tokenType?: TokenType | null, value?: string | null, filter?: boolean | null, extraParams?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type TokenFiltersQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type TokenFiltersQuery = { __typename?: 'Query', tokenFilters?: { __typename?: 'DefaultConnection_TokenFilter', edges?: Array<{ __typename?: 'DefaultEdge_TokenFilter', node?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteTokenFiltersMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteTokenFiltersMutation = { __typename?: 'Mutation', deleteTokenFilter?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null } | null };

export type AddTokenFilterToAnalyzerMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddTokenFilterToAnalyzerMutation = { __typename?: 'Mutation', addTokenFilterToAnalyzer?: { __typename?: 'Tuple2_Analyzer_TokenFilter', left?: { __typename?: 'Analyzer', id?: string | null } | null, right?: { __typename?: 'TokenFilter', id?: string | null } | null } | null };

export type UnboundAnalyzersByTokenFilterQueryVariables = Exact<{
  tokenFilterId: Scalars['BigInteger'];
}>;


export type UnboundAnalyzersByTokenFilterQuery = { __typename?: 'Query', unboundAnalyzersByTokenFilter?: Array<{ __typename?: 'Analyzer', name?: string | null, id?: string | null } | null> | null };

export type TokenFilterQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TokenFilterQuery = { __typename?: 'Query', tokenFilter?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null, description?: string | null, jsonConfig?: string | null, type?: string | null } | null };

export type CreateOrUpdateTokenFilterMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  type: Scalars['String'];
}>;


export type CreateOrUpdateTokenFilterMutation = { __typename?: 'Mutation', tokenFilter?: { __typename?: 'Response_TokenFilter', entity?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type TabTokenTabQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TabTokenTabQuery = { __typename?: 'Query', tokenTab?: { __typename?: 'TokenTab', id?: string | null, name?: string | null, description?: string | null, value?: string | null, filter?: boolean | null, tokenType?: TokenType | null, extraParams?: string | null, docTypeField?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null } | null } | null };

export type CreateOrUpdateTabTokenMutationVariables = Exact<{
  tokenTabId?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  value: Scalars['String'];
  filter: Scalars['Boolean'];
  tokenType: TokenType;
  docTypeFieldId?: InputMaybe<Scalars['BigInteger']>;
  extraParams?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateTabTokenMutation = { __typename?: 'Mutation', tokenTabWithDocTypeField?: { __typename?: 'Response_TokenTab', entity?: { __typename?: 'TokenTab', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DocTypeFieldValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DocTypeFieldValueQuery = { __typename?: 'Query', value?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null } | null };

export type DocTypeFieldOptionsTokenTabQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type DocTypeFieldOptionsTokenTabQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type TabTokensQueryQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type TabTokensQueryQuery = { __typename?: 'Query', totalTokenTabs?: { __typename?: 'DefaultConnection_TokenTab', edges?: Array<{ __typename?: 'DefaultEdge_TokenTab', node?: { __typename?: 'TokenTab', id?: string | null, name?: string | null, tokenType?: TokenType | null, value?: string | null, filter?: boolean | null, extraParams?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteTabTokenMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteTabTokenMutation = { __typename?: 'Mutation', deleteTokenTab?: { __typename?: 'TokenTab', id?: string | null, name?: string | null } | null };

export type UnassociatedTokenTabsInTabQueryVariables = Exact<{
  id: Scalars['BigInteger'];
}>;


export type UnassociatedTokenTabsInTabQuery = { __typename?: 'Query', unboundTabsByTokenTab?: Array<{ __typename?: 'Tab', id?: string | null, name?: string | null } | null> | null };

export type TokenizerQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TokenizerQuery = { __typename?: 'Query', tokenizer?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null, description?: string | null, jsonConfig?: string | null, type?: string | null } | null };

export type CreateOrUpdateTokenizerMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  type: Scalars['String'];
}>;


export type CreateOrUpdateTokenizerMutation = { __typename?: 'Mutation', tokenizer?: { __typename?: 'Response_Tokenizer', entity?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type TokenizersQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type TokenizersQuery = { __typename?: 'Query', tokenizers?: { __typename?: 'DefaultConnection_Tokenizer', edges?: Array<{ __typename?: 'DefaultEdge_Tokenizer', node?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteTokenizerMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteTokenizerMutation = { __typename?: 'Mutation', deleteTokenizer?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null } | null };

export type AnnotatorsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type AnnotatorsQuery = { __typename?: 'Query', annotators?: { __typename?: 'DefaultConnection_Annotator', edges?: Array<{ __typename?: 'DefaultEdge_Annotator', node?: { __typename?: 'Annotator', id?: string | null, name?: string | null, description?: string | null, size?: number | null, type?: AnnotatorType | null, fieldName?: string | null, fuziness?: Fuzziness | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteAnnotatosMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteAnnotatosMutation = { __typename?: 'Mutation', deleteAnnotator?: { __typename?: 'Annotator', id?: string | null, name?: string | null } | null };

export type DocTypeFieldOptionsAnnotatorsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type DocTypeFieldOptionsAnnotatorsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type AnnotatorQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type AnnotatorQuery = { __typename?: 'Query', annotator?: { __typename?: 'Annotator', id?: string | null, fuziness?: Fuzziness | null, size?: number | null, type?: AnnotatorType | null, description?: string | null, name?: string | null, fieldName?: string | null, extraParams?: string | null, docTypeField?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null } | null } | null };

export type CreateOrUpdateAnnotatorMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  fieldName: Scalars['String'];
  fuziness: Fuzziness;
  type: AnnotatorType;
  description?: InputMaybe<Scalars['String']>;
  size?: InputMaybe<Scalars['Int']>;
  name: Scalars['String'];
  docTypeFieldId?: InputMaybe<Scalars['BigInteger']>;
  extraParams?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateAnnotatorMutation = { __typename?: 'Mutation', annotatorWithDocTypeField?: { __typename?: 'Response_Annotator', entity?: { __typename?: 'Annotator', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DocTypeFieldOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
  annotatorId: Scalars['ID'];
}>;


export type DocTypeFieldOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type BindDocTypeFieldToDataSourceMutationVariables = Exact<{
  documentTypeFieldId: Scalars['ID'];
  annotatorId: Scalars['ID'];
}>;


export type BindDocTypeFieldToDataSourceMutation = { __typename?: 'Mutation', bindAnnotatorToDocTypeField?: { __typename?: 'Tuple2_Annotator_DocTypeField', left?: { __typename?: 'Annotator', id?: string | null, docTypeField?: { __typename?: 'DocTypeField', id?: string | null } | null } | null, right?: { __typename?: 'DocTypeField', id?: string | null } | null } | null };

export type UnbindDocTypeFieldToDataSourceMutationVariables = Exact<{
  documentTypeFieldId: Scalars['ID'];
  annotatorId: Scalars['ID'];
}>;


export type UnbindDocTypeFieldToDataSourceMutation = { __typename?: 'Mutation', unbindAnnotatorFromDocTypeField?: { __typename?: 'Tuple2_Annotator_DocTypeField', left?: { __typename?: 'Annotator', id?: string | null } | null } | null };

export type BucketsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type BucketsQuery = { __typename?: 'Query', buckets?: { __typename?: 'DefaultConnection_Bucket', edges?: Array<{ __typename?: 'DefaultEdge_Bucket', node?: { __typename?: 'Bucket', id?: string | null, name?: string | null, description?: string | null, enabled: boolean } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type QueryAnalysisOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type QueryAnalysisOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_QueryAnalysis', edges?: Array<{ __typename?: 'DefaultEdge_QueryAnalysis', node?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type QueryAnalysisValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type QueryAnalysisValueQuery = { __typename?: 'Query', value?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null, description?: string | null } | null };

export type BindQueryAnalysisToBucketMutationVariables = Exact<{
  bucketId: Scalars['ID'];
  queryAnalysis: Scalars['ID'];
}>;


export type BindQueryAnalysisToBucketMutation = { __typename?: 'Mutation', bindQueryAnalysisToBucket?: { __typename?: 'Tuple2_Bucket_QueryAnalysis', left?: { __typename?: 'Bucket', id?: string | null, queryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null } | null } | null, right?: { __typename?: 'QueryAnalysis', id?: string | null } | null } | null };

export type UnbindQueryAnalysisFromBucketMutationVariables = Exact<{
  bucketId: Scalars['ID'];
}>;


export type UnbindQueryAnalysisFromBucketMutation = { __typename?: 'Mutation', unbindQueryAnalysisFromBucket?: { __typename?: 'Tuple2_Bucket_QueryAnalysis', right?: { __typename?: 'QueryAnalysis', id?: string | null } | null } | null };

export type BindSearchConfigToBucketMutationVariables = Exact<{
  bucketId: Scalars['ID'];
  searchConfigId: Scalars['ID'];
}>;


export type BindSearchConfigToBucketMutation = { __typename?: 'Mutation', bindSearchConfigToBucket?: { __typename?: 'Tuple2_Bucket_SearchConfig', left?: { __typename?: 'Bucket', id?: string | null, searchConfig?: { __typename?: 'SearchConfig', id?: string | null } | null } | null, right?: { __typename?: 'SearchConfig', id?: string | null } | null } | null };

export type UnbindSearchConfigFromBucketMutationVariables = Exact<{
  bucketId: Scalars['ID'];
}>;


export type UnbindSearchConfigFromBucketMutation = { __typename?: 'Mutation', unbindSearchConfigFromBucket?: { __typename?: 'Tuple2_Bucket_SearchConfig', right?: { __typename?: 'SearchConfig', id?: string | null } | null } | null };

export type BindLanguageToBucketMutationVariables = Exact<{
  bucketId: Scalars['ID'];
  languageId: Scalars['ID'];
}>;


export type BindLanguageToBucketMutation = { __typename?: 'Mutation', bindLanguageToBucket?: { __typename?: 'Tuple2_Bucket_Language', left?: { __typename?: 'Bucket', id?: string | null, language?: { __typename?: 'Language', id?: string | null } | null } | null, right?: { __typename?: 'Language', id?: string | null } | null } | null };

export type UnbindLanguageFromBucketMutationVariables = Exact<{
  bucketId: Scalars['ID'];
}>;


export type UnbindLanguageFromBucketMutation = { __typename?: 'Mutation', unbindLanguageFromBucket?: { __typename?: 'Tuple2_Bucket_Language', right?: { __typename?: 'Language', id?: string | null } | null } | null };

export type BucketQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type BucketQuery = { __typename?: 'Query', bucket?: { __typename?: 'Bucket', id?: string | null, name?: string | null, description?: string | null, enabled: boolean, refreshOnDate?: boolean | null, refreshOnQuery?: boolean | null, refreshOnSuggestionCategory?: boolean | null, refreshOnTab?: boolean | null, retrieveType?: RetrieveType | null, queryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null } | null, searchConfig?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null } | null, ragConfigurationChat?: { __typename?: 'RAGConfiguration', id?: string | null, name?: string | null } | null, ragConfigurationChatTool?: { __typename?: 'RAGConfiguration', id?: string | null, name?: string | null } | null, ragConfigurationSimpleGenerate?: { __typename?: 'RAGConfiguration', id?: string | null, name?: string | null } | null, language?: { __typename?: 'Language', id?: string | null, name?: string | null } | null } | null };

export type EnableBucketMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EnableBucketMutation = { __typename?: 'Mutation', enableBucket?: { __typename?: 'Bucket', id?: string | null, name?: string | null } | null };

export type DeleteBucketMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteBucketMutation = { __typename?: 'Mutation', deleteBucket?: { __typename?: 'Bucket', id?: string | null, name?: string | null } | null };

export type BucketDataSourcesQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type BucketDataSourcesQuery = { __typename?: 'Query', bucket?: { __typename?: 'Bucket', id?: string | null, tabs?: { __typename?: 'DefaultConnection_Tab', edges?: Array<{ __typename?: 'DefaultEdge_Tab', node?: { __typename?: 'Tab', name?: string | null, id?: string | null } | null } | null> | null } | null, suggestionCategories?: { __typename?: 'DefaultConnection_SuggestionCategory', edges?: Array<{ __typename?: 'DefaultEdge_SuggestionCategory', node?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null } | null } | null> | null } | null, datasources?: { __typename?: 'DefaultConnection_Datasource', edges?: Array<{ __typename?: 'DefaultEdge_Datasource', node?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type CreateOrUpdateBucketMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  refreshOnDate: Scalars['Boolean'];
  refreshOnQuery: Scalars['Boolean'];
  refreshOnSuggestionCategory: Scalars['Boolean'];
  refreshOnTab: Scalars['Boolean'];
  retrieveType: RetrieveType;
  datasourceIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
  suggestionCategoryIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
  tabIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
  queryAnalysisId?: InputMaybe<Scalars['BigInteger']>;
  defaultLanguageId?: InputMaybe<Scalars['BigInteger']>;
  searchConfigId?: InputMaybe<Scalars['BigInteger']>;
  ragConfigurationChat?: InputMaybe<Scalars['BigInteger']>;
  ragConfigurationChatTool?: InputMaybe<Scalars['BigInteger']>;
  ragConfigurationSimpleGenerate?: InputMaybe<Scalars['BigInteger']>;
}>;


export type CreateOrUpdateBucketMutation = { __typename?: 'Mutation', bucketWithLists?: { __typename?: 'Response_Bucket', entity?: { __typename?: 'Bucket', id?: string | null, name?: string | null, enabled: boolean } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type AddDataSourceToBucketMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddDataSourceToBucketMutation = { __typename?: 'Mutation', addDatasourceToBucket?: { __typename?: 'Tuple2_Bucket_Datasource', left?: { __typename?: 'Bucket', id?: string | null } | null, right?: { __typename?: 'Datasource', id?: string | null } | null } | null };

export type RemoveDataSourceFromBucketMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveDataSourceFromBucketMutation = { __typename?: 'Mutation', removeDatasourceFromBucket?: { __typename?: 'Tuple2_Bucket_Datasource', left?: { __typename?: 'Bucket', id?: string | null } | null, right?: { __typename?: 'Datasource', id?: string | null } | null } | null };

export type BucketLanguagesQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type BucketLanguagesQuery = { __typename?: 'Query', bucket?: { __typename?: 'Bucket', id?: string | null, languages?: { __typename?: 'DefaultConnection_Language', edges?: Array<{ __typename?: 'DefaultEdge_Language', node?: { __typename?: 'Language', id?: string | null, name?: string | null, value?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type AddLanguageToBucketMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddLanguageToBucketMutation = { __typename?: 'Mutation', addLanguageToBucket?: { __typename?: 'Tuple2_Bucket_Language', left?: { __typename?: 'Bucket', id?: string | null } | null, right?: { __typename?: 'Language', id?: string | null } | null } | null };

export type RemoveLanguageFromBucketMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveLanguageFromBucketMutation = { __typename?: 'Mutation', removeLanguageFromBucket?: { __typename?: 'Tuple2_Bucket_Language', left?: { __typename?: 'Bucket', id?: string | null } | null, right?: { __typename?: 'Language', id?: string | null } | null } | null };

export type BucketTabsQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type BucketTabsQuery = { __typename?: 'Query', bucket?: { __typename?: 'Bucket', id?: string | null, tabs?: { __typename?: 'DefaultConnection_Tab', edges?: Array<{ __typename?: 'DefaultEdge_Tab', node?: { __typename?: 'Tab', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type AddTabToBucketMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddTabToBucketMutation = { __typename?: 'Mutation', addTabToBucket?: { __typename?: 'Tuple2_Bucket_Tab', left?: { __typename?: 'Bucket', id?: string | null } | null, right?: { __typename?: 'Tab', id?: string | null } | null } | null };

export type RemoveTabFromBucketMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveTabFromBucketMutation = { __typename?: 'Mutation', removeTabFromBucket?: { __typename?: 'Tuple2_Bucket_Tab', left?: { __typename?: 'Bucket', id?: string | null } | null, right?: { __typename?: 'Tab', id?: string | null } | null } | null };

export type BucketSuggestionCategoriesQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type BucketSuggestionCategoriesQuery = { __typename?: 'Query', bucket?: { __typename?: 'Bucket', id?: string | null, suggestionCategories?: { __typename?: 'DefaultConnection_SuggestionCategory', edges?: Array<{ __typename?: 'DefaultEdge_SuggestionCategory', node?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type AddSuggestionCategoryToBucketMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddSuggestionCategoryToBucketMutation = { __typename?: 'Mutation', addSuggestionCategoryToBucket?: { __typename?: 'Tuple2_Bucket_SuggestionCategory', left?: { __typename?: 'Bucket', id?: string | null } | null, right?: { __typename?: 'SuggestionCategory', id?: string | null } | null } | null };

export type RemoveSuggestionCategoryFromBucketMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveSuggestionCategoryFromBucketMutation = { __typename?: 'Mutation', removeSuggestionCategoryFromBucket?: { __typename?: 'Tuple2_Bucket_SuggestionCategory', left?: { __typename?: 'Bucket', id?: string | null } | null, right?: { __typename?: 'SuggestionCategory', id?: string | null } | null } | null };

export type CreateDataIndexMutationVariables = Exact<{
  name: Scalars['String'];
  datasourceId: Scalars['ID'];
  description?: InputMaybe<Scalars['String']>;
  knnIndex?: InputMaybe<Scalars['Boolean']>;
  docTypeIds?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
  chunkType?: InputMaybe<ChunkType>;
  chunkWindowSize?: InputMaybe<Scalars['Int']>;
  embeddingJsonConfig?: InputMaybe<Scalars['String']>;
  embeddingDocTypeFieldId?: InputMaybe<Scalars['BigInteger']>;
  settings?: InputMaybe<Scalars['String']>;
}>;


export type CreateDataIndexMutation = { __typename?: 'Mutation', dataIndex?: { __typename?: 'Response_DataIndex', entity?: { __typename?: 'DataIndex', name?: string | null } | null } | null };

export type DataIndicesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type DataIndicesQuery = { __typename?: 'Query', dataIndices?: { __typename?: 'DefaultConnection_DataIndex', edges?: Array<{ __typename?: 'DefaultEdge_DataIndex', node?: { __typename?: 'DataIndex', id?: string | null, name?: string | null, description?: string | null, createDate?: any | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DataIndexQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DataIndexQuery = { __typename?: 'Query', dataIndex?: { __typename?: 'DataIndex', name?: string | null, description?: string | null, settings?: string | null, chunkType?: ChunkType | null, chunkWindowSize?: number | null, embeddingJsonConfig?: string | null, knnIndex?: boolean | null, datasource?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null, embeddingDocTypeField?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null } | null, docTypes?: { __typename?: 'DefaultConnection_DocType', edges?: Array<{ __typename?: 'DefaultEdge_DocType', node?: { __typename?: 'DocType', id?: string | null, name?: string | null } | null } | null> | null } | null, cat?: { __typename?: 'CatResponse', docsCount?: string | null, docsDeleted?: string | null, storeSize: any } | null } | null };

export type DataIndexMappingQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DataIndexMappingQuery = { __typename?: 'Query', dataIndex?: { __typename?: 'DataIndex', mappings?: string | null } | null };

export type DataSourcesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  sortByList?: InputMaybe<Array<SortByInput> | SortByInput>;
}>;


export type DataSourcesQuery = { __typename?: 'Query', datasources?: { __typename: 'DefaultConnection_Datasource', edges?: Array<{ __typename: 'DefaultEdge_Datasource', node?: { __typename: 'Datasource', id?: string | null, name?: string | null, schedulable?: boolean | null, lastIngestionDate?: any | null, scheduling?: string | null, jsonConfig?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteDataSourceMutationVariables = Exact<{
  id: Scalars['ID'];
  datasourceName: Scalars['String'];
}>;


export type DeleteDataSourceMutation = { __typename?: 'Mutation', deleteDatasource?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null };

export type UnboundBucketsByDatasourceQueryVariables = Exact<{
  datasourceId: Scalars['BigInteger'];
}>;


export type UnboundBucketsByDatasourceQuery = { __typename?: 'Query', unboundBucketsByDatasource?: Array<{ __typename?: 'Bucket', name?: string | null, id?: string | null } | null> | null };

export type DataSourceQueryVariables = Exact<{
  id: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
}>;


export type DataSourceQuery = { __typename?: 'Query', datasource?: { __typename?: 'Datasource', id?: string | null, name?: string | null, description?: string | null, schedulable?: boolean | null, scheduling?: string | null, jsonConfig?: string | null, reindexable?: boolean | null, reindexing?: string | null, purgeable?: boolean | null, purging?: string | null, purgeMaxAge?: string | null, lastIngestionDate?: any | null, pluginDriver?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null, provisioning?: Provisioning | null, jsonConfig?: string | null } | null, dataIndex?: { __typename?: 'DataIndex', id?: string | null, name?: string | null, description?: string | null, knnIndex?: boolean | null } | null, enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null } | null, dataIndexes?: { __typename?: 'DefaultConnection_DataIndex', edges?: Array<{ __typename?: 'DefaultEdge_DataIndex', node?: { __typename?: 'DataIndex', id?: string | null, name?: string | null } | null } | null> | null } | null } | null };

export type CreateDatasourceConnectionMutationVariables = Exact<{
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  schedulable: Scalars['Boolean'];
  scheduling: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
  pluginDriverId: Scalars['BigInteger'];
  pipeline?: InputMaybe<PipelineWithItemsDtoInput>;
  pipelineId?: InputMaybe<Scalars['BigInteger']>;
  reindexable: Scalars['Boolean'];
  reindexing: Scalars['String'];
  purgeable: Scalars['Boolean'];
  purging: Scalars['String'];
  purgeMaxAge: Scalars['String'];
  dataIndex: DataIndexDtoInput;
}>;


export type CreateDatasourceConnectionMutation = { __typename?: 'Mutation', createDatasourceConnection?: { __typename?: 'Response_Datasource', entity?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type UpdateDatasourceConnectionMutationVariables = Exact<{
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  schedulable: Scalars['Boolean'];
  scheduling: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
  pipeline?: InputMaybe<PipelineWithItemsDtoInput>;
  pipelineId?: InputMaybe<Scalars['BigInteger']>;
  dataIndexId: Scalars['BigInteger'];
  datasourceId: Scalars['BigInteger'];
  reindexable: Scalars['Boolean'];
  reindexing: Scalars['String'];
  purging: Scalars['String'];
  purgeable: Scalars['Boolean'];
  purgeMaxAge: Scalars['String'];
}>;


export type UpdateDatasourceConnectionMutation = { __typename?: 'Mutation', updateDatasourceConnection?: { __typename?: 'Response_Datasource', entity?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type QDatasourceSchedulersQueryVariables = Exact<{
  id: Scalars['ID'];
  first?: InputMaybe<Scalars['Int']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type QDatasourceSchedulersQuery = { __typename?: 'Query', datasource?: { __typename?: 'Datasource', id?: string | null, schedulers?: { __typename?: 'DefaultConnection_Scheduler', edges?: Array<{ __typename?: 'DefaultEdge_Scheduler', node?: { __typename?: 'Scheduler', id?: string | null, status?: SchedulerStatus | null, modifiedDate?: any | null } | null } | null> | null, pageInfo?: { __typename: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type DataSourceInformationQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DataSourceInformationQuery = { __typename?: 'Query', datasource?: { __typename?: 'Datasource', dataIndex?: { __typename?: 'DataIndex', cat?: { __typename?: 'CatResponse', docsCount?: string | null, docsDeleted?: string | null, health?: string | null, index?: string | null, pri?: string | null, priStoreSize: any, rep?: string | null, status?: string | null, storeSize: any, uuid?: string | null } | null } | null } | null };

export type EnrichPipelineOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type EnrichPipelineOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_EnrichPipeline', edges?: Array<{ __typename?: 'DefaultEdge_EnrichPipeline', node?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type EnrichItemsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type EnrichItemsQuery = { __typename?: 'Query', enrichItems?: { __typename?: 'DefaultConnection_EnrichItem', edges?: Array<{ __typename?: 'DefaultEdge_EnrichItem', node?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null, description?: string | null, type?: EnrichItemType | null, serviceName?: string | null, jsonConfig?: string | null, script?: string | null, behaviorMergeType?: BehaviorMergeType | null, jsonPath?: string | null, behaviorOnError?: BehaviorOnError | null, requestTimeout?: any | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type PluginDriversQueryVariables = Exact<{ [key: string]: never; }>;


export type PluginDriversQuery = { __typename?: 'Query', pluginDriversPageFilter?: { __typename?: 'Page_PluginDriver', content?: Array<{ __typename?: 'PluginDriver', id?: string | null, name?: string | null, description?: string | null, jsonConfig?: string | null, provisioning?: Provisioning | null, type?: PluginDriverType | null } | null> | null } | null };

export type CreateOrUpdatePluginDriverMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: PluginDriverType;
  jsonConfig?: InputMaybe<Scalars['String']>;
  provisioning?: InputMaybe<Provisioning>;
}>;


export type CreateOrUpdatePluginDriverMutation = { __typename?: 'Mutation', pluginDriver?: { __typename?: 'Response_PluginDriver', entity?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type EnrichPipelinesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  after?: InputMaybe<Scalars['String']>;
}>;


export type EnrichPipelinesQuery = { __typename?: 'Query', enrichPipelines?: { __typename?: 'DefaultConnection_EnrichPipeline', edges?: Array<{ __typename?: 'DefaultEdge_EnrichPipeline', node?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type EnrichPipelinesValueOptionsQueryVariables = Exact<{
  id: Scalars['BigInteger'];
}>;


export type EnrichPipelinesValueOptionsQuery = { __typename?: 'Query', unboundEnrichPipelines?: Array<{ __typename?: 'EnrichPipeline', name?: string | null, id?: string | null } | null> | null };

export type DeleteEnrichPipelineMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteEnrichPipelineMutation = { __typename?: 'Mutation', deleteEnrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null } | null };

export type EnrichPipelineQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EnrichPipelineQuery = { __typename?: 'Query', enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null, description?: string | null } | null };

export type AssociatedEnrichPipelineEnrichItemsQueryVariables = Exact<{
  enrichPipelineId: Scalars['ID'];
}>;


export type AssociatedEnrichPipelineEnrichItemsQuery = { __typename?: 'Query', enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null, enrichItems?: { __typename?: 'DefaultConnection_EnrichItem', edges?: Array<{ __typename?: 'DefaultEdge_EnrichItem', node?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type UnassociatedEnrichPipelineEnrichItemsQueryVariables = Exact<{
  enrichPipelineId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
}>;


export type UnassociatedEnrichPipelineEnrichItemsQuery = { __typename?: 'Query', enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null, enrichItems?: { __typename?: 'DefaultConnection_EnrichItem', edges?: Array<{ __typename?: 'DefaultEdge_EnrichItem', node?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type CreateOrUpdateEnrichPipelineMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateEnrichPipelineMutation = { __typename?: 'Mutation', enrichPipeline?: { __typename?: 'Response_EnrichPipeline', entity?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type AddEnrichItemToEnrichPipelineMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddEnrichItemToEnrichPipelineMutation = { __typename?: 'Mutation', addEnrichItemToEnrichPipeline?: { __typename?: 'Tuple2_EnrichPipeline_EnrichItem', left?: { __typename?: 'EnrichPipeline', id?: string | null } | null, right?: { __typename?: 'EnrichItem', id?: string | null } | null } | null };

export type RemoveEnrichItemFromEnrichPipelineMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveEnrichItemFromEnrichPipelineMutation = { __typename?: 'Mutation', removeEnrichItemFromEnrichPipeline?: { __typename?: 'Tuple2_EnrichPipeline_EnrichItem', left?: { __typename?: 'EnrichPipeline', id?: string | null } | null, right?: { __typename?: 'EnrichItem', id?: string | null } | null } | null };

export type SortEnrichItemsMutationVariables = Exact<{
  enrichPipelineId: Scalars['ID'];
  enrichItemIdList?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>> | InputMaybe<Scalars['BigInteger']>>;
}>;


export type SortEnrichItemsMutation = { __typename?: 'Mutation', sortEnrichItems?: { __typename?: 'EnrichPipeline', id?: string | null, enrichItems?: { __typename?: 'DefaultConnection_EnrichItem', edges?: Array<{ __typename?: 'DefaultEdge_EnrichItem', node?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null } | null } | null };

export type EnrichPipelineWithItemsMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  items?: InputMaybe<Array<InputMaybe<ItemDtoInput>> | InputMaybe<ItemDtoInput>>;
  name: Scalars['String'];
  description: Scalars['String'];
}>;


export type EnrichPipelineWithItemsMutation = { __typename?: 'Mutation', enrichPipelineWithEnrichItems?: { __typename?: 'Response_EnrichPipeline', entity?: { __typename?: 'EnrichPipeline', name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };


export const LanguagesDocument = gql`
    query Languages($searchText: String, $cursor: String) {
  languages(searchText: $searchText, first: 20, after: $cursor) {
    edges {
      node {
        id
        name
        value
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useLanguagesQuery__
 *
 * To run a query within a React component, call `useLanguagesQuery` and pass it any options that fit your needs.
 * When your component renders, `useLanguagesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useLanguagesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useLanguagesQuery(baseOptions?: Apollo.QueryHookOptions<LanguagesQuery, LanguagesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<LanguagesQuery, LanguagesQueryVariables>(LanguagesDocument, options);
      }
export function useLanguagesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<LanguagesQuery, LanguagesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<LanguagesQuery, LanguagesQueryVariables>(LanguagesDocument, options);
        }
export type LanguagesQueryHookResult = ReturnType<typeof useLanguagesQuery>;
export type LanguagesLazyQueryHookResult = ReturnType<typeof useLanguagesLazyQuery>;
export type LanguagesQueryResult = Apollo.QueryResult<LanguagesQuery, LanguagesQueryVariables>;
export const AnalyzersDocument = gql`
    query Analyzers($searchText: String, $after: String) {
  analyzers(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        type
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useAnalyzersQuery__
 *
 * To run a query within a React component, call `useAnalyzersQuery` and pass it any options that fit your needs.
 * When your component renders, `useAnalyzersQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAnalyzersQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useAnalyzersQuery(baseOptions?: Apollo.QueryHookOptions<AnalyzersQuery, AnalyzersQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzersQuery, AnalyzersQueryVariables>(AnalyzersDocument, options);
      }
export function useAnalyzersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzersQuery, AnalyzersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzersQuery, AnalyzersQueryVariables>(AnalyzersDocument, options);
        }
export type AnalyzersQueryHookResult = ReturnType<typeof useAnalyzersQuery>;
export type AnalyzersLazyQueryHookResult = ReturnType<typeof useAnalyzersLazyQuery>;
export type AnalyzersQueryResult = Apollo.QueryResult<AnalyzersQuery, AnalyzersQueryVariables>;
export const AnalyzerOptionsDocument = gql`
    query AnalyzerOptions($searchText: String, $cursor: String) {
  options: analyzers(searchText: $searchText, first: 5, after: $cursor) {
    edges {
      node {
        name
        description
        type
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useAnalyzerOptionsQuery__
 *
 * To run a query within a React component, call `useAnalyzerOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useAnalyzerOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAnalyzerOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useAnalyzerOptionsQuery(baseOptions?: Apollo.QueryHookOptions<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>(AnalyzerOptionsDocument, options);
      }
export function useAnalyzerOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>(AnalyzerOptionsDocument, options);
        }
export type AnalyzerOptionsQueryHookResult = ReturnType<typeof useAnalyzerOptionsQuery>;
export type AnalyzerOptionsLazyQueryHookResult = ReturnType<typeof useAnalyzerOptionsLazyQuery>;
export type AnalyzerOptionsQueryResult = Apollo.QueryResult<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>;
export const AnalyzerValueDocument = gql`
    query analyzerValue($id: ID!) {
  value: analyzer(id: $id) {
    id
    name
    description
    type
  }
}
    `;

/**
 * __useAnalyzerValueQuery__
 *
 * To run a query within a React component, call `useAnalyzerValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useAnalyzerValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAnalyzerValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useAnalyzerValueQuery(baseOptions: Apollo.QueryHookOptions<AnalyzerValueQuery, AnalyzerValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzerValueQuery, AnalyzerValueQueryVariables>(AnalyzerValueDocument, options);
      }
export function useAnalyzerValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzerValueQuery, AnalyzerValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzerValueQuery, AnalyzerValueQueryVariables>(AnalyzerValueDocument, options);
        }
export type AnalyzerValueQueryHookResult = ReturnType<typeof useAnalyzerValueQuery>;
export type AnalyzerValueLazyQueryHookResult = ReturnType<typeof useAnalyzerValueLazyQuery>;
export type AnalyzerValueQueryResult = Apollo.QueryResult<AnalyzerValueQuery, AnalyzerValueQueryVariables>;
export const BindAnalyzerToDocTypeFieldDocument = gql`
    mutation BindAnalyzerToDocTypeField($analyzerId: ID!, $docTypeFieldId: ID!) {
  bindAnalyzerToDocTypeField(
    analyzerId: $analyzerId
    docTypeFieldId: $docTypeFieldId
  ) {
    left {
      id
      docType {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindAnalyzerToDocTypeFieldMutationFn = Apollo.MutationFunction<BindAnalyzerToDocTypeFieldMutation, BindAnalyzerToDocTypeFieldMutationVariables>;

/**
 * __useBindAnalyzerToDocTypeFieldMutation__
 *
 * To run a mutation, you first call `useBindAnalyzerToDocTypeFieldMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindAnalyzerToDocTypeFieldMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindAnalyzerToDocTypeFieldMutation, { data, loading, error }] = useBindAnalyzerToDocTypeFieldMutation({
 *   variables: {
 *      analyzerId: // value for 'analyzerId'
 *      docTypeFieldId: // value for 'docTypeFieldId'
 *   },
 * });
 */
export function useBindAnalyzerToDocTypeFieldMutation(baseOptions?: Apollo.MutationHookOptions<BindAnalyzerToDocTypeFieldMutation, BindAnalyzerToDocTypeFieldMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindAnalyzerToDocTypeFieldMutation, BindAnalyzerToDocTypeFieldMutationVariables>(BindAnalyzerToDocTypeFieldDocument, options);
      }
export type BindAnalyzerToDocTypeFieldMutationHookResult = ReturnType<typeof useBindAnalyzerToDocTypeFieldMutation>;
export type BindAnalyzerToDocTypeFieldMutationResult = Apollo.MutationResult<BindAnalyzerToDocTypeFieldMutation>;
export type BindAnalyzerToDocTypeFieldMutationOptions = Apollo.BaseMutationOptions<BindAnalyzerToDocTypeFieldMutation, BindAnalyzerToDocTypeFieldMutationVariables>;
export const UnbindQueryAnalysisFromDocTypeFieldDocument = gql`
    mutation UnbindQueryAnalysisFromDocTypeField($docTypeFieldId: ID!) {
  unbindAnalyzerFromDocTypeField(docTypeFieldId: $docTypeFieldId) {
    right {
      id
    }
  }
}
    `;
export type UnbindQueryAnalysisFromDocTypeFieldMutationFn = Apollo.MutationFunction<UnbindQueryAnalysisFromDocTypeFieldMutation, UnbindQueryAnalysisFromDocTypeFieldMutationVariables>;

/**
 * __useUnbindQueryAnalysisFromDocTypeFieldMutation__
 *
 * To run a mutation, you first call `useUnbindQueryAnalysisFromDocTypeFieldMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindQueryAnalysisFromDocTypeFieldMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindQueryAnalysisFromDocTypeFieldMutation, { data, loading, error }] = useUnbindQueryAnalysisFromDocTypeFieldMutation({
 *   variables: {
 *      docTypeFieldId: // value for 'docTypeFieldId'
 *   },
 * });
 */
export function useUnbindQueryAnalysisFromDocTypeFieldMutation(baseOptions?: Apollo.MutationHookOptions<UnbindQueryAnalysisFromDocTypeFieldMutation, UnbindQueryAnalysisFromDocTypeFieldMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindQueryAnalysisFromDocTypeFieldMutation, UnbindQueryAnalysisFromDocTypeFieldMutationVariables>(UnbindQueryAnalysisFromDocTypeFieldDocument, options);
      }
export type UnbindQueryAnalysisFromDocTypeFieldMutationHookResult = ReturnType<typeof useUnbindQueryAnalysisFromDocTypeFieldMutation>;
export type UnbindQueryAnalysisFromDocTypeFieldMutationResult = Apollo.MutationResult<UnbindQueryAnalysisFromDocTypeFieldMutation>;
export type UnbindQueryAnalysisFromDocTypeFieldMutationOptions = Apollo.BaseMutationOptions<UnbindQueryAnalysisFromDocTypeFieldMutation, UnbindQueryAnalysisFromDocTypeFieldMutationVariables>;
export const SearchConfigOptionsDocument = gql`
    query SearchConfigOptions($searchText: String, $cursor: String) {
  options: searchConfigs(searchText: $searchText, first: 5, after: $cursor) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useSearchConfigOptionsQuery__
 *
 * To run a query within a React component, call `useSearchConfigOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useSearchConfigOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSearchConfigOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useSearchConfigOptionsQuery(baseOptions?: Apollo.QueryHookOptions<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>(SearchConfigOptionsDocument, options);
      }
export function useSearchConfigOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>(SearchConfigOptionsDocument, options);
        }
export type SearchConfigOptionsQueryHookResult = ReturnType<typeof useSearchConfigOptionsQuery>;
export type SearchConfigOptionsLazyQueryHookResult = ReturnType<typeof useSearchConfigOptionsLazyQuery>;
export type SearchConfigOptionsQueryResult = Apollo.QueryResult<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>;
export const SearchConfigValueDocument = gql`
    query SearchConfigValue($id: ID!) {
  value: searchConfig(id: $id) {
    id
    name
    description
  }
}
    `;

/**
 * __useSearchConfigValueQuery__
 *
 * To run a query within a React component, call `useSearchConfigValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useSearchConfigValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSearchConfigValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useSearchConfigValueQuery(baseOptions: Apollo.QueryHookOptions<SearchConfigValueQuery, SearchConfigValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SearchConfigValueQuery, SearchConfigValueQueryVariables>(SearchConfigValueDocument, options);
      }
export function useSearchConfigValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SearchConfigValueQuery, SearchConfigValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SearchConfigValueQuery, SearchConfigValueQueryVariables>(SearchConfigValueDocument, options);
        }
export type SearchConfigValueQueryHookResult = ReturnType<typeof useSearchConfigValueQuery>;
export type SearchConfigValueLazyQueryHookResult = ReturnType<typeof useSearchConfigValueLazyQuery>;
export type SearchConfigValueQueryResult = Apollo.QueryResult<SearchConfigValueQuery, SearchConfigValueQueryVariables>;
export const BindTokenizerToAnalyzerDocument = gql`
    mutation BindTokenizerToAnalyzer($analyzerId: ID!, $tokenizerId: ID!) {
  bindTokenizerToAnalyzer(analyzerId: $analyzerId, tokenizerId: $tokenizerId) {
    left {
      id
      tokenizer {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindTokenizerToAnalyzerMutationFn = Apollo.MutationFunction<BindTokenizerToAnalyzerMutation, BindTokenizerToAnalyzerMutationVariables>;

/**
 * __useBindTokenizerToAnalyzerMutation__
 *
 * To run a mutation, you first call `useBindTokenizerToAnalyzerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindTokenizerToAnalyzerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindTokenizerToAnalyzerMutation, { data, loading, error }] = useBindTokenizerToAnalyzerMutation({
 *   variables: {
 *      analyzerId: // value for 'analyzerId'
 *      tokenizerId: // value for 'tokenizerId'
 *   },
 * });
 */
export function useBindTokenizerToAnalyzerMutation(baseOptions?: Apollo.MutationHookOptions<BindTokenizerToAnalyzerMutation, BindTokenizerToAnalyzerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindTokenizerToAnalyzerMutation, BindTokenizerToAnalyzerMutationVariables>(BindTokenizerToAnalyzerDocument, options);
      }
export type BindTokenizerToAnalyzerMutationHookResult = ReturnType<typeof useBindTokenizerToAnalyzerMutation>;
export type BindTokenizerToAnalyzerMutationResult = Apollo.MutationResult<BindTokenizerToAnalyzerMutation>;
export type BindTokenizerToAnalyzerMutationOptions = Apollo.BaseMutationOptions<BindTokenizerToAnalyzerMutation, BindTokenizerToAnalyzerMutationVariables>;
export const UnbindTokenizerFromAnalyzerDocument = gql`
    mutation UnbindTokenizerFromAnalyzer($analyzerId: ID!) {
  unbindTokenizerFromAnalyzer(analyzerId: $analyzerId) {
    right {
      id
    }
  }
}
    `;
export type UnbindTokenizerFromAnalyzerMutationFn = Apollo.MutationFunction<UnbindTokenizerFromAnalyzerMutation, UnbindTokenizerFromAnalyzerMutationVariables>;

/**
 * __useUnbindTokenizerFromAnalyzerMutation__
 *
 * To run a mutation, you first call `useUnbindTokenizerFromAnalyzerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindTokenizerFromAnalyzerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindTokenizerFromAnalyzerMutation, { data, loading, error }] = useUnbindTokenizerFromAnalyzerMutation({
 *   variables: {
 *      analyzerId: // value for 'analyzerId'
 *   },
 * });
 */
export function useUnbindTokenizerFromAnalyzerMutation(baseOptions?: Apollo.MutationHookOptions<UnbindTokenizerFromAnalyzerMutation, UnbindTokenizerFromAnalyzerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindTokenizerFromAnalyzerMutation, UnbindTokenizerFromAnalyzerMutationVariables>(UnbindTokenizerFromAnalyzerDocument, options);
      }
export type UnbindTokenizerFromAnalyzerMutationHookResult = ReturnType<typeof useUnbindTokenizerFromAnalyzerMutation>;
export type UnbindTokenizerFromAnalyzerMutationResult = Apollo.MutationResult<UnbindTokenizerFromAnalyzerMutation>;
export type UnbindTokenizerFromAnalyzerMutationOptions = Apollo.BaseMutationOptions<UnbindTokenizerFromAnalyzerMutation, UnbindTokenizerFromAnalyzerMutationVariables>;
export const LanguagesOptionsDocument = gql`
    query LanguagesOptions($searchText: String, $cursor: String) {
  options: languages(searchText: $searchText, after: $cursor) {
    edges {
      node {
        id
        name
        value
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useLanguagesOptionsQuery__
 *
 * To run a query within a React component, call `useLanguagesOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useLanguagesOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useLanguagesOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useLanguagesOptionsQuery(baseOptions?: Apollo.QueryHookOptions<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>(LanguagesOptionsDocument, options);
      }
export function useLanguagesOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>(LanguagesOptionsDocument, options);
        }
export type LanguagesOptionsQueryHookResult = ReturnType<typeof useLanguagesOptionsQuery>;
export type LanguagesOptionsLazyQueryHookResult = ReturnType<typeof useLanguagesOptionsLazyQuery>;
export type LanguagesOptionsQueryResult = Apollo.QueryResult<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>;
export const LanguageValueDocument = gql`
    query LanguageValue($id: ID!) {
  value: language(id: $id) {
    id
    name
    value
  }
}
    `;

/**
 * __useLanguageValueQuery__
 *
 * To run a query within a React component, call `useLanguageValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useLanguageValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useLanguageValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useLanguageValueQuery(baseOptions: Apollo.QueryHookOptions<LanguageValueQuery, LanguageValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<LanguageValueQuery, LanguageValueQueryVariables>(LanguageValueDocument, options);
      }
export function useLanguageValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<LanguageValueQuery, LanguageValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<LanguageValueQuery, LanguageValueQueryVariables>(LanguageValueDocument, options);
        }
export type LanguageValueQueryHookResult = ReturnType<typeof useLanguageValueQuery>;
export type LanguageValueLazyQueryHookResult = ReturnType<typeof useLanguageValueLazyQuery>;
export type LanguageValueQueryResult = Apollo.QueryResult<LanguageValueQuery, LanguageValueQueryVariables>;
export const AnalyzerDocument = gql`
    query Analyzer($id: ID!) {
  analyzer(id: $id) {
    id
    name
    description
    type
    jsonConfig
    tokenizer {
      id
      name
    }
  }
}
    `;

/**
 * __useAnalyzerQuery__
 *
 * To run a query within a React component, call `useAnalyzerQuery` and pass it any options that fit your needs.
 * When your component renders, `useAnalyzerQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAnalyzerQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useAnalyzerQuery(baseOptions: Apollo.QueryHookOptions<AnalyzerQuery, AnalyzerQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzerQuery, AnalyzerQueryVariables>(AnalyzerDocument, options);
      }
export function useAnalyzerLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzerQuery, AnalyzerQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzerQuery, AnalyzerQueryVariables>(AnalyzerDocument, options);
        }
export type AnalyzerQueryHookResult = ReturnType<typeof useAnalyzerQuery>;
export type AnalyzerLazyQueryHookResult = ReturnType<typeof useAnalyzerLazyQuery>;
export type AnalyzerQueryResult = Apollo.QueryResult<AnalyzerQuery, AnalyzerQueryVariables>;
export const DeleteAnalyzerDocument = gql`
    mutation DeleteAnalyzer($id: ID!) {
  deleteAnalyzer(analyzerId: $id) {
    id
    name
  }
}
    `;
export type DeleteAnalyzerMutationFn = Apollo.MutationFunction<DeleteAnalyzerMutation, DeleteAnalyzerMutationVariables>;

/**
 * __useDeleteAnalyzerMutation__
 *
 * To run a mutation, you first call `useDeleteAnalyzerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteAnalyzerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteAnalyzerMutation, { data, loading, error }] = useDeleteAnalyzerMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteAnalyzerMutation(baseOptions?: Apollo.MutationHookOptions<DeleteAnalyzerMutation, DeleteAnalyzerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteAnalyzerMutation, DeleteAnalyzerMutationVariables>(DeleteAnalyzerDocument, options);
      }
export type DeleteAnalyzerMutationHookResult = ReturnType<typeof useDeleteAnalyzerMutation>;
export type DeleteAnalyzerMutationResult = Apollo.MutationResult<DeleteAnalyzerMutation>;
export type DeleteAnalyzerMutationOptions = Apollo.BaseMutationOptions<DeleteAnalyzerMutation, DeleteAnalyzerMutationVariables>;
export const CreateOrUpdateAnalyzerDocument = gql`
    mutation CreateOrUpdateAnalyzer($id: ID, $name: String!, $description: String, $type: String!, $tokenFilterIds: [BigInteger], $charFilterIds: [BigInteger], $tokenizerId: BigInteger, $jsonConfig: String) {
  analyzerWithLists(
    id: $id
    analyzerWithListsDTO: {name: $name, type: $type, description: $description, tokenFilterIds: $tokenFilterIds, charFilterIds: $charFilterIds, tokenizerId: $tokenizerId, jsonConfig: $jsonConfig}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateAnalyzerMutationFn = Apollo.MutationFunction<CreateOrUpdateAnalyzerMutation, CreateOrUpdateAnalyzerMutationVariables>;

/**
 * __useCreateOrUpdateAnalyzerMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateAnalyzerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateAnalyzerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateAnalyzerMutation, { data, loading, error }] = useCreateOrUpdateAnalyzerMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      type: // value for 'type'
 *      tokenFilterIds: // value for 'tokenFilterIds'
 *      charFilterIds: // value for 'charFilterIds'
 *      tokenizerId: // value for 'tokenizerId'
 *      jsonConfig: // value for 'jsonConfig'
 *   },
 * });
 */
export function useCreateOrUpdateAnalyzerMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateAnalyzerMutation, CreateOrUpdateAnalyzerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateAnalyzerMutation, CreateOrUpdateAnalyzerMutationVariables>(CreateOrUpdateAnalyzerDocument, options);
      }
export type CreateOrUpdateAnalyzerMutationHookResult = ReturnType<typeof useCreateOrUpdateAnalyzerMutation>;
export type CreateOrUpdateAnalyzerMutationResult = Apollo.MutationResult<CreateOrUpdateAnalyzerMutation>;
export type CreateOrUpdateAnalyzerMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateAnalyzerMutation, CreateOrUpdateAnalyzerMutationVariables>;
export const AnalyzersAssociationsDocument = gql`
    query AnalyzersAssociations($parentId: ID!, $unassociated: Boolean!) {
  analyzer(id: $parentId) {
    id
    charFilters(notEqual: $unassociated) {
      edges {
        node {
          id
          name
        }
      }
    }
    tokenFilters(notEqual: $unassociated) {
      edges {
        node {
          id
          name
        }
      }
    }
  }
}
    `;

/**
 * __useAnalyzersAssociationsQuery__
 *
 * To run a query within a React component, call `useAnalyzersAssociationsQuery` and pass it any options that fit your needs.
 * When your component renders, `useAnalyzersAssociationsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAnalyzersAssociationsQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      unassociated: // value for 'unassociated'
 *   },
 * });
 */
export function useAnalyzersAssociationsQuery(baseOptions: Apollo.QueryHookOptions<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>(AnalyzersAssociationsDocument, options);
      }
export function useAnalyzersAssociationsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>(AnalyzersAssociationsDocument, options);
        }
export type AnalyzersAssociationsQueryHookResult = ReturnType<typeof useAnalyzersAssociationsQuery>;
export type AnalyzersAssociationsLazyQueryHookResult = ReturnType<typeof useAnalyzersAssociationsLazyQuery>;
export type AnalyzersAssociationsQueryResult = Apollo.QueryResult<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>;
export const CharfiltersDocument = gql`
    query Charfilters($searchText: String, $after: String) {
  charFilters(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useCharfiltersQuery__
 *
 * To run a query within a React component, call `useCharfiltersQuery` and pass it any options that fit your needs.
 * When your component renders, `useCharfiltersQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useCharfiltersQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useCharfiltersQuery(baseOptions?: Apollo.QueryHookOptions<CharfiltersQuery, CharfiltersQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<CharfiltersQuery, CharfiltersQueryVariables>(CharfiltersDocument, options);
      }
export function useCharfiltersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<CharfiltersQuery, CharfiltersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<CharfiltersQuery, CharfiltersQueryVariables>(CharfiltersDocument, options);
        }
export type CharfiltersQueryHookResult = ReturnType<typeof useCharfiltersQuery>;
export type CharfiltersLazyQueryHookResult = ReturnType<typeof useCharfiltersLazyQuery>;
export type CharfiltersQueryResult = Apollo.QueryResult<CharfiltersQuery, CharfiltersQueryVariables>;
export const DeleteCharFiltersDocument = gql`
    mutation DeleteCharFilters($id: ID!) {
  deleteCharFilter(charFilterId: $id) {
    id
    name
  }
}
    `;
export type DeleteCharFiltersMutationFn = Apollo.MutationFunction<DeleteCharFiltersMutation, DeleteCharFiltersMutationVariables>;

/**
 * __useDeleteCharFiltersMutation__
 *
 * To run a mutation, you first call `useDeleteCharFiltersMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteCharFiltersMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteCharFiltersMutation, { data, loading, error }] = useDeleteCharFiltersMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteCharFiltersMutation(baseOptions?: Apollo.MutationHookOptions<DeleteCharFiltersMutation, DeleteCharFiltersMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteCharFiltersMutation, DeleteCharFiltersMutationVariables>(DeleteCharFiltersDocument, options);
      }
export type DeleteCharFiltersMutationHookResult = ReturnType<typeof useDeleteCharFiltersMutation>;
export type DeleteCharFiltersMutationResult = Apollo.MutationResult<DeleteCharFiltersMutation>;
export type DeleteCharFiltersMutationOptions = Apollo.BaseMutationOptions<DeleteCharFiltersMutation, DeleteCharFiltersMutationVariables>;
export const UnboundAnalyzersByCharFilterDocument = gql`
    query UnboundAnalyzersByCharFilter($charFilterId: BigInteger!) {
  unboundAnalyzersByCharFilter(charFilterId: $charFilterId) {
    name
    id
  }
}
    `;

/**
 * __useUnboundAnalyzersByCharFilterQuery__
 *
 * To run a query within a React component, call `useUnboundAnalyzersByCharFilterQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundAnalyzersByCharFilterQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundAnalyzersByCharFilterQuery({
 *   variables: {
 *      charFilterId: // value for 'charFilterId'
 *   },
 * });
 */
export function useUnboundAnalyzersByCharFilterQuery(baseOptions: Apollo.QueryHookOptions<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>(UnboundAnalyzersByCharFilterDocument, options);
      }
export function useUnboundAnalyzersByCharFilterLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>(UnboundAnalyzersByCharFilterDocument, options);
        }
export type UnboundAnalyzersByCharFilterQueryHookResult = ReturnType<typeof useUnboundAnalyzersByCharFilterQuery>;
export type UnboundAnalyzersByCharFilterLazyQueryHookResult = ReturnType<typeof useUnboundAnalyzersByCharFilterLazyQuery>;
export type UnboundAnalyzersByCharFilterQueryResult = Apollo.QueryResult<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>;
export const AddCharFiltersToAnalyzerDocument = gql`
    mutation AddCharFiltersToAnalyzer($childId: ID!, $parentId: ID!) {
  addCharFilterToAnalyzer(charFilterId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddCharFiltersToAnalyzerMutationFn = Apollo.MutationFunction<AddCharFiltersToAnalyzerMutation, AddCharFiltersToAnalyzerMutationVariables>;

/**
 * __useAddCharFiltersToAnalyzerMutation__
 *
 * To run a mutation, you first call `useAddCharFiltersToAnalyzerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddCharFiltersToAnalyzerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addCharFiltersToAnalyzerMutation, { data, loading, error }] = useAddCharFiltersToAnalyzerMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddCharFiltersToAnalyzerMutation(baseOptions?: Apollo.MutationHookOptions<AddCharFiltersToAnalyzerMutation, AddCharFiltersToAnalyzerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddCharFiltersToAnalyzerMutation, AddCharFiltersToAnalyzerMutationVariables>(AddCharFiltersToAnalyzerDocument, options);
      }
export type AddCharFiltersToAnalyzerMutationHookResult = ReturnType<typeof useAddCharFiltersToAnalyzerMutation>;
export type AddCharFiltersToAnalyzerMutationResult = Apollo.MutationResult<AddCharFiltersToAnalyzerMutation>;
export type AddCharFiltersToAnalyzerMutationOptions = Apollo.BaseMutationOptions<AddCharFiltersToAnalyzerMutation, AddCharFiltersToAnalyzerMutationVariables>;
export const CharFilterDocument = gql`
    query CharFilter($id: ID!) {
  charFilter(id: $id) {
    id
    name
    description
    jsonConfig
    type
  }
}
    `;

/**
 * __useCharFilterQuery__
 *
 * To run a query within a React component, call `useCharFilterQuery` and pass it any options that fit your needs.
 * When your component renders, `useCharFilterQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useCharFilterQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useCharFilterQuery(baseOptions: Apollo.QueryHookOptions<CharFilterQuery, CharFilterQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<CharFilterQuery, CharFilterQueryVariables>(CharFilterDocument, options);
      }
export function useCharFilterLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<CharFilterQuery, CharFilterQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<CharFilterQuery, CharFilterQueryVariables>(CharFilterDocument, options);
        }
export type CharFilterQueryHookResult = ReturnType<typeof useCharFilterQuery>;
export type CharFilterLazyQueryHookResult = ReturnType<typeof useCharFilterLazyQuery>;
export type CharFilterQueryResult = Apollo.QueryResult<CharFilterQuery, CharFilterQueryVariables>;
export const CreateOrUpdateCharFilterDocument = gql`
    mutation CreateOrUpdateCharFilter($id: ID, $name: String!, $description: String, $jsonConfig: String, $type: String!) {
  charFilter(
    id: $id
    charFilterDTO: {name: $name, description: $description, jsonConfig: $jsonConfig, type: $type}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateCharFilterMutationFn = Apollo.MutationFunction<CreateOrUpdateCharFilterMutation, CreateOrUpdateCharFilterMutationVariables>;

/**
 * __useCreateOrUpdateCharFilterMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateCharFilterMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateCharFilterMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateCharFilterMutation, { data, loading, error }] = useCreateOrUpdateCharFilterMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      jsonConfig: // value for 'jsonConfig'
 *      type: // value for 'type'
 *   },
 * });
 */
export function useCreateOrUpdateCharFilterMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateCharFilterMutation, CreateOrUpdateCharFilterMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateCharFilterMutation, CreateOrUpdateCharFilterMutationVariables>(CreateOrUpdateCharFilterDocument, options);
      }
export type CreateOrUpdateCharFilterMutationHookResult = ReturnType<typeof useCreateOrUpdateCharFilterMutation>;
export type CreateOrUpdateCharFilterMutationResult = Apollo.MutationResult<CreateOrUpdateCharFilterMutation>;
export type CreateOrUpdateCharFilterMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateCharFilterMutation, CreateOrUpdateCharFilterMutationVariables>;
export const DataIndexInformationDocument = gql`
    query dataIndexInformation {
  buckets {
    edges {
      node {
        datasources {
          edges {
            node {
              dataIndex {
                cat {
                  docsCount
                  docsDeleted
                  health
                  index
                  pri
                  priStoreSize
                  rep
                  status
                  storeSize
                  uuid
                }
              }
            }
          }
        }
      }
    }
  }
}
    `;

/**
 * __useDataIndexInformationQuery__
 *
 * To run a query within a React component, call `useDataIndexInformationQuery` and pass it any options that fit your needs.
 * When your component renders, `useDataIndexInformationQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDataIndexInformationQuery({
 *   variables: {
 *   },
 * });
 */
export function useDataIndexInformationQuery(baseOptions?: Apollo.QueryHookOptions<DataIndexInformationQuery, DataIndexInformationQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataIndexInformationQuery, DataIndexInformationQueryVariables>(DataIndexInformationDocument, options);
      }
export function useDataIndexInformationLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataIndexInformationQuery, DataIndexInformationQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataIndexInformationQuery, DataIndexInformationQueryVariables>(DataIndexInformationDocument, options);
        }
export type DataIndexInformationQueryHookResult = ReturnType<typeof useDataIndexInformationQuery>;
export type DataIndexInformationLazyQueryHookResult = ReturnType<typeof useDataIndexInformationLazyQuery>;
export type DataIndexInformationQueryResult = Apollo.QueryResult<DataIndexInformationQuery, DataIndexInformationQueryVariables>;
export const SchedulersFaiulureDocument = gql`
    query SchedulersFaiulure($searchText: String) {
  schedulers(searchText: $searchText) {
    edges {
      node {
        id
        modifiedDate
        errorDescription
        lastIngestionDate
        status
        datasource {
          id
          name
        }
        newDataIndex {
          id
          name
        }
      }
    }
  }
}
    `;

/**
 * __useSchedulersFaiulureQuery__
 *
 * To run a query within a React component, call `useSchedulersFaiulureQuery` and pass it any options that fit your needs.
 * When your component renders, `useSchedulersFaiulureQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSchedulersFaiulureQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *   },
 * });
 */
export function useSchedulersFaiulureQuery(baseOptions?: Apollo.QueryHookOptions<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>(SchedulersFaiulureDocument, options);
      }
export function useSchedulersFaiulureLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>(SchedulersFaiulureDocument, options);
        }
export type SchedulersFaiulureQueryHookResult = ReturnType<typeof useSchedulersFaiulureQuery>;
export type SchedulersFaiulureLazyQueryHookResult = ReturnType<typeof useSchedulersFaiulureLazyQuery>;
export type SchedulersFaiulureQueryResult = Apollo.QueryResult<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>;
export const DocumentTypeTemplatesDocument = gql`
    query DocumentTypeTemplates($searchText: String, $after: String) {
  docTypeTemplates(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useDocumentTypeTemplatesQuery__
 *
 * To run a query within a React component, call `useDocumentTypeTemplatesQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocumentTypeTemplatesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocumentTypeTemplatesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useDocumentTypeTemplatesQuery(baseOptions?: Apollo.QueryHookOptions<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>(DocumentTypeTemplatesDocument, options);
      }
export function useDocumentTypeTemplatesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>(DocumentTypeTemplatesDocument, options);
        }
export type DocumentTypeTemplatesQueryHookResult = ReturnType<typeof useDocumentTypeTemplatesQuery>;
export type DocumentTypeTemplatesLazyQueryHookResult = ReturnType<typeof useDocumentTypeTemplatesLazyQuery>;
export type DocumentTypeTemplatesQueryResult = Apollo.QueryResult<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>;
export const DeleteDocumentTypeTemplateDocument = gql`
    mutation DeleteDocumentTypeTemplate($id: ID!) {
  deleteDocTypeTemplate(docTypeTemplateId: $id) {
    id
    name
  }
}
    `;
export type DeleteDocumentTypeTemplateMutationFn = Apollo.MutationFunction<DeleteDocumentTypeTemplateMutation, DeleteDocumentTypeTemplateMutationVariables>;

/**
 * __useDeleteDocumentTypeTemplateMutation__
 *
 * To run a mutation, you first call `useDeleteDocumentTypeTemplateMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteDocumentTypeTemplateMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteDocumentTypeTemplateMutation, { data, loading, error }] = useDeleteDocumentTypeTemplateMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteDocumentTypeTemplateMutation(baseOptions?: Apollo.MutationHookOptions<DeleteDocumentTypeTemplateMutation, DeleteDocumentTypeTemplateMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteDocumentTypeTemplateMutation, DeleteDocumentTypeTemplateMutationVariables>(DeleteDocumentTypeTemplateDocument, options);
      }
export type DeleteDocumentTypeTemplateMutationHookResult = ReturnType<typeof useDeleteDocumentTypeTemplateMutation>;
export type DeleteDocumentTypeTemplateMutationResult = Apollo.MutationResult<DeleteDocumentTypeTemplateMutation>;
export type DeleteDocumentTypeTemplateMutationOptions = Apollo.BaseMutationOptions<DeleteDocumentTypeTemplateMutation, DeleteDocumentTypeTemplateMutationVariables>;
export const DocumentTypesDocument = gql`
    query DocumentTypes($searchText: String, $cursor: String) {
  docTypes(searchText: $searchText, first: 20, after: $cursor) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useDocumentTypesQuery__
 *
 * To run a query within a React component, call `useDocumentTypesQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocumentTypesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocumentTypesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useDocumentTypesQuery(baseOptions?: Apollo.QueryHookOptions<DocumentTypesQuery, DocumentTypesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypesQuery, DocumentTypesQueryVariables>(DocumentTypesDocument, options);
      }
export function useDocumentTypesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypesQuery, DocumentTypesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypesQuery, DocumentTypesQueryVariables>(DocumentTypesDocument, options);
        }
export type DocumentTypesQueryHookResult = ReturnType<typeof useDocumentTypesQuery>;
export type DocumentTypesLazyQueryHookResult = ReturnType<typeof useDocumentTypesLazyQuery>;
export type DocumentTypesQueryResult = Apollo.QueryResult<DocumentTypesQuery, DocumentTypesQueryVariables>;
export const DocumentTypeTemplateDocument = gql`
    query DocumentTypeTemplate($id: ID!) {
  docTypeTemplate(id: $id) {
    id
    name
    description
    templateType
    source
    compiled
  }
}
    `;

/**
 * __useDocumentTypeTemplateQuery__
 *
 * To run a query within a React component, call `useDocumentTypeTemplateQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocumentTypeTemplateQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocumentTypeTemplateQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDocumentTypeTemplateQuery(baseOptions: Apollo.QueryHookOptions<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>(DocumentTypeTemplateDocument, options);
      }
export function useDocumentTypeTemplateLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>(DocumentTypeTemplateDocument, options);
        }
export type DocumentTypeTemplateQueryHookResult = ReturnType<typeof useDocumentTypeTemplateQuery>;
export type DocumentTypeTemplateLazyQueryHookResult = ReturnType<typeof useDocumentTypeTemplateLazyQuery>;
export type DocumentTypeTemplateQueryResult = Apollo.QueryResult<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>;
export const CreateOrUpdateDocumentTypeTemplateDocument = gql`
    mutation CreateOrUpdateDocumentTypeTemplate($id: ID, $name: String!, $description: String, $templateType: TemplateType!, $source: String!, $compiled: String!) {
  docTypeTemplate(
    id: $id
    docTypeTemplateDTO: {name: $name, description: $description, templateType: $templateType, source: $source, compiled: $compiled}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateDocumentTypeTemplateMutationFn = Apollo.MutationFunction<CreateOrUpdateDocumentTypeTemplateMutation, CreateOrUpdateDocumentTypeTemplateMutationVariables>;

/**
 * __useCreateOrUpdateDocumentTypeTemplateMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateDocumentTypeTemplateMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateDocumentTypeTemplateMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateDocumentTypeTemplateMutation, { data, loading, error }] = useCreateOrUpdateDocumentTypeTemplateMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      templateType: // value for 'templateType'
 *      source: // value for 'source'
 *      compiled: // value for 'compiled'
 *   },
 * });
 */
export function useCreateOrUpdateDocumentTypeTemplateMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateDocumentTypeTemplateMutation, CreateOrUpdateDocumentTypeTemplateMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateDocumentTypeTemplateMutation, CreateOrUpdateDocumentTypeTemplateMutationVariables>(CreateOrUpdateDocumentTypeTemplateDocument, options);
      }
export type CreateOrUpdateDocumentTypeTemplateMutationHookResult = ReturnType<typeof useCreateOrUpdateDocumentTypeTemplateMutation>;
export type CreateOrUpdateDocumentTypeTemplateMutationResult = Apollo.MutationResult<CreateOrUpdateDocumentTypeTemplateMutation>;
export type CreateOrUpdateDocumentTypeTemplateMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateDocumentTypeTemplateMutation, CreateOrUpdateDocumentTypeTemplateMutationVariables>;
export const DocumentTypeFieldDocument = gql`
    query DocumentTypeField($id: ID!) {
  docTypeField(id: $id) {
    id
    name
    description
    fieldType
    boost
    searchable
    exclude
    fieldName
    jsonConfig
    sortable
    analyzer {
      id
      name
    }
    translations {
      key
      language
      value
      description
    }
  }
}
    `;

/**
 * __useDocumentTypeFieldQuery__
 *
 * To run a query within a React component, call `useDocumentTypeFieldQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocumentTypeFieldQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocumentTypeFieldQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDocumentTypeFieldQuery(baseOptions: Apollo.QueryHookOptions<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>(DocumentTypeFieldDocument, options);
      }
export function useDocumentTypeFieldLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>(DocumentTypeFieldDocument, options);
        }
export type DocumentTypeFieldQueryHookResult = ReturnType<typeof useDocumentTypeFieldQuery>;
export type DocumentTypeFieldLazyQueryHookResult = ReturnType<typeof useDocumentTypeFieldLazyQuery>;
export type DocumentTypeFieldQueryResult = Apollo.QueryResult<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>;
export const CreateOrUpdateDocumentTypeSubFieldsDocument = gql`
    mutation createOrUpdateDocumentTypeSubFields($parentDocTypeFieldId: ID!, $name: String!, $fieldName: String!, $jsonConfig: String, $searchable: Boolean!, $boost: Float, $fieldType: FieldType!, $sortable: Boolean!) {
  createSubField(
    parentDocTypeFieldId: $parentDocTypeFieldId
    docTypeFieldDTO: {name: $name, fieldName: $fieldName, jsonConfig: $jsonConfig, searchable: $searchable, boost: $boost, fieldType: $fieldType, sortable: $sortable}
  ) {
    entity {
      id
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateDocumentTypeSubFieldsMutationFn = Apollo.MutationFunction<CreateOrUpdateDocumentTypeSubFieldsMutation, CreateOrUpdateDocumentTypeSubFieldsMutationVariables>;

/**
 * __useCreateOrUpdateDocumentTypeSubFieldsMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateDocumentTypeSubFieldsMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateDocumentTypeSubFieldsMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateDocumentTypeSubFieldsMutation, { data, loading, error }] = useCreateOrUpdateDocumentTypeSubFieldsMutation({
 *   variables: {
 *      parentDocTypeFieldId: // value for 'parentDocTypeFieldId'
 *      name: // value for 'name'
 *      fieldName: // value for 'fieldName'
 *      jsonConfig: // value for 'jsonConfig'
 *      searchable: // value for 'searchable'
 *      boost: // value for 'boost'
 *      fieldType: // value for 'fieldType'
 *      sortable: // value for 'sortable'
 *   },
 * });
 */
export function useCreateOrUpdateDocumentTypeSubFieldsMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateDocumentTypeSubFieldsMutation, CreateOrUpdateDocumentTypeSubFieldsMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateDocumentTypeSubFieldsMutation, CreateOrUpdateDocumentTypeSubFieldsMutationVariables>(CreateOrUpdateDocumentTypeSubFieldsDocument, options);
      }
export type CreateOrUpdateDocumentTypeSubFieldsMutationHookResult = ReturnType<typeof useCreateOrUpdateDocumentTypeSubFieldsMutation>;
export type CreateOrUpdateDocumentTypeSubFieldsMutationResult = Apollo.MutationResult<CreateOrUpdateDocumentTypeSubFieldsMutation>;
export type CreateOrUpdateDocumentTypeSubFieldsMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateDocumentTypeSubFieldsMutation, CreateOrUpdateDocumentTypeSubFieldsMutationVariables>;
export const CreateOrUpdateDocumentTypeDocument = gql`
    mutation CreateOrUpdateDocumentType($id: ID, $name: String!, $description: String) {
  docType(id: $id, docTypeDTO: {name: $name, description: $description}) {
    entity {
      id
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateDocumentTypeMutationFn = Apollo.MutationFunction<CreateOrUpdateDocumentTypeMutation, CreateOrUpdateDocumentTypeMutationVariables>;

/**
 * __useCreateOrUpdateDocumentTypeMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateDocumentTypeMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateDocumentTypeMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateDocumentTypeMutation, { data, loading, error }] = useCreateOrUpdateDocumentTypeMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *   },
 * });
 */
export function useCreateOrUpdateDocumentTypeMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateDocumentTypeMutation, CreateOrUpdateDocumentTypeMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateDocumentTypeMutation, CreateOrUpdateDocumentTypeMutationVariables>(CreateOrUpdateDocumentTypeDocument, options);
      }
export type CreateOrUpdateDocumentTypeMutationHookResult = ReturnType<typeof useCreateOrUpdateDocumentTypeMutation>;
export type CreateOrUpdateDocumentTypeMutationResult = Apollo.MutationResult<CreateOrUpdateDocumentTypeMutation>;
export type CreateOrUpdateDocumentTypeMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateDocumentTypeMutation, CreateOrUpdateDocumentTypeMutationVariables>;
export const DocumentTypeDocument = gql`
    query DocumentType($id: ID!) {
  docType(id: $id) {
    id
    name
    description
    docTypeTemplate {
      id
      name
    }
  }
}
    `;

/**
 * __useDocumentTypeQuery__
 *
 * To run a query within a React component, call `useDocumentTypeQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocumentTypeQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocumentTypeQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDocumentTypeQuery(baseOptions: Apollo.QueryHookOptions<DocumentTypeQuery, DocumentTypeQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeQuery, DocumentTypeQueryVariables>(DocumentTypeDocument, options);
      }
export function useDocumentTypeLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeQuery, DocumentTypeQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeQuery, DocumentTypeQueryVariables>(DocumentTypeDocument, options);
        }
export type DocumentTypeQueryHookResult = ReturnType<typeof useDocumentTypeQuery>;
export type DocumentTypeLazyQueryHookResult = ReturnType<typeof useDocumentTypeLazyQuery>;
export type DocumentTypeQueryResult = Apollo.QueryResult<DocumentTypeQuery, DocumentTypeQueryVariables>;
export const DocTypeFieldsByParentDocument = gql`
    query DocTypeFieldsByParent($searchText: String, $parentId: BigInteger!, $docTypeId: ID!) {
  docTypeFieldsFromDocTypeByParent(
    parentId: $parentId
    searchText: $searchText
    first: 30
    docTypeId: $docTypeId
  ) {
    edges {
      node {
        id
        name
        description
        fieldType
        boost
        searchable
        exclude
        fieldName
        jsonConfig
        sortable
        parent {
          id
          fieldName
        }
      }
    }
  }
}
    `;

/**
 * __useDocTypeFieldsByParentQuery__
 *
 * To run a query within a React component, call `useDocTypeFieldsByParentQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocTypeFieldsByParentQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocTypeFieldsByParentQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      parentId: // value for 'parentId'
 *      docTypeId: // value for 'docTypeId'
 *   },
 * });
 */
export function useDocTypeFieldsByParentQuery(baseOptions: Apollo.QueryHookOptions<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>(DocTypeFieldsByParentDocument, options);
      }
export function useDocTypeFieldsByParentLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>(DocTypeFieldsByParentDocument, options);
        }
export type DocTypeFieldsByParentQueryHookResult = ReturnType<typeof useDocTypeFieldsByParentQuery>;
export type DocTypeFieldsByParentLazyQueryHookResult = ReturnType<typeof useDocTypeFieldsByParentLazyQuery>;
export type DocTypeFieldsByParentQueryResult = Apollo.QueryResult<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>;
export const CreateOrUpdateDocumentTypeFieldDocument = gql`
    mutation CreateOrUpdateDocumentTypeField($documentTypeId: ID!, $documentTypeFieldId: ID, $name: String!, $fieldName: String!, $description: String, $fieldType: FieldType!, $boost: Float, $searchable: Boolean!, $exclude: Boolean, $jsonConfig: String, $sortable: Boolean!, $analyzerId: BigInteger) {
  docTypeFieldWithAnalyzer(
    docTypeId: $documentTypeId
    docTypeFieldId: $documentTypeFieldId
    docTypeFieldWithAnalyzerDTO: {name: $name, description: $description, fieldType: $fieldType, boost: $boost, searchable: $searchable, exclude: $exclude, fieldName: $fieldName, jsonConfig: $jsonConfig, sortable: $sortable, analyzerId: $analyzerId}
  ) {
    entity {
      id
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateDocumentTypeFieldMutationFn = Apollo.MutationFunction<CreateOrUpdateDocumentTypeFieldMutation, CreateOrUpdateDocumentTypeFieldMutationVariables>;

/**
 * __useCreateOrUpdateDocumentTypeFieldMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateDocumentTypeFieldMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateDocumentTypeFieldMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateDocumentTypeFieldMutation, { data, loading, error }] = useCreateOrUpdateDocumentTypeFieldMutation({
 *   variables: {
 *      documentTypeId: // value for 'documentTypeId'
 *      documentTypeFieldId: // value for 'documentTypeFieldId'
 *      name: // value for 'name'
 *      fieldName: // value for 'fieldName'
 *      description: // value for 'description'
 *      fieldType: // value for 'fieldType'
 *      boost: // value for 'boost'
 *      searchable: // value for 'searchable'
 *      exclude: // value for 'exclude'
 *      jsonConfig: // value for 'jsonConfig'
 *      sortable: // value for 'sortable'
 *      analyzerId: // value for 'analyzerId'
 *   },
 * });
 */
export function useCreateOrUpdateDocumentTypeFieldMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateDocumentTypeFieldMutation, CreateOrUpdateDocumentTypeFieldMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateDocumentTypeFieldMutation, CreateOrUpdateDocumentTypeFieldMutationVariables>(CreateOrUpdateDocumentTypeFieldDocument, options);
      }
export type CreateOrUpdateDocumentTypeFieldMutationHookResult = ReturnType<typeof useCreateOrUpdateDocumentTypeFieldMutation>;
export type CreateOrUpdateDocumentTypeFieldMutationResult = Apollo.MutationResult<CreateOrUpdateDocumentTypeFieldMutation>;
export type CreateOrUpdateDocumentTypeFieldMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateDocumentTypeFieldMutation, CreateOrUpdateDocumentTypeFieldMutationVariables>;
export const DeleteDocumentTypeDocument = gql`
    mutation DeleteDocumentType($id: ID!, $docTypeName: String) {
  deleteDocType(docTypeId: $id, docTypeName: $docTypeName) {
    id
  }
}
    `;
export type DeleteDocumentTypeMutationFn = Apollo.MutationFunction<DeleteDocumentTypeMutation, DeleteDocumentTypeMutationVariables>;

/**
 * __useDeleteDocumentTypeMutation__
 *
 * To run a mutation, you first call `useDeleteDocumentTypeMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteDocumentTypeMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteDocumentTypeMutation, { data, loading, error }] = useDeleteDocumentTypeMutation({
 *   variables: {
 *      id: // value for 'id'
 *      docTypeName: // value for 'docTypeName'
 *   },
 * });
 */
export function useDeleteDocumentTypeMutation(baseOptions?: Apollo.MutationHookOptions<DeleteDocumentTypeMutation, DeleteDocumentTypeMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteDocumentTypeMutation, DeleteDocumentTypeMutationVariables>(DeleteDocumentTypeDocument, options);
      }
export type DeleteDocumentTypeMutationHookResult = ReturnType<typeof useDeleteDocumentTypeMutation>;
export type DeleteDocumentTypeMutationResult = Apollo.MutationResult<DeleteDocumentTypeMutation>;
export type DeleteDocumentTypeMutationOptions = Apollo.BaseMutationOptions<DeleteDocumentTypeMutation, DeleteDocumentTypeMutationVariables>;
export const DocTypeTemplateListDocument = gql`
    query docTypeTemplateList {
  docTypeTemplates {
    edges {
      node {
        name
        id
      }
    }
  }
}
    `;

/**
 * __useDocTypeTemplateListQuery__
 *
 * To run a query within a React component, call `useDocTypeTemplateListQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocTypeTemplateListQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocTypeTemplateListQuery({
 *   variables: {
 *   },
 * });
 */
export function useDocTypeTemplateListQuery(baseOptions?: Apollo.QueryHookOptions<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>(DocTypeTemplateListDocument, options);
      }
export function useDocTypeTemplateListLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>(DocTypeTemplateListDocument, options);
        }
export type DocTypeTemplateListQueryHookResult = ReturnType<typeof useDocTypeTemplateListQuery>;
export type DocTypeTemplateListLazyQueryHookResult = ReturnType<typeof useDocTypeTemplateListLazyQuery>;
export type DocTypeTemplateListQueryResult = Apollo.QueryResult<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>;
export const CreateOrUpdateDocTypeWithTemplateDocument = gql`
    mutation CreateOrUpdateDocTypeWithTemplate($name: String!, $description: String, $docTypeTemplateId: BigInteger, $id: ID) {
  docTypeWithTemplate(
    id: $id
    docTypeWithTemplateDTO: {name: $name, description: $description, docTypeTemplateId: $docTypeTemplateId}
  ) {
    entity {
      id
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateDocTypeWithTemplateMutationFn = Apollo.MutationFunction<CreateOrUpdateDocTypeWithTemplateMutation, CreateOrUpdateDocTypeWithTemplateMutationVariables>;

/**
 * __useCreateOrUpdateDocTypeWithTemplateMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateDocTypeWithTemplateMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateDocTypeWithTemplateMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateDocTypeWithTemplateMutation, { data, loading, error }] = useCreateOrUpdateDocTypeWithTemplateMutation({
 *   variables: {
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      docTypeTemplateId: // value for 'docTypeTemplateId'
 *      id: // value for 'id'
 *   },
 * });
 */
export function useCreateOrUpdateDocTypeWithTemplateMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateDocTypeWithTemplateMutation, CreateOrUpdateDocTypeWithTemplateMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateDocTypeWithTemplateMutation, CreateOrUpdateDocTypeWithTemplateMutationVariables>(CreateOrUpdateDocTypeWithTemplateDocument, options);
      }
export type CreateOrUpdateDocTypeWithTemplateMutationHookResult = ReturnType<typeof useCreateOrUpdateDocTypeWithTemplateMutation>;
export type CreateOrUpdateDocTypeWithTemplateMutationResult = Apollo.MutationResult<CreateOrUpdateDocTypeWithTemplateMutation>;
export type CreateOrUpdateDocTypeWithTemplateMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateDocTypeWithTemplateMutation, CreateOrUpdateDocTypeWithTemplateMutationVariables>;
export const UnboundAnalyzersDocument = gql`
    query UnboundAnalyzers {
  analyzers {
    edges {
      node {
        id
        name
      }
    }
  }
}
    `;

/**
 * __useUnboundAnalyzersQuery__
 *
 * To run a query within a React component, call `useUnboundAnalyzersQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundAnalyzersQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundAnalyzersQuery({
 *   variables: {
 *   },
 * });
 */
export function useUnboundAnalyzersQuery(baseOptions?: Apollo.QueryHookOptions<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>(UnboundAnalyzersDocument, options);
      }
export function useUnboundAnalyzersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>(UnboundAnalyzersDocument, options);
        }
export type UnboundAnalyzersQueryHookResult = ReturnType<typeof useUnboundAnalyzersQuery>;
export type UnboundAnalyzersLazyQueryHookResult = ReturnType<typeof useUnboundAnalyzersLazyQuery>;
export type UnboundAnalyzersQueryResult = Apollo.QueryResult<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>;
export const EmbeddingModelsDocument = gql`
    query EmbeddingModels($searchText: String, $after: String) {
  embeddingModels(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        enabled
      }
    }
  }
}
    `;

/**
 * __useEmbeddingModelsQuery__
 *
 * To run a query within a React component, call `useEmbeddingModelsQuery` and pass it any options that fit your needs.
 * When your component renders, `useEmbeddingModelsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEmbeddingModelsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useEmbeddingModelsQuery(baseOptions?: Apollo.QueryHookOptions<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>(EmbeddingModelsDocument, options);
      }
export function useEmbeddingModelsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>(EmbeddingModelsDocument, options);
        }
export type EmbeddingModelsQueryHookResult = ReturnType<typeof useEmbeddingModelsQuery>;
export type EmbeddingModelsLazyQueryHookResult = ReturnType<typeof useEmbeddingModelsLazyQuery>;
export type EmbeddingModelsQueryResult = Apollo.QueryResult<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>;
export const EnableEmbeddingModelDocument = gql`
    mutation EnableEmbeddingModel($id: ID!) {
  enableEmbeddingModel(id: $id) {
    id
    name
  }
}
    `;
export type EnableEmbeddingModelMutationFn = Apollo.MutationFunction<EnableEmbeddingModelMutation, EnableEmbeddingModelMutationVariables>;

/**
 * __useEnableEmbeddingModelMutation__
 *
 * To run a mutation, you first call `useEnableEmbeddingModelMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useEnableEmbeddingModelMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [enableEmbeddingModelMutation, { data, loading, error }] = useEnableEmbeddingModelMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useEnableEmbeddingModelMutation(baseOptions?: Apollo.MutationHookOptions<EnableEmbeddingModelMutation, EnableEmbeddingModelMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<EnableEmbeddingModelMutation, EnableEmbeddingModelMutationVariables>(EnableEmbeddingModelDocument, options);
      }
export type EnableEmbeddingModelMutationHookResult = ReturnType<typeof useEnableEmbeddingModelMutation>;
export type EnableEmbeddingModelMutationResult = Apollo.MutationResult<EnableEmbeddingModelMutation>;
export type EnableEmbeddingModelMutationOptions = Apollo.BaseMutationOptions<EnableEmbeddingModelMutation, EnableEmbeddingModelMutationVariables>;
export const DeleteEmbeddingModelDocument = gql`
    mutation DeleteEmbeddingModel($id: ID!) {
  deleteEmbeddingModel(embeddingModelId: $id) {
    id
    name
  }
}
    `;
export type DeleteEmbeddingModelMutationFn = Apollo.MutationFunction<DeleteEmbeddingModelMutation, DeleteEmbeddingModelMutationVariables>;

/**
 * __useDeleteEmbeddingModelMutation__
 *
 * To run a mutation, you first call `useDeleteEmbeddingModelMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteEmbeddingModelMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteEmbeddingModelMutation, { data, loading, error }] = useDeleteEmbeddingModelMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteEmbeddingModelMutation(baseOptions?: Apollo.MutationHookOptions<DeleteEmbeddingModelMutation, DeleteEmbeddingModelMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteEmbeddingModelMutation, DeleteEmbeddingModelMutationVariables>(DeleteEmbeddingModelDocument, options);
      }
export type DeleteEmbeddingModelMutationHookResult = ReturnType<typeof useDeleteEmbeddingModelMutation>;
export type DeleteEmbeddingModelMutationResult = Apollo.MutationResult<DeleteEmbeddingModelMutation>;
export type DeleteEmbeddingModelMutationOptions = Apollo.BaseMutationOptions<DeleteEmbeddingModelMutation, DeleteEmbeddingModelMutationVariables>;
export const EmbeddingModelDocument = gql`
    query EmbeddingModel($id: ID!) {
  embeddingModel(id: $id) {
    name
    description
    apiUrl
    apiKey
    vectorSize
    jsonConfig
    providerModel {
      provider
      model
    }
  }
}
    `;

/**
 * __useEmbeddingModelQuery__
 *
 * To run a query within a React component, call `useEmbeddingModelQuery` and pass it any options that fit your needs.
 * When your component renders, `useEmbeddingModelQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEmbeddingModelQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useEmbeddingModelQuery(baseOptions: Apollo.QueryHookOptions<EmbeddingModelQuery, EmbeddingModelQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EmbeddingModelQuery, EmbeddingModelQueryVariables>(EmbeddingModelDocument, options);
      }
export function useEmbeddingModelLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EmbeddingModelQuery, EmbeddingModelQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EmbeddingModelQuery, EmbeddingModelQueryVariables>(EmbeddingModelDocument, options);
        }
export type EmbeddingModelQueryHookResult = ReturnType<typeof useEmbeddingModelQuery>;
export type EmbeddingModelLazyQueryHookResult = ReturnType<typeof useEmbeddingModelLazyQuery>;
export type EmbeddingModelQueryResult = Apollo.QueryResult<EmbeddingModelQuery, EmbeddingModelQueryVariables>;
export const CreateOrUpdateEmbeddingModelDocument = gql`
    mutation CreateOrUpdateEmbeddingModel($id: ID, $apiKey: String, $apiUrl: String!, $description: String!, $name: String!, $vectorSize: Int!, $providerModel: ProviderModelDTOInput!, $jsonConfig: String) {
  embeddingModel(
    id: $id
    embeddingModelDTO: {name: $name, apiKey: $apiKey, apiUrl: $apiUrl, description: $description, vectorSize: $vectorSize, providerModel: $providerModel, jsonConfig: $jsonConfig}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateEmbeddingModelMutationFn = Apollo.MutationFunction<CreateOrUpdateEmbeddingModelMutation, CreateOrUpdateEmbeddingModelMutationVariables>;

/**
 * __useCreateOrUpdateEmbeddingModelMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateEmbeddingModelMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateEmbeddingModelMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateEmbeddingModelMutation, { data, loading, error }] = useCreateOrUpdateEmbeddingModelMutation({
 *   variables: {
 *      id: // value for 'id'
 *      apiKey: // value for 'apiKey'
 *      apiUrl: // value for 'apiUrl'
 *      description: // value for 'description'
 *      name: // value for 'name'
 *      vectorSize: // value for 'vectorSize'
 *      providerModel: // value for 'providerModel'
 *      jsonConfig: // value for 'jsonConfig'
 *   },
 * });
 */
export function useCreateOrUpdateEmbeddingModelMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateEmbeddingModelMutation, CreateOrUpdateEmbeddingModelMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateEmbeddingModelMutation, CreateOrUpdateEmbeddingModelMutationVariables>(CreateOrUpdateEmbeddingModelDocument, options);
      }
export type CreateOrUpdateEmbeddingModelMutationHookResult = ReturnType<typeof useCreateOrUpdateEmbeddingModelMutation>;
export type CreateOrUpdateEmbeddingModelMutationResult = Apollo.MutationResult<CreateOrUpdateEmbeddingModelMutation>;
export type CreateOrUpdateEmbeddingModelMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateEmbeddingModelMutation, CreateOrUpdateEmbeddingModelMutationVariables>;
export const UnboundEnrichPipelinesDocument = gql`
    query UnboundEnrichPipelines($itemId: BigInteger!) {
  unboundEnrichPipelines(itemId: $itemId) {
    name
    id
  }
}
    `;

/**
 * __useUnboundEnrichPipelinesQuery__
 *
 * To run a query within a React component, call `useUnboundEnrichPipelinesQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundEnrichPipelinesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundEnrichPipelinesQuery({
 *   variables: {
 *      itemId: // value for 'itemId'
 *   },
 * });
 */
export function useUnboundEnrichPipelinesQuery(baseOptions: Apollo.QueryHookOptions<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>(UnboundEnrichPipelinesDocument, options);
      }
export function useUnboundEnrichPipelinesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>(UnboundEnrichPipelinesDocument, options);
        }
export type UnboundEnrichPipelinesQueryHookResult = ReturnType<typeof useUnboundEnrichPipelinesQuery>;
export type UnboundEnrichPipelinesLazyQueryHookResult = ReturnType<typeof useUnboundEnrichPipelinesLazyQuery>;
export type UnboundEnrichPipelinesQueryResult = Apollo.QueryResult<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>;
export const DeleteEnrichItemDocument = gql`
    mutation DeleteEnrichItem($id: ID!) {
  deleteEnrichItem(enrichItemId: $id) {
    id
    name
  }
}
    `;
export type DeleteEnrichItemMutationFn = Apollo.MutationFunction<DeleteEnrichItemMutation, DeleteEnrichItemMutationVariables>;

/**
 * __useDeleteEnrichItemMutation__
 *
 * To run a mutation, you first call `useDeleteEnrichItemMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteEnrichItemMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteEnrichItemMutation, { data, loading, error }] = useDeleteEnrichItemMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteEnrichItemMutation(baseOptions?: Apollo.MutationHookOptions<DeleteEnrichItemMutation, DeleteEnrichItemMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteEnrichItemMutation, DeleteEnrichItemMutationVariables>(DeleteEnrichItemDocument, options);
      }
export type DeleteEnrichItemMutationHookResult = ReturnType<typeof useDeleteEnrichItemMutation>;
export type DeleteEnrichItemMutationResult = Apollo.MutationResult<DeleteEnrichItemMutation>;
export type DeleteEnrichItemMutationOptions = Apollo.BaseMutationOptions<DeleteEnrichItemMutation, DeleteEnrichItemMutationVariables>;
export const EnrichItemDocument = gql`
    query EnrichItem($id: ID!) {
  enrichItem(id: $id) {
    id
    name
    description
    type
    serviceName
    jsonConfig
    script
    behaviorMergeType
    jsonPath
    behaviorOnError
    requestTimeout
  }
}
    `;

/**
 * __useEnrichItemQuery__
 *
 * To run a query within a React component, call `useEnrichItemQuery` and pass it any options that fit your needs.
 * When your component renders, `useEnrichItemQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEnrichItemQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useEnrichItemQuery(baseOptions: Apollo.QueryHookOptions<EnrichItemQuery, EnrichItemQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichItemQuery, EnrichItemQueryVariables>(EnrichItemDocument, options);
      }
export function useEnrichItemLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichItemQuery, EnrichItemQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichItemQuery, EnrichItemQueryVariables>(EnrichItemDocument, options);
        }
export type EnrichItemQueryHookResult = ReturnType<typeof useEnrichItemQuery>;
export type EnrichItemLazyQueryHookResult = ReturnType<typeof useEnrichItemLazyQuery>;
export type EnrichItemQueryResult = Apollo.QueryResult<EnrichItemQuery, EnrichItemQueryVariables>;
export const CreateOrUpdateEnrichItemDocument = gql`
    mutation CreateOrUpdateEnrichItem($id: ID, $name: String!, $description: String, $type: EnrichItemType!, $serviceName: String!, $jsonConfig: String, $script: String, $behaviorMergeType: BehaviorMergeType!, $jsonPath: String!, $behaviorOnError: BehaviorOnError!, $requestTimeout: BigInteger!) {
  enrichItem(
    id: $id
    enrichItemDTO: {name: $name, description: $description, type: $type, serviceName: $serviceName, jsonConfig: $jsonConfig, script: $script, behaviorMergeType: $behaviorMergeType, jsonPath: $jsonPath, behaviorOnError: $behaviorOnError, requestTimeout: $requestTimeout}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateEnrichItemMutationFn = Apollo.MutationFunction<CreateOrUpdateEnrichItemMutation, CreateOrUpdateEnrichItemMutationVariables>;

/**
 * __useCreateOrUpdateEnrichItemMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateEnrichItemMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateEnrichItemMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateEnrichItemMutation, { data, loading, error }] = useCreateOrUpdateEnrichItemMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      type: // value for 'type'
 *      serviceName: // value for 'serviceName'
 *      jsonConfig: // value for 'jsonConfig'
 *      script: // value for 'script'
 *      behaviorMergeType: // value for 'behaviorMergeType'
 *      jsonPath: // value for 'jsonPath'
 *      behaviorOnError: // value for 'behaviorOnError'
 *      requestTimeout: // value for 'requestTimeout'
 *   },
 * });
 */
export function useCreateOrUpdateEnrichItemMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateEnrichItemMutation, CreateOrUpdateEnrichItemMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateEnrichItemMutation, CreateOrUpdateEnrichItemMutationVariables>(CreateOrUpdateEnrichItemDocument, options);
      }
export type CreateOrUpdateEnrichItemMutationHookResult = ReturnType<typeof useCreateOrUpdateEnrichItemMutation>;
export type CreateOrUpdateEnrichItemMutationResult = Apollo.MutationResult<CreateOrUpdateEnrichItemMutation>;
export type CreateOrUpdateEnrichItemMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateEnrichItemMutation, CreateOrUpdateEnrichItemMutationVariables>;
export const LargeLanguageModelsDocument = gql`
    query LargeLanguageModels($searchText: String, $after: String) {
  largeLanguageModels(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        enabled
        providerModel {
          provider
          model
        }
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useLargeLanguageModelsQuery__
 *
 * To run a query within a React component, call `useLargeLanguageModelsQuery` and pass it any options that fit your needs.
 * When your component renders, `useLargeLanguageModelsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useLargeLanguageModelsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useLargeLanguageModelsQuery(baseOptions?: Apollo.QueryHookOptions<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>(LargeLanguageModelsDocument, options);
      }
export function useLargeLanguageModelsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>(LargeLanguageModelsDocument, options);
        }
export type LargeLanguageModelsQueryHookResult = ReturnType<typeof useLargeLanguageModelsQuery>;
export type LargeLanguageModelsLazyQueryHookResult = ReturnType<typeof useLargeLanguageModelsLazyQuery>;
export type LargeLanguageModelsQueryResult = Apollo.QueryResult<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>;
export const EnableLargeLanguageModelDocument = gql`
    mutation EnableLargeLanguageModel($id: ID!) {
  enableLargeLanguageModel(id: $id) {
    id
    name
  }
}
    `;
export type EnableLargeLanguageModelMutationFn = Apollo.MutationFunction<EnableLargeLanguageModelMutation, EnableLargeLanguageModelMutationVariables>;

/**
 * __useEnableLargeLanguageModelMutation__
 *
 * To run a mutation, you first call `useEnableLargeLanguageModelMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useEnableLargeLanguageModelMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [enableLargeLanguageModelMutation, { data, loading, error }] = useEnableLargeLanguageModelMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useEnableLargeLanguageModelMutation(baseOptions?: Apollo.MutationHookOptions<EnableLargeLanguageModelMutation, EnableLargeLanguageModelMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<EnableLargeLanguageModelMutation, EnableLargeLanguageModelMutationVariables>(EnableLargeLanguageModelDocument, options);
      }
export type EnableLargeLanguageModelMutationHookResult = ReturnType<typeof useEnableLargeLanguageModelMutation>;
export type EnableLargeLanguageModelMutationResult = Apollo.MutationResult<EnableLargeLanguageModelMutation>;
export type EnableLargeLanguageModelMutationOptions = Apollo.BaseMutationOptions<EnableLargeLanguageModelMutation, EnableLargeLanguageModelMutationVariables>;
export const DeleteLargeLanguageModelDocument = gql`
    mutation DeleteLargeLanguageModel($id: ID!) {
  deleteLargeLanguageModel(largeLanguageModelId: $id) {
    id
    name
  }
}
    `;
export type DeleteLargeLanguageModelMutationFn = Apollo.MutationFunction<DeleteLargeLanguageModelMutation, DeleteLargeLanguageModelMutationVariables>;

/**
 * __useDeleteLargeLanguageModelMutation__
 *
 * To run a mutation, you first call `useDeleteLargeLanguageModelMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteLargeLanguageModelMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteLargeLanguageModelMutation, { data, loading, error }] = useDeleteLargeLanguageModelMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteLargeLanguageModelMutation(baseOptions?: Apollo.MutationHookOptions<DeleteLargeLanguageModelMutation, DeleteLargeLanguageModelMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteLargeLanguageModelMutation, DeleteLargeLanguageModelMutationVariables>(DeleteLargeLanguageModelDocument, options);
      }
export type DeleteLargeLanguageModelMutationHookResult = ReturnType<typeof useDeleteLargeLanguageModelMutation>;
export type DeleteLargeLanguageModelMutationResult = Apollo.MutationResult<DeleteLargeLanguageModelMutation>;
export type DeleteLargeLanguageModelMutationOptions = Apollo.BaseMutationOptions<DeleteLargeLanguageModelMutation, DeleteLargeLanguageModelMutationVariables>;
export const CreateOrUpdateLargeLanguageModelDocument = gql`
    mutation CreateOrUpdateLargeLanguageModel($id: ID, $apiKey: String, $apiUrl: String!, $description: String!, $name: String!, $jsonConfig: String, $providerModel: ProviderModelDTOInput!, $contextWindow: Int, $retrieveCitations: Boolean) {
  largeLanguageModel(
    id: $id
    largeLanguageModelDTO: {name: $name, apiKey: $apiKey, apiUrl: $apiUrl, description: $description, jsonConfig: $jsonConfig, providerModel: $providerModel, contextWindow: $contextWindow, retrieveCitations: $retrieveCitations}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateLargeLanguageModelMutationFn = Apollo.MutationFunction<CreateOrUpdateLargeLanguageModelMutation, CreateOrUpdateLargeLanguageModelMutationVariables>;

/**
 * __useCreateOrUpdateLargeLanguageModelMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateLargeLanguageModelMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateLargeLanguageModelMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateLargeLanguageModelMutation, { data, loading, error }] = useCreateOrUpdateLargeLanguageModelMutation({
 *   variables: {
 *      id: // value for 'id'
 *      apiKey: // value for 'apiKey'
 *      apiUrl: // value for 'apiUrl'
 *      description: // value for 'description'
 *      name: // value for 'name'
 *      jsonConfig: // value for 'jsonConfig'
 *      providerModel: // value for 'providerModel'
 *      contextWindow: // value for 'contextWindow'
 *      retrieveCitations: // value for 'retrieveCitations'
 *   },
 * });
 */
export function useCreateOrUpdateLargeLanguageModelMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateLargeLanguageModelMutation, CreateOrUpdateLargeLanguageModelMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateLargeLanguageModelMutation, CreateOrUpdateLargeLanguageModelMutationVariables>(CreateOrUpdateLargeLanguageModelDocument, options);
      }
export type CreateOrUpdateLargeLanguageModelMutationHookResult = ReturnType<typeof useCreateOrUpdateLargeLanguageModelMutation>;
export type CreateOrUpdateLargeLanguageModelMutationResult = Apollo.MutationResult<CreateOrUpdateLargeLanguageModelMutation>;
export type CreateOrUpdateLargeLanguageModelMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateLargeLanguageModelMutation, CreateOrUpdateLargeLanguageModelMutationVariables>;
export const LargeLanguageModelDocument = gql`
    query LargeLanguageModel($id: ID!) {
  largeLanguageModel(id: $id) {
    name
    description
    apiUrl
    apiKey
    jsonConfig
    contextWindow
    retrieveCitations
    providerModel {
      provider
      model
    }
  }
}
    `;

/**
 * __useLargeLanguageModelQuery__
 *
 * To run a query within a React component, call `useLargeLanguageModelQuery` and pass it any options that fit your needs.
 * When your component renders, `useLargeLanguageModelQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useLargeLanguageModelQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useLargeLanguageModelQuery(baseOptions: Apollo.QueryHookOptions<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>(LargeLanguageModelDocument, options);
      }
export function useLargeLanguageModelLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>(LargeLanguageModelDocument, options);
        }
export type LargeLanguageModelQueryHookResult = ReturnType<typeof useLargeLanguageModelQuery>;
export type LargeLanguageModelLazyQueryHookResult = ReturnType<typeof useLargeLanguageModelLazyQuery>;
export type LargeLanguageModelQueryResult = Apollo.QueryResult<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>;
export const SchedulersDocument = gql`
    query Schedulers {
  schedulers(searchText: "FAILURE") {
    edges {
      node {
        scheduleId
        datasource {
          id
          name
        }
        status
      }
    }
  }
}
    `;

/**
 * __useSchedulersQuery__
 *
 * To run a query within a React component, call `useSchedulersQuery` and pass it any options that fit your needs.
 * When your component renders, `useSchedulersQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSchedulersQuery({
 *   variables: {
 *   },
 * });
 */
export function useSchedulersQuery(baseOptions?: Apollo.QueryHookOptions<SchedulersQuery, SchedulersQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SchedulersQuery, SchedulersQueryVariables>(SchedulersDocument, options);
      }
export function useSchedulersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SchedulersQuery, SchedulersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SchedulersQuery, SchedulersQueryVariables>(SchedulersDocument, options);
        }
export type SchedulersQueryHookResult = ReturnType<typeof useSchedulersQuery>;
export type SchedulersLazyQueryHookResult = ReturnType<typeof useSchedulersLazyQuery>;
export type SchedulersQueryResult = Apollo.QueryResult<SchedulersQuery, SchedulersQueryVariables>;
export const SchedulersErrorDocument = gql`
    query schedulersError {
  schedulers(searchText: "ERROR") {
    edges {
      node {
        scheduleId
        createDate
        datasource {
          id
          name
        }
        status
      }
    }
  }
}
    `;

/**
 * __useSchedulersErrorQuery__
 *
 * To run a query within a React component, call `useSchedulersErrorQuery` and pass it any options that fit your needs.
 * When your component renders, `useSchedulersErrorQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSchedulersErrorQuery({
 *   variables: {
 *   },
 * });
 */
export function useSchedulersErrorQuery(baseOptions?: Apollo.QueryHookOptions<SchedulersErrorQuery, SchedulersErrorQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SchedulersErrorQuery, SchedulersErrorQueryVariables>(SchedulersErrorDocument, options);
      }
export function useSchedulersErrorLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SchedulersErrorQuery, SchedulersErrorQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SchedulersErrorQuery, SchedulersErrorQueryVariables>(SchedulersErrorDocument, options);
        }
export type SchedulersErrorQueryHookResult = ReturnType<typeof useSchedulersErrorQuery>;
export type SchedulersErrorLazyQueryHookResult = ReturnType<typeof useSchedulersErrorLazyQuery>;
export type SchedulersErrorQueryResult = Apollo.QueryResult<SchedulersErrorQuery, SchedulersErrorQueryVariables>;
export const SchedulerDocument = gql`
    query Scheduler($id: ID!) {
  scheduler(id: $id) {
    scheduleId
    createDate
    modifiedDate
    lastIngestionDate
    status
    errorDescription
  }
}
    `;

/**
 * __useSchedulerQuery__
 *
 * To run a query within a React component, call `useSchedulerQuery` and pass it any options that fit your needs.
 * When your component renders, `useSchedulerQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSchedulerQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useSchedulerQuery(baseOptions: Apollo.QueryHookOptions<SchedulerQuery, SchedulerQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SchedulerQuery, SchedulerQueryVariables>(SchedulerDocument, options);
      }
export function useSchedulerLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SchedulerQuery, SchedulerQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SchedulerQuery, SchedulerQueryVariables>(SchedulerDocument, options);
        }
export type SchedulerQueryHookResult = ReturnType<typeof useSchedulerQuery>;
export type SchedulerLazyQueryHookResult = ReturnType<typeof useSchedulerLazyQuery>;
export type SchedulerQueryResult = Apollo.QueryResult<SchedulerQuery, SchedulerQueryVariables>;
export const PluginDriverDocument = gql`
    query PluginDriver($id: ID!) {
  pluginDriver(id: $id) {
    id
    name
    description
    type
    jsonConfig
    provisioning
    aclMappings {
      userField
      docTypeField {
        name
        id
        fieldName
      }
    }
  }
}
    `;

/**
 * __usePluginDriverQuery__
 *
 * To run a query within a React component, call `usePluginDriverQuery` and pass it any options that fit your needs.
 * When your component renders, `usePluginDriverQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = usePluginDriverQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function usePluginDriverQuery(baseOptions: Apollo.QueryHookOptions<PluginDriverQuery, PluginDriverQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PluginDriverQuery, PluginDriverQueryVariables>(PluginDriverDocument, options);
      }
export function usePluginDriverLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PluginDriverQuery, PluginDriverQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PluginDriverQuery, PluginDriverQueryVariables>(PluginDriverDocument, options);
        }
export type PluginDriverQueryHookResult = ReturnType<typeof usePluginDriverQuery>;
export type PluginDriverLazyQueryHookResult = ReturnType<typeof usePluginDriverLazyQuery>;
export type PluginDriverQueryResult = Apollo.QueryResult<PluginDriverQuery, PluginDriverQueryVariables>;
export const CreateOrUpdatePluginDriverMutationDocument = gql`
    mutation CreateOrUpdatePluginDriverMutation($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $jsonConfig: String, $provisioning: Provisioning!) {
  pluginDriver(
    id: $id
    pluginDriverDTO: {name: $name, description: $description, type: $type, jsonConfig: $jsonConfig, provisioning: $provisioning}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdatePluginDriverMutationMutationFn = Apollo.MutationFunction<CreateOrUpdatePluginDriverMutationMutation, CreateOrUpdatePluginDriverMutationMutationVariables>;

/**
 * __useCreateOrUpdatePluginDriverMutationMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdatePluginDriverMutationMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdatePluginDriverMutationMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdatePluginDriverMutationMutation, { data, loading, error }] = useCreateOrUpdatePluginDriverMutationMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      type: // value for 'type'
 *      jsonConfig: // value for 'jsonConfig'
 *      provisioning: // value for 'provisioning'
 *   },
 * });
 */
export function useCreateOrUpdatePluginDriverMutationMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdatePluginDriverMutationMutation, CreateOrUpdatePluginDriverMutationMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdatePluginDriverMutationMutation, CreateOrUpdatePluginDriverMutationMutationVariables>(CreateOrUpdatePluginDriverMutationDocument, options);
      }
export type CreateOrUpdatePluginDriverMutationMutationHookResult = ReturnType<typeof useCreateOrUpdatePluginDriverMutationMutation>;
export type CreateOrUpdatePluginDriverMutationMutationResult = Apollo.MutationResult<CreateOrUpdatePluginDriverMutationMutation>;
export type CreateOrUpdatePluginDriverMutationMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdatePluginDriverMutationMutation, CreateOrUpdatePluginDriverMutationMutationVariables>;
export const PluginDriverByNameDocument = gql`
    query PluginDriverByName($name: String) {
  pluginDrivers(searchText: $name, first: 1) {
    edges {
      node {
        id
      }
    }
  }
}
    `;

/**
 * __usePluginDriverByNameQuery__
 *
 * To run a query within a React component, call `usePluginDriverByNameQuery` and pass it any options that fit your needs.
 * When your component renders, `usePluginDriverByNameQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = usePluginDriverByNameQuery({
 *   variables: {
 *      name: // value for 'name'
 *   },
 * });
 */
export function usePluginDriverByNameQuery(baseOptions?: Apollo.QueryHookOptions<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>(PluginDriverByNameDocument, options);
      }
export function usePluginDriverByNameLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>(PluginDriverByNameDocument, options);
        }
export type PluginDriverByNameQueryHookResult = ReturnType<typeof usePluginDriverByNameQuery>;
export type PluginDriverByNameLazyQueryHookResult = ReturnType<typeof usePluginDriverByNameLazyQuery>;
export type PluginDriverByNameQueryResult = Apollo.QueryResult<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>;
export const PluginDriversInfoQueryDocument = gql`
    query PluginDriversInfoQuery($searchText: String, $after: String) {
  pluginDrivers(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        type
        aclMappings {
          userField
          docTypeField {
            fieldName
          }
        }
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __usePluginDriversInfoQueryQuery__
 *
 * To run a query within a React component, call `usePluginDriversInfoQueryQuery` and pass it any options that fit your needs.
 * When your component renders, `usePluginDriversInfoQueryQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = usePluginDriversInfoQueryQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function usePluginDriversInfoQueryQuery(baseOptions?: Apollo.QueryHookOptions<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>(PluginDriversInfoQueryDocument, options);
      }
export function usePluginDriversInfoQueryLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>(PluginDriversInfoQueryDocument, options);
        }
export type PluginDriversInfoQueryQueryHookResult = ReturnType<typeof usePluginDriversInfoQueryQuery>;
export type PluginDriversInfoQueryLazyQueryHookResult = ReturnType<typeof usePluginDriversInfoQueryLazyQuery>;
export type PluginDriversInfoQueryQueryResult = Apollo.QueryResult<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>;
export const DeletePluginDriverDocument = gql`
    mutation DeletePluginDriver($id: ID!) {
  deletePluginDriver(pluginDriverId: $id) {
    id
    name
  }
}
    `;
export type DeletePluginDriverMutationFn = Apollo.MutationFunction<DeletePluginDriverMutation, DeletePluginDriverMutationVariables>;

/**
 * __useDeletePluginDriverMutation__
 *
 * To run a mutation, you first call `useDeletePluginDriverMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeletePluginDriverMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deletePluginDriverMutation, { data, loading, error }] = useDeletePluginDriverMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeletePluginDriverMutation(baseOptions?: Apollo.MutationHookOptions<DeletePluginDriverMutation, DeletePluginDriverMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeletePluginDriverMutation, DeletePluginDriverMutationVariables>(DeletePluginDriverDocument, options);
      }
export type DeletePluginDriverMutationHookResult = ReturnType<typeof useDeletePluginDriverMutation>;
export type DeletePluginDriverMutationResult = Apollo.MutationResult<DeletePluginDriverMutation>;
export type DeletePluginDriverMutationOptions = Apollo.BaseMutationOptions<DeletePluginDriverMutation, DeletePluginDriverMutationVariables>;
export const PluginDriverWithDocTypeDocument = gql`
    mutation PluginDriverWithDocType($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $jsonConfig: String!, $provisioning: Provisioning!, $docTypeUserDTOSet: [DocTypeUserDTOInput]) {
  pluginDriverWithDocType(
    id: $id
    pluginWithDocTypeDTO: {name: $name, description: $description, type: $type, jsonConfig: $jsonConfig, provisioning: $provisioning, docTypeUserDTOSet: $docTypeUserDTOSet}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type PluginDriverWithDocTypeMutationFn = Apollo.MutationFunction<PluginDriverWithDocTypeMutation, PluginDriverWithDocTypeMutationVariables>;

/**
 * __usePluginDriverWithDocTypeMutation__
 *
 * To run a mutation, you first call `usePluginDriverWithDocTypeMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `usePluginDriverWithDocTypeMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [pluginDriverWithDocTypeMutation, { data, loading, error }] = usePluginDriverWithDocTypeMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      type: // value for 'type'
 *      jsonConfig: // value for 'jsonConfig'
 *      provisioning: // value for 'provisioning'
 *      docTypeUserDTOSet: // value for 'docTypeUserDTOSet'
 *   },
 * });
 */
export function usePluginDriverWithDocTypeMutation(baseOptions?: Apollo.MutationHookOptions<PluginDriverWithDocTypeMutation, PluginDriverWithDocTypeMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<PluginDriverWithDocTypeMutation, PluginDriverWithDocTypeMutationVariables>(PluginDriverWithDocTypeDocument, options);
      }
export type PluginDriverWithDocTypeMutationHookResult = ReturnType<typeof usePluginDriverWithDocTypeMutation>;
export type PluginDriverWithDocTypeMutationResult = Apollo.MutationResult<PluginDriverWithDocTypeMutation>;
export type PluginDriverWithDocTypeMutationOptions = Apollo.BaseMutationOptions<PluginDriverWithDocTypeMutation, PluginDriverWithDocTypeMutationVariables>;
export const PluginDriverToDocumentTypeFieldsDocument = gql`
    query PluginDriverToDocumentTypeFields($parentId: ID!, $searchText: String, $cursor: String) {
  pluginDriver(id: $parentId) {
    id
    aclMappings {
      userField
      docTypeField {
        id
        name
      }
    }
    docTypeFields(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          docType {
            id
          }
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
}
    `;

/**
 * __usePluginDriverToDocumentTypeFieldsQuery__
 *
 * To run a query within a React component, call `usePluginDriverToDocumentTypeFieldsQuery` and pass it any options that fit your needs.
 * When your component renders, `usePluginDriverToDocumentTypeFieldsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = usePluginDriverToDocumentTypeFieldsQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function usePluginDriverToDocumentTypeFieldsQuery(baseOptions: Apollo.QueryHookOptions<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>(PluginDriverToDocumentTypeFieldsDocument, options);
      }
export function usePluginDriverToDocumentTypeFieldsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>(PluginDriverToDocumentTypeFieldsDocument, options);
        }
export type PluginDriverToDocumentTypeFieldsQueryHookResult = ReturnType<typeof usePluginDriverToDocumentTypeFieldsQuery>;
export type PluginDriverToDocumentTypeFieldsLazyQueryHookResult = ReturnType<typeof usePluginDriverToDocumentTypeFieldsLazyQuery>;
export type PluginDriverToDocumentTypeFieldsQueryResult = Apollo.QueryResult<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>;
export const DocumentTypeFieldsForPluginDocument = gql`
    query DocumentTypeFieldsForPlugin($searchText: String) {
  docTypeFields(searchText: $searchText) {
    edges {
      node {
        id
        name
        description
        __typename
      }
      __typename
    }
    __typename
  }
}
    `;

/**
 * __useDocumentTypeFieldsForPluginQuery__
 *
 * To run a query within a React component, call `useDocumentTypeFieldsForPluginQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocumentTypeFieldsForPluginQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocumentTypeFieldsForPluginQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *   },
 * });
 */
export function useDocumentTypeFieldsForPluginQuery(baseOptions?: Apollo.QueryHookOptions<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>(DocumentTypeFieldsForPluginDocument, options);
      }
export function useDocumentTypeFieldsForPluginLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>(DocumentTypeFieldsForPluginDocument, options);
        }
export type DocumentTypeFieldsForPluginQueryHookResult = ReturnType<typeof useDocumentTypeFieldsForPluginQuery>;
export type DocumentTypeFieldsForPluginLazyQueryHookResult = ReturnType<typeof useDocumentTypeFieldsForPluginLazyQuery>;
export type DocumentTypeFieldsForPluginQueryResult = Apollo.QueryResult<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>;
export const QueryAnalysesRulesDocument = gql`
    query QueryAnalysesRules($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  queryAnalysis(id: $parentId) {
    id
    rules(
      searchText: $searchText
      notEqual: $unassociated
      first: 20
      after: $cursor
    ) {
      edges {
        node {
          id
          name
          lhs
          rhs
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
}
    `;

/**
 * __useQueryAnalysesRulesQuery__
 *
 * To run a query within a React component, call `useQueryAnalysesRulesQuery` and pass it any options that fit your needs.
 * When your component renders, `useQueryAnalysesRulesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQueryAnalysesRulesQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      unassociated: // value for 'unassociated'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useQueryAnalysesRulesQuery(baseOptions: Apollo.QueryHookOptions<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>(QueryAnalysesRulesDocument, options);
      }
export function useQueryAnalysesRulesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>(QueryAnalysesRulesDocument, options);
        }
export type QueryAnalysesRulesQueryHookResult = ReturnType<typeof useQueryAnalysesRulesQuery>;
export type QueryAnalysesRulesLazyQueryHookResult = ReturnType<typeof useQueryAnalysesRulesLazyQuery>;
export type QueryAnalysesRulesQueryResult = Apollo.QueryResult<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>;
export const AddRulesToQueryAnalysesDocument = gql`
    mutation AddRulesToQueryAnalyses($childId: ID!, $parentId: ID!) {
  addRuleToQueryAnalysis(ruleId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddRulesToQueryAnalysesMutationFn = Apollo.MutationFunction<AddRulesToQueryAnalysesMutation, AddRulesToQueryAnalysesMutationVariables>;

/**
 * __useAddRulesToQueryAnalysesMutation__
 *
 * To run a mutation, you first call `useAddRulesToQueryAnalysesMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddRulesToQueryAnalysesMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addRulesToQueryAnalysesMutation, { data, loading, error }] = useAddRulesToQueryAnalysesMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddRulesToQueryAnalysesMutation(baseOptions?: Apollo.MutationHookOptions<AddRulesToQueryAnalysesMutation, AddRulesToQueryAnalysesMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddRulesToQueryAnalysesMutation, AddRulesToQueryAnalysesMutationVariables>(AddRulesToQueryAnalysesDocument, options);
      }
export type AddRulesToQueryAnalysesMutationHookResult = ReturnType<typeof useAddRulesToQueryAnalysesMutation>;
export type AddRulesToQueryAnalysesMutationResult = Apollo.MutationResult<AddRulesToQueryAnalysesMutation>;
export type AddRulesToQueryAnalysesMutationOptions = Apollo.BaseMutationOptions<AddRulesToQueryAnalysesMutation, AddRulesToQueryAnalysesMutationVariables>;
export const RemoveRuleFromQueryAnalysesDocument = gql`
    mutation RemoveRuleFromQueryAnalyses($childId: ID!, $parentId: ID!) {
  removeRuleFromQueryAnalysis(ruleId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveRuleFromQueryAnalysesMutationFn = Apollo.MutationFunction<RemoveRuleFromQueryAnalysesMutation, RemoveRuleFromQueryAnalysesMutationVariables>;

/**
 * __useRemoveRuleFromQueryAnalysesMutation__
 *
 * To run a mutation, you first call `useRemoveRuleFromQueryAnalysesMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveRuleFromQueryAnalysesMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeRuleFromQueryAnalysesMutation, { data, loading, error }] = useRemoveRuleFromQueryAnalysesMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveRuleFromQueryAnalysesMutation(baseOptions?: Apollo.MutationHookOptions<RemoveRuleFromQueryAnalysesMutation, RemoveRuleFromQueryAnalysesMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveRuleFromQueryAnalysesMutation, RemoveRuleFromQueryAnalysesMutationVariables>(RemoveRuleFromQueryAnalysesDocument, options);
      }
export type RemoveRuleFromQueryAnalysesMutationHookResult = ReturnType<typeof useRemoveRuleFromQueryAnalysesMutation>;
export type RemoveRuleFromQueryAnalysesMutationResult = Apollo.MutationResult<RemoveRuleFromQueryAnalysesMutation>;
export type RemoveRuleFromQueryAnalysesMutationOptions = Apollo.BaseMutationOptions<RemoveRuleFromQueryAnalysesMutation, RemoveRuleFromQueryAnalysesMutationVariables>;
export const QueryAnalysisDocument = gql`
    query QueryAnalysis($id: ID!) {
  queryAnalysis(id: $id) {
    id
    name
    description
    stopWords
    annotators {
      edges {
        node {
          id
          name
        }
      }
    }
    rules {
      edges {
        node {
          id
          name
        }
      }
    }
  }
}
    `;

/**
 * __useQueryAnalysisQuery__
 *
 * To run a query within a React component, call `useQueryAnalysisQuery` and pass it any options that fit your needs.
 * When your component renders, `useQueryAnalysisQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQueryAnalysisQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useQueryAnalysisQuery(baseOptions: Apollo.QueryHookOptions<QueryAnalysisQuery, QueryAnalysisQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysisQuery, QueryAnalysisQueryVariables>(QueryAnalysisDocument, options);
      }
export function useQueryAnalysisLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysisQuery, QueryAnalysisQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysisQuery, QueryAnalysisQueryVariables>(QueryAnalysisDocument, options);
        }
export type QueryAnalysisQueryHookResult = ReturnType<typeof useQueryAnalysisQuery>;
export type QueryAnalysisLazyQueryHookResult = ReturnType<typeof useQueryAnalysisLazyQuery>;
export type QueryAnalysisQueryResult = Apollo.QueryResult<QueryAnalysisQuery, QueryAnalysisQueryVariables>;
export const QueryAnalysesDocument = gql`
    query QueryAnalyses($searchText: String, $after: String) {
  queryAnalyses(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useQueryAnalysesQuery__
 *
 * To run a query within a React component, call `useQueryAnalysesQuery` and pass it any options that fit your needs.
 * When your component renders, `useQueryAnalysesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQueryAnalysesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useQueryAnalysesQuery(baseOptions?: Apollo.QueryHookOptions<QueryAnalysesQuery, QueryAnalysesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysesQuery, QueryAnalysesQueryVariables>(QueryAnalysesDocument, options);
      }
export function useQueryAnalysesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysesQuery, QueryAnalysesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysesQuery, QueryAnalysesQueryVariables>(QueryAnalysesDocument, options);
        }
export type QueryAnalysesQueryHookResult = ReturnType<typeof useQueryAnalysesQuery>;
export type QueryAnalysesLazyQueryHookResult = ReturnType<typeof useQueryAnalysesLazyQuery>;
export type QueryAnalysesQueryResult = Apollo.QueryResult<QueryAnalysesQuery, QueryAnalysesQueryVariables>;
export const DeleteQueryAnalysisDocument = gql`
    mutation DeleteQueryAnalysis($id: ID!) {
  deleteQueryAnalysis(queryAnalysisId: $id) {
    id
    name
  }
}
    `;
export type DeleteQueryAnalysisMutationFn = Apollo.MutationFunction<DeleteQueryAnalysisMutation, DeleteQueryAnalysisMutationVariables>;

/**
 * __useDeleteQueryAnalysisMutation__
 *
 * To run a mutation, you first call `useDeleteQueryAnalysisMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteQueryAnalysisMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteQueryAnalysisMutation, { data, loading, error }] = useDeleteQueryAnalysisMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteQueryAnalysisMutation(baseOptions?: Apollo.MutationHookOptions<DeleteQueryAnalysisMutation, DeleteQueryAnalysisMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteQueryAnalysisMutation, DeleteQueryAnalysisMutationVariables>(DeleteQueryAnalysisDocument, options);
      }
export type DeleteQueryAnalysisMutationHookResult = ReturnType<typeof useDeleteQueryAnalysisMutation>;
export type DeleteQueryAnalysisMutationResult = Apollo.MutationResult<DeleteQueryAnalysisMutation>;
export type DeleteQueryAnalysisMutationOptions = Apollo.BaseMutationOptions<DeleteQueryAnalysisMutation, DeleteQueryAnalysisMutationVariables>;
export const CreateOrUpdateQueryAnalysisDocument = gql`
    mutation CreateOrUpdateQueryAnalysis($id: ID, $name: String!, $description: String, $stopWords: String, $annotatorsIds: [BigInteger], $rulesIds: [BigInteger]) {
  queryAnalysisWithLists(
    id: $id
    queryAnalysisWithListsDTO: {name: $name, description: $description, stopWords: $stopWords, annotatorsIds: $annotatorsIds, rulesIds: $rulesIds}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateQueryAnalysisMutationFn = Apollo.MutationFunction<CreateOrUpdateQueryAnalysisMutation, CreateOrUpdateQueryAnalysisMutationVariables>;

/**
 * __useCreateOrUpdateQueryAnalysisMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateQueryAnalysisMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateQueryAnalysisMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateQueryAnalysisMutation, { data, loading, error }] = useCreateOrUpdateQueryAnalysisMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      stopWords: // value for 'stopWords'
 *      annotatorsIds: // value for 'annotatorsIds'
 *      rulesIds: // value for 'rulesIds'
 *   },
 * });
 */
export function useCreateOrUpdateQueryAnalysisMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateQueryAnalysisMutation, CreateOrUpdateQueryAnalysisMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateQueryAnalysisMutation, CreateOrUpdateQueryAnalysisMutationVariables>(CreateOrUpdateQueryAnalysisDocument, options);
      }
export type CreateOrUpdateQueryAnalysisMutationHookResult = ReturnType<typeof useCreateOrUpdateQueryAnalysisMutation>;
export type CreateOrUpdateQueryAnalysisMutationResult = Apollo.MutationResult<CreateOrUpdateQueryAnalysisMutation>;
export type CreateOrUpdateQueryAnalysisMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateQueryAnalysisMutation, CreateOrUpdateQueryAnalysisMutationVariables>;
export const QueryAnalysisAssociationsDocument = gql`
    query QueryAnalysisAssociations($parentId: ID!, $unassociated: Boolean!) {
  queryAnalysis(id: $parentId) {
    id
    annotators(notEqual: $unassociated) {
      edges {
        node {
          id
          name
        }
      }
    }
    rules(notEqual: $unassociated) {
      edges {
        node {
          id
          name
        }
      }
    }
  }
}
    `;

/**
 * __useQueryAnalysisAssociationsQuery__
 *
 * To run a query within a React component, call `useQueryAnalysisAssociationsQuery` and pass it any options that fit your needs.
 * When your component renders, `useQueryAnalysisAssociationsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQueryAnalysisAssociationsQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      unassociated: // value for 'unassociated'
 *   },
 * });
 */
export function useQueryAnalysisAssociationsQuery(baseOptions: Apollo.QueryHookOptions<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>(QueryAnalysisAssociationsDocument, options);
      }
export function useQueryAnalysisAssociationsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>(QueryAnalysisAssociationsDocument, options);
        }
export type QueryAnalysisAssociationsQueryHookResult = ReturnType<typeof useQueryAnalysisAssociationsQuery>;
export type QueryAnalysisAssociationsLazyQueryHookResult = ReturnType<typeof useQueryAnalysisAssociationsLazyQuery>;
export type QueryAnalysisAssociationsQueryResult = Apollo.QueryResult<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>;
export const RagConfigurationsDocument = gql`
    query RagConfigurations($searchText: String, $after: String) {
  ragConfigurations(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        type
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useRagConfigurationsQuery__
 *
 * To run a query within a React component, call `useRagConfigurationsQuery` and pass it any options that fit your needs.
 * When your component renders, `useRagConfigurationsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useRagConfigurationsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useRagConfigurationsQuery(baseOptions?: Apollo.QueryHookOptions<RagConfigurationsQuery, RagConfigurationsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<RagConfigurationsQuery, RagConfigurationsQueryVariables>(RagConfigurationsDocument, options);
      }
export function useRagConfigurationsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<RagConfigurationsQuery, RagConfigurationsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<RagConfigurationsQuery, RagConfigurationsQueryVariables>(RagConfigurationsDocument, options);
        }
export type RagConfigurationsQueryHookResult = ReturnType<typeof useRagConfigurationsQuery>;
export type RagConfigurationsLazyQueryHookResult = ReturnType<typeof useRagConfigurationsLazyQuery>;
export type RagConfigurationsQueryResult = Apollo.QueryResult<RagConfigurationsQuery, RagConfigurationsQueryVariables>;
export const RagConfigurationDocument = gql`
    query RagConfiguration($id: ID!) {
  ragConfiguration(id: $id) {
    id
    name
    description
    type
    reformulate
    chunkWindow
    rephrasePrompt
    prompt
    jsonConfig
    ragToolDescription
    promptNoRag
  }
}
    `;

/**
 * __useRagConfigurationQuery__
 *
 * To run a query within a React component, call `useRagConfigurationQuery` and pass it any options that fit your needs.
 * When your component renders, `useRagConfigurationQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useRagConfigurationQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useRagConfigurationQuery(baseOptions: Apollo.QueryHookOptions<RagConfigurationQuery, RagConfigurationQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<RagConfigurationQuery, RagConfigurationQueryVariables>(RagConfigurationDocument, options);
      }
export function useRagConfigurationLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<RagConfigurationQuery, RagConfigurationQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<RagConfigurationQuery, RagConfigurationQueryVariables>(RagConfigurationDocument, options);
        }
export type RagConfigurationQueryHookResult = ReturnType<typeof useRagConfigurationQuery>;
export type RagConfigurationLazyQueryHookResult = ReturnType<typeof useRagConfigurationLazyQuery>;
export type RagConfigurationQueryResult = Apollo.QueryResult<RagConfigurationQuery, RagConfigurationQueryVariables>;
export const CreateRagConfigDocument = gql`
    mutation createRAGConfig($name: String!, $description: String, $type: RAGType!, $reformulate: Boolean, $chunkWindow: Int, $rephrasePrompt: String, $prompt: String, $jsonConfig: String, $ragToolDescription: String, $promptNoRag: String) {
  createRAGConfiguration(
    createRAGConfigurationDTO: {name: $name, description: $description, type: $type, reformulate: $reformulate, chunkWindow: $chunkWindow, rephrasePrompt: $rephrasePrompt, prompt: $prompt, jsonConfig: $jsonConfig, ragToolDescription: $ragToolDescription, promptNoRag: $promptNoRag}
  ) {
    entity {
      id
      name
      type
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateRagConfigMutationFn = Apollo.MutationFunction<CreateRagConfigMutation, CreateRagConfigMutationVariables>;

/**
 * __useCreateRagConfigMutation__
 *
 * To run a mutation, you first call `useCreateRagConfigMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateRagConfigMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createRagConfigMutation, { data, loading, error }] = useCreateRagConfigMutation({
 *   variables: {
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      type: // value for 'type'
 *      reformulate: // value for 'reformulate'
 *      chunkWindow: // value for 'chunkWindow'
 *      rephrasePrompt: // value for 'rephrasePrompt'
 *      prompt: // value for 'prompt'
 *      jsonConfig: // value for 'jsonConfig'
 *      ragToolDescription: // value for 'ragToolDescription'
 *      promptNoRag: // value for 'promptNoRag'
 *   },
 * });
 */
export function useCreateRagConfigMutation(baseOptions?: Apollo.MutationHookOptions<CreateRagConfigMutation, CreateRagConfigMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateRagConfigMutation, CreateRagConfigMutationVariables>(CreateRagConfigDocument, options);
      }
export type CreateRagConfigMutationHookResult = ReturnType<typeof useCreateRagConfigMutation>;
export type CreateRagConfigMutationResult = Apollo.MutationResult<CreateRagConfigMutation>;
export type CreateRagConfigMutationOptions = Apollo.BaseMutationOptions<CreateRagConfigMutation, CreateRagConfigMutationVariables>;
export const UpdateRagConfigurationDocument = gql`
    mutation updateRAGConfiguration($id: ID!, $name: String!, $description: String, $reformulate: Boolean, $chunkWindow: Int, $rephrasePrompt: String, $prompt: String, $jsonConfig: String, $ragToolDescription: String, $promptNoRag: String, $patch: Boolean) {
  updateRAGConfiguration(
    id: $id
    patch: $patch
    ragConfigurationDTO: {name: $name, description: $description, reformulate: $reformulate, chunkWindow: $chunkWindow, rephrasePrompt: $rephrasePrompt, prompt: $prompt, jsonConfig: $jsonConfig, ragToolDescription: $ragToolDescription, promptNoRag: $promptNoRag}
  ) {
    entity {
      id
      name
      type
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type UpdateRagConfigurationMutationFn = Apollo.MutationFunction<UpdateRagConfigurationMutation, UpdateRagConfigurationMutationVariables>;

/**
 * __useUpdateRagConfigurationMutation__
 *
 * To run a mutation, you first call `useUpdateRagConfigurationMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUpdateRagConfigurationMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [updateRagConfigurationMutation, { data, loading, error }] = useUpdateRagConfigurationMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      reformulate: // value for 'reformulate'
 *      chunkWindow: // value for 'chunkWindow'
 *      rephrasePrompt: // value for 'rephrasePrompt'
 *      prompt: // value for 'prompt'
 *      jsonConfig: // value for 'jsonConfig'
 *      ragToolDescription: // value for 'ragToolDescription'
 *      promptNoRag: // value for 'promptNoRag'
 *      patch: // value for 'patch'
 *   },
 * });
 */
export function useUpdateRagConfigurationMutation(baseOptions?: Apollo.MutationHookOptions<UpdateRagConfigurationMutation, UpdateRagConfigurationMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UpdateRagConfigurationMutation, UpdateRagConfigurationMutationVariables>(UpdateRagConfigurationDocument, options);
      }
export type UpdateRagConfigurationMutationHookResult = ReturnType<typeof useUpdateRagConfigurationMutation>;
export type UpdateRagConfigurationMutationResult = Apollo.MutationResult<UpdateRagConfigurationMutation>;
export type UpdateRagConfigurationMutationOptions = Apollo.BaseMutationOptions<UpdateRagConfigurationMutation, UpdateRagConfigurationMutationVariables>;
export const DeleteRagConfigurationDocument = gql`
    mutation DeleteRagConfiguration($id: ID!) {
  deleteRAGConfiguration(id: $id) {
    id
  }
}
    `;
export type DeleteRagConfigurationMutationFn = Apollo.MutationFunction<DeleteRagConfigurationMutation, DeleteRagConfigurationMutationVariables>;

/**
 * __useDeleteRagConfigurationMutation__
 *
 * To run a mutation, you first call `useDeleteRagConfigurationMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteRagConfigurationMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteRagConfigurationMutation, { data, loading, error }] = useDeleteRagConfigurationMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteRagConfigurationMutation(baseOptions?: Apollo.MutationHookOptions<DeleteRagConfigurationMutation, DeleteRagConfigurationMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteRagConfigurationMutation, DeleteRagConfigurationMutationVariables>(DeleteRagConfigurationDocument, options);
      }
export type DeleteRagConfigurationMutationHookResult = ReturnType<typeof useDeleteRagConfigurationMutation>;
export type DeleteRagConfigurationMutationResult = Apollo.MutationResult<DeleteRagConfigurationMutation>;
export type DeleteRagConfigurationMutationOptions = Apollo.BaseMutationOptions<DeleteRagConfigurationMutation, DeleteRagConfigurationMutationVariables>;
export const UnboundRagConfigurationsByBucketDocument = gql`
    query UnboundRagConfigurationsByBucket($bucketId: ID!, $ragType: RAGType!) {
  unboundRAGConfigurationByBucket(bucketId: $bucketId, ragType: $ragType) {
    id
    name
  }
}
    `;

/**
 * __useUnboundRagConfigurationsByBucketQuery__
 *
 * To run a query within a React component, call `useUnboundRagConfigurationsByBucketQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundRagConfigurationsByBucketQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundRagConfigurationsByBucketQuery({
 *   variables: {
 *      bucketId: // value for 'bucketId'
 *      ragType: // value for 'ragType'
 *   },
 * });
 */
export function useUnboundRagConfigurationsByBucketQuery(baseOptions: Apollo.QueryHookOptions<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>(UnboundRagConfigurationsByBucketDocument, options);
      }
export function useUnboundRagConfigurationsByBucketLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>(UnboundRagConfigurationsByBucketDocument, options);
        }
export type UnboundRagConfigurationsByBucketQueryHookResult = ReturnType<typeof useUnboundRagConfigurationsByBucketQuery>;
export type UnboundRagConfigurationsByBucketLazyQueryHookResult = ReturnType<typeof useUnboundRagConfigurationsByBucketLazyQuery>;
export type UnboundRagConfigurationsByBucketQueryResult = Apollo.QueryResult<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>;
export const RulesDocument = gql`
    query Rules($searchText: String, $after: String) {
  rules(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        lhs
        rhs
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useRulesQuery__
 *
 * To run a query within a React component, call `useRulesQuery` and pass it any options that fit your needs.
 * When your component renders, `useRulesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useRulesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useRulesQuery(baseOptions?: Apollo.QueryHookOptions<RulesQuery, RulesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<RulesQuery, RulesQueryVariables>(RulesDocument, options);
      }
export function useRulesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<RulesQuery, RulesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<RulesQuery, RulesQueryVariables>(RulesDocument, options);
        }
export type RulesQueryHookResult = ReturnType<typeof useRulesQuery>;
export type RulesLazyQueryHookResult = ReturnType<typeof useRulesLazyQuery>;
export type RulesQueryResult = Apollo.QueryResult<RulesQuery, RulesQueryVariables>;
export const DeleteRulesDocument = gql`
    mutation DeleteRules($id: ID!) {
  deleteRule(ruleId: $id) {
    id
    name
  }
}
    `;
export type DeleteRulesMutationFn = Apollo.MutationFunction<DeleteRulesMutation, DeleteRulesMutationVariables>;

/**
 * __useDeleteRulesMutation__
 *
 * To run a mutation, you first call `useDeleteRulesMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteRulesMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteRulesMutation, { data, loading, error }] = useDeleteRulesMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteRulesMutation(baseOptions?: Apollo.MutationHookOptions<DeleteRulesMutation, DeleteRulesMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteRulesMutation, DeleteRulesMutationVariables>(DeleteRulesDocument, options);
      }
export type DeleteRulesMutationHookResult = ReturnType<typeof useDeleteRulesMutation>;
export type DeleteRulesMutationResult = Apollo.MutationResult<DeleteRulesMutation>;
export type DeleteRulesMutationOptions = Apollo.BaseMutationOptions<DeleteRulesMutation, DeleteRulesMutationVariables>;
export const RuleDocument = gql`
    query Rule($id: ID!) {
  rule: rule(id: $id) {
    id
    name
    description
    lhs
    rhs
  }
}
    `;

/**
 * __useRuleQuery__
 *
 * To run a query within a React component, call `useRuleQuery` and pass it any options that fit your needs.
 * When your component renders, `useRuleQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useRuleQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useRuleQuery(baseOptions: Apollo.QueryHookOptions<RuleQuery, RuleQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<RuleQuery, RuleQueryVariables>(RuleDocument, options);
      }
export function useRuleLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<RuleQuery, RuleQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<RuleQuery, RuleQueryVariables>(RuleDocument, options);
        }
export type RuleQueryHookResult = ReturnType<typeof useRuleQuery>;
export type RuleLazyQueryHookResult = ReturnType<typeof useRuleLazyQuery>;
export type RuleQueryResult = Apollo.QueryResult<RuleQuery, RuleQueryVariables>;
export const CreateOrUpdateRuleQueryDocument = gql`
    mutation CreateOrUpdateRuleQuery($id: ID, $name: String!, $description: String, $lhs: String!, $rhs: String!) {
  rule(
    id: $id
    ruleDTO: {name: $name, description: $description, lhs: $lhs, rhs: $rhs}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateRuleQueryMutationFn = Apollo.MutationFunction<CreateOrUpdateRuleQueryMutation, CreateOrUpdateRuleQueryMutationVariables>;

/**
 * __useCreateOrUpdateRuleQueryMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateRuleQueryMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateRuleQueryMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateRuleQueryMutation, { data, loading, error }] = useCreateOrUpdateRuleQueryMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      lhs: // value for 'lhs'
 *      rhs: // value for 'rhs'
 *   },
 * });
 */
export function useCreateOrUpdateRuleQueryMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateRuleQueryMutation, CreateOrUpdateRuleQueryMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateRuleQueryMutation, CreateOrUpdateRuleQueryMutationVariables>(CreateOrUpdateRuleQueryDocument, options);
      }
export type CreateOrUpdateRuleQueryMutationHookResult = ReturnType<typeof useCreateOrUpdateRuleQueryMutation>;
export type CreateOrUpdateRuleQueryMutationResult = Apollo.MutationResult<CreateOrUpdateRuleQueryMutation>;
export type CreateOrUpdateRuleQueryMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateRuleQueryMutation, CreateOrUpdateRuleQueryMutationVariables>;
export const SearchConfigDocument = gql`
    query SearchConfig($id: ID!) {
  searchConfig(id: $id) {
    id
    name
    description
    minScore
    minScoreSuggestions
    minScoreSearch
    queryParserConfigs {
      edges {
        node {
          id
          name
          type
          jsonConfig
        }
      }
    }
  }
}
    `;

/**
 * __useSearchConfigQuery__
 *
 * To run a query within a React component, call `useSearchConfigQuery` and pass it any options that fit your needs.
 * When your component renders, `useSearchConfigQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSearchConfigQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useSearchConfigQuery(baseOptions: Apollo.QueryHookOptions<SearchConfigQuery, SearchConfigQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SearchConfigQuery, SearchConfigQueryVariables>(SearchConfigDocument, options);
      }
export function useSearchConfigLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SearchConfigQuery, SearchConfigQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SearchConfigQuery, SearchConfigQueryVariables>(SearchConfigDocument, options);
        }
export type SearchConfigQueryHookResult = ReturnType<typeof useSearchConfigQuery>;
export type SearchConfigLazyQueryHookResult = ReturnType<typeof useSearchConfigLazyQuery>;
export type SearchConfigQueryResult = Apollo.QueryResult<SearchConfigQuery, SearchConfigQueryVariables>;
export const CreateOrUpdateSearchConfigDocument = gql`
    mutation CreateOrUpdateSearchConfig($id: ID, $name: String!, $description: String, $minScore: Float!, $minScoreSuggestions: Boolean!, $minScoreSearch: Boolean!, $queryParsersConfig: [QueryParserConfigDTOInput]) {
  searchConfigWithQueryParsers(
    id: $id
    searchConfigWithQueryParsersDTO: {name: $name, description: $description, minScore: $minScore, minScoreSuggestions: $minScoreSuggestions, minScoreSearch: $minScoreSearch, queryParsers: $queryParsersConfig}
  ) {
    entity {
      id
      name
      minScore
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateSearchConfigMutationFn = Apollo.MutationFunction<CreateOrUpdateSearchConfigMutation, CreateOrUpdateSearchConfigMutationVariables>;

/**
 * __useCreateOrUpdateSearchConfigMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateSearchConfigMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateSearchConfigMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateSearchConfigMutation, { data, loading, error }] = useCreateOrUpdateSearchConfigMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      minScore: // value for 'minScore'
 *      minScoreSuggestions: // value for 'minScoreSuggestions'
 *      minScoreSearch: // value for 'minScoreSearch'
 *      queryParsersConfig: // value for 'queryParsersConfig'
 *   },
 * });
 */
export function useCreateOrUpdateSearchConfigMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateSearchConfigMutation, CreateOrUpdateSearchConfigMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateSearchConfigMutation, CreateOrUpdateSearchConfigMutationVariables>(CreateOrUpdateSearchConfigDocument, options);
      }
export type CreateOrUpdateSearchConfigMutationHookResult = ReturnType<typeof useCreateOrUpdateSearchConfigMutation>;
export type CreateOrUpdateSearchConfigMutationResult = Apollo.MutationResult<CreateOrUpdateSearchConfigMutation>;
export type CreateOrUpdateSearchConfigMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateSearchConfigMutation, CreateOrUpdateSearchConfigMutationVariables>;
export const SearchConfigsDocument = gql`
    query SearchConfigs($searchText: String, $after: String) {
  searchConfigs(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        minScore
        minScoreSuggestions
        minScoreSearch
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useSearchConfigsQuery__
 *
 * To run a query within a React component, call `useSearchConfigsQuery` and pass it any options that fit your needs.
 * When your component renders, `useSearchConfigsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSearchConfigsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useSearchConfigsQuery(baseOptions?: Apollo.QueryHookOptions<SearchConfigsQuery, SearchConfigsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SearchConfigsQuery, SearchConfigsQueryVariables>(SearchConfigsDocument, options);
      }
export function useSearchConfigsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SearchConfigsQuery, SearchConfigsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SearchConfigsQuery, SearchConfigsQueryVariables>(SearchConfigsDocument, options);
        }
export type SearchConfigsQueryHookResult = ReturnType<typeof useSearchConfigsQuery>;
export type SearchConfigsLazyQueryHookResult = ReturnType<typeof useSearchConfigsLazyQuery>;
export type SearchConfigsQueryResult = Apollo.QueryResult<SearchConfigsQuery, SearchConfigsQueryVariables>;
export const DeleteSearchConfigDocument = gql`
    mutation DeleteSearchConfig($id: ID!) {
  deleteSearchConfig(searchConfigId: $id) {
    id
    name
  }
}
    `;
export type DeleteSearchConfigMutationFn = Apollo.MutationFunction<DeleteSearchConfigMutation, DeleteSearchConfigMutationVariables>;

/**
 * __useDeleteSearchConfigMutation__
 *
 * To run a mutation, you first call `useDeleteSearchConfigMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteSearchConfigMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteSearchConfigMutation, { data, loading, error }] = useDeleteSearchConfigMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteSearchConfigMutation(baseOptions?: Apollo.MutationHookOptions<DeleteSearchConfigMutation, DeleteSearchConfigMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteSearchConfigMutation, DeleteSearchConfigMutationVariables>(DeleteSearchConfigDocument, options);
      }
export type DeleteSearchConfigMutationHookResult = ReturnType<typeof useDeleteSearchConfigMutation>;
export type DeleteSearchConfigMutationResult = Apollo.MutationResult<DeleteSearchConfigMutation>;
export type DeleteSearchConfigMutationOptions = Apollo.BaseMutationOptions<DeleteSearchConfigMutation, DeleteSearchConfigMutationVariables>;
export const QueryParserConfigDocument = gql`
    query queryParserConfig {
  queryParserConfigFormConfigurations
}
    `;

/**
 * __useQueryParserConfigQuery__
 *
 * To run a query within a React component, call `useQueryParserConfigQuery` and pass it any options that fit your needs.
 * When your component renders, `useQueryParserConfigQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQueryParserConfigQuery({
 *   variables: {
 *   },
 * });
 */
export function useQueryParserConfigQuery(baseOptions?: Apollo.QueryHookOptions<QueryParserConfigQuery, QueryParserConfigQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryParserConfigQuery, QueryParserConfigQueryVariables>(QueryParserConfigDocument, options);
      }
export function useQueryParserConfigLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryParserConfigQuery, QueryParserConfigQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryParserConfigQuery, QueryParserConfigQueryVariables>(QueryParserConfigDocument, options);
        }
export type QueryParserConfigQueryHookResult = ReturnType<typeof useQueryParserConfigQuery>;
export type QueryParserConfigLazyQueryHookResult = ReturnType<typeof useQueryParserConfigLazyQuery>;
export type QueryParserConfigQueryResult = Apollo.QueryResult<QueryParserConfigQuery, QueryParserConfigQueryVariables>;
export const AddSuggestionCategoryTranslationDocument = gql`
    mutation AddSuggestionCategoryTranslation($suggestionCategoryId: ID!, $language: String!, $key: String, $value: String!) {
  addSuggestionCategoryTranslation(
    suggestionCategoryId: $suggestionCategoryId
    language: $language
    key: $key
    value: $value
  ) {
    left
    right
  }
}
    `;
export type AddSuggestionCategoryTranslationMutationFn = Apollo.MutationFunction<AddSuggestionCategoryTranslationMutation, AddSuggestionCategoryTranslationMutationVariables>;

/**
 * __useAddSuggestionCategoryTranslationMutation__
 *
 * To run a mutation, you first call `useAddSuggestionCategoryTranslationMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddSuggestionCategoryTranslationMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addSuggestionCategoryTranslationMutation, { data, loading, error }] = useAddSuggestionCategoryTranslationMutation({
 *   variables: {
 *      suggestionCategoryId: // value for 'suggestionCategoryId'
 *      language: // value for 'language'
 *      key: // value for 'key'
 *      value: // value for 'value'
 *   },
 * });
 */
export function useAddSuggestionCategoryTranslationMutation(baseOptions?: Apollo.MutationHookOptions<AddSuggestionCategoryTranslationMutation, AddSuggestionCategoryTranslationMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddSuggestionCategoryTranslationMutation, AddSuggestionCategoryTranslationMutationVariables>(AddSuggestionCategoryTranslationDocument, options);
      }
export type AddSuggestionCategoryTranslationMutationHookResult = ReturnType<typeof useAddSuggestionCategoryTranslationMutation>;
export type AddSuggestionCategoryTranslationMutationResult = Apollo.MutationResult<AddSuggestionCategoryTranslationMutation>;
export type AddSuggestionCategoryTranslationMutationOptions = Apollo.BaseMutationOptions<AddSuggestionCategoryTranslationMutation, AddSuggestionCategoryTranslationMutationVariables>;
export const UnboundBucketsBySuggestionCategoryDocument = gql`
    query UnboundBucketsBySuggestionCategory($id: BigInteger!) {
  unboundBucketsBySuggestionCategory(suggestionCategoryId: $id) {
    name
    id
  }
}
    `;

/**
 * __useUnboundBucketsBySuggestionCategoryQuery__
 *
 * To run a query within a React component, call `useUnboundBucketsBySuggestionCategoryQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundBucketsBySuggestionCategoryQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundBucketsBySuggestionCategoryQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useUnboundBucketsBySuggestionCategoryQuery(baseOptions: Apollo.QueryHookOptions<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>(UnboundBucketsBySuggestionCategoryDocument, options);
      }
export function useUnboundBucketsBySuggestionCategoryLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>(UnboundBucketsBySuggestionCategoryDocument, options);
        }
export type UnboundBucketsBySuggestionCategoryQueryHookResult = ReturnType<typeof useUnboundBucketsBySuggestionCategoryQuery>;
export type UnboundBucketsBySuggestionCategoryLazyQueryHookResult = ReturnType<typeof useUnboundBucketsBySuggestionCategoryLazyQuery>;
export type UnboundBucketsBySuggestionCategoryQueryResult = Apollo.QueryResult<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>;
export const SuggestionCategoriesDocument = gql`
    query SuggestionCategories($searchText: String, $after: String) {
  suggestionCategories(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        priority
        multiSelect
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useSuggestionCategoriesQuery__
 *
 * To run a query within a React component, call `useSuggestionCategoriesQuery` and pass it any options that fit your needs.
 * When your component renders, `useSuggestionCategoriesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSuggestionCategoriesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useSuggestionCategoriesQuery(baseOptions?: Apollo.QueryHookOptions<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>(SuggestionCategoriesDocument, options);
      }
export function useSuggestionCategoriesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>(SuggestionCategoriesDocument, options);
        }
export type SuggestionCategoriesQueryHookResult = ReturnType<typeof useSuggestionCategoriesQuery>;
export type SuggestionCategoriesLazyQueryHookResult = ReturnType<typeof useSuggestionCategoriesLazyQuery>;
export type SuggestionCategoriesQueryResult = Apollo.QueryResult<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>;
export const DeleteSuggestionCategoryDocument = gql`
    mutation DeleteSuggestionCategory($id: ID!) {
  deleteSuggestionCategory(suggestionCategoryId: $id) {
    id
    name
  }
}
    `;
export type DeleteSuggestionCategoryMutationFn = Apollo.MutationFunction<DeleteSuggestionCategoryMutation, DeleteSuggestionCategoryMutationVariables>;

/**
 * __useDeleteSuggestionCategoryMutation__
 *
 * To run a mutation, you first call `useDeleteSuggestionCategoryMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteSuggestionCategoryMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteSuggestionCategoryMutation, { data, loading, error }] = useDeleteSuggestionCategoryMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteSuggestionCategoryMutation(baseOptions?: Apollo.MutationHookOptions<DeleteSuggestionCategoryMutation, DeleteSuggestionCategoryMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteSuggestionCategoryMutation, DeleteSuggestionCategoryMutationVariables>(DeleteSuggestionCategoryDocument, options);
      }
export type DeleteSuggestionCategoryMutationHookResult = ReturnType<typeof useDeleteSuggestionCategoryMutation>;
export type DeleteSuggestionCategoryMutationResult = Apollo.MutationResult<DeleteSuggestionCategoryMutation>;
export type DeleteSuggestionCategoryMutationOptions = Apollo.BaseMutationOptions<DeleteSuggestionCategoryMutation, DeleteSuggestionCategoryMutationVariables>;
export const SuggestionCategoryDocumentTypeFieldsDocument = gql`
    query SuggestionCategoryDocumentTypeFields($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  suggestionCategory(id: $parentId) {
    id
    docTypeField {
      name
      subFields(
        searchText: $searchText
        notEqual: $unassociated
        first: 20
        after: $cursor
      ) {
        edges {
          node {
            id
            name
            description
            docType {
              id
            }
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
}
    `;

/**
 * __useSuggestionCategoryDocumentTypeFieldsQuery__
 *
 * To run a query within a React component, call `useSuggestionCategoryDocumentTypeFieldsQuery` and pass it any options that fit your needs.
 * When your component renders, `useSuggestionCategoryDocumentTypeFieldsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSuggestionCategoryDocumentTypeFieldsQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      unassociated: // value for 'unassociated'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useSuggestionCategoryDocumentTypeFieldsQuery(baseOptions: Apollo.QueryHookOptions<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>(SuggestionCategoryDocumentTypeFieldsDocument, options);
      }
export function useSuggestionCategoryDocumentTypeFieldsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>(SuggestionCategoryDocumentTypeFieldsDocument, options);
        }
export type SuggestionCategoryDocumentTypeFieldsQueryHookResult = ReturnType<typeof useSuggestionCategoryDocumentTypeFieldsQuery>;
export type SuggestionCategoryDocumentTypeFieldsLazyQueryHookResult = ReturnType<typeof useSuggestionCategoryDocumentTypeFieldsLazyQuery>;
export type SuggestionCategoryDocumentTypeFieldsQueryResult = Apollo.QueryResult<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>;
export const AddDocumentTypeFieldToSuggestionCategoryDocument = gql`
    mutation AddDocumentTypeFieldToSuggestionCategory($childId: ID!, $parentId: ID!) {
  addDocTypeFieldToSuggestionCategory(
    docTypeFieldId: $childId
    suggestionCategoryId: $parentId
  ) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddDocumentTypeFieldToSuggestionCategoryMutationFn = Apollo.MutationFunction<AddDocumentTypeFieldToSuggestionCategoryMutation, AddDocumentTypeFieldToSuggestionCategoryMutationVariables>;

/**
 * __useAddDocumentTypeFieldToSuggestionCategoryMutation__
 *
 * To run a mutation, you first call `useAddDocumentTypeFieldToSuggestionCategoryMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddDocumentTypeFieldToSuggestionCategoryMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addDocumentTypeFieldToSuggestionCategoryMutation, { data, loading, error }] = useAddDocumentTypeFieldToSuggestionCategoryMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddDocumentTypeFieldToSuggestionCategoryMutation(baseOptions?: Apollo.MutationHookOptions<AddDocumentTypeFieldToSuggestionCategoryMutation, AddDocumentTypeFieldToSuggestionCategoryMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddDocumentTypeFieldToSuggestionCategoryMutation, AddDocumentTypeFieldToSuggestionCategoryMutationVariables>(AddDocumentTypeFieldToSuggestionCategoryDocument, options);
      }
export type AddDocumentTypeFieldToSuggestionCategoryMutationHookResult = ReturnType<typeof useAddDocumentTypeFieldToSuggestionCategoryMutation>;
export type AddDocumentTypeFieldToSuggestionCategoryMutationResult = Apollo.MutationResult<AddDocumentTypeFieldToSuggestionCategoryMutation>;
export type AddDocumentTypeFieldToSuggestionCategoryMutationOptions = Apollo.BaseMutationOptions<AddDocumentTypeFieldToSuggestionCategoryMutation, AddDocumentTypeFieldToSuggestionCategoryMutationVariables>;
export const SuggestionCategoryDocument = gql`
    query SuggestionCategory($id: ID!) {
  suggestionCategory(id: $id) {
    id
    name
    description
    priority
    multiSelect
    docTypeField {
      id
      name
    }
    translations {
      key
      language
      value
      description
    }
  }
}
    `;

/**
 * __useSuggestionCategoryQuery__
 *
 * To run a query within a React component, call `useSuggestionCategoryQuery` and pass it any options that fit your needs.
 * When your component renders, `useSuggestionCategoryQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSuggestionCategoryQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useSuggestionCategoryQuery(baseOptions: Apollo.QueryHookOptions<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>(SuggestionCategoryDocument, options);
      }
export function useSuggestionCategoryLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>(SuggestionCategoryDocument, options);
        }
export type SuggestionCategoryQueryHookResult = ReturnType<typeof useSuggestionCategoryQuery>;
export type SuggestionCategoryLazyQueryHookResult = ReturnType<typeof useSuggestionCategoryLazyQuery>;
export type SuggestionCategoryQueryResult = Apollo.QueryResult<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>;
export const UnboundDocTypeFieldsBySuggestionCategoryDocument = gql`
    query UnboundDocTypeFieldsBySuggestionCategory($suggestionCategoryId: BigInteger!) {
  unboundDocTypeFieldsBySuggestionCategory(
    suggestionCategoryId: $suggestionCategoryId
  ) {
    id
    name
  }
}
    `;

/**
 * __useUnboundDocTypeFieldsBySuggestionCategoryQuery__
 *
 * To run a query within a React component, call `useUnboundDocTypeFieldsBySuggestionCategoryQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundDocTypeFieldsBySuggestionCategoryQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundDocTypeFieldsBySuggestionCategoryQuery({
 *   variables: {
 *      suggestionCategoryId: // value for 'suggestionCategoryId'
 *   },
 * });
 */
export function useUnboundDocTypeFieldsBySuggestionCategoryQuery(baseOptions: Apollo.QueryHookOptions<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>(UnboundDocTypeFieldsBySuggestionCategoryDocument, options);
      }
export function useUnboundDocTypeFieldsBySuggestionCategoryLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>(UnboundDocTypeFieldsBySuggestionCategoryDocument, options);
        }
export type UnboundDocTypeFieldsBySuggestionCategoryQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldsBySuggestionCategoryQuery>;
export type UnboundDocTypeFieldsBySuggestionCategoryLazyQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldsBySuggestionCategoryLazyQuery>;
export type UnboundDocTypeFieldsBySuggestionCategoryQueryResult = Apollo.QueryResult<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>;
export const DocTypeFieldsDocument = gql`
    query DocTypeFields($after: String) {
  docTypeFields(first: 20, after: $after) {
    edges {
      node {
        id
        name
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useDocTypeFieldsQuery__
 *
 * To run a query within a React component, call `useDocTypeFieldsQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocTypeFieldsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocTypeFieldsQuery({
 *   variables: {
 *      after: // value for 'after'
 *   },
 * });
 */
export function useDocTypeFieldsQuery(baseOptions?: Apollo.QueryHookOptions<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>(DocTypeFieldsDocument, options);
      }
export function useDocTypeFieldsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>(DocTypeFieldsDocument, options);
        }
export type DocTypeFieldsQueryHookResult = ReturnType<typeof useDocTypeFieldsQuery>;
export type DocTypeFieldsLazyQueryHookResult = ReturnType<typeof useDocTypeFieldsLazyQuery>;
export type DocTypeFieldsQueryResult = Apollo.QueryResult<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>;
export const CreateOrUpdateSuggestionCategoryDocument = gql`
    mutation CreateOrUpdateSuggestionCategory($id: ID, $name: String!, $description: String, $priority: Float!, $multiSelect: Boolean!, $docTypeFieldId: BigInteger) {
  suggestionCategoryWithDocTypeField(
    id: $id
    suggestionCategoryWithDocTypeFieldDTO: {name: $name, description: $description, priority: $priority, multiSelect: $multiSelect, docTypeFieldId: $docTypeFieldId}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateSuggestionCategoryMutationFn = Apollo.MutationFunction<CreateOrUpdateSuggestionCategoryMutation, CreateOrUpdateSuggestionCategoryMutationVariables>;

/**
 * __useCreateOrUpdateSuggestionCategoryMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateSuggestionCategoryMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateSuggestionCategoryMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateSuggestionCategoryMutation, { data, loading, error }] = useCreateOrUpdateSuggestionCategoryMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      priority: // value for 'priority'
 *      multiSelect: // value for 'multiSelect'
 *      docTypeFieldId: // value for 'docTypeFieldId'
 *   },
 * });
 */
export function useCreateOrUpdateSuggestionCategoryMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateSuggestionCategoryMutation, CreateOrUpdateSuggestionCategoryMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateSuggestionCategoryMutation, CreateOrUpdateSuggestionCategoryMutationVariables>(CreateOrUpdateSuggestionCategoryDocument, options);
      }
export type CreateOrUpdateSuggestionCategoryMutationHookResult = ReturnType<typeof useCreateOrUpdateSuggestionCategoryMutation>;
export type CreateOrUpdateSuggestionCategoryMutationResult = Apollo.MutationResult<CreateOrUpdateSuggestionCategoryMutation>;
export type CreateOrUpdateSuggestionCategoryMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateSuggestionCategoryMutation, CreateOrUpdateSuggestionCategoryMutationVariables>;
export const TabsDocument = gql`
    query Tabs($searchText: String, $after: String) {
  tabs(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        priority
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useTabsQuery__
 *
 * To run a query within a React component, call `useTabsQuery` and pass it any options that fit your needs.
 * When your component renders, `useTabsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTabsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useTabsQuery(baseOptions?: Apollo.QueryHookOptions<TabsQuery, TabsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TabsQuery, TabsQueryVariables>(TabsDocument, options);
      }
export function useTabsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TabsQuery, TabsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TabsQuery, TabsQueryVariables>(TabsDocument, options);
        }
export type TabsQueryHookResult = ReturnType<typeof useTabsQuery>;
export type TabsLazyQueryHookResult = ReturnType<typeof useTabsLazyQuery>;
export type TabsQueryResult = Apollo.QueryResult<TabsQuery, TabsQueryVariables>;
export const DeleteTabsDocument = gql`
    mutation DeleteTabs($id: ID!) {
  deleteTab(tabId: $id) {
    id
    name
  }
}
    `;
export type DeleteTabsMutationFn = Apollo.MutationFunction<DeleteTabsMutation, DeleteTabsMutationVariables>;

/**
 * __useDeleteTabsMutation__
 *
 * To run a mutation, you first call `useDeleteTabsMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteTabsMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteTabsMutation, { data, loading, error }] = useDeleteTabsMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteTabsMutation(baseOptions?: Apollo.MutationHookOptions<DeleteTabsMutation, DeleteTabsMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteTabsMutation, DeleteTabsMutationVariables>(DeleteTabsDocument, options);
      }
export type DeleteTabsMutationHookResult = ReturnType<typeof useDeleteTabsMutation>;
export type DeleteTabsMutationResult = Apollo.MutationResult<DeleteTabsMutation>;
export type DeleteTabsMutationOptions = Apollo.BaseMutationOptions<DeleteTabsMutation, DeleteTabsMutationVariables>;
export const UnboundBucketsByTabDocument = gql`
    query UnboundBucketsByTab($id: BigInteger!) {
  unboundBucketsByTab(tabId: $id) {
    name
    id
  }
}
    `;

/**
 * __useUnboundBucketsByTabQuery__
 *
 * To run a query within a React component, call `useUnboundBucketsByTabQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundBucketsByTabQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundBucketsByTabQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useUnboundBucketsByTabQuery(baseOptions: Apollo.QueryHookOptions<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>(UnboundBucketsByTabDocument, options);
      }
export function useUnboundBucketsByTabLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>(UnboundBucketsByTabDocument, options);
        }
export type UnboundBucketsByTabQueryHookResult = ReturnType<typeof useUnboundBucketsByTabQuery>;
export type UnboundBucketsByTabLazyQueryHookResult = ReturnType<typeof useUnboundBucketsByTabLazyQuery>;
export type UnboundBucketsByTabQueryResult = Apollo.QueryResult<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>;
export const TabDocument = gql`
    query Tab($id: ID!, $unasociated: Boolean) {
  tab(id: $id) {
    id
    name
    description
    priority
    tokenTabs(notEqual: $unasociated) {
      edges {
        node {
          name
          id
        }
      }
    }
    translations {
      key
      language
      value
      description
    }
  }
}
    `;

/**
 * __useTabQuery__
 *
 * To run a query within a React component, call `useTabQuery` and pass it any options that fit your needs.
 * When your component renders, `useTabQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTabQuery({
 *   variables: {
 *      id: // value for 'id'
 *      unasociated: // value for 'unasociated'
 *   },
 * });
 */
export function useTabQuery(baseOptions: Apollo.QueryHookOptions<TabQuery, TabQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TabQuery, TabQueryVariables>(TabDocument, options);
      }
export function useTabLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TabQuery, TabQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TabQuery, TabQueryVariables>(TabDocument, options);
        }
export type TabQueryHookResult = ReturnType<typeof useTabQuery>;
export type TabLazyQueryHookResult = ReturnType<typeof useTabLazyQuery>;
export type TabQueryResult = Apollo.QueryResult<TabQuery, TabQueryVariables>;
export const CreateOrUpdateTabDocument = gql`
    mutation CreateOrUpdateTab($id: ID, $name: String!, $description: String, $priority: Int!, $tokenTabIds: [BigInteger]) {
  tabWithTokenTabs(
    id: $id
    tabWithTokenTabsDTO: {name: $name, description: $description, priority: $priority, tokenTabIds: $tokenTabIds}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateTabMutationFn = Apollo.MutationFunction<CreateOrUpdateTabMutation, CreateOrUpdateTabMutationVariables>;

/**
 * __useCreateOrUpdateTabMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateTabMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateTabMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateTabMutation, { data, loading, error }] = useCreateOrUpdateTabMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      priority: // value for 'priority'
 *      tokenTabIds: // value for 'tokenTabIds'
 *   },
 * });
 */
export function useCreateOrUpdateTabMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateTabMutation, CreateOrUpdateTabMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateTabMutation, CreateOrUpdateTabMutationVariables>(CreateOrUpdateTabDocument, options);
      }
export type CreateOrUpdateTabMutationHookResult = ReturnType<typeof useCreateOrUpdateTabMutation>;
export type CreateOrUpdateTabMutationResult = Apollo.MutationResult<CreateOrUpdateTabMutation>;
export type CreateOrUpdateTabMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateTabMutation, CreateOrUpdateTabMutationVariables>;
export const TabTokenTabsDocument = gql`
    query TabTokenTabs($parentId: ID!, $searchText: String, $cursor: String, $unassociated: Boolean!) {
  tab(id: $parentId) {
    id
    tokenTabs(
      searchText: $searchText
      notEqual: $unassociated
      first: 20
      after: $cursor
    ) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
}
    `;

/**
 * __useTabTokenTabsQuery__
 *
 * To run a query within a React component, call `useTabTokenTabsQuery` and pass it any options that fit your needs.
 * When your component renders, `useTabTokenTabsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTabTokenTabsQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *      unassociated: // value for 'unassociated'
 *   },
 * });
 */
export function useTabTokenTabsQuery(baseOptions: Apollo.QueryHookOptions<TabTokenTabsQuery, TabTokenTabsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TabTokenTabsQuery, TabTokenTabsQueryVariables>(TabTokenTabsDocument, options);
      }
export function useTabTokenTabsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TabTokenTabsQuery, TabTokenTabsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TabTokenTabsQuery, TabTokenTabsQueryVariables>(TabTokenTabsDocument, options);
        }
export type TabTokenTabsQueryHookResult = ReturnType<typeof useTabTokenTabsQuery>;
export type TabTokenTabsLazyQueryHookResult = ReturnType<typeof useTabTokenTabsLazyQuery>;
export type TabTokenTabsQueryResult = Apollo.QueryResult<TabTokenTabsQuery, TabTokenTabsQueryVariables>;
export const AddTokenTabToTabDocument = gql`
    mutation AddTokenTabToTab($childId: ID!, $parentId: ID!) {
  addTokenTabToTab(id: $parentId, tokenTabId: $childId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddTokenTabToTabMutationFn = Apollo.MutationFunction<AddTokenTabToTabMutation, AddTokenTabToTabMutationVariables>;

/**
 * __useAddTokenTabToTabMutation__
 *
 * To run a mutation, you first call `useAddTokenTabToTabMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddTokenTabToTabMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addTokenTabToTabMutation, { data, loading, error }] = useAddTokenTabToTabMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddTokenTabToTabMutation(baseOptions?: Apollo.MutationHookOptions<AddTokenTabToTabMutation, AddTokenTabToTabMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddTokenTabToTabMutation, AddTokenTabToTabMutationVariables>(AddTokenTabToTabDocument, options);
      }
export type AddTokenTabToTabMutationHookResult = ReturnType<typeof useAddTokenTabToTabMutation>;
export type AddTokenTabToTabMutationResult = Apollo.MutationResult<AddTokenTabToTabMutation>;
export type AddTokenTabToTabMutationOptions = Apollo.BaseMutationOptions<AddTokenTabToTabMutation, AddTokenTabToTabMutationVariables>;
export const RemoveTokenTabToTabDocument = gql`
    mutation RemoveTokenTabToTab($childId: ID!, $parentId: ID!) {
  removeTokenTabToTab(id: $parentId, tokenTabId: $childId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveTokenTabToTabMutationFn = Apollo.MutationFunction<RemoveTokenTabToTabMutation, RemoveTokenTabToTabMutationVariables>;

/**
 * __useRemoveTokenTabToTabMutation__
 *
 * To run a mutation, you first call `useRemoveTokenTabToTabMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveTokenTabToTabMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeTokenTabToTabMutation, { data, loading, error }] = useRemoveTokenTabToTabMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveTokenTabToTabMutation(baseOptions?: Apollo.MutationHookOptions<RemoveTokenTabToTabMutation, RemoveTokenTabToTabMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveTokenTabToTabMutation, RemoveTokenTabToTabMutationVariables>(RemoveTokenTabToTabDocument, options);
      }
export type RemoveTokenTabToTabMutationHookResult = ReturnType<typeof useRemoveTokenTabToTabMutation>;
export type RemoveTokenTabToTabMutationResult = Apollo.MutationResult<RemoveTokenTabToTabMutation>;
export type RemoveTokenTabToTabMutationOptions = Apollo.BaseMutationOptions<RemoveTokenTabToTabMutation, RemoveTokenTabToTabMutationVariables>;
export const TabTokensDocument = gql`
    query TabTokens($searchText: String, $cursor: String) {
  totalTokenTabs(searchText: $searchText, first: 20, after: $cursor) {
    edges {
      node {
        id
        name
        tokenType
        value
        filter
        extraParams
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useTabTokensQuery__
 *
 * To run a query within a React component, call `useTabTokensQuery` and pass it any options that fit your needs.
 * When your component renders, `useTabTokensQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTabTokensQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useTabTokensQuery(baseOptions?: Apollo.QueryHookOptions<TabTokensQuery, TabTokensQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TabTokensQuery, TabTokensQueryVariables>(TabTokensDocument, options);
      }
export function useTabTokensLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TabTokensQuery, TabTokensQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TabTokensQuery, TabTokensQueryVariables>(TabTokensDocument, options);
        }
export type TabTokensQueryHookResult = ReturnType<typeof useTabTokensQuery>;
export type TabTokensLazyQueryHookResult = ReturnType<typeof useTabTokensLazyQuery>;
export type TabTokensQueryResult = Apollo.QueryResult<TabTokensQuery, TabTokensQueryVariables>;
export const TokenFiltersDocument = gql`
    query TokenFilters($searchText: String, $after: String) {
  tokenFilters(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useTokenFiltersQuery__
 *
 * To run a query within a React component, call `useTokenFiltersQuery` and pass it any options that fit your needs.
 * When your component renders, `useTokenFiltersQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTokenFiltersQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useTokenFiltersQuery(baseOptions?: Apollo.QueryHookOptions<TokenFiltersQuery, TokenFiltersQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TokenFiltersQuery, TokenFiltersQueryVariables>(TokenFiltersDocument, options);
      }
export function useTokenFiltersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TokenFiltersQuery, TokenFiltersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TokenFiltersQuery, TokenFiltersQueryVariables>(TokenFiltersDocument, options);
        }
export type TokenFiltersQueryHookResult = ReturnType<typeof useTokenFiltersQuery>;
export type TokenFiltersLazyQueryHookResult = ReturnType<typeof useTokenFiltersLazyQuery>;
export type TokenFiltersQueryResult = Apollo.QueryResult<TokenFiltersQuery, TokenFiltersQueryVariables>;
export const DeleteTokenFiltersDocument = gql`
    mutation DeleteTokenFilters($id: ID!) {
  deleteTokenFilter(tokenFilterId: $id) {
    id
    name
  }
}
    `;
export type DeleteTokenFiltersMutationFn = Apollo.MutationFunction<DeleteTokenFiltersMutation, DeleteTokenFiltersMutationVariables>;

/**
 * __useDeleteTokenFiltersMutation__
 *
 * To run a mutation, you first call `useDeleteTokenFiltersMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteTokenFiltersMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteTokenFiltersMutation, { data, loading, error }] = useDeleteTokenFiltersMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteTokenFiltersMutation(baseOptions?: Apollo.MutationHookOptions<DeleteTokenFiltersMutation, DeleteTokenFiltersMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteTokenFiltersMutation, DeleteTokenFiltersMutationVariables>(DeleteTokenFiltersDocument, options);
      }
export type DeleteTokenFiltersMutationHookResult = ReturnType<typeof useDeleteTokenFiltersMutation>;
export type DeleteTokenFiltersMutationResult = Apollo.MutationResult<DeleteTokenFiltersMutation>;
export type DeleteTokenFiltersMutationOptions = Apollo.BaseMutationOptions<DeleteTokenFiltersMutation, DeleteTokenFiltersMutationVariables>;
export const AddTokenFilterToAnalyzerDocument = gql`
    mutation AddTokenFilterToAnalyzer($childId: ID!, $parentId: ID!) {
  addTokenFilterToAnalyzer(tokenFilterId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddTokenFilterToAnalyzerMutationFn = Apollo.MutationFunction<AddTokenFilterToAnalyzerMutation, AddTokenFilterToAnalyzerMutationVariables>;

/**
 * __useAddTokenFilterToAnalyzerMutation__
 *
 * To run a mutation, you first call `useAddTokenFilterToAnalyzerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddTokenFilterToAnalyzerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addTokenFilterToAnalyzerMutation, { data, loading, error }] = useAddTokenFilterToAnalyzerMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddTokenFilterToAnalyzerMutation(baseOptions?: Apollo.MutationHookOptions<AddTokenFilterToAnalyzerMutation, AddTokenFilterToAnalyzerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddTokenFilterToAnalyzerMutation, AddTokenFilterToAnalyzerMutationVariables>(AddTokenFilterToAnalyzerDocument, options);
      }
export type AddTokenFilterToAnalyzerMutationHookResult = ReturnType<typeof useAddTokenFilterToAnalyzerMutation>;
export type AddTokenFilterToAnalyzerMutationResult = Apollo.MutationResult<AddTokenFilterToAnalyzerMutation>;
export type AddTokenFilterToAnalyzerMutationOptions = Apollo.BaseMutationOptions<AddTokenFilterToAnalyzerMutation, AddTokenFilterToAnalyzerMutationVariables>;
export const UnboundAnalyzersByTokenFilterDocument = gql`
    query UnboundAnalyzersByTokenFilter($tokenFilterId: BigInteger!) {
  unboundAnalyzersByTokenFilter(tokenFilterId: $tokenFilterId) {
    name
    id
  }
}
    `;

/**
 * __useUnboundAnalyzersByTokenFilterQuery__
 *
 * To run a query within a React component, call `useUnboundAnalyzersByTokenFilterQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundAnalyzersByTokenFilterQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundAnalyzersByTokenFilterQuery({
 *   variables: {
 *      tokenFilterId: // value for 'tokenFilterId'
 *   },
 * });
 */
export function useUnboundAnalyzersByTokenFilterQuery(baseOptions: Apollo.QueryHookOptions<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>(UnboundAnalyzersByTokenFilterDocument, options);
      }
export function useUnboundAnalyzersByTokenFilterLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>(UnboundAnalyzersByTokenFilterDocument, options);
        }
export type UnboundAnalyzersByTokenFilterQueryHookResult = ReturnType<typeof useUnboundAnalyzersByTokenFilterQuery>;
export type UnboundAnalyzersByTokenFilterLazyQueryHookResult = ReturnType<typeof useUnboundAnalyzersByTokenFilterLazyQuery>;
export type UnboundAnalyzersByTokenFilterQueryResult = Apollo.QueryResult<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>;
export const TokenFilterDocument = gql`
    query TokenFilter($id: ID!) {
  tokenFilter(id: $id) {
    id
    name
    description
    jsonConfig
    type
  }
}
    `;

/**
 * __useTokenFilterQuery__
 *
 * To run a query within a React component, call `useTokenFilterQuery` and pass it any options that fit your needs.
 * When your component renders, `useTokenFilterQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTokenFilterQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useTokenFilterQuery(baseOptions: Apollo.QueryHookOptions<TokenFilterQuery, TokenFilterQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TokenFilterQuery, TokenFilterQueryVariables>(TokenFilterDocument, options);
      }
export function useTokenFilterLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TokenFilterQuery, TokenFilterQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TokenFilterQuery, TokenFilterQueryVariables>(TokenFilterDocument, options);
        }
export type TokenFilterQueryHookResult = ReturnType<typeof useTokenFilterQuery>;
export type TokenFilterLazyQueryHookResult = ReturnType<typeof useTokenFilterLazyQuery>;
export type TokenFilterQueryResult = Apollo.QueryResult<TokenFilterQuery, TokenFilterQueryVariables>;
export const CreateOrUpdateTokenFilterDocument = gql`
    mutation CreateOrUpdateTokenFilter($id: ID, $name: String!, $description: String, $jsonConfig: String, $type: String!) {
  tokenFilter(
    id: $id
    tokenFilterDTO: {name: $name, description: $description, jsonConfig: $jsonConfig, type: $type}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateTokenFilterMutationFn = Apollo.MutationFunction<CreateOrUpdateTokenFilterMutation, CreateOrUpdateTokenFilterMutationVariables>;

/**
 * __useCreateOrUpdateTokenFilterMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateTokenFilterMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateTokenFilterMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateTokenFilterMutation, { data, loading, error }] = useCreateOrUpdateTokenFilterMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      jsonConfig: // value for 'jsonConfig'
 *      type: // value for 'type'
 *   },
 * });
 */
export function useCreateOrUpdateTokenFilterMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateTokenFilterMutation, CreateOrUpdateTokenFilterMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateTokenFilterMutation, CreateOrUpdateTokenFilterMutationVariables>(CreateOrUpdateTokenFilterDocument, options);
      }
export type CreateOrUpdateTokenFilterMutationHookResult = ReturnType<typeof useCreateOrUpdateTokenFilterMutation>;
export type CreateOrUpdateTokenFilterMutationResult = Apollo.MutationResult<CreateOrUpdateTokenFilterMutation>;
export type CreateOrUpdateTokenFilterMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateTokenFilterMutation, CreateOrUpdateTokenFilterMutationVariables>;
export const TabTokenTabDocument = gql`
    query TabTokenTab($id: ID!) {
  tokenTab(id: $id) {
    id
    name
    description
    value
    filter
    tokenType
    docTypeField {
      id
      name
    }
    extraParams
  }
}
    `;

/**
 * __useTabTokenTabQuery__
 *
 * To run a query within a React component, call `useTabTokenTabQuery` and pass it any options that fit your needs.
 * When your component renders, `useTabTokenTabQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTabTokenTabQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useTabTokenTabQuery(baseOptions: Apollo.QueryHookOptions<TabTokenTabQuery, TabTokenTabQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TabTokenTabQuery, TabTokenTabQueryVariables>(TabTokenTabDocument, options);
      }
export function useTabTokenTabLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TabTokenTabQuery, TabTokenTabQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TabTokenTabQuery, TabTokenTabQueryVariables>(TabTokenTabDocument, options);
        }
export type TabTokenTabQueryHookResult = ReturnType<typeof useTabTokenTabQuery>;
export type TabTokenTabLazyQueryHookResult = ReturnType<typeof useTabTokenTabLazyQuery>;
export type TabTokenTabQueryResult = Apollo.QueryResult<TabTokenTabQuery, TabTokenTabQueryVariables>;
export const CreateOrUpdateTabTokenDocument = gql`
    mutation CreateOrUpdateTabToken($tokenTabId: ID, $name: String!, $description: String, $value: String!, $filter: Boolean!, $tokenType: TokenType!, $docTypeFieldId: BigInteger, $extraParams: String) {
  tokenTabWithDocTypeField(
    id: $tokenTabId
    tokenTabWithDocTypeFieldDTO: {name: $name, description: $description, filter: $filter, tokenType: $tokenType, value: $value, extraParams: $extraParams, docTypeFieldId: $docTypeFieldId}
  ) {
    entity {
      id
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateTabTokenMutationFn = Apollo.MutationFunction<CreateOrUpdateTabTokenMutation, CreateOrUpdateTabTokenMutationVariables>;

/**
 * __useCreateOrUpdateTabTokenMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateTabTokenMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateTabTokenMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateTabTokenMutation, { data, loading, error }] = useCreateOrUpdateTabTokenMutation({
 *   variables: {
 *      tokenTabId: // value for 'tokenTabId'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      value: // value for 'value'
 *      filter: // value for 'filter'
 *      tokenType: // value for 'tokenType'
 *      docTypeFieldId: // value for 'docTypeFieldId'
 *      extraParams: // value for 'extraParams'
 *   },
 * });
 */
export function useCreateOrUpdateTabTokenMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateTabTokenMutation, CreateOrUpdateTabTokenMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateTabTokenMutation, CreateOrUpdateTabTokenMutationVariables>(CreateOrUpdateTabTokenDocument, options);
      }
export type CreateOrUpdateTabTokenMutationHookResult = ReturnType<typeof useCreateOrUpdateTabTokenMutation>;
export type CreateOrUpdateTabTokenMutationResult = Apollo.MutationResult<CreateOrUpdateTabTokenMutation>;
export type CreateOrUpdateTabTokenMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateTabTokenMutation, CreateOrUpdateTabTokenMutationVariables>;
export const DocTypeFieldValueDocument = gql`
    query DocTypeFieldValue($id: ID!) {
  value: docTypeField(id: $id) {
    id
    name
    description
  }
}
    `;

/**
 * __useDocTypeFieldValueQuery__
 *
 * To run a query within a React component, call `useDocTypeFieldValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocTypeFieldValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocTypeFieldValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDocTypeFieldValueQuery(baseOptions: Apollo.QueryHookOptions<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>(DocTypeFieldValueDocument, options);
      }
export function useDocTypeFieldValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>(DocTypeFieldValueDocument, options);
        }
export type DocTypeFieldValueQueryHookResult = ReturnType<typeof useDocTypeFieldValueQuery>;
export type DocTypeFieldValueLazyQueryHookResult = ReturnType<typeof useDocTypeFieldValueLazyQuery>;
export type DocTypeFieldValueQueryResult = Apollo.QueryResult<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>;
export const DocTypeFieldOptionsTokenTabDocument = gql`
    query DocTypeFieldOptionsTokenTab($searchText: String, $after: String) {
  options: docTypeFields(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useDocTypeFieldOptionsTokenTabQuery__
 *
 * To run a query within a React component, call `useDocTypeFieldOptionsTokenTabQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocTypeFieldOptionsTokenTabQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocTypeFieldOptionsTokenTabQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useDocTypeFieldOptionsTokenTabQuery(baseOptions?: Apollo.QueryHookOptions<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>(DocTypeFieldOptionsTokenTabDocument, options);
      }
export function useDocTypeFieldOptionsTokenTabLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>(DocTypeFieldOptionsTokenTabDocument, options);
        }
export type DocTypeFieldOptionsTokenTabQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsTokenTabQuery>;
export type DocTypeFieldOptionsTokenTabLazyQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsTokenTabLazyQuery>;
export type DocTypeFieldOptionsTokenTabQueryResult = Apollo.QueryResult<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>;
export const TabTokensQueryDocument = gql`
    query TabTokensQuery($searchText: String, $cursor: String) {
  totalTokenTabs(searchText: $searchText, first: 20, after: $cursor) {
    edges {
      node {
        id
        name
        tokenType
        value
        filter
        extraParams
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useTabTokensQueryQuery__
 *
 * To run a query within a React component, call `useTabTokensQueryQuery` and pass it any options that fit your needs.
 * When your component renders, `useTabTokensQueryQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTabTokensQueryQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useTabTokensQueryQuery(baseOptions?: Apollo.QueryHookOptions<TabTokensQueryQuery, TabTokensQueryQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TabTokensQueryQuery, TabTokensQueryQueryVariables>(TabTokensQueryDocument, options);
      }
export function useTabTokensQueryLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TabTokensQueryQuery, TabTokensQueryQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TabTokensQueryQuery, TabTokensQueryQueryVariables>(TabTokensQueryDocument, options);
        }
export type TabTokensQueryQueryHookResult = ReturnType<typeof useTabTokensQueryQuery>;
export type TabTokensQueryLazyQueryHookResult = ReturnType<typeof useTabTokensQueryLazyQuery>;
export type TabTokensQueryQueryResult = Apollo.QueryResult<TabTokensQueryQuery, TabTokensQueryQueryVariables>;
export const DeleteTabTokenDocument = gql`
    mutation DeleteTabToken($id: ID!) {
  deleteTokenTab(tokenTabId: $id) {
    id
    name
  }
}
    `;
export type DeleteTabTokenMutationFn = Apollo.MutationFunction<DeleteTabTokenMutation, DeleteTabTokenMutationVariables>;

/**
 * __useDeleteTabTokenMutation__
 *
 * To run a mutation, you first call `useDeleteTabTokenMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteTabTokenMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteTabTokenMutation, { data, loading, error }] = useDeleteTabTokenMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteTabTokenMutation(baseOptions?: Apollo.MutationHookOptions<DeleteTabTokenMutation, DeleteTabTokenMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteTabTokenMutation, DeleteTabTokenMutationVariables>(DeleteTabTokenDocument, options);
      }
export type DeleteTabTokenMutationHookResult = ReturnType<typeof useDeleteTabTokenMutation>;
export type DeleteTabTokenMutationResult = Apollo.MutationResult<DeleteTabTokenMutation>;
export type DeleteTabTokenMutationOptions = Apollo.BaseMutationOptions<DeleteTabTokenMutation, DeleteTabTokenMutationVariables>;
export const UnassociatedTokenTabsInTabDocument = gql`
    query unassociatedTokenTabsInTab($id: BigInteger!) {
  unboundTabsByTokenTab(tokenTabId: $id) {
    id
    name
  }
}
    `;

/**
 * __useUnassociatedTokenTabsInTabQuery__
 *
 * To run a query within a React component, call `useUnassociatedTokenTabsInTabQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnassociatedTokenTabsInTabQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnassociatedTokenTabsInTabQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useUnassociatedTokenTabsInTabQuery(baseOptions: Apollo.QueryHookOptions<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>(UnassociatedTokenTabsInTabDocument, options);
      }
export function useUnassociatedTokenTabsInTabLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>(UnassociatedTokenTabsInTabDocument, options);
        }
export type UnassociatedTokenTabsInTabQueryHookResult = ReturnType<typeof useUnassociatedTokenTabsInTabQuery>;
export type UnassociatedTokenTabsInTabLazyQueryHookResult = ReturnType<typeof useUnassociatedTokenTabsInTabLazyQuery>;
export type UnassociatedTokenTabsInTabQueryResult = Apollo.QueryResult<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>;
export const TokenizerDocument = gql`
    query Tokenizer($id: ID!) {
  tokenizer(id: $id) {
    id
    name
    description
    jsonConfig
    type
  }
}
    `;

/**
 * __useTokenizerQuery__
 *
 * To run a query within a React component, call `useTokenizerQuery` and pass it any options that fit your needs.
 * When your component renders, `useTokenizerQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTokenizerQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useTokenizerQuery(baseOptions: Apollo.QueryHookOptions<TokenizerQuery, TokenizerQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TokenizerQuery, TokenizerQueryVariables>(TokenizerDocument, options);
      }
export function useTokenizerLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TokenizerQuery, TokenizerQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TokenizerQuery, TokenizerQueryVariables>(TokenizerDocument, options);
        }
export type TokenizerQueryHookResult = ReturnType<typeof useTokenizerQuery>;
export type TokenizerLazyQueryHookResult = ReturnType<typeof useTokenizerLazyQuery>;
export type TokenizerQueryResult = Apollo.QueryResult<TokenizerQuery, TokenizerQueryVariables>;
export const CreateOrUpdateTokenizerDocument = gql`
    mutation CreateOrUpdateTokenizer($id: ID, $name: String!, $description: String, $jsonConfig: String, $type: String!) {
  tokenizer(
    id: $id
    tokenizerDTO: {name: $name, description: $description, jsonConfig: $jsonConfig, type: $type}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateTokenizerMutationFn = Apollo.MutationFunction<CreateOrUpdateTokenizerMutation, CreateOrUpdateTokenizerMutationVariables>;

/**
 * __useCreateOrUpdateTokenizerMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateTokenizerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateTokenizerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateTokenizerMutation, { data, loading, error }] = useCreateOrUpdateTokenizerMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      jsonConfig: // value for 'jsonConfig'
 *      type: // value for 'type'
 *   },
 * });
 */
export function useCreateOrUpdateTokenizerMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateTokenizerMutation, CreateOrUpdateTokenizerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateTokenizerMutation, CreateOrUpdateTokenizerMutationVariables>(CreateOrUpdateTokenizerDocument, options);
      }
export type CreateOrUpdateTokenizerMutationHookResult = ReturnType<typeof useCreateOrUpdateTokenizerMutation>;
export type CreateOrUpdateTokenizerMutationResult = Apollo.MutationResult<CreateOrUpdateTokenizerMutation>;
export type CreateOrUpdateTokenizerMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateTokenizerMutation, CreateOrUpdateTokenizerMutationVariables>;
export const TokenizersDocument = gql`
    query Tokenizers($searchText: String, $after: String) {
  tokenizers(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useTokenizersQuery__
 *
 * To run a query within a React component, call `useTokenizersQuery` and pass it any options that fit your needs.
 * When your component renders, `useTokenizersQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTokenizersQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useTokenizersQuery(baseOptions?: Apollo.QueryHookOptions<TokenizersQuery, TokenizersQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TokenizersQuery, TokenizersQueryVariables>(TokenizersDocument, options);
      }
export function useTokenizersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TokenizersQuery, TokenizersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TokenizersQuery, TokenizersQueryVariables>(TokenizersDocument, options);
        }
export type TokenizersQueryHookResult = ReturnType<typeof useTokenizersQuery>;
export type TokenizersLazyQueryHookResult = ReturnType<typeof useTokenizersLazyQuery>;
export type TokenizersQueryResult = Apollo.QueryResult<TokenizersQuery, TokenizersQueryVariables>;
export const DeleteTokenizerDocument = gql`
    mutation DeleteTokenizer($id: ID!) {
  deleteTokenizer(tokenizerId: $id) {
    id
    name
  }
}
    `;
export type DeleteTokenizerMutationFn = Apollo.MutationFunction<DeleteTokenizerMutation, DeleteTokenizerMutationVariables>;

/**
 * __useDeleteTokenizerMutation__
 *
 * To run a mutation, you first call `useDeleteTokenizerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteTokenizerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteTokenizerMutation, { data, loading, error }] = useDeleteTokenizerMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteTokenizerMutation(baseOptions?: Apollo.MutationHookOptions<DeleteTokenizerMutation, DeleteTokenizerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteTokenizerMutation, DeleteTokenizerMutationVariables>(DeleteTokenizerDocument, options);
      }
export type DeleteTokenizerMutationHookResult = ReturnType<typeof useDeleteTokenizerMutation>;
export type DeleteTokenizerMutationResult = Apollo.MutationResult<DeleteTokenizerMutation>;
export type DeleteTokenizerMutationOptions = Apollo.BaseMutationOptions<DeleteTokenizerMutation, DeleteTokenizerMutationVariables>;
export const AnnotatorsDocument = gql`
    query Annotators($searchText: String, $after: String) {
  annotators(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        size
        type
        fieldName
        fuziness
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useAnnotatorsQuery__
 *
 * To run a query within a React component, call `useAnnotatorsQuery` and pass it any options that fit your needs.
 * When your component renders, `useAnnotatorsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAnnotatorsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useAnnotatorsQuery(baseOptions?: Apollo.QueryHookOptions<AnnotatorsQuery, AnnotatorsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnnotatorsQuery, AnnotatorsQueryVariables>(AnnotatorsDocument, options);
      }
export function useAnnotatorsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnnotatorsQuery, AnnotatorsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnnotatorsQuery, AnnotatorsQueryVariables>(AnnotatorsDocument, options);
        }
export type AnnotatorsQueryHookResult = ReturnType<typeof useAnnotatorsQuery>;
export type AnnotatorsLazyQueryHookResult = ReturnType<typeof useAnnotatorsLazyQuery>;
export type AnnotatorsQueryResult = Apollo.QueryResult<AnnotatorsQuery, AnnotatorsQueryVariables>;
export const DeleteAnnotatosDocument = gql`
    mutation DeleteAnnotatos($id: ID!) {
  deleteAnnotator(annotatorId: $id) {
    id
    name
  }
}
    `;
export type DeleteAnnotatosMutationFn = Apollo.MutationFunction<DeleteAnnotatosMutation, DeleteAnnotatosMutationVariables>;

/**
 * __useDeleteAnnotatosMutation__
 *
 * To run a mutation, you first call `useDeleteAnnotatosMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteAnnotatosMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteAnnotatosMutation, { data, loading, error }] = useDeleteAnnotatosMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteAnnotatosMutation(baseOptions?: Apollo.MutationHookOptions<DeleteAnnotatosMutation, DeleteAnnotatosMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteAnnotatosMutation, DeleteAnnotatosMutationVariables>(DeleteAnnotatosDocument, options);
      }
export type DeleteAnnotatosMutationHookResult = ReturnType<typeof useDeleteAnnotatosMutation>;
export type DeleteAnnotatosMutationResult = Apollo.MutationResult<DeleteAnnotatosMutation>;
export type DeleteAnnotatosMutationOptions = Apollo.BaseMutationOptions<DeleteAnnotatosMutation, DeleteAnnotatosMutationVariables>;
export const DocTypeFieldOptionsAnnotatorsDocument = gql`
    query DocTypeFieldOptionsAnnotators($searchText: String, $cursor: String) {
  options: docTypeFields(searchText: $searchText, first: 20, after: $cursor) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useDocTypeFieldOptionsAnnotatorsQuery__
 *
 * To run a query within a React component, call `useDocTypeFieldOptionsAnnotatorsQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocTypeFieldOptionsAnnotatorsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocTypeFieldOptionsAnnotatorsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useDocTypeFieldOptionsAnnotatorsQuery(baseOptions?: Apollo.QueryHookOptions<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>(DocTypeFieldOptionsAnnotatorsDocument, options);
      }
export function useDocTypeFieldOptionsAnnotatorsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>(DocTypeFieldOptionsAnnotatorsDocument, options);
        }
export type DocTypeFieldOptionsAnnotatorsQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsAnnotatorsQuery>;
export type DocTypeFieldOptionsAnnotatorsLazyQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsAnnotatorsLazyQuery>;
export type DocTypeFieldOptionsAnnotatorsQueryResult = Apollo.QueryResult<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>;
export const AnnotatorDocument = gql`
    query Annotator($id: ID!) {
  annotator(id: $id) {
    id
    fuziness
    size
    type
    description
    name
    fieldName
    docTypeField {
      id
      name
    }
    extraParams
  }
}
    `;

/**
 * __useAnnotatorQuery__
 *
 * To run a query within a React component, call `useAnnotatorQuery` and pass it any options that fit your needs.
 * When your component renders, `useAnnotatorQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAnnotatorQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useAnnotatorQuery(baseOptions: Apollo.QueryHookOptions<AnnotatorQuery, AnnotatorQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnnotatorQuery, AnnotatorQueryVariables>(AnnotatorDocument, options);
      }
export function useAnnotatorLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnnotatorQuery, AnnotatorQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnnotatorQuery, AnnotatorQueryVariables>(AnnotatorDocument, options);
        }
export type AnnotatorQueryHookResult = ReturnType<typeof useAnnotatorQuery>;
export type AnnotatorLazyQueryHookResult = ReturnType<typeof useAnnotatorLazyQuery>;
export type AnnotatorQueryResult = Apollo.QueryResult<AnnotatorQuery, AnnotatorQueryVariables>;
export const CreateOrUpdateAnnotatorDocument = gql`
    mutation CreateOrUpdateAnnotator($id: ID, $fieldName: String!, $fuziness: Fuzziness!, $type: AnnotatorType!, $description: String, $size: Int, $name: String!, $docTypeFieldId: BigInteger, $extraParams: String) {
  annotatorWithDocTypeField(
    id: $id
    annotatorDTO: {fieldName: $fieldName, fuziness: $fuziness, size: $size, type: $type, description: $description, name: $name, docTypeFieldId: $docTypeFieldId, extraParams: $extraParams}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateAnnotatorMutationFn = Apollo.MutationFunction<CreateOrUpdateAnnotatorMutation, CreateOrUpdateAnnotatorMutationVariables>;

/**
 * __useCreateOrUpdateAnnotatorMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateAnnotatorMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateAnnotatorMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateAnnotatorMutation, { data, loading, error }] = useCreateOrUpdateAnnotatorMutation({
 *   variables: {
 *      id: // value for 'id'
 *      fieldName: // value for 'fieldName'
 *      fuziness: // value for 'fuziness'
 *      type: // value for 'type'
 *      description: // value for 'description'
 *      size: // value for 'size'
 *      name: // value for 'name'
 *      docTypeFieldId: // value for 'docTypeFieldId'
 *      extraParams: // value for 'extraParams'
 *   },
 * });
 */
export function useCreateOrUpdateAnnotatorMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateAnnotatorMutation, CreateOrUpdateAnnotatorMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateAnnotatorMutation, CreateOrUpdateAnnotatorMutationVariables>(CreateOrUpdateAnnotatorDocument, options);
      }
export type CreateOrUpdateAnnotatorMutationHookResult = ReturnType<typeof useCreateOrUpdateAnnotatorMutation>;
export type CreateOrUpdateAnnotatorMutationResult = Apollo.MutationResult<CreateOrUpdateAnnotatorMutation>;
export type CreateOrUpdateAnnotatorMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateAnnotatorMutation, CreateOrUpdateAnnotatorMutationVariables>;
export const DocTypeFieldOptionsDocument = gql`
    query DocTypeFieldOptions($searchText: String, $cursor: String, $annotatorId: ID!) {
  options: docTypeFieldNotInAnnotator(
    annotatorId: $annotatorId
    searchText: $searchText
    first: 5
    after: $cursor
  ) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useDocTypeFieldOptionsQuery__
 *
 * To run a query within a React component, call `useDocTypeFieldOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocTypeFieldOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocTypeFieldOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *      annotatorId: // value for 'annotatorId'
 *   },
 * });
 */
export function useDocTypeFieldOptionsQuery(baseOptions: Apollo.QueryHookOptions<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>(DocTypeFieldOptionsDocument, options);
      }
export function useDocTypeFieldOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>(DocTypeFieldOptionsDocument, options);
        }
export type DocTypeFieldOptionsQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsQuery>;
export type DocTypeFieldOptionsLazyQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsLazyQuery>;
export type DocTypeFieldOptionsQueryResult = Apollo.QueryResult<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>;
export const BindDocTypeFieldToDataSourceDocument = gql`
    mutation BindDocTypeFieldToDataSource($documentTypeFieldId: ID!, $annotatorId: ID!) {
  bindAnnotatorToDocTypeField(
    docTypeFieldId: $documentTypeFieldId
    id: $annotatorId
  ) {
    left {
      id
      docTypeField {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindDocTypeFieldToDataSourceMutationFn = Apollo.MutationFunction<BindDocTypeFieldToDataSourceMutation, BindDocTypeFieldToDataSourceMutationVariables>;

/**
 * __useBindDocTypeFieldToDataSourceMutation__
 *
 * To run a mutation, you first call `useBindDocTypeFieldToDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindDocTypeFieldToDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindDocTypeFieldToDataSourceMutation, { data, loading, error }] = useBindDocTypeFieldToDataSourceMutation({
 *   variables: {
 *      documentTypeFieldId: // value for 'documentTypeFieldId'
 *      annotatorId: // value for 'annotatorId'
 *   },
 * });
 */
export function useBindDocTypeFieldToDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<BindDocTypeFieldToDataSourceMutation, BindDocTypeFieldToDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindDocTypeFieldToDataSourceMutation, BindDocTypeFieldToDataSourceMutationVariables>(BindDocTypeFieldToDataSourceDocument, options);
      }
export type BindDocTypeFieldToDataSourceMutationHookResult = ReturnType<typeof useBindDocTypeFieldToDataSourceMutation>;
export type BindDocTypeFieldToDataSourceMutationResult = Apollo.MutationResult<BindDocTypeFieldToDataSourceMutation>;
export type BindDocTypeFieldToDataSourceMutationOptions = Apollo.BaseMutationOptions<BindDocTypeFieldToDataSourceMutation, BindDocTypeFieldToDataSourceMutationVariables>;
export const UnbindDocTypeFieldToDataSourceDocument = gql`
    mutation UnbindDocTypeFieldToDataSource($documentTypeFieldId: ID!, $annotatorId: ID!) {
  unbindAnnotatorFromDocTypeField(
    docTypeFieldId: $documentTypeFieldId
    id: $annotatorId
  ) {
    left {
      id
    }
  }
}
    `;
export type UnbindDocTypeFieldToDataSourceMutationFn = Apollo.MutationFunction<UnbindDocTypeFieldToDataSourceMutation, UnbindDocTypeFieldToDataSourceMutationVariables>;

/**
 * __useUnbindDocTypeFieldToDataSourceMutation__
 *
 * To run a mutation, you first call `useUnbindDocTypeFieldToDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindDocTypeFieldToDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindDocTypeFieldToDataSourceMutation, { data, loading, error }] = useUnbindDocTypeFieldToDataSourceMutation({
 *   variables: {
 *      documentTypeFieldId: // value for 'documentTypeFieldId'
 *      annotatorId: // value for 'annotatorId'
 *   },
 * });
 */
export function useUnbindDocTypeFieldToDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<UnbindDocTypeFieldToDataSourceMutation, UnbindDocTypeFieldToDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindDocTypeFieldToDataSourceMutation, UnbindDocTypeFieldToDataSourceMutationVariables>(UnbindDocTypeFieldToDataSourceDocument, options);
      }
export type UnbindDocTypeFieldToDataSourceMutationHookResult = ReturnType<typeof useUnbindDocTypeFieldToDataSourceMutation>;
export type UnbindDocTypeFieldToDataSourceMutationResult = Apollo.MutationResult<UnbindDocTypeFieldToDataSourceMutation>;
export type UnbindDocTypeFieldToDataSourceMutationOptions = Apollo.BaseMutationOptions<UnbindDocTypeFieldToDataSourceMutation, UnbindDocTypeFieldToDataSourceMutationVariables>;
export const BucketsDocument = gql`
    query Buckets($searchText: String, $after: String) {
  buckets(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        enabled
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useBucketsQuery__
 *
 * To run a query within a React component, call `useBucketsQuery` and pass it any options that fit your needs.
 * When your component renders, `useBucketsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useBucketsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useBucketsQuery(baseOptions?: Apollo.QueryHookOptions<BucketsQuery, BucketsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketsQuery, BucketsQueryVariables>(BucketsDocument, options);
      }
export function useBucketsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketsQuery, BucketsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketsQuery, BucketsQueryVariables>(BucketsDocument, options);
        }
export type BucketsQueryHookResult = ReturnType<typeof useBucketsQuery>;
export type BucketsLazyQueryHookResult = ReturnType<typeof useBucketsLazyQuery>;
export type BucketsQueryResult = Apollo.QueryResult<BucketsQuery, BucketsQueryVariables>;
export const QueryAnalysisOptionsDocument = gql`
    query QueryAnalysisOptions($searchText: String, $cursor: String) {
  options: queryAnalyses(searchText: $searchText, first: 5, after: $cursor) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useQueryAnalysisOptionsQuery__
 *
 * To run a query within a React component, call `useQueryAnalysisOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useQueryAnalysisOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQueryAnalysisOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useQueryAnalysisOptionsQuery(baseOptions?: Apollo.QueryHookOptions<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>(QueryAnalysisOptionsDocument, options);
      }
export function useQueryAnalysisOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>(QueryAnalysisOptionsDocument, options);
        }
export type QueryAnalysisOptionsQueryHookResult = ReturnType<typeof useQueryAnalysisOptionsQuery>;
export type QueryAnalysisOptionsLazyQueryHookResult = ReturnType<typeof useQueryAnalysisOptionsLazyQuery>;
export type QueryAnalysisOptionsQueryResult = Apollo.QueryResult<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>;
export const QueryAnalysisValueDocument = gql`
    query QueryAnalysisValue($id: ID!) {
  value: queryAnalysis(id: $id) {
    id
    name
    description
  }
}
    `;

/**
 * __useQueryAnalysisValueQuery__
 *
 * To run a query within a React component, call `useQueryAnalysisValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useQueryAnalysisValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQueryAnalysisValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useQueryAnalysisValueQuery(baseOptions: Apollo.QueryHookOptions<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>(QueryAnalysisValueDocument, options);
      }
export function useQueryAnalysisValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>(QueryAnalysisValueDocument, options);
        }
export type QueryAnalysisValueQueryHookResult = ReturnType<typeof useQueryAnalysisValueQuery>;
export type QueryAnalysisValueLazyQueryHookResult = ReturnType<typeof useQueryAnalysisValueLazyQuery>;
export type QueryAnalysisValueQueryResult = Apollo.QueryResult<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>;
export const BindQueryAnalysisToBucketDocument = gql`
    mutation BindQueryAnalysisToBucket($bucketId: ID!, $queryAnalysis: ID!) {
  bindQueryAnalysisToBucket(bucketId: $bucketId, queryAnalysisId: $queryAnalysis) {
    left {
      id
      queryAnalysis {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindQueryAnalysisToBucketMutationFn = Apollo.MutationFunction<BindQueryAnalysisToBucketMutation, BindQueryAnalysisToBucketMutationVariables>;

/**
 * __useBindQueryAnalysisToBucketMutation__
 *
 * To run a mutation, you first call `useBindQueryAnalysisToBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindQueryAnalysisToBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindQueryAnalysisToBucketMutation, { data, loading, error }] = useBindQueryAnalysisToBucketMutation({
 *   variables: {
 *      bucketId: // value for 'bucketId'
 *      queryAnalysis: // value for 'queryAnalysis'
 *   },
 * });
 */
export function useBindQueryAnalysisToBucketMutation(baseOptions?: Apollo.MutationHookOptions<BindQueryAnalysisToBucketMutation, BindQueryAnalysisToBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindQueryAnalysisToBucketMutation, BindQueryAnalysisToBucketMutationVariables>(BindQueryAnalysisToBucketDocument, options);
      }
export type BindQueryAnalysisToBucketMutationHookResult = ReturnType<typeof useBindQueryAnalysisToBucketMutation>;
export type BindQueryAnalysisToBucketMutationResult = Apollo.MutationResult<BindQueryAnalysisToBucketMutation>;
export type BindQueryAnalysisToBucketMutationOptions = Apollo.BaseMutationOptions<BindQueryAnalysisToBucketMutation, BindQueryAnalysisToBucketMutationVariables>;
export const UnbindQueryAnalysisFromBucketDocument = gql`
    mutation UnbindQueryAnalysisFromBucket($bucketId: ID!) {
  unbindQueryAnalysisFromBucket(bucketId: $bucketId) {
    right {
      id
    }
  }
}
    `;
export type UnbindQueryAnalysisFromBucketMutationFn = Apollo.MutationFunction<UnbindQueryAnalysisFromBucketMutation, UnbindQueryAnalysisFromBucketMutationVariables>;

/**
 * __useUnbindQueryAnalysisFromBucketMutation__
 *
 * To run a mutation, you first call `useUnbindQueryAnalysisFromBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindQueryAnalysisFromBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindQueryAnalysisFromBucketMutation, { data, loading, error }] = useUnbindQueryAnalysisFromBucketMutation({
 *   variables: {
 *      bucketId: // value for 'bucketId'
 *   },
 * });
 */
export function useUnbindQueryAnalysisFromBucketMutation(baseOptions?: Apollo.MutationHookOptions<UnbindQueryAnalysisFromBucketMutation, UnbindQueryAnalysisFromBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindQueryAnalysisFromBucketMutation, UnbindQueryAnalysisFromBucketMutationVariables>(UnbindQueryAnalysisFromBucketDocument, options);
      }
export type UnbindQueryAnalysisFromBucketMutationHookResult = ReturnType<typeof useUnbindQueryAnalysisFromBucketMutation>;
export type UnbindQueryAnalysisFromBucketMutationResult = Apollo.MutationResult<UnbindQueryAnalysisFromBucketMutation>;
export type UnbindQueryAnalysisFromBucketMutationOptions = Apollo.BaseMutationOptions<UnbindQueryAnalysisFromBucketMutation, UnbindQueryAnalysisFromBucketMutationVariables>;
export const BindSearchConfigToBucketDocument = gql`
    mutation BindSearchConfigToBucket($bucketId: ID!, $searchConfigId: ID!) {
  bindSearchConfigToBucket(bucketId: $bucketId, searchConfigId: $searchConfigId) {
    left {
      id
      searchConfig {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindSearchConfigToBucketMutationFn = Apollo.MutationFunction<BindSearchConfigToBucketMutation, BindSearchConfigToBucketMutationVariables>;

/**
 * __useBindSearchConfigToBucketMutation__
 *
 * To run a mutation, you first call `useBindSearchConfigToBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindSearchConfigToBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindSearchConfigToBucketMutation, { data, loading, error }] = useBindSearchConfigToBucketMutation({
 *   variables: {
 *      bucketId: // value for 'bucketId'
 *      searchConfigId: // value for 'searchConfigId'
 *   },
 * });
 */
export function useBindSearchConfigToBucketMutation(baseOptions?: Apollo.MutationHookOptions<BindSearchConfigToBucketMutation, BindSearchConfigToBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindSearchConfigToBucketMutation, BindSearchConfigToBucketMutationVariables>(BindSearchConfigToBucketDocument, options);
      }
export type BindSearchConfigToBucketMutationHookResult = ReturnType<typeof useBindSearchConfigToBucketMutation>;
export type BindSearchConfigToBucketMutationResult = Apollo.MutationResult<BindSearchConfigToBucketMutation>;
export type BindSearchConfigToBucketMutationOptions = Apollo.BaseMutationOptions<BindSearchConfigToBucketMutation, BindSearchConfigToBucketMutationVariables>;
export const UnbindSearchConfigFromBucketDocument = gql`
    mutation UnbindSearchConfigFromBucket($bucketId: ID!) {
  unbindSearchConfigFromBucket(bucketId: $bucketId) {
    right {
      id
    }
  }
}
    `;
export type UnbindSearchConfigFromBucketMutationFn = Apollo.MutationFunction<UnbindSearchConfigFromBucketMutation, UnbindSearchConfigFromBucketMutationVariables>;

/**
 * __useUnbindSearchConfigFromBucketMutation__
 *
 * To run a mutation, you first call `useUnbindSearchConfigFromBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindSearchConfigFromBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindSearchConfigFromBucketMutation, { data, loading, error }] = useUnbindSearchConfigFromBucketMutation({
 *   variables: {
 *      bucketId: // value for 'bucketId'
 *   },
 * });
 */
export function useUnbindSearchConfigFromBucketMutation(baseOptions?: Apollo.MutationHookOptions<UnbindSearchConfigFromBucketMutation, UnbindSearchConfigFromBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindSearchConfigFromBucketMutation, UnbindSearchConfigFromBucketMutationVariables>(UnbindSearchConfigFromBucketDocument, options);
      }
export type UnbindSearchConfigFromBucketMutationHookResult = ReturnType<typeof useUnbindSearchConfigFromBucketMutation>;
export type UnbindSearchConfigFromBucketMutationResult = Apollo.MutationResult<UnbindSearchConfigFromBucketMutation>;
export type UnbindSearchConfigFromBucketMutationOptions = Apollo.BaseMutationOptions<UnbindSearchConfigFromBucketMutation, UnbindSearchConfigFromBucketMutationVariables>;
export const BindLanguageToBucketDocument = gql`
    mutation BindLanguageToBucket($bucketId: ID!, $languageId: ID!) {
  bindLanguageToBucket(bucketId: $bucketId, languageId: $languageId) {
    left {
      id
      language {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindLanguageToBucketMutationFn = Apollo.MutationFunction<BindLanguageToBucketMutation, BindLanguageToBucketMutationVariables>;

/**
 * __useBindLanguageToBucketMutation__
 *
 * To run a mutation, you first call `useBindLanguageToBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindLanguageToBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindLanguageToBucketMutation, { data, loading, error }] = useBindLanguageToBucketMutation({
 *   variables: {
 *      bucketId: // value for 'bucketId'
 *      languageId: // value for 'languageId'
 *   },
 * });
 */
export function useBindLanguageToBucketMutation(baseOptions?: Apollo.MutationHookOptions<BindLanguageToBucketMutation, BindLanguageToBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindLanguageToBucketMutation, BindLanguageToBucketMutationVariables>(BindLanguageToBucketDocument, options);
      }
export type BindLanguageToBucketMutationHookResult = ReturnType<typeof useBindLanguageToBucketMutation>;
export type BindLanguageToBucketMutationResult = Apollo.MutationResult<BindLanguageToBucketMutation>;
export type BindLanguageToBucketMutationOptions = Apollo.BaseMutationOptions<BindLanguageToBucketMutation, BindLanguageToBucketMutationVariables>;
export const UnbindLanguageFromBucketDocument = gql`
    mutation UnbindLanguageFromBucket($bucketId: ID!) {
  unbindLanguageFromBucket(bucketId: $bucketId) {
    right {
      id
    }
  }
}
    `;
export type UnbindLanguageFromBucketMutationFn = Apollo.MutationFunction<UnbindLanguageFromBucketMutation, UnbindLanguageFromBucketMutationVariables>;

/**
 * __useUnbindLanguageFromBucketMutation__
 *
 * To run a mutation, you first call `useUnbindLanguageFromBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindLanguageFromBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindLanguageFromBucketMutation, { data, loading, error }] = useUnbindLanguageFromBucketMutation({
 *   variables: {
 *      bucketId: // value for 'bucketId'
 *   },
 * });
 */
export function useUnbindLanguageFromBucketMutation(baseOptions?: Apollo.MutationHookOptions<UnbindLanguageFromBucketMutation, UnbindLanguageFromBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindLanguageFromBucketMutation, UnbindLanguageFromBucketMutationVariables>(UnbindLanguageFromBucketDocument, options);
      }
export type UnbindLanguageFromBucketMutationHookResult = ReturnType<typeof useUnbindLanguageFromBucketMutation>;
export type UnbindLanguageFromBucketMutationResult = Apollo.MutationResult<UnbindLanguageFromBucketMutation>;
export type UnbindLanguageFromBucketMutationOptions = Apollo.BaseMutationOptions<UnbindLanguageFromBucketMutation, UnbindLanguageFromBucketMutationVariables>;
export const BucketDocument = gql`
    query Bucket($id: ID!) {
  bucket(id: $id) {
    id
    name
    description
    enabled
    refreshOnDate
    refreshOnQuery
    refreshOnSuggestionCategory
    refreshOnTab
    retrieveType
    queryAnalysis {
      id
      name
    }
    searchConfig {
      id
      name
    }
    ragConfigurationChat {
      id
      name
    }
    ragConfigurationChatTool {
      id
      name
    }
    ragConfigurationSimpleGenerate {
      id
      name
    }
    language {
      id
      name
    }
  }
}
    `;

/**
 * __useBucketQuery__
 *
 * To run a query within a React component, call `useBucketQuery` and pass it any options that fit your needs.
 * When your component renders, `useBucketQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useBucketQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useBucketQuery(baseOptions: Apollo.QueryHookOptions<BucketQuery, BucketQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketQuery, BucketQueryVariables>(BucketDocument, options);
      }
export function useBucketLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketQuery, BucketQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketQuery, BucketQueryVariables>(BucketDocument, options);
        }
export type BucketQueryHookResult = ReturnType<typeof useBucketQuery>;
export type BucketLazyQueryHookResult = ReturnType<typeof useBucketLazyQuery>;
export type BucketQueryResult = Apollo.QueryResult<BucketQuery, BucketQueryVariables>;
export const EnableBucketDocument = gql`
    mutation EnableBucket($id: ID!) {
  enableBucket(id: $id) {
    id
    name
  }
}
    `;
export type EnableBucketMutationFn = Apollo.MutationFunction<EnableBucketMutation, EnableBucketMutationVariables>;

/**
 * __useEnableBucketMutation__
 *
 * To run a mutation, you first call `useEnableBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useEnableBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [enableBucketMutation, { data, loading, error }] = useEnableBucketMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useEnableBucketMutation(baseOptions?: Apollo.MutationHookOptions<EnableBucketMutation, EnableBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<EnableBucketMutation, EnableBucketMutationVariables>(EnableBucketDocument, options);
      }
export type EnableBucketMutationHookResult = ReturnType<typeof useEnableBucketMutation>;
export type EnableBucketMutationResult = Apollo.MutationResult<EnableBucketMutation>;
export type EnableBucketMutationOptions = Apollo.BaseMutationOptions<EnableBucketMutation, EnableBucketMutationVariables>;
export const DeleteBucketDocument = gql`
    mutation DeleteBucket($id: ID!) {
  deleteBucket(bucketId: $id) {
    id
    name
  }
}
    `;
export type DeleteBucketMutationFn = Apollo.MutationFunction<DeleteBucketMutation, DeleteBucketMutationVariables>;

/**
 * __useDeleteBucketMutation__
 *
 * To run a mutation, you first call `useDeleteBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteBucketMutation, { data, loading, error }] = useDeleteBucketMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteBucketMutation(baseOptions?: Apollo.MutationHookOptions<DeleteBucketMutation, DeleteBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteBucketMutation, DeleteBucketMutationVariables>(DeleteBucketDocument, options);
      }
export type DeleteBucketMutationHookResult = ReturnType<typeof useDeleteBucketMutation>;
export type DeleteBucketMutationResult = Apollo.MutationResult<DeleteBucketMutation>;
export type DeleteBucketMutationOptions = Apollo.BaseMutationOptions<DeleteBucketMutation, DeleteBucketMutationVariables>;
export const BucketDataSourcesDocument = gql`
    query BucketDataSources($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  bucket(id: $parentId) {
    id
    tabs(
      searchText: $searchText
      first: 20
      after: $cursor
      notEqual: $unassociated
    ) {
      edges {
        node {
          name
          id
        }
      }
    }
    suggestionCategories(
      searchText: $searchText
      first: 20
      after: $cursor
      notEqual: $unassociated
    ) {
      edges {
        node {
          id
          name
        }
      }
    }
    datasources(
      searchText: $searchText
      first: 20
      after: $cursor
      notEqual: $unassociated
    ) {
      edges {
        node {
          id
          name
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
}
    `;

/**
 * __useBucketDataSourcesQuery__
 *
 * To run a query within a React component, call `useBucketDataSourcesQuery` and pass it any options that fit your needs.
 * When your component renders, `useBucketDataSourcesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useBucketDataSourcesQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      unassociated: // value for 'unassociated'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useBucketDataSourcesQuery(baseOptions: Apollo.QueryHookOptions<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>(BucketDataSourcesDocument, options);
      }
export function useBucketDataSourcesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>(BucketDataSourcesDocument, options);
        }
export type BucketDataSourcesQueryHookResult = ReturnType<typeof useBucketDataSourcesQuery>;
export type BucketDataSourcesLazyQueryHookResult = ReturnType<typeof useBucketDataSourcesLazyQuery>;
export type BucketDataSourcesQueryResult = Apollo.QueryResult<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>;
export const CreateOrUpdateBucketDocument = gql`
    mutation CreateOrUpdateBucket($id: ID, $name: String!, $description: String, $refreshOnDate: Boolean!, $refreshOnQuery: Boolean!, $refreshOnSuggestionCategory: Boolean!, $refreshOnTab: Boolean!, $retrieveType: RetrieveType!, $datasourceIds: [BigInteger], $suggestionCategoryIds: [BigInteger], $tabIds: [BigInteger], $queryAnalysisId: BigInteger, $defaultLanguageId: BigInteger, $searchConfigId: BigInteger, $ragConfigurationChat: BigInteger, $ragConfigurationChatTool: BigInteger, $ragConfigurationSimpleGenerate: BigInteger) {
  bucketWithLists(
    id: $id
    bucketWithListsDTO: {name: $name, description: $description, refreshOnDate: $refreshOnDate, refreshOnQuery: $refreshOnQuery, refreshOnSuggestionCategory: $refreshOnSuggestionCategory, refreshOnTab: $refreshOnTab, retrieveType: $retrieveType, datasourceIds: $datasourceIds, suggestionCategoryIds: $suggestionCategoryIds, tabIds: $tabIds, queryAnalysisId: $queryAnalysisId, defaultLanguageId: $defaultLanguageId, searchConfigId: $searchConfigId, ragConfigurationChat: $ragConfigurationChat, ragConfigurationChatTool: $ragConfigurationChatTool, ragConfigurationSimpleGenerate: $ragConfigurationSimpleGenerate}
  ) {
    entity {
      id
      name
      enabled
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateBucketMutationFn = Apollo.MutationFunction<CreateOrUpdateBucketMutation, CreateOrUpdateBucketMutationVariables>;

/**
 * __useCreateOrUpdateBucketMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateBucketMutation, { data, loading, error }] = useCreateOrUpdateBucketMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      refreshOnDate: // value for 'refreshOnDate'
 *      refreshOnQuery: // value for 'refreshOnQuery'
 *      refreshOnSuggestionCategory: // value for 'refreshOnSuggestionCategory'
 *      refreshOnTab: // value for 'refreshOnTab'
 *      retrieveType: // value for 'retrieveType'
 *      datasourceIds: // value for 'datasourceIds'
 *      suggestionCategoryIds: // value for 'suggestionCategoryIds'
 *      tabIds: // value for 'tabIds'
 *      queryAnalysisId: // value for 'queryAnalysisId'
 *      defaultLanguageId: // value for 'defaultLanguageId'
 *      searchConfigId: // value for 'searchConfigId'
 *      ragConfigurationChat: // value for 'ragConfigurationChat'
 *      ragConfigurationChatTool: // value for 'ragConfigurationChatTool'
 *      ragConfigurationSimpleGenerate: // value for 'ragConfigurationSimpleGenerate'
 *   },
 * });
 */
export function useCreateOrUpdateBucketMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateBucketMutation, CreateOrUpdateBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateBucketMutation, CreateOrUpdateBucketMutationVariables>(CreateOrUpdateBucketDocument, options);
      }
export type CreateOrUpdateBucketMutationHookResult = ReturnType<typeof useCreateOrUpdateBucketMutation>;
export type CreateOrUpdateBucketMutationResult = Apollo.MutationResult<CreateOrUpdateBucketMutation>;
export type CreateOrUpdateBucketMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateBucketMutation, CreateOrUpdateBucketMutationVariables>;
export const AddDataSourceToBucketDocument = gql`
    mutation AddDataSourceToBucket($childId: ID!, $parentId: ID!) {
  addDatasourceToBucket(datasourceId: $childId, bucketId: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddDataSourceToBucketMutationFn = Apollo.MutationFunction<AddDataSourceToBucketMutation, AddDataSourceToBucketMutationVariables>;

/**
 * __useAddDataSourceToBucketMutation__
 *
 * To run a mutation, you first call `useAddDataSourceToBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddDataSourceToBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addDataSourceToBucketMutation, { data, loading, error }] = useAddDataSourceToBucketMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddDataSourceToBucketMutation(baseOptions?: Apollo.MutationHookOptions<AddDataSourceToBucketMutation, AddDataSourceToBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddDataSourceToBucketMutation, AddDataSourceToBucketMutationVariables>(AddDataSourceToBucketDocument, options);
      }
export type AddDataSourceToBucketMutationHookResult = ReturnType<typeof useAddDataSourceToBucketMutation>;
export type AddDataSourceToBucketMutationResult = Apollo.MutationResult<AddDataSourceToBucketMutation>;
export type AddDataSourceToBucketMutationOptions = Apollo.BaseMutationOptions<AddDataSourceToBucketMutation, AddDataSourceToBucketMutationVariables>;
export const RemoveDataSourceFromBucketDocument = gql`
    mutation RemoveDataSourceFromBucket($childId: ID!, $parentId: ID!) {
  removeDatasourceFromBucket(datasourceId: $childId, bucketId: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveDataSourceFromBucketMutationFn = Apollo.MutationFunction<RemoveDataSourceFromBucketMutation, RemoveDataSourceFromBucketMutationVariables>;

/**
 * __useRemoveDataSourceFromBucketMutation__
 *
 * To run a mutation, you first call `useRemoveDataSourceFromBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveDataSourceFromBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeDataSourceFromBucketMutation, { data, loading, error }] = useRemoveDataSourceFromBucketMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveDataSourceFromBucketMutation(baseOptions?: Apollo.MutationHookOptions<RemoveDataSourceFromBucketMutation, RemoveDataSourceFromBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveDataSourceFromBucketMutation, RemoveDataSourceFromBucketMutationVariables>(RemoveDataSourceFromBucketDocument, options);
      }
export type RemoveDataSourceFromBucketMutationHookResult = ReturnType<typeof useRemoveDataSourceFromBucketMutation>;
export type RemoveDataSourceFromBucketMutationResult = Apollo.MutationResult<RemoveDataSourceFromBucketMutation>;
export type RemoveDataSourceFromBucketMutationOptions = Apollo.BaseMutationOptions<RemoveDataSourceFromBucketMutation, RemoveDataSourceFromBucketMutationVariables>;
export const BucketLanguagesDocument = gql`
    query BucketLanguages($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  bucket(id: $parentId) {
    id
    languages(
      searchText: $searchText
      first: 20
      after: $cursor
      notEqual: $unassociated
    ) {
      edges {
        node {
          id
          name
          value
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
}
    `;

/**
 * __useBucketLanguagesQuery__
 *
 * To run a query within a React component, call `useBucketLanguagesQuery` and pass it any options that fit your needs.
 * When your component renders, `useBucketLanguagesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useBucketLanguagesQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      unassociated: // value for 'unassociated'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useBucketLanguagesQuery(baseOptions: Apollo.QueryHookOptions<BucketLanguagesQuery, BucketLanguagesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketLanguagesQuery, BucketLanguagesQueryVariables>(BucketLanguagesDocument, options);
      }
export function useBucketLanguagesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketLanguagesQuery, BucketLanguagesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketLanguagesQuery, BucketLanguagesQueryVariables>(BucketLanguagesDocument, options);
        }
export type BucketLanguagesQueryHookResult = ReturnType<typeof useBucketLanguagesQuery>;
export type BucketLanguagesLazyQueryHookResult = ReturnType<typeof useBucketLanguagesLazyQuery>;
export type BucketLanguagesQueryResult = Apollo.QueryResult<BucketLanguagesQuery, BucketLanguagesQueryVariables>;
export const AddLanguageToBucketDocument = gql`
    mutation AddLanguageToBucket($childId: ID!, $parentId: ID!) {
  addLanguageToBucket(languageId: $childId, bucketId: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddLanguageToBucketMutationFn = Apollo.MutationFunction<AddLanguageToBucketMutation, AddLanguageToBucketMutationVariables>;

/**
 * __useAddLanguageToBucketMutation__
 *
 * To run a mutation, you first call `useAddLanguageToBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddLanguageToBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addLanguageToBucketMutation, { data, loading, error }] = useAddLanguageToBucketMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddLanguageToBucketMutation(baseOptions?: Apollo.MutationHookOptions<AddLanguageToBucketMutation, AddLanguageToBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddLanguageToBucketMutation, AddLanguageToBucketMutationVariables>(AddLanguageToBucketDocument, options);
      }
export type AddLanguageToBucketMutationHookResult = ReturnType<typeof useAddLanguageToBucketMutation>;
export type AddLanguageToBucketMutationResult = Apollo.MutationResult<AddLanguageToBucketMutation>;
export type AddLanguageToBucketMutationOptions = Apollo.BaseMutationOptions<AddLanguageToBucketMutation, AddLanguageToBucketMutationVariables>;
export const RemoveLanguageFromBucketDocument = gql`
    mutation RemoveLanguageFromBucket($childId: ID!, $parentId: ID!) {
  removeLanguageFromBucket(languageId: $childId, bucketId: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveLanguageFromBucketMutationFn = Apollo.MutationFunction<RemoveLanguageFromBucketMutation, RemoveLanguageFromBucketMutationVariables>;

/**
 * __useRemoveLanguageFromBucketMutation__
 *
 * To run a mutation, you first call `useRemoveLanguageFromBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveLanguageFromBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeLanguageFromBucketMutation, { data, loading, error }] = useRemoveLanguageFromBucketMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveLanguageFromBucketMutation(baseOptions?: Apollo.MutationHookOptions<RemoveLanguageFromBucketMutation, RemoveLanguageFromBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveLanguageFromBucketMutation, RemoveLanguageFromBucketMutationVariables>(RemoveLanguageFromBucketDocument, options);
      }
export type RemoveLanguageFromBucketMutationHookResult = ReturnType<typeof useRemoveLanguageFromBucketMutation>;
export type RemoveLanguageFromBucketMutationResult = Apollo.MutationResult<RemoveLanguageFromBucketMutation>;
export type RemoveLanguageFromBucketMutationOptions = Apollo.BaseMutationOptions<RemoveLanguageFromBucketMutation, RemoveLanguageFromBucketMutationVariables>;
export const BucketTabsDocument = gql`
    query BucketTabs($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  bucket(id: $parentId) {
    id
    tabs(
      searchText: $searchText
      notEqual: $unassociated
      first: 20
      after: $cursor
    ) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
}
    `;

/**
 * __useBucketTabsQuery__
 *
 * To run a query within a React component, call `useBucketTabsQuery` and pass it any options that fit your needs.
 * When your component renders, `useBucketTabsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useBucketTabsQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      unassociated: // value for 'unassociated'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useBucketTabsQuery(baseOptions: Apollo.QueryHookOptions<BucketTabsQuery, BucketTabsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketTabsQuery, BucketTabsQueryVariables>(BucketTabsDocument, options);
      }
export function useBucketTabsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketTabsQuery, BucketTabsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketTabsQuery, BucketTabsQueryVariables>(BucketTabsDocument, options);
        }
export type BucketTabsQueryHookResult = ReturnType<typeof useBucketTabsQuery>;
export type BucketTabsLazyQueryHookResult = ReturnType<typeof useBucketTabsLazyQuery>;
export type BucketTabsQueryResult = Apollo.QueryResult<BucketTabsQuery, BucketTabsQueryVariables>;
export const AddTabToBucketDocument = gql`
    mutation AddTabToBucket($childId: ID!, $parentId: ID!) {
  addTabToBucket(tabId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddTabToBucketMutationFn = Apollo.MutationFunction<AddTabToBucketMutation, AddTabToBucketMutationVariables>;

/**
 * __useAddTabToBucketMutation__
 *
 * To run a mutation, you first call `useAddTabToBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddTabToBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addTabToBucketMutation, { data, loading, error }] = useAddTabToBucketMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddTabToBucketMutation(baseOptions?: Apollo.MutationHookOptions<AddTabToBucketMutation, AddTabToBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddTabToBucketMutation, AddTabToBucketMutationVariables>(AddTabToBucketDocument, options);
      }
export type AddTabToBucketMutationHookResult = ReturnType<typeof useAddTabToBucketMutation>;
export type AddTabToBucketMutationResult = Apollo.MutationResult<AddTabToBucketMutation>;
export type AddTabToBucketMutationOptions = Apollo.BaseMutationOptions<AddTabToBucketMutation, AddTabToBucketMutationVariables>;
export const RemoveTabFromBucketDocument = gql`
    mutation RemoveTabFromBucket($childId: ID!, $parentId: ID!) {
  removeTabFromBucket(tabId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveTabFromBucketMutationFn = Apollo.MutationFunction<RemoveTabFromBucketMutation, RemoveTabFromBucketMutationVariables>;

/**
 * __useRemoveTabFromBucketMutation__
 *
 * To run a mutation, you first call `useRemoveTabFromBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveTabFromBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeTabFromBucketMutation, { data, loading, error }] = useRemoveTabFromBucketMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveTabFromBucketMutation(baseOptions?: Apollo.MutationHookOptions<RemoveTabFromBucketMutation, RemoveTabFromBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveTabFromBucketMutation, RemoveTabFromBucketMutationVariables>(RemoveTabFromBucketDocument, options);
      }
export type RemoveTabFromBucketMutationHookResult = ReturnType<typeof useRemoveTabFromBucketMutation>;
export type RemoveTabFromBucketMutationResult = Apollo.MutationResult<RemoveTabFromBucketMutation>;
export type RemoveTabFromBucketMutationOptions = Apollo.BaseMutationOptions<RemoveTabFromBucketMutation, RemoveTabFromBucketMutationVariables>;
export const BucketSuggestionCategoriesDocument = gql`
    query BucketSuggestionCategories($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  bucket(id: $parentId) {
    id
    suggestionCategories(
      searchText: $searchText
      notEqual: $unassociated
      first: 20
      after: $cursor
    ) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
}
    `;

/**
 * __useBucketSuggestionCategoriesQuery__
 *
 * To run a query within a React component, call `useBucketSuggestionCategoriesQuery` and pass it any options that fit your needs.
 * When your component renders, `useBucketSuggestionCategoriesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useBucketSuggestionCategoriesQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      unassociated: // value for 'unassociated'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useBucketSuggestionCategoriesQuery(baseOptions: Apollo.QueryHookOptions<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>(BucketSuggestionCategoriesDocument, options);
      }
export function useBucketSuggestionCategoriesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>(BucketSuggestionCategoriesDocument, options);
        }
export type BucketSuggestionCategoriesQueryHookResult = ReturnType<typeof useBucketSuggestionCategoriesQuery>;
export type BucketSuggestionCategoriesLazyQueryHookResult = ReturnType<typeof useBucketSuggestionCategoriesLazyQuery>;
export type BucketSuggestionCategoriesQueryResult = Apollo.QueryResult<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>;
export const AddSuggestionCategoryToBucketDocument = gql`
    mutation AddSuggestionCategoryToBucket($childId: ID!, $parentId: ID!) {
  addSuggestionCategoryToBucket(
    suggestionCategoryId: $childId
    bucketId: $parentId
  ) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddSuggestionCategoryToBucketMutationFn = Apollo.MutationFunction<AddSuggestionCategoryToBucketMutation, AddSuggestionCategoryToBucketMutationVariables>;

/**
 * __useAddSuggestionCategoryToBucketMutation__
 *
 * To run a mutation, you first call `useAddSuggestionCategoryToBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddSuggestionCategoryToBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addSuggestionCategoryToBucketMutation, { data, loading, error }] = useAddSuggestionCategoryToBucketMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddSuggestionCategoryToBucketMutation(baseOptions?: Apollo.MutationHookOptions<AddSuggestionCategoryToBucketMutation, AddSuggestionCategoryToBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddSuggestionCategoryToBucketMutation, AddSuggestionCategoryToBucketMutationVariables>(AddSuggestionCategoryToBucketDocument, options);
      }
export type AddSuggestionCategoryToBucketMutationHookResult = ReturnType<typeof useAddSuggestionCategoryToBucketMutation>;
export type AddSuggestionCategoryToBucketMutationResult = Apollo.MutationResult<AddSuggestionCategoryToBucketMutation>;
export type AddSuggestionCategoryToBucketMutationOptions = Apollo.BaseMutationOptions<AddSuggestionCategoryToBucketMutation, AddSuggestionCategoryToBucketMutationVariables>;
export const RemoveSuggestionCategoryFromBucketDocument = gql`
    mutation RemoveSuggestionCategoryFromBucket($childId: ID!, $parentId: ID!) {
  removeSuggestionCategoryFromBucket(
    suggestionCategoryId: $childId
    bucketId: $parentId
  ) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveSuggestionCategoryFromBucketMutationFn = Apollo.MutationFunction<RemoveSuggestionCategoryFromBucketMutation, RemoveSuggestionCategoryFromBucketMutationVariables>;

/**
 * __useRemoveSuggestionCategoryFromBucketMutation__
 *
 * To run a mutation, you first call `useRemoveSuggestionCategoryFromBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveSuggestionCategoryFromBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeSuggestionCategoryFromBucketMutation, { data, loading, error }] = useRemoveSuggestionCategoryFromBucketMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveSuggestionCategoryFromBucketMutation(baseOptions?: Apollo.MutationHookOptions<RemoveSuggestionCategoryFromBucketMutation, RemoveSuggestionCategoryFromBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveSuggestionCategoryFromBucketMutation, RemoveSuggestionCategoryFromBucketMutationVariables>(RemoveSuggestionCategoryFromBucketDocument, options);
      }
export type RemoveSuggestionCategoryFromBucketMutationHookResult = ReturnType<typeof useRemoveSuggestionCategoryFromBucketMutation>;
export type RemoveSuggestionCategoryFromBucketMutationResult = Apollo.MutationResult<RemoveSuggestionCategoryFromBucketMutation>;
export type RemoveSuggestionCategoryFromBucketMutationOptions = Apollo.BaseMutationOptions<RemoveSuggestionCategoryFromBucketMutation, RemoveSuggestionCategoryFromBucketMutationVariables>;
export const CreateDataIndexDocument = gql`
    mutation CreateDataIndex($name: String!, $datasourceId: ID!, $description: String, $knnIndex: Boolean, $docTypeIds: [BigInteger], $chunkType: ChunkType, $chunkWindowSize: Int, $embeddingJsonConfig: String, $embeddingDocTypeFieldId: BigInteger, $settings: String) {
  dataIndex(
    datasourceId: $datasourceId
    dataIndexDTO: {name: $name, description: $description, knnIndex: $knnIndex, docTypeIds: $docTypeIds, chunkType: $chunkType, chunkWindowSize: $chunkWindowSize, embeddingJsonConfig: $embeddingJsonConfig, embeddingDocTypeFieldId: $embeddingDocTypeFieldId, settings: $settings}
  ) {
    entity {
      name
    }
  }
}
    `;
export type CreateDataIndexMutationFn = Apollo.MutationFunction<CreateDataIndexMutation, CreateDataIndexMutationVariables>;

/**
 * __useCreateDataIndexMutation__
 *
 * To run a mutation, you first call `useCreateDataIndexMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateDataIndexMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createDataIndexMutation, { data, loading, error }] = useCreateDataIndexMutation({
 *   variables: {
 *      name: // value for 'name'
 *      datasourceId: // value for 'datasourceId'
 *      description: // value for 'description'
 *      knnIndex: // value for 'knnIndex'
 *      docTypeIds: // value for 'docTypeIds'
 *      chunkType: // value for 'chunkType'
 *      chunkWindowSize: // value for 'chunkWindowSize'
 *      embeddingJsonConfig: // value for 'embeddingJsonConfig'
 *      embeddingDocTypeFieldId: // value for 'embeddingDocTypeFieldId'
 *      settings: // value for 'settings'
 *   },
 * });
 */
export function useCreateDataIndexMutation(baseOptions?: Apollo.MutationHookOptions<CreateDataIndexMutation, CreateDataIndexMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateDataIndexMutation, CreateDataIndexMutationVariables>(CreateDataIndexDocument, options);
      }
export type CreateDataIndexMutationHookResult = ReturnType<typeof useCreateDataIndexMutation>;
export type CreateDataIndexMutationResult = Apollo.MutationResult<CreateDataIndexMutation>;
export type CreateDataIndexMutationOptions = Apollo.BaseMutationOptions<CreateDataIndexMutation, CreateDataIndexMutationVariables>;
export const DataIndicesDocument = gql`
    query DataIndices($searchText: String, $first: Int, $after: String) {
  dataIndices(
    searchText: $searchText
    first: $first
    before: $after
    sortByList: [{column: "modifiedDate", direction: DESC}]
  ) {
    edges {
      node {
        id
        name
        description
        createDate
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useDataIndicesQuery__
 *
 * To run a query within a React component, call `useDataIndicesQuery` and pass it any options that fit your needs.
 * When your component renders, `useDataIndicesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDataIndicesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      first: // value for 'first'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useDataIndicesQuery(baseOptions?: Apollo.QueryHookOptions<DataIndicesQuery, DataIndicesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataIndicesQuery, DataIndicesQueryVariables>(DataIndicesDocument, options);
      }
export function useDataIndicesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataIndicesQuery, DataIndicesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataIndicesQuery, DataIndicesQueryVariables>(DataIndicesDocument, options);
        }
export type DataIndicesQueryHookResult = ReturnType<typeof useDataIndicesQuery>;
export type DataIndicesLazyQueryHookResult = ReturnType<typeof useDataIndicesLazyQuery>;
export type DataIndicesQueryResult = Apollo.QueryResult<DataIndicesQuery, DataIndicesQueryVariables>;
export const DataIndexDocument = gql`
    query DataIndex($id: ID!) {
  dataIndex(id: $id) {
    name
    description
    settings
    chunkType
    chunkWindowSize
    embeddingJsonConfig
    knnIndex
    settings
    datasource {
      id
      name
    }
    embeddingDocTypeField {
      id
      name
    }
    docTypes {
      edges {
        node {
          id
          name
        }
      }
    }
    cat {
      docsCount
      docsDeleted
      storeSize
    }
  }
}
    `;

/**
 * __useDataIndexQuery__
 *
 * To run a query within a React component, call `useDataIndexQuery` and pass it any options that fit your needs.
 * When your component renders, `useDataIndexQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDataIndexQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDataIndexQuery(baseOptions: Apollo.QueryHookOptions<DataIndexQuery, DataIndexQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataIndexQuery, DataIndexQueryVariables>(DataIndexDocument, options);
      }
export function useDataIndexLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataIndexQuery, DataIndexQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataIndexQuery, DataIndexQueryVariables>(DataIndexDocument, options);
        }
export type DataIndexQueryHookResult = ReturnType<typeof useDataIndexQuery>;
export type DataIndexLazyQueryHookResult = ReturnType<typeof useDataIndexLazyQuery>;
export type DataIndexQueryResult = Apollo.QueryResult<DataIndexQuery, DataIndexQueryVariables>;
export const DataIndexMappingDocument = gql`
    query DataIndexMapping($id: ID!) {
  dataIndex(id: $id) {
    mappings
  }
}
    `;

/**
 * __useDataIndexMappingQuery__
 *
 * To run a query within a React component, call `useDataIndexMappingQuery` and pass it any options that fit your needs.
 * When your component renders, `useDataIndexMappingQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDataIndexMappingQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDataIndexMappingQuery(baseOptions: Apollo.QueryHookOptions<DataIndexMappingQuery, DataIndexMappingQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataIndexMappingQuery, DataIndexMappingQueryVariables>(DataIndexMappingDocument, options);
      }
export function useDataIndexMappingLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataIndexMappingQuery, DataIndexMappingQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataIndexMappingQuery, DataIndexMappingQueryVariables>(DataIndexMappingDocument, options);
        }
export type DataIndexMappingQueryHookResult = ReturnType<typeof useDataIndexMappingQuery>;
export type DataIndexMappingLazyQueryHookResult = ReturnType<typeof useDataIndexMappingLazyQuery>;
export type DataIndexMappingQueryResult = Apollo.QueryResult<DataIndexMappingQuery, DataIndexMappingQueryVariables>;
export const DataSourcesDocument = gql`
    query DataSources($searchText: String, $after: String, $first: Int = 10, $sortByList: [SortByInput!]) {
  datasources(
    searchText: $searchText
    first: $first
    after: $after
    sortByList: $sortByList
  ) {
    edges {
      node {
        id
        name
        schedulable
        lastIngestionDate
        scheduling
        jsonConfig
        description
        __typename
      }
      __typename
    }
    pageInfo {
      hasNextPage
      endCursor
      __typename
    }
    __typename
  }
}
    `;

/**
 * __useDataSourcesQuery__
 *
 * To run a query within a React component, call `useDataSourcesQuery` and pass it any options that fit your needs.
 * When your component renders, `useDataSourcesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDataSourcesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *      first: // value for 'first'
 *      sortByList: // value for 'sortByList'
 *   },
 * });
 */
export function useDataSourcesQuery(baseOptions?: Apollo.QueryHookOptions<DataSourcesQuery, DataSourcesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataSourcesQuery, DataSourcesQueryVariables>(DataSourcesDocument, options);
      }
export function useDataSourcesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataSourcesQuery, DataSourcesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataSourcesQuery, DataSourcesQueryVariables>(DataSourcesDocument, options);
        }
export type DataSourcesQueryHookResult = ReturnType<typeof useDataSourcesQuery>;
export type DataSourcesLazyQueryHookResult = ReturnType<typeof useDataSourcesLazyQuery>;
export type DataSourcesQueryResult = Apollo.QueryResult<DataSourcesQuery, DataSourcesQueryVariables>;
export const DeleteDataSourceDocument = gql`
    mutation DeleteDataSource($id: ID!, $datasourceName: String!) {
  deleteDatasource(datasourceId: $id, datasourceName: $datasourceName) {
    id
    name
  }
}
    `;
export type DeleteDataSourceMutationFn = Apollo.MutationFunction<DeleteDataSourceMutation, DeleteDataSourceMutationVariables>;

/**
 * __useDeleteDataSourceMutation__
 *
 * To run a mutation, you first call `useDeleteDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteDataSourceMutation, { data, loading, error }] = useDeleteDataSourceMutation({
 *   variables: {
 *      id: // value for 'id'
 *      datasourceName: // value for 'datasourceName'
 *   },
 * });
 */
export function useDeleteDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<DeleteDataSourceMutation, DeleteDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteDataSourceMutation, DeleteDataSourceMutationVariables>(DeleteDataSourceDocument, options);
      }
export type DeleteDataSourceMutationHookResult = ReturnType<typeof useDeleteDataSourceMutation>;
export type DeleteDataSourceMutationResult = Apollo.MutationResult<DeleteDataSourceMutation>;
export type DeleteDataSourceMutationOptions = Apollo.BaseMutationOptions<DeleteDataSourceMutation, DeleteDataSourceMutationVariables>;
export const UnboundBucketsByDatasourceDocument = gql`
    query UnboundBucketsByDatasource($datasourceId: BigInteger!) {
  unboundBucketsByDatasource(datasourceId: $datasourceId) {
    name
    id
  }
}
    `;

/**
 * __useUnboundBucketsByDatasourceQuery__
 *
 * To run a query within a React component, call `useUnboundBucketsByDatasourceQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundBucketsByDatasourceQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundBucketsByDatasourceQuery({
 *   variables: {
 *      datasourceId: // value for 'datasourceId'
 *   },
 * });
 */
export function useUnboundBucketsByDatasourceQuery(baseOptions: Apollo.QueryHookOptions<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>(UnboundBucketsByDatasourceDocument, options);
      }
export function useUnboundBucketsByDatasourceLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>(UnboundBucketsByDatasourceDocument, options);
        }
export type UnboundBucketsByDatasourceQueryHookResult = ReturnType<typeof useUnboundBucketsByDatasourceQuery>;
export type UnboundBucketsByDatasourceLazyQueryHookResult = ReturnType<typeof useUnboundBucketsByDatasourceLazyQuery>;
export type UnboundBucketsByDatasourceQueryResult = Apollo.QueryResult<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>;
export const DataSourceDocument = gql`
    query DataSource($id: ID!, $searchText: String) {
  datasource(id: $id) {
    id
    name
    description
    schedulable
    scheduling
    jsonConfig
    reindexable
    reindexing
    purgeable
    purging
    purgeMaxAge
    lastIngestionDate
    pluginDriver {
      id
      name
      provisioning
      jsonConfig
    }
    dataIndex {
      id
      name
      description
      knnIndex
    }
    enrichPipeline {
      id
      name
    }
    dataIndexes(searchText: $searchText) {
      edges {
        node {
          id
          name
        }
      }
    }
  }
}
    `;

/**
 * __useDataSourceQuery__
 *
 * To run a query within a React component, call `useDataSourceQuery` and pass it any options that fit your needs.
 * When your component renders, `useDataSourceQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDataSourceQuery({
 *   variables: {
 *      id: // value for 'id'
 *      searchText: // value for 'searchText'
 *   },
 * });
 */
export function useDataSourceQuery(baseOptions: Apollo.QueryHookOptions<DataSourceQuery, DataSourceQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataSourceQuery, DataSourceQueryVariables>(DataSourceDocument, options);
      }
export function useDataSourceLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataSourceQuery, DataSourceQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataSourceQuery, DataSourceQueryVariables>(DataSourceDocument, options);
        }
export type DataSourceQueryHookResult = ReturnType<typeof useDataSourceQuery>;
export type DataSourceLazyQueryHookResult = ReturnType<typeof useDataSourceLazyQuery>;
export type DataSourceQueryResult = Apollo.QueryResult<DataSourceQuery, DataSourceQueryVariables>;
export const CreateDatasourceConnectionDocument = gql`
    mutation createDatasourceConnection($name: String!, $description: String, $schedulable: Boolean!, $scheduling: String!, $jsonConfig: String, $pluginDriverId: BigInteger!, $pipeline: PipelineWithItemsDTOInput, $pipelineId: BigInteger, $reindexable: Boolean!, $reindexing: String!, $purgeable: Boolean!, $purging: String!, $purgeMaxAge: String!, $dataIndex: DataIndexDTOInput!) {
  createDatasourceConnection(
    datasourceConnection: {name: $name, description: $description, schedulable: $schedulable, scheduling: $scheduling, jsonConfig: $jsonConfig, pluginDriverId: $pluginDriverId, pipeline: $pipeline, pipelineId: $pipelineId, reindexable: $reindexable, reindexing: $reindexing, purgeable: $purgeable, purging: $purging, purgeMaxAge: $purgeMaxAge, dataIndex: $dataIndex}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateDatasourceConnectionMutationFn = Apollo.MutationFunction<CreateDatasourceConnectionMutation, CreateDatasourceConnectionMutationVariables>;

/**
 * __useCreateDatasourceConnectionMutation__
 *
 * To run a mutation, you first call `useCreateDatasourceConnectionMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateDatasourceConnectionMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createDatasourceConnectionMutation, { data, loading, error }] = useCreateDatasourceConnectionMutation({
 *   variables: {
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      schedulable: // value for 'schedulable'
 *      scheduling: // value for 'scheduling'
 *      jsonConfig: // value for 'jsonConfig'
 *      pluginDriverId: // value for 'pluginDriverId'
 *      pipeline: // value for 'pipeline'
 *      pipelineId: // value for 'pipelineId'
 *      reindexable: // value for 'reindexable'
 *      reindexing: // value for 'reindexing'
 *      purgeable: // value for 'purgeable'
 *      purging: // value for 'purging'
 *      purgeMaxAge: // value for 'purgeMaxAge'
 *      dataIndex: // value for 'dataIndex'
 *   },
 * });
 */
export function useCreateDatasourceConnectionMutation(baseOptions?: Apollo.MutationHookOptions<CreateDatasourceConnectionMutation, CreateDatasourceConnectionMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateDatasourceConnectionMutation, CreateDatasourceConnectionMutationVariables>(CreateDatasourceConnectionDocument, options);
      }
export type CreateDatasourceConnectionMutationHookResult = ReturnType<typeof useCreateDatasourceConnectionMutation>;
export type CreateDatasourceConnectionMutationResult = Apollo.MutationResult<CreateDatasourceConnectionMutation>;
export type CreateDatasourceConnectionMutationOptions = Apollo.BaseMutationOptions<CreateDatasourceConnectionMutation, CreateDatasourceConnectionMutationVariables>;
export const UpdateDatasourceConnectionDocument = gql`
    mutation updateDatasourceConnection($name: String!, $description: String, $schedulable: Boolean!, $scheduling: String!, $jsonConfig: String, $pipeline: PipelineWithItemsDTOInput, $pipelineId: BigInteger, $dataIndexId: BigInteger!, $datasourceId: BigInteger!, $reindexable: Boolean!, $reindexing: String!, $purging: String!, $purgeable: Boolean!, $purgeMaxAge: String!) {
  updateDatasourceConnection(
    datasourceConnection: {name: $name, description: $description, schedulable: $schedulable, scheduling: $scheduling, jsonConfig: $jsonConfig, pipeline: $pipeline, pipelineId: $pipelineId, dataIndexId: $dataIndexId, datasourceId: $datasourceId, reindexable: $reindexable, reindexing: $reindexing, purging: $purging, purgeable: $purgeable, purgeMaxAge: $purgeMaxAge}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type UpdateDatasourceConnectionMutationFn = Apollo.MutationFunction<UpdateDatasourceConnectionMutation, UpdateDatasourceConnectionMutationVariables>;

/**
 * __useUpdateDatasourceConnectionMutation__
 *
 * To run a mutation, you first call `useUpdateDatasourceConnectionMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUpdateDatasourceConnectionMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [updateDatasourceConnectionMutation, { data, loading, error }] = useUpdateDatasourceConnectionMutation({
 *   variables: {
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      schedulable: // value for 'schedulable'
 *      scheduling: // value for 'scheduling'
 *      jsonConfig: // value for 'jsonConfig'
 *      pipeline: // value for 'pipeline'
 *      pipelineId: // value for 'pipelineId'
 *      dataIndexId: // value for 'dataIndexId'
 *      datasourceId: // value for 'datasourceId'
 *      reindexable: // value for 'reindexable'
 *      reindexing: // value for 'reindexing'
 *      purging: // value for 'purging'
 *      purgeable: // value for 'purgeable'
 *      purgeMaxAge: // value for 'purgeMaxAge'
 *   },
 * });
 */
export function useUpdateDatasourceConnectionMutation(baseOptions?: Apollo.MutationHookOptions<UpdateDatasourceConnectionMutation, UpdateDatasourceConnectionMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UpdateDatasourceConnectionMutation, UpdateDatasourceConnectionMutationVariables>(UpdateDatasourceConnectionDocument, options);
      }
export type UpdateDatasourceConnectionMutationHookResult = ReturnType<typeof useUpdateDatasourceConnectionMutation>;
export type UpdateDatasourceConnectionMutationResult = Apollo.MutationResult<UpdateDatasourceConnectionMutation>;
export type UpdateDatasourceConnectionMutationOptions = Apollo.BaseMutationOptions<UpdateDatasourceConnectionMutation, UpdateDatasourceConnectionMutationVariables>;
export const QDatasourceSchedulersDocument = gql`
    query qDatasourceSchedulers($id: ID!, $first: Int, $after: String) {
  datasource(id: $id) {
    id
    schedulers(
      first: $first
      before: $after
      sortByList: {column: "modifiedDate", direction: DESC}
    ) {
      edges {
        node {
          id
          status
          modifiedDate
        }
      }
      pageInfo {
        hasNextPage
        endCursor
        __typename
      }
    }
  }
}
    `;

/**
 * __useQDatasourceSchedulersQuery__
 *
 * To run a query within a React component, call `useQDatasourceSchedulersQuery` and pass it any options that fit your needs.
 * When your component renders, `useQDatasourceSchedulersQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQDatasourceSchedulersQuery({
 *   variables: {
 *      id: // value for 'id'
 *      first: // value for 'first'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useQDatasourceSchedulersQuery(baseOptions: Apollo.QueryHookOptions<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>(QDatasourceSchedulersDocument, options);
      }
export function useQDatasourceSchedulersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>(QDatasourceSchedulersDocument, options);
        }
export type QDatasourceSchedulersQueryHookResult = ReturnType<typeof useQDatasourceSchedulersQuery>;
export type QDatasourceSchedulersLazyQueryHookResult = ReturnType<typeof useQDatasourceSchedulersLazyQuery>;
export type QDatasourceSchedulersQueryResult = Apollo.QueryResult<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>;
export const DataSourceInformationDocument = gql`
    query DataSourceInformation($id: ID!) {
  datasource(id: $id) {
    dataIndex {
      cat {
        docsCount
        docsDeleted
        health
        index
        pri
        priStoreSize
        rep
        status
        storeSize
        uuid
      }
    }
  }
}
    `;

/**
 * __useDataSourceInformationQuery__
 *
 * To run a query within a React component, call `useDataSourceInformationQuery` and pass it any options that fit your needs.
 * When your component renders, `useDataSourceInformationQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDataSourceInformationQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDataSourceInformationQuery(baseOptions: Apollo.QueryHookOptions<DataSourceInformationQuery, DataSourceInformationQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataSourceInformationQuery, DataSourceInformationQueryVariables>(DataSourceInformationDocument, options);
      }
export function useDataSourceInformationLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataSourceInformationQuery, DataSourceInformationQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataSourceInformationQuery, DataSourceInformationQueryVariables>(DataSourceInformationDocument, options);
        }
export type DataSourceInformationQueryHookResult = ReturnType<typeof useDataSourceInformationQuery>;
export type DataSourceInformationLazyQueryHookResult = ReturnType<typeof useDataSourceInformationLazyQuery>;
export type DataSourceInformationQueryResult = Apollo.QueryResult<DataSourceInformationQuery, DataSourceInformationQueryVariables>;
export const EnrichPipelineOptionsDocument = gql`
    query EnrichPipelineOptions($searchText: String, $cursor: String) {
  options: enrichPipelines(searchText: $searchText, after: $cursor) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useEnrichPipelineOptionsQuery__
 *
 * To run a query within a React component, call `useEnrichPipelineOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useEnrichPipelineOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEnrichPipelineOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useEnrichPipelineOptionsQuery(baseOptions?: Apollo.QueryHookOptions<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>(EnrichPipelineOptionsDocument, options);
      }
export function useEnrichPipelineOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>(EnrichPipelineOptionsDocument, options);
        }
export type EnrichPipelineOptionsQueryHookResult = ReturnType<typeof useEnrichPipelineOptionsQuery>;
export type EnrichPipelineOptionsLazyQueryHookResult = ReturnType<typeof useEnrichPipelineOptionsLazyQuery>;
export type EnrichPipelineOptionsQueryResult = Apollo.QueryResult<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>;
export const EnrichItemsDocument = gql`
    query EnrichItems($searchText: String, $after: String) {
  enrichItems(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        type
        serviceName
        jsonConfig
        script
        behaviorMergeType
        jsonPath
        behaviorOnError
        requestTimeout
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useEnrichItemsQuery__
 *
 * To run a query within a React component, call `useEnrichItemsQuery` and pass it any options that fit your needs.
 * When your component renders, `useEnrichItemsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEnrichItemsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useEnrichItemsQuery(baseOptions?: Apollo.QueryHookOptions<EnrichItemsQuery, EnrichItemsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichItemsQuery, EnrichItemsQueryVariables>(EnrichItemsDocument, options);
      }
export function useEnrichItemsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichItemsQuery, EnrichItemsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichItemsQuery, EnrichItemsQueryVariables>(EnrichItemsDocument, options);
        }
export type EnrichItemsQueryHookResult = ReturnType<typeof useEnrichItemsQuery>;
export type EnrichItemsLazyQueryHookResult = ReturnType<typeof useEnrichItemsLazyQuery>;
export type EnrichItemsQueryResult = Apollo.QueryResult<EnrichItemsQuery, EnrichItemsQueryVariables>;
export const PluginDriversDocument = gql`
    query PluginDrivers {
  pluginDriversPageFilter(pageable: {}) {
    content {
      id
      name
      description
      jsonConfig
      provisioning
      type
    }
  }
}
    `;

/**
 * __usePluginDriversQuery__
 *
 * To run a query within a React component, call `usePluginDriversQuery` and pass it any options that fit your needs.
 * When your component renders, `usePluginDriversQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = usePluginDriversQuery({
 *   variables: {
 *   },
 * });
 */
export function usePluginDriversQuery(baseOptions?: Apollo.QueryHookOptions<PluginDriversQuery, PluginDriversQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PluginDriversQuery, PluginDriversQueryVariables>(PluginDriversDocument, options);
      }
export function usePluginDriversLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PluginDriversQuery, PluginDriversQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PluginDriversQuery, PluginDriversQueryVariables>(PluginDriversDocument, options);
        }
export type PluginDriversQueryHookResult = ReturnType<typeof usePluginDriversQuery>;
export type PluginDriversLazyQueryHookResult = ReturnType<typeof usePluginDriversLazyQuery>;
export type PluginDriversQueryResult = Apollo.QueryResult<PluginDriversQuery, PluginDriversQueryVariables>;
export const CreateOrUpdatePluginDriverDocument = gql`
    mutation CreateOrUpdatePluginDriver($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $jsonConfig: String, $provisioning: Provisioning) {
  pluginDriver(
    id: $id
    pluginDriverDTO: {name: $name, description: $description, type: $type, jsonConfig: $jsonConfig, provisioning: $provisioning}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdatePluginDriverMutationFn = Apollo.MutationFunction<CreateOrUpdatePluginDriverMutation, CreateOrUpdatePluginDriverMutationVariables>;

/**
 * __useCreateOrUpdatePluginDriverMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdatePluginDriverMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdatePluginDriverMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdatePluginDriverMutation, { data, loading, error }] = useCreateOrUpdatePluginDriverMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      type: // value for 'type'
 *      jsonConfig: // value for 'jsonConfig'
 *      provisioning: // value for 'provisioning'
 *   },
 * });
 */
export function useCreateOrUpdatePluginDriverMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdatePluginDriverMutation, CreateOrUpdatePluginDriverMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdatePluginDriverMutation, CreateOrUpdatePluginDriverMutationVariables>(CreateOrUpdatePluginDriverDocument, options);
      }
export type CreateOrUpdatePluginDriverMutationHookResult = ReturnType<typeof useCreateOrUpdatePluginDriverMutation>;
export type CreateOrUpdatePluginDriverMutationResult = Apollo.MutationResult<CreateOrUpdatePluginDriverMutation>;
export type CreateOrUpdatePluginDriverMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdatePluginDriverMutation, CreateOrUpdatePluginDriverMutationVariables>;
export const EnrichPipelinesDocument = gql`
    query EnrichPipelines($searchText: String, $after: String) {
  enrichPipelines(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useEnrichPipelinesQuery__
 *
 * To run a query within a React component, call `useEnrichPipelinesQuery` and pass it any options that fit your needs.
 * When your component renders, `useEnrichPipelinesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEnrichPipelinesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useEnrichPipelinesQuery(baseOptions?: Apollo.QueryHookOptions<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>(EnrichPipelinesDocument, options);
      }
export function useEnrichPipelinesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>(EnrichPipelinesDocument, options);
        }
export type EnrichPipelinesQueryHookResult = ReturnType<typeof useEnrichPipelinesQuery>;
export type EnrichPipelinesLazyQueryHookResult = ReturnType<typeof useEnrichPipelinesLazyQuery>;
export type EnrichPipelinesQueryResult = Apollo.QueryResult<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>;
export const EnrichPipelinesValueOptionsDocument = gql`
    query EnrichPipelinesValueOptions($id: BigInteger!) {
  unboundEnrichPipelines(itemId: $id) {
    name
    id
  }
}
    `;

/**
 * __useEnrichPipelinesValueOptionsQuery__
 *
 * To run a query within a React component, call `useEnrichPipelinesValueOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useEnrichPipelinesValueOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEnrichPipelinesValueOptionsQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useEnrichPipelinesValueOptionsQuery(baseOptions: Apollo.QueryHookOptions<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>(EnrichPipelinesValueOptionsDocument, options);
      }
export function useEnrichPipelinesValueOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>(EnrichPipelinesValueOptionsDocument, options);
        }
export type EnrichPipelinesValueOptionsQueryHookResult = ReturnType<typeof useEnrichPipelinesValueOptionsQuery>;
export type EnrichPipelinesValueOptionsLazyQueryHookResult = ReturnType<typeof useEnrichPipelinesValueOptionsLazyQuery>;
export type EnrichPipelinesValueOptionsQueryResult = Apollo.QueryResult<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>;
export const DeleteEnrichPipelineDocument = gql`
    mutation DeleteEnrichPipeline($id: ID!) {
  deleteEnrichPipeline(enrichPipelineId: $id) {
    id
    name
  }
}
    `;
export type DeleteEnrichPipelineMutationFn = Apollo.MutationFunction<DeleteEnrichPipelineMutation, DeleteEnrichPipelineMutationVariables>;

/**
 * __useDeleteEnrichPipelineMutation__
 *
 * To run a mutation, you first call `useDeleteEnrichPipelineMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteEnrichPipelineMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteEnrichPipelineMutation, { data, loading, error }] = useDeleteEnrichPipelineMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteEnrichPipelineMutation(baseOptions?: Apollo.MutationHookOptions<DeleteEnrichPipelineMutation, DeleteEnrichPipelineMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteEnrichPipelineMutation, DeleteEnrichPipelineMutationVariables>(DeleteEnrichPipelineDocument, options);
      }
export type DeleteEnrichPipelineMutationHookResult = ReturnType<typeof useDeleteEnrichPipelineMutation>;
export type DeleteEnrichPipelineMutationResult = Apollo.MutationResult<DeleteEnrichPipelineMutation>;
export type DeleteEnrichPipelineMutationOptions = Apollo.BaseMutationOptions<DeleteEnrichPipelineMutation, DeleteEnrichPipelineMutationVariables>;
export const EnrichPipelineDocument = gql`
    query EnrichPipeline($id: ID!) {
  enrichPipeline(id: $id) {
    id
    name
    description
  }
}
    `;

/**
 * __useEnrichPipelineQuery__
 *
 * To run a query within a React component, call `useEnrichPipelineQuery` and pass it any options that fit your needs.
 * When your component renders, `useEnrichPipelineQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEnrichPipelineQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useEnrichPipelineQuery(baseOptions: Apollo.QueryHookOptions<EnrichPipelineQuery, EnrichPipelineQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichPipelineQuery, EnrichPipelineQueryVariables>(EnrichPipelineDocument, options);
      }
export function useEnrichPipelineLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichPipelineQuery, EnrichPipelineQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichPipelineQuery, EnrichPipelineQueryVariables>(EnrichPipelineDocument, options);
        }
export type EnrichPipelineQueryHookResult = ReturnType<typeof useEnrichPipelineQuery>;
export type EnrichPipelineLazyQueryHookResult = ReturnType<typeof useEnrichPipelineLazyQuery>;
export type EnrichPipelineQueryResult = Apollo.QueryResult<EnrichPipelineQuery, EnrichPipelineQueryVariables>;
export const AssociatedEnrichPipelineEnrichItemsDocument = gql`
    query AssociatedEnrichPipelineEnrichItems($enrichPipelineId: ID!) {
  enrichPipeline(id: $enrichPipelineId) {
    id
    enrichItems {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
}
    `;

/**
 * __useAssociatedEnrichPipelineEnrichItemsQuery__
 *
 * To run a query within a React component, call `useAssociatedEnrichPipelineEnrichItemsQuery` and pass it any options that fit your needs.
 * When your component renders, `useAssociatedEnrichPipelineEnrichItemsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAssociatedEnrichPipelineEnrichItemsQuery({
 *   variables: {
 *      enrichPipelineId: // value for 'enrichPipelineId'
 *   },
 * });
 */
export function useAssociatedEnrichPipelineEnrichItemsQuery(baseOptions: Apollo.QueryHookOptions<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>(AssociatedEnrichPipelineEnrichItemsDocument, options);
      }
export function useAssociatedEnrichPipelineEnrichItemsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>(AssociatedEnrichPipelineEnrichItemsDocument, options);
        }
export type AssociatedEnrichPipelineEnrichItemsQueryHookResult = ReturnType<typeof useAssociatedEnrichPipelineEnrichItemsQuery>;
export type AssociatedEnrichPipelineEnrichItemsLazyQueryHookResult = ReturnType<typeof useAssociatedEnrichPipelineEnrichItemsLazyQuery>;
export type AssociatedEnrichPipelineEnrichItemsQueryResult = Apollo.QueryResult<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>;
export const UnassociatedEnrichPipelineEnrichItemsDocument = gql`
    query UnassociatedEnrichPipelineEnrichItems($enrichPipelineId: ID!, $searchText: String) {
  enrichPipeline(id: $enrichPipelineId) {
    id
    enrichItems(searchText: $searchText, not: true, first: 20) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
}
    `;

/**
 * __useUnassociatedEnrichPipelineEnrichItemsQuery__
 *
 * To run a query within a React component, call `useUnassociatedEnrichPipelineEnrichItemsQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnassociatedEnrichPipelineEnrichItemsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnassociatedEnrichPipelineEnrichItemsQuery({
 *   variables: {
 *      enrichPipelineId: // value for 'enrichPipelineId'
 *      searchText: // value for 'searchText'
 *   },
 * });
 */
export function useUnassociatedEnrichPipelineEnrichItemsQuery(baseOptions: Apollo.QueryHookOptions<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>(UnassociatedEnrichPipelineEnrichItemsDocument, options);
      }
export function useUnassociatedEnrichPipelineEnrichItemsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>(UnassociatedEnrichPipelineEnrichItemsDocument, options);
        }
export type UnassociatedEnrichPipelineEnrichItemsQueryHookResult = ReturnType<typeof useUnassociatedEnrichPipelineEnrichItemsQuery>;
export type UnassociatedEnrichPipelineEnrichItemsLazyQueryHookResult = ReturnType<typeof useUnassociatedEnrichPipelineEnrichItemsLazyQuery>;
export type UnassociatedEnrichPipelineEnrichItemsQueryResult = Apollo.QueryResult<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>;
export const CreateOrUpdateEnrichPipelineDocument = gql`
    mutation CreateOrUpdateEnrichPipeline($id: ID, $name: String!, $description: String) {
  enrichPipeline(
    id: $id
    enrichPipelineDTO: {name: $name, description: $description}
  ) {
    entity {
      id
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateEnrichPipelineMutationFn = Apollo.MutationFunction<CreateOrUpdateEnrichPipelineMutation, CreateOrUpdateEnrichPipelineMutationVariables>;

/**
 * __useCreateOrUpdateEnrichPipelineMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateEnrichPipelineMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateEnrichPipelineMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateEnrichPipelineMutation, { data, loading, error }] = useCreateOrUpdateEnrichPipelineMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *   },
 * });
 */
export function useCreateOrUpdateEnrichPipelineMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateEnrichPipelineMutation, CreateOrUpdateEnrichPipelineMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateEnrichPipelineMutation, CreateOrUpdateEnrichPipelineMutationVariables>(CreateOrUpdateEnrichPipelineDocument, options);
      }
export type CreateOrUpdateEnrichPipelineMutationHookResult = ReturnType<typeof useCreateOrUpdateEnrichPipelineMutation>;
export type CreateOrUpdateEnrichPipelineMutationResult = Apollo.MutationResult<CreateOrUpdateEnrichPipelineMutation>;
export type CreateOrUpdateEnrichPipelineMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateEnrichPipelineMutation, CreateOrUpdateEnrichPipelineMutationVariables>;
export const AddEnrichItemToEnrichPipelineDocument = gql`
    mutation AddEnrichItemToEnrichPipeline($childId: ID!, $parentId: ID!) {
  addEnrichItemToEnrichPipeline(
    enrichItemId: $childId
    enrichPipelineId: $parentId
  ) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddEnrichItemToEnrichPipelineMutationFn = Apollo.MutationFunction<AddEnrichItemToEnrichPipelineMutation, AddEnrichItemToEnrichPipelineMutationVariables>;

/**
 * __useAddEnrichItemToEnrichPipelineMutation__
 *
 * To run a mutation, you first call `useAddEnrichItemToEnrichPipelineMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddEnrichItemToEnrichPipelineMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addEnrichItemToEnrichPipelineMutation, { data, loading, error }] = useAddEnrichItemToEnrichPipelineMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddEnrichItemToEnrichPipelineMutation(baseOptions?: Apollo.MutationHookOptions<AddEnrichItemToEnrichPipelineMutation, AddEnrichItemToEnrichPipelineMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddEnrichItemToEnrichPipelineMutation, AddEnrichItemToEnrichPipelineMutationVariables>(AddEnrichItemToEnrichPipelineDocument, options);
      }
export type AddEnrichItemToEnrichPipelineMutationHookResult = ReturnType<typeof useAddEnrichItemToEnrichPipelineMutation>;
export type AddEnrichItemToEnrichPipelineMutationResult = Apollo.MutationResult<AddEnrichItemToEnrichPipelineMutation>;
export type AddEnrichItemToEnrichPipelineMutationOptions = Apollo.BaseMutationOptions<AddEnrichItemToEnrichPipelineMutation, AddEnrichItemToEnrichPipelineMutationVariables>;
export const RemoveEnrichItemFromEnrichPipelineDocument = gql`
    mutation RemoveEnrichItemFromEnrichPipeline($childId: ID!, $parentId: ID!) {
  removeEnrichItemFromEnrichPipeline(
    enrichItemId: $childId
    enrichPipelineId: $parentId
  ) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveEnrichItemFromEnrichPipelineMutationFn = Apollo.MutationFunction<RemoveEnrichItemFromEnrichPipelineMutation, RemoveEnrichItemFromEnrichPipelineMutationVariables>;

/**
 * __useRemoveEnrichItemFromEnrichPipelineMutation__
 *
 * To run a mutation, you first call `useRemoveEnrichItemFromEnrichPipelineMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveEnrichItemFromEnrichPipelineMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeEnrichItemFromEnrichPipelineMutation, { data, loading, error }] = useRemoveEnrichItemFromEnrichPipelineMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveEnrichItemFromEnrichPipelineMutation(baseOptions?: Apollo.MutationHookOptions<RemoveEnrichItemFromEnrichPipelineMutation, RemoveEnrichItemFromEnrichPipelineMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveEnrichItemFromEnrichPipelineMutation, RemoveEnrichItemFromEnrichPipelineMutationVariables>(RemoveEnrichItemFromEnrichPipelineDocument, options);
      }
export type RemoveEnrichItemFromEnrichPipelineMutationHookResult = ReturnType<typeof useRemoveEnrichItemFromEnrichPipelineMutation>;
export type RemoveEnrichItemFromEnrichPipelineMutationResult = Apollo.MutationResult<RemoveEnrichItemFromEnrichPipelineMutation>;
export type RemoveEnrichItemFromEnrichPipelineMutationOptions = Apollo.BaseMutationOptions<RemoveEnrichItemFromEnrichPipelineMutation, RemoveEnrichItemFromEnrichPipelineMutationVariables>;
export const SortEnrichItemsDocument = gql`
    mutation SortEnrichItems($enrichPipelineId: ID!, $enrichItemIdList: [BigInteger]) {
  sortEnrichItems(
    enrichPipelineId: $enrichPipelineId
    enrichItemIdList: $enrichItemIdList
  ) {
    id
    enrichItems {
      edges {
        node {
          id
          name
          description
        }
      }
    }
  }
}
    `;
export type SortEnrichItemsMutationFn = Apollo.MutationFunction<SortEnrichItemsMutation, SortEnrichItemsMutationVariables>;

/**
 * __useSortEnrichItemsMutation__
 *
 * To run a mutation, you first call `useSortEnrichItemsMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSortEnrichItemsMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [sortEnrichItemsMutation, { data, loading, error }] = useSortEnrichItemsMutation({
 *   variables: {
 *      enrichPipelineId: // value for 'enrichPipelineId'
 *      enrichItemIdList: // value for 'enrichItemIdList'
 *   },
 * });
 */
export function useSortEnrichItemsMutation(baseOptions?: Apollo.MutationHookOptions<SortEnrichItemsMutation, SortEnrichItemsMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SortEnrichItemsMutation, SortEnrichItemsMutationVariables>(SortEnrichItemsDocument, options);
      }
export type SortEnrichItemsMutationHookResult = ReturnType<typeof useSortEnrichItemsMutation>;
export type SortEnrichItemsMutationResult = Apollo.MutationResult<SortEnrichItemsMutation>;
export type SortEnrichItemsMutationOptions = Apollo.BaseMutationOptions<SortEnrichItemsMutation, SortEnrichItemsMutationVariables>;
export const EnrichPipelineWithItemsDocument = gql`
    mutation EnrichPipelineWithItems($id: ID, $items: [ItemDTOInput], $name: String!, $description: String!) {
  enrichPipelineWithEnrichItems(
    id: $id
    pipelineWithItemsDTO: {items: $items, name: $name, description: $description}
  ) {
    entity {
      name
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type EnrichPipelineWithItemsMutationFn = Apollo.MutationFunction<EnrichPipelineWithItemsMutation, EnrichPipelineWithItemsMutationVariables>;

/**
 * __useEnrichPipelineWithItemsMutation__
 *
 * To run a mutation, you first call `useEnrichPipelineWithItemsMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useEnrichPipelineWithItemsMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [enrichPipelineWithItemsMutation, { data, loading, error }] = useEnrichPipelineWithItemsMutation({
 *   variables: {
 *      id: // value for 'id'
 *      items: // value for 'items'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *   },
 * });
 */
export function useEnrichPipelineWithItemsMutation(baseOptions?: Apollo.MutationHookOptions<EnrichPipelineWithItemsMutation, EnrichPipelineWithItemsMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<EnrichPipelineWithItemsMutation, EnrichPipelineWithItemsMutationVariables>(EnrichPipelineWithItemsDocument, options);
      }
export type EnrichPipelineWithItemsMutationHookResult = ReturnType<typeof useEnrichPipelineWithItemsMutation>;
export type EnrichPipelineWithItemsMutationResult = Apollo.MutationResult<EnrichPipelineWithItemsMutation>;
export type EnrichPipelineWithItemsMutationOptions = Apollo.BaseMutationOptions<EnrichPipelineWithItemsMutation, EnrichPipelineWithItemsMutationVariables>;
// Generated on 2025-09-29T12:33:21+02:00
