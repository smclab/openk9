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

export type Annotator = {
  __typename?: 'Annotator';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  docTypeField?: Maybe<DocTypeField>;
  docTypeFieldNotInAnnotator?: Maybe<Connection_DocTypeField>;
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
  Ner = 'NER',
  NerAutocomplete = 'NER_AUTOCOMPLETE',
  Stopword = 'STOPWORD',
  Token = 'TOKEN'
}

export enum BehaviorMergeType {
  Merge = 'MERGE',
  Replace = 'REPLACE'
}

export type Bucket = {
  __typename?: 'Bucket';
  catIndices?: Maybe<Array<Maybe<CatResponse>>>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  datasources?: Maybe<Connection_Datasource>;
  description?: Maybe<Scalars['String']>;
  docCount?: Maybe<Scalars['BigInteger']>;
  enabled?: Maybe<Scalars['Boolean']>;
  id?: Maybe<Scalars['ID']>;
  indexCount?: Maybe<Scalars['BigInteger']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  queryAnalysis?: Maybe<QueryAnalysis>;
  searchConfig?: Maybe<SearchConfig>;
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
};

export type CatResponse = {
  __typename?: 'CatResponse';
  docsCount?: Maybe<Scalars['String']>;
  docsDeleted?: Maybe<Scalars['String']>;
  health?: Maybe<Scalars['String']>;
  index?: Maybe<Scalars['String']>;
  pri?: Maybe<Scalars['String']>;
  priStoreSize?: Maybe<Scalars['String']>;
  rep?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  storeSize?: Maybe<Scalars['String']>;
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
};

export type CharFilterDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
};

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
export type Connection_Rule = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Rule>>>;
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

export type DataIndex = {
  __typename?: 'DataIndex';
  cat?: Maybe<CatResponse>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  docCount?: Maybe<Scalars['BigInteger']>;
  docTypes?: Maybe<Connection_DocType>;
  id?: Maybe<Scalars['ID']>;
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
  description?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
};

export type Datasource = {
  __typename?: 'Datasource';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  dataIndex?: Maybe<DataIndex>;
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
  /** If true set datasource as schedulable */
  schedulable?: Maybe<Scalars['Boolean']>;
  /** Chron quartz expression to define scheduling of datasource */
  scheduling?: Maybe<Scalars['String']>;
};

export type DatasourceDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  /** Json configuration with custom fields for datasource */
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  /** If true datasource is scheduled based on defined scheduling expression */
  schedulable: Scalars['Boolean'];
  /** Chron quartz expression to define scheduling of datasource */
  scheduling: Scalars['String'];
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

export type DefaultConnection_Rule = Connection_Rule & {
  __typename?: 'DefaultConnection_Rule';
  edges?: Maybe<Array<Maybe<Edge_Rule>>>;
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

export type DefaultEdge_Rule = Edge_Rule & {
  __typename?: 'DefaultEdge_Rule';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Rule>;
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
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  keyword: Scalars['Boolean'];
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  numeric: Scalars['Boolean'];
  parent?: Maybe<DocTypeField>;
  searchable: Scalars['Boolean'];
  searchableAndAutocomplete: Scalars['Boolean'];
  searchableAndDate: Scalars['Boolean'];
  searchableAndText: Scalars['Boolean'];
  sortable: Scalars['Boolean'];
  subFields?: Maybe<Connection_DocTypeField>;
  text: Scalars['Boolean'];
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
export type Edge_Rule = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Rule>;
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

export type EnrichItem = {
  __typename?: 'EnrichItem';
  behaviorMergeType?: Maybe<BehaviorMergeType>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['ID']>;
  jsonConfig?: Maybe<Scalars['String']>;
  jsonPath?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  serviceName?: Maybe<Scalars['String']>;
  type?: Maybe<EnrichItemType>;
  validationScript?: Maybe<Scalars['String']>;
};

export type EnrichItemDtoInput = {
  behaviorMergeType: BehaviorMergeType;
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  jsonPath: Scalars['String'];
  name: Scalars['String'];
  serviceName: Scalars['String'];
  type: EnrichItemType;
  validationScript?: InputMaybe<Scalars['String']>;
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
  Integer = 'INTEGER',
  Ip = 'IP',
  IpRange = 'IP_RANGE',
  Join = 'JOIN',
  Keyword = 'KEYWORD',
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

export enum Fuzziness {
  Auto = 'AUTO',
  One = 'ONE',
  Two = 'TWO',
  Zero = 'ZERO'
}

/** Mutation root */
export type Mutation = {
  __typename?: 'Mutation';
  addAnnotatorToQueryAnalysis?: Maybe<Tuple2_QueryAnalysis_Annotator>;
  addCharFilterToAnalyzer?: Maybe<Tuple2_Analyzer_CharFilter>;
  addDatasourceToBucket?: Maybe<Tuple2_Bucket_Datasource>;
  addDocTypeFieldToPluginDriver?: Maybe<Tuple2_PluginDriver_DocTypeField>;
  addDocTypeFieldToSuggestionCategory?: Maybe<Tuple2_SuggestionCategory_DocTypeField>;
  addDocTypeToDataIndex?: Maybe<Tuple2_DataIndex_DocType>;
  addEnrichItemToEnrichPipeline?: Maybe<Tuple2_EnrichPipeline_EnrichItem>;
  addRuleToQueryAnalysis?: Maybe<Tuple2_QueryAnalysis_Rule>;
  addSuggestionCategoryToBucket?: Maybe<Tuple2_Bucket_SuggestionCategory>;
  addTabToBucket?: Maybe<Tuple2_Bucket_Tab>;
  addTokenFilterToAnalyzer?: Maybe<Tuple2_Analyzer_TokenFilter>;
  analyzer?: Maybe<Response_Analyzer>;
  annotator?: Maybe<Response_Annotator>;
  bindAnalyzerToDocTypeField?: Maybe<Tuple2_DocTypeField_Analyzer>;
  bindAnnotatorToDocTypeField?: Maybe<Tuple2_Annotator_DocTypeField>;
  bindDataIndexToDatasource?: Maybe<Tuple2_Datasource_DataIndex>;
  bindDocTypeFieldToTokenTab?: Maybe<Tuple2_TokenTab_DocTypeField>;
  bindDocTypeToDocTypeTemplate?: Maybe<Tuple2_DocType_DocTypeTemplate>;
  bindEnrichPipelineToDatasource?: Maybe<Tuple2_Datasource_EnrichPipeline>;
  bindPluginDriverToDatasource?: Maybe<Tuple2_Datasource_PluginDriver>;
  bindQueryAnalysisToBucket?: Maybe<Tuple2_Bucket_QueryAnalysis>;
  bindSearchConfigToBucket?: Maybe<Tuple2_Bucket_SearchConfig>;
  bindTokenizerToAnalyzer?: Maybe<Tuple2_Analyzer_Tokenizer>;
  bucket?: Maybe<Response_Bucket>;
  charFilter?: Maybe<Response_CharFilter>;
  createDatasourceAndAddPluginDriver?: Maybe<Tuple2_Datasource_PluginDriver>;
  createSubField?: Maybe<Response_DocTypeField>;
  dataIndex?: Maybe<Response_DataIndex>;
  datasource?: Maybe<Response_Datasource>;
  deleteAnalyzer?: Maybe<Analyzer>;
  deleteAnnotator?: Maybe<Annotator>;
  deleteBucket?: Maybe<Bucket>;
  deleteCharFilter?: Maybe<CharFilter>;
  deleteDataIndex?: Maybe<DataIndex>;
  deleteDatasource?: Maybe<Datasource>;
  deleteDocType?: Maybe<DocType>;
  deleteDocTypeTemplate?: Maybe<DocTypeTemplate>;
  deleteEnrichItem?: Maybe<EnrichItem>;
  deleteEnrichPipeline?: Maybe<EnrichPipeline>;
  deletePluginDriver?: Maybe<PluginDriver>;
  deleteQueryAnalysis?: Maybe<QueryAnalysis>;
  deleteRule?: Maybe<Rule>;
  deleteSearchConfig?: Maybe<SearchConfig>;
  deleteSuggestionCategory?: Maybe<SuggestionCategory>;
  deleteTab?: Maybe<Tab>;
  deleteTokenFilter?: Maybe<TokenFilter>;
  deleteTokenizer?: Maybe<Tokenizer>;
  docType?: Maybe<Response_DocType>;
  docTypeField?: Maybe<Response_DocTypeField>;
  docTypeTemplate?: Maybe<Response_DocTypeTemplate>;
  enableBucket?: Maybe<Bucket>;
  enrichItem?: Maybe<Response_EnrichItem>;
  enrichPipeline?: Maybe<Response_EnrichPipeline>;
  multiSelect?: Maybe<SuggestionCategory>;
  pluginDriver?: Maybe<Response_PluginDriver>;
  queryAnalysis?: Maybe<Response_QueryAnalysis>;
  queryParserConfig?: Maybe<Response_QueryParserConfig>;
  removeAnnotatorFromQueryAnalysis?: Maybe<Tuple2_QueryAnalysis_Annotator>;
  removeCharFilterFromAnalyzer?: Maybe<Tuple2_Analyzer_CharFilter>;
  removeCharFilterListFromAnalyzer?: Maybe<Analyzer>;
  removeDatasourceFromBucket?: Maybe<Tuple2_Bucket_Datasource>;
  removeDocTypeField?: Maybe<Tuple2_DocType_BigInteger>;
  removeDocTypeFieldFromPluginDriver?: Maybe<Tuple2_PluginDriver_DocTypeField>;
  removeDocTypeFieldFromSuggestionCategory?: Maybe<Tuple2_SuggestionCategory_DocTypeField>;
  removeDocTypeFromDataIndex?: Maybe<Tuple2_DataIndex_DocType>;
  removeEnrichItemFromEnrichPipeline?: Maybe<Tuple2_EnrichPipeline_EnrichItem>;
  removeQueryParserConfig?: Maybe<Tuple2_SearchConfig_BigInteger>;
  removeRuleFromQueryAnalysis?: Maybe<Tuple2_QueryAnalysis_Rule>;
  removeSuggestionCategoryFromBucket?: Maybe<Tuple2_Bucket_SuggestionCategory>;
  removeTabFromBucket?: Maybe<Tuple2_Bucket_Tab>;
  removeTokenFilterFromAnalyzer?: Maybe<Tuple2_Analyzer_TokenFilter>;
  removeTokenFilterListFromAnalyzer?: Maybe<Analyzer>;
  removeTokenTab?: Maybe<Tuple2_Tab_BigInteger>;
  rule?: Maybe<Response_Rule>;
  searchConfig?: Maybe<Response_SearchConfig>;
  sortEnrichItems?: Maybe<EnrichPipeline>;
  suggestionCategory?: Maybe<Response_SuggestionCategory>;
  tab?: Maybe<Response_Tab>;
  tokenFilter?: Maybe<Response_TokenFilter>;
  tokenTab?: Maybe<Response_TokenTab>;
  tokenizer?: Maybe<Response_Tokenizer>;
  unbindAnalyzerFromDocTypeField?: Maybe<Tuple2_DocTypeField_Analyzer>;
  unbindAnnotatorFromDocTypeField?: Maybe<Tuple2_Annotator_DocTypeField>;
  unbindDataIndexFromDatasource?: Maybe<Datasource>;
  unbindDocTypeFieldFromTokenTab?: Maybe<Tuple2_TokenTab_DocTypeField>;
  unbindDocTypeTemplateFromDocType?: Maybe<DocType>;
  unbindEnrichPipelineToDatasource?: Maybe<Datasource>;
  unbindPluginDriverToDatasource?: Maybe<Datasource>;
  unbindQueryAnalysisFromBucket?: Maybe<Tuple2_Bucket_QueryAnalysis>;
  unbindSearchConfigFromBucket?: Maybe<Tuple2_Bucket_SearchConfig>;
  unbindTokenizerFromAnalyzer?: Maybe<Tuple2_Analyzer_Tokenizer>;
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
export type MutationAddRuleToQueryAnalysisArgs = {
  id: Scalars['ID'];
  ruleId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddSuggestionCategoryToBucketArgs = {
  bucketId: Scalars['ID'];
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddTabToBucketArgs = {
  id: Scalars['ID'];
  tabId: Scalars['ID'];
};


/** Mutation root */
export type MutationAddTokenFilterToAnalyzerArgs = {
  id: Scalars['ID'];
  tokenFilterId: Scalars['ID'];
};


/** Mutation root */
export type MutationAnalyzerArgs = {
  analyzerDTO?: InputMaybe<AnalyzerDtoInput>;
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
export type MutationCreateSubFieldArgs = {
  docTypeFieldDTO?: InputMaybe<DocTypeFieldDtoInput>;
  parentDocTypeFieldId: Scalars['ID'];
};


/** Mutation root */
export type MutationDataIndexArgs = {
  dataIndexDTO?: InputMaybe<DataIndexDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
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
};


/** Mutation root */
export type MutationDeleteDocTypeArgs = {
  docTypeId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteDocTypeTemplateArgs = {
  docTypeTemplateId: Scalars['ID'];
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
export type MutationDeletePluginDriverArgs = {
  pluginDriverId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteQueryAnalysisArgs = {
  queryAnalysisId: Scalars['ID'];
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
export type MutationDeleteSuggestionCategoryArgs = {
  suggestionCategoryId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteTabArgs = {
  tabId: Scalars['ID'];
};


/** Mutation root */
export type MutationDeleteTokenFilterArgs = {
  tokenFilterId: Scalars['ID'];
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
export type MutationDocTypeTemplateArgs = {
  docTypeTemplateDTO?: InputMaybe<DocTypeTemplateDtoInput>;
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationEnableBucketArgs = {
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
export type MutationQueryAnalysisArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  queryAnalysisDTO?: InputMaybe<QueryAnalysisDtoInput>;
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
export type MutationRemoveTokenTabArgs = {
  tabId: Scalars['ID'];
  tokenTabId?: InputMaybe<Scalars['ID']>;
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
export type MutationSortEnrichItemsArgs = {
  enrichItemIdList?: InputMaybe<Array<InputMaybe<Scalars['BigInteger']>>>;
  enrichPipelineId: Scalars['ID'];
};


/** Mutation root */
export type MutationSuggestionCategoryArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  suggestionCategoryDTO?: InputMaybe<SuggestionCategoryDtoInput>;
};


/** Mutation root */
export type MutationTabArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  tabDTO?: InputMaybe<TabDtoInput>;
};


/** Mutation root */
export type MutationTokenFilterArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  tokenFilterDTO?: InputMaybe<TokenFilterDtoInput>;
};


/** Mutation root */
export type MutationTokenTabArgs = {
  patch?: InputMaybe<Scalars['Boolean']>;
  tabId: Scalars['ID'];
  tokenTabDTO?: InputMaybe<TokenTabDtoInput>;
  tokenTabId?: InputMaybe<Scalars['ID']>;
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
export type MutationUnbindPluginDriverToDatasourceArgs = {
  datasourceId: Scalars['ID'];
};


/** Mutation root */
export type MutationUnbindQueryAnalysisFromBucketArgs = {
  bucketId: Scalars['ID'];
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
export type MutationUserFieldArgs = {
  docTypeFieldId: Scalars['ID'];
  pluginDriverId: Scalars['ID'];
  userField?: InputMaybe<UserField>;
};

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
  docTypeFieldsFromDocType?: Maybe<Connection_DocTypeField>;
  docTypeFieldsNotInTokenTab?: Maybe<Connection_DocTypeField>;
  docTypeTemplate?: Maybe<DocTypeTemplate>;
  docTypeTemplates?: Maybe<Connection_DocTypeTemplate>;
  docTypes?: Maybe<Connection_DocType>;
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
  pluginDriver?: Maybe<PluginDriver>;
  pluginDrivers?: Maybe<Connection_PluginDriver>;
  queryAnalyses?: Maybe<Connection_QueryAnalysis>;
  queryAnalysis?: Maybe<QueryAnalysis>;
  queryParserConfig?: Maybe<QueryParserConfig>;
  queryParserConfigs?: Maybe<Connection_QueryParserConfig>;
  rule?: Maybe<Rule>;
  rules?: Maybe<Connection_Rule>;
  searchConfig?: Maybe<SearchConfig>;
  searchConfigs?: Maybe<Connection_SearchConfig>;
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
  type?: Maybe<Scalars['String']>;
};

export type QueryParserConfigDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
  type: Scalars['String'];
};

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
  enrichItemCreated?: Maybe<EnrichItem>;
  enrichItemDeleted?: Maybe<EnrichItem>;
  enrichItemUpdated?: Maybe<EnrichItem>;
  enrichPipelineCreated?: Maybe<EnrichPipeline>;
  enrichPipelineDeleted?: Maybe<EnrichPipeline>;
  enrichPipelineUpdated?: Maybe<EnrichPipeline>;
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
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  docTypeFields?: Maybe<Connection_DocTypeField>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  multiSelect: Scalars['Boolean'];
  name?: Maybe<Scalars['String']>;
  priority?: Maybe<Scalars['Float']>;
};


export type SuggestionCategoryDocTypeFieldsArgs = {
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
  tokenTabs?: Maybe<Connection_TokenTab>;
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
};

export type TokenFilterDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
};

export type TokenTab = {
  __typename?: 'TokenTab';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  description?: Maybe<Scalars['String']>;
  docTypeField?: Maybe<DocTypeField>;
  docTypeFieldsNotInTokenTab?: Maybe<Connection_DocTypeField>;
  filter?: Maybe<Scalars['Boolean']>;
  id?: Maybe<Scalars['ID']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  tab?: Maybe<Tab>;
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
  filter: Scalars['Boolean'];
  name: Scalars['String'];
  tokenType: TokenType;
  value: Scalars['String'];
};

export enum TokenType {
  Autocomplete = 'AUTOCOMPLETE',
  Date = 'DATE',
  Doctype = 'DOCTYPE',
  Entity = 'ENTITY',
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
};

export type TokenizerDtoInput = {
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
  name: Scalars['String'];
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

export type Tuple2_Bucket_QueryAnalysis = {
  __typename?: 'Tuple2_Bucket_QueryAnalysis';
  left?: Maybe<Bucket>;
  right?: Maybe<QueryAnalysis>;
};

export type Tuple2_Bucket_SearchConfig = {
  __typename?: 'Tuple2_Bucket_SearchConfig';
  left?: Maybe<Bucket>;
  right?: Maybe<SearchConfig>;
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

export type Tuple2_SuggestionCategory_DocTypeField = {
  __typename?: 'Tuple2_SuggestionCategory_DocTypeField';
  left?: Maybe<SuggestionCategory>;
  right?: Maybe<DocTypeField>;
};

export type Tuple2_Tab_BigInteger = {
  __typename?: 'Tuple2_Tab_BigInteger';
  left?: Maybe<Tab>;
  right?: Maybe<Scalars['BigInteger']>;
};

export type Tuple2_TokenTab_DocTypeField = {
  __typename?: 'Tuple2_TokenTab_DocTypeField';
  left?: Maybe<TokenTab>;
  right?: Maybe<DocTypeField>;
};

export enum UserField {
  Email = 'EMAIL',
  Name = 'NAME',
  NameSurname = 'NAME_SURNAME',
  Roles = 'ROLES',
  Surname = 'SURNAME',
  Username = 'USERNAME'
}

export type AnalyzerQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type AnalyzerQuery = { __typename?: 'Query', analyzer?: { __typename?: 'Analyzer', id?: string | null, name?: string | null, type?: string | null, description?: string | null, jsonConfig?: string | null, tokenizer?: { __typename?: 'Tokenizer', id?: string | null } | null } | null };

export type CreateOrUpdateAnalyzerMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateAnalyzerMutation = { __typename?: 'Mutation', analyzer?: { __typename?: 'Response_Analyzer', entity?: { __typename?: 'Analyzer', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type TokenizerOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type TokenizerOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_Tokenizer', edges?: Array<{ __typename?: 'DefaultEdge_Tokenizer', node?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type TokenizerValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TokenizerValueQuery = { __typename?: 'Query', value?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null, description?: string | null } | null };

export type BindTokenizerToAnalyzerMutationVariables = Exact<{
  analyzerId: Scalars['ID'];
  tokenizerId: Scalars['ID'];
}>;


export type BindTokenizerToAnalyzerMutation = { __typename?: 'Mutation', bindTokenizerToAnalyzer?: { __typename?: 'Tuple2_Analyzer_Tokenizer', left?: { __typename?: 'Analyzer', id?: string | null, tokenizer?: { __typename?: 'Tokenizer', id?: string | null } | null } | null, right?: { __typename?: 'Tokenizer', id?: string | null } | null } | null };

export type UnbindnTokenizerToAnalyzerMutationVariables = Exact<{
  analyzerId: Scalars['ID'];
}>;


export type UnbindnTokenizerToAnalyzerMutation = { __typename?: 'Mutation', unbindTokenizerFromAnalyzer?: { __typename?: 'Tuple2_Analyzer_Tokenizer', left?: { __typename?: 'Analyzer', id?: string | null } | null } | null };

export type AnalyzerCharFiltersQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type AnalyzerCharFiltersQuery = { __typename?: 'Query', analyzer?: { __typename?: 'Analyzer', id?: string | null, charFilters?: { __typename?: 'DefaultConnection_CharFilter', edges?: Array<{ __typename?: 'DefaultEdge_CharFilter', node?: { __typename?: 'CharFilter', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type AddCharFiltersToAnalyzerMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddCharFiltersToAnalyzerMutation = { __typename?: 'Mutation', addCharFilterToAnalyzer?: { __typename?: 'Tuple2_Analyzer_CharFilter', left?: { __typename?: 'Analyzer', id?: string | null } | null, right?: { __typename?: 'CharFilter', id?: string | null } | null } | null };

export type RemoveCharFiltersToAnalyzerMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveCharFiltersToAnalyzerMutation = { __typename?: 'Mutation', removeCharFilterFromAnalyzer?: { __typename?: 'Tuple2_Analyzer_CharFilter', left?: { __typename?: 'Analyzer', id?: string | null } | null, right?: { __typename?: 'CharFilter', id?: string | null } | null } | null };

export type AnalyzerTokenFiltersQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type AnalyzerTokenFiltersQuery = { __typename?: 'Query', analyzer?: { __typename?: 'Analyzer', id?: string | null, tokenFilters?: { __typename?: 'DefaultConnection_TokenFilter', edges?: Array<{ __typename?: 'DefaultEdge_TokenFilter', node?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type AddTokenFilterToAnalyzerMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddTokenFilterToAnalyzerMutation = { __typename?: 'Mutation', addTokenFilterToAnalyzer?: { __typename?: 'Tuple2_Analyzer_TokenFilter', left?: { __typename?: 'Analyzer', id?: string | null } | null, right?: { __typename?: 'TokenFilter', id?: string | null } | null } | null };

export type RemoveTokenFilterToAnalyzerMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveTokenFilterToAnalyzerMutation = { __typename?: 'Mutation', removeTokenFilterFromAnalyzer?: { __typename?: 'Tuple2_Analyzer_TokenFilter', left?: { __typename?: 'Analyzer', id?: string | null } | null, right?: { __typename?: 'TokenFilter', id?: string | null } | null } | null };

export type AnalyzersQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type AnalyzersQuery = { __typename?: 'Query', analyzers?: { __typename?: 'DefaultConnection_Analyzer', edges?: Array<{ __typename?: 'DefaultEdge_Analyzer', node?: { __typename?: 'Analyzer', id?: string | null, name?: string | null, description?: string | null, type?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteAnalyzersMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteAnalyzersMutation = { __typename?: 'Mutation', deleteAnalyzer?: { __typename?: 'Analyzer', id?: string | null, name?: string | null } | null };

export type AnnotatorQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type AnnotatorQuery = { __typename?: 'Query', annotator?: { __typename?: 'Annotator', id?: string | null, fuziness?: Fuzziness | null, size?: number | null, type?: AnnotatorType | null, description?: string | null, name?: string | null, fieldName?: string | null, docTypeField?: { __typename?: 'DocTypeField', id?: string | null } | null } | null };

export type CreateOrUpdateAnnotatorMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  fieldName: Scalars['String'];
  fuziness: Fuzziness;
  type: AnnotatorType;
  description?: InputMaybe<Scalars['String']>;
  size?: InputMaybe<Scalars['Int']>;
  name: Scalars['String'];
}>;


export type CreateOrUpdateAnnotatorMutation = { __typename?: 'Mutation', annotator?: { __typename?: 'Response_Annotator', entity?: { __typename?: 'Annotator', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DocTypeFieldOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
  annotatorId: Scalars['ID'];
}>;


export type DocTypeFieldOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DocTypeFieldValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DocTypeFieldValueQuery = { __typename?: 'Query', value?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null } | null };

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

export type AnnotatorsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type AnnotatorsQuery = { __typename?: 'Query', annotators?: { __typename?: 'DefaultConnection_Annotator', edges?: Array<{ __typename?: 'DefaultEdge_Annotator', node?: { __typename?: 'Annotator', id?: string | null, name?: string | null, description?: string | null, size?: number | null, type?: AnnotatorType | null, fieldName?: string | null, fuziness?: Fuzziness | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteAnnotatosMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteAnnotatosMutation = { __typename?: 'Mutation', deleteAnnotator?: { __typename?: 'Annotator', id?: string | null, name?: string | null } | null };

export type BucketQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type BucketQuery = { __typename?: 'Query', bucket?: { __typename?: 'Bucket', id?: string | null, name?: string | null, description?: string | null, enabled?: boolean | null, queryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null } | null, searchConfig?: { __typename?: 'SearchConfig', id?: string | null } | null } | null };

export type EnableBucketMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EnableBucketMutation = { __typename?: 'Mutation', enableBucket?: { __typename?: 'Bucket', id?: string | null, name?: string | null } | null };

export type CreateOrUpdateBucketMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateBucketMutation = { __typename?: 'Mutation', bucket?: { __typename?: 'Response_Bucket', entity?: { __typename?: 'Bucket', id?: string | null, name?: string | null, enabled?: boolean | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

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

export type SearchConfigOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type SearchConfigOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_SearchConfig', edges?: Array<{ __typename?: 'DefaultEdge_SearchConfig', node?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type SearchConfigValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type SearchConfigValueQuery = { __typename?: 'Query', value?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, description?: string | null } | null };

export type BindSearchConfigToBucketMutationVariables = Exact<{
  bucketId: Scalars['ID'];
  searchConfigId: Scalars['ID'];
}>;


export type BindSearchConfigToBucketMutation = { __typename?: 'Mutation', bindSearchConfigToBucket?: { __typename?: 'Tuple2_Bucket_SearchConfig', left?: { __typename?: 'Bucket', id?: string | null, searchConfig?: { __typename?: 'SearchConfig', id?: string | null } | null } | null, right?: { __typename?: 'SearchConfig', id?: string | null } | null } | null };

export type UnbindSearchConfigFromBucketMutationVariables = Exact<{
  bucketId: Scalars['ID'];
}>;


export type UnbindSearchConfigFromBucketMutation = { __typename?: 'Mutation', unbindSearchConfigFromBucket?: { __typename?: 'Tuple2_Bucket_SearchConfig', right?: { __typename?: 'SearchConfig', id?: string | null } | null } | null };

export type BucketDataSourcesQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type BucketDataSourcesQuery = { __typename?: 'Query', bucket?: { __typename?: 'Bucket', id?: string | null, datasources?: { __typename?: 'DefaultConnection_Datasource', edges?: Array<{ __typename?: 'DefaultEdge_Datasource', node?: { __typename?: 'Datasource', id?: string | null, name?: string | null, description?: string | null, schedulable?: boolean | null, lastIngestionDate?: any | null, scheduling?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

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

export type BucketsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type BucketsQuery = { __typename?: 'Query', buckets?: { __typename?: 'DefaultConnection_Bucket', edges?: Array<{ __typename?: 'DefaultEdge_Bucket', node?: { __typename?: 'Bucket', id?: string | null, name?: string | null, description?: string | null, enabled?: boolean | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteBucketMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteBucketMutation = { __typename?: 'Mutation', deleteBucket?: { __typename?: 'Bucket', id?: string | null, name?: string | null } | null };

export type CharFilterQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type CharFilterQuery = { __typename?: 'Query', charFilter?: { __typename?: 'CharFilter', id?: string | null, name?: string | null, description?: string | null, jsonConfig?: string | null } | null };

export type CreateOrUpdateCharFilterMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateCharFilterMutation = { __typename?: 'Mutation', charFilter?: { __typename?: 'Response_CharFilter', entity?: { __typename?: 'CharFilter', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type CharfiltersQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type CharfiltersQuery = { __typename?: 'Query', charFilters?: { __typename?: 'DefaultConnection_CharFilter', edges?: Array<{ __typename?: 'DefaultEdge_CharFilter', node?: { __typename?: 'CharFilter', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteCharFiltersMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteCharFiltersMutation = { __typename?: 'Mutation', deleteCharFilter?: { __typename?: 'CharFilter', id?: string | null, name?: string | null } | null };

export type DataSourceQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DataSourceQuery = { __typename?: 'Query', datasource?: { __typename?: 'Datasource', id?: string | null, name?: string | null, description?: string | null, schedulable?: boolean | null, scheduling?: string | null, jsonConfig?: string | null, pluginDriver?: { __typename?: 'PluginDriver', id?: string | null } | null, dataIndex?: { __typename?: 'DataIndex', id?: string | null } | null, enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null } | null } | null };

export type CreateOrUpdateDataSourceMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  schedulable: Scalars['Boolean'];
  scheduling: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateDataSourceMutation = { __typename?: 'Mutation', datasource?: { __typename?: 'Response_Datasource', entity?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type PluginDriverOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type PluginDriverOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_PluginDriver', edges?: Array<{ __typename?: 'DefaultEdge_PluginDriver', node?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type PluginDriverValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type PluginDriverValueQuery = { __typename?: 'Query', value?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null, description?: string | null } | null };

export type BindPluginDriverToDataSourceMutationVariables = Exact<{
  datasourceId: Scalars['ID'];
  pluginDriverId: Scalars['ID'];
}>;


export type BindPluginDriverToDataSourceMutation = { __typename?: 'Mutation', bindPluginDriverToDatasource?: { __typename?: 'Tuple2_Datasource_PluginDriver', left?: { __typename?: 'Datasource', id?: string | null, pluginDriver?: { __typename?: 'PluginDriver', id?: string | null } | null } | null, right?: { __typename?: 'PluginDriver', id?: string | null } | null } | null };

export type UnbindPluginDriverFromDataSourceMutationVariables = Exact<{
  datasourceId: Scalars['ID'];
}>;


export type UnbindPluginDriverFromDataSourceMutation = { __typename?: 'Mutation', unbindPluginDriverToDatasource?: { __typename?: 'Datasource', pluginDriver?: { __typename?: 'PluginDriver', id?: string | null } | null } | null };

export type EnrichPipelineOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type EnrichPipelineOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_EnrichPipeline', edges?: Array<{ __typename?: 'DefaultEdge_EnrichPipeline', node?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type EnrichPipelineValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EnrichPipelineValueQuery = { __typename?: 'Query', value?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null, description?: string | null } | null };

export type BindEnrichPipelineToDataSourceMutationVariables = Exact<{
  datasourceId: Scalars['ID'];
  enrichPipelineId: Scalars['ID'];
}>;


export type BindEnrichPipelineToDataSourceMutation = { __typename?: 'Mutation', bindEnrichPipelineToDatasource?: { __typename?: 'Tuple2_Datasource_EnrichPipeline', left?: { __typename?: 'Datasource', id?: string | null, enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null } | null } | null, right?: { __typename?: 'EnrichPipeline', id?: string | null } | null } | null };

export type UnbindEnrichPipelineToDataSourceMutationVariables = Exact<{
  datasourceId: Scalars['ID'];
}>;


export type UnbindEnrichPipelineToDataSourceMutation = { __typename?: 'Mutation', unbindEnrichPipelineToDatasource?: { __typename?: 'Datasource', enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null } | null } | null };

export type DataIndexOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type DataIndexOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_DataIndex', edges?: Array<{ __typename?: 'DefaultEdge_DataIndex', node?: { __typename?: 'DataIndex', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DataIndexValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DataIndexValueQuery = { __typename?: 'Query', value?: { __typename?: 'DataIndex', id?: string | null, name?: string | null, description?: string | null } | null };

export type BindDataIndexToDataSourceMutationVariables = Exact<{
  datasourceId: Scalars['ID'];
  dataIndexId: Scalars['ID'];
}>;


export type BindDataIndexToDataSourceMutation = { __typename?: 'Mutation', bindDataIndexToDatasource?: { __typename?: 'Tuple2_Datasource_DataIndex', left?: { __typename?: 'Datasource', id?: string | null, dataIndex?: { __typename?: 'DataIndex', id?: string | null } | null } | null, right?: { __typename?: 'DataIndex', id?: string | null } | null } | null };

export type UnbindDataIndexToDataSourceMutationVariables = Exact<{
  datasourceId: Scalars['ID'];
}>;


export type UnbindDataIndexToDataSourceMutation = { __typename?: 'Mutation', unbindDataIndexFromDatasource?: { __typename?: 'Datasource', dataIndex?: { __typename?: 'DataIndex', id?: string | null } | null } | null };

export type DataSourcesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type DataSourcesQuery = { __typename?: 'Query', datasources?: { __typename?: 'DefaultConnection_Datasource', edges?: Array<{ __typename?: 'DefaultEdge_Datasource', node?: { __typename?: 'Datasource', id?: string | null, name?: string | null, schedulable?: boolean | null, lastIngestionDate?: any | null, scheduling?: string | null, jsonConfig?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteDataSourceMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteDataSourceMutation = { __typename?: 'Mutation', deleteDatasource?: { __typename?: 'Datasource', id?: string | null, name?: string | null } | null };

export type DocumentTypeQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DocumentTypeQuery = { __typename?: 'Query', docType?: { __typename?: 'DocType', id?: string | null, name?: string | null, description?: string | null, docTypeTemplate?: { __typename?: 'DocTypeTemplate', id?: string | null } | null } | null };

export type CreateOrUpdateDocumentTypeMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateDocumentTypeMutation = { __typename?: 'Mutation', docType?: { __typename?: 'Response_DocType', entity?: { __typename?: 'DocType', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DocumentTypeTemplateOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type DocumentTypeTemplateOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_DocTypeTemplate', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeTemplate', node?: { __typename?: 'DocTypeTemplate', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DocumentTypeTemplateValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DocumentTypeTemplateValueQuery = { __typename?: 'Query', value?: { __typename?: 'DocTypeTemplate', id?: string | null, name?: string | null, description?: string | null } | null };

export type BindDocumentTypeTemplateToDocumentTypeMutationVariables = Exact<{
  documentTypeId: Scalars['ID'];
  documentTypeTemplateId: Scalars['ID'];
}>;


export type BindDocumentTypeTemplateToDocumentTypeMutation = { __typename?: 'Mutation', bindDocTypeToDocTypeTemplate?: { __typename?: 'Tuple2_DocType_DocTypeTemplate', left?: { __typename?: 'DocType', id?: string | null, docTypeTemplate?: { __typename?: 'DocTypeTemplate', id?: string | null } | null } | null, right?: { __typename?: 'DocTypeTemplate', id?: string | null } | null } | null };

export type UnbindDocumentTypeTemplateFromDocumentTypeMutationVariables = Exact<{
  documentTypeId: Scalars['ID'];
}>;


export type UnbindDocumentTypeTemplateFromDocumentTypeMutation = { __typename?: 'Mutation', unbindDocTypeTemplateFromDocType?: { __typename?: 'DocType', id?: string | null, docTypeTemplate?: { __typename?: 'DocTypeTemplate', id?: string | null } | null } | null };

export type DocumentTypeFieldQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DocumentTypeFieldQuery = { __typename?: 'Query', docTypeField?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null, fieldType?: FieldType | null, boost?: number | null, searchable: boolean, exclude?: boolean | null, fieldName?: string | null, jsonConfig?: string | null, sortable: boolean, analyzer?: { __typename?: 'Analyzer', id?: string | null } | null } | null };

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

export type AnalyzerOptionsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type AnalyzerOptionsQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_Analyzer', edges?: Array<{ __typename?: 'DefaultEdge_Analyzer', node?: { __typename?: 'Analyzer', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type AnalyzerValueQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type AnalyzerValueQuery = { __typename?: 'Query', value?: { __typename?: 'Analyzer', id?: string | null, name?: string | null, description?: string | null } | null };

export type BindAnalyzerToDocTypeFieldMutationVariables = Exact<{
  documentTypeFieldId: Scalars['ID'];
  analyzerId: Scalars['ID'];
}>;


export type BindAnalyzerToDocTypeFieldMutation = { __typename?: 'Mutation', bindAnalyzerToDocTypeField?: { __typename?: 'Tuple2_DocTypeField_Analyzer', left?: { __typename?: 'DocTypeField', id?: string | null, analyzer?: { __typename?: 'Analyzer', id?: string | null } | null } | null, right?: { __typename?: 'Analyzer', id?: string | null } | null } | null };

export type UnbindnAlyzerToDocTypeFieldMutationVariables = Exact<{
  documentTypeFieldId: Scalars['ID'];
}>;


export type UnbindnAlyzerToDocTypeFieldMutation = { __typename?: 'Mutation', unbindAnalyzerFromDocTypeField?: { __typename?: 'Tuple2_DocTypeField_Analyzer', left?: { __typename?: 'DocTypeField', id?: string | null, analyzer?: { __typename?: 'Analyzer', id?: string | null } | null } | null, right?: { __typename?: 'Analyzer', id?: string | null } | null } | null };

export type DeleteDocumentTypeFieldMutationVariables = Exact<{
  documentTypeId: Scalars['ID'];
  documentTypeFieldId: Scalars['ID'];
}>;


export type DeleteDocumentTypeFieldMutation = { __typename?: 'Mutation', removeDocTypeField?: { __typename?: 'Tuple2_DocType_BigInteger', right?: any | null } | null };

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

export type DocumentTypeTemplatesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
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

export type DeleteDocumentTypeMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteDocumentTypeMutation = { __typename?: 'Mutation', deleteDocType?: { __typename?: 'DocType', id?: string | null } | null };

export type EnrichItemQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EnrichItemQuery = { __typename?: 'Query', enrichItem?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null, description?: string | null, type?: EnrichItemType | null, serviceName?: string | null, jsonConfig?: string | null, validationScript?: string | null, behaviorMergeType?: BehaviorMergeType | null, jsonPath?: string | null } | null };

export type CreateOrUpdateEnrichItemMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: EnrichItemType;
  serviceName: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
  validationScript?: InputMaybe<Scalars['String']>;
  behaviorMergeType: BehaviorMergeType;
  jsonPath: Scalars['String'];
}>;


export type CreateOrUpdateEnrichItemMutation = { __typename?: 'Mutation', enrichItem?: { __typename?: 'Response_EnrichItem', entity?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type EnrichItemsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type EnrichItemsQuery = { __typename?: 'Query', enrichItems?: { __typename?: 'DefaultConnection_EnrichItem', edges?: Array<{ __typename?: 'DefaultEdge_EnrichItem', node?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null, description?: string | null, type?: EnrichItemType | null, serviceName?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteEnrichItemMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteEnrichItemMutation = { __typename?: 'Mutation', deleteEnrichItem?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null } | null };

export type EnrichPipelineQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type EnrichPipelineQuery = { __typename?: 'Query', enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null, description?: string | null } | null };

export type CreateOrUpdateEnrichPipelineMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateEnrichPipelineMutation = { __typename?: 'Mutation', enrichPipeline?: { __typename?: 'Response_EnrichPipeline', entity?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type AssociatedEnrichPipelineEnrichItemsQueryVariables = Exact<{
  enrichPipelineId: Scalars['ID'];
}>;


export type AssociatedEnrichPipelineEnrichItemsQuery = { __typename?: 'Query', enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null, enrichItems?: { __typename?: 'DefaultConnection_EnrichItem', edges?: Array<{ __typename?: 'DefaultEdge_EnrichItem', node?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type UnassociatedEnrichPipelineEnrichItemsQueryVariables = Exact<{
  enrichPipelineId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
}>;


export type UnassociatedEnrichPipelineEnrichItemsQuery = { __typename?: 'Query', enrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null, enrichItems?: { __typename?: 'DefaultConnection_EnrichItem', edges?: Array<{ __typename?: 'DefaultEdge_EnrichItem', node?: { __typename?: 'EnrichItem', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

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

export type EnrichPipelinesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type EnrichPipelinesQuery = { __typename?: 'Query', enrichPipelines?: { __typename?: 'DefaultConnection_EnrichPipeline', edges?: Array<{ __typename?: 'DefaultEdge_EnrichPipeline', node?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteEnrichPipelineMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteEnrichPipelineMutation = { __typename?: 'Mutation', deleteEnrichPipeline?: { __typename?: 'EnrichPipeline', id?: string | null, name?: string | null } | null };

export type MonitoringEventsQueryVariables = Exact<{
  field?: InputMaybe<EventSortable>;
  ordering?: InputMaybe<Scalars['String']>;
}>;


export type MonitoringEventsQuery = { __typename?: 'Query', event?: Array<{ __typename?: 'Event', id?: string | null, className?: string | null, created?: any | null, groupKey?: string | null, size?: number | null, type?: string | null, version?: number | null, classPK?: string | null, parsingDate?: any | null } | null> | null };

export type MonitoringEventDataQueryVariables = Exact<{
  id?: InputMaybe<Scalars['String']>;
}>;


export type MonitoringEventDataQuery = { __typename?: 'Query', eventData?: string | null };

export type PluginDriverQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type PluginDriverQuery = { __typename?: 'Query', pluginDriver?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null, description?: string | null, type?: PluginDriverType | null, jsonConfig?: string | null } | null };

export type CreateOrUpdatePluginDriverMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: PluginDriverType;
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdatePluginDriverMutation = { __typename?: 'Mutation', pluginDriver?: { __typename?: 'Response_PluginDriver', entity?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type PluginDriverByNameQueryVariables = Exact<{
  name?: InputMaybe<Scalars['String']>;
}>;


export type PluginDriverByNameQuery = { __typename?: 'Query', pluginDrivers?: { __typename?: 'DefaultConnection_PluginDriver', edges?: Array<{ __typename?: 'DefaultEdge_PluginDriver', node?: { __typename?: 'PluginDriver', id?: string | null } | null } | null> | null } | null };

export type PluginDriverToDocumentTypeFieldsQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type PluginDriverToDocumentTypeFieldsQuery = { __typename?: 'Query', pluginDriver?: { __typename?: 'PluginDriver', id?: string | null, aclMappings?: Array<{ __typename?: 'PluginDriverAclMapping', userField?: UserField | null, docTypeField?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null } | null } | null> | null, docTypeFields?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null, docType?: { __typename?: 'DocType', id?: string | null } | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type DocumentTypeFieldsForPluginQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
}>;


export type DocumentTypeFieldsForPluginQuery = { __typename?: 'Query', docTypeFields?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null } | null };

export type AddDocumentTypeFieldToPluginDriversMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
  userField?: InputMaybe<UserField>;
}>;


export type AddDocumentTypeFieldToPluginDriversMutation = { __typename?: 'Mutation', addDocTypeFieldToPluginDriver?: { __typename?: 'Tuple2_PluginDriver_DocTypeField', left?: { __typename?: 'PluginDriver', id?: string | null } | null, right?: { __typename?: 'DocTypeField', id?: string | null } | null } | null };

export type RemoveDocumentTypeFieldFromPluginDriversMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveDocumentTypeFieldFromPluginDriversMutation = { __typename?: 'Mutation', removeDocTypeFieldFromPluginDriver?: { __typename?: 'Tuple2_PluginDriver_DocTypeField', left?: { __typename?: 'PluginDriver', id?: string | null } | null, right?: { __typename?: 'DocTypeField', id?: string | null } | null } | null };

export type ChangeUserfieldMutationVariables = Exact<{
  docTypeFieldId: Scalars['ID'];
  pluginDriverId: Scalars['ID'];
  userField?: InputMaybe<UserField>;
}>;


export type ChangeUserfieldMutation = { __typename?: 'Mutation', userField?: { __typename?: 'AclMapping', userField?: UserField | null } | null };

export type PluginDriversQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type PluginDriversQuery = { __typename?: 'Query', pluginDrivers?: { __typename?: 'DefaultConnection_PluginDriver', edges?: Array<{ __typename?: 'DefaultEdge_PluginDriver', node?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null, description?: string | null, type?: PluginDriverType | null, aclMappings?: Array<{ __typename?: 'PluginDriverAclMapping', userField?: UserField | null, docTypeField?: { __typename?: 'DocTypeField', fieldName?: string | null } | null } | null> | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeletePluginDriverMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeletePluginDriverMutation = { __typename?: 'Mutation', deletePluginDriver?: { __typename?: 'PluginDriver', id?: string | null, name?: string | null } | null };

export type QueryAnalysesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type QueryAnalysesQuery = { __typename?: 'Query', queryAnalyses?: { __typename?: 'DefaultConnection_QueryAnalysis', edges?: Array<{ __typename?: 'DefaultEdge_QueryAnalysis', node?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteQueryAnalysisMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteQueryAnalysisMutation = { __typename?: 'Mutation', deleteQueryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null } | null };

export type QueryAnalysesAnnotatorsQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type QueryAnalysesAnnotatorsQuery = { __typename?: 'Query', queryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null, annotators?: { __typename?: 'DefaultConnection_Annotator', edges?: Array<{ __typename?: 'DefaultEdge_Annotator', node?: { __typename?: 'Annotator', id?: string | null, name?: string | null, fieldName?: string | null, fuziness?: Fuzziness | null, size?: number | null, description?: string | null, type?: AnnotatorType | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type AddAnnotatorsToQueryAnalysesMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddAnnotatorsToQueryAnalysesMutation = { __typename?: 'Mutation', addAnnotatorToQueryAnalysis?: { __typename?: 'Tuple2_QueryAnalysis_Annotator', left?: { __typename?: 'QueryAnalysis', id?: string | null } | null, right?: { __typename?: 'Annotator', id?: string | null } | null } | null };

export type RemoveAnnotatorFromQueryAnalysesMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveAnnotatorFromQueryAnalysesMutation = { __typename?: 'Mutation', removeAnnotatorFromQueryAnalysis?: { __typename?: 'Tuple2_QueryAnalysis_Annotator', left?: { __typename?: 'QueryAnalysis', id?: string | null } | null, right?: { __typename?: 'Annotator', id?: string | null } | null } | null };

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


export type QueryAnalysisQuery = { __typename?: 'Query', queryAnalysis?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null, description?: string | null, stopWords?: string | null } | null };

export type CreateOrUpdateQueryAnalysisMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  stopWords?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateQueryAnalysisMutation = { __typename?: 'Mutation', queryAnalysis?: { __typename?: 'Response_QueryAnalysis', entity?: { __typename?: 'QueryAnalysis', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type QueryParserConfigQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type QueryParserConfigQuery = { __typename?: 'Query', queryParserConfig?: { __typename?: 'QueryParserConfig', id?: string | null, name?: string | null, description?: string | null, type?: string | null, jsonConfig?: string | null } | null };

export type CreateOrUpdateQueryParserConfigMutationVariables = Exact<{
  queryParserConfigId?: InputMaybe<Scalars['ID']>;
  searchConfigId: Scalars['ID'];
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  type: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateQueryParserConfigMutation = { __typename?: 'Mutation', queryParserConfig?: { __typename?: 'Response_QueryParserConfig', entity?: { __typename?: 'QueryParserConfig', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type QueryParserConfigsQueryVariables = Exact<{
  queryParserConfigId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type QueryParserConfigsQuery = { __typename?: 'Query', queryParserConfigs?: { __typename?: 'DefaultConnection_QueryParserConfig', edges?: Array<{ __typename?: 'DefaultEdge_QueryParserConfig', node?: { __typename?: 'QueryParserConfig', id?: string | null, name?: string | null, description?: string | null, type?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteQueryParserMutationVariables = Exact<{
  searchConfigId: Scalars['ID'];
  queryParserConfigId: Scalars['ID'];
}>;


export type DeleteQueryParserMutation = { __typename?: 'Mutation', removeQueryParserConfig?: { __typename?: 'Tuple2_SearchConfig_BigInteger', left?: { __typename?: 'SearchConfig', id?: string | null } | null } | null };

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

export type RulesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type RulesQuery = { __typename?: 'Query', rules?: { __typename?: 'DefaultConnection_Rule', edges?: Array<{ __typename?: 'DefaultEdge_Rule', node?: { __typename?: 'Rule', id?: string | null, name?: string | null, lhs?: string | null, rhs?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteRulesMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteRulesMutation = { __typename?: 'Mutation', deleteRule?: { __typename?: 'Rule', id?: string | null, name?: string | null } | null };

export type SearchConfigQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type SearchConfigQuery = { __typename?: 'Query', searchConfig?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, description?: string | null, minScore?: number | null, minScoreSuggestions: boolean, minScoreSearch: boolean } | null };

export type CreateOrUpdateSearchConfigMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  minScore: Scalars['Float'];
  minScoreSuggestions: Scalars['Boolean'];
  minScoreSearch: Scalars['Boolean'];
}>;


export type CreateOrUpdateSearchConfigMutation = { __typename?: 'Mutation', searchConfig?: { __typename?: 'Response_SearchConfig', entity?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, minScore?: number | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type SearchConfigsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type SearchConfigsQuery = { __typename?: 'Query', searchConfigs?: { __typename?: 'DefaultConnection_SearchConfig', edges?: Array<{ __typename?: 'DefaultEdge_SearchConfig', node?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null, description?: string | null, minScore?: number | null, minScoreSuggestions: boolean, minScoreSearch: boolean } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteSearchConfigMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteSearchConfigMutation = { __typename?: 'Mutation', deleteSearchConfig?: { __typename?: 'SearchConfig', id?: string | null, name?: string | null } | null };

export type DocumentTypeFieldsQueryVariables = Exact<{
  documentTypeId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type DocumentTypeFieldsQuery = { __typename?: 'Query', docTypeFieldsFromDocType?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null, fieldType?: FieldType | null, boost?: number | null, searchable: boolean, exclude?: boolean | null, fieldName?: string | null, sortable: boolean, subFields?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null, fieldType?: FieldType | null, boost?: number | null, searchable: boolean, exclude?: boolean | null, fieldName?: string | null, sortable: boolean } | null } | null> | null } | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

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
}>;


export type CreateOrUpdateDocumentTypeFieldMutation = { __typename?: 'Mutation', docTypeField?: { __typename?: 'Response_DocTypeField', entity?: { __typename?: 'DocTypeField', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type CreateDocumentTypeSubFieldsMutationVariables = Exact<{
  parentDocTypeFieldId: Scalars['ID'];
  name: Scalars['String'];
  fieldName: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
  searchable: Scalars['Boolean'];
  boost?: InputMaybe<Scalars['Float']>;
  fieldType: FieldType;
  description?: InputMaybe<Scalars['String']>;
  sortable: Scalars['Boolean'];
}>;


export type CreateDocumentTypeSubFieldsMutation = { __typename?: 'Mutation', createSubField?: { __typename?: 'Response_DocTypeField', entity?: { __typename?: 'DocTypeField', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type SuggestionCategoriesQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type SuggestionCategoriesQuery = { __typename?: 'Query', suggestionCategories?: { __typename?: 'DefaultConnection_SuggestionCategory', edges?: Array<{ __typename?: 'DefaultEdge_SuggestionCategory', node?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null, description?: string | null, priority?: number | null, multiSelect: boolean } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteSuggestionCategoryMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteSuggestionCategoryMutation = { __typename?: 'Mutation', deleteSuggestionCategory?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null } | null };

export type SuggestionCategoryQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type SuggestionCategoryQuery = { __typename?: 'Query', suggestionCategory?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null, description?: string | null, priority?: number | null, multiSelect: boolean } | null };

export type CreateOrUpdateSuggestionCategoryMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  priority: Scalars['Float'];
  multiSelect: Scalars['Boolean'];
}>;


export type CreateOrUpdateSuggestionCategoryMutation = { __typename?: 'Mutation', suggestionCategory?: { __typename?: 'Response_SuggestionCategory', entity?: { __typename?: 'SuggestionCategory', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type SuggestionCategoryDocumentTypeFieldsQueryVariables = Exact<{
  parentId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  unassociated: Scalars['Boolean'];
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type SuggestionCategoryDocumentTypeFieldsQuery = { __typename?: 'Query', suggestionCategory?: { __typename?: 'SuggestionCategory', id?: string | null, docTypeFields?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null, docType?: { __typename?: 'DocType', id?: string | null } | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null } | null };

export type AddDocumentTypeFieldToSuggestionCategoryMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type AddDocumentTypeFieldToSuggestionCategoryMutation = { __typename?: 'Mutation', addDocTypeFieldToSuggestionCategory?: { __typename?: 'Tuple2_SuggestionCategory_DocTypeField', left?: { __typename?: 'SuggestionCategory', id?: string | null } | null, right?: { __typename?: 'DocTypeField', id?: string | null } | null } | null };

export type RemoveDocumentTypeFieldFromSuggestionCategoryMutationVariables = Exact<{
  childId: Scalars['ID'];
  parentId: Scalars['ID'];
}>;


export type RemoveDocumentTypeFieldFromSuggestionCategoryMutation = { __typename?: 'Mutation', removeDocTypeFieldFromSuggestionCategory?: { __typename?: 'Tuple2_SuggestionCategory_DocTypeField', left?: { __typename?: 'SuggestionCategory', id?: string | null } | null, right?: { __typename?: 'DocTypeField', id?: string | null } | null } | null };

export type TabQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TabQuery = { __typename?: 'Query', tab?: { __typename?: 'Tab', id?: string | null, name?: string | null, description?: string | null, priority?: number | null } | null };

export type CreateOrUpdateTabMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  priority: Scalars['Int'];
}>;


export type CreateOrUpdateTabMutation = { __typename?: 'Mutation', tab?: { __typename?: 'Response_Tab', entity?: { __typename?: 'Tab', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type TabTokenTabQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TabTokenTabQuery = { __typename?: 'Query', tokenTab?: { __typename?: 'TokenTab', id?: string | null, name?: string | null, description?: string | null, value?: string | null, filter?: boolean | null, tokenType?: TokenType | null, docTypeField?: { __typename?: 'DocTypeField', id?: string | null } | null } | null };

export type CreateOrUpdateTabTokenMutationVariables = Exact<{
  tabId: Scalars['ID'];
  tabTokenId?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  value: Scalars['String'];
  filter: Scalars['Boolean'];
  tokenType: TokenType;
}>;


export type CreateOrUpdateTabTokenMutation = { __typename?: 'Mutation', tokenTab?: { __typename?: 'Response_TokenTab', entity?: { __typename?: 'TokenTab', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type DocTypeFieldOptionsTokenTabQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type DocTypeFieldOptionsTokenTabQuery = { __typename?: 'Query', options?: { __typename?: 'DefaultConnection_DocTypeField', edges?: Array<{ __typename?: 'DefaultEdge_DocTypeField', node?: { __typename?: 'DocTypeField', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type BindDocTypeFieldToTabTokenMutationVariables = Exact<{
  documentTypeFieldId: Scalars['ID'];
  tokenTabId: Scalars['ID'];
}>;


export type BindDocTypeFieldToTabTokenMutation = { __typename?: 'Mutation', bindDocTypeFieldToTokenTab?: { __typename?: 'Tuple2_TokenTab_DocTypeField', left?: { __typename?: 'TokenTab', id?: string | null, docTypeField?: { __typename?: 'DocTypeField', id?: string | null } | null } | null, right?: { __typename?: 'DocTypeField', id?: string | null } | null } | null };

export type UnbindDocTypeFieldToTabTokenMutationVariables = Exact<{
  documentTypeFieldId: Scalars['ID'];
  tokenTabId: Scalars['ID'];
}>;


export type UnbindDocTypeFieldToTabTokenMutation = { __typename?: 'Mutation', unbindDocTypeFieldFromTokenTab?: { __typename?: 'Tuple2_TokenTab_DocTypeField', left?: { __typename?: 'TokenTab', id?: string | null } | null } | null };

export type TabTokensQueryVariables = Exact<{
  tabId: Scalars['ID'];
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type TabTokensQuery = { __typename?: 'Query', tokenTabs?: { __typename?: 'DefaultConnection_TokenTab', edges?: Array<{ __typename?: 'DefaultEdge_TokenTab', node?: { __typename?: 'TokenTab', id?: string | null, name?: string | null, tokenType?: TokenType | null, value?: string | null, filter?: boolean | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteTabTokenTabMutationVariables = Exact<{
  tabId: Scalars['ID'];
  TabTokenTabs: Scalars['ID'];
}>;


export type DeleteTabTokenTabMutation = { __typename?: 'Mutation', removeTokenTab?: { __typename?: 'Tuple2_Tab_BigInteger', right?: any | null } | null };

export type TabsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type TabsQuery = { __typename?: 'Query', tabs?: { __typename?: 'DefaultConnection_Tab', edges?: Array<{ __typename?: 'DefaultEdge_Tab', node?: { __typename?: 'Tab', id?: string | null, name?: string | null, description?: string | null, priority?: number | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteTabsMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteTabsMutation = { __typename?: 'Mutation', deleteTab?: { __typename?: 'Tab', id?: string | null, name?: string | null } | null };

export type TokenFilterQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TokenFilterQuery = { __typename?: 'Query', tokenFilter?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null, description?: string | null, jsonConfig?: string | null } | null };

export type CreateOrUpdateTokenFilterMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateTokenFilterMutation = { __typename?: 'Mutation', tokenFilter?: { __typename?: 'Response_TokenFilter', entity?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type TokenFiltersQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type TokenFiltersQuery = { __typename?: 'Query', tokenFilters?: { __typename?: 'DefaultConnection_TokenFilter', edges?: Array<{ __typename?: 'DefaultEdge_TokenFilter', node?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteTokenFiltersMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteTokenFiltersMutation = { __typename?: 'Mutation', deleteTokenFilter?: { __typename?: 'TokenFilter', id?: string | null, name?: string | null } | null };

export type TokenizerQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TokenizerQuery = { __typename?: 'Query', tokenizer?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null, description?: string | null, jsonConfig?: string | null } | null };

export type CreateOrUpdateTokenizerMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateOrUpdateTokenizerMutation = { __typename?: 'Mutation', tokenizer?: { __typename?: 'Response_Tokenizer', entity?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type TokenizersQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type TokenizersQuery = { __typename?: 'Query', tokenizers?: { __typename?: 'DefaultConnection_Tokenizer', edges?: Array<{ __typename?: 'DefaultEdge_Tokenizer', node?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null, description?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type DeleteTokenizerMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type DeleteTokenizerMutation = { __typename?: 'Mutation', deleteTokenizer?: { __typename?: 'Tokenizer', id?: string | null, name?: string | null } | null };

export type CreateSitemapDataSourceMutationVariables = Exact<{
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  schedulable: Scalars['Boolean'];
  scheduling: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateSitemapDataSourceMutation = { __typename?: 'Mutation', datasource?: { __typename?: 'Response_Datasource', entity?: { __typename?: 'Datasource', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type CreateWebCrawlerDataSourceMutationVariables = Exact<{
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  schedulable: Scalars['Boolean'];
  scheduling: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateWebCrawlerDataSourceMutation = { __typename?: 'Mutation', datasource?: { __typename?: 'Response_Datasource', entity?: { __typename?: 'Datasource', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type CreateYouTubeDataSourceMutationVariables = Exact<{
  name: Scalars['String'];
  description?: InputMaybe<Scalars['String']>;
  schedulable: Scalars['Boolean'];
  scheduling: Scalars['String'];
  jsonConfig?: InputMaybe<Scalars['String']>;
}>;


export type CreateYouTubeDataSourceMutation = { __typename?: 'Mutation', datasource?: { __typename?: 'Response_Datasource', entity?: { __typename?: 'Datasource', id?: string | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };


export const AnalyzerDocument = gql`
    query Analyzer($id: ID!) {
  analyzer(id: $id) {
    id
    name
    type
    description
    jsonConfig
    tokenizer {
      id
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
export const CreateOrUpdateAnalyzerDocument = gql`
    mutation CreateOrUpdateAnalyzer($id: ID, $name: String!, $description: String, $type: String!, $jsonConfig: String) {
  analyzer(
    id: $id
    analyzerDTO: {name: $name, description: $description, type: $type, jsonConfig: $jsonConfig}
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
export const TokenizerOptionsDocument = gql`
    query TokenizerOptions($searchText: String, $cursor: String) {
  options: tokenizers(searchText: $searchText, first: 5, after: $cursor) {
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
 * __useTokenizerOptionsQuery__
 *
 * To run a query within a React component, call `useTokenizerOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useTokenizerOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTokenizerOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useTokenizerOptionsQuery(baseOptions?: Apollo.QueryHookOptions<TokenizerOptionsQuery, TokenizerOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TokenizerOptionsQuery, TokenizerOptionsQueryVariables>(TokenizerOptionsDocument, options);
      }
export function useTokenizerOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TokenizerOptionsQuery, TokenizerOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TokenizerOptionsQuery, TokenizerOptionsQueryVariables>(TokenizerOptionsDocument, options);
        }
export type TokenizerOptionsQueryHookResult = ReturnType<typeof useTokenizerOptionsQuery>;
export type TokenizerOptionsLazyQueryHookResult = ReturnType<typeof useTokenizerOptionsLazyQuery>;
export type TokenizerOptionsQueryResult = Apollo.QueryResult<TokenizerOptionsQuery, TokenizerOptionsQueryVariables>;
export const TokenizerValueDocument = gql`
    query TokenizerValue($id: ID!) {
  value: tokenizer(id: $id) {
    id
    name
    description
  }
}
    `;

/**
 * __useTokenizerValueQuery__
 *
 * To run a query within a React component, call `useTokenizerValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useTokenizerValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTokenizerValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useTokenizerValueQuery(baseOptions: Apollo.QueryHookOptions<TokenizerValueQuery, TokenizerValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TokenizerValueQuery, TokenizerValueQueryVariables>(TokenizerValueDocument, options);
      }
export function useTokenizerValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TokenizerValueQuery, TokenizerValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TokenizerValueQuery, TokenizerValueQueryVariables>(TokenizerValueDocument, options);
        }
export type TokenizerValueQueryHookResult = ReturnType<typeof useTokenizerValueQuery>;
export type TokenizerValueLazyQueryHookResult = ReturnType<typeof useTokenizerValueLazyQuery>;
export type TokenizerValueQueryResult = Apollo.QueryResult<TokenizerValueQuery, TokenizerValueQueryVariables>;
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
export const UnbindnTokenizerToAnalyzerDocument = gql`
    mutation UnbindnTokenizerToAnalyzer($analyzerId: ID!) {
  unbindTokenizerFromAnalyzer(analyzerId: $analyzerId) {
    left {
      id
    }
  }
}
    `;
export type UnbindnTokenizerToAnalyzerMutationFn = Apollo.MutationFunction<UnbindnTokenizerToAnalyzerMutation, UnbindnTokenizerToAnalyzerMutationVariables>;

/**
 * __useUnbindnTokenizerToAnalyzerMutation__
 *
 * To run a mutation, you first call `useUnbindnTokenizerToAnalyzerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindnTokenizerToAnalyzerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindnTokenizerToAnalyzerMutation, { data, loading, error }] = useUnbindnTokenizerToAnalyzerMutation({
 *   variables: {
 *      analyzerId: // value for 'analyzerId'
 *   },
 * });
 */
export function useUnbindnTokenizerToAnalyzerMutation(baseOptions?: Apollo.MutationHookOptions<UnbindnTokenizerToAnalyzerMutation, UnbindnTokenizerToAnalyzerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindnTokenizerToAnalyzerMutation, UnbindnTokenizerToAnalyzerMutationVariables>(UnbindnTokenizerToAnalyzerDocument, options);
      }
export type UnbindnTokenizerToAnalyzerMutationHookResult = ReturnType<typeof useUnbindnTokenizerToAnalyzerMutation>;
export type UnbindnTokenizerToAnalyzerMutationResult = Apollo.MutationResult<UnbindnTokenizerToAnalyzerMutation>;
export type UnbindnTokenizerToAnalyzerMutationOptions = Apollo.BaseMutationOptions<UnbindnTokenizerToAnalyzerMutation, UnbindnTokenizerToAnalyzerMutationVariables>;
export const AnalyzerCharFiltersDocument = gql`
    query AnalyzerCharFilters($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  analyzer(id: $parentId) {
    id
    charFilters(
      searchText: $searchText
      notEqual: $unassociated
      first: 25
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
 * __useAnalyzerCharFiltersQuery__
 *
 * To run a query within a React component, call `useAnalyzerCharFiltersQuery` and pass it any options that fit your needs.
 * When your component renders, `useAnalyzerCharFiltersQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAnalyzerCharFiltersQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      unassociated: // value for 'unassociated'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useAnalyzerCharFiltersQuery(baseOptions: Apollo.QueryHookOptions<AnalyzerCharFiltersQuery, AnalyzerCharFiltersQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzerCharFiltersQuery, AnalyzerCharFiltersQueryVariables>(AnalyzerCharFiltersDocument, options);
      }
export function useAnalyzerCharFiltersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzerCharFiltersQuery, AnalyzerCharFiltersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzerCharFiltersQuery, AnalyzerCharFiltersQueryVariables>(AnalyzerCharFiltersDocument, options);
        }
export type AnalyzerCharFiltersQueryHookResult = ReturnType<typeof useAnalyzerCharFiltersQuery>;
export type AnalyzerCharFiltersLazyQueryHookResult = ReturnType<typeof useAnalyzerCharFiltersLazyQuery>;
export type AnalyzerCharFiltersQueryResult = Apollo.QueryResult<AnalyzerCharFiltersQuery, AnalyzerCharFiltersQueryVariables>;
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
export const RemoveCharFiltersToAnalyzerDocument = gql`
    mutation RemoveCharFiltersToAnalyzer($childId: ID!, $parentId: ID!) {
  removeCharFilterFromAnalyzer(charFilterId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveCharFiltersToAnalyzerMutationFn = Apollo.MutationFunction<RemoveCharFiltersToAnalyzerMutation, RemoveCharFiltersToAnalyzerMutationVariables>;

/**
 * __useRemoveCharFiltersToAnalyzerMutation__
 *
 * To run a mutation, you first call `useRemoveCharFiltersToAnalyzerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveCharFiltersToAnalyzerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeCharFiltersToAnalyzerMutation, { data, loading, error }] = useRemoveCharFiltersToAnalyzerMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveCharFiltersToAnalyzerMutation(baseOptions?: Apollo.MutationHookOptions<RemoveCharFiltersToAnalyzerMutation, RemoveCharFiltersToAnalyzerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveCharFiltersToAnalyzerMutation, RemoveCharFiltersToAnalyzerMutationVariables>(RemoveCharFiltersToAnalyzerDocument, options);
      }
export type RemoveCharFiltersToAnalyzerMutationHookResult = ReturnType<typeof useRemoveCharFiltersToAnalyzerMutation>;
export type RemoveCharFiltersToAnalyzerMutationResult = Apollo.MutationResult<RemoveCharFiltersToAnalyzerMutation>;
export type RemoveCharFiltersToAnalyzerMutationOptions = Apollo.BaseMutationOptions<RemoveCharFiltersToAnalyzerMutation, RemoveCharFiltersToAnalyzerMutationVariables>;
export const AnalyzerTokenFiltersDocument = gql`
    query AnalyzerTokenFilters($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  analyzer(id: $parentId) {
    id
    tokenFilters(
      searchText: $searchText
      notEqual: $unassociated
      first: 25
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
 * __useAnalyzerTokenFiltersQuery__
 *
 * To run a query within a React component, call `useAnalyzerTokenFiltersQuery` and pass it any options that fit your needs.
 * When your component renders, `useAnalyzerTokenFiltersQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAnalyzerTokenFiltersQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      unassociated: // value for 'unassociated'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useAnalyzerTokenFiltersQuery(baseOptions: Apollo.QueryHookOptions<AnalyzerTokenFiltersQuery, AnalyzerTokenFiltersQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzerTokenFiltersQuery, AnalyzerTokenFiltersQueryVariables>(AnalyzerTokenFiltersDocument, options);
      }
export function useAnalyzerTokenFiltersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzerTokenFiltersQuery, AnalyzerTokenFiltersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzerTokenFiltersQuery, AnalyzerTokenFiltersQueryVariables>(AnalyzerTokenFiltersDocument, options);
        }
export type AnalyzerTokenFiltersQueryHookResult = ReturnType<typeof useAnalyzerTokenFiltersQuery>;
export type AnalyzerTokenFiltersLazyQueryHookResult = ReturnType<typeof useAnalyzerTokenFiltersLazyQuery>;
export type AnalyzerTokenFiltersQueryResult = Apollo.QueryResult<AnalyzerTokenFiltersQuery, AnalyzerTokenFiltersQueryVariables>;
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
export const RemoveTokenFilterToAnalyzerDocument = gql`
    mutation RemoveTokenFilterToAnalyzer($childId: ID!, $parentId: ID!) {
  removeTokenFilterFromAnalyzer(tokenFilterId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveTokenFilterToAnalyzerMutationFn = Apollo.MutationFunction<RemoveTokenFilterToAnalyzerMutation, RemoveTokenFilterToAnalyzerMutationVariables>;

/**
 * __useRemoveTokenFilterToAnalyzerMutation__
 *
 * To run a mutation, you first call `useRemoveTokenFilterToAnalyzerMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveTokenFilterToAnalyzerMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeTokenFilterToAnalyzerMutation, { data, loading, error }] = useRemoveTokenFilterToAnalyzerMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveTokenFilterToAnalyzerMutation(baseOptions?: Apollo.MutationHookOptions<RemoveTokenFilterToAnalyzerMutation, RemoveTokenFilterToAnalyzerMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveTokenFilterToAnalyzerMutation, RemoveTokenFilterToAnalyzerMutationVariables>(RemoveTokenFilterToAnalyzerDocument, options);
      }
export type RemoveTokenFilterToAnalyzerMutationHookResult = ReturnType<typeof useRemoveTokenFilterToAnalyzerMutation>;
export type RemoveTokenFilterToAnalyzerMutationResult = Apollo.MutationResult<RemoveTokenFilterToAnalyzerMutation>;
export type RemoveTokenFilterToAnalyzerMutationOptions = Apollo.BaseMutationOptions<RemoveTokenFilterToAnalyzerMutation, RemoveTokenFilterToAnalyzerMutationVariables>;
export const AnalyzersDocument = gql`
    query Analyzers($searchText: String, $cursor: String) {
  analyzers(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const DeleteAnalyzersDocument = gql`
    mutation DeleteAnalyzers($id: ID!) {
  deleteAnalyzer(analyzerId: $id) {
    id
    name
  }
}
    `;
export type DeleteAnalyzersMutationFn = Apollo.MutationFunction<DeleteAnalyzersMutation, DeleteAnalyzersMutationVariables>;

/**
 * __useDeleteAnalyzersMutation__
 *
 * To run a mutation, you first call `useDeleteAnalyzersMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteAnalyzersMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteAnalyzersMutation, { data, loading, error }] = useDeleteAnalyzersMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteAnalyzersMutation(baseOptions?: Apollo.MutationHookOptions<DeleteAnalyzersMutation, DeleteAnalyzersMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteAnalyzersMutation, DeleteAnalyzersMutationVariables>(DeleteAnalyzersDocument, options);
      }
export type DeleteAnalyzersMutationHookResult = ReturnType<typeof useDeleteAnalyzersMutation>;
export type DeleteAnalyzersMutationResult = Apollo.MutationResult<DeleteAnalyzersMutation>;
export type DeleteAnalyzersMutationOptions = Apollo.BaseMutationOptions<DeleteAnalyzersMutation, DeleteAnalyzersMutationVariables>;
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
    }
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
    mutation CreateOrUpdateAnnotator($id: ID, $fieldName: String!, $fuziness: Fuzziness!, $type: AnnotatorType!, $description: String, $size: Int, $name: String!) {
  annotator(
    id: $id
    annotatorDTO: {fieldName: $fieldName, fuziness: $fuziness, size: $size, type: $type, description: $description, name: $name}
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
export const AnnotatorsDocument = gql`
    query Annotators($searchText: String, $cursor: String) {
  annotators(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const BucketDocument = gql`
    query Bucket($id: ID!) {
  bucket(id: $id) {
    id
    name
    description
    enabled
    queryAnalysis {
      id
    }
    searchConfig {
      id
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
export const CreateOrUpdateBucketDocument = gql`
    mutation CreateOrUpdateBucket($id: ID, $name: String!, $description: String) {
  bucket(id: $id, bucketDTO: {name: $name, description: $description}) {
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
export const BucketDataSourcesDocument = gql`
    query BucketDataSources($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  bucket(id: $parentId) {
    id
    datasources(
      searchText: $searchText
      first: 25
      after: $cursor
      notEqual: $unassociated
    ) {
      edges {
        node {
          id
          name
          description
          schedulable
          lastIngestionDate
          scheduling
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
export const BucketSuggestionCategoriesDocument = gql`
    query BucketSuggestionCategories($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  bucket(id: $parentId) {
    id
    suggestionCategories(
      searchText: $searchText
      notEqual: $unassociated
      first: 25
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
export const BucketTabsDocument = gql`
    query BucketTabs($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  bucket(id: $parentId) {
    id
    tabs(
      searchText: $searchText
      notEqual: $unassociated
      first: 25
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
export const BucketsDocument = gql`
    query Buckets($searchText: String, $cursor: String) {
  buckets(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const CharFilterDocument = gql`
    query CharFilter($id: ID!) {
  charFilter(id: $id) {
    id
    name
    description
    jsonConfig
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
    mutation CreateOrUpdateCharFilter($id: ID, $name: String!, $description: String, $jsonConfig: String) {
  charFilter(
    id: $id
    charFilterDTO: {name: $name, description: $description, jsonConfig: $jsonConfig}
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
export const CharfiltersDocument = gql`
    query Charfilters($searchText: String, $cursor: String) {
  charFilters(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const DataSourceDocument = gql`
    query DataSource($id: ID!) {
  datasource(id: $id) {
    id
    name
    description
    schedulable
    scheduling
    jsonConfig
    pluginDriver {
      id
    }
    dataIndex {
      id
    }
    enrichPipeline {
      id
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
export const CreateOrUpdateDataSourceDocument = gql`
    mutation CreateOrUpdateDataSource($id: ID, $name: String!, $description: String, $schedulable: Boolean!, $scheduling: String!, $jsonConfig: String) {
  datasource(
    id: $id
    datasourceDTO: {name: $name, description: $description, schedulable: $schedulable, scheduling: $scheduling, jsonConfig: $jsonConfig}
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
export type CreateOrUpdateDataSourceMutationFn = Apollo.MutationFunction<CreateOrUpdateDataSourceMutation, CreateOrUpdateDataSourceMutationVariables>;

/**
 * __useCreateOrUpdateDataSourceMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateDataSourceMutation, { data, loading, error }] = useCreateOrUpdateDataSourceMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      schedulable: // value for 'schedulable'
 *      scheduling: // value for 'scheduling'
 *      jsonConfig: // value for 'jsonConfig'
 *   },
 * });
 */
export function useCreateOrUpdateDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateDataSourceMutation, CreateOrUpdateDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateDataSourceMutation, CreateOrUpdateDataSourceMutationVariables>(CreateOrUpdateDataSourceDocument, options);
      }
export type CreateOrUpdateDataSourceMutationHookResult = ReturnType<typeof useCreateOrUpdateDataSourceMutation>;
export type CreateOrUpdateDataSourceMutationResult = Apollo.MutationResult<CreateOrUpdateDataSourceMutation>;
export type CreateOrUpdateDataSourceMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateDataSourceMutation, CreateOrUpdateDataSourceMutationVariables>;
export const PluginDriverOptionsDocument = gql`
    query PluginDriverOptions($searchText: String, $cursor: String) {
  options: pluginDrivers(searchText: $searchText, first: 5, after: $cursor) {
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
 * __usePluginDriverOptionsQuery__
 *
 * To run a query within a React component, call `usePluginDriverOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `usePluginDriverOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = usePluginDriverOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function usePluginDriverOptionsQuery(baseOptions?: Apollo.QueryHookOptions<PluginDriverOptionsQuery, PluginDriverOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PluginDriverOptionsQuery, PluginDriverOptionsQueryVariables>(PluginDriverOptionsDocument, options);
      }
export function usePluginDriverOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PluginDriverOptionsQuery, PluginDriverOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PluginDriverOptionsQuery, PluginDriverOptionsQueryVariables>(PluginDriverOptionsDocument, options);
        }
export type PluginDriverOptionsQueryHookResult = ReturnType<typeof usePluginDriverOptionsQuery>;
export type PluginDriverOptionsLazyQueryHookResult = ReturnType<typeof usePluginDriverOptionsLazyQuery>;
export type PluginDriverOptionsQueryResult = Apollo.QueryResult<PluginDriverOptionsQuery, PluginDriverOptionsQueryVariables>;
export const PluginDriverValueDocument = gql`
    query PluginDriverValue($id: ID!) {
  value: pluginDriver(id: $id) {
    id
    name
    description
  }
}
    `;

/**
 * __usePluginDriverValueQuery__
 *
 * To run a query within a React component, call `usePluginDriverValueQuery` and pass it any options that fit your needs.
 * When your component renders, `usePluginDriverValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = usePluginDriverValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function usePluginDriverValueQuery(baseOptions: Apollo.QueryHookOptions<PluginDriverValueQuery, PluginDriverValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PluginDriverValueQuery, PluginDriverValueQueryVariables>(PluginDriverValueDocument, options);
      }
export function usePluginDriverValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PluginDriverValueQuery, PluginDriverValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PluginDriverValueQuery, PluginDriverValueQueryVariables>(PluginDriverValueDocument, options);
        }
export type PluginDriverValueQueryHookResult = ReturnType<typeof usePluginDriverValueQuery>;
export type PluginDriverValueLazyQueryHookResult = ReturnType<typeof usePluginDriverValueLazyQuery>;
export type PluginDriverValueQueryResult = Apollo.QueryResult<PluginDriverValueQuery, PluginDriverValueQueryVariables>;
export const BindPluginDriverToDataSourceDocument = gql`
    mutation BindPluginDriverToDataSource($datasourceId: ID!, $pluginDriverId: ID!) {
  bindPluginDriverToDatasource(
    datasourceId: $datasourceId
    pluginDriverId: $pluginDriverId
  ) {
    left {
      id
      pluginDriver {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindPluginDriverToDataSourceMutationFn = Apollo.MutationFunction<BindPluginDriverToDataSourceMutation, BindPluginDriverToDataSourceMutationVariables>;

/**
 * __useBindPluginDriverToDataSourceMutation__
 *
 * To run a mutation, you first call `useBindPluginDriverToDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindPluginDriverToDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindPluginDriverToDataSourceMutation, { data, loading, error }] = useBindPluginDriverToDataSourceMutation({
 *   variables: {
 *      datasourceId: // value for 'datasourceId'
 *      pluginDriverId: // value for 'pluginDriverId'
 *   },
 * });
 */
export function useBindPluginDriverToDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<BindPluginDriverToDataSourceMutation, BindPluginDriverToDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindPluginDriverToDataSourceMutation, BindPluginDriverToDataSourceMutationVariables>(BindPluginDriverToDataSourceDocument, options);
      }
export type BindPluginDriverToDataSourceMutationHookResult = ReturnType<typeof useBindPluginDriverToDataSourceMutation>;
export type BindPluginDriverToDataSourceMutationResult = Apollo.MutationResult<BindPluginDriverToDataSourceMutation>;
export type BindPluginDriverToDataSourceMutationOptions = Apollo.BaseMutationOptions<BindPluginDriverToDataSourceMutation, BindPluginDriverToDataSourceMutationVariables>;
export const UnbindPluginDriverFromDataSourceDocument = gql`
    mutation UnbindPluginDriverFromDataSource($datasourceId: ID!) {
  unbindPluginDriverToDatasource(datasourceId: $datasourceId) {
    pluginDriver {
      id
    }
  }
}
    `;
export type UnbindPluginDriverFromDataSourceMutationFn = Apollo.MutationFunction<UnbindPluginDriverFromDataSourceMutation, UnbindPluginDriverFromDataSourceMutationVariables>;

/**
 * __useUnbindPluginDriverFromDataSourceMutation__
 *
 * To run a mutation, you first call `useUnbindPluginDriverFromDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindPluginDriverFromDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindPluginDriverFromDataSourceMutation, { data, loading, error }] = useUnbindPluginDriverFromDataSourceMutation({
 *   variables: {
 *      datasourceId: // value for 'datasourceId'
 *   },
 * });
 */
export function useUnbindPluginDriverFromDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<UnbindPluginDriverFromDataSourceMutation, UnbindPluginDriverFromDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindPluginDriverFromDataSourceMutation, UnbindPluginDriverFromDataSourceMutationVariables>(UnbindPluginDriverFromDataSourceDocument, options);
      }
export type UnbindPluginDriverFromDataSourceMutationHookResult = ReturnType<typeof useUnbindPluginDriverFromDataSourceMutation>;
export type UnbindPluginDriverFromDataSourceMutationResult = Apollo.MutationResult<UnbindPluginDriverFromDataSourceMutation>;
export type UnbindPluginDriverFromDataSourceMutationOptions = Apollo.BaseMutationOptions<UnbindPluginDriverFromDataSourceMutation, UnbindPluginDriverFromDataSourceMutationVariables>;
export const EnrichPipelineOptionsDocument = gql`
    query EnrichPipelineOptions($searchText: String, $cursor: String) {
  options: enrichPipelines(searchText: $searchText, first: 5, after: $cursor) {
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
export const EnrichPipelineValueDocument = gql`
    query EnrichPipelineValue($id: ID!) {
  value: enrichPipeline(id: $id) {
    id
    name
    description
  }
}
    `;

/**
 * __useEnrichPipelineValueQuery__
 *
 * To run a query within a React component, call `useEnrichPipelineValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useEnrichPipelineValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEnrichPipelineValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useEnrichPipelineValueQuery(baseOptions: Apollo.QueryHookOptions<EnrichPipelineValueQuery, EnrichPipelineValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichPipelineValueQuery, EnrichPipelineValueQueryVariables>(EnrichPipelineValueDocument, options);
      }
export function useEnrichPipelineValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichPipelineValueQuery, EnrichPipelineValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichPipelineValueQuery, EnrichPipelineValueQueryVariables>(EnrichPipelineValueDocument, options);
        }
export type EnrichPipelineValueQueryHookResult = ReturnType<typeof useEnrichPipelineValueQuery>;
export type EnrichPipelineValueLazyQueryHookResult = ReturnType<typeof useEnrichPipelineValueLazyQuery>;
export type EnrichPipelineValueQueryResult = Apollo.QueryResult<EnrichPipelineValueQuery, EnrichPipelineValueQueryVariables>;
export const BindEnrichPipelineToDataSourceDocument = gql`
    mutation BindEnrichPipelineToDataSource($datasourceId: ID!, $enrichPipelineId: ID!) {
  bindEnrichPipelineToDatasource(
    datasourceId: $datasourceId
    enrichPipelineId: $enrichPipelineId
  ) {
    left {
      id
      enrichPipeline {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindEnrichPipelineToDataSourceMutationFn = Apollo.MutationFunction<BindEnrichPipelineToDataSourceMutation, BindEnrichPipelineToDataSourceMutationVariables>;

/**
 * __useBindEnrichPipelineToDataSourceMutation__
 *
 * To run a mutation, you first call `useBindEnrichPipelineToDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindEnrichPipelineToDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindEnrichPipelineToDataSourceMutation, { data, loading, error }] = useBindEnrichPipelineToDataSourceMutation({
 *   variables: {
 *      datasourceId: // value for 'datasourceId'
 *      enrichPipelineId: // value for 'enrichPipelineId'
 *   },
 * });
 */
export function useBindEnrichPipelineToDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<BindEnrichPipelineToDataSourceMutation, BindEnrichPipelineToDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindEnrichPipelineToDataSourceMutation, BindEnrichPipelineToDataSourceMutationVariables>(BindEnrichPipelineToDataSourceDocument, options);
      }
export type BindEnrichPipelineToDataSourceMutationHookResult = ReturnType<typeof useBindEnrichPipelineToDataSourceMutation>;
export type BindEnrichPipelineToDataSourceMutationResult = Apollo.MutationResult<BindEnrichPipelineToDataSourceMutation>;
export type BindEnrichPipelineToDataSourceMutationOptions = Apollo.BaseMutationOptions<BindEnrichPipelineToDataSourceMutation, BindEnrichPipelineToDataSourceMutationVariables>;
export const UnbindEnrichPipelineToDataSourceDocument = gql`
    mutation UnbindEnrichPipelineToDataSource($datasourceId: ID!) {
  unbindEnrichPipelineToDatasource(datasourceId: $datasourceId) {
    enrichPipeline {
      id
    }
  }
}
    `;
export type UnbindEnrichPipelineToDataSourceMutationFn = Apollo.MutationFunction<UnbindEnrichPipelineToDataSourceMutation, UnbindEnrichPipelineToDataSourceMutationVariables>;

/**
 * __useUnbindEnrichPipelineToDataSourceMutation__
 *
 * To run a mutation, you first call `useUnbindEnrichPipelineToDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindEnrichPipelineToDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindEnrichPipelineToDataSourceMutation, { data, loading, error }] = useUnbindEnrichPipelineToDataSourceMutation({
 *   variables: {
 *      datasourceId: // value for 'datasourceId'
 *   },
 * });
 */
export function useUnbindEnrichPipelineToDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<UnbindEnrichPipelineToDataSourceMutation, UnbindEnrichPipelineToDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindEnrichPipelineToDataSourceMutation, UnbindEnrichPipelineToDataSourceMutationVariables>(UnbindEnrichPipelineToDataSourceDocument, options);
      }
export type UnbindEnrichPipelineToDataSourceMutationHookResult = ReturnType<typeof useUnbindEnrichPipelineToDataSourceMutation>;
export type UnbindEnrichPipelineToDataSourceMutationResult = Apollo.MutationResult<UnbindEnrichPipelineToDataSourceMutation>;
export type UnbindEnrichPipelineToDataSourceMutationOptions = Apollo.BaseMutationOptions<UnbindEnrichPipelineToDataSourceMutation, UnbindEnrichPipelineToDataSourceMutationVariables>;
export const DataIndexOptionsDocument = gql`
    query DataIndexOptions($searchText: String, $cursor: String) {
  options: dataIndices(searchText: $searchText, first: 5, after: $cursor) {
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
 * __useDataIndexOptionsQuery__
 *
 * To run a query within a React component, call `useDataIndexOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useDataIndexOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDataIndexOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useDataIndexOptionsQuery(baseOptions?: Apollo.QueryHookOptions<DataIndexOptionsQuery, DataIndexOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataIndexOptionsQuery, DataIndexOptionsQueryVariables>(DataIndexOptionsDocument, options);
      }
export function useDataIndexOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataIndexOptionsQuery, DataIndexOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataIndexOptionsQuery, DataIndexOptionsQueryVariables>(DataIndexOptionsDocument, options);
        }
export type DataIndexOptionsQueryHookResult = ReturnType<typeof useDataIndexOptionsQuery>;
export type DataIndexOptionsLazyQueryHookResult = ReturnType<typeof useDataIndexOptionsLazyQuery>;
export type DataIndexOptionsQueryResult = Apollo.QueryResult<DataIndexOptionsQuery, DataIndexOptionsQueryVariables>;
export const DataIndexValueDocument = gql`
    query DataIndexValue($id: ID!) {
  value: dataIndex(id: $id) {
    id
    name
    description
  }
}
    `;

/**
 * __useDataIndexValueQuery__
 *
 * To run a query within a React component, call `useDataIndexValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useDataIndexValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDataIndexValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDataIndexValueQuery(baseOptions: Apollo.QueryHookOptions<DataIndexValueQuery, DataIndexValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataIndexValueQuery, DataIndexValueQueryVariables>(DataIndexValueDocument, options);
      }
export function useDataIndexValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataIndexValueQuery, DataIndexValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataIndexValueQuery, DataIndexValueQueryVariables>(DataIndexValueDocument, options);
        }
export type DataIndexValueQueryHookResult = ReturnType<typeof useDataIndexValueQuery>;
export type DataIndexValueLazyQueryHookResult = ReturnType<typeof useDataIndexValueLazyQuery>;
export type DataIndexValueQueryResult = Apollo.QueryResult<DataIndexValueQuery, DataIndexValueQueryVariables>;
export const BindDataIndexToDataSourceDocument = gql`
    mutation BindDataIndexToDataSource($datasourceId: ID!, $dataIndexId: ID!) {
  bindDataIndexToDatasource(
    datasourceId: $datasourceId
    dataIndexId: $dataIndexId
  ) {
    left {
      id
      dataIndex {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindDataIndexToDataSourceMutationFn = Apollo.MutationFunction<BindDataIndexToDataSourceMutation, BindDataIndexToDataSourceMutationVariables>;

/**
 * __useBindDataIndexToDataSourceMutation__
 *
 * To run a mutation, you first call `useBindDataIndexToDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindDataIndexToDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindDataIndexToDataSourceMutation, { data, loading, error }] = useBindDataIndexToDataSourceMutation({
 *   variables: {
 *      datasourceId: // value for 'datasourceId'
 *      dataIndexId: // value for 'dataIndexId'
 *   },
 * });
 */
export function useBindDataIndexToDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<BindDataIndexToDataSourceMutation, BindDataIndexToDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindDataIndexToDataSourceMutation, BindDataIndexToDataSourceMutationVariables>(BindDataIndexToDataSourceDocument, options);
      }
export type BindDataIndexToDataSourceMutationHookResult = ReturnType<typeof useBindDataIndexToDataSourceMutation>;
export type BindDataIndexToDataSourceMutationResult = Apollo.MutationResult<BindDataIndexToDataSourceMutation>;
export type BindDataIndexToDataSourceMutationOptions = Apollo.BaseMutationOptions<BindDataIndexToDataSourceMutation, BindDataIndexToDataSourceMutationVariables>;
export const UnbindDataIndexToDataSourceDocument = gql`
    mutation UnbindDataIndexToDataSource($datasourceId: ID!) {
  unbindDataIndexFromDatasource(datasourceId: $datasourceId) {
    dataIndex {
      id
    }
  }
}
    `;
export type UnbindDataIndexToDataSourceMutationFn = Apollo.MutationFunction<UnbindDataIndexToDataSourceMutation, UnbindDataIndexToDataSourceMutationVariables>;

/**
 * __useUnbindDataIndexToDataSourceMutation__
 *
 * To run a mutation, you first call `useUnbindDataIndexToDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindDataIndexToDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindDataIndexToDataSourceMutation, { data, loading, error }] = useUnbindDataIndexToDataSourceMutation({
 *   variables: {
 *      datasourceId: // value for 'datasourceId'
 *   },
 * });
 */
export function useUnbindDataIndexToDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<UnbindDataIndexToDataSourceMutation, UnbindDataIndexToDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindDataIndexToDataSourceMutation, UnbindDataIndexToDataSourceMutationVariables>(UnbindDataIndexToDataSourceDocument, options);
      }
export type UnbindDataIndexToDataSourceMutationHookResult = ReturnType<typeof useUnbindDataIndexToDataSourceMutation>;
export type UnbindDataIndexToDataSourceMutationResult = Apollo.MutationResult<UnbindDataIndexToDataSourceMutation>;
export type UnbindDataIndexToDataSourceMutationOptions = Apollo.BaseMutationOptions<UnbindDataIndexToDataSourceMutation, UnbindDataIndexToDataSourceMutationVariables>;
export const DataSourcesDocument = gql`
    query DataSources($searchText: String, $cursor: String) {
  datasources(searchText: $searchText, first: 25, after: $cursor) {
    edges {
      node {
        id
        name
        schedulable
        lastIngestionDate
        scheduling
        jsonConfig
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
 *      cursor: // value for 'cursor'
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
    mutation DeleteDataSource($id: ID!) {
  deleteDatasource(datasourceId: $id) {
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
export const DocumentTypeDocument = gql`
    query DocumentType($id: ID!) {
  docType(id: $id) {
    id
    name
    description
    docTypeTemplate {
      id
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
export const DocumentTypeTemplateOptionsDocument = gql`
    query DocumentTypeTemplateOptions($searchText: String, $cursor: String) {
  options: docTypeTemplates(searchText: $searchText, first: 5, after: $cursor) {
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
 * __useDocumentTypeTemplateOptionsQuery__
 *
 * To run a query within a React component, call `useDocumentTypeTemplateOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocumentTypeTemplateOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocumentTypeTemplateOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useDocumentTypeTemplateOptionsQuery(baseOptions?: Apollo.QueryHookOptions<DocumentTypeTemplateOptionsQuery, DocumentTypeTemplateOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeTemplateOptionsQuery, DocumentTypeTemplateOptionsQueryVariables>(DocumentTypeTemplateOptionsDocument, options);
      }
export function useDocumentTypeTemplateOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeTemplateOptionsQuery, DocumentTypeTemplateOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeTemplateOptionsQuery, DocumentTypeTemplateOptionsQueryVariables>(DocumentTypeTemplateOptionsDocument, options);
        }
export type DocumentTypeTemplateOptionsQueryHookResult = ReturnType<typeof useDocumentTypeTemplateOptionsQuery>;
export type DocumentTypeTemplateOptionsLazyQueryHookResult = ReturnType<typeof useDocumentTypeTemplateOptionsLazyQuery>;
export type DocumentTypeTemplateOptionsQueryResult = Apollo.QueryResult<DocumentTypeTemplateOptionsQuery, DocumentTypeTemplateOptionsQueryVariables>;
export const DocumentTypeTemplateValueDocument = gql`
    query DocumentTypeTemplateValue($id: ID!) {
  value: docTypeTemplate(id: $id) {
    id
    name
    description
  }
}
    `;

/**
 * __useDocumentTypeTemplateValueQuery__
 *
 * To run a query within a React component, call `useDocumentTypeTemplateValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocumentTypeTemplateValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocumentTypeTemplateValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDocumentTypeTemplateValueQuery(baseOptions: Apollo.QueryHookOptions<DocumentTypeTemplateValueQuery, DocumentTypeTemplateValueQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeTemplateValueQuery, DocumentTypeTemplateValueQueryVariables>(DocumentTypeTemplateValueDocument, options);
      }
export function useDocumentTypeTemplateValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeTemplateValueQuery, DocumentTypeTemplateValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeTemplateValueQuery, DocumentTypeTemplateValueQueryVariables>(DocumentTypeTemplateValueDocument, options);
        }
export type DocumentTypeTemplateValueQueryHookResult = ReturnType<typeof useDocumentTypeTemplateValueQuery>;
export type DocumentTypeTemplateValueLazyQueryHookResult = ReturnType<typeof useDocumentTypeTemplateValueLazyQuery>;
export type DocumentTypeTemplateValueQueryResult = Apollo.QueryResult<DocumentTypeTemplateValueQuery, DocumentTypeTemplateValueQueryVariables>;
export const BindDocumentTypeTemplateToDocumentTypeDocument = gql`
    mutation BindDocumentTypeTemplateToDocumentType($documentTypeId: ID!, $documentTypeTemplateId: ID!) {
  bindDocTypeToDocTypeTemplate(
    docTypeId: $documentTypeId
    docTypeTemplateId: $documentTypeTemplateId
  ) {
    left {
      id
      docTypeTemplate {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type BindDocumentTypeTemplateToDocumentTypeMutationFn = Apollo.MutationFunction<BindDocumentTypeTemplateToDocumentTypeMutation, BindDocumentTypeTemplateToDocumentTypeMutationVariables>;

/**
 * __useBindDocumentTypeTemplateToDocumentTypeMutation__
 *
 * To run a mutation, you first call `useBindDocumentTypeTemplateToDocumentTypeMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindDocumentTypeTemplateToDocumentTypeMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindDocumentTypeTemplateToDocumentTypeMutation, { data, loading, error }] = useBindDocumentTypeTemplateToDocumentTypeMutation({
 *   variables: {
 *      documentTypeId: // value for 'documentTypeId'
 *      documentTypeTemplateId: // value for 'documentTypeTemplateId'
 *   },
 * });
 */
export function useBindDocumentTypeTemplateToDocumentTypeMutation(baseOptions?: Apollo.MutationHookOptions<BindDocumentTypeTemplateToDocumentTypeMutation, BindDocumentTypeTemplateToDocumentTypeMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindDocumentTypeTemplateToDocumentTypeMutation, BindDocumentTypeTemplateToDocumentTypeMutationVariables>(BindDocumentTypeTemplateToDocumentTypeDocument, options);
      }
export type BindDocumentTypeTemplateToDocumentTypeMutationHookResult = ReturnType<typeof useBindDocumentTypeTemplateToDocumentTypeMutation>;
export type BindDocumentTypeTemplateToDocumentTypeMutationResult = Apollo.MutationResult<BindDocumentTypeTemplateToDocumentTypeMutation>;
export type BindDocumentTypeTemplateToDocumentTypeMutationOptions = Apollo.BaseMutationOptions<BindDocumentTypeTemplateToDocumentTypeMutation, BindDocumentTypeTemplateToDocumentTypeMutationVariables>;
export const UnbindDocumentTypeTemplateFromDocumentTypeDocument = gql`
    mutation UnbindDocumentTypeTemplateFromDocumentType($documentTypeId: ID!) {
  unbindDocTypeTemplateFromDocType(docTypeId: $documentTypeId) {
    id
    docTypeTemplate {
      id
    }
  }
}
    `;
export type UnbindDocumentTypeTemplateFromDocumentTypeMutationFn = Apollo.MutationFunction<UnbindDocumentTypeTemplateFromDocumentTypeMutation, UnbindDocumentTypeTemplateFromDocumentTypeMutationVariables>;

/**
 * __useUnbindDocumentTypeTemplateFromDocumentTypeMutation__
 *
 * To run a mutation, you first call `useUnbindDocumentTypeTemplateFromDocumentTypeMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindDocumentTypeTemplateFromDocumentTypeMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindDocumentTypeTemplateFromDocumentTypeMutation, { data, loading, error }] = useUnbindDocumentTypeTemplateFromDocumentTypeMutation({
 *   variables: {
 *      documentTypeId: // value for 'documentTypeId'
 *   },
 * });
 */
export function useUnbindDocumentTypeTemplateFromDocumentTypeMutation(baseOptions?: Apollo.MutationHookOptions<UnbindDocumentTypeTemplateFromDocumentTypeMutation, UnbindDocumentTypeTemplateFromDocumentTypeMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindDocumentTypeTemplateFromDocumentTypeMutation, UnbindDocumentTypeTemplateFromDocumentTypeMutationVariables>(UnbindDocumentTypeTemplateFromDocumentTypeDocument, options);
      }
export type UnbindDocumentTypeTemplateFromDocumentTypeMutationHookResult = ReturnType<typeof useUnbindDocumentTypeTemplateFromDocumentTypeMutation>;
export type UnbindDocumentTypeTemplateFromDocumentTypeMutationResult = Apollo.MutationResult<UnbindDocumentTypeTemplateFromDocumentTypeMutation>;
export type UnbindDocumentTypeTemplateFromDocumentTypeMutationOptions = Apollo.BaseMutationOptions<UnbindDocumentTypeTemplateFromDocumentTypeMutation, UnbindDocumentTypeTemplateFromDocumentTypeMutationVariables>;
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
export const AnalyzerOptionsDocument = gql`
    query AnalyzerOptions($searchText: String, $cursor: String) {
  options: analyzers(searchText: $searchText, first: 5, after: $cursor) {
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
    query AnalyzerValue($id: ID!) {
  value: analyzer(id: $id) {
    id
    name
    description
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
    mutation BindAnalyzerToDocTypeField($documentTypeFieldId: ID!, $analyzerId: ID!) {
  bindAnalyzerToDocTypeField(
    docTypeFieldId: $documentTypeFieldId
    analyzerId: $analyzerId
  ) {
    left {
      id
      analyzer {
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
 *      documentTypeFieldId: // value for 'documentTypeFieldId'
 *      analyzerId: // value for 'analyzerId'
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
export const UnbindnAlyzerToDocTypeFieldDocument = gql`
    mutation UnbindnAlyzerToDocTypeField($documentTypeFieldId: ID!) {
  unbindAnalyzerFromDocTypeField(docTypeFieldId: $documentTypeFieldId) {
    left {
      id
      analyzer {
        id
      }
    }
    right {
      id
    }
  }
}
    `;
export type UnbindnAlyzerToDocTypeFieldMutationFn = Apollo.MutationFunction<UnbindnAlyzerToDocTypeFieldMutation, UnbindnAlyzerToDocTypeFieldMutationVariables>;

/**
 * __useUnbindnAlyzerToDocTypeFieldMutation__
 *
 * To run a mutation, you first call `useUnbindnAlyzerToDocTypeFieldMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindnAlyzerToDocTypeFieldMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindnAlyzerToDocTypeFieldMutation, { data, loading, error }] = useUnbindnAlyzerToDocTypeFieldMutation({
 *   variables: {
 *      documentTypeFieldId: // value for 'documentTypeFieldId'
 *   },
 * });
 */
export function useUnbindnAlyzerToDocTypeFieldMutation(baseOptions?: Apollo.MutationHookOptions<UnbindnAlyzerToDocTypeFieldMutation, UnbindnAlyzerToDocTypeFieldMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindnAlyzerToDocTypeFieldMutation, UnbindnAlyzerToDocTypeFieldMutationVariables>(UnbindnAlyzerToDocTypeFieldDocument, options);
      }
export type UnbindnAlyzerToDocTypeFieldMutationHookResult = ReturnType<typeof useUnbindnAlyzerToDocTypeFieldMutation>;
export type UnbindnAlyzerToDocTypeFieldMutationResult = Apollo.MutationResult<UnbindnAlyzerToDocTypeFieldMutation>;
export type UnbindnAlyzerToDocTypeFieldMutationOptions = Apollo.BaseMutationOptions<UnbindnAlyzerToDocTypeFieldMutation, UnbindnAlyzerToDocTypeFieldMutationVariables>;
export const DeleteDocumentTypeFieldDocument = gql`
    mutation DeleteDocumentTypeField($documentTypeId: ID!, $documentTypeFieldId: ID!) {
  removeDocTypeField(
    docTypeId: $documentTypeId
    docTypeFieldId: $documentTypeFieldId
  ) {
    right
  }
}
    `;
export type DeleteDocumentTypeFieldMutationFn = Apollo.MutationFunction<DeleteDocumentTypeFieldMutation, DeleteDocumentTypeFieldMutationVariables>;

/**
 * __useDeleteDocumentTypeFieldMutation__
 *
 * To run a mutation, you first call `useDeleteDocumentTypeFieldMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteDocumentTypeFieldMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteDocumentTypeFieldMutation, { data, loading, error }] = useDeleteDocumentTypeFieldMutation({
 *   variables: {
 *      documentTypeId: // value for 'documentTypeId'
 *      documentTypeFieldId: // value for 'documentTypeFieldId'
 *   },
 * });
 */
export function useDeleteDocumentTypeFieldMutation(baseOptions?: Apollo.MutationHookOptions<DeleteDocumentTypeFieldMutation, DeleteDocumentTypeFieldMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteDocumentTypeFieldMutation, DeleteDocumentTypeFieldMutationVariables>(DeleteDocumentTypeFieldDocument, options);
      }
export type DeleteDocumentTypeFieldMutationHookResult = ReturnType<typeof useDeleteDocumentTypeFieldMutation>;
export type DeleteDocumentTypeFieldMutationResult = Apollo.MutationResult<DeleteDocumentTypeFieldMutation>;
export type DeleteDocumentTypeFieldMutationOptions = Apollo.BaseMutationOptions<DeleteDocumentTypeFieldMutation, DeleteDocumentTypeFieldMutationVariables>;
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
export const DocumentTypeTemplatesDocument = gql`
    query DocumentTypeTemplates($searchText: String, $cursor: String) {
  docTypeTemplates(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
  docTypes(searchText: $searchText, first: 25, after: $cursor) {
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
export const DeleteDocumentTypeDocument = gql`
    mutation DeleteDocumentType($id: ID!) {
  deleteDocType(docTypeId: $id) {
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
export const EnrichItemDocument = gql`
    query EnrichItem($id: ID!) {
  enrichItem(id: $id) {
    id
    name
    description
    type
    serviceName
    jsonConfig
    validationScript
    behaviorMergeType
    jsonPath
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
    mutation CreateOrUpdateEnrichItem($id: ID, $name: String!, $description: String, $type: EnrichItemType!, $serviceName: String!, $jsonConfig: String, $validationScript: String, $behaviorMergeType: BehaviorMergeType!, $jsonPath: String!) {
  enrichItem(
    id: $id
    enrichItemDTO: {name: $name, description: $description, type: $type, serviceName: $serviceName, jsonConfig: $jsonConfig, validationScript: $validationScript, behaviorMergeType: $behaviorMergeType, jsonPath: $jsonPath}
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
 *      validationScript: // value for 'validationScript'
 *      behaviorMergeType: // value for 'behaviorMergeType'
 *      jsonPath: // value for 'jsonPath'
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
export const EnrichItemsDocument = gql`
    query EnrichItems($searchText: String, $cursor: String) {
  enrichItems(searchText: $searchText, first: 25, after: $cursor) {
    edges {
      node {
        id
        name
        description
        type
        serviceName
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
 *      cursor: // value for 'cursor'
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
    enrichItems(searchText: $searchText, not: true, first: 25) {
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
export const EnrichPipelinesDocument = gql`
    query EnrichPipelines($searchText: String, $cursor: String) {
  enrichPipelines(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const MonitoringEventsDocument = gql`
    query MonitoringEvents($field: EventSortable, $ordering: String) {
  event(sortBy: $field, sortType: $ordering, from: 0, size: 10) {
    id
    className
    created
    groupKey
    size
    type
    version
    classPK
    parsingDate
  }
}
    `;

/**
 * __useMonitoringEventsQuery__
 *
 * To run a query within a React component, call `useMonitoringEventsQuery` and pass it any options that fit your needs.
 * When your component renders, `useMonitoringEventsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useMonitoringEventsQuery({
 *   variables: {
 *      field: // value for 'field'
 *      ordering: // value for 'ordering'
 *   },
 * });
 */
export function useMonitoringEventsQuery(baseOptions?: Apollo.QueryHookOptions<MonitoringEventsQuery, MonitoringEventsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<MonitoringEventsQuery, MonitoringEventsQueryVariables>(MonitoringEventsDocument, options);
      }
export function useMonitoringEventsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<MonitoringEventsQuery, MonitoringEventsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<MonitoringEventsQuery, MonitoringEventsQueryVariables>(MonitoringEventsDocument, options);
        }
export type MonitoringEventsQueryHookResult = ReturnType<typeof useMonitoringEventsQuery>;
export type MonitoringEventsLazyQueryHookResult = ReturnType<typeof useMonitoringEventsLazyQuery>;
export type MonitoringEventsQueryResult = Apollo.QueryResult<MonitoringEventsQuery, MonitoringEventsQueryVariables>;
export const MonitoringEventDataDocument = gql`
    query MonitoringEventData($id: String) {
  eventData(id: $id)
}
    `;

/**
 * __useMonitoringEventDataQuery__
 *
 * To run a query within a React component, call `useMonitoringEventDataQuery` and pass it any options that fit your needs.
 * When your component renders, `useMonitoringEventDataQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useMonitoringEventDataQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useMonitoringEventDataQuery(baseOptions?: Apollo.QueryHookOptions<MonitoringEventDataQuery, MonitoringEventDataQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<MonitoringEventDataQuery, MonitoringEventDataQueryVariables>(MonitoringEventDataDocument, options);
      }
export function useMonitoringEventDataLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<MonitoringEventDataQuery, MonitoringEventDataQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<MonitoringEventDataQuery, MonitoringEventDataQueryVariables>(MonitoringEventDataDocument, options);
        }
export type MonitoringEventDataQueryHookResult = ReturnType<typeof useMonitoringEventDataQuery>;
export type MonitoringEventDataLazyQueryHookResult = ReturnType<typeof useMonitoringEventDataLazyQuery>;
export type MonitoringEventDataQueryResult = Apollo.QueryResult<MonitoringEventDataQuery, MonitoringEventDataQueryVariables>;
export const PluginDriverDocument = gql`
    query PluginDriver($id: ID!) {
  pluginDriver(id: $id) {
    id
    name
    description
    type
    jsonConfig
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
export const CreateOrUpdatePluginDriverDocument = gql`
    mutation CreateOrUpdatePluginDriver($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $jsonConfig: String) {
  pluginDriver(
    id: $id
    pluginDriverDTO: {name: $name, description: $description, type: $type, jsonConfig: $jsonConfig}
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
      }
    }
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
export const AddDocumentTypeFieldToPluginDriversDocument = gql`
    mutation AddDocumentTypeFieldToPluginDrivers($childId: ID!, $parentId: ID!, $userField: UserField) {
  addDocTypeFieldToPluginDriver(
    docTypeFieldId: $childId
    pluginDriverId: $parentId
    userField: $userField
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
export type AddDocumentTypeFieldToPluginDriversMutationFn = Apollo.MutationFunction<AddDocumentTypeFieldToPluginDriversMutation, AddDocumentTypeFieldToPluginDriversMutationVariables>;

/**
 * __useAddDocumentTypeFieldToPluginDriversMutation__
 *
 * To run a mutation, you first call `useAddDocumentTypeFieldToPluginDriversMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddDocumentTypeFieldToPluginDriversMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addDocumentTypeFieldToPluginDriversMutation, { data, loading, error }] = useAddDocumentTypeFieldToPluginDriversMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *      userField: // value for 'userField'
 *   },
 * });
 */
export function useAddDocumentTypeFieldToPluginDriversMutation(baseOptions?: Apollo.MutationHookOptions<AddDocumentTypeFieldToPluginDriversMutation, AddDocumentTypeFieldToPluginDriversMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddDocumentTypeFieldToPluginDriversMutation, AddDocumentTypeFieldToPluginDriversMutationVariables>(AddDocumentTypeFieldToPluginDriversDocument, options);
      }
export type AddDocumentTypeFieldToPluginDriversMutationHookResult = ReturnType<typeof useAddDocumentTypeFieldToPluginDriversMutation>;
export type AddDocumentTypeFieldToPluginDriversMutationResult = Apollo.MutationResult<AddDocumentTypeFieldToPluginDriversMutation>;
export type AddDocumentTypeFieldToPluginDriversMutationOptions = Apollo.BaseMutationOptions<AddDocumentTypeFieldToPluginDriversMutation, AddDocumentTypeFieldToPluginDriversMutationVariables>;
export const RemoveDocumentTypeFieldFromPluginDriversDocument = gql`
    mutation RemoveDocumentTypeFieldFromPluginDrivers($childId: ID!, $parentId: ID!) {
  removeDocTypeFieldFromPluginDriver(
    docTypeFieldId: $childId
    pluginDriverId: $parentId
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
export type RemoveDocumentTypeFieldFromPluginDriversMutationFn = Apollo.MutationFunction<RemoveDocumentTypeFieldFromPluginDriversMutation, RemoveDocumentTypeFieldFromPluginDriversMutationVariables>;

/**
 * __useRemoveDocumentTypeFieldFromPluginDriversMutation__
 *
 * To run a mutation, you first call `useRemoveDocumentTypeFieldFromPluginDriversMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveDocumentTypeFieldFromPluginDriversMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeDocumentTypeFieldFromPluginDriversMutation, { data, loading, error }] = useRemoveDocumentTypeFieldFromPluginDriversMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveDocumentTypeFieldFromPluginDriversMutation(baseOptions?: Apollo.MutationHookOptions<RemoveDocumentTypeFieldFromPluginDriversMutation, RemoveDocumentTypeFieldFromPluginDriversMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveDocumentTypeFieldFromPluginDriversMutation, RemoveDocumentTypeFieldFromPluginDriversMutationVariables>(RemoveDocumentTypeFieldFromPluginDriversDocument, options);
      }
export type RemoveDocumentTypeFieldFromPluginDriversMutationHookResult = ReturnType<typeof useRemoveDocumentTypeFieldFromPluginDriversMutation>;
export type RemoveDocumentTypeFieldFromPluginDriversMutationResult = Apollo.MutationResult<RemoveDocumentTypeFieldFromPluginDriversMutation>;
export type RemoveDocumentTypeFieldFromPluginDriversMutationOptions = Apollo.BaseMutationOptions<RemoveDocumentTypeFieldFromPluginDriversMutation, RemoveDocumentTypeFieldFromPluginDriversMutationVariables>;
export const ChangeUserfieldDocument = gql`
    mutation ChangeUserfield($docTypeFieldId: ID!, $pluginDriverId: ID!, $userField: UserField) {
  userField(
    docTypeFieldId: $docTypeFieldId
    pluginDriverId: $pluginDriverId
    userField: $userField
  ) {
    userField
  }
}
    `;
export type ChangeUserfieldMutationFn = Apollo.MutationFunction<ChangeUserfieldMutation, ChangeUserfieldMutationVariables>;

/**
 * __useChangeUserfieldMutation__
 *
 * To run a mutation, you first call `useChangeUserfieldMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useChangeUserfieldMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [changeUserfieldMutation, { data, loading, error }] = useChangeUserfieldMutation({
 *   variables: {
 *      docTypeFieldId: // value for 'docTypeFieldId'
 *      pluginDriverId: // value for 'pluginDriverId'
 *      userField: // value for 'userField'
 *   },
 * });
 */
export function useChangeUserfieldMutation(baseOptions?: Apollo.MutationHookOptions<ChangeUserfieldMutation, ChangeUserfieldMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<ChangeUserfieldMutation, ChangeUserfieldMutationVariables>(ChangeUserfieldDocument, options);
      }
export type ChangeUserfieldMutationHookResult = ReturnType<typeof useChangeUserfieldMutation>;
export type ChangeUserfieldMutationResult = Apollo.MutationResult<ChangeUserfieldMutation>;
export type ChangeUserfieldMutationOptions = Apollo.BaseMutationOptions<ChangeUserfieldMutation, ChangeUserfieldMutationVariables>;
export const PluginDriversDocument = gql`
    query PluginDrivers($searchText: String, $cursor: String) {
  pluginDrivers(searchText: $searchText, first: 25, after: $cursor) {
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
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
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
export const QueryAnalysesDocument = gql`
    query QueryAnalyses($searchText: String, $cursor: String) {
  queryAnalyses(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const QueryAnalysesAnnotatorsDocument = gql`
    query QueryAnalysesAnnotators($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  queryAnalysis(id: $parentId) {
    id
    annotators(
      searchText: $searchText
      notEqual: $unassociated
      first: 25
      after: $cursor
    ) {
      edges {
        node {
          id
          name
          fieldName
          fuziness
          size
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
}
    `;

/**
 * __useQueryAnalysesAnnotatorsQuery__
 *
 * To run a query within a React component, call `useQueryAnalysesAnnotatorsQuery` and pass it any options that fit your needs.
 * When your component renders, `useQueryAnalysesAnnotatorsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQueryAnalysesAnnotatorsQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *      searchText: // value for 'searchText'
 *      unassociated: // value for 'unassociated'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useQueryAnalysesAnnotatorsQuery(baseOptions: Apollo.QueryHookOptions<QueryAnalysesAnnotatorsQuery, QueryAnalysesAnnotatorsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysesAnnotatorsQuery, QueryAnalysesAnnotatorsQueryVariables>(QueryAnalysesAnnotatorsDocument, options);
      }
export function useQueryAnalysesAnnotatorsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysesAnnotatorsQuery, QueryAnalysesAnnotatorsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysesAnnotatorsQuery, QueryAnalysesAnnotatorsQueryVariables>(QueryAnalysesAnnotatorsDocument, options);
        }
export type QueryAnalysesAnnotatorsQueryHookResult = ReturnType<typeof useQueryAnalysesAnnotatorsQuery>;
export type QueryAnalysesAnnotatorsLazyQueryHookResult = ReturnType<typeof useQueryAnalysesAnnotatorsLazyQuery>;
export type QueryAnalysesAnnotatorsQueryResult = Apollo.QueryResult<QueryAnalysesAnnotatorsQuery, QueryAnalysesAnnotatorsQueryVariables>;
export const AddAnnotatorsToQueryAnalysesDocument = gql`
    mutation AddAnnotatorsToQueryAnalyses($childId: ID!, $parentId: ID!) {
  addAnnotatorToQueryAnalysis(annotatorId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type AddAnnotatorsToQueryAnalysesMutationFn = Apollo.MutationFunction<AddAnnotatorsToQueryAnalysesMutation, AddAnnotatorsToQueryAnalysesMutationVariables>;

/**
 * __useAddAnnotatorsToQueryAnalysesMutation__
 *
 * To run a mutation, you first call `useAddAnnotatorsToQueryAnalysesMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddAnnotatorsToQueryAnalysesMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addAnnotatorsToQueryAnalysesMutation, { data, loading, error }] = useAddAnnotatorsToQueryAnalysesMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddAnnotatorsToQueryAnalysesMutation(baseOptions?: Apollo.MutationHookOptions<AddAnnotatorsToQueryAnalysesMutation, AddAnnotatorsToQueryAnalysesMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddAnnotatorsToQueryAnalysesMutation, AddAnnotatorsToQueryAnalysesMutationVariables>(AddAnnotatorsToQueryAnalysesDocument, options);
      }
export type AddAnnotatorsToQueryAnalysesMutationHookResult = ReturnType<typeof useAddAnnotatorsToQueryAnalysesMutation>;
export type AddAnnotatorsToQueryAnalysesMutationResult = Apollo.MutationResult<AddAnnotatorsToQueryAnalysesMutation>;
export type AddAnnotatorsToQueryAnalysesMutationOptions = Apollo.BaseMutationOptions<AddAnnotatorsToQueryAnalysesMutation, AddAnnotatorsToQueryAnalysesMutationVariables>;
export const RemoveAnnotatorFromQueryAnalysesDocument = gql`
    mutation RemoveAnnotatorFromQueryAnalyses($childId: ID!, $parentId: ID!) {
  removeAnnotatorFromQueryAnalysis(annotatorId: $childId, id: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveAnnotatorFromQueryAnalysesMutationFn = Apollo.MutationFunction<RemoveAnnotatorFromQueryAnalysesMutation, RemoveAnnotatorFromQueryAnalysesMutationVariables>;

/**
 * __useRemoveAnnotatorFromQueryAnalysesMutation__
 *
 * To run a mutation, you first call `useRemoveAnnotatorFromQueryAnalysesMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveAnnotatorFromQueryAnalysesMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeAnnotatorFromQueryAnalysesMutation, { data, loading, error }] = useRemoveAnnotatorFromQueryAnalysesMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveAnnotatorFromQueryAnalysesMutation(baseOptions?: Apollo.MutationHookOptions<RemoveAnnotatorFromQueryAnalysesMutation, RemoveAnnotatorFromQueryAnalysesMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveAnnotatorFromQueryAnalysesMutation, RemoveAnnotatorFromQueryAnalysesMutationVariables>(RemoveAnnotatorFromQueryAnalysesDocument, options);
      }
export type RemoveAnnotatorFromQueryAnalysesMutationHookResult = ReturnType<typeof useRemoveAnnotatorFromQueryAnalysesMutation>;
export type RemoveAnnotatorFromQueryAnalysesMutationResult = Apollo.MutationResult<RemoveAnnotatorFromQueryAnalysesMutation>;
export type RemoveAnnotatorFromQueryAnalysesMutationOptions = Apollo.BaseMutationOptions<RemoveAnnotatorFromQueryAnalysesMutation, RemoveAnnotatorFromQueryAnalysesMutationVariables>;
export const QueryAnalysesRulesDocument = gql`
    query QueryAnalysesRules($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  queryAnalysis(id: $parentId) {
    id
    rules(
      searchText: $searchText
      notEqual: $unassociated
      first: 25
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
export const CreateOrUpdateQueryAnalysisDocument = gql`
    mutation CreateOrUpdateQueryAnalysis($id: ID, $name: String!, $description: String, $stopWords: String) {
  queryAnalysis(
    id: $id
    queryAnalysisDTO: {name: $name, description: $description, stopWords: $stopWords}
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
export const QueryParserConfigDocument = gql`
    query QueryParserConfig($id: ID!) {
  queryParserConfig(id: $id) {
    id
    name
    description
    type
    jsonConfig
  }
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
 *      id: // value for 'id'
 *   },
 * });
 */
export function useQueryParserConfigQuery(baseOptions: Apollo.QueryHookOptions<QueryParserConfigQuery, QueryParserConfigQueryVariables>) {
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
export const CreateOrUpdateQueryParserConfigDocument = gql`
    mutation CreateOrUpdateQueryParserConfig($queryParserConfigId: ID, $searchConfigId: ID!, $name: String!, $description: String, $type: String!, $jsonConfig: String) {
  queryParserConfig(
    searchConfigId: $searchConfigId
    queryParserConfigId: $queryParserConfigId
    queryParserConfigDTO: {name: $name, description: $description, type: $type, jsonConfig: $jsonConfig}
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
export type CreateOrUpdateQueryParserConfigMutationFn = Apollo.MutationFunction<CreateOrUpdateQueryParserConfigMutation, CreateOrUpdateQueryParserConfigMutationVariables>;

/**
 * __useCreateOrUpdateQueryParserConfigMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateQueryParserConfigMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateQueryParserConfigMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateQueryParserConfigMutation, { data, loading, error }] = useCreateOrUpdateQueryParserConfigMutation({
 *   variables: {
 *      queryParserConfigId: // value for 'queryParserConfigId'
 *      searchConfigId: // value for 'searchConfigId'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      type: // value for 'type'
 *      jsonConfig: // value for 'jsonConfig'
 *   },
 * });
 */
export function useCreateOrUpdateQueryParserConfigMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateQueryParserConfigMutation, CreateOrUpdateQueryParserConfigMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateQueryParserConfigMutation, CreateOrUpdateQueryParserConfigMutationVariables>(CreateOrUpdateQueryParserConfigDocument, options);
      }
export type CreateOrUpdateQueryParserConfigMutationHookResult = ReturnType<typeof useCreateOrUpdateQueryParserConfigMutation>;
export type CreateOrUpdateQueryParserConfigMutationResult = Apollo.MutationResult<CreateOrUpdateQueryParserConfigMutation>;
export type CreateOrUpdateQueryParserConfigMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateQueryParserConfigMutation, CreateOrUpdateQueryParserConfigMutationVariables>;
export const QueryParserConfigsDocument = gql`
    query QueryParserConfigs($queryParserConfigId: ID!, $searchText: String, $cursor: String) {
  queryParserConfigs(
    searchConfigId: $queryParserConfigId
    searchText: $searchText
    first: 25
    after: $cursor
  ) {
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
 * __useQueryParserConfigsQuery__
 *
 * To run a query within a React component, call `useQueryParserConfigsQuery` and pass it any options that fit your needs.
 * When your component renders, `useQueryParserConfigsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useQueryParserConfigsQuery({
 *   variables: {
 *      queryParserConfigId: // value for 'queryParserConfigId'
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useQueryParserConfigsQuery(baseOptions: Apollo.QueryHookOptions<QueryParserConfigsQuery, QueryParserConfigsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryParserConfigsQuery, QueryParserConfigsQueryVariables>(QueryParserConfigsDocument, options);
      }
export function useQueryParserConfigsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryParserConfigsQuery, QueryParserConfigsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryParserConfigsQuery, QueryParserConfigsQueryVariables>(QueryParserConfigsDocument, options);
        }
export type QueryParserConfigsQueryHookResult = ReturnType<typeof useQueryParserConfigsQuery>;
export type QueryParserConfigsLazyQueryHookResult = ReturnType<typeof useQueryParserConfigsLazyQuery>;
export type QueryParserConfigsQueryResult = Apollo.QueryResult<QueryParserConfigsQuery, QueryParserConfigsQueryVariables>;
export const DeleteQueryParserDocument = gql`
    mutation DeleteQueryParser($searchConfigId: ID!, $queryParserConfigId: ID!) {
  removeQueryParserConfig(
    queryParserConfigId: $queryParserConfigId
    searchConfigId: $searchConfigId
  ) {
    left {
      id
    }
  }
}
    `;
export type DeleteQueryParserMutationFn = Apollo.MutationFunction<DeleteQueryParserMutation, DeleteQueryParserMutationVariables>;

/**
 * __useDeleteQueryParserMutation__
 *
 * To run a mutation, you first call `useDeleteQueryParserMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteQueryParserMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteQueryParserMutation, { data, loading, error }] = useDeleteQueryParserMutation({
 *   variables: {
 *      searchConfigId: // value for 'searchConfigId'
 *      queryParserConfigId: // value for 'queryParserConfigId'
 *   },
 * });
 */
export function useDeleteQueryParserMutation(baseOptions?: Apollo.MutationHookOptions<DeleteQueryParserMutation, DeleteQueryParserMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteQueryParserMutation, DeleteQueryParserMutationVariables>(DeleteQueryParserDocument, options);
      }
export type DeleteQueryParserMutationHookResult = ReturnType<typeof useDeleteQueryParserMutation>;
export type DeleteQueryParserMutationResult = Apollo.MutationResult<DeleteQueryParserMutation>;
export type DeleteQueryParserMutationOptions = Apollo.BaseMutationOptions<DeleteQueryParserMutation, DeleteQueryParserMutationVariables>;
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
export const RulesDocument = gql`
    query Rules($searchText: String, $cursor: String) {
  rules(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const SearchConfigDocument = gql`
    query SearchConfig($id: ID!) {
  searchConfig(id: $id) {
    id
    name
    description
    minScore
    minScoreSuggestions
    minScoreSearch
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
    mutation CreateOrUpdateSearchConfig($id: ID, $name: String!, $description: String, $minScore: Float!, $minScoreSuggestions: Boolean!, $minScoreSearch: Boolean!) {
  searchConfig(
    id: $id
    searchConfigDTO: {name: $name, description: $description, minScore: $minScore, minScoreSuggestions: $minScoreSuggestions, minScoreSearch: $minScoreSearch}
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
    query SearchConfigs($searchText: String, $cursor: String) {
  searchConfigs(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const DocumentTypeFieldsDocument = gql`
    query DocumentTypeFields($documentTypeId: ID!, $searchText: String, $cursor: String) {
  docTypeFieldsFromDocType(
    docTypeId: $documentTypeId
    searchText: $searchText
    first: 25
    after: $cursor
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
        sortable
        subFields {
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
              sortable
            }
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
 * __useDocumentTypeFieldsQuery__
 *
 * To run a query within a React component, call `useDocumentTypeFieldsQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocumentTypeFieldsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocumentTypeFieldsQuery({
 *   variables: {
 *      documentTypeId: // value for 'documentTypeId'
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useDocumentTypeFieldsQuery(baseOptions: Apollo.QueryHookOptions<DocumentTypeFieldsQuery, DocumentTypeFieldsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeFieldsQuery, DocumentTypeFieldsQueryVariables>(DocumentTypeFieldsDocument, options);
      }
export function useDocumentTypeFieldsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeFieldsQuery, DocumentTypeFieldsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeFieldsQuery, DocumentTypeFieldsQueryVariables>(DocumentTypeFieldsDocument, options);
        }
export type DocumentTypeFieldsQueryHookResult = ReturnType<typeof useDocumentTypeFieldsQuery>;
export type DocumentTypeFieldsLazyQueryHookResult = ReturnType<typeof useDocumentTypeFieldsLazyQuery>;
export type DocumentTypeFieldsQueryResult = Apollo.QueryResult<DocumentTypeFieldsQuery, DocumentTypeFieldsQueryVariables>;
export const CreateOrUpdateDocumentTypeFieldDocument = gql`
    mutation CreateOrUpdateDocumentTypeField($documentTypeId: ID!, $documentTypeFieldId: ID, $name: String!, $fieldName: String!, $description: String, $fieldType: FieldType!, $boost: Float, $searchable: Boolean!, $exclude: Boolean, $jsonConfig: String, $sortable: Boolean!) {
  docTypeField(
    docTypeId: $documentTypeId
    docTypeFieldId: $documentTypeFieldId
    docTypeFieldDTO: {name: $name, description: $description, fieldType: $fieldType, boost: $boost, searchable: $searchable, exclude: $exclude, fieldName: $fieldName, jsonConfig: $jsonConfig, sortable: $sortable}
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
export const CreateDocumentTypeSubFieldsDocument = gql`
    mutation createDocumentTypeSubFields($parentDocTypeFieldId: ID!, $name: String!, $fieldName: String!, $jsonConfig: String, $searchable: Boolean!, $boost: Float, $fieldType: FieldType!, $description: String, $sortable: Boolean!) {
  createSubField(
    parentDocTypeFieldId: $parentDocTypeFieldId
    docTypeFieldDTO: {name: $name, fieldName: $fieldName, jsonConfig: $jsonConfig, searchable: $searchable, boost: $boost, fieldType: $fieldType, description: $description, sortable: $sortable}
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
export type CreateDocumentTypeSubFieldsMutationFn = Apollo.MutationFunction<CreateDocumentTypeSubFieldsMutation, CreateDocumentTypeSubFieldsMutationVariables>;

/**
 * __useCreateDocumentTypeSubFieldsMutation__
 *
 * To run a mutation, you first call `useCreateDocumentTypeSubFieldsMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateDocumentTypeSubFieldsMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createDocumentTypeSubFieldsMutation, { data, loading, error }] = useCreateDocumentTypeSubFieldsMutation({
 *   variables: {
 *      parentDocTypeFieldId: // value for 'parentDocTypeFieldId'
 *      name: // value for 'name'
 *      fieldName: // value for 'fieldName'
 *      jsonConfig: // value for 'jsonConfig'
 *      searchable: // value for 'searchable'
 *      boost: // value for 'boost'
 *      fieldType: // value for 'fieldType'
 *      description: // value for 'description'
 *      sortable: // value for 'sortable'
 *   },
 * });
 */
export function useCreateDocumentTypeSubFieldsMutation(baseOptions?: Apollo.MutationHookOptions<CreateDocumentTypeSubFieldsMutation, CreateDocumentTypeSubFieldsMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateDocumentTypeSubFieldsMutation, CreateDocumentTypeSubFieldsMutationVariables>(CreateDocumentTypeSubFieldsDocument, options);
      }
export type CreateDocumentTypeSubFieldsMutationHookResult = ReturnType<typeof useCreateDocumentTypeSubFieldsMutation>;
export type CreateDocumentTypeSubFieldsMutationResult = Apollo.MutationResult<CreateDocumentTypeSubFieldsMutation>;
export type CreateDocumentTypeSubFieldsMutationOptions = Apollo.BaseMutationOptions<CreateDocumentTypeSubFieldsMutation, CreateDocumentTypeSubFieldsMutationVariables>;
export const SuggestionCategoriesDocument = gql`
    query SuggestionCategories($searchText: String, $cursor: String) {
  suggestionCategories(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const SuggestionCategoryDocument = gql`
    query SuggestionCategory($id: ID!) {
  suggestionCategory(id: $id) {
    id
    name
    description
    priority
    multiSelect
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
export const CreateOrUpdateSuggestionCategoryDocument = gql`
    mutation CreateOrUpdateSuggestionCategory($id: ID, $name: String!, $description: String, $priority: Float!, $multiSelect: Boolean!) {
  suggestionCategory(
    id: $id
    suggestionCategoryDTO: {name: $name, description: $description, priority: $priority, multiSelect: $multiSelect}
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
export const SuggestionCategoryDocumentTypeFieldsDocument = gql`
    query SuggestionCategoryDocumentTypeFields($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  suggestionCategory(id: $parentId) {
    id
    docTypeFields(
      searchText: $searchText
      notEqual: $unassociated
      first: 25
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
export const RemoveDocumentTypeFieldFromSuggestionCategoryDocument = gql`
    mutation RemoveDocumentTypeFieldFromSuggestionCategory($childId: ID!, $parentId: ID!) {
  removeDocTypeFieldFromSuggestionCategory(
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
export type RemoveDocumentTypeFieldFromSuggestionCategoryMutationFn = Apollo.MutationFunction<RemoveDocumentTypeFieldFromSuggestionCategoryMutation, RemoveDocumentTypeFieldFromSuggestionCategoryMutationVariables>;

/**
 * __useRemoveDocumentTypeFieldFromSuggestionCategoryMutation__
 *
 * To run a mutation, you first call `useRemoveDocumentTypeFieldFromSuggestionCategoryMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveDocumentTypeFieldFromSuggestionCategoryMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeDocumentTypeFieldFromSuggestionCategoryMutation, { data, loading, error }] = useRemoveDocumentTypeFieldFromSuggestionCategoryMutation({
 *   variables: {
 *      childId: // value for 'childId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveDocumentTypeFieldFromSuggestionCategoryMutation(baseOptions?: Apollo.MutationHookOptions<RemoveDocumentTypeFieldFromSuggestionCategoryMutation, RemoveDocumentTypeFieldFromSuggestionCategoryMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveDocumentTypeFieldFromSuggestionCategoryMutation, RemoveDocumentTypeFieldFromSuggestionCategoryMutationVariables>(RemoveDocumentTypeFieldFromSuggestionCategoryDocument, options);
      }
export type RemoveDocumentTypeFieldFromSuggestionCategoryMutationHookResult = ReturnType<typeof useRemoveDocumentTypeFieldFromSuggestionCategoryMutation>;
export type RemoveDocumentTypeFieldFromSuggestionCategoryMutationResult = Apollo.MutationResult<RemoveDocumentTypeFieldFromSuggestionCategoryMutation>;
export type RemoveDocumentTypeFieldFromSuggestionCategoryMutationOptions = Apollo.BaseMutationOptions<RemoveDocumentTypeFieldFromSuggestionCategoryMutation, RemoveDocumentTypeFieldFromSuggestionCategoryMutationVariables>;
export const TabDocument = gql`
    query Tab($id: ID!) {
  tab(id: $id) {
    id
    name
    description
    priority
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
    mutation CreateOrUpdateTab($id: ID, $name: String!, $description: String, $priority: Int!) {
  tab(
    id: $id
    tabDTO: {name: $name, description: $description, priority: $priority}
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
    }
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
    mutation CreateOrUpdateTabToken($tabId: ID!, $tabTokenId: ID, $name: String!, $description: String, $value: String!, $filter: Boolean!, $tokenType: TokenType!) {
  tokenTab(
    tabId: $tabId
    tokenTabId: $tabTokenId
    tokenTabDTO: {name: $name, description: $description, filter: $filter, tokenType: $tokenType, value: $value}
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
 *      tabId: // value for 'tabId'
 *      tabTokenId: // value for 'tabTokenId'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      value: // value for 'value'
 *      filter: // value for 'filter'
 *      tokenType: // value for 'tokenType'
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
export const DocTypeFieldOptionsTokenTabDocument = gql`
    query DocTypeFieldOptionsTokenTab($searchText: String, $cursor: String) {
  options: docTypeFields(searchText: $searchText, first: 5, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const BindDocTypeFieldToTabTokenDocument = gql`
    mutation BindDocTypeFieldToTabToken($documentTypeFieldId: ID!, $tokenTabId: ID!) {
  bindDocTypeFieldToTokenTab(
    docTypeFieldId: $documentTypeFieldId
    tokenTabId: $tokenTabId
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
export type BindDocTypeFieldToTabTokenMutationFn = Apollo.MutationFunction<BindDocTypeFieldToTabTokenMutation, BindDocTypeFieldToTabTokenMutationVariables>;

/**
 * __useBindDocTypeFieldToTabTokenMutation__
 *
 * To run a mutation, you first call `useBindDocTypeFieldToTabTokenMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useBindDocTypeFieldToTabTokenMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [bindDocTypeFieldToTabTokenMutation, { data, loading, error }] = useBindDocTypeFieldToTabTokenMutation({
 *   variables: {
 *      documentTypeFieldId: // value for 'documentTypeFieldId'
 *      tokenTabId: // value for 'tokenTabId'
 *   },
 * });
 */
export function useBindDocTypeFieldToTabTokenMutation(baseOptions?: Apollo.MutationHookOptions<BindDocTypeFieldToTabTokenMutation, BindDocTypeFieldToTabTokenMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<BindDocTypeFieldToTabTokenMutation, BindDocTypeFieldToTabTokenMutationVariables>(BindDocTypeFieldToTabTokenDocument, options);
      }
export type BindDocTypeFieldToTabTokenMutationHookResult = ReturnType<typeof useBindDocTypeFieldToTabTokenMutation>;
export type BindDocTypeFieldToTabTokenMutationResult = Apollo.MutationResult<BindDocTypeFieldToTabTokenMutation>;
export type BindDocTypeFieldToTabTokenMutationOptions = Apollo.BaseMutationOptions<BindDocTypeFieldToTabTokenMutation, BindDocTypeFieldToTabTokenMutationVariables>;
export const UnbindDocTypeFieldToTabTokenDocument = gql`
    mutation UnbindDocTypeFieldToTabToken($documentTypeFieldId: ID!, $tokenTabId: ID!) {
  unbindDocTypeFieldFromTokenTab(
    docTypeFieldId: $documentTypeFieldId
    id: $tokenTabId
  ) {
    left {
      id
    }
  }
}
    `;
export type UnbindDocTypeFieldToTabTokenMutationFn = Apollo.MutationFunction<UnbindDocTypeFieldToTabTokenMutation, UnbindDocTypeFieldToTabTokenMutationVariables>;

/**
 * __useUnbindDocTypeFieldToTabTokenMutation__
 *
 * To run a mutation, you first call `useUnbindDocTypeFieldToTabTokenMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUnbindDocTypeFieldToTabTokenMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [unbindDocTypeFieldToTabTokenMutation, { data, loading, error }] = useUnbindDocTypeFieldToTabTokenMutation({
 *   variables: {
 *      documentTypeFieldId: // value for 'documentTypeFieldId'
 *      tokenTabId: // value for 'tokenTabId'
 *   },
 * });
 */
export function useUnbindDocTypeFieldToTabTokenMutation(baseOptions?: Apollo.MutationHookOptions<UnbindDocTypeFieldToTabTokenMutation, UnbindDocTypeFieldToTabTokenMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UnbindDocTypeFieldToTabTokenMutation, UnbindDocTypeFieldToTabTokenMutationVariables>(UnbindDocTypeFieldToTabTokenDocument, options);
      }
export type UnbindDocTypeFieldToTabTokenMutationHookResult = ReturnType<typeof useUnbindDocTypeFieldToTabTokenMutation>;
export type UnbindDocTypeFieldToTabTokenMutationResult = Apollo.MutationResult<UnbindDocTypeFieldToTabTokenMutation>;
export type UnbindDocTypeFieldToTabTokenMutationOptions = Apollo.BaseMutationOptions<UnbindDocTypeFieldToTabTokenMutation, UnbindDocTypeFieldToTabTokenMutationVariables>;
export const TabTokensDocument = gql`
    query TabTokens($tabId: ID!, $searchText: String, $cursor: String) {
  tokenTabs(tabId: $tabId, searchText: $searchText, first: 25, after: $cursor) {
    edges {
      node {
        id
        name
        tokenType
        value
        filter
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
 *      tabId: // value for 'tabId'
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useTabTokensQuery(baseOptions: Apollo.QueryHookOptions<TabTokensQuery, TabTokensQueryVariables>) {
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
export const DeleteTabTokenTabDocument = gql`
    mutation DeleteTabTokenTab($tabId: ID!, $TabTokenTabs: ID!) {
  removeTokenTab(tabId: $tabId, tokenTabId: $TabTokenTabs) {
    right
  }
}
    `;
export type DeleteTabTokenTabMutationFn = Apollo.MutationFunction<DeleteTabTokenTabMutation, DeleteTabTokenTabMutationVariables>;

/**
 * __useDeleteTabTokenTabMutation__
 *
 * To run a mutation, you first call `useDeleteTabTokenTabMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteTabTokenTabMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteTabTokenTabMutation, { data, loading, error }] = useDeleteTabTokenTabMutation({
 *   variables: {
 *      tabId: // value for 'tabId'
 *      TabTokenTabs: // value for 'TabTokenTabs'
 *   },
 * });
 */
export function useDeleteTabTokenTabMutation(baseOptions?: Apollo.MutationHookOptions<DeleteTabTokenTabMutation, DeleteTabTokenTabMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteTabTokenTabMutation, DeleteTabTokenTabMutationVariables>(DeleteTabTokenTabDocument, options);
      }
export type DeleteTabTokenTabMutationHookResult = ReturnType<typeof useDeleteTabTokenTabMutation>;
export type DeleteTabTokenTabMutationResult = Apollo.MutationResult<DeleteTabTokenTabMutation>;
export type DeleteTabTokenTabMutationOptions = Apollo.BaseMutationOptions<DeleteTabTokenTabMutation, DeleteTabTokenTabMutationVariables>;
export const TabsDocument = gql`
    query Tabs($searchText: String, $cursor: String) {
  tabs(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const TokenFilterDocument = gql`
    query TokenFilter($id: ID!) {
  tokenFilter(id: $id) {
    id
    name
    description
    jsonConfig
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
    mutation CreateOrUpdateTokenFilter($id: ID, $name: String!, $description: String, $jsonConfig: String) {
  tokenFilter(
    id: $id
    tokenFilterDTO: {name: $name, description: $description, jsonConfig: $jsonConfig}
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
export const TokenFiltersDocument = gql`
    query TokenFilters($searchText: String, $cursor: String) {
  tokenFilters(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const TokenizerDocument = gql`
    query Tokenizer($id: ID!) {
  tokenizer(id: $id) {
    id
    name
    description
    jsonConfig
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
    mutation CreateOrUpdateTokenizer($id: ID, $name: String!, $description: String, $jsonConfig: String) {
  tokenizer(
    id: $id
    tokenizerDTO: {name: $name, description: $description, jsonConfig: $jsonConfig}
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
    query Tokenizers($searchText: String, $cursor: String) {
  tokenizers(searchText: $searchText, first: 25, after: $cursor) {
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
 *      cursor: // value for 'cursor'
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
export const CreateSitemapDataSourceDocument = gql`
    mutation CreateSitemapDataSource($name: String!, $description: String, $schedulable: Boolean!, $scheduling: String!, $jsonConfig: String) {
  datasource(
    datasourceDTO: {name: $name, description: $description, schedulable: $schedulable, scheduling: $scheduling, jsonConfig: $jsonConfig}
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
export type CreateSitemapDataSourceMutationFn = Apollo.MutationFunction<CreateSitemapDataSourceMutation, CreateSitemapDataSourceMutationVariables>;

/**
 * __useCreateSitemapDataSourceMutation__
 *
 * To run a mutation, you first call `useCreateSitemapDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateSitemapDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createSitemapDataSourceMutation, { data, loading, error }] = useCreateSitemapDataSourceMutation({
 *   variables: {
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      schedulable: // value for 'schedulable'
 *      scheduling: // value for 'scheduling'
 *      jsonConfig: // value for 'jsonConfig'
 *   },
 * });
 */
export function useCreateSitemapDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<CreateSitemapDataSourceMutation, CreateSitemapDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateSitemapDataSourceMutation, CreateSitemapDataSourceMutationVariables>(CreateSitemapDataSourceDocument, options);
      }
export type CreateSitemapDataSourceMutationHookResult = ReturnType<typeof useCreateSitemapDataSourceMutation>;
export type CreateSitemapDataSourceMutationResult = Apollo.MutationResult<CreateSitemapDataSourceMutation>;
export type CreateSitemapDataSourceMutationOptions = Apollo.BaseMutationOptions<CreateSitemapDataSourceMutation, CreateSitemapDataSourceMutationVariables>;
export const CreateWebCrawlerDataSourceDocument = gql`
    mutation CreateWebCrawlerDataSource($name: String!, $description: String, $schedulable: Boolean!, $scheduling: String!, $jsonConfig: String) {
  datasource(
    datasourceDTO: {name: $name, description: $description, schedulable: $schedulable, scheduling: $scheduling, jsonConfig: $jsonConfig}
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
export type CreateWebCrawlerDataSourceMutationFn = Apollo.MutationFunction<CreateWebCrawlerDataSourceMutation, CreateWebCrawlerDataSourceMutationVariables>;

/**
 * __useCreateWebCrawlerDataSourceMutation__
 *
 * To run a mutation, you first call `useCreateWebCrawlerDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateWebCrawlerDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createWebCrawlerDataSourceMutation, { data, loading, error }] = useCreateWebCrawlerDataSourceMutation({
 *   variables: {
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      schedulable: // value for 'schedulable'
 *      scheduling: // value for 'scheduling'
 *      jsonConfig: // value for 'jsonConfig'
 *   },
 * });
 */
export function useCreateWebCrawlerDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<CreateWebCrawlerDataSourceMutation, CreateWebCrawlerDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateWebCrawlerDataSourceMutation, CreateWebCrawlerDataSourceMutationVariables>(CreateWebCrawlerDataSourceDocument, options);
      }
export type CreateWebCrawlerDataSourceMutationHookResult = ReturnType<typeof useCreateWebCrawlerDataSourceMutation>;
export type CreateWebCrawlerDataSourceMutationResult = Apollo.MutationResult<CreateWebCrawlerDataSourceMutation>;
export type CreateWebCrawlerDataSourceMutationOptions = Apollo.BaseMutationOptions<CreateWebCrawlerDataSourceMutation, CreateWebCrawlerDataSourceMutationVariables>;
export const CreateYouTubeDataSourceDocument = gql`
    mutation CreateYouTubeDataSource($name: String!, $description: String, $schedulable: Boolean!, $scheduling: String!, $jsonConfig: String) {
  datasource(
    datasourceDTO: {name: $name, description: $description, schedulable: $schedulable, scheduling: $scheduling, jsonConfig: $jsonConfig}
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
export type CreateYouTubeDataSourceMutationFn = Apollo.MutationFunction<CreateYouTubeDataSourceMutation, CreateYouTubeDataSourceMutationVariables>;

/**
 * __useCreateYouTubeDataSourceMutation__
 *
 * To run a mutation, you first call `useCreateYouTubeDataSourceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateYouTubeDataSourceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createYouTubeDataSourceMutation, { data, loading, error }] = useCreateYouTubeDataSourceMutation({
 *   variables: {
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      schedulable: // value for 'schedulable'
 *      scheduling: // value for 'scheduling'
 *      jsonConfig: // value for 'jsonConfig'
 *   },
 * });
 */
export function useCreateYouTubeDataSourceMutation(baseOptions?: Apollo.MutationHookOptions<CreateYouTubeDataSourceMutation, CreateYouTubeDataSourceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateYouTubeDataSourceMutation, CreateYouTubeDataSourceMutationVariables>(CreateYouTubeDataSourceDocument, options);
      }
export type CreateYouTubeDataSourceMutationHookResult = ReturnType<typeof useCreateYouTubeDataSourceMutation>;
export type CreateYouTubeDataSourceMutationResult = Apollo.MutationResult<CreateYouTubeDataSourceMutation>;
export type CreateYouTubeDataSourceMutationOptions = Apollo.BaseMutationOptions<CreateYouTubeDataSourceMutation, CreateYouTubeDataSourceMutationVariables>;
// Generated on 2023-03-07T15:17:52+01:00
