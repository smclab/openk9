/** Internal type. DO NOT USE DIRECTLY. */
type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
/** Internal type. DO NOT USE DIRECTLY. */
export type Incremental<T> = T | { [P in keyof T]?: P extends ' $fragmentName' | '__typename' ? T[P] : never };
import { gql } from '@apollo/client';
import * as Apollo from '@apollo/client';
const defaultOptions = {} as const;
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

export type AutocompleteDtoInput = {
  description?: string | null | undefined;
  /**
   * The field list used in the autocomplete query,
   * must not be empty, all the docTypeField must be of type search_as_you_type
   *  and must have a parent.
   */
  fieldIds?: Array<any> | null | undefined;
  /** 	Edit distance allowed for fuzzy matching (e.g., "0", "1", "2", or "AUTO") */
  fuzziness?: Fuzziness | null | undefined;
  /** 	Minimum number of optional clauses that must match for a document to be returned */
  minimumShouldMatch?: string | null | undefined;
  name: string;
  /** 	Boolean operator to combine query terms (AND or OR) */
  operator?: BooleanOperator | null | undefined;
  /** 	When disabled, perfect matches with the searched text are removed from suggestions */
  perfectMatchIncluded?: boolean | null | undefined;
  /** 	Maximum number of results to return. */
  resultSize?: number | null | undefined;
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

export enum BooleanOperator {
  And = 'AND',
  Or = 'OR'
}

export enum BoundaryScannerType {
  Chars = 'CHARS',
  Sentence = 'SENTENCE',
  Word = 'WORD'
}

export enum ChunkType {
  ChunkTypeCharacterTextSplitter = 'CHUNK_TYPE_CHARACTER_TEXT_SPLITTER',
  ChunkTypeDefault = 'CHUNK_TYPE_DEFAULT',
  ChunkTypeFastChunker = 'CHUNK_TYPE_FAST_CHUNKER',
  ChunkTypeLateChunker = 'CHUNK_TYPE_LATE_CHUNKER',
  ChunkTypeNeuralChunker = 'CHUNK_TYPE_NEURAL_CHUNKER',
  ChunkTypeRecursiveSplitter = 'CHUNK_TYPE_RECURSIVE_SPLITTER',
  ChunkTypeSemanticSplitter = 'CHUNK_TYPE_SEMANTIC_SPLITTER',
  ChunkTypeSentenceSplitter = 'CHUNK_TYPE_SENTENCE_SPLITTER',
  ChunkTypeTableChunker = 'CHUNK_TYPE_TABLE_CHUNKER',
  ChunkTypeTextSplitter = 'CHUNK_TYPE_TEXT_SPLITTER',
  ChunkTypeTokenTextSplitter = 'CHUNK_TYPE_TOKEN_TEXT_SPLITTER',
  Unrecognized = 'UNRECOGNIZED'
}

export type DataIndexDtoInput = {
  /**
   * The docTypeField ids whose values form the header prepended to every
   * chunk before embedding, in this order. Cannot hold the
   * embeddingDocTypeFieldId. Empty or absent means no header.
   */
  chunkHeaderFieldIds?: Array<any> | null | undefined;
  /** The chunk strategy to apply. */
  chunkType?: ChunkType | null | undefined;
  /** The number of chunks before and after every chunk. */
  chunkWindowSize?: number | null | undefined;
  description?: string | null | undefined;
  /** The list of documentType ids that the dataIndex is composed of. */
  docTypeIds?: Array<any> | null | undefined;
  /**
   * The field used during the text embedding,
   * must be a valid docTypeFieldId.
   */
  embeddingDocTypeFieldId?: any;
  /** The configurations used by the embedding model, if needed. */
  embeddingJsonConfig?: string | null | undefined;
  /**
   * Define if this index is a knn index, this property enables
   * vector similarity search features on this DataIndex.
   */
  knnIndex?: boolean | null | undefined;
  name: string;
  /** The settings that will be used when the associated indexTemplate is created. */
  settings?: string | null | undefined;
};

export enum Direction {
  Asc = 'ASC',
  Desc = 'DESC'
}

export type DocTypeUserDtoInput = {
  docTypeId: any;
  userField?: UserField | null | undefined;
};

export enum EnrichItemType {
  GroovyScript = 'GROOVY_SCRIPT',
  HttpAsync = 'HTTP_ASYNC',
  HttpSync = 'HTTP_SYNC'
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

export enum FragmenterType {
  Simple = 'SIMPLE',
  Span = 'SPAN'
}

export enum Fuzziness {
  Auto = 'AUTO',
  One = 'ONE',
  Two = 'TWO',
  Zero = 'ZERO'
}

export enum HighlightType {
  Fvh = 'FVH',
  Plain = 'PLAIN',
  Unified = 'UNIFIED'
}

export type ItemDtoInput = {
  enrichItemId: any;
  weight: number;
};

export enum OffsetSourceType {
  IndexOptions = 'INDEX_OPTIONS',
  None = 'NONE',
  TermVector = 'TERM_VECTOR'
}

export enum OrderType {
  None = 'NONE',
  Score = 'SCORE'
}

export type PipelineWithItemsDtoInput = {
  description?: string | null | undefined;
  items?: Array<ItemDtoInput | null | undefined> | null | undefined;
  name: string;
};

export enum PluginDriverType {
  Http = 'HTTP'
}

export type ProviderModelDtoInput = {
  /** The specific model like: GPT-4, LLaMA 2, Mistral 7B. */
  model: string;
  /** The model provider like: OpenAI, Meta, Mistral. */
  provider: string;
};

export enum Provisioning {
  System = 'SYSTEM',
  User = 'USER'
}

export type QueryParserConfigDtoInput = {
  description?: string | null | undefined;
  jsonConfig?: string | null | undefined;
  name: string;
  type: string;
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

export enum RagType {
  ChatRag = 'CHAT_RAG',
  ChatRagTool = 'CHAT_RAG_TOOL',
  SimpleGenerate = 'SIMPLE_GENERATE'
}

export type RangeDtoInput = {
  /** The end value of the range. */
  end?: number | null | undefined;
  /** The start value of the range. */
  start?: number | null | undefined;
};

export type ResourceUriInput = {
  baseUri?: string | null | undefined;
  path?: string | null | undefined;
};

export type ResultAclConfigDtoInput = {
  description?: string | null | undefined;
  /**
   * Whether the ACLs decide what is visible of each result instead of
   * filtering the query. False when omitted on create or update; left
   * unchanged when omitted on patch.
   */
  enabled?: boolean | null | undefined;
  name: string;
  /**
   * Roles whose holders read every result in full, compared with the
   * roles of the JWT. Each role must be non-blank and on a single line.
   */
  superUserRoles?: Array<string> | null | undefined;
  /**
   * IDs of the DocTypeFields still visible on a result the user is not
   * authorized to read. Excluded fields, OBJECT fields, containers with
   * sub-fields, variants of an i18n field and multi-fields are rejected.
   */
  visibleFieldIds?: Array<any> | null | undefined;
};

export enum RetrieveType {
  Hybrid = 'HYBRID',
  Knn = 'KNN',
  Text = 'TEXT'
}

export enum SchedulerStatus {
  Cancelled = 'CANCELLED',
  Error = 'ERROR',
  Failure = 'FAILURE',
  Finished = 'FINISHED',
  Running = 'RUNNING',
  Stale = 'STALE'
}

export type SortByInput = {
  column?: string | null | undefined;
  direction?: Direction | null | undefined;
};

export enum SortType {
  Frequency = 'FREQUENCY',
  Score = 'SCORE'
}

export enum SortingType {
  Asc = 'ASC',
  Desc = 'DESC'
}

export enum Status {
  /** OpenSearch accepted the settings and the mappings */
  Applied = 'APPLIED',
  /** The index has to be closed and the caller did not allow it, so nothing was written to the live index */
  CloseRequired = 'CLOSE_REQUIRED',
  /** OpenSearch refused, and rejects the whole document when it does: that index needs a reindex */
  Failed = 'FAILED',
  /** The index had to be closed and a scheduling is running on its datasource, so the live index was left alone */
  Skipped = 'SKIPPED',
  /** The index does not exist yet, so only its template was written */
  TemplateOnly = 'TEMPLATE_ONLY'
}

export enum SuggestMode {
  Always = 'ALWAYS',
  Missing = 'MISSING',
  Popular = 'POPULAR'
}

export enum TemplateType {
  JavascriptCompiled = 'JAVASCRIPT_COMPILED',
  JavascriptSource = 'JAVASCRIPT_SOURCE',
  TypescriptSource = 'TYPESCRIPT_SOURCE'
}

export enum TokenType {
  Autocomplete = 'AUTOCOMPLETE',
  Date = 'DATE',
  DateOrder = 'DATE_ORDER',
  Doctype = 'DOCTYPE',
  Entity = 'ENTITY',
  Filter = 'FILTER',
  Text = 'TEXT'
}

export enum UserField {
  Email = 'EMAIL',
  Roles = 'ROLES',
  Username = 'USERNAME'
}

export enum VectorDataType {
  /** Packed binary vectors; maps to a binary knn_vector (faiss engine, hamming space). Requires a vectorSize multiple of 8. */
  Binary = 'BINARY',
  /** Signed 8-bit integer vectors; maps to a byte knn_vector (lucene engine, cosinesimil space). */
  Byte = 'BYTE',
  /** Full precision 32-bit float vectors (default, no quantization). */
  Float32 = 'FLOAT32'
}

export type LanguagesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type LanguagesQuery = { languages: { edges: Array<{ node: { id: string | null, name: string | null, value: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type AnalyzersQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type AnalyzersQuery = { analyzers: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, type: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type AnalyzerOptionsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type AnalyzerOptionsQuery = { options: { edges: Array<{ node: { name: string | null, description: string | null, type: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type AnalyzerValueQueryVariables = Exact<{
  id: string | number;
}>;


export type AnalyzerValueQuery = { value: { id: string | null, name: string | null, description: string | null, type: string | null } | null };

export type BindAnalyzerToDocTypeFieldMutationVariables = Exact<{
  analyzerId: string | number;
  docTypeFieldId: string | number;
}>;


export type BindAnalyzerToDocTypeFieldMutation = { bindAnalyzerToDocTypeField: { left: { id: string | null, docType: { id: string | null } | null } | null, right: { id: string | null } | null } | null };

export type UnbindQueryAnalysisFromDocTypeFieldMutationVariables = Exact<{
  docTypeFieldId: string | number;
}>;


export type UnbindQueryAnalysisFromDocTypeFieldMutation = { unbindAnalyzerFromDocTypeField: { right: { id: string | null } | null } | null };

export type SearchConfigOptionsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  first?: number | null | undefined;
  cursor?: string | null | undefined;
}>;


export type SearchConfigOptionsQuery = { options: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type SearchConfigValueQueryVariables = Exact<{
  id: string | number;
}>;


export type SearchConfigValueQuery = { value: { id: string | null, name: string | null, description: string | null } | null };

export type BindTokenizerToAnalyzerMutationVariables = Exact<{
  analyzerId: string | number;
  tokenizerId: string | number;
}>;


export type BindTokenizerToAnalyzerMutation = { bindTokenizerToAnalyzer: { left: { id: string | null, tokenizer: { id: string | null } | null } | null, right: { id: string | null } | null } | null };

export type UnbindTokenizerFromAnalyzerMutationVariables = Exact<{
  analyzerId: string | number;
}>;


export type UnbindTokenizerFromAnalyzerMutation = { unbindTokenizerFromAnalyzer: { right: { id: string | null } | null } | null };

export type LanguagesOptionsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type LanguagesOptionsQuery = { options: { edges: Array<{ node: { id: string | null, name: string | null, value: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type LanguageValueQueryVariables = Exact<{
  id: string | number;
}>;


export type LanguageValueQuery = { value: { id: string | null, name: string | null, value: string | null } | null };

export type AnalyzerQueryVariables = Exact<{
  id: string | number;
}>;


export type AnalyzerQuery = { analyzer: { id: string | null, name: string | null, description: string | null, type: string | null, jsonConfig: string | null, tokenizer: { id: string | null, name: string | null } | null } | null };

export type DeleteAnalyzerMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteAnalyzerMutation = { deleteAnalyzer: { id: string | null, name: string | null } | null };

export type CreateOrUpdateAnalyzerMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  type: string;
  tokenFilterIds?: Array<any> | any | null | undefined;
  charFilterIds?: Array<any> | any | null | undefined;
  tokenizerId?: any;
  jsonConfig?: string | null | undefined;
}>;


export type CreateOrUpdateAnalyzerMutation = { analyzerWithLists: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type AnalyzersAssociationsQueryVariables = Exact<{
  parentId: string | number;
  unassociated: boolean;
}>;


export type AnalyzersAssociationsQuery = { analyzer: { id: string | null, charFilters: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null, tokenFilters: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null } | null };

export type AutocompletesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type AutocompletesQuery = { autocompletes: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteAutocompleteMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteAutocompleteMutation = { deleteAutocomplete: { id: string | null, name: string | null } | null };

export type AutocompleteQueryVariables = Exact<{
  id: string | number;
}>;


export type AutocompleteQuery = { autocomplete: { id: string | null, fuzziness: Fuzziness | null, minimumShouldMatch: string | null, name: string | null, operator: BooleanOperator | null, resultSize: number | null, perfectMatchIncluded: boolean, fields: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null } | null };

export type CreateOrUpdateAutocompleteMutationVariables = Exact<{
  id?: string | number | null | undefined;
  autocompleteDTO: AutocompleteDtoInput;
}>;


export type CreateOrUpdateAutocompleteMutation = { autocomplete: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type UnboundDocTypeFieldByAutocompleteQueryVariables = Exact<{
  autocompleteId: any;
}>;


export type UnboundDocTypeFieldByAutocompleteQuery = { unboundDocTypeFieldByAutocomplete: Array<{ id: string | null, name: string | null } | null> | null };

export type AutocorrectionsOptionsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type AutocorrectionsOptionsQuery = { autocorrections: { edges: Array<{ cursor: string | null, node: { id: string | null, description: string | null, name: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type AutocorrectionValueQueryVariables = Exact<{
  id: string | number;
}>;


export type AutocorrectionValueQuery = { autocorrection: { id: string | null, modifiedDate: any, description: string | null, maxEdit: number | null, minWordLength: number | null, name: string | null, prefixLength: number | null, sort: SortType | null, suggestMode: SuggestMode | null, enableSearchWithCorrection: boolean, autocorrectionDocTypeField: { id: string | null, name: string | null } | null } | null };

export type DeleteAutocorrectionMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteAutocorrectionMutation = { deleteAutocorrection: { id: string | null } | null };

export type SaveOrUpdateAutocorrectionMutationVariables = Exact<{
  id: string | number;
}>;


export type SaveOrUpdateAutocorrectionMutation = { deleteAutocorrection: { id: string | null } | null };

export type SaveAutocorrectionMutationVariables = Exact<{
  id?: string | number | null | undefined;
  autocorrectionDocTypeFieldId?: any;
  enableSearchWithCorrection: boolean;
  maxEdit: number;
  minWordLength: number;
  prefixLength: number;
  sort: SortType;
  suggestMode: SuggestMode;
  name: string;
}>;


export type SaveAutocorrectionMutation = { autocorrection: { entity: { id: string | null } | null } | null };

export type AllDocTypeFieldsQueryVariables = Exact<{ [key: string]: never; }>;


export type AllDocTypeFieldsQuery = { docTypeFields: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null };

export type CharfiltersQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type CharfiltersQuery = { charFilters: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteCharFiltersMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteCharFiltersMutation = { deleteCharFilter: { id: string | null, name: string | null } | null };

export type UnboundAnalyzersByCharFilterQueryVariables = Exact<{
  charFilterId: any;
}>;


export type UnboundAnalyzersByCharFilterQuery = { unboundAnalyzersByCharFilter: Array<{ name: string | null, id: string | null } | null> | null };

export type AddCharFiltersToAnalyzerMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddCharFiltersToAnalyzerMutation = { addCharFilterToAnalyzer: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type CharFilterQueryVariables = Exact<{
  id: string | number;
}>;


export type CharFilterQuery = { charFilter: { id: string | null, name: string | null, description: string | null, jsonConfig: string | null, type: string | null } | null };

export type CreateOrUpdateCharFilterMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  jsonConfig?: string | null | undefined;
  type: string;
}>;


export type CreateOrUpdateCharFilterMutation = { charFilter: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type DataIndexInformationQueryVariables = Exact<{ [key: string]: never; }>;


export type DataIndexInformationQuery = { buckets: { edges: Array<{ node: { datasources: { edges: Array<{ node: { dataIndex: { cat: { docsCount: string | null, docsDeleted: string | null, health: string | null, index: string | null, pri: string | null, priStoreSize: any, rep: string | null, status: string | null, storeSize: any, uuid: string | null } | null } | null } | null } | null> | null } | null } | null } | null> | null } | null };

export type SchedulersFaiulureQueryVariables = Exact<{
  searchText?: string | null | undefined;
}>;


export type SchedulersFaiulureQuery = { schedulers: { edges: Array<{ node: { id: string | null, modifiedDate: any, errorDescription: string | null, lastIngestionDate: any, status: SchedulerStatus | null, datasource: { id: string | null, name: string | null } | null, newDataIndex: { id: string | null, name: string | null } | null } | null } | null> | null } | null };

export type DocumentTypeTemplatesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type DocumentTypeTemplatesQuery = { docTypeTemplates: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteDocumentTypeTemplateMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteDocumentTypeTemplateMutation = { deleteDocTypeTemplate: { id: string | null, name: string | null } | null };

export type DocumentTypesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type DocumentTypesQuery = { docTypes: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DocumentTypeTemplateQueryVariables = Exact<{
  id: string | number;
}>;


export type DocumentTypeTemplateQuery = { docTypeTemplate: { id: string | null, name: string | null, description: string | null, templateType: TemplateType | null, source: string | null, compiled: string | null } | null };

export type CreateOrUpdateDocumentTypeTemplateMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  templateType: TemplateType;
  source: string;
  compiled: string;
}>;


export type CreateOrUpdateDocumentTypeTemplateMutation = { docTypeTemplate: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type DocumentTypeFieldQueryVariables = Exact<{
  id: string | number;
}>;


export type DocumentTypeFieldQuery = { docTypeField: { id: string | null, name: string | null, description: string | null, fieldType: FieldType | null, offsetSource: OffsetSourceType | null, boost: number | null, searchable: boolean | null, exclude: boolean | null, fieldName: string | null, jsonConfig: string | null, sortable: boolean | null, analyzer: { id: string | null, name: string | null } | null, searchAnalyzer: { id: string | null, name: string | null } | null, translations: Array<{ key: string | null, language: string | null, value: string | null, description: string | null } | null> | null } | null };

export type CreateOrUpdateDocumentTypeSubFieldsMutationVariables = Exact<{
  parentDocTypeFieldId: string | number;
  name: string;
  fieldName: string;
  jsonConfig?: string | null | undefined;
  searchable: boolean;
  boost?: number | null | undefined;
  fieldType: FieldType;
  sortable: boolean;
  offsetSource?: OffsetSourceType | null | undefined;
}>;


export type CreateOrUpdateDocumentTypeSubFieldsMutation = { createSubField: { entity: { id: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type CreateOrUpdateDocumentTypeMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
}>;


export type CreateOrUpdateDocumentTypeMutation = { docType: { entity: { id: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type DocumentTypeQueryVariables = Exact<{
  id: string | number;
}>;


export type DocumentTypeQuery = { docType: { id: string | null, name: string | null, description: string | null, docTypeTemplate: { id: string | null, name: string | null } | null } | null };

export type DocTypeFieldsByParentQueryVariables = Exact<{
  searchText?: string | null | undefined;
  parentId: any;
  docTypeId: string | number;
  first?: number | null | undefined;
  after?: string | null | undefined;
}>;


export type DocTypeFieldsByParentQuery = { docTypeFieldsFromDocTypeByParent: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, fieldType: FieldType | null, boost: number | null, searchable: boolean | null, exclude: boolean | null, fieldName: string | null, jsonConfig: string | null, sortable: boolean | null, parent: { id: string | null, fieldName: string | null } | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type CreateOrUpdateDocumentTypeFieldMutationVariables = Exact<{
  documentTypeId: string | number;
  documentTypeFieldId?: string | number | null | undefined;
  docTypeFieldName?: string | null | undefined;
  name: string;
  fieldName: string;
  description?: string | null | undefined;
  fieldType: FieldType;
  boost?: number | null | undefined;
  searchable: boolean;
  exclude?: boolean | null | undefined;
  jsonConfig?: string | null | undefined;
  sortable: boolean;
  analyzerId?: any;
  searchAnalyzerId?: any;
  offsetSource?: OffsetSourceType | null | undefined;
}>;


export type CreateOrUpdateDocumentTypeFieldMutation = { docTypeFieldWithAnalyzer: { entity: { id: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type DeleteDocumentTypeMutationVariables = Exact<{
  id: string | number;
  docTypeName?: string | null | undefined;
}>;


export type DeleteDocumentTypeMutation = { deleteDocType: { id: string | null } | null };

export type DeleteDocumentTypeFieldMutationVariables = Exact<{
  docTypeFieldId: string | number;
  docTypeFieldName?: string | null | undefined;
}>;


export type DeleteDocumentTypeFieldMutation = { deleteDocTypeField: { id: string | null, name: string | null } | null };

export type DocTypeTemplateListQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
  first?: number | null | undefined;
}>;


export type DocTypeTemplateListQuery = { docTypeTemplates: { edges: Array<{ node: { name: string | null, id: string | null } | null } | null> | null } | null };

export type CreateOrUpdateDocTypeWithTemplateMutationVariables = Exact<{
  name: string;
  description?: string | null | undefined;
  docTypeTemplateId?: any;
  id?: string | number | null | undefined;
}>;


export type CreateOrUpdateDocTypeWithTemplateMutation = { docTypeWithTemplate: { entity: { id: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type UnboundAnalyzersQueryVariables = Exact<{ [key: string]: never; }>;


export type UnboundAnalyzersQuery = { analyzers: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null };

export type AlignIndexesMutationVariables = Exact<{
  docTypeId: string | number;
  closeIfNeeded: boolean;
}>;


export type AlignIndexesMutation = { alignIndexes: Array<{ dataIndexId: any, indexName: string | null, status: Status | null, reason: string | null } | null> | null };

export type EmbeddingModelsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type EmbeddingModelsQuery = { embeddingModels: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, enabled: boolean } | null } | null> | null } | null };

export type EnableEmbeddingModelMutationVariables = Exact<{
  id: string | number;
}>;


export type EnableEmbeddingModelMutation = { enableEmbeddingModel: { id: string | null, name: string | null } | null };

export type DeleteEmbeddingModelMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteEmbeddingModelMutation = { deleteEmbeddingModel: { id: string | null, name: string | null } | null };

export type EmbeddingModelQueryVariables = Exact<{
  id: string | number;
}>;


export type EmbeddingModelQuery = { embeddingModel: { name: string | null, description: string | null, apiUrl: string | null, apiKey: string | null, vectorSize: number | null, vectorDataType: VectorDataType | null, jsonConfig: string | null, providerModel: { provider: string | null, model: string | null } | null } | null };

export type CreateOrUpdateEmbeddingModelMutationVariables = Exact<{
  id?: string | number | null | undefined;
  apiKey?: string | null | undefined;
  apiUrl: string;
  description: string;
  name: string;
  vectorSize: number;
  vectorDataType?: VectorDataType | null | undefined;
  providerModel: ProviderModelDtoInput;
  jsonConfig?: string | null | undefined;
}>;


export type CreateOrUpdateEmbeddingModelMutation = { embeddingModel: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type UnboundEnrichPipelinesQueryVariables = Exact<{
  itemId: any;
}>;


export type UnboundEnrichPipelinesQuery = { unboundEnrichPipelines: Array<{ name: string | null, id: string | null } | null> | null };

export type DeleteEnrichItemMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteEnrichItemMutation = { deleteEnrichItem: { id: string | null, name: string | null } | null };

export type EnrichItemQueryVariables = Exact<{
  id: string | number;
}>;


export type EnrichItemQuery = { enrichItem: { id: string | null, name: string | null, description: string | null, type: EnrichItemType | null, script: string | null, jsonConfig: string | null, behaviorMergeType: BehaviorMergeType | null, jsonPath: string | null, behaviorOnError: BehaviorOnError | null, requestTimeout: any, resourceUri: { baseUri: string | null, path: string | null } | null } | null };

export type CreateOrUpdateEnrichItemMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  type: EnrichItemType;
  resourceUri: ResourceUriInput;
  script?: string | null | undefined;
  jsonConfig?: string | null | undefined;
  behaviorMergeType: BehaviorMergeType;
  jsonPath: string;
  behaviorOnError: BehaviorOnError;
  requestTimeout: any;
}>;


export type CreateOrUpdateEnrichItemMutation = { enrichItem: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type HighlightsQueryVariables = Exact<{ [key: string]: never; }>;


export type HighlightsQuery = { highlights: Array<{ id: string | null, name: string | null, description: string | null, type: HighlightType } | null> | null };

export type HighlightQueryVariables = Exact<{
  id: string | number;
}>;


export type HighlightQuery = { highlight: { id: string | null, name: string | null, description: string | null, type: HighlightType, boundaryScanner: BoundaryScannerType | null, boundaryChars: string | null, fragmenter: FragmenterType | null, fragmentSize: number | null, numberOfFragments: number | null, order: OrderType | null, fields: Array<{ id: string | null, name: string | null } | null> | null } | null };

export type DocTypeFieldsByOffsetSourceQueryVariables = Exact<{
  offsetSource?: OffsetSourceType | null | undefined;
}>;


export type DocTypeFieldsByOffsetSourceQuery = { docTypeFieldsByOffsetSource: Array<{ id: string | null, name: string | null } | null> | null };

export type CreateOrUpdateHighlightMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  type?: HighlightType | null | undefined;
  fieldIds?: Array<any> | any | null | undefined;
  boundaryScanner?: BoundaryScannerType | null | undefined;
  boundaryChars?: string | null | undefined;
  fragmenter?: FragmenterType | null | undefined;
  fragmentSize?: number | null | undefined;
  numberOfFragments?: number | null | undefined;
  order?: OrderType | null | undefined;
  patch?: boolean | null | undefined;
}>;


export type CreateOrUpdateHighlightMutation = { highlight: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type DeleteHighlightMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteHighlightMutation = { deleteHighlight: { id: string | null, name: string | null } | null };

export type LargeLanguageModelsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type LargeLanguageModelsQuery = { largeLanguageModels: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, enabled: boolean, providerModel: { provider: string | null, model: string | null } | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type EnableLargeLanguageModelMutationVariables = Exact<{
  id: string | number;
}>;


export type EnableLargeLanguageModelMutation = { enableLargeLanguageModel: { id: string | null, name: string | null } | null };

export type DeleteLargeLanguageModelMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteLargeLanguageModelMutation = { deleteLargeLanguageModel: { id: string | null, name: string | null } | null };

export type CreateOrUpdateLargeLanguageModelMutationVariables = Exact<{
  id?: string | number | null | undefined;
  apiKey?: string | null | undefined;
  apiUrl: string;
  description: string;
  name: string;
  jsonConfig?: string | null | undefined;
  providerModel: ProviderModelDtoInput;
  contextWindow?: number | null | undefined;
  retrieveCitations?: boolean | null | undefined;
}>;


export type CreateOrUpdateLargeLanguageModelMutation = { largeLanguageModel: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type LargeLanguageModelQueryVariables = Exact<{
  id: string | number;
}>;


export type LargeLanguageModelQuery = { largeLanguageModel: { name: string | null, description: string | null, apiUrl: string | null, apiKey: string | null, jsonConfig: string | null, contextWindow: number | null, retrieveCitations: boolean | null, providerModel: { provider: string | null, model: string | null } | null } | null };

export type SchedulersQueryVariables = Exact<{ [key: string]: never; }>;


export type SchedulersQuery = { schedulers: { edges: Array<{ node: { scheduleId: string | null, status: SchedulerStatus | null, datasource: { id: string | null, name: string | null } | null } | null } | null> | null } | null };

export type SchedulersErrorQueryVariables = Exact<{ [key: string]: never; }>;


export type SchedulersErrorQuery = { schedulers: { edges: Array<{ node: { scheduleId: string | null, createDate: any, status: SchedulerStatus | null, datasource: { id: string | null, name: string | null } | null } | null } | null> | null } | null };

export type SchedulerQueryVariables = Exact<{
  id: string | number;
}>;


export type SchedulerQuery = { scheduler: { scheduleId: string | null, createDate: any, modifiedDate: any, lastIngestionDate: any, status: SchedulerStatus | null, errorDescription: string | null } | null };

export type PluginDriverQueryVariables = Exact<{
  id: string | number;
}>;


export type PluginDriverQuery = { pluginDriver: { id: string | null, name: string | null, description: string | null, type: PluginDriverType | null, provisioning: Provisioning | null, resourceUri: { baseUri: string | null, path: string | null } | null, aclMappings: Array<{ userField: UserField | null, docTypeField: { name: string | null, id: string | null, fieldName: string | null } | null } | null> | null } | null };

export type CreateOrUpdatePluginDriverMutationMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  type: PluginDriverType;
  resourceUri?: ResourceUriInput | null | undefined;
  provisioning: Provisioning;
}>;


export type CreateOrUpdatePluginDriverMutationMutation = { pluginDriver: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type PluginDriverByNameQueryVariables = Exact<{
  name?: string | null | undefined;
}>;


export type PluginDriverByNameQuery = { pluginDrivers: { edges: Array<{ node: { id: string | null } | null } | null> | null } | null };

export type PluginDriversInfoQueryQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type PluginDriversInfoQueryQuery = { pluginDrivers: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, type: PluginDriverType | null, aclMappings: Array<{ userField: UserField | null, docTypeField: { fieldName: string | null } | null } | null> | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeletePluginDriverMutationVariables = Exact<{
  id: string | number;
}>;


export type DeletePluginDriverMutation = { deletePluginDriver: { id: string | null, name: string | null } | null };

export type PluginDriverWithDocTypeMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  type: PluginDriverType;
  resourceUri: ResourceUriInput;
  provisioning: Provisioning;
  docTypeUserDTOSet?: Array<DocTypeUserDtoInput | null | undefined> | DocTypeUserDtoInput | null | undefined;
}>;


export type PluginDriverWithDocTypeMutation = { pluginDriverWithDocType: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type PluginDriverToDocumentTypeFieldsQueryVariables = Exact<{
  parentId: string | number;
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type PluginDriverToDocumentTypeFieldsQuery = { pluginDriver: { id: string | null, aclMappings: Array<{ userField: UserField | null, docTypeField: { id: string | null, name: string | null } | null } | null> | null, docTypeFields: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, docType: { id: string | null } | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null } | null };

export type DocumentTypeFieldsForPluginQueryVariables = Exact<{
  searchText?: string | null | undefined;
}>;


export type DocumentTypeFieldsForPluginQuery = { docTypeFields: { __typename: 'DefaultConnection_DocTypeField', edges: Array<{ __typename: 'DefaultEdge_DocTypeField', node: { __typename: 'DocTypeField', id: string | null, name: string | null, description: string | null } | null } | null> | null } | null };

export type QueryAnalysesRulesQueryVariables = Exact<{
  parentId: string | number;
  searchText?: string | null | undefined;
  unassociated: boolean;
  cursor?: string | null | undefined;
}>;


export type QueryAnalysesRulesQuery = { queryAnalysis: { id: string | null, rules: { edges: Array<{ node: { id: string | null, name: string | null, lhs: string | null, rhs: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null } | null };

export type AddRulesToQueryAnalysesMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddRulesToQueryAnalysesMutation = { addRuleToQueryAnalysis: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type RemoveRuleFromQueryAnalysesMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type RemoveRuleFromQueryAnalysesMutation = { removeRuleFromQueryAnalysis: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type QueryAnalysisQueryVariables = Exact<{
  id: string | number;
}>;


export type QueryAnalysisQuery = { queryAnalysis: { id: string | null, name: string | null, description: string | null, stopWords: string | null, annotators: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null, rules: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null } | null };

export type QueryAnalysesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type QueryAnalysesQuery = { queryAnalyses: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteQueryAnalysisMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteQueryAnalysisMutation = { deleteQueryAnalysis: { id: string | null, name: string | null } | null };

export type CreateOrUpdateQueryAnalysisMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  stopWords?: string | null | undefined;
  annotatorsIds?: Array<any> | any | null | undefined;
  rulesIds?: Array<any> | any | null | undefined;
}>;


export type CreateOrUpdateQueryAnalysisMutation = { queryAnalysisWithLists: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type QueryAnalysisAssociationsQueryVariables = Exact<{
  parentId: string | number;
  unassociated: boolean;
}>;


export type QueryAnalysisAssociationsQuery = { queryAnalysis: { id: string | null, annotators: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null, rules: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null } | null };

export type RagConfigurationsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type RagConfigurationsQuery = { ragConfigurations: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, type: RagType | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type RagConfigurationQueryVariables = Exact<{
  id: string | number;
}>;


export type RagConfigurationQuery = { ragConfiguration: { id: string | null, name: string | null, description: string | null, type: RagType | null, reformulate: boolean | null, chunkWindow: number | null, rephrasePrompt: string | null, prompt: string | null, jsonConfig: string | null, ragToolDescription: string | null, promptNoRag: string | null, enableConversationTitle: boolean | null, range: { start: number | null, end: number | null } | null } | null };

export type CreateRagConfigMutationVariables = Exact<{
  name: string;
  description?: string | null | undefined;
  type: RagType;
  reformulate?: boolean | null | undefined;
  chunkWindow?: number | null | undefined;
  rephrasePrompt?: string | null | undefined;
  prompt?: string | null | undefined;
  jsonConfig?: string | null | undefined;
  ragToolDescription?: string | null | undefined;
  promptNoRag?: string | null | undefined;
  enableConversationTitle?: boolean | null | undefined;
  range?: RangeDtoInput | null | undefined;
}>;


export type CreateRagConfigMutation = { createRAGConfiguration: { entity: { id: string | null, name: string | null, type: RagType | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type UpdateRagConfigurationMutationVariables = Exact<{
  id: string | number;
  name: string;
  description?: string | null | undefined;
  reformulate?: boolean | null | undefined;
  chunkWindow?: number | null | undefined;
  rephrasePrompt?: string | null | undefined;
  prompt?: string | null | undefined;
  jsonConfig?: string | null | undefined;
  ragToolDescription?: string | null | undefined;
  promptNoRag?: string | null | undefined;
  enableConversationTitle?: boolean | null | undefined;
  range?: RangeDtoInput | null | undefined;
  patch?: boolean | null | undefined;
}>;


export type UpdateRagConfigurationMutation = { updateRAGConfiguration: { entity: { id: string | null, name: string | null, type: RagType | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type DeleteRagConfigurationMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteRagConfigurationMutation = { deleteRAGConfiguration: { id: string | null } | null };

export type UnboundRagConfigurationsByBucketQueryVariables = Exact<{
  bucketId: string | number;
  ragType: RagType;
}>;


export type UnboundRagConfigurationsByBucketQuery = { unboundRAGConfigurationByBucket: Array<{ id: string | null, name: string | null } | null> | null };

export type ResultAclConfigQueryVariables = Exact<{ [key: string]: never; }>;


export type ResultAclConfigQuery = { resultAclConfig: { id: string | null, enabled: boolean, superUserRoles: Array<string | null> | null, visibleFields: Array<{ id: string | null, name: string | null } | null> | null } | null };

export type UnboundDocTypeFieldByResultAclConfigQueryVariables = Exact<{ [key: string]: never; }>;


export type UnboundDocTypeFieldByResultAclConfigQuery = { unboundDocTypeFieldByResultAclConfig: Array<{ id: string | null, name: string | null } | null> | null };

export type CreateOrUpdateResultAclConfigMutationVariables = Exact<{
  resultAclConfigDTO: ResultAclConfigDtoInput;
}>;


export type CreateOrUpdateResultAclConfigMutation = { resultAclConfig: { entity: { id: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type RulesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type RulesQuery = { rules: { edges: Array<{ node: { id: string | null, name: string | null, lhs: string | null, rhs: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteRulesMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteRulesMutation = { deleteRule: { id: string | null, name: string | null } | null };

export type RuleQueryVariables = Exact<{
  id: string | number;
}>;


export type RuleQuery = { rule: { id: string | null, name: string | null, description: string | null, lhs: string | null, rhs: string | null } | null };

export type CreateOrUpdateRuleQueryMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  lhs: string;
  rhs: string;
}>;


export type CreateOrUpdateRuleQueryMutation = { rule: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type SearchConfigQueryVariables = Exact<{
  id: string | number;
}>;


export type SearchConfigQuery = { searchConfig: { id: string | null, name: string | null, description: string | null, minScore: number | null, minScoreSuggestions: boolean, minScoreSearch: boolean, maxSearchPageFrom: number | null, maxSearchPageSize: number | null, maxTextQueryLength: number | null, queryParserConfigs: { edges: Array<{ node: { id: string | null, name: string | null, type: QueryParserType | null, jsonConfig: string | null } | null } | null> | null } | null } | null };

export type CreateOrUpdateSearchConfigMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  minScore: number;
  minScoreSuggestions: boolean;
  minScoreSearch: boolean;
  maxSearchPageFrom?: number | null | undefined;
  maxSearchPageSize?: number | null | undefined;
  maxTextQueryLength?: number | null | undefined;
  queryParsersConfig?: Array<QueryParserConfigDtoInput | null | undefined> | QueryParserConfigDtoInput | null | undefined;
}>;


export type CreateOrUpdateSearchConfigMutation = { searchConfigWithQueryParsers: { entity: { id: string | null, name: string | null, minScore: number | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type SearchConfigsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  first?: number | null | undefined;
  after?: string | null | undefined;
}>;


export type SearchConfigsQuery = { searchConfigs: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, minScore: number | null, minScoreSuggestions: boolean, minScoreSearch: boolean } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteSearchConfigMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteSearchConfigMutation = { deleteSearchConfig: { id: string | null, name: string | null } | null };

export type QueryParserConfigQueryVariables = Exact<{ [key: string]: never; }>;


export type QueryParserConfigQuery = { queryParserConfigFormConfigurations: any };

export type SortingQueryVariables = Exact<{
  id: string | number;
}>;


export type SortingQuery = { sorting: { id: string | null, name: string | null, description: string | null, priority: number | null, defaultSort: boolean, type: SortingType | null, docTypeField: { id: string | null, name: string | null } | null } | null };

export type CreateOrUpdateSortingMutationVariables = Exact<{
  sortingId?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  priority: number;
  type: SortingType;
  defaultSort: boolean;
  docTypeFieldId?: any;
}>;


export type CreateOrUpdateSortingMutation = { sortingWithDocTypeField: { entity: { id: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type SortingsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type SortingsQuery = { totalSortings: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, priority: number | null, defaultSort: boolean, type: SortingType | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteSortingMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteSortingMutation = { deleteSorting: { id: string | null, name: string | null } | null };

export type UnboundBucketsBySuggestionCategoryQueryVariables = Exact<{
  id: any;
}>;


export type UnboundBucketsBySuggestionCategoryQuery = { unboundBucketsBySuggestionCategory: Array<{ name: string | null, id: string | null } | null> | null };

export type SuggestionCategoriesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type SuggestionCategoriesQuery = { suggestionCategories: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, priority: number | null, multiSelect: boolean } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteSuggestionCategoryMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteSuggestionCategoryMutation = { deleteSuggestionCategory: { id: string | null, name: string | null } | null };

export type SuggestionCategoryDocumentTypeFieldsQueryVariables = Exact<{
  parentId: string | number;
  searchText?: string | null | undefined;
  unassociated: boolean;
  cursor?: string | null | undefined;
}>;


export type SuggestionCategoryDocumentTypeFieldsQuery = { suggestionCategory: { id: string | null, docTypeField: { name: string | null, subFields: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, docType: { id: string | null } | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null } | null } | null };

export type AddDocumentTypeFieldToSuggestionCategoryMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddDocumentTypeFieldToSuggestionCategoryMutation = { addDocTypeFieldToSuggestionCategory: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type SuggestionCategoryQueryVariables = Exact<{
  id: string | number;
}>;


export type SuggestionCategoryQuery = { suggestionCategory: { id: string | null, name: string | null, description: string | null, priority: number | null, multiSelect: boolean, docTypeField: { id: string | null, name: string | null } | null, translations: Array<{ key: string | null, language: string | null, value: string | null, description: string | null } | null> | null } | null };

export type AddSuggestionCategoryTranslationMutationVariables = Exact<{
  suggestionCategoryId: string | number;
  language: string;
  key?: string | null | undefined;
  value: string;
}>;


export type AddSuggestionCategoryTranslationMutation = { addSuggestionCategoryTranslation: { left: string | null, right: string | null } | null };

export type UnboundDocTypeFieldsBySuggestionCategoryQueryVariables = Exact<{
  suggestionCategoryId: any;
}>;


export type UnboundDocTypeFieldsBySuggestionCategoryQuery = { unboundDocTypeFieldsBySuggestionCategory: Array<{ id: string | null, name: string | null } | null> | null };

export type DocTypeFieldsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  first?: number | null | undefined;
  after?: string | null | undefined;
}>;


export type DocTypeFieldsQuery = { docTypeFields: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DocTypeFieldsByTypeQueryVariables = Exact<{
  fieldType?: FieldType | null | undefined;
}>;


export type DocTypeFieldsByTypeQuery = { docTypeFieldsByType: Array<{ id: string | null, name: string | null } | null> | null };

export type CreateOrUpdateSuggestionCategoryMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  priority: number;
  multiSelect: boolean;
  docTypeFieldId?: any;
}>;


export type CreateOrUpdateSuggestionCategoryMutation = { suggestionCategoryWithDocTypeField: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type TabsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type TabsQuery = { tabs: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, priority: number | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteTabsMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteTabsMutation = { deleteTab: { id: string | null, name: string | null } | null };

export type UnboundBucketsByTabQueryVariables = Exact<{
  id: any;
}>;


export type UnboundBucketsByTabQuery = { unboundBucketsByTab: Array<{ name: string | null, id: string | null } | null> | null };

export type AddTabTranslationMutationVariables = Exact<{
  tabId: string | number;
  language: string;
  key?: string | null | undefined;
  value: string;
}>;


export type AddTabTranslationMutation = { addTabTranslation: { left: string | null, right: string | null } | null };

export type TabQueryVariables = Exact<{
  id: string | number;
  unasociated?: boolean | null | undefined;
}>;


export type TabQuery = { tab: { id: string | null, name: string | null, description: string | null, priority: number | null, tokenTabs: { edges: Array<{ node: { name: string | null, id: string | null } | null } | null> | null } | null, sortings: { edges: Array<{ node: { name: string | null, id: string | null } | null } | null> | null } | null, translations: Array<{ key: string | null, language: string | null, value: string | null, description: string | null } | null> | null } | null };

export type CreateOrUpdateTabMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  priority: number;
  tokenTabIds?: Array<any> | any | null | undefined;
  sortingIds?: Array<any> | any | null | undefined;
}>;


export type CreateOrUpdateTabMutation = { tabWithTokenTabs: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type TabTokenTabsQueryVariables = Exact<{
  parentId: string | number;
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
  unassociated: boolean;
}>;


export type TabTokenTabsQuery = { tab: { id: string | null, tokenTabs: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null } | null };

export type AddTokenTabToTabMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddTokenTabToTabMutation = { addTokenTabToTab: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type RemoveTokenTabToTabMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type RemoveTokenTabToTabMutation = { removeTokenTabToTab: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type TabTokensQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type TabTokensQuery = { totalTokenTabs: { edges: Array<{ node: { id: string | null, name: string | null, tokenType: TokenType | null, value: string | null, filter: boolean | null, extraParams: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type TokenFiltersQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type TokenFiltersQuery = { tokenFilters: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteTokenFiltersMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteTokenFiltersMutation = { deleteTokenFilter: { id: string | null, name: string | null } | null };

export type AddTokenFilterToAnalyzerMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddTokenFilterToAnalyzerMutation = { addTokenFilterToAnalyzer: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type UnboundAnalyzersByTokenFilterQueryVariables = Exact<{
  tokenFilterId: any;
}>;


export type UnboundAnalyzersByTokenFilterQuery = { unboundAnalyzersByTokenFilter: Array<{ name: string | null, id: string | null } | null> | null };

export type TokenFilterQueryVariables = Exact<{
  id: string | number;
}>;


export type TokenFilterQuery = { tokenFilter: { id: string | null, name: string | null, description: string | null, jsonConfig: string | null, type: string | null } | null };

export type CreateOrUpdateTokenFilterMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  jsonConfig?: string | null | undefined;
  type: string;
}>;


export type CreateOrUpdateTokenFilterMutation = { tokenFilter: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type TabTokenTabQueryVariables = Exact<{
  id: string | number;
}>;


export type TabTokenTabQuery = { tokenTab: { id: string | null, name: string | null, description: string | null, value: string | null, filter: boolean | null, tokenType: TokenType | null, extraParams: string | null, docTypeField: { id: string | null, name: string | null } | null } | null };

export type CreateOrUpdateTabTokenMutationVariables = Exact<{
  tokenTabId?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  value: string;
  filter: boolean;
  tokenType: TokenType;
  docTypeFieldId?: any;
  extraParams?: string | null | undefined;
}>;


export type CreateOrUpdateTabTokenMutation = { tokenTabWithDocTypeField: { entity: { id: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type DocTypeFieldValueQueryVariables = Exact<{
  id: string | number;
}>;


export type DocTypeFieldValueQuery = { value: { id: string | null, name: string | null, description: string | null } | null };

export type DocTypeFieldOptionsTokenTabQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type DocTypeFieldOptionsTokenTabQuery = { options: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type TabTokensQueryQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type TabTokensQueryQuery = { totalTokenTabs: { edges: Array<{ node: { id: string | null, name: string | null, tokenType: TokenType | null, value: string | null, filter: boolean | null, extraParams: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteTabTokenMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteTabTokenMutation = { deleteTokenTab: { id: string | null, name: string | null } | null };

export type UnassociatedTokenTabsInTabQueryVariables = Exact<{
  id: any;
}>;


export type UnassociatedTokenTabsInTabQuery = { unboundTabsByTokenTab: Array<{ id: string | null, name: string | null } | null> | null };

export type TokenizerQueryVariables = Exact<{
  id: string | number;
}>;


export type TokenizerQuery = { tokenizer: { id: string | null, name: string | null, description: string | null, jsonConfig: string | null, type: string | null } | null };

export type CreateOrUpdateTokenizerMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  jsonConfig?: string | null | undefined;
  type: string;
}>;


export type CreateOrUpdateTokenizerMutation = { tokenizer: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type TokenizersQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type TokenizersQuery = { tokenizers: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteTokenizerMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteTokenizerMutation = { deleteTokenizer: { id: string | null, name: string | null } | null };

export type AnnotatorsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type AnnotatorsQuery = { annotators: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, size: number | null, type: AnnotatorType | null, fieldName: string | null, fuziness: Fuzziness | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteAnnotatosMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteAnnotatosMutation = { deleteAnnotator: { id: string | null, name: string | null } | null };

export type DocTypeFieldOptionsAnnotatorsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type DocTypeFieldOptionsAnnotatorsQuery = { options: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type AnnotatorQueryVariables = Exact<{
  id: string | number;
}>;


export type AnnotatorQuery = { annotator: { id: string | null, fuziness: Fuzziness | null, size: number | null, type: AnnotatorType | null, description: string | null, name: string | null, fieldName: string | null, extraParams: string | null, docTypeField: { id: string | null, name: string | null } | null } | null };

export type CreateOrUpdateAnnotatorMutationVariables = Exact<{
  id?: string | number | null | undefined;
  fieldName: string;
  fuziness: Fuzziness;
  type: AnnotatorType;
  description?: string | null | undefined;
  size?: number | null | undefined;
  name: string;
  docTypeFieldId?: any;
  extraParams?: string | null | undefined;
}>;


export type CreateOrUpdateAnnotatorMutation = { annotatorWithDocTypeField: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type DocTypeFieldOptionsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
  annotatorId: string | number;
}>;


export type DocTypeFieldOptionsQuery = { options: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type BindDocTypeFieldToDataSourceMutationVariables = Exact<{
  documentTypeFieldId: string | number;
  annotatorId: string | number;
}>;


export type BindDocTypeFieldToDataSourceMutation = { bindAnnotatorToDocTypeField: { left: { id: string | null, docTypeField: { id: string | null } | null } | null, right: { id: string | null } | null } | null };

export type UnbindDocTypeFieldToDataSourceMutationVariables = Exact<{
  documentTypeFieldId: string | number;
  annotatorId: string | number;
}>;


export type UnbindDocTypeFieldToDataSourceMutation = { unbindAnnotatorFromDocTypeField: { left: { id: string | null } | null } | null };

export type BucketsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type BucketsQuery = { buckets: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, enabled: boolean } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type QueryAnalysisOptionsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type QueryAnalysisOptionsQuery = { options: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type QueryAnalysisValueQueryVariables = Exact<{
  id: string | number;
}>;


export type QueryAnalysisValueQuery = { value: { id: string | null, name: string | null, description: string | null } | null };

export type BindQueryAnalysisToBucketMutationVariables = Exact<{
  bucketId: string | number;
  queryAnalysis: string | number;
}>;


export type BindQueryAnalysisToBucketMutation = { bindQueryAnalysisToBucket: { left: { id: string | null, queryAnalysis: { id: string | null } | null } | null, right: { id: string | null } | null } | null };

export type UnbindQueryAnalysisFromBucketMutationVariables = Exact<{
  bucketId: string | number;
}>;


export type UnbindQueryAnalysisFromBucketMutation = { unbindQueryAnalysisFromBucket: { right: { id: string | null } | null } | null };

export type BindSearchConfigToBucketMutationVariables = Exact<{
  bucketId: string | number;
  searchConfigId: string | number;
}>;


export type BindSearchConfigToBucketMutation = { bindSearchConfigToBucket: { left: { id: string | null, searchConfig: { id: string | null } | null } | null, right: { id: string | null } | null } | null };

export type UnbindSearchConfigFromBucketMutationVariables = Exact<{
  bucketId: string | number;
}>;


export type UnbindSearchConfigFromBucketMutation = { unbindSearchConfigFromBucket: { right: { id: string | null } | null } | null };

export type BindLanguageToBucketMutationVariables = Exact<{
  bucketId: string | number;
  languageId: string | number;
}>;


export type BindLanguageToBucketMutation = { bindLanguageToBucket: { left: { id: string | null, language: { id: string | null } | null } | null, right: { id: string | null } | null } | null };

export type UnbindLanguageFromBucketMutationVariables = Exact<{
  bucketId: string | number;
}>;


export type UnbindLanguageFromBucketMutation = { unbindLanguageFromBucket: { right: { id: string | null } | null } | null };

export type BucketQueryVariables = Exact<{
  id: string | number;
}>;


export type BucketQuery = { bucket: { id: string | null, name: string | null, description: string | null, enabled: boolean, refreshOnDate: boolean | null, refreshOnQuery: boolean | null, refreshOnSuggestionCategory: boolean | null, refreshOnTab: boolean | null, retrieveType: RetrieveType | null, queryAnalysis: { id: string | null, name: string | null } | null, searchConfig: { id: string | null, name: string | null } | null, ragConfigurationChat: { id: string | null, name: string | null } | null, ragConfigurationChatTool: { id: string | null, name: string | null } | null, ragConfigurationSimpleGenerate: { id: string | null, name: string | null } | null, language: { id: string | null, name: string | null } | null, autocorrection: { id: string | null, name: string | null } | null, autocomplete: { id: string | null, name: string | null } | null, highlight: { id: string | null, name: string | null } | null } | null };

export type EnableBucketMutationVariables = Exact<{
  id: string | number;
}>;


export type EnableBucketMutation = { enableBucket: { id: string | null, name: string | null } | null };

export type DeleteBucketMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteBucketMutation = { deleteBucket: { id: string | null, name: string | null } | null };

export type BucketDataSourcesQueryVariables = Exact<{
  parentId: string | number;
  searchText?: string | null | undefined;
  unassociated: boolean;
  cursor?: string | null | undefined;
}>;


export type BucketDataSourcesQuery = { bucket: { id: string | null, tabs: { edges: Array<{ node: { name: string | null, id: string | null } | null } | null> | null } | null, suggestionCategories: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null, datasources: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null, languages: { edges: Array<{ node: { id: string | null, name: string | null, value: string | null } | null } | null> | null } | null } | null };

export type CreateOrUpdateBucketMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  refreshOnDate: boolean;
  refreshOnQuery: boolean;
  refreshOnSuggestionCategory: boolean;
  refreshOnTab: boolean;
  retrieveType: RetrieveType;
  datasourceIds?: Array<any> | any | null | undefined;
  suggestionCategoryIds?: Array<any> | any | null | undefined;
  tabIds?: Array<any> | any | null | undefined;
  queryAnalysisId?: any;
  defaultLanguageId?: any;
  searchConfigId?: any;
  ragConfigurationChat?: any;
  ragConfigurationChatTool?: any;
  ragConfigurationSimpleGenerate?: any;
  autocorrection?: any;
  autocomplete?: any;
  highlightId?: any;
  languageIds?: Array<any> | any | null | undefined;
}>;


export type CreateOrUpdateBucketMutation = { bucketWithLists: { entity: { id: string | null, name: string | null, enabled: boolean } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type AddDataSourceToBucketMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddDataSourceToBucketMutation = { addDatasourceToBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type RemoveDataSourceFromBucketMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type RemoveDataSourceFromBucketMutation = { removeDatasourceFromBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type BucketLanguagesQueryVariables = Exact<{
  parentId: string | number;
  searchText?: string | null | undefined;
  unassociated: boolean;
  cursor?: string | null | undefined;
}>;


export type BucketLanguagesQuery = { bucket: { id: string | null, languages: { edges: Array<{ node: { id: string | null, name: string | null, value: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null } | null };

export type AddLanguageToBucketMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddLanguageToBucketMutation = { addLanguageToBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type RemoveLanguageFromBucketMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type RemoveLanguageFromBucketMutation = { removeLanguageFromBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type BucketTabsQueryVariables = Exact<{
  parentId: string | number;
  searchText?: string | null | undefined;
  unassociated: boolean;
  cursor?: string | null | undefined;
}>;


export type BucketTabsQuery = { bucket: { id: string | null, tabs: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null } | null };

export type AddTabToBucketMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddTabToBucketMutation = { addTabToBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type RemoveTabFromBucketMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type RemoveTabFromBucketMutation = { removeTabFromBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type AddAutocorrectionToBucketMutationVariables = Exact<{
  autocorrectionId: string | number;
  parentId: string | number;
}>;


export type AddAutocorrectionToBucketMutation = { bindAutocorrectionToBucket: { left: { id: string | null } | null } | null };

export type RemoveAutocorrectionFromBucketMutationVariables = Exact<{
  parentId: string | number;
}>;


export type RemoveAutocorrectionFromBucketMutation = { unbindAutocorrectionFromBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type AddAutocompleteToBucketMutationVariables = Exact<{
  autocompleteId: string | number;
  parentId: string | number;
}>;


export type AddAutocompleteToBucketMutation = { bindAutocompleteToBucket: { left: { id: string | null } | null } | null };

export type RemoveAutocompleteFromBucketMutationVariables = Exact<{
  parentId: string | number;
}>;


export type RemoveAutocompleteFromBucketMutation = { unbindAutocompleteFromBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type BucketSuggestionCategoriesQueryVariables = Exact<{
  parentId: string | number;
  searchText?: string | null | undefined;
  unassociated: boolean;
  cursor?: string | null | undefined;
}>;


export type BucketSuggestionCategoriesQuery = { bucket: { id: string | null, suggestionCategories: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null } | null };

export type UnboundAutocorrectionByBucketQueryVariables = Exact<{
  parentId: string | number;
}>;


export type UnboundAutocorrectionByBucketQuery = { bucket: { id: string | null, name: string | null } | null };

export type UnboundAutocompleteByBucketQueryVariables = Exact<{
  parentId: string | number;
}>;


export type UnboundAutocompleteByBucketQuery = { bucket: { id: string | null, name: string | null } | null };

export type AddSuggestionCategoryToBucketMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddSuggestionCategoryToBucketMutation = { addSuggestionCategoryToBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type RemoveSuggestionCategoryFromBucketMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type RemoveSuggestionCategoryFromBucketMutation = { removeSuggestionCategoryFromBucket: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type AutocompletesOptionsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type AutocompletesOptionsQuery = { autocompletes: { edges: Array<{ cursor: string | null, node: { id: string | null, description: string | null, name: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type CreateDataIndexMutationVariables = Exact<{
  name: string;
  datasourceId: string | number;
  description?: string | null | undefined;
  knnIndex?: boolean | null | undefined;
  docTypeIds?: Array<any> | any | null | undefined;
  chunkType?: ChunkType | null | undefined;
  chunkWindowSize?: number | null | undefined;
  embeddingJsonConfig?: string | null | undefined;
  embeddingDocTypeFieldId?: any;
  chunkHeaderFieldIds?: Array<any> | any | null | undefined;
  settings?: string | null | undefined;
}>;


export type CreateDataIndexMutation = { dataIndex: { entity: { name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type DataIndicesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  first?: number | null | undefined;
  after?: string | null | undefined;
}>;


export type DataIndicesQuery = { dataIndices: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, createDate: any, docTypes: { edges: Array<{ node: { id: string | null } | null } | null> | null } | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DataIndexQueryVariables = Exact<{
  id: string | number;
}>;


export type DataIndexQuery = { dataIndex: { name: string | null, description: string | null, settings: string | null, customSettings: string | null, chunkType: ChunkType | null, chunkWindowSize: number | null, embeddingJsonConfig: string | null, knnIndex: boolean | null, datasource: { id: string | null, name: string | null } | null, embeddingDocTypeField: { id: string | null, name: string | null } | null, chunkHeaderFields: Array<{ id: string | null, name: string | null } | null> | null, docTypes: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null, cat: { docsCount: string | null, docsDeleted: string | null, storeSize: any } | null } | null };

export type DatasourceIndexConfigQueryVariables = Exact<{
  id: string | number;
}>;


export type DatasourceIndexConfigQuery = { datasource: { knnIndex: boolean | null, chunkType: ChunkType | null, chunkWindowSize: number | null, embeddingJsonConfig: string | null, settings: string | null, embeddingDocTypeField: { id: string | null, name: string | null } | null, docTypes: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null } | null };

export type DeleteDataIndexMutationVariables = Exact<{
  dataIndexId: string | number;
  dataIndexName: string;
}>;


export type DeleteDataIndexMutation = { deleteDataIndex: { id: string | null, name: string | null } | null };

export type DataIndexMappingQueryVariables = Exact<{
  id: string | number;
}>;


export type DataIndexMappingQuery = { dataIndex: { mappings: string | null } | null };

export type UpdateIndexSettingsMutationVariables = Exact<{
  dataIndexId: string | number;
  settings?: string | null | undefined;
  closeIfNeeded: boolean;
}>;


export type UpdateIndexSettingsMutation = { updateIndexSettings: { dataIndexId: any, indexName: string | null, status: Status | null, reason: string | null } | null };

export type AlignIndexMutationVariables = Exact<{
  dataIndexId: string | number;
  closeIfNeeded: boolean;
}>;


export type AlignIndexMutation = { alignIndex: { dataIndexId: any, indexName: string | null, status: Status | null, reason: string | null } | null };

export type DataSourcesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
  first?: number | null | undefined;
  sortByList?: Array<SortByInput> | SortByInput | null | undefined;
}>;


export type DataSourcesQuery = { datasources: { __typename: 'DefaultConnection_Datasource', edges: Array<{ __typename: 'DefaultEdge_Datasource', node: { __typename: 'Datasource', id: string | null, name: string | null, schedulable: boolean | null, scheduling: string | null, reindexable: boolean | null, reindexing: string | null, lastIngestionDate: any, jsonConfig: string | null, description: string | null } | null } | null> | null, pageInfo: { __typename: 'DefaultPageInfo', hasNextPage: boolean, endCursor: string | null } | null } | null };

export type DeleteDataSourceMutationVariables = Exact<{
  id: string | number;
  datasourceName: string;
}>;


export type DeleteDataSourceMutation = { deleteDatasource: { id: string | null, name: string | null } | null };

export type UnboundBucketsByDatasourceQueryVariables = Exact<{
  datasourceId: any;
}>;


export type UnboundBucketsByDatasourceQuery = { unboundBucketsByDatasource: Array<{ name: string | null, id: string | null } | null> | null };

export type DataSourceQueryVariables = Exact<{
  id: string | number;
  searchText?: string | null | undefined;
}>;


export type DataSourceQuery = { datasource: { id: string | null, name: string | null, description: string | null, schedulable: boolean | null, scheduling: string | null, jsonConfig: string | null, reindexable: boolean | null, reindexing: string | null, purgeable: boolean | null, purging: string | null, purgeMaxAge: string | null, lastIngestionDate: any, pluginDriver: { id: string | null, name: string | null, provisioning: Provisioning | null, resourceUri: { baseUri: string | null, path: string | null } | null } | null, dataIndex: { id: string | null, name: string | null, description: string | null, knnIndex: boolean | null } | null, enrichPipeline: { id: string | null, name: string | null } | null, dataIndexes: { edges: Array<{ node: { id: string | null, name: string | null } | null } | null> | null } | null } | null };

export type CreateDatasourceConnectionMutationVariables = Exact<{
  name: string;
  description?: string | null | undefined;
  schedulable: boolean;
  scheduling: string;
  jsonConfig?: string | null | undefined;
  pluginDriverId: any;
  pipeline?: PipelineWithItemsDtoInput | null | undefined;
  pipelineId?: any;
  reindexable: boolean;
  reindexing: string;
  purgeable: boolean;
  purging: string;
  purgeMaxAge: string;
  dataIndex: DataIndexDtoInput;
  knnIndex?: boolean | null | undefined;
  chunkType?: ChunkType | null | undefined;
  chunkWindowSize?: number | null | undefined;
  embeddingJsonConfig?: string | null | undefined;
  embeddingDocTypeFieldId?: any;
  docTypeIds?: Array<any> | any | null | undefined;
}>;


export type CreateDatasourceConnectionMutation = { createDatasourceConnection: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type UpdateDatasourceConnectionMutationVariables = Exact<{
  name: string;
  description?: string | null | undefined;
  schedulable: boolean;
  scheduling: string;
  jsonConfig?: string | null | undefined;
  pipeline?: PipelineWithItemsDtoInput | null | undefined;
  pipelineId?: any;
  dataIndexId: any;
  datasourceId: any;
  reindexable: boolean;
  reindexing: string;
  purging: string;
  purgeable: boolean;
  purgeMaxAge: string;
}>;


export type UpdateDatasourceConnectionMutation = { updateDatasourceConnection: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type QDatasourceSchedulersQueryVariables = Exact<{
  id: string | number;
  first?: number | null | undefined;
  after?: string | null | undefined;
}>;


export type QDatasourceSchedulersQuery = { datasource: { id: string | null, schedulers: { edges: Array<{ node: { id: string | null, status: SchedulerStatus | null, modifiedDate: any, reindex: boolean } | null } | null> | null, pageInfo: { __typename: 'DefaultPageInfo', hasNextPage: boolean, endCursor: string | null } | null } | null } | null };

export type DataSourceInformationQueryVariables = Exact<{
  id: string | number;
}>;


export type DataSourceInformationQuery = { datasource: { dataIndex: { cat: { docsCount: string | null, docsDeleted: string | null, health: string | null, index: string | null, pri: string | null, priStoreSize: any, rep: string | null, status: string | null, storeSize: any, uuid: string | null } | null } | null } | null };

export type EnrichPipelineOptionsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type EnrichPipelineOptionsQuery = { options: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type EnrichItemsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type EnrichItemsQuery = { enrichItems: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null, type: EnrichItemType | null, script: string | null, behaviorMergeType: BehaviorMergeType | null, jsonPath: string | null, behaviorOnError: BehaviorOnError | null, requestTimeout: any, resourceUri: { baseUri: string | null, path: string | null } | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type PluginDriversQueryVariables = Exact<{ [key: string]: never; }>;


export type PluginDriversQuery = { pluginDriversPageFilter: { content: Array<{ id: string | null, name: string | null, description: string | null, provisioning: Provisioning | null, type: PluginDriverType | null, resourceUri: { baseUri: string | null, path: string | null } | null } | null> | null } | null };

export type CreateOrUpdatePluginDriverMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
  type: PluginDriverType;
  resourceUri?: ResourceUriInput | null | undefined;
  provisioning?: Provisioning | null | undefined;
}>;


export type CreateOrUpdatePluginDriverMutation = { pluginDriver: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type EnrichPipelinesQueryVariables = Exact<{
  searchText?: string | null | undefined;
  after?: string | null | undefined;
}>;


export type EnrichPipelinesQuery = { enrichPipelines: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null };

export type EnrichPipelinesValueOptionsQueryVariables = Exact<{
  id: any;
}>;


export type EnrichPipelinesValueOptionsQuery = { unboundEnrichPipelines: Array<{ name: string | null, id: string | null } | null> | null };

export type DeleteEnrichPipelineMutationVariables = Exact<{
  id: string | number;
}>;


export type DeleteEnrichPipelineMutation = { deleteEnrichPipeline: { id: string | null, name: string | null } | null };

export type EnrichPipelineQueryVariables = Exact<{
  id: string | number;
}>;


export type EnrichPipelineQuery = { enrichPipeline: { id: string | null, name: string | null, description: string | null } | null };

export type AssociatedEnrichPipelineEnrichItemsQueryVariables = Exact<{
  enrichPipelineId: string | number;
}>;


export type AssociatedEnrichPipelineEnrichItemsQuery = { enrichPipeline: { id: string | null, enrichItems: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null } | null };

export type UnassociatedEnrichPipelineEnrichItemsQueryVariables = Exact<{
  enrichPipelineId: string | number;
  searchText?: string | null | undefined;
}>;


export type UnassociatedEnrichPipelineEnrichItemsQuery = { enrichPipeline: { id: string | null, enrichItems: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null, pageInfo: { hasNextPage: boolean, endCursor: string | null } | null } | null } | null };

export type CreateOrUpdateEnrichPipelineMutationVariables = Exact<{
  id?: string | number | null | undefined;
  name: string;
  description?: string | null | undefined;
}>;


export type CreateOrUpdateEnrichPipelineMutation = { enrichPipeline: { entity: { id: string | null, name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };

export type AddEnrichItemToEnrichPipelineMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type AddEnrichItemToEnrichPipelineMutation = { addEnrichItemToEnrichPipeline: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type RemoveEnrichItemFromEnrichPipelineMutationVariables = Exact<{
  childId: string | number;
  parentId: string | number;
}>;


export type RemoveEnrichItemFromEnrichPipelineMutation = { removeEnrichItemFromEnrichPipeline: { left: { id: string | null } | null, right: { id: string | null } | null } | null };

export type SortEnrichItemsMutationVariables = Exact<{
  enrichPipelineId: string | number;
  enrichItemIdList?: Array<any> | any | null | undefined;
}>;


export type SortEnrichItemsMutation = { sortEnrichItems: { id: string | null, enrichItems: { edges: Array<{ node: { id: string | null, name: string | null, description: string | null } | null } | null> | null } | null } | null };

export type EnrichPipelineWithItemsMutationVariables = Exact<{
  id?: string | number | null | undefined;
  items?: Array<ItemDtoInput | null | undefined> | ItemDtoInput | null | undefined;
  name: string;
  description: string;
}>;


export type EnrichPipelineWithItemsMutation = { enrichPipelineWithEnrichItems: { entity: { name: string | null } | null, fieldValidators: Array<{ field: string | null, message: string | null } | null> | null } | null };


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
// @ts-ignore
export function useLanguagesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<LanguagesQuery, LanguagesQueryVariables>): Apollo.UseSuspenseQueryResult<LanguagesQuery, LanguagesQueryVariables>;
export function useLanguagesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LanguagesQuery, LanguagesQueryVariables>): Apollo.UseSuspenseQueryResult<LanguagesQuery | undefined, LanguagesQueryVariables>;
export function useLanguagesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LanguagesQuery, LanguagesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<LanguagesQuery, LanguagesQueryVariables>(LanguagesDocument, options);
        }
export type LanguagesQueryHookResult = ReturnType<typeof useLanguagesQuery>;
export type LanguagesLazyQueryHookResult = ReturnType<typeof useLanguagesLazyQuery>;
export type LanguagesSuspenseQueryHookResult = ReturnType<typeof useLanguagesSuspenseQuery>;
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
// @ts-ignore
export function useAnalyzersSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AnalyzersQuery, AnalyzersQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzersQuery, AnalyzersQueryVariables>;
export function useAnalyzersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzersQuery, AnalyzersQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzersQuery | undefined, AnalyzersQueryVariables>;
export function useAnalyzersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzersQuery, AnalyzersQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AnalyzersQuery, AnalyzersQueryVariables>(AnalyzersDocument, options);
        }
export type AnalyzersQueryHookResult = ReturnType<typeof useAnalyzersQuery>;
export type AnalyzersLazyQueryHookResult = ReturnType<typeof useAnalyzersLazyQuery>;
export type AnalyzersSuspenseQueryHookResult = ReturnType<typeof useAnalyzersSuspenseQuery>;
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
// @ts-ignore
export function useAnalyzerOptionsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>;
export function useAnalyzerOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzerOptionsQuery | undefined, AnalyzerOptionsQueryVariables>;
export function useAnalyzerOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AnalyzerOptionsQuery, AnalyzerOptionsQueryVariables>(AnalyzerOptionsDocument, options);
        }
export type AnalyzerOptionsQueryHookResult = ReturnType<typeof useAnalyzerOptionsQuery>;
export type AnalyzerOptionsLazyQueryHookResult = ReturnType<typeof useAnalyzerOptionsLazyQuery>;
export type AnalyzerOptionsSuspenseQueryHookResult = ReturnType<typeof useAnalyzerOptionsSuspenseQuery>;
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
export function useAnalyzerValueQuery(baseOptions: Apollo.QueryHookOptions<AnalyzerValueQuery, AnalyzerValueQueryVariables> & ({ variables: AnalyzerValueQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzerValueQuery, AnalyzerValueQueryVariables>(AnalyzerValueDocument, options);
      }
export function useAnalyzerValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzerValueQuery, AnalyzerValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzerValueQuery, AnalyzerValueQueryVariables>(AnalyzerValueDocument, options);
        }
// @ts-ignore
export function useAnalyzerValueSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AnalyzerValueQuery, AnalyzerValueQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzerValueQuery, AnalyzerValueQueryVariables>;
export function useAnalyzerValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzerValueQuery, AnalyzerValueQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzerValueQuery | undefined, AnalyzerValueQueryVariables>;
export function useAnalyzerValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzerValueQuery, AnalyzerValueQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AnalyzerValueQuery, AnalyzerValueQueryVariables>(AnalyzerValueDocument, options);
        }
export type AnalyzerValueQueryHookResult = ReturnType<typeof useAnalyzerValueQuery>;
export type AnalyzerValueLazyQueryHookResult = ReturnType<typeof useAnalyzerValueLazyQuery>;
export type AnalyzerValueSuspenseQueryHookResult = ReturnType<typeof useAnalyzerValueSuspenseQuery>;
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
    query SearchConfigOptions($searchText: String, $first: Int, $cursor: String) {
  options: searchConfigs(searchText: $searchText, first: $first, after: $cursor) {
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
 *      first: // value for 'first'
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
// @ts-ignore
export function useSearchConfigOptionsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>;
export function useSearchConfigOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<SearchConfigOptionsQuery | undefined, SearchConfigOptionsQueryVariables>;
export function useSearchConfigOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SearchConfigOptionsQuery, SearchConfigOptionsQueryVariables>(SearchConfigOptionsDocument, options);
        }
export type SearchConfigOptionsQueryHookResult = ReturnType<typeof useSearchConfigOptionsQuery>;
export type SearchConfigOptionsLazyQueryHookResult = ReturnType<typeof useSearchConfigOptionsLazyQuery>;
export type SearchConfigOptionsSuspenseQueryHookResult = ReturnType<typeof useSearchConfigOptionsSuspenseQuery>;
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
export function useSearchConfigValueQuery(baseOptions: Apollo.QueryHookOptions<SearchConfigValueQuery, SearchConfigValueQueryVariables> & ({ variables: SearchConfigValueQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SearchConfigValueQuery, SearchConfigValueQueryVariables>(SearchConfigValueDocument, options);
      }
export function useSearchConfigValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SearchConfigValueQuery, SearchConfigValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SearchConfigValueQuery, SearchConfigValueQueryVariables>(SearchConfigValueDocument, options);
        }
// @ts-ignore
export function useSearchConfigValueSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SearchConfigValueQuery, SearchConfigValueQueryVariables>): Apollo.UseSuspenseQueryResult<SearchConfigValueQuery, SearchConfigValueQueryVariables>;
export function useSearchConfigValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SearchConfigValueQuery, SearchConfigValueQueryVariables>): Apollo.UseSuspenseQueryResult<SearchConfigValueQuery | undefined, SearchConfigValueQueryVariables>;
export function useSearchConfigValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SearchConfigValueQuery, SearchConfigValueQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SearchConfigValueQuery, SearchConfigValueQueryVariables>(SearchConfigValueDocument, options);
        }
export type SearchConfigValueQueryHookResult = ReturnType<typeof useSearchConfigValueQuery>;
export type SearchConfigValueLazyQueryHookResult = ReturnType<typeof useSearchConfigValueLazyQuery>;
export type SearchConfigValueSuspenseQueryHookResult = ReturnType<typeof useSearchConfigValueSuspenseQuery>;
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
// @ts-ignore
export function useLanguagesOptionsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>;
export function useLanguagesOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<LanguagesOptionsQuery | undefined, LanguagesOptionsQueryVariables>;
export function useLanguagesOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<LanguagesOptionsQuery, LanguagesOptionsQueryVariables>(LanguagesOptionsDocument, options);
        }
export type LanguagesOptionsQueryHookResult = ReturnType<typeof useLanguagesOptionsQuery>;
export type LanguagesOptionsLazyQueryHookResult = ReturnType<typeof useLanguagesOptionsLazyQuery>;
export type LanguagesOptionsSuspenseQueryHookResult = ReturnType<typeof useLanguagesOptionsSuspenseQuery>;
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
export function useLanguageValueQuery(baseOptions: Apollo.QueryHookOptions<LanguageValueQuery, LanguageValueQueryVariables> & ({ variables: LanguageValueQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<LanguageValueQuery, LanguageValueQueryVariables>(LanguageValueDocument, options);
      }
export function useLanguageValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<LanguageValueQuery, LanguageValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<LanguageValueQuery, LanguageValueQueryVariables>(LanguageValueDocument, options);
        }
// @ts-ignore
export function useLanguageValueSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<LanguageValueQuery, LanguageValueQueryVariables>): Apollo.UseSuspenseQueryResult<LanguageValueQuery, LanguageValueQueryVariables>;
export function useLanguageValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LanguageValueQuery, LanguageValueQueryVariables>): Apollo.UseSuspenseQueryResult<LanguageValueQuery | undefined, LanguageValueQueryVariables>;
export function useLanguageValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LanguageValueQuery, LanguageValueQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<LanguageValueQuery, LanguageValueQueryVariables>(LanguageValueDocument, options);
        }
export type LanguageValueQueryHookResult = ReturnType<typeof useLanguageValueQuery>;
export type LanguageValueLazyQueryHookResult = ReturnType<typeof useLanguageValueLazyQuery>;
export type LanguageValueSuspenseQueryHookResult = ReturnType<typeof useLanguageValueSuspenseQuery>;
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
export function useAnalyzerQuery(baseOptions: Apollo.QueryHookOptions<AnalyzerQuery, AnalyzerQueryVariables> & ({ variables: AnalyzerQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzerQuery, AnalyzerQueryVariables>(AnalyzerDocument, options);
      }
export function useAnalyzerLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzerQuery, AnalyzerQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzerQuery, AnalyzerQueryVariables>(AnalyzerDocument, options);
        }
// @ts-ignore
export function useAnalyzerSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AnalyzerQuery, AnalyzerQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzerQuery, AnalyzerQueryVariables>;
export function useAnalyzerSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzerQuery, AnalyzerQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzerQuery | undefined, AnalyzerQueryVariables>;
export function useAnalyzerSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzerQuery, AnalyzerQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AnalyzerQuery, AnalyzerQueryVariables>(AnalyzerDocument, options);
        }
export type AnalyzerQueryHookResult = ReturnType<typeof useAnalyzerQuery>;
export type AnalyzerLazyQueryHookResult = ReturnType<typeof useAnalyzerLazyQuery>;
export type AnalyzerSuspenseQueryHookResult = ReturnType<typeof useAnalyzerSuspenseQuery>;
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
export function useAnalyzersAssociationsQuery(baseOptions: Apollo.QueryHookOptions<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables> & ({ variables: AnalyzersAssociationsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>(AnalyzersAssociationsDocument, options);
      }
export function useAnalyzersAssociationsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>(AnalyzersAssociationsDocument, options);
        }
// @ts-ignore
export function useAnalyzersAssociationsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>;
export function useAnalyzersAssociationsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>): Apollo.UseSuspenseQueryResult<AnalyzersAssociationsQuery | undefined, AnalyzersAssociationsQueryVariables>;
export function useAnalyzersAssociationsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>(AnalyzersAssociationsDocument, options);
        }
export type AnalyzersAssociationsQueryHookResult = ReturnType<typeof useAnalyzersAssociationsQuery>;
export type AnalyzersAssociationsLazyQueryHookResult = ReturnType<typeof useAnalyzersAssociationsLazyQuery>;
export type AnalyzersAssociationsSuspenseQueryHookResult = ReturnType<typeof useAnalyzersAssociationsSuspenseQuery>;
export type AnalyzersAssociationsQueryResult = Apollo.QueryResult<AnalyzersAssociationsQuery, AnalyzersAssociationsQueryVariables>;
export const AutocompletesDocument = gql`
    query Autocompletes($searchText: String, $after: String) {
  autocompletes(searchText: $searchText, first: 20, after: $after) {
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
 * __useAutocompletesQuery__
 *
 * To run a query within a React component, call `useAutocompletesQuery` and pass it any options that fit your needs.
 * When your component renders, `useAutocompletesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAutocompletesQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useAutocompletesQuery(baseOptions?: Apollo.QueryHookOptions<AutocompletesQuery, AutocompletesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AutocompletesQuery, AutocompletesQueryVariables>(AutocompletesDocument, options);
      }
export function useAutocompletesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AutocompletesQuery, AutocompletesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AutocompletesQuery, AutocompletesQueryVariables>(AutocompletesDocument, options);
        }
// @ts-ignore
export function useAutocompletesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AutocompletesQuery, AutocompletesQueryVariables>): Apollo.UseSuspenseQueryResult<AutocompletesQuery, AutocompletesQueryVariables>;
export function useAutocompletesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocompletesQuery, AutocompletesQueryVariables>): Apollo.UseSuspenseQueryResult<AutocompletesQuery | undefined, AutocompletesQueryVariables>;
export function useAutocompletesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocompletesQuery, AutocompletesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AutocompletesQuery, AutocompletesQueryVariables>(AutocompletesDocument, options);
        }
export type AutocompletesQueryHookResult = ReturnType<typeof useAutocompletesQuery>;
export type AutocompletesLazyQueryHookResult = ReturnType<typeof useAutocompletesLazyQuery>;
export type AutocompletesSuspenseQueryHookResult = ReturnType<typeof useAutocompletesSuspenseQuery>;
export type AutocompletesQueryResult = Apollo.QueryResult<AutocompletesQuery, AutocompletesQueryVariables>;
export const DeleteAutocompleteDocument = gql`
    mutation DeleteAutocomplete($id: ID!) {
  deleteAutocomplete(id: $id) {
    id
    name
  }
}
    `;
export type DeleteAutocompleteMutationFn = Apollo.MutationFunction<DeleteAutocompleteMutation, DeleteAutocompleteMutationVariables>;

/**
 * __useDeleteAutocompleteMutation__
 *
 * To run a mutation, you first call `useDeleteAutocompleteMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteAutocompleteMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteAutocompleteMutation, { data, loading, error }] = useDeleteAutocompleteMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteAutocompleteMutation(baseOptions?: Apollo.MutationHookOptions<DeleteAutocompleteMutation, DeleteAutocompleteMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteAutocompleteMutation, DeleteAutocompleteMutationVariables>(DeleteAutocompleteDocument, options);
      }
export type DeleteAutocompleteMutationHookResult = ReturnType<typeof useDeleteAutocompleteMutation>;
export type DeleteAutocompleteMutationResult = Apollo.MutationResult<DeleteAutocompleteMutation>;
export type DeleteAutocompleteMutationOptions = Apollo.BaseMutationOptions<DeleteAutocompleteMutation, DeleteAutocompleteMutationVariables>;
export const AutocompleteDocument = gql`
    query Autocomplete($id: ID!) {
  autocomplete(id: $id) {
    id
    fuzziness
    minimumShouldMatch
    name
    operator
    resultSize
    perfectMatchIncluded
    fields {
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
 * __useAutocompleteQuery__
 *
 * To run a query within a React component, call `useAutocompleteQuery` and pass it any options that fit your needs.
 * When your component renders, `useAutocompleteQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAutocompleteQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useAutocompleteQuery(baseOptions: Apollo.QueryHookOptions<AutocompleteQuery, AutocompleteQueryVariables> & ({ variables: AutocompleteQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AutocompleteQuery, AutocompleteQueryVariables>(AutocompleteDocument, options);
      }
export function useAutocompleteLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AutocompleteQuery, AutocompleteQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AutocompleteQuery, AutocompleteQueryVariables>(AutocompleteDocument, options);
        }
// @ts-ignore
export function useAutocompleteSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AutocompleteQuery, AutocompleteQueryVariables>): Apollo.UseSuspenseQueryResult<AutocompleteQuery, AutocompleteQueryVariables>;
export function useAutocompleteSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocompleteQuery, AutocompleteQueryVariables>): Apollo.UseSuspenseQueryResult<AutocompleteQuery | undefined, AutocompleteQueryVariables>;
export function useAutocompleteSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocompleteQuery, AutocompleteQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AutocompleteQuery, AutocompleteQueryVariables>(AutocompleteDocument, options);
        }
export type AutocompleteQueryHookResult = ReturnType<typeof useAutocompleteQuery>;
export type AutocompleteLazyQueryHookResult = ReturnType<typeof useAutocompleteLazyQuery>;
export type AutocompleteSuspenseQueryHookResult = ReturnType<typeof useAutocompleteSuspenseQuery>;
export type AutocompleteQueryResult = Apollo.QueryResult<AutocompleteQuery, AutocompleteQueryVariables>;
export const CreateOrUpdateAutocompleteDocument = gql`
    mutation CreateOrUpdateAutocomplete($id: ID, $autocompleteDTO: AutocompleteDTOInput!) {
  autocomplete(id: $id, autocompleteDTO: $autocompleteDTO) {
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
export type CreateOrUpdateAutocompleteMutationFn = Apollo.MutationFunction<CreateOrUpdateAutocompleteMutation, CreateOrUpdateAutocompleteMutationVariables>;

/**
 * __useCreateOrUpdateAutocompleteMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateAutocompleteMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateAutocompleteMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateAutocompleteMutation, { data, loading, error }] = useCreateOrUpdateAutocompleteMutation({
 *   variables: {
 *      id: // value for 'id'
 *      autocompleteDTO: // value for 'autocompleteDTO'
 *   },
 * });
 */
export function useCreateOrUpdateAutocompleteMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateAutocompleteMutation, CreateOrUpdateAutocompleteMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateAutocompleteMutation, CreateOrUpdateAutocompleteMutationVariables>(CreateOrUpdateAutocompleteDocument, options);
      }
export type CreateOrUpdateAutocompleteMutationHookResult = ReturnType<typeof useCreateOrUpdateAutocompleteMutation>;
export type CreateOrUpdateAutocompleteMutationResult = Apollo.MutationResult<CreateOrUpdateAutocompleteMutation>;
export type CreateOrUpdateAutocompleteMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateAutocompleteMutation, CreateOrUpdateAutocompleteMutationVariables>;
export const UnboundDocTypeFieldByAutocompleteDocument = gql`
    query UnboundDocTypeFieldByAutocomplete($autocompleteId: BigInteger!) {
  unboundDocTypeFieldByAutocomplete(autocompleteId: $autocompleteId) {
    id
    name
  }
}
    `;

/**
 * __useUnboundDocTypeFieldByAutocompleteQuery__
 *
 * To run a query within a React component, call `useUnboundDocTypeFieldByAutocompleteQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundDocTypeFieldByAutocompleteQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundDocTypeFieldByAutocompleteQuery({
 *   variables: {
 *      autocompleteId: // value for 'autocompleteId'
 *   },
 * });
 */
export function useUnboundDocTypeFieldByAutocompleteQuery(baseOptions: Apollo.QueryHookOptions<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables> & ({ variables: UnboundDocTypeFieldByAutocompleteQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables>(UnboundDocTypeFieldByAutocompleteDocument, options);
      }
export function useUnboundDocTypeFieldByAutocompleteLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables>(UnboundDocTypeFieldByAutocompleteDocument, options);
        }
// @ts-ignore
export function useUnboundDocTypeFieldByAutocompleteSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables>;
export function useUnboundDocTypeFieldByAutocompleteSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundDocTypeFieldByAutocompleteQuery | undefined, UnboundDocTypeFieldByAutocompleteQueryVariables>;
export function useUnboundDocTypeFieldByAutocompleteSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables>(UnboundDocTypeFieldByAutocompleteDocument, options);
        }
export type UnboundDocTypeFieldByAutocompleteQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldByAutocompleteQuery>;
export type UnboundDocTypeFieldByAutocompleteLazyQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldByAutocompleteLazyQuery>;
export type UnboundDocTypeFieldByAutocompleteSuspenseQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldByAutocompleteSuspenseQuery>;
export type UnboundDocTypeFieldByAutocompleteQueryResult = Apollo.QueryResult<UnboundDocTypeFieldByAutocompleteQuery, UnboundDocTypeFieldByAutocompleteQueryVariables>;
export const AutocorrectionsOptionsDocument = gql`
    query AutocorrectionsOptions($searchText: String, $after: String) {
  autocorrections(searchText: $searchText, first: 5, after: $after) {
    edges {
      cursor
      node {
        id
        description
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
 * __useAutocorrectionsOptionsQuery__
 *
 * To run a query within a React component, call `useAutocorrectionsOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useAutocorrectionsOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAutocorrectionsOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useAutocorrectionsOptionsQuery(baseOptions?: Apollo.QueryHookOptions<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>(AutocorrectionsOptionsDocument, options);
      }
export function useAutocorrectionsOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>(AutocorrectionsOptionsDocument, options);
        }
// @ts-ignore
export function useAutocorrectionsOptionsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>;
export function useAutocorrectionsOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<AutocorrectionsOptionsQuery | undefined, AutocorrectionsOptionsQueryVariables>;
export function useAutocorrectionsOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>(AutocorrectionsOptionsDocument, options);
        }
export type AutocorrectionsOptionsQueryHookResult = ReturnType<typeof useAutocorrectionsOptionsQuery>;
export type AutocorrectionsOptionsLazyQueryHookResult = ReturnType<typeof useAutocorrectionsOptionsLazyQuery>;
export type AutocorrectionsOptionsSuspenseQueryHookResult = ReturnType<typeof useAutocorrectionsOptionsSuspenseQuery>;
export type AutocorrectionsOptionsQueryResult = Apollo.QueryResult<AutocorrectionsOptionsQuery, AutocorrectionsOptionsQueryVariables>;
export const AutocorrectionValueDocument = gql`
    query AutocorrectionValue($id: ID!) {
  autocorrection(id: $id) {
    id
    modifiedDate
    description
    maxEdit
    minWordLength
    name
    prefixLength
    sort
    suggestMode
    enableSearchWithCorrection
    autocorrectionDocTypeField {
      id
      name
    }
  }
}
    `;

/**
 * __useAutocorrectionValueQuery__
 *
 * To run a query within a React component, call `useAutocorrectionValueQuery` and pass it any options that fit your needs.
 * When your component renders, `useAutocorrectionValueQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAutocorrectionValueQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useAutocorrectionValueQuery(baseOptions: Apollo.QueryHookOptions<AutocorrectionValueQuery, AutocorrectionValueQueryVariables> & ({ variables: AutocorrectionValueQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AutocorrectionValueQuery, AutocorrectionValueQueryVariables>(AutocorrectionValueDocument, options);
      }
export function useAutocorrectionValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AutocorrectionValueQuery, AutocorrectionValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AutocorrectionValueQuery, AutocorrectionValueQueryVariables>(AutocorrectionValueDocument, options);
        }
// @ts-ignore
export function useAutocorrectionValueSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AutocorrectionValueQuery, AutocorrectionValueQueryVariables>): Apollo.UseSuspenseQueryResult<AutocorrectionValueQuery, AutocorrectionValueQueryVariables>;
export function useAutocorrectionValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocorrectionValueQuery, AutocorrectionValueQueryVariables>): Apollo.UseSuspenseQueryResult<AutocorrectionValueQuery | undefined, AutocorrectionValueQueryVariables>;
export function useAutocorrectionValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocorrectionValueQuery, AutocorrectionValueQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AutocorrectionValueQuery, AutocorrectionValueQueryVariables>(AutocorrectionValueDocument, options);
        }
export type AutocorrectionValueQueryHookResult = ReturnType<typeof useAutocorrectionValueQuery>;
export type AutocorrectionValueLazyQueryHookResult = ReturnType<typeof useAutocorrectionValueLazyQuery>;
export type AutocorrectionValueSuspenseQueryHookResult = ReturnType<typeof useAutocorrectionValueSuspenseQuery>;
export type AutocorrectionValueQueryResult = Apollo.QueryResult<AutocorrectionValueQuery, AutocorrectionValueQueryVariables>;
export const DeleteAutocorrectionDocument = gql`
    mutation DeleteAutocorrection($id: ID!) {
  deleteAutocorrection(id: $id) {
    id
  }
}
    `;
export type DeleteAutocorrectionMutationFn = Apollo.MutationFunction<DeleteAutocorrectionMutation, DeleteAutocorrectionMutationVariables>;

/**
 * __useDeleteAutocorrectionMutation__
 *
 * To run a mutation, you first call `useDeleteAutocorrectionMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteAutocorrectionMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteAutocorrectionMutation, { data, loading, error }] = useDeleteAutocorrectionMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteAutocorrectionMutation(baseOptions?: Apollo.MutationHookOptions<DeleteAutocorrectionMutation, DeleteAutocorrectionMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteAutocorrectionMutation, DeleteAutocorrectionMutationVariables>(DeleteAutocorrectionDocument, options);
      }
export type DeleteAutocorrectionMutationHookResult = ReturnType<typeof useDeleteAutocorrectionMutation>;
export type DeleteAutocorrectionMutationResult = Apollo.MutationResult<DeleteAutocorrectionMutation>;
export type DeleteAutocorrectionMutationOptions = Apollo.BaseMutationOptions<DeleteAutocorrectionMutation, DeleteAutocorrectionMutationVariables>;
export const SaveOrUpdateAutocorrectionDocument = gql`
    mutation SaveOrUpdateAutocorrection($id: ID!) {
  deleteAutocorrection(id: $id) {
    id
  }
}
    `;
export type SaveOrUpdateAutocorrectionMutationFn = Apollo.MutationFunction<SaveOrUpdateAutocorrectionMutation, SaveOrUpdateAutocorrectionMutationVariables>;

/**
 * __useSaveOrUpdateAutocorrectionMutation__
 *
 * To run a mutation, you first call `useSaveOrUpdateAutocorrectionMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSaveOrUpdateAutocorrectionMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [saveOrUpdateAutocorrectionMutation, { data, loading, error }] = useSaveOrUpdateAutocorrectionMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useSaveOrUpdateAutocorrectionMutation(baseOptions?: Apollo.MutationHookOptions<SaveOrUpdateAutocorrectionMutation, SaveOrUpdateAutocorrectionMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SaveOrUpdateAutocorrectionMutation, SaveOrUpdateAutocorrectionMutationVariables>(SaveOrUpdateAutocorrectionDocument, options);
      }
export type SaveOrUpdateAutocorrectionMutationHookResult = ReturnType<typeof useSaveOrUpdateAutocorrectionMutation>;
export type SaveOrUpdateAutocorrectionMutationResult = Apollo.MutationResult<SaveOrUpdateAutocorrectionMutation>;
export type SaveOrUpdateAutocorrectionMutationOptions = Apollo.BaseMutationOptions<SaveOrUpdateAutocorrectionMutation, SaveOrUpdateAutocorrectionMutationVariables>;
export const SaveAutocorrectionDocument = gql`
    mutation saveAutocorrection($id: ID, $autocorrectionDocTypeFieldId: BigInteger, $enableSearchWithCorrection: Boolean!, $maxEdit: Int!, $minWordLength: Int!, $prefixLength: Int!, $sort: SortType!, $suggestMode: SuggestMode!, $name: String!) {
  autocorrection(
    id: $id
    autocorrectionDTO: {autocorrectionDocTypeFieldId: $autocorrectionDocTypeFieldId, enableSearchWithCorrection: $enableSearchWithCorrection, maxEdit: $maxEdit, minWordLength: $minWordLength, prefixLength: $prefixLength, sort: $sort, suggestMode: $suggestMode, name: $name}
  ) {
    entity {
      id
    }
  }
}
    `;
export type SaveAutocorrectionMutationFn = Apollo.MutationFunction<SaveAutocorrectionMutation, SaveAutocorrectionMutationVariables>;

/**
 * __useSaveAutocorrectionMutation__
 *
 * To run a mutation, you first call `useSaveAutocorrectionMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSaveAutocorrectionMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [saveAutocorrectionMutation, { data, loading, error }] = useSaveAutocorrectionMutation({
 *   variables: {
 *      id: // value for 'id'
 *      autocorrectionDocTypeFieldId: // value for 'autocorrectionDocTypeFieldId'
 *      enableSearchWithCorrection: // value for 'enableSearchWithCorrection'
 *      maxEdit: // value for 'maxEdit'
 *      minWordLength: // value for 'minWordLength'
 *      prefixLength: // value for 'prefixLength'
 *      sort: // value for 'sort'
 *      suggestMode: // value for 'suggestMode'
 *      name: // value for 'name'
 *   },
 * });
 */
export function useSaveAutocorrectionMutation(baseOptions?: Apollo.MutationHookOptions<SaveAutocorrectionMutation, SaveAutocorrectionMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SaveAutocorrectionMutation, SaveAutocorrectionMutationVariables>(SaveAutocorrectionDocument, options);
      }
export type SaveAutocorrectionMutationHookResult = ReturnType<typeof useSaveAutocorrectionMutation>;
export type SaveAutocorrectionMutationResult = Apollo.MutationResult<SaveAutocorrectionMutation>;
export type SaveAutocorrectionMutationOptions = Apollo.BaseMutationOptions<SaveAutocorrectionMutation, SaveAutocorrectionMutationVariables>;
export const AllDocTypeFieldsDocument = gql`
    query AllDocTypeFields {
  docTypeFields {
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
 * __useAllDocTypeFieldsQuery__
 *
 * To run a query within a React component, call `useAllDocTypeFieldsQuery` and pass it any options that fit your needs.
 * When your component renders, `useAllDocTypeFieldsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAllDocTypeFieldsQuery({
 *   variables: {
 *   },
 * });
 */
export function useAllDocTypeFieldsQuery(baseOptions?: Apollo.QueryHookOptions<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>(AllDocTypeFieldsDocument, options);
      }
export function useAllDocTypeFieldsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>(AllDocTypeFieldsDocument, options);
        }
// @ts-ignore
export function useAllDocTypeFieldsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>): Apollo.UseSuspenseQueryResult<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>;
export function useAllDocTypeFieldsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>): Apollo.UseSuspenseQueryResult<AllDocTypeFieldsQuery | undefined, AllDocTypeFieldsQueryVariables>;
export function useAllDocTypeFieldsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>(AllDocTypeFieldsDocument, options);
        }
export type AllDocTypeFieldsQueryHookResult = ReturnType<typeof useAllDocTypeFieldsQuery>;
export type AllDocTypeFieldsLazyQueryHookResult = ReturnType<typeof useAllDocTypeFieldsLazyQuery>;
export type AllDocTypeFieldsSuspenseQueryHookResult = ReturnType<typeof useAllDocTypeFieldsSuspenseQuery>;
export type AllDocTypeFieldsQueryResult = Apollo.QueryResult<AllDocTypeFieldsQuery, AllDocTypeFieldsQueryVariables>;
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
// @ts-ignore
export function useCharfiltersSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<CharfiltersQuery, CharfiltersQueryVariables>): Apollo.UseSuspenseQueryResult<CharfiltersQuery, CharfiltersQueryVariables>;
export function useCharfiltersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<CharfiltersQuery, CharfiltersQueryVariables>): Apollo.UseSuspenseQueryResult<CharfiltersQuery | undefined, CharfiltersQueryVariables>;
export function useCharfiltersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<CharfiltersQuery, CharfiltersQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<CharfiltersQuery, CharfiltersQueryVariables>(CharfiltersDocument, options);
        }
export type CharfiltersQueryHookResult = ReturnType<typeof useCharfiltersQuery>;
export type CharfiltersLazyQueryHookResult = ReturnType<typeof useCharfiltersLazyQuery>;
export type CharfiltersSuspenseQueryHookResult = ReturnType<typeof useCharfiltersSuspenseQuery>;
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
export function useUnboundAnalyzersByCharFilterQuery(baseOptions: Apollo.QueryHookOptions<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables> & ({ variables: UnboundAnalyzersByCharFilterQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>(UnboundAnalyzersByCharFilterDocument, options);
      }
export function useUnboundAnalyzersByCharFilterLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>(UnboundAnalyzersByCharFilterDocument, options);
        }
// @ts-ignore
export function useUnboundAnalyzersByCharFilterSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>;
export function useUnboundAnalyzersByCharFilterSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAnalyzersByCharFilterQuery | undefined, UnboundAnalyzersByCharFilterQueryVariables>;
export function useUnboundAnalyzersByCharFilterSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundAnalyzersByCharFilterQuery, UnboundAnalyzersByCharFilterQueryVariables>(UnboundAnalyzersByCharFilterDocument, options);
        }
export type UnboundAnalyzersByCharFilterQueryHookResult = ReturnType<typeof useUnboundAnalyzersByCharFilterQuery>;
export type UnboundAnalyzersByCharFilterLazyQueryHookResult = ReturnType<typeof useUnboundAnalyzersByCharFilterLazyQuery>;
export type UnboundAnalyzersByCharFilterSuspenseQueryHookResult = ReturnType<typeof useUnboundAnalyzersByCharFilterSuspenseQuery>;
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
export function useCharFilterQuery(baseOptions: Apollo.QueryHookOptions<CharFilterQuery, CharFilterQueryVariables> & ({ variables: CharFilterQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<CharFilterQuery, CharFilterQueryVariables>(CharFilterDocument, options);
      }
export function useCharFilterLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<CharFilterQuery, CharFilterQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<CharFilterQuery, CharFilterQueryVariables>(CharFilterDocument, options);
        }
// @ts-ignore
export function useCharFilterSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<CharFilterQuery, CharFilterQueryVariables>): Apollo.UseSuspenseQueryResult<CharFilterQuery, CharFilterQueryVariables>;
export function useCharFilterSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<CharFilterQuery, CharFilterQueryVariables>): Apollo.UseSuspenseQueryResult<CharFilterQuery | undefined, CharFilterQueryVariables>;
export function useCharFilterSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<CharFilterQuery, CharFilterQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<CharFilterQuery, CharFilterQueryVariables>(CharFilterDocument, options);
        }
export type CharFilterQueryHookResult = ReturnType<typeof useCharFilterQuery>;
export type CharFilterLazyQueryHookResult = ReturnType<typeof useCharFilterLazyQuery>;
export type CharFilterSuspenseQueryHookResult = ReturnType<typeof useCharFilterSuspenseQuery>;
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
// @ts-ignore
export function useDataIndexInformationSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DataIndexInformationQuery, DataIndexInformationQueryVariables>): Apollo.UseSuspenseQueryResult<DataIndexInformationQuery, DataIndexInformationQueryVariables>;
export function useDataIndexInformationSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataIndexInformationQuery, DataIndexInformationQueryVariables>): Apollo.UseSuspenseQueryResult<DataIndexInformationQuery | undefined, DataIndexInformationQueryVariables>;
export function useDataIndexInformationSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataIndexInformationQuery, DataIndexInformationQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DataIndexInformationQuery, DataIndexInformationQueryVariables>(DataIndexInformationDocument, options);
        }
export type DataIndexInformationQueryHookResult = ReturnType<typeof useDataIndexInformationQuery>;
export type DataIndexInformationLazyQueryHookResult = ReturnType<typeof useDataIndexInformationLazyQuery>;
export type DataIndexInformationSuspenseQueryHookResult = ReturnType<typeof useDataIndexInformationSuspenseQuery>;
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
// @ts-ignore
export function useSchedulersFaiulureSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>): Apollo.UseSuspenseQueryResult<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>;
export function useSchedulersFaiulureSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>): Apollo.UseSuspenseQueryResult<SchedulersFaiulureQuery | undefined, SchedulersFaiulureQueryVariables>;
export function useSchedulersFaiulureSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SchedulersFaiulureQuery, SchedulersFaiulureQueryVariables>(SchedulersFaiulureDocument, options);
        }
export type SchedulersFaiulureQueryHookResult = ReturnType<typeof useSchedulersFaiulureQuery>;
export type SchedulersFaiulureLazyQueryHookResult = ReturnType<typeof useSchedulersFaiulureLazyQuery>;
export type SchedulersFaiulureSuspenseQueryHookResult = ReturnType<typeof useSchedulersFaiulureSuspenseQuery>;
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
// @ts-ignore
export function useDocumentTypeTemplatesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>;
export function useDocumentTypeTemplatesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeTemplatesQuery | undefined, DocumentTypeTemplatesQueryVariables>;
export function useDocumentTypeTemplatesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocumentTypeTemplatesQuery, DocumentTypeTemplatesQueryVariables>(DocumentTypeTemplatesDocument, options);
        }
export type DocumentTypeTemplatesQueryHookResult = ReturnType<typeof useDocumentTypeTemplatesQuery>;
export type DocumentTypeTemplatesLazyQueryHookResult = ReturnType<typeof useDocumentTypeTemplatesLazyQuery>;
export type DocumentTypeTemplatesSuspenseQueryHookResult = ReturnType<typeof useDocumentTypeTemplatesSuspenseQuery>;
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
    query DocumentTypes($searchText: String, $after: String) {
  docTypes(searchText: $searchText, first: 20, after: $after) {
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
 *      after: // value for 'after'
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
// @ts-ignore
export function useDocumentTypesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocumentTypesQuery, DocumentTypesQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypesQuery, DocumentTypesQueryVariables>;
export function useDocumentTypesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypesQuery, DocumentTypesQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypesQuery | undefined, DocumentTypesQueryVariables>;
export function useDocumentTypesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypesQuery, DocumentTypesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocumentTypesQuery, DocumentTypesQueryVariables>(DocumentTypesDocument, options);
        }
export type DocumentTypesQueryHookResult = ReturnType<typeof useDocumentTypesQuery>;
export type DocumentTypesLazyQueryHookResult = ReturnType<typeof useDocumentTypesLazyQuery>;
export type DocumentTypesSuspenseQueryHookResult = ReturnType<typeof useDocumentTypesSuspenseQuery>;
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
export function useDocumentTypeTemplateQuery(baseOptions: Apollo.QueryHookOptions<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables> & ({ variables: DocumentTypeTemplateQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>(DocumentTypeTemplateDocument, options);
      }
export function useDocumentTypeTemplateLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>(DocumentTypeTemplateDocument, options);
        }
// @ts-ignore
export function useDocumentTypeTemplateSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>;
export function useDocumentTypeTemplateSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeTemplateQuery | undefined, DocumentTypeTemplateQueryVariables>;
export function useDocumentTypeTemplateSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocumentTypeTemplateQuery, DocumentTypeTemplateQueryVariables>(DocumentTypeTemplateDocument, options);
        }
export type DocumentTypeTemplateQueryHookResult = ReturnType<typeof useDocumentTypeTemplateQuery>;
export type DocumentTypeTemplateLazyQueryHookResult = ReturnType<typeof useDocumentTypeTemplateLazyQuery>;
export type DocumentTypeTemplateSuspenseQueryHookResult = ReturnType<typeof useDocumentTypeTemplateSuspenseQuery>;
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
    offsetSource
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
    searchAnalyzer {
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
export function useDocumentTypeFieldQuery(baseOptions: Apollo.QueryHookOptions<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables> & ({ variables: DocumentTypeFieldQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>(DocumentTypeFieldDocument, options);
      }
export function useDocumentTypeFieldLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>(DocumentTypeFieldDocument, options);
        }
// @ts-ignore
export function useDocumentTypeFieldSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>;
export function useDocumentTypeFieldSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeFieldQuery | undefined, DocumentTypeFieldQueryVariables>;
export function useDocumentTypeFieldSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>(DocumentTypeFieldDocument, options);
        }
export type DocumentTypeFieldQueryHookResult = ReturnType<typeof useDocumentTypeFieldQuery>;
export type DocumentTypeFieldLazyQueryHookResult = ReturnType<typeof useDocumentTypeFieldLazyQuery>;
export type DocumentTypeFieldSuspenseQueryHookResult = ReturnType<typeof useDocumentTypeFieldSuspenseQuery>;
export type DocumentTypeFieldQueryResult = Apollo.QueryResult<DocumentTypeFieldQuery, DocumentTypeFieldQueryVariables>;
export const CreateOrUpdateDocumentTypeSubFieldsDocument = gql`
    mutation createOrUpdateDocumentTypeSubFields($parentDocTypeFieldId: ID!, $name: String!, $fieldName: String!, $jsonConfig: String, $searchable: Boolean!, $boost: Float, $fieldType: FieldType!, $sortable: Boolean!, $offsetSource: OffsetSourceType) {
  createSubField(
    parentDocTypeFieldId: $parentDocTypeFieldId
    docTypeFieldDTO: {name: $name, fieldName: $fieldName, jsonConfig: $jsonConfig, searchable: $searchable, boost: $boost, fieldType: $fieldType, sortable: $sortable, offsetSource: $offsetSource}
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
 *      offsetSource: // value for 'offsetSource'
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
export function useDocumentTypeQuery(baseOptions: Apollo.QueryHookOptions<DocumentTypeQuery, DocumentTypeQueryVariables> & ({ variables: DocumentTypeQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocumentTypeQuery, DocumentTypeQueryVariables>(DocumentTypeDocument, options);
      }
export function useDocumentTypeLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocumentTypeQuery, DocumentTypeQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocumentTypeQuery, DocumentTypeQueryVariables>(DocumentTypeDocument, options);
        }
// @ts-ignore
export function useDocumentTypeSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocumentTypeQuery, DocumentTypeQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeQuery, DocumentTypeQueryVariables>;
export function useDocumentTypeSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeQuery, DocumentTypeQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeQuery | undefined, DocumentTypeQueryVariables>;
export function useDocumentTypeSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeQuery, DocumentTypeQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocumentTypeQuery, DocumentTypeQueryVariables>(DocumentTypeDocument, options);
        }
export type DocumentTypeQueryHookResult = ReturnType<typeof useDocumentTypeQuery>;
export type DocumentTypeLazyQueryHookResult = ReturnType<typeof useDocumentTypeLazyQuery>;
export type DocumentTypeSuspenseQueryHookResult = ReturnType<typeof useDocumentTypeSuspenseQuery>;
export type DocumentTypeQueryResult = Apollo.QueryResult<DocumentTypeQuery, DocumentTypeQueryVariables>;
export const DocTypeFieldsByParentDocument = gql`
    query DocTypeFieldsByParent($searchText: String, $parentId: BigInteger!, $docTypeId: ID!, $first: Int, $after: String) {
  docTypeFieldsFromDocTypeByParent(
    parentId: $parentId
    searchText: $searchText
    first: $first
    after: $after
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
    pageInfo {
      hasNextPage
      endCursor
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
 *      first: // value for 'first'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useDocTypeFieldsByParentQuery(baseOptions: Apollo.QueryHookOptions<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables> & ({ variables: DocTypeFieldsByParentQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>(DocTypeFieldsByParentDocument, options);
      }
export function useDocTypeFieldsByParentLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>(DocTypeFieldsByParentDocument, options);
        }
// @ts-ignore
export function useDocTypeFieldsByParentSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>;
export function useDocTypeFieldsByParentSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldsByParentQuery | undefined, DocTypeFieldsByParentQueryVariables>;
export function useDocTypeFieldsByParentSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>(DocTypeFieldsByParentDocument, options);
        }
export type DocTypeFieldsByParentQueryHookResult = ReturnType<typeof useDocTypeFieldsByParentQuery>;
export type DocTypeFieldsByParentLazyQueryHookResult = ReturnType<typeof useDocTypeFieldsByParentLazyQuery>;
export type DocTypeFieldsByParentSuspenseQueryHookResult = ReturnType<typeof useDocTypeFieldsByParentSuspenseQuery>;
export type DocTypeFieldsByParentQueryResult = Apollo.QueryResult<DocTypeFieldsByParentQuery, DocTypeFieldsByParentQueryVariables>;
export const CreateOrUpdateDocumentTypeFieldDocument = gql`
    mutation CreateOrUpdateDocumentTypeField($documentTypeId: ID!, $documentTypeFieldId: ID, $docTypeFieldName: String, $name: String!, $fieldName: String!, $description: String, $fieldType: FieldType!, $boost: Float, $searchable: Boolean!, $exclude: Boolean, $jsonConfig: String, $sortable: Boolean!, $analyzerId: BigInteger, $searchAnalyzerId: BigInteger, $offsetSource: OffsetSourceType) {
  docTypeFieldWithAnalyzer(
    docTypeId: $documentTypeId
    docTypeFieldId: $documentTypeFieldId
    docTypeFieldName: $docTypeFieldName
    docTypeFieldWithAnalyzerDTO: {name: $name, description: $description, fieldType: $fieldType, boost: $boost, searchable: $searchable, exclude: $exclude, fieldName: $fieldName, jsonConfig: $jsonConfig, sortable: $sortable, analyzerId: $analyzerId, searchAnalyzerId: $searchAnalyzerId, offsetSource: $offsetSource}
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
 *      docTypeFieldName: // value for 'docTypeFieldName'
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
 *      searchAnalyzerId: // value for 'searchAnalyzerId'
 *      offsetSource: // value for 'offsetSource'
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
export const DeleteDocumentTypeFieldDocument = gql`
    mutation DeleteDocumentTypeField($docTypeFieldId: ID!, $docTypeFieldName: String) {
  deleteDocTypeField(
    docTypeFieldId: $docTypeFieldId
    docTypeFieldName: $docTypeFieldName
  ) {
    id
    name
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
 *      docTypeFieldId: // value for 'docTypeFieldId'
 *      docTypeFieldName: // value for 'docTypeFieldName'
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
export const DocTypeTemplateListDocument = gql`
    query docTypeTemplateList($searchText: String, $after: String, $first: Int) {
  docTypeTemplates(searchText: $searchText, first: $first, after: $after) {
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
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *      first: // value for 'first'
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
// @ts-ignore
export function useDocTypeTemplateListSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>;
export function useDocTypeTemplateListSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeTemplateListQuery | undefined, DocTypeTemplateListQueryVariables>;
export function useDocTypeTemplateListSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocTypeTemplateListQuery, DocTypeTemplateListQueryVariables>(DocTypeTemplateListDocument, options);
        }
export type DocTypeTemplateListQueryHookResult = ReturnType<typeof useDocTypeTemplateListQuery>;
export type DocTypeTemplateListLazyQueryHookResult = ReturnType<typeof useDocTypeTemplateListLazyQuery>;
export type DocTypeTemplateListSuspenseQueryHookResult = ReturnType<typeof useDocTypeTemplateListSuspenseQuery>;
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
// @ts-ignore
export function useUnboundAnalyzersSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>;
export function useUnboundAnalyzersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAnalyzersQuery | undefined, UnboundAnalyzersQueryVariables>;
export function useUnboundAnalyzersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>(UnboundAnalyzersDocument, options);
        }
export type UnboundAnalyzersQueryHookResult = ReturnType<typeof useUnboundAnalyzersQuery>;
export type UnboundAnalyzersLazyQueryHookResult = ReturnType<typeof useUnboundAnalyzersLazyQuery>;
export type UnboundAnalyzersSuspenseQueryHookResult = ReturnType<typeof useUnboundAnalyzersSuspenseQuery>;
export type UnboundAnalyzersQueryResult = Apollo.QueryResult<UnboundAnalyzersQuery, UnboundAnalyzersQueryVariables>;
export const AlignIndexesDocument = gql`
    mutation AlignIndexes($docTypeId: ID!, $closeIfNeeded: Boolean!) {
  alignIndexes(docTypeId: $docTypeId, closeIfNeeded: $closeIfNeeded) {
    dataIndexId
    indexName
    status
    reason
  }
}
    `;
export type AlignIndexesMutationFn = Apollo.MutationFunction<AlignIndexesMutation, AlignIndexesMutationVariables>;

/**
 * __useAlignIndexesMutation__
 *
 * To run a mutation, you first call `useAlignIndexesMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAlignIndexesMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [alignIndexesMutation, { data, loading, error }] = useAlignIndexesMutation({
 *   variables: {
 *      docTypeId: // value for 'docTypeId'
 *      closeIfNeeded: // value for 'closeIfNeeded'
 *   },
 * });
 */
export function useAlignIndexesMutation(baseOptions?: Apollo.MutationHookOptions<AlignIndexesMutation, AlignIndexesMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AlignIndexesMutation, AlignIndexesMutationVariables>(AlignIndexesDocument, options);
      }
export type AlignIndexesMutationHookResult = ReturnType<typeof useAlignIndexesMutation>;
export type AlignIndexesMutationResult = Apollo.MutationResult<AlignIndexesMutation>;
export type AlignIndexesMutationOptions = Apollo.BaseMutationOptions<AlignIndexesMutation, AlignIndexesMutationVariables>;
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
// @ts-ignore
export function useEmbeddingModelsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>): Apollo.UseSuspenseQueryResult<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>;
export function useEmbeddingModelsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>): Apollo.UseSuspenseQueryResult<EmbeddingModelsQuery | undefined, EmbeddingModelsQueryVariables>;
export function useEmbeddingModelsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<EmbeddingModelsQuery, EmbeddingModelsQueryVariables>(EmbeddingModelsDocument, options);
        }
export type EmbeddingModelsQueryHookResult = ReturnType<typeof useEmbeddingModelsQuery>;
export type EmbeddingModelsLazyQueryHookResult = ReturnType<typeof useEmbeddingModelsLazyQuery>;
export type EmbeddingModelsSuspenseQueryHookResult = ReturnType<typeof useEmbeddingModelsSuspenseQuery>;
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
    vectorDataType
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
export function useEmbeddingModelQuery(baseOptions: Apollo.QueryHookOptions<EmbeddingModelQuery, EmbeddingModelQueryVariables> & ({ variables: EmbeddingModelQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EmbeddingModelQuery, EmbeddingModelQueryVariables>(EmbeddingModelDocument, options);
      }
export function useEmbeddingModelLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EmbeddingModelQuery, EmbeddingModelQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EmbeddingModelQuery, EmbeddingModelQueryVariables>(EmbeddingModelDocument, options);
        }
// @ts-ignore
export function useEmbeddingModelSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<EmbeddingModelQuery, EmbeddingModelQueryVariables>): Apollo.UseSuspenseQueryResult<EmbeddingModelQuery, EmbeddingModelQueryVariables>;
export function useEmbeddingModelSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EmbeddingModelQuery, EmbeddingModelQueryVariables>): Apollo.UseSuspenseQueryResult<EmbeddingModelQuery | undefined, EmbeddingModelQueryVariables>;
export function useEmbeddingModelSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EmbeddingModelQuery, EmbeddingModelQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<EmbeddingModelQuery, EmbeddingModelQueryVariables>(EmbeddingModelDocument, options);
        }
export type EmbeddingModelQueryHookResult = ReturnType<typeof useEmbeddingModelQuery>;
export type EmbeddingModelLazyQueryHookResult = ReturnType<typeof useEmbeddingModelLazyQuery>;
export type EmbeddingModelSuspenseQueryHookResult = ReturnType<typeof useEmbeddingModelSuspenseQuery>;
export type EmbeddingModelQueryResult = Apollo.QueryResult<EmbeddingModelQuery, EmbeddingModelQueryVariables>;
export const CreateOrUpdateEmbeddingModelDocument = gql`
    mutation CreateOrUpdateEmbeddingModel($id: ID, $apiKey: String, $apiUrl: String!, $description: String!, $name: String!, $vectorSize: Int!, $vectorDataType: VectorDataType, $providerModel: ProviderModelDTOInput!, $jsonConfig: String) {
  embeddingModel(
    id: $id
    embeddingModelDTO: {name: $name, apiKey: $apiKey, apiUrl: $apiUrl, description: $description, vectorSize: $vectorSize, vectorDataType: $vectorDataType, providerModel: $providerModel, jsonConfig: $jsonConfig}
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
 *      vectorDataType: // value for 'vectorDataType'
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
export function useUnboundEnrichPipelinesQuery(baseOptions: Apollo.QueryHookOptions<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables> & ({ variables: UnboundEnrichPipelinesQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>(UnboundEnrichPipelinesDocument, options);
      }
export function useUnboundEnrichPipelinesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>(UnboundEnrichPipelinesDocument, options);
        }
// @ts-ignore
export function useUnboundEnrichPipelinesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>;
export function useUnboundEnrichPipelinesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundEnrichPipelinesQuery | undefined, UnboundEnrichPipelinesQueryVariables>;
export function useUnboundEnrichPipelinesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundEnrichPipelinesQuery, UnboundEnrichPipelinesQueryVariables>(UnboundEnrichPipelinesDocument, options);
        }
export type UnboundEnrichPipelinesQueryHookResult = ReturnType<typeof useUnboundEnrichPipelinesQuery>;
export type UnboundEnrichPipelinesLazyQueryHookResult = ReturnType<typeof useUnboundEnrichPipelinesLazyQuery>;
export type UnboundEnrichPipelinesSuspenseQueryHookResult = ReturnType<typeof useUnboundEnrichPipelinesSuspenseQuery>;
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
    resourceUri {
      baseUri
      path
    }
    script
    jsonConfig
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
export function useEnrichItemQuery(baseOptions: Apollo.QueryHookOptions<EnrichItemQuery, EnrichItemQueryVariables> & ({ variables: EnrichItemQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichItemQuery, EnrichItemQueryVariables>(EnrichItemDocument, options);
      }
export function useEnrichItemLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichItemQuery, EnrichItemQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichItemQuery, EnrichItemQueryVariables>(EnrichItemDocument, options);
        }
// @ts-ignore
export function useEnrichItemSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<EnrichItemQuery, EnrichItemQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichItemQuery, EnrichItemQueryVariables>;
export function useEnrichItemSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichItemQuery, EnrichItemQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichItemQuery | undefined, EnrichItemQueryVariables>;
export function useEnrichItemSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichItemQuery, EnrichItemQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<EnrichItemQuery, EnrichItemQueryVariables>(EnrichItemDocument, options);
        }
export type EnrichItemQueryHookResult = ReturnType<typeof useEnrichItemQuery>;
export type EnrichItemLazyQueryHookResult = ReturnType<typeof useEnrichItemLazyQuery>;
export type EnrichItemSuspenseQueryHookResult = ReturnType<typeof useEnrichItemSuspenseQuery>;
export type EnrichItemQueryResult = Apollo.QueryResult<EnrichItemQuery, EnrichItemQueryVariables>;
export const CreateOrUpdateEnrichItemDocument = gql`
    mutation CreateOrUpdateEnrichItem($id: ID, $name: String!, $description: String, $type: EnrichItemType!, $resourceUri: ResourceUriInput!, $script: String, $jsonConfig: String, $behaviorMergeType: BehaviorMergeType!, $jsonPath: String!, $behaviorOnError: BehaviorOnError!, $requestTimeout: BigInteger!) {
  enrichItem(
    id: $id
    enrichItemDTO: {name: $name, description: $description, type: $type, resourceUri: $resourceUri, script: $script, jsonConfig: $jsonConfig, behaviorMergeType: $behaviorMergeType, jsonPath: $jsonPath, behaviorOnError: $behaviorOnError, requestTimeout: $requestTimeout}
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
 *      resourceUri: // value for 'resourceUri'
 *      script: // value for 'script'
 *      jsonConfig: // value for 'jsonConfig'
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
export const HighlightsDocument = gql`
    query Highlights {
  highlights {
    id
    name
    description
    type
  }
}
    `;

/**
 * __useHighlightsQuery__
 *
 * To run a query within a React component, call `useHighlightsQuery` and pass it any options that fit your needs.
 * When your component renders, `useHighlightsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useHighlightsQuery({
 *   variables: {
 *   },
 * });
 */
export function useHighlightsQuery(baseOptions?: Apollo.QueryHookOptions<HighlightsQuery, HighlightsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<HighlightsQuery, HighlightsQueryVariables>(HighlightsDocument, options);
      }
export function useHighlightsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<HighlightsQuery, HighlightsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<HighlightsQuery, HighlightsQueryVariables>(HighlightsDocument, options);
        }
// @ts-ignore
export function useHighlightsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<HighlightsQuery, HighlightsQueryVariables>): Apollo.UseSuspenseQueryResult<HighlightsQuery, HighlightsQueryVariables>;
export function useHighlightsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<HighlightsQuery, HighlightsQueryVariables>): Apollo.UseSuspenseQueryResult<HighlightsQuery | undefined, HighlightsQueryVariables>;
export function useHighlightsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<HighlightsQuery, HighlightsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<HighlightsQuery, HighlightsQueryVariables>(HighlightsDocument, options);
        }
export type HighlightsQueryHookResult = ReturnType<typeof useHighlightsQuery>;
export type HighlightsLazyQueryHookResult = ReturnType<typeof useHighlightsLazyQuery>;
export type HighlightsSuspenseQueryHookResult = ReturnType<typeof useHighlightsSuspenseQuery>;
export type HighlightsQueryResult = Apollo.QueryResult<HighlightsQuery, HighlightsQueryVariables>;
export const HighlightDocument = gql`
    query Highlight($id: ID!) {
  highlight(id: $id) {
    id
    name
    description
    type
    boundaryScanner
    boundaryChars
    fragmenter
    fragmentSize
    numberOfFragments
    order
    fields {
      id
      name
    }
  }
}
    `;

/**
 * __useHighlightQuery__
 *
 * To run a query within a React component, call `useHighlightQuery` and pass it any options that fit your needs.
 * When your component renders, `useHighlightQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useHighlightQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useHighlightQuery(baseOptions: Apollo.QueryHookOptions<HighlightQuery, HighlightQueryVariables> & ({ variables: HighlightQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<HighlightQuery, HighlightQueryVariables>(HighlightDocument, options);
      }
export function useHighlightLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<HighlightQuery, HighlightQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<HighlightQuery, HighlightQueryVariables>(HighlightDocument, options);
        }
// @ts-ignore
export function useHighlightSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<HighlightQuery, HighlightQueryVariables>): Apollo.UseSuspenseQueryResult<HighlightQuery, HighlightQueryVariables>;
export function useHighlightSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<HighlightQuery, HighlightQueryVariables>): Apollo.UseSuspenseQueryResult<HighlightQuery | undefined, HighlightQueryVariables>;
export function useHighlightSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<HighlightQuery, HighlightQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<HighlightQuery, HighlightQueryVariables>(HighlightDocument, options);
        }
export type HighlightQueryHookResult = ReturnType<typeof useHighlightQuery>;
export type HighlightLazyQueryHookResult = ReturnType<typeof useHighlightLazyQuery>;
export type HighlightSuspenseQueryHookResult = ReturnType<typeof useHighlightSuspenseQuery>;
export type HighlightQueryResult = Apollo.QueryResult<HighlightQuery, HighlightQueryVariables>;
export const DocTypeFieldsByOffsetSourceDocument = gql`
    query DocTypeFieldsByOffsetSource($offsetSource: OffsetSourceType) {
  docTypeFieldsByOffsetSource(offsetSource: $offsetSource) {
    id
    name
  }
}
    `;

/**
 * __useDocTypeFieldsByOffsetSourceQuery__
 *
 * To run a query within a React component, call `useDocTypeFieldsByOffsetSourceQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocTypeFieldsByOffsetSourceQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocTypeFieldsByOffsetSourceQuery({
 *   variables: {
 *      offsetSource: // value for 'offsetSource'
 *   },
 * });
 */
export function useDocTypeFieldsByOffsetSourceQuery(baseOptions?: Apollo.QueryHookOptions<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>(DocTypeFieldsByOffsetSourceDocument, options);
      }
export function useDocTypeFieldsByOffsetSourceLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>(DocTypeFieldsByOffsetSourceDocument, options);
        }
// @ts-ignore
export function useDocTypeFieldsByOffsetSourceSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>;
export function useDocTypeFieldsByOffsetSourceSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldsByOffsetSourceQuery | undefined, DocTypeFieldsByOffsetSourceQueryVariables>;
export function useDocTypeFieldsByOffsetSourceSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>(DocTypeFieldsByOffsetSourceDocument, options);
        }
export type DocTypeFieldsByOffsetSourceQueryHookResult = ReturnType<typeof useDocTypeFieldsByOffsetSourceQuery>;
export type DocTypeFieldsByOffsetSourceLazyQueryHookResult = ReturnType<typeof useDocTypeFieldsByOffsetSourceLazyQuery>;
export type DocTypeFieldsByOffsetSourceSuspenseQueryHookResult = ReturnType<typeof useDocTypeFieldsByOffsetSourceSuspenseQuery>;
export type DocTypeFieldsByOffsetSourceQueryResult = Apollo.QueryResult<DocTypeFieldsByOffsetSourceQuery, DocTypeFieldsByOffsetSourceQueryVariables>;
export const CreateOrUpdateHighlightDocument = gql`
    mutation CreateOrUpdateHighlight($id: ID, $name: String!, $description: String, $type: HighlightType, $fieldIds: [BigInteger], $boundaryScanner: BoundaryScannerType, $boundaryChars: String, $fragmenter: FragmenterType, $fragmentSize: Int, $numberOfFragments: Int, $order: OrderType, $patch: Boolean) {
  highlight(
    id: $id
    patch: $patch
    highlightDTO: {name: $name, description: $description, type: $type, fieldIds: $fieldIds, boundaryScanner: $boundaryScanner, boundaryChars: $boundaryChars, fragmenter: $fragmenter, fragmentSize: $fragmentSize, numberOfFragments: $numberOfFragments, order: $order}
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
export type CreateOrUpdateHighlightMutationFn = Apollo.MutationFunction<CreateOrUpdateHighlightMutation, CreateOrUpdateHighlightMutationVariables>;

/**
 * __useCreateOrUpdateHighlightMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateHighlightMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateHighlightMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateHighlightMutation, { data, loading, error }] = useCreateOrUpdateHighlightMutation({
 *   variables: {
 *      id: // value for 'id'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      type: // value for 'type'
 *      fieldIds: // value for 'fieldIds'
 *      boundaryScanner: // value for 'boundaryScanner'
 *      boundaryChars: // value for 'boundaryChars'
 *      fragmenter: // value for 'fragmenter'
 *      fragmentSize: // value for 'fragmentSize'
 *      numberOfFragments: // value for 'numberOfFragments'
 *      order: // value for 'order'
 *      patch: // value for 'patch'
 *   },
 * });
 */
export function useCreateOrUpdateHighlightMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateHighlightMutation, CreateOrUpdateHighlightMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateHighlightMutation, CreateOrUpdateHighlightMutationVariables>(CreateOrUpdateHighlightDocument, options);
      }
export type CreateOrUpdateHighlightMutationHookResult = ReturnType<typeof useCreateOrUpdateHighlightMutation>;
export type CreateOrUpdateHighlightMutationResult = Apollo.MutationResult<CreateOrUpdateHighlightMutation>;
export type CreateOrUpdateHighlightMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateHighlightMutation, CreateOrUpdateHighlightMutationVariables>;
export const DeleteHighlightDocument = gql`
    mutation DeleteHighlight($id: ID!) {
  deleteHighlight(id: $id) {
    id
    name
  }
}
    `;
export type DeleteHighlightMutationFn = Apollo.MutationFunction<DeleteHighlightMutation, DeleteHighlightMutationVariables>;

/**
 * __useDeleteHighlightMutation__
 *
 * To run a mutation, you first call `useDeleteHighlightMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteHighlightMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteHighlightMutation, { data, loading, error }] = useDeleteHighlightMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteHighlightMutation(baseOptions?: Apollo.MutationHookOptions<DeleteHighlightMutation, DeleteHighlightMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteHighlightMutation, DeleteHighlightMutationVariables>(DeleteHighlightDocument, options);
      }
export type DeleteHighlightMutationHookResult = ReturnType<typeof useDeleteHighlightMutation>;
export type DeleteHighlightMutationResult = Apollo.MutationResult<DeleteHighlightMutation>;
export type DeleteHighlightMutationOptions = Apollo.BaseMutationOptions<DeleteHighlightMutation, DeleteHighlightMutationVariables>;
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
// @ts-ignore
export function useLargeLanguageModelsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>): Apollo.UseSuspenseQueryResult<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>;
export function useLargeLanguageModelsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>): Apollo.UseSuspenseQueryResult<LargeLanguageModelsQuery | undefined, LargeLanguageModelsQueryVariables>;
export function useLargeLanguageModelsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<LargeLanguageModelsQuery, LargeLanguageModelsQueryVariables>(LargeLanguageModelsDocument, options);
        }
export type LargeLanguageModelsQueryHookResult = ReturnType<typeof useLargeLanguageModelsQuery>;
export type LargeLanguageModelsLazyQueryHookResult = ReturnType<typeof useLargeLanguageModelsLazyQuery>;
export type LargeLanguageModelsSuspenseQueryHookResult = ReturnType<typeof useLargeLanguageModelsSuspenseQuery>;
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
export function useLargeLanguageModelQuery(baseOptions: Apollo.QueryHookOptions<LargeLanguageModelQuery, LargeLanguageModelQueryVariables> & ({ variables: LargeLanguageModelQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>(LargeLanguageModelDocument, options);
      }
export function useLargeLanguageModelLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>(LargeLanguageModelDocument, options);
        }
// @ts-ignore
export function useLargeLanguageModelSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>): Apollo.UseSuspenseQueryResult<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>;
export function useLargeLanguageModelSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>): Apollo.UseSuspenseQueryResult<LargeLanguageModelQuery | undefined, LargeLanguageModelQueryVariables>;
export function useLargeLanguageModelSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<LargeLanguageModelQuery, LargeLanguageModelQueryVariables>(LargeLanguageModelDocument, options);
        }
export type LargeLanguageModelQueryHookResult = ReturnType<typeof useLargeLanguageModelQuery>;
export type LargeLanguageModelLazyQueryHookResult = ReturnType<typeof useLargeLanguageModelLazyQuery>;
export type LargeLanguageModelSuspenseQueryHookResult = ReturnType<typeof useLargeLanguageModelSuspenseQuery>;
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
// @ts-ignore
export function useSchedulersSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SchedulersQuery, SchedulersQueryVariables>): Apollo.UseSuspenseQueryResult<SchedulersQuery, SchedulersQueryVariables>;
export function useSchedulersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SchedulersQuery, SchedulersQueryVariables>): Apollo.UseSuspenseQueryResult<SchedulersQuery | undefined, SchedulersQueryVariables>;
export function useSchedulersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SchedulersQuery, SchedulersQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SchedulersQuery, SchedulersQueryVariables>(SchedulersDocument, options);
        }
export type SchedulersQueryHookResult = ReturnType<typeof useSchedulersQuery>;
export type SchedulersLazyQueryHookResult = ReturnType<typeof useSchedulersLazyQuery>;
export type SchedulersSuspenseQueryHookResult = ReturnType<typeof useSchedulersSuspenseQuery>;
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
// @ts-ignore
export function useSchedulersErrorSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SchedulersErrorQuery, SchedulersErrorQueryVariables>): Apollo.UseSuspenseQueryResult<SchedulersErrorQuery, SchedulersErrorQueryVariables>;
export function useSchedulersErrorSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SchedulersErrorQuery, SchedulersErrorQueryVariables>): Apollo.UseSuspenseQueryResult<SchedulersErrorQuery | undefined, SchedulersErrorQueryVariables>;
export function useSchedulersErrorSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SchedulersErrorQuery, SchedulersErrorQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SchedulersErrorQuery, SchedulersErrorQueryVariables>(SchedulersErrorDocument, options);
        }
export type SchedulersErrorQueryHookResult = ReturnType<typeof useSchedulersErrorQuery>;
export type SchedulersErrorLazyQueryHookResult = ReturnType<typeof useSchedulersErrorLazyQuery>;
export type SchedulersErrorSuspenseQueryHookResult = ReturnType<typeof useSchedulersErrorSuspenseQuery>;
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
export function useSchedulerQuery(baseOptions: Apollo.QueryHookOptions<SchedulerQuery, SchedulerQueryVariables> & ({ variables: SchedulerQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SchedulerQuery, SchedulerQueryVariables>(SchedulerDocument, options);
      }
export function useSchedulerLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SchedulerQuery, SchedulerQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SchedulerQuery, SchedulerQueryVariables>(SchedulerDocument, options);
        }
// @ts-ignore
export function useSchedulerSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SchedulerQuery, SchedulerQueryVariables>): Apollo.UseSuspenseQueryResult<SchedulerQuery, SchedulerQueryVariables>;
export function useSchedulerSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SchedulerQuery, SchedulerQueryVariables>): Apollo.UseSuspenseQueryResult<SchedulerQuery | undefined, SchedulerQueryVariables>;
export function useSchedulerSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SchedulerQuery, SchedulerQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SchedulerQuery, SchedulerQueryVariables>(SchedulerDocument, options);
        }
export type SchedulerQueryHookResult = ReturnType<typeof useSchedulerQuery>;
export type SchedulerLazyQueryHookResult = ReturnType<typeof useSchedulerLazyQuery>;
export type SchedulerSuspenseQueryHookResult = ReturnType<typeof useSchedulerSuspenseQuery>;
export type SchedulerQueryResult = Apollo.QueryResult<SchedulerQuery, SchedulerQueryVariables>;
export const PluginDriverDocument = gql`
    query PluginDriver($id: ID!) {
  pluginDriver(id: $id) {
    id
    name
    description
    type
    resourceUri {
      baseUri
      path
    }
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
export function usePluginDriverQuery(baseOptions: Apollo.QueryHookOptions<PluginDriverQuery, PluginDriverQueryVariables> & ({ variables: PluginDriverQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PluginDriverQuery, PluginDriverQueryVariables>(PluginDriverDocument, options);
      }
export function usePluginDriverLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PluginDriverQuery, PluginDriverQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PluginDriverQuery, PluginDriverQueryVariables>(PluginDriverDocument, options);
        }
// @ts-ignore
export function usePluginDriverSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<PluginDriverQuery, PluginDriverQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriverQuery, PluginDriverQueryVariables>;
export function usePluginDriverSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriverQuery, PluginDriverQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriverQuery | undefined, PluginDriverQueryVariables>;
export function usePluginDriverSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriverQuery, PluginDriverQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<PluginDriverQuery, PluginDriverQueryVariables>(PluginDriverDocument, options);
        }
export type PluginDriverQueryHookResult = ReturnType<typeof usePluginDriverQuery>;
export type PluginDriverLazyQueryHookResult = ReturnType<typeof usePluginDriverLazyQuery>;
export type PluginDriverSuspenseQueryHookResult = ReturnType<typeof usePluginDriverSuspenseQuery>;
export type PluginDriverQueryResult = Apollo.QueryResult<PluginDriverQuery, PluginDriverQueryVariables>;
export const CreateOrUpdatePluginDriverMutationDocument = gql`
    mutation CreateOrUpdatePluginDriverMutation($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $resourceUri: ResourceUriInput, $provisioning: Provisioning!) {
  pluginDriver(
    id: $id
    pluginDriverDTO: {name: $name, description: $description, type: $type, resourceUri: $resourceUri, provisioning: $provisioning}
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
 *      resourceUri: // value for 'resourceUri'
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
// @ts-ignore
export function usePluginDriverByNameSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>;
export function usePluginDriverByNameSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriverByNameQuery | undefined, PluginDriverByNameQueryVariables>;
export function usePluginDriverByNameSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<PluginDriverByNameQuery, PluginDriverByNameQueryVariables>(PluginDriverByNameDocument, options);
        }
export type PluginDriverByNameQueryHookResult = ReturnType<typeof usePluginDriverByNameQuery>;
export type PluginDriverByNameLazyQueryHookResult = ReturnType<typeof usePluginDriverByNameLazyQuery>;
export type PluginDriverByNameSuspenseQueryHookResult = ReturnType<typeof usePluginDriverByNameSuspenseQuery>;
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
// @ts-ignore
export function usePluginDriversInfoQuerySuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>;
export function usePluginDriversInfoQuerySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriversInfoQueryQuery | undefined, PluginDriversInfoQueryQueryVariables>;
export function usePluginDriversInfoQuerySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<PluginDriversInfoQueryQuery, PluginDriversInfoQueryQueryVariables>(PluginDriversInfoQueryDocument, options);
        }
export type PluginDriversInfoQueryQueryHookResult = ReturnType<typeof usePluginDriversInfoQueryQuery>;
export type PluginDriversInfoQueryLazyQueryHookResult = ReturnType<typeof usePluginDriversInfoQueryLazyQuery>;
export type PluginDriversInfoQuerySuspenseQueryHookResult = ReturnType<typeof usePluginDriversInfoQuerySuspenseQuery>;
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
    mutation PluginDriverWithDocType($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $resourceUri: ResourceUriInput!, $provisioning: Provisioning!, $docTypeUserDTOSet: [DocTypeUserDTOInput]) {
  pluginDriverWithDocType(
    id: $id
    pluginWithDocTypeDTO: {name: $name, description: $description, type: $type, resourceUri: $resourceUri, provisioning: $provisioning, docTypeUserDTOSet: $docTypeUserDTOSet}
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
 *      resourceUri: // value for 'resourceUri'
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
export function usePluginDriverToDocumentTypeFieldsQuery(baseOptions: Apollo.QueryHookOptions<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables> & ({ variables: PluginDriverToDocumentTypeFieldsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>(PluginDriverToDocumentTypeFieldsDocument, options);
      }
export function usePluginDriverToDocumentTypeFieldsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>(PluginDriverToDocumentTypeFieldsDocument, options);
        }
// @ts-ignore
export function usePluginDriverToDocumentTypeFieldsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>;
export function usePluginDriverToDocumentTypeFieldsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriverToDocumentTypeFieldsQuery | undefined, PluginDriverToDocumentTypeFieldsQueryVariables>;
export function usePluginDriverToDocumentTypeFieldsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<PluginDriverToDocumentTypeFieldsQuery, PluginDriverToDocumentTypeFieldsQueryVariables>(PluginDriverToDocumentTypeFieldsDocument, options);
        }
export type PluginDriverToDocumentTypeFieldsQueryHookResult = ReturnType<typeof usePluginDriverToDocumentTypeFieldsQuery>;
export type PluginDriverToDocumentTypeFieldsLazyQueryHookResult = ReturnType<typeof usePluginDriverToDocumentTypeFieldsLazyQuery>;
export type PluginDriverToDocumentTypeFieldsSuspenseQueryHookResult = ReturnType<typeof usePluginDriverToDocumentTypeFieldsSuspenseQuery>;
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
// @ts-ignore
export function useDocumentTypeFieldsForPluginSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>;
export function useDocumentTypeFieldsForPluginSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>): Apollo.UseSuspenseQueryResult<DocumentTypeFieldsForPluginQuery | undefined, DocumentTypeFieldsForPluginQueryVariables>;
export function useDocumentTypeFieldsForPluginSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>(DocumentTypeFieldsForPluginDocument, options);
        }
export type DocumentTypeFieldsForPluginQueryHookResult = ReturnType<typeof useDocumentTypeFieldsForPluginQuery>;
export type DocumentTypeFieldsForPluginLazyQueryHookResult = ReturnType<typeof useDocumentTypeFieldsForPluginLazyQuery>;
export type DocumentTypeFieldsForPluginSuspenseQueryHookResult = ReturnType<typeof useDocumentTypeFieldsForPluginSuspenseQuery>;
export type DocumentTypeFieldsForPluginQueryResult = Apollo.QueryResult<DocumentTypeFieldsForPluginQuery, DocumentTypeFieldsForPluginQueryVariables>;
export const QueryAnalysesRulesDocument = gql`
    query QueryAnalysesRules($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  queryAnalysis(id: $parentId) {
    id
    rules(
      searchText: $searchText
      notEqual: $unassociated
      first: 1000
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
export function useQueryAnalysesRulesQuery(baseOptions: Apollo.QueryHookOptions<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables> & ({ variables: QueryAnalysesRulesQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>(QueryAnalysesRulesDocument, options);
      }
export function useQueryAnalysesRulesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>(QueryAnalysesRulesDocument, options);
        }
// @ts-ignore
export function useQueryAnalysesRulesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>;
export function useQueryAnalysesRulesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysesRulesQuery | undefined, QueryAnalysesRulesQueryVariables>;
export function useQueryAnalysesRulesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<QueryAnalysesRulesQuery, QueryAnalysesRulesQueryVariables>(QueryAnalysesRulesDocument, options);
        }
export type QueryAnalysesRulesQueryHookResult = ReturnType<typeof useQueryAnalysesRulesQuery>;
export type QueryAnalysesRulesLazyQueryHookResult = ReturnType<typeof useQueryAnalysesRulesLazyQuery>;
export type QueryAnalysesRulesSuspenseQueryHookResult = ReturnType<typeof useQueryAnalysesRulesSuspenseQuery>;
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
export function useQueryAnalysisQuery(baseOptions: Apollo.QueryHookOptions<QueryAnalysisQuery, QueryAnalysisQueryVariables> & ({ variables: QueryAnalysisQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysisQuery, QueryAnalysisQueryVariables>(QueryAnalysisDocument, options);
      }
export function useQueryAnalysisLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysisQuery, QueryAnalysisQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysisQuery, QueryAnalysisQueryVariables>(QueryAnalysisDocument, options);
        }
// @ts-ignore
export function useQueryAnalysisSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<QueryAnalysisQuery, QueryAnalysisQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysisQuery, QueryAnalysisQueryVariables>;
export function useQueryAnalysisSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysisQuery, QueryAnalysisQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysisQuery | undefined, QueryAnalysisQueryVariables>;
export function useQueryAnalysisSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysisQuery, QueryAnalysisQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<QueryAnalysisQuery, QueryAnalysisQueryVariables>(QueryAnalysisDocument, options);
        }
export type QueryAnalysisQueryHookResult = ReturnType<typeof useQueryAnalysisQuery>;
export type QueryAnalysisLazyQueryHookResult = ReturnType<typeof useQueryAnalysisLazyQuery>;
export type QueryAnalysisSuspenseQueryHookResult = ReturnType<typeof useQueryAnalysisSuspenseQuery>;
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
// @ts-ignore
export function useQueryAnalysesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<QueryAnalysesQuery, QueryAnalysesQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysesQuery, QueryAnalysesQueryVariables>;
export function useQueryAnalysesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysesQuery, QueryAnalysesQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysesQuery | undefined, QueryAnalysesQueryVariables>;
export function useQueryAnalysesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysesQuery, QueryAnalysesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<QueryAnalysesQuery, QueryAnalysesQueryVariables>(QueryAnalysesDocument, options);
        }
export type QueryAnalysesQueryHookResult = ReturnType<typeof useQueryAnalysesQuery>;
export type QueryAnalysesLazyQueryHookResult = ReturnType<typeof useQueryAnalysesLazyQuery>;
export type QueryAnalysesSuspenseQueryHookResult = ReturnType<typeof useQueryAnalysesSuspenseQuery>;
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
export function useQueryAnalysisAssociationsQuery(baseOptions: Apollo.QueryHookOptions<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables> & ({ variables: QueryAnalysisAssociationsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>(QueryAnalysisAssociationsDocument, options);
      }
export function useQueryAnalysisAssociationsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>(QueryAnalysisAssociationsDocument, options);
        }
// @ts-ignore
export function useQueryAnalysisAssociationsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>;
export function useQueryAnalysisAssociationsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysisAssociationsQuery | undefined, QueryAnalysisAssociationsQueryVariables>;
export function useQueryAnalysisAssociationsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<QueryAnalysisAssociationsQuery, QueryAnalysisAssociationsQueryVariables>(QueryAnalysisAssociationsDocument, options);
        }
export type QueryAnalysisAssociationsQueryHookResult = ReturnType<typeof useQueryAnalysisAssociationsQuery>;
export type QueryAnalysisAssociationsLazyQueryHookResult = ReturnType<typeof useQueryAnalysisAssociationsLazyQuery>;
export type QueryAnalysisAssociationsSuspenseQueryHookResult = ReturnType<typeof useQueryAnalysisAssociationsSuspenseQuery>;
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
// @ts-ignore
export function useRagConfigurationsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<RagConfigurationsQuery, RagConfigurationsQueryVariables>): Apollo.UseSuspenseQueryResult<RagConfigurationsQuery, RagConfigurationsQueryVariables>;
export function useRagConfigurationsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<RagConfigurationsQuery, RagConfigurationsQueryVariables>): Apollo.UseSuspenseQueryResult<RagConfigurationsQuery | undefined, RagConfigurationsQueryVariables>;
export function useRagConfigurationsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<RagConfigurationsQuery, RagConfigurationsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<RagConfigurationsQuery, RagConfigurationsQueryVariables>(RagConfigurationsDocument, options);
        }
export type RagConfigurationsQueryHookResult = ReturnType<typeof useRagConfigurationsQuery>;
export type RagConfigurationsLazyQueryHookResult = ReturnType<typeof useRagConfigurationsLazyQuery>;
export type RagConfigurationsSuspenseQueryHookResult = ReturnType<typeof useRagConfigurationsSuspenseQuery>;
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
    enableConversationTitle
    range {
      start
      end
    }
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
export function useRagConfigurationQuery(baseOptions: Apollo.QueryHookOptions<RagConfigurationQuery, RagConfigurationQueryVariables> & ({ variables: RagConfigurationQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<RagConfigurationQuery, RagConfigurationQueryVariables>(RagConfigurationDocument, options);
      }
export function useRagConfigurationLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<RagConfigurationQuery, RagConfigurationQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<RagConfigurationQuery, RagConfigurationQueryVariables>(RagConfigurationDocument, options);
        }
// @ts-ignore
export function useRagConfigurationSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<RagConfigurationQuery, RagConfigurationQueryVariables>): Apollo.UseSuspenseQueryResult<RagConfigurationQuery, RagConfigurationQueryVariables>;
export function useRagConfigurationSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<RagConfigurationQuery, RagConfigurationQueryVariables>): Apollo.UseSuspenseQueryResult<RagConfigurationQuery | undefined, RagConfigurationQueryVariables>;
export function useRagConfigurationSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<RagConfigurationQuery, RagConfigurationQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<RagConfigurationQuery, RagConfigurationQueryVariables>(RagConfigurationDocument, options);
        }
export type RagConfigurationQueryHookResult = ReturnType<typeof useRagConfigurationQuery>;
export type RagConfigurationLazyQueryHookResult = ReturnType<typeof useRagConfigurationLazyQuery>;
export type RagConfigurationSuspenseQueryHookResult = ReturnType<typeof useRagConfigurationSuspenseQuery>;
export type RagConfigurationQueryResult = Apollo.QueryResult<RagConfigurationQuery, RagConfigurationQueryVariables>;
export const CreateRagConfigDocument = gql`
    mutation createRAGConfig($name: String!, $description: String, $type: RAGType!, $reformulate: Boolean, $chunkWindow: Int, $rephrasePrompt: String, $prompt: String, $jsonConfig: String, $ragToolDescription: String, $promptNoRag: String, $enableConversationTitle: Boolean, $range: RangeDTOInput) {
  createRAGConfiguration(
    createRAGConfigurationDTO: {name: $name, description: $description, type: $type, reformulate: $reformulate, chunkWindow: $chunkWindow, rephrasePrompt: $rephrasePrompt, prompt: $prompt, jsonConfig: $jsonConfig, ragToolDescription: $ragToolDescription, promptNoRag: $promptNoRag, enableConversationTitle: $enableConversationTitle, range: $range}
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
 *      enableConversationTitle: // value for 'enableConversationTitle'
 *      range: // value for 'range'
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
    mutation updateRAGConfiguration($id: ID!, $name: String!, $description: String, $reformulate: Boolean, $chunkWindow: Int, $rephrasePrompt: String, $prompt: String, $jsonConfig: String, $ragToolDescription: String, $promptNoRag: String, $enableConversationTitle: Boolean, $range: RangeDTOInput, $patch: Boolean) {
  updateRAGConfiguration(
    id: $id
    patch: $patch
    ragConfigurationDTO: {name: $name, description: $description, reformulate: $reformulate, chunkWindow: $chunkWindow, rephrasePrompt: $rephrasePrompt, prompt: $prompt, jsonConfig: $jsonConfig, ragToolDescription: $ragToolDescription, promptNoRag: $promptNoRag, enableConversationTitle: $enableConversationTitle, range: $range}
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
 *      enableConversationTitle: // value for 'enableConversationTitle'
 *      range: // value for 'range'
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
export function useUnboundRagConfigurationsByBucketQuery(baseOptions: Apollo.QueryHookOptions<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables> & ({ variables: UnboundRagConfigurationsByBucketQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>(UnboundRagConfigurationsByBucketDocument, options);
      }
export function useUnboundRagConfigurationsByBucketLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>(UnboundRagConfigurationsByBucketDocument, options);
        }
// @ts-ignore
export function useUnboundRagConfigurationsByBucketSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>;
export function useUnboundRagConfigurationsByBucketSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundRagConfigurationsByBucketQuery | undefined, UnboundRagConfigurationsByBucketQueryVariables>;
export function useUnboundRagConfigurationsByBucketSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>(UnboundRagConfigurationsByBucketDocument, options);
        }
export type UnboundRagConfigurationsByBucketQueryHookResult = ReturnType<typeof useUnboundRagConfigurationsByBucketQuery>;
export type UnboundRagConfigurationsByBucketLazyQueryHookResult = ReturnType<typeof useUnboundRagConfigurationsByBucketLazyQuery>;
export type UnboundRagConfigurationsByBucketSuspenseQueryHookResult = ReturnType<typeof useUnboundRagConfigurationsByBucketSuspenseQuery>;
export type UnboundRagConfigurationsByBucketQueryResult = Apollo.QueryResult<UnboundRagConfigurationsByBucketQuery, UnboundRagConfigurationsByBucketQueryVariables>;
export const ResultAclConfigDocument = gql`
    query ResultAclConfig {
  resultAclConfig {
    id
    enabled
    superUserRoles
    visibleFields {
      id
      name
    }
  }
}
    `;

/**
 * __useResultAclConfigQuery__
 *
 * To run a query within a React component, call `useResultAclConfigQuery` and pass it any options that fit your needs.
 * When your component renders, `useResultAclConfigQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useResultAclConfigQuery({
 *   variables: {
 *   },
 * });
 */
export function useResultAclConfigQuery(baseOptions?: Apollo.QueryHookOptions<ResultAclConfigQuery, ResultAclConfigQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<ResultAclConfigQuery, ResultAclConfigQueryVariables>(ResultAclConfigDocument, options);
      }
export function useResultAclConfigLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<ResultAclConfigQuery, ResultAclConfigQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<ResultAclConfigQuery, ResultAclConfigQueryVariables>(ResultAclConfigDocument, options);
        }
// @ts-ignore
export function useResultAclConfigSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<ResultAclConfigQuery, ResultAclConfigQueryVariables>): Apollo.UseSuspenseQueryResult<ResultAclConfigQuery, ResultAclConfigQueryVariables>;
export function useResultAclConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<ResultAclConfigQuery, ResultAclConfigQueryVariables>): Apollo.UseSuspenseQueryResult<ResultAclConfigQuery | undefined, ResultAclConfigQueryVariables>;
export function useResultAclConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<ResultAclConfigQuery, ResultAclConfigQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<ResultAclConfigQuery, ResultAclConfigQueryVariables>(ResultAclConfigDocument, options);
        }
export type ResultAclConfigQueryHookResult = ReturnType<typeof useResultAclConfigQuery>;
export type ResultAclConfigLazyQueryHookResult = ReturnType<typeof useResultAclConfigLazyQuery>;
export type ResultAclConfigSuspenseQueryHookResult = ReturnType<typeof useResultAclConfigSuspenseQuery>;
export type ResultAclConfigQueryResult = Apollo.QueryResult<ResultAclConfigQuery, ResultAclConfigQueryVariables>;
export const UnboundDocTypeFieldByResultAclConfigDocument = gql`
    query UnboundDocTypeFieldByResultAclConfig {
  unboundDocTypeFieldByResultAclConfig {
    id
    name
  }
}
    `;

/**
 * __useUnboundDocTypeFieldByResultAclConfigQuery__
 *
 * To run a query within a React component, call `useUnboundDocTypeFieldByResultAclConfigQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundDocTypeFieldByResultAclConfigQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundDocTypeFieldByResultAclConfigQuery({
 *   variables: {
 *   },
 * });
 */
export function useUnboundDocTypeFieldByResultAclConfigQuery(baseOptions?: Apollo.QueryHookOptions<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>(UnboundDocTypeFieldByResultAclConfigDocument, options);
      }
export function useUnboundDocTypeFieldByResultAclConfigLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>(UnboundDocTypeFieldByResultAclConfigDocument, options);
        }
// @ts-ignore
export function useUnboundDocTypeFieldByResultAclConfigSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>;
export function useUnboundDocTypeFieldByResultAclConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundDocTypeFieldByResultAclConfigQuery | undefined, UnboundDocTypeFieldByResultAclConfigQueryVariables>;
export function useUnboundDocTypeFieldByResultAclConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>(UnboundDocTypeFieldByResultAclConfigDocument, options);
        }
export type UnboundDocTypeFieldByResultAclConfigQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldByResultAclConfigQuery>;
export type UnboundDocTypeFieldByResultAclConfigLazyQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldByResultAclConfigLazyQuery>;
export type UnboundDocTypeFieldByResultAclConfigSuspenseQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldByResultAclConfigSuspenseQuery>;
export type UnboundDocTypeFieldByResultAclConfigQueryResult = Apollo.QueryResult<UnboundDocTypeFieldByResultAclConfigQuery, UnboundDocTypeFieldByResultAclConfigQueryVariables>;
export const CreateOrUpdateResultAclConfigDocument = gql`
    mutation CreateOrUpdateResultAclConfig($resultAclConfigDTO: ResultAclConfigDTOInput!) {
  resultAclConfig(resultAclConfigDTO: $resultAclConfigDTO, patch: false) {
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
export type CreateOrUpdateResultAclConfigMutationFn = Apollo.MutationFunction<CreateOrUpdateResultAclConfigMutation, CreateOrUpdateResultAclConfigMutationVariables>;

/**
 * __useCreateOrUpdateResultAclConfigMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateResultAclConfigMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateResultAclConfigMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateResultAclConfigMutation, { data, loading, error }] = useCreateOrUpdateResultAclConfigMutation({
 *   variables: {
 *      resultAclConfigDTO: // value for 'resultAclConfigDTO'
 *   },
 * });
 */
export function useCreateOrUpdateResultAclConfigMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateResultAclConfigMutation, CreateOrUpdateResultAclConfigMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateResultAclConfigMutation, CreateOrUpdateResultAclConfigMutationVariables>(CreateOrUpdateResultAclConfigDocument, options);
      }
export type CreateOrUpdateResultAclConfigMutationHookResult = ReturnType<typeof useCreateOrUpdateResultAclConfigMutation>;
export type CreateOrUpdateResultAclConfigMutationResult = Apollo.MutationResult<CreateOrUpdateResultAclConfigMutation>;
export type CreateOrUpdateResultAclConfigMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateResultAclConfigMutation, CreateOrUpdateResultAclConfigMutationVariables>;
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
// @ts-ignore
export function useRulesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<RulesQuery, RulesQueryVariables>): Apollo.UseSuspenseQueryResult<RulesQuery, RulesQueryVariables>;
export function useRulesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<RulesQuery, RulesQueryVariables>): Apollo.UseSuspenseQueryResult<RulesQuery | undefined, RulesQueryVariables>;
export function useRulesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<RulesQuery, RulesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<RulesQuery, RulesQueryVariables>(RulesDocument, options);
        }
export type RulesQueryHookResult = ReturnType<typeof useRulesQuery>;
export type RulesLazyQueryHookResult = ReturnType<typeof useRulesLazyQuery>;
export type RulesSuspenseQueryHookResult = ReturnType<typeof useRulesSuspenseQuery>;
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
export function useRuleQuery(baseOptions: Apollo.QueryHookOptions<RuleQuery, RuleQueryVariables> & ({ variables: RuleQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<RuleQuery, RuleQueryVariables>(RuleDocument, options);
      }
export function useRuleLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<RuleQuery, RuleQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<RuleQuery, RuleQueryVariables>(RuleDocument, options);
        }
// @ts-ignore
export function useRuleSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<RuleQuery, RuleQueryVariables>): Apollo.UseSuspenseQueryResult<RuleQuery, RuleQueryVariables>;
export function useRuleSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<RuleQuery, RuleQueryVariables>): Apollo.UseSuspenseQueryResult<RuleQuery | undefined, RuleQueryVariables>;
export function useRuleSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<RuleQuery, RuleQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<RuleQuery, RuleQueryVariables>(RuleDocument, options);
        }
export type RuleQueryHookResult = ReturnType<typeof useRuleQuery>;
export type RuleLazyQueryHookResult = ReturnType<typeof useRuleLazyQuery>;
export type RuleSuspenseQueryHookResult = ReturnType<typeof useRuleSuspenseQuery>;
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
    maxSearchPageFrom
    maxSearchPageSize
    maxTextQueryLength
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
export function useSearchConfigQuery(baseOptions: Apollo.QueryHookOptions<SearchConfigQuery, SearchConfigQueryVariables> & ({ variables: SearchConfigQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SearchConfigQuery, SearchConfigQueryVariables>(SearchConfigDocument, options);
      }
export function useSearchConfigLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SearchConfigQuery, SearchConfigQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SearchConfigQuery, SearchConfigQueryVariables>(SearchConfigDocument, options);
        }
// @ts-ignore
export function useSearchConfigSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SearchConfigQuery, SearchConfigQueryVariables>): Apollo.UseSuspenseQueryResult<SearchConfigQuery, SearchConfigQueryVariables>;
export function useSearchConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SearchConfigQuery, SearchConfigQueryVariables>): Apollo.UseSuspenseQueryResult<SearchConfigQuery | undefined, SearchConfigQueryVariables>;
export function useSearchConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SearchConfigQuery, SearchConfigQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SearchConfigQuery, SearchConfigQueryVariables>(SearchConfigDocument, options);
        }
export type SearchConfigQueryHookResult = ReturnType<typeof useSearchConfigQuery>;
export type SearchConfigLazyQueryHookResult = ReturnType<typeof useSearchConfigLazyQuery>;
export type SearchConfigSuspenseQueryHookResult = ReturnType<typeof useSearchConfigSuspenseQuery>;
export type SearchConfigQueryResult = Apollo.QueryResult<SearchConfigQuery, SearchConfigQueryVariables>;
export const CreateOrUpdateSearchConfigDocument = gql`
    mutation CreateOrUpdateSearchConfig($id: ID, $name: String!, $description: String, $minScore: Float!, $minScoreSuggestions: Boolean!, $minScoreSearch: Boolean!, $maxSearchPageFrom: Int, $maxSearchPageSize: Int, $maxTextQueryLength: Int, $queryParsersConfig: [QueryParserConfigDTOInput]) {
  searchConfigWithQueryParsers(
    id: $id
    searchConfigWithQueryParsersDTO: {name: $name, description: $description, minScore: $minScore, minScoreSuggestions: $minScoreSuggestions, minScoreSearch: $minScoreSearch, maxSearchPageFrom: $maxSearchPageFrom, maxSearchPageSize: $maxSearchPageSize, maxTextQueryLength: $maxTextQueryLength, queryParsers: $queryParsersConfig}
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
 *      maxSearchPageFrom: // value for 'maxSearchPageFrom'
 *      maxSearchPageSize: // value for 'maxSearchPageSize'
 *      maxTextQueryLength: // value for 'maxTextQueryLength'
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
    query SearchConfigs($searchText: String, $first: Int, $after: String) {
  searchConfigs(searchText: $searchText, first: $first, after: $after) {
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
 *      first: // value for 'first'
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
// @ts-ignore
export function useSearchConfigsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SearchConfigsQuery, SearchConfigsQueryVariables>): Apollo.UseSuspenseQueryResult<SearchConfigsQuery, SearchConfigsQueryVariables>;
export function useSearchConfigsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SearchConfigsQuery, SearchConfigsQueryVariables>): Apollo.UseSuspenseQueryResult<SearchConfigsQuery | undefined, SearchConfigsQueryVariables>;
export function useSearchConfigsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SearchConfigsQuery, SearchConfigsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SearchConfigsQuery, SearchConfigsQueryVariables>(SearchConfigsDocument, options);
        }
export type SearchConfigsQueryHookResult = ReturnType<typeof useSearchConfigsQuery>;
export type SearchConfigsLazyQueryHookResult = ReturnType<typeof useSearchConfigsLazyQuery>;
export type SearchConfigsSuspenseQueryHookResult = ReturnType<typeof useSearchConfigsSuspenseQuery>;
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
// @ts-ignore
export function useQueryParserConfigSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<QueryParserConfigQuery, QueryParserConfigQueryVariables>): Apollo.UseSuspenseQueryResult<QueryParserConfigQuery, QueryParserConfigQueryVariables>;
export function useQueryParserConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryParserConfigQuery, QueryParserConfigQueryVariables>): Apollo.UseSuspenseQueryResult<QueryParserConfigQuery | undefined, QueryParserConfigQueryVariables>;
export function useQueryParserConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryParserConfigQuery, QueryParserConfigQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<QueryParserConfigQuery, QueryParserConfigQueryVariables>(QueryParserConfigDocument, options);
        }
export type QueryParserConfigQueryHookResult = ReturnType<typeof useQueryParserConfigQuery>;
export type QueryParserConfigLazyQueryHookResult = ReturnType<typeof useQueryParserConfigLazyQuery>;
export type QueryParserConfigSuspenseQueryHookResult = ReturnType<typeof useQueryParserConfigSuspenseQuery>;
export type QueryParserConfigQueryResult = Apollo.QueryResult<QueryParserConfigQuery, QueryParserConfigQueryVariables>;
export const SortingDocument = gql`
    query Sorting($id: ID!) {
  sorting(id: $id) {
    id
    name
    description
    priority
    defaultSort
    type
    docTypeField {
      id
      name
    }
  }
}
    `;

/**
 * __useSortingQuery__
 *
 * To run a query within a React component, call `useSortingQuery` and pass it any options that fit your needs.
 * When your component renders, `useSortingQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSortingQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useSortingQuery(baseOptions: Apollo.QueryHookOptions<SortingQuery, SortingQueryVariables> & ({ variables: SortingQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SortingQuery, SortingQueryVariables>(SortingDocument, options);
      }
export function useSortingLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SortingQuery, SortingQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SortingQuery, SortingQueryVariables>(SortingDocument, options);
        }
// @ts-ignore
export function useSortingSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SortingQuery, SortingQueryVariables>): Apollo.UseSuspenseQueryResult<SortingQuery, SortingQueryVariables>;
export function useSortingSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SortingQuery, SortingQueryVariables>): Apollo.UseSuspenseQueryResult<SortingQuery | undefined, SortingQueryVariables>;
export function useSortingSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SortingQuery, SortingQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SortingQuery, SortingQueryVariables>(SortingDocument, options);
        }
export type SortingQueryHookResult = ReturnType<typeof useSortingQuery>;
export type SortingLazyQueryHookResult = ReturnType<typeof useSortingLazyQuery>;
export type SortingSuspenseQueryHookResult = ReturnType<typeof useSortingSuspenseQuery>;
export type SortingQueryResult = Apollo.QueryResult<SortingQuery, SortingQueryVariables>;
export const CreateOrUpdateSortingDocument = gql`
    mutation CreateOrUpdateSorting($sortingId: ID, $name: String!, $description: String, $priority: Float!, $type: SortingType!, $defaultSort: Boolean!, $docTypeFieldId: BigInteger) {
  sortingWithDocTypeField(
    id: $sortingId
    sortingWithDocTypeFieldDTO: {name: $name, description: $description, priority: $priority, type: $type, defaultSort: $defaultSort, docTypeFieldId: $docTypeFieldId}
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
export type CreateOrUpdateSortingMutationFn = Apollo.MutationFunction<CreateOrUpdateSortingMutation, CreateOrUpdateSortingMutationVariables>;

/**
 * __useCreateOrUpdateSortingMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateSortingMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateSortingMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateSortingMutation, { data, loading, error }] = useCreateOrUpdateSortingMutation({
 *   variables: {
 *      sortingId: // value for 'sortingId'
 *      name: // value for 'name'
 *      description: // value for 'description'
 *      priority: // value for 'priority'
 *      type: // value for 'type'
 *      defaultSort: // value for 'defaultSort'
 *      docTypeFieldId: // value for 'docTypeFieldId'
 *   },
 * });
 */
export function useCreateOrUpdateSortingMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateSortingMutation, CreateOrUpdateSortingMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateSortingMutation, CreateOrUpdateSortingMutationVariables>(CreateOrUpdateSortingDocument, options);
      }
export type CreateOrUpdateSortingMutationHookResult = ReturnType<typeof useCreateOrUpdateSortingMutation>;
export type CreateOrUpdateSortingMutationResult = Apollo.MutationResult<CreateOrUpdateSortingMutation>;
export type CreateOrUpdateSortingMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateSortingMutation, CreateOrUpdateSortingMutationVariables>;
export const SortingsDocument = gql`
    query Sortings($searchText: String, $after: String) {
  totalSortings(searchText: $searchText, first: 20, after: $after) {
    edges {
      node {
        id
        name
        description
        priority
        defaultSort
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
 * __useSortingsQuery__
 *
 * To run a query within a React component, call `useSortingsQuery` and pass it any options that fit your needs.
 * When your component renders, `useSortingsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useSortingsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      after: // value for 'after'
 *   },
 * });
 */
export function useSortingsQuery(baseOptions?: Apollo.QueryHookOptions<SortingsQuery, SortingsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SortingsQuery, SortingsQueryVariables>(SortingsDocument, options);
      }
export function useSortingsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SortingsQuery, SortingsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SortingsQuery, SortingsQueryVariables>(SortingsDocument, options);
        }
// @ts-ignore
export function useSortingsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SortingsQuery, SortingsQueryVariables>): Apollo.UseSuspenseQueryResult<SortingsQuery, SortingsQueryVariables>;
export function useSortingsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SortingsQuery, SortingsQueryVariables>): Apollo.UseSuspenseQueryResult<SortingsQuery | undefined, SortingsQueryVariables>;
export function useSortingsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SortingsQuery, SortingsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SortingsQuery, SortingsQueryVariables>(SortingsDocument, options);
        }
export type SortingsQueryHookResult = ReturnType<typeof useSortingsQuery>;
export type SortingsLazyQueryHookResult = ReturnType<typeof useSortingsLazyQuery>;
export type SortingsSuspenseQueryHookResult = ReturnType<typeof useSortingsSuspenseQuery>;
export type SortingsQueryResult = Apollo.QueryResult<SortingsQuery, SortingsQueryVariables>;
export const DeleteSortingDocument = gql`
    mutation DeleteSorting($id: ID!) {
  deleteSorting(sortingId: $id) {
    id
    name
  }
}
    `;
export type DeleteSortingMutationFn = Apollo.MutationFunction<DeleteSortingMutation, DeleteSortingMutationVariables>;

/**
 * __useDeleteSortingMutation__
 *
 * To run a mutation, you first call `useDeleteSortingMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteSortingMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteSortingMutation, { data, loading, error }] = useDeleteSortingMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDeleteSortingMutation(baseOptions?: Apollo.MutationHookOptions<DeleteSortingMutation, DeleteSortingMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteSortingMutation, DeleteSortingMutationVariables>(DeleteSortingDocument, options);
      }
export type DeleteSortingMutationHookResult = ReturnType<typeof useDeleteSortingMutation>;
export type DeleteSortingMutationResult = Apollo.MutationResult<DeleteSortingMutation>;
export type DeleteSortingMutationOptions = Apollo.BaseMutationOptions<DeleteSortingMutation, DeleteSortingMutationVariables>;
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
export function useUnboundBucketsBySuggestionCategoryQuery(baseOptions: Apollo.QueryHookOptions<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables> & ({ variables: UnboundBucketsBySuggestionCategoryQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>(UnboundBucketsBySuggestionCategoryDocument, options);
      }
export function useUnboundBucketsBySuggestionCategoryLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>(UnboundBucketsBySuggestionCategoryDocument, options);
        }
// @ts-ignore
export function useUnboundBucketsBySuggestionCategorySuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>;
export function useUnboundBucketsBySuggestionCategorySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundBucketsBySuggestionCategoryQuery | undefined, UnboundBucketsBySuggestionCategoryQueryVariables>;
export function useUnboundBucketsBySuggestionCategorySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundBucketsBySuggestionCategoryQuery, UnboundBucketsBySuggestionCategoryQueryVariables>(UnboundBucketsBySuggestionCategoryDocument, options);
        }
export type UnboundBucketsBySuggestionCategoryQueryHookResult = ReturnType<typeof useUnboundBucketsBySuggestionCategoryQuery>;
export type UnboundBucketsBySuggestionCategoryLazyQueryHookResult = ReturnType<typeof useUnboundBucketsBySuggestionCategoryLazyQuery>;
export type UnboundBucketsBySuggestionCategorySuspenseQueryHookResult = ReturnType<typeof useUnboundBucketsBySuggestionCategorySuspenseQuery>;
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
// @ts-ignore
export function useSuggestionCategoriesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>): Apollo.UseSuspenseQueryResult<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>;
export function useSuggestionCategoriesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>): Apollo.UseSuspenseQueryResult<SuggestionCategoriesQuery | undefined, SuggestionCategoriesQueryVariables>;
export function useSuggestionCategoriesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SuggestionCategoriesQuery, SuggestionCategoriesQueryVariables>(SuggestionCategoriesDocument, options);
        }
export type SuggestionCategoriesQueryHookResult = ReturnType<typeof useSuggestionCategoriesQuery>;
export type SuggestionCategoriesLazyQueryHookResult = ReturnType<typeof useSuggestionCategoriesLazyQuery>;
export type SuggestionCategoriesSuspenseQueryHookResult = ReturnType<typeof useSuggestionCategoriesSuspenseQuery>;
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
        first: 1000
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
export function useSuggestionCategoryDocumentTypeFieldsQuery(baseOptions: Apollo.QueryHookOptions<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables> & ({ variables: SuggestionCategoryDocumentTypeFieldsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>(SuggestionCategoryDocumentTypeFieldsDocument, options);
      }
export function useSuggestionCategoryDocumentTypeFieldsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>(SuggestionCategoryDocumentTypeFieldsDocument, options);
        }
// @ts-ignore
export function useSuggestionCategoryDocumentTypeFieldsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>): Apollo.UseSuspenseQueryResult<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>;
export function useSuggestionCategoryDocumentTypeFieldsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>): Apollo.UseSuspenseQueryResult<SuggestionCategoryDocumentTypeFieldsQuery | undefined, SuggestionCategoryDocumentTypeFieldsQueryVariables>;
export function useSuggestionCategoryDocumentTypeFieldsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SuggestionCategoryDocumentTypeFieldsQuery, SuggestionCategoryDocumentTypeFieldsQueryVariables>(SuggestionCategoryDocumentTypeFieldsDocument, options);
        }
export type SuggestionCategoryDocumentTypeFieldsQueryHookResult = ReturnType<typeof useSuggestionCategoryDocumentTypeFieldsQuery>;
export type SuggestionCategoryDocumentTypeFieldsLazyQueryHookResult = ReturnType<typeof useSuggestionCategoryDocumentTypeFieldsLazyQuery>;
export type SuggestionCategoryDocumentTypeFieldsSuspenseQueryHookResult = ReturnType<typeof useSuggestionCategoryDocumentTypeFieldsSuspenseQuery>;
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
export function useSuggestionCategoryQuery(baseOptions: Apollo.QueryHookOptions<SuggestionCategoryQuery, SuggestionCategoryQueryVariables> & ({ variables: SuggestionCategoryQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>(SuggestionCategoryDocument, options);
      }
export function useSuggestionCategoryLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>(SuggestionCategoryDocument, options);
        }
// @ts-ignore
export function useSuggestionCategorySuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>): Apollo.UseSuspenseQueryResult<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>;
export function useSuggestionCategorySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>): Apollo.UseSuspenseQueryResult<SuggestionCategoryQuery | undefined, SuggestionCategoryQueryVariables>;
export function useSuggestionCategorySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>(SuggestionCategoryDocument, options);
        }
export type SuggestionCategoryQueryHookResult = ReturnType<typeof useSuggestionCategoryQuery>;
export type SuggestionCategoryLazyQueryHookResult = ReturnType<typeof useSuggestionCategoryLazyQuery>;
export type SuggestionCategorySuspenseQueryHookResult = ReturnType<typeof useSuggestionCategorySuspenseQuery>;
export type SuggestionCategoryQueryResult = Apollo.QueryResult<SuggestionCategoryQuery, SuggestionCategoryQueryVariables>;
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
export function useUnboundDocTypeFieldsBySuggestionCategoryQuery(baseOptions: Apollo.QueryHookOptions<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables> & ({ variables: UnboundDocTypeFieldsBySuggestionCategoryQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>(UnboundDocTypeFieldsBySuggestionCategoryDocument, options);
      }
export function useUnboundDocTypeFieldsBySuggestionCategoryLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>(UnboundDocTypeFieldsBySuggestionCategoryDocument, options);
        }
// @ts-ignore
export function useUnboundDocTypeFieldsBySuggestionCategorySuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>;
export function useUnboundDocTypeFieldsBySuggestionCategorySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundDocTypeFieldsBySuggestionCategoryQuery | undefined, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>;
export function useUnboundDocTypeFieldsBySuggestionCategorySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>(UnboundDocTypeFieldsBySuggestionCategoryDocument, options);
        }
export type UnboundDocTypeFieldsBySuggestionCategoryQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldsBySuggestionCategoryQuery>;
export type UnboundDocTypeFieldsBySuggestionCategoryLazyQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldsBySuggestionCategoryLazyQuery>;
export type UnboundDocTypeFieldsBySuggestionCategorySuspenseQueryHookResult = ReturnType<typeof useUnboundDocTypeFieldsBySuggestionCategorySuspenseQuery>;
export type UnboundDocTypeFieldsBySuggestionCategoryQueryResult = Apollo.QueryResult<UnboundDocTypeFieldsBySuggestionCategoryQuery, UnboundDocTypeFieldsBySuggestionCategoryQueryVariables>;
export const DocTypeFieldsDocument = gql`
    query DocTypeFields($searchText: String, $first: Int = 20, $after: String) {
  docTypeFields(searchText: $searchText, first: $first, after: $after) {
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
 *      searchText: // value for 'searchText'
 *      first: // value for 'first'
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
// @ts-ignore
export function useDocTypeFieldsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>;
export function useDocTypeFieldsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldsQuery | undefined, DocTypeFieldsQueryVariables>;
export function useDocTypeFieldsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>(DocTypeFieldsDocument, options);
        }
export type DocTypeFieldsQueryHookResult = ReturnType<typeof useDocTypeFieldsQuery>;
export type DocTypeFieldsLazyQueryHookResult = ReturnType<typeof useDocTypeFieldsLazyQuery>;
export type DocTypeFieldsSuspenseQueryHookResult = ReturnType<typeof useDocTypeFieldsSuspenseQuery>;
export type DocTypeFieldsQueryResult = Apollo.QueryResult<DocTypeFieldsQuery, DocTypeFieldsQueryVariables>;
export const DocTypeFieldsByTypeDocument = gql`
    query DocTypeFieldsByType($fieldType: FieldType) {
  docTypeFieldsByType(fieldType: $fieldType) {
    id
    name
  }
}
    `;

/**
 * __useDocTypeFieldsByTypeQuery__
 *
 * To run a query within a React component, call `useDocTypeFieldsByTypeQuery` and pass it any options that fit your needs.
 * When your component renders, `useDocTypeFieldsByTypeQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDocTypeFieldsByTypeQuery({
 *   variables: {
 *      fieldType: // value for 'fieldType'
 *   },
 * });
 */
export function useDocTypeFieldsByTypeQuery(baseOptions?: Apollo.QueryHookOptions<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>(DocTypeFieldsByTypeDocument, options);
      }
export function useDocTypeFieldsByTypeLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>(DocTypeFieldsByTypeDocument, options);
        }
// @ts-ignore
export function useDocTypeFieldsByTypeSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>;
export function useDocTypeFieldsByTypeSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldsByTypeQuery | undefined, DocTypeFieldsByTypeQueryVariables>;
export function useDocTypeFieldsByTypeSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>(DocTypeFieldsByTypeDocument, options);
        }
export type DocTypeFieldsByTypeQueryHookResult = ReturnType<typeof useDocTypeFieldsByTypeQuery>;
export type DocTypeFieldsByTypeLazyQueryHookResult = ReturnType<typeof useDocTypeFieldsByTypeLazyQuery>;
export type DocTypeFieldsByTypeSuspenseQueryHookResult = ReturnType<typeof useDocTypeFieldsByTypeSuspenseQuery>;
export type DocTypeFieldsByTypeQueryResult = Apollo.QueryResult<DocTypeFieldsByTypeQuery, DocTypeFieldsByTypeQueryVariables>;
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
// @ts-ignore
export function useTabsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TabsQuery, TabsQueryVariables>): Apollo.UseSuspenseQueryResult<TabsQuery, TabsQueryVariables>;
export function useTabsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabsQuery, TabsQueryVariables>): Apollo.UseSuspenseQueryResult<TabsQuery | undefined, TabsQueryVariables>;
export function useTabsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabsQuery, TabsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TabsQuery, TabsQueryVariables>(TabsDocument, options);
        }
export type TabsQueryHookResult = ReturnType<typeof useTabsQuery>;
export type TabsLazyQueryHookResult = ReturnType<typeof useTabsLazyQuery>;
export type TabsSuspenseQueryHookResult = ReturnType<typeof useTabsSuspenseQuery>;
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
export function useUnboundBucketsByTabQuery(baseOptions: Apollo.QueryHookOptions<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables> & ({ variables: UnboundBucketsByTabQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>(UnboundBucketsByTabDocument, options);
      }
export function useUnboundBucketsByTabLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>(UnboundBucketsByTabDocument, options);
        }
// @ts-ignore
export function useUnboundBucketsByTabSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>;
export function useUnboundBucketsByTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundBucketsByTabQuery | undefined, UnboundBucketsByTabQueryVariables>;
export function useUnboundBucketsByTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>(UnboundBucketsByTabDocument, options);
        }
export type UnboundBucketsByTabQueryHookResult = ReturnType<typeof useUnboundBucketsByTabQuery>;
export type UnboundBucketsByTabLazyQueryHookResult = ReturnType<typeof useUnboundBucketsByTabLazyQuery>;
export type UnboundBucketsByTabSuspenseQueryHookResult = ReturnType<typeof useUnboundBucketsByTabSuspenseQuery>;
export type UnboundBucketsByTabQueryResult = Apollo.QueryResult<UnboundBucketsByTabQuery, UnboundBucketsByTabQueryVariables>;
export const AddTabTranslationDocument = gql`
    mutation AddTabTranslation($tabId: ID!, $language: String!, $key: String, $value: String!) {
  addTabTranslation(tabId: $tabId, language: $language, key: $key, value: $value) {
    left
    right
  }
}
    `;
export type AddTabTranslationMutationFn = Apollo.MutationFunction<AddTabTranslationMutation, AddTabTranslationMutationVariables>;

/**
 * __useAddTabTranslationMutation__
 *
 * To run a mutation, you first call `useAddTabTranslationMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddTabTranslationMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addTabTranslationMutation, { data, loading, error }] = useAddTabTranslationMutation({
 *   variables: {
 *      tabId: // value for 'tabId'
 *      language: // value for 'language'
 *      key: // value for 'key'
 *      value: // value for 'value'
 *   },
 * });
 */
export function useAddTabTranslationMutation(baseOptions?: Apollo.MutationHookOptions<AddTabTranslationMutation, AddTabTranslationMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddTabTranslationMutation, AddTabTranslationMutationVariables>(AddTabTranslationDocument, options);
      }
export type AddTabTranslationMutationHookResult = ReturnType<typeof useAddTabTranslationMutation>;
export type AddTabTranslationMutationResult = Apollo.MutationResult<AddTabTranslationMutation>;
export type AddTabTranslationMutationOptions = Apollo.BaseMutationOptions<AddTabTranslationMutation, AddTabTranslationMutationVariables>;
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
    sortings(notEqual: $unasociated) {
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
export function useTabQuery(baseOptions: Apollo.QueryHookOptions<TabQuery, TabQueryVariables> & ({ variables: TabQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TabQuery, TabQueryVariables>(TabDocument, options);
      }
export function useTabLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TabQuery, TabQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TabQuery, TabQueryVariables>(TabDocument, options);
        }
// @ts-ignore
export function useTabSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TabQuery, TabQueryVariables>): Apollo.UseSuspenseQueryResult<TabQuery, TabQueryVariables>;
export function useTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabQuery, TabQueryVariables>): Apollo.UseSuspenseQueryResult<TabQuery | undefined, TabQueryVariables>;
export function useTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabQuery, TabQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TabQuery, TabQueryVariables>(TabDocument, options);
        }
export type TabQueryHookResult = ReturnType<typeof useTabQuery>;
export type TabLazyQueryHookResult = ReturnType<typeof useTabLazyQuery>;
export type TabSuspenseQueryHookResult = ReturnType<typeof useTabSuspenseQuery>;
export type TabQueryResult = Apollo.QueryResult<TabQuery, TabQueryVariables>;
export const CreateOrUpdateTabDocument = gql`
    mutation CreateOrUpdateTab($id: ID, $name: String!, $description: String, $priority: Int!, $tokenTabIds: [BigInteger], $sortingIds: [BigInteger]) {
  tabWithTokenTabs(
    id: $id
    tabWithTokenTabsDTO: {name: $name, description: $description, priority: $priority, tokenTabIds: $tokenTabIds, sortingIds: $sortingIds}
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
 *      sortingIds: // value for 'sortingIds'
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
      first: 1000
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
export function useTabTokenTabsQuery(baseOptions: Apollo.QueryHookOptions<TabTokenTabsQuery, TabTokenTabsQueryVariables> & ({ variables: TabTokenTabsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TabTokenTabsQuery, TabTokenTabsQueryVariables>(TabTokenTabsDocument, options);
      }
export function useTabTokenTabsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TabTokenTabsQuery, TabTokenTabsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TabTokenTabsQuery, TabTokenTabsQueryVariables>(TabTokenTabsDocument, options);
        }
// @ts-ignore
export function useTabTokenTabsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TabTokenTabsQuery, TabTokenTabsQueryVariables>): Apollo.UseSuspenseQueryResult<TabTokenTabsQuery, TabTokenTabsQueryVariables>;
export function useTabTokenTabsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabTokenTabsQuery, TabTokenTabsQueryVariables>): Apollo.UseSuspenseQueryResult<TabTokenTabsQuery | undefined, TabTokenTabsQueryVariables>;
export function useTabTokenTabsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabTokenTabsQuery, TabTokenTabsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TabTokenTabsQuery, TabTokenTabsQueryVariables>(TabTokenTabsDocument, options);
        }
export type TabTokenTabsQueryHookResult = ReturnType<typeof useTabTokenTabsQuery>;
export type TabTokenTabsLazyQueryHookResult = ReturnType<typeof useTabTokenTabsLazyQuery>;
export type TabTokenTabsSuspenseQueryHookResult = ReturnType<typeof useTabTokenTabsSuspenseQuery>;
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
    query TabTokens($searchText: String, $after: String) {
  totalTokenTabs(searchText: $searchText, first: 20, after: $after) {
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
 *      after: // value for 'after'
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
// @ts-ignore
export function useTabTokensSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TabTokensQuery, TabTokensQueryVariables>): Apollo.UseSuspenseQueryResult<TabTokensQuery, TabTokensQueryVariables>;
export function useTabTokensSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabTokensQuery, TabTokensQueryVariables>): Apollo.UseSuspenseQueryResult<TabTokensQuery | undefined, TabTokensQueryVariables>;
export function useTabTokensSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabTokensQuery, TabTokensQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TabTokensQuery, TabTokensQueryVariables>(TabTokensDocument, options);
        }
export type TabTokensQueryHookResult = ReturnType<typeof useTabTokensQuery>;
export type TabTokensLazyQueryHookResult = ReturnType<typeof useTabTokensLazyQuery>;
export type TabTokensSuspenseQueryHookResult = ReturnType<typeof useTabTokensSuspenseQuery>;
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
// @ts-ignore
export function useTokenFiltersSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TokenFiltersQuery, TokenFiltersQueryVariables>): Apollo.UseSuspenseQueryResult<TokenFiltersQuery, TokenFiltersQueryVariables>;
export function useTokenFiltersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TokenFiltersQuery, TokenFiltersQueryVariables>): Apollo.UseSuspenseQueryResult<TokenFiltersQuery | undefined, TokenFiltersQueryVariables>;
export function useTokenFiltersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TokenFiltersQuery, TokenFiltersQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TokenFiltersQuery, TokenFiltersQueryVariables>(TokenFiltersDocument, options);
        }
export type TokenFiltersQueryHookResult = ReturnType<typeof useTokenFiltersQuery>;
export type TokenFiltersLazyQueryHookResult = ReturnType<typeof useTokenFiltersLazyQuery>;
export type TokenFiltersSuspenseQueryHookResult = ReturnType<typeof useTokenFiltersSuspenseQuery>;
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
export function useUnboundAnalyzersByTokenFilterQuery(baseOptions: Apollo.QueryHookOptions<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables> & ({ variables: UnboundAnalyzersByTokenFilterQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>(UnboundAnalyzersByTokenFilterDocument, options);
      }
export function useUnboundAnalyzersByTokenFilterLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>(UnboundAnalyzersByTokenFilterDocument, options);
        }
// @ts-ignore
export function useUnboundAnalyzersByTokenFilterSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>;
export function useUnboundAnalyzersByTokenFilterSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAnalyzersByTokenFilterQuery | undefined, UnboundAnalyzersByTokenFilterQueryVariables>;
export function useUnboundAnalyzersByTokenFilterSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundAnalyzersByTokenFilterQuery, UnboundAnalyzersByTokenFilterQueryVariables>(UnboundAnalyzersByTokenFilterDocument, options);
        }
export type UnboundAnalyzersByTokenFilterQueryHookResult = ReturnType<typeof useUnboundAnalyzersByTokenFilterQuery>;
export type UnboundAnalyzersByTokenFilterLazyQueryHookResult = ReturnType<typeof useUnboundAnalyzersByTokenFilterLazyQuery>;
export type UnboundAnalyzersByTokenFilterSuspenseQueryHookResult = ReturnType<typeof useUnboundAnalyzersByTokenFilterSuspenseQuery>;
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
export function useTokenFilterQuery(baseOptions: Apollo.QueryHookOptions<TokenFilterQuery, TokenFilterQueryVariables> & ({ variables: TokenFilterQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TokenFilterQuery, TokenFilterQueryVariables>(TokenFilterDocument, options);
      }
export function useTokenFilterLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TokenFilterQuery, TokenFilterQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TokenFilterQuery, TokenFilterQueryVariables>(TokenFilterDocument, options);
        }
// @ts-ignore
export function useTokenFilterSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TokenFilterQuery, TokenFilterQueryVariables>): Apollo.UseSuspenseQueryResult<TokenFilterQuery, TokenFilterQueryVariables>;
export function useTokenFilterSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TokenFilterQuery, TokenFilterQueryVariables>): Apollo.UseSuspenseQueryResult<TokenFilterQuery | undefined, TokenFilterQueryVariables>;
export function useTokenFilterSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TokenFilterQuery, TokenFilterQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TokenFilterQuery, TokenFilterQueryVariables>(TokenFilterDocument, options);
        }
export type TokenFilterQueryHookResult = ReturnType<typeof useTokenFilterQuery>;
export type TokenFilterLazyQueryHookResult = ReturnType<typeof useTokenFilterLazyQuery>;
export type TokenFilterSuspenseQueryHookResult = ReturnType<typeof useTokenFilterSuspenseQuery>;
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
export function useTabTokenTabQuery(baseOptions: Apollo.QueryHookOptions<TabTokenTabQuery, TabTokenTabQueryVariables> & ({ variables: TabTokenTabQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TabTokenTabQuery, TabTokenTabQueryVariables>(TabTokenTabDocument, options);
      }
export function useTabTokenTabLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TabTokenTabQuery, TabTokenTabQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TabTokenTabQuery, TabTokenTabQueryVariables>(TabTokenTabDocument, options);
        }
// @ts-ignore
export function useTabTokenTabSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TabTokenTabQuery, TabTokenTabQueryVariables>): Apollo.UseSuspenseQueryResult<TabTokenTabQuery, TabTokenTabQueryVariables>;
export function useTabTokenTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabTokenTabQuery, TabTokenTabQueryVariables>): Apollo.UseSuspenseQueryResult<TabTokenTabQuery | undefined, TabTokenTabQueryVariables>;
export function useTabTokenTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabTokenTabQuery, TabTokenTabQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TabTokenTabQuery, TabTokenTabQueryVariables>(TabTokenTabDocument, options);
        }
export type TabTokenTabQueryHookResult = ReturnType<typeof useTabTokenTabQuery>;
export type TabTokenTabLazyQueryHookResult = ReturnType<typeof useTabTokenTabLazyQuery>;
export type TabTokenTabSuspenseQueryHookResult = ReturnType<typeof useTabTokenTabSuspenseQuery>;
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
export function useDocTypeFieldValueQuery(baseOptions: Apollo.QueryHookOptions<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables> & ({ variables: DocTypeFieldValueQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>(DocTypeFieldValueDocument, options);
      }
export function useDocTypeFieldValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>(DocTypeFieldValueDocument, options);
        }
// @ts-ignore
export function useDocTypeFieldValueSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>;
export function useDocTypeFieldValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldValueQuery | undefined, DocTypeFieldValueQueryVariables>;
export function useDocTypeFieldValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocTypeFieldValueQuery, DocTypeFieldValueQueryVariables>(DocTypeFieldValueDocument, options);
        }
export type DocTypeFieldValueQueryHookResult = ReturnType<typeof useDocTypeFieldValueQuery>;
export type DocTypeFieldValueLazyQueryHookResult = ReturnType<typeof useDocTypeFieldValueLazyQuery>;
export type DocTypeFieldValueSuspenseQueryHookResult = ReturnType<typeof useDocTypeFieldValueSuspenseQuery>;
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
// @ts-ignore
export function useDocTypeFieldOptionsTokenTabSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>;
export function useDocTypeFieldOptionsTokenTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldOptionsTokenTabQuery | undefined, DocTypeFieldOptionsTokenTabQueryVariables>;
export function useDocTypeFieldOptionsTokenTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocTypeFieldOptionsTokenTabQuery, DocTypeFieldOptionsTokenTabQueryVariables>(DocTypeFieldOptionsTokenTabDocument, options);
        }
export type DocTypeFieldOptionsTokenTabQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsTokenTabQuery>;
export type DocTypeFieldOptionsTokenTabLazyQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsTokenTabLazyQuery>;
export type DocTypeFieldOptionsTokenTabSuspenseQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsTokenTabSuspenseQuery>;
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
// @ts-ignore
export function useTabTokensQuerySuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TabTokensQueryQuery, TabTokensQueryQueryVariables>): Apollo.UseSuspenseQueryResult<TabTokensQueryQuery, TabTokensQueryQueryVariables>;
export function useTabTokensQuerySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabTokensQueryQuery, TabTokensQueryQueryVariables>): Apollo.UseSuspenseQueryResult<TabTokensQueryQuery | undefined, TabTokensQueryQueryVariables>;
export function useTabTokensQuerySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TabTokensQueryQuery, TabTokensQueryQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TabTokensQueryQuery, TabTokensQueryQueryVariables>(TabTokensQueryDocument, options);
        }
export type TabTokensQueryQueryHookResult = ReturnType<typeof useTabTokensQueryQuery>;
export type TabTokensQueryLazyQueryHookResult = ReturnType<typeof useTabTokensQueryLazyQuery>;
export type TabTokensQuerySuspenseQueryHookResult = ReturnType<typeof useTabTokensQuerySuspenseQuery>;
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
export function useUnassociatedTokenTabsInTabQuery(baseOptions: Apollo.QueryHookOptions<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables> & ({ variables: UnassociatedTokenTabsInTabQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>(UnassociatedTokenTabsInTabDocument, options);
      }
export function useUnassociatedTokenTabsInTabLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>(UnassociatedTokenTabsInTabDocument, options);
        }
// @ts-ignore
export function useUnassociatedTokenTabsInTabSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>): Apollo.UseSuspenseQueryResult<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>;
export function useUnassociatedTokenTabsInTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>): Apollo.UseSuspenseQueryResult<UnassociatedTokenTabsInTabQuery | undefined, UnassociatedTokenTabsInTabQueryVariables>;
export function useUnassociatedTokenTabsInTabSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnassociatedTokenTabsInTabQuery, UnassociatedTokenTabsInTabQueryVariables>(UnassociatedTokenTabsInTabDocument, options);
        }
export type UnassociatedTokenTabsInTabQueryHookResult = ReturnType<typeof useUnassociatedTokenTabsInTabQuery>;
export type UnassociatedTokenTabsInTabLazyQueryHookResult = ReturnType<typeof useUnassociatedTokenTabsInTabLazyQuery>;
export type UnassociatedTokenTabsInTabSuspenseQueryHookResult = ReturnType<typeof useUnassociatedTokenTabsInTabSuspenseQuery>;
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
export function useTokenizerQuery(baseOptions: Apollo.QueryHookOptions<TokenizerQuery, TokenizerQueryVariables> & ({ variables: TokenizerQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TokenizerQuery, TokenizerQueryVariables>(TokenizerDocument, options);
      }
export function useTokenizerLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TokenizerQuery, TokenizerQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TokenizerQuery, TokenizerQueryVariables>(TokenizerDocument, options);
        }
// @ts-ignore
export function useTokenizerSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TokenizerQuery, TokenizerQueryVariables>): Apollo.UseSuspenseQueryResult<TokenizerQuery, TokenizerQueryVariables>;
export function useTokenizerSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TokenizerQuery, TokenizerQueryVariables>): Apollo.UseSuspenseQueryResult<TokenizerQuery | undefined, TokenizerQueryVariables>;
export function useTokenizerSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TokenizerQuery, TokenizerQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TokenizerQuery, TokenizerQueryVariables>(TokenizerDocument, options);
        }
export type TokenizerQueryHookResult = ReturnType<typeof useTokenizerQuery>;
export type TokenizerLazyQueryHookResult = ReturnType<typeof useTokenizerLazyQuery>;
export type TokenizerSuspenseQueryHookResult = ReturnType<typeof useTokenizerSuspenseQuery>;
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
// @ts-ignore
export function useTokenizersSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TokenizersQuery, TokenizersQueryVariables>): Apollo.UseSuspenseQueryResult<TokenizersQuery, TokenizersQueryVariables>;
export function useTokenizersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TokenizersQuery, TokenizersQueryVariables>): Apollo.UseSuspenseQueryResult<TokenizersQuery | undefined, TokenizersQueryVariables>;
export function useTokenizersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TokenizersQuery, TokenizersQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TokenizersQuery, TokenizersQueryVariables>(TokenizersDocument, options);
        }
export type TokenizersQueryHookResult = ReturnType<typeof useTokenizersQuery>;
export type TokenizersLazyQueryHookResult = ReturnType<typeof useTokenizersLazyQuery>;
export type TokenizersSuspenseQueryHookResult = ReturnType<typeof useTokenizersSuspenseQuery>;
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
// @ts-ignore
export function useAnnotatorsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AnnotatorsQuery, AnnotatorsQueryVariables>): Apollo.UseSuspenseQueryResult<AnnotatorsQuery, AnnotatorsQueryVariables>;
export function useAnnotatorsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnnotatorsQuery, AnnotatorsQueryVariables>): Apollo.UseSuspenseQueryResult<AnnotatorsQuery | undefined, AnnotatorsQueryVariables>;
export function useAnnotatorsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnnotatorsQuery, AnnotatorsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AnnotatorsQuery, AnnotatorsQueryVariables>(AnnotatorsDocument, options);
        }
export type AnnotatorsQueryHookResult = ReturnType<typeof useAnnotatorsQuery>;
export type AnnotatorsLazyQueryHookResult = ReturnType<typeof useAnnotatorsLazyQuery>;
export type AnnotatorsSuspenseQueryHookResult = ReturnType<typeof useAnnotatorsSuspenseQuery>;
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
// @ts-ignore
export function useDocTypeFieldOptionsAnnotatorsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>;
export function useDocTypeFieldOptionsAnnotatorsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldOptionsAnnotatorsQuery | undefined, DocTypeFieldOptionsAnnotatorsQueryVariables>;
export function useDocTypeFieldOptionsAnnotatorsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocTypeFieldOptionsAnnotatorsQuery, DocTypeFieldOptionsAnnotatorsQueryVariables>(DocTypeFieldOptionsAnnotatorsDocument, options);
        }
export type DocTypeFieldOptionsAnnotatorsQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsAnnotatorsQuery>;
export type DocTypeFieldOptionsAnnotatorsLazyQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsAnnotatorsLazyQuery>;
export type DocTypeFieldOptionsAnnotatorsSuspenseQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsAnnotatorsSuspenseQuery>;
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
export function useAnnotatorQuery(baseOptions: Apollo.QueryHookOptions<AnnotatorQuery, AnnotatorQueryVariables> & ({ variables: AnnotatorQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AnnotatorQuery, AnnotatorQueryVariables>(AnnotatorDocument, options);
      }
export function useAnnotatorLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AnnotatorQuery, AnnotatorQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AnnotatorQuery, AnnotatorQueryVariables>(AnnotatorDocument, options);
        }
// @ts-ignore
export function useAnnotatorSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AnnotatorQuery, AnnotatorQueryVariables>): Apollo.UseSuspenseQueryResult<AnnotatorQuery, AnnotatorQueryVariables>;
export function useAnnotatorSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnnotatorQuery, AnnotatorQueryVariables>): Apollo.UseSuspenseQueryResult<AnnotatorQuery | undefined, AnnotatorQueryVariables>;
export function useAnnotatorSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AnnotatorQuery, AnnotatorQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AnnotatorQuery, AnnotatorQueryVariables>(AnnotatorDocument, options);
        }
export type AnnotatorQueryHookResult = ReturnType<typeof useAnnotatorQuery>;
export type AnnotatorLazyQueryHookResult = ReturnType<typeof useAnnotatorLazyQuery>;
export type AnnotatorSuspenseQueryHookResult = ReturnType<typeof useAnnotatorSuspenseQuery>;
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
export function useDocTypeFieldOptionsQuery(baseOptions: Apollo.QueryHookOptions<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables> & ({ variables: DocTypeFieldOptionsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>(DocTypeFieldOptionsDocument, options);
      }
export function useDocTypeFieldOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>(DocTypeFieldOptionsDocument, options);
        }
// @ts-ignore
export function useDocTypeFieldOptionsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>;
export function useDocTypeFieldOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<DocTypeFieldOptionsQuery | undefined, DocTypeFieldOptionsQueryVariables>;
export function useDocTypeFieldOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DocTypeFieldOptionsQuery, DocTypeFieldOptionsQueryVariables>(DocTypeFieldOptionsDocument, options);
        }
export type DocTypeFieldOptionsQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsQuery>;
export type DocTypeFieldOptionsLazyQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsLazyQuery>;
export type DocTypeFieldOptionsSuspenseQueryHookResult = ReturnType<typeof useDocTypeFieldOptionsSuspenseQuery>;
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
// @ts-ignore
export function useBucketsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<BucketsQuery, BucketsQueryVariables>): Apollo.UseSuspenseQueryResult<BucketsQuery, BucketsQueryVariables>;
export function useBucketsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketsQuery, BucketsQueryVariables>): Apollo.UseSuspenseQueryResult<BucketsQuery | undefined, BucketsQueryVariables>;
export function useBucketsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketsQuery, BucketsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<BucketsQuery, BucketsQueryVariables>(BucketsDocument, options);
        }
export type BucketsQueryHookResult = ReturnType<typeof useBucketsQuery>;
export type BucketsLazyQueryHookResult = ReturnType<typeof useBucketsLazyQuery>;
export type BucketsSuspenseQueryHookResult = ReturnType<typeof useBucketsSuspenseQuery>;
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
// @ts-ignore
export function useQueryAnalysisOptionsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>;
export function useQueryAnalysisOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysisOptionsQuery | undefined, QueryAnalysisOptionsQueryVariables>;
export function useQueryAnalysisOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<QueryAnalysisOptionsQuery, QueryAnalysisOptionsQueryVariables>(QueryAnalysisOptionsDocument, options);
        }
export type QueryAnalysisOptionsQueryHookResult = ReturnType<typeof useQueryAnalysisOptionsQuery>;
export type QueryAnalysisOptionsLazyQueryHookResult = ReturnType<typeof useQueryAnalysisOptionsLazyQuery>;
export type QueryAnalysisOptionsSuspenseQueryHookResult = ReturnType<typeof useQueryAnalysisOptionsSuspenseQuery>;
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
export function useQueryAnalysisValueQuery(baseOptions: Apollo.QueryHookOptions<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables> & ({ variables: QueryAnalysisValueQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>(QueryAnalysisValueDocument, options);
      }
export function useQueryAnalysisValueLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>(QueryAnalysisValueDocument, options);
        }
// @ts-ignore
export function useQueryAnalysisValueSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>;
export function useQueryAnalysisValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>): Apollo.UseSuspenseQueryResult<QueryAnalysisValueQuery | undefined, QueryAnalysisValueQueryVariables>;
export function useQueryAnalysisValueSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<QueryAnalysisValueQuery, QueryAnalysisValueQueryVariables>(QueryAnalysisValueDocument, options);
        }
export type QueryAnalysisValueQueryHookResult = ReturnType<typeof useQueryAnalysisValueQuery>;
export type QueryAnalysisValueLazyQueryHookResult = ReturnType<typeof useQueryAnalysisValueLazyQuery>;
export type QueryAnalysisValueSuspenseQueryHookResult = ReturnType<typeof useQueryAnalysisValueSuspenseQuery>;
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
    autocorrection {
      id
      name
    }
    autocomplete {
      id
      name
    }
    highlight {
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
export function useBucketQuery(baseOptions: Apollo.QueryHookOptions<BucketQuery, BucketQueryVariables> & ({ variables: BucketQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketQuery, BucketQueryVariables>(BucketDocument, options);
      }
export function useBucketLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketQuery, BucketQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketQuery, BucketQueryVariables>(BucketDocument, options);
        }
// @ts-ignore
export function useBucketSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<BucketQuery, BucketQueryVariables>): Apollo.UseSuspenseQueryResult<BucketQuery, BucketQueryVariables>;
export function useBucketSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketQuery, BucketQueryVariables>): Apollo.UseSuspenseQueryResult<BucketQuery | undefined, BucketQueryVariables>;
export function useBucketSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketQuery, BucketQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<BucketQuery, BucketQueryVariables>(BucketDocument, options);
        }
export type BucketQueryHookResult = ReturnType<typeof useBucketQuery>;
export type BucketLazyQueryHookResult = ReturnType<typeof useBucketLazyQuery>;
export type BucketSuspenseQueryHookResult = ReturnType<typeof useBucketSuspenseQuery>;
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
      first: 1000
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
      first: 1000
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
      first: 1000
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
    languages(
      searchText: $searchText
      first: 1000
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
export function useBucketDataSourcesQuery(baseOptions: Apollo.QueryHookOptions<BucketDataSourcesQuery, BucketDataSourcesQueryVariables> & ({ variables: BucketDataSourcesQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>(BucketDataSourcesDocument, options);
      }
export function useBucketDataSourcesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>(BucketDataSourcesDocument, options);
        }
// @ts-ignore
export function useBucketDataSourcesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>): Apollo.UseSuspenseQueryResult<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>;
export function useBucketDataSourcesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>): Apollo.UseSuspenseQueryResult<BucketDataSourcesQuery | undefined, BucketDataSourcesQueryVariables>;
export function useBucketDataSourcesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>(BucketDataSourcesDocument, options);
        }
export type BucketDataSourcesQueryHookResult = ReturnType<typeof useBucketDataSourcesQuery>;
export type BucketDataSourcesLazyQueryHookResult = ReturnType<typeof useBucketDataSourcesLazyQuery>;
export type BucketDataSourcesSuspenseQueryHookResult = ReturnType<typeof useBucketDataSourcesSuspenseQuery>;
export type BucketDataSourcesQueryResult = Apollo.QueryResult<BucketDataSourcesQuery, BucketDataSourcesQueryVariables>;
export const CreateOrUpdateBucketDocument = gql`
    mutation CreateOrUpdateBucket($id: ID, $name: String!, $description: String, $refreshOnDate: Boolean!, $refreshOnQuery: Boolean!, $refreshOnSuggestionCategory: Boolean!, $refreshOnTab: Boolean!, $retrieveType: RetrieveType!, $datasourceIds: [BigInteger], $suggestionCategoryIds: [BigInteger], $tabIds: [BigInteger], $queryAnalysisId: BigInteger, $defaultLanguageId: BigInteger, $searchConfigId: BigInteger, $ragConfigurationChat: BigInteger, $ragConfigurationChatTool: BigInteger, $ragConfigurationSimpleGenerate: BigInteger, $autocorrection: BigInteger, $autocomplete: BigInteger, $highlightId: BigInteger, $languageIds: [BigInteger]) {
  bucketWithLists(
    id: $id
    bucketWithListsDTO: {name: $name, description: $description, refreshOnDate: $refreshOnDate, refreshOnQuery: $refreshOnQuery, refreshOnSuggestionCategory: $refreshOnSuggestionCategory, refreshOnTab: $refreshOnTab, retrieveType: $retrieveType, datasourceIds: $datasourceIds, suggestionCategoryIds: $suggestionCategoryIds, tabIds: $tabIds, queryAnalysisId: $queryAnalysisId, defaultLanguageId: $defaultLanguageId, searchConfigId: $searchConfigId, ragConfigurationChat: $ragConfigurationChat, ragConfigurationChatTool: $ragConfigurationChatTool, ragConfigurationSimpleGenerate: $ragConfigurationSimpleGenerate, autocorrectionId: $autocorrection, autocompleteId: $autocomplete, highlightId: $highlightId, languageIds: $languageIds}
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
 *      autocorrection: // value for 'autocorrection'
 *      autocomplete: // value for 'autocomplete'
 *      highlightId: // value for 'highlightId'
 *      languageIds: // value for 'languageIds'
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
      first: 1000
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
export function useBucketLanguagesQuery(baseOptions: Apollo.QueryHookOptions<BucketLanguagesQuery, BucketLanguagesQueryVariables> & ({ variables: BucketLanguagesQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketLanguagesQuery, BucketLanguagesQueryVariables>(BucketLanguagesDocument, options);
      }
export function useBucketLanguagesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketLanguagesQuery, BucketLanguagesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketLanguagesQuery, BucketLanguagesQueryVariables>(BucketLanguagesDocument, options);
        }
// @ts-ignore
export function useBucketLanguagesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<BucketLanguagesQuery, BucketLanguagesQueryVariables>): Apollo.UseSuspenseQueryResult<BucketLanguagesQuery, BucketLanguagesQueryVariables>;
export function useBucketLanguagesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketLanguagesQuery, BucketLanguagesQueryVariables>): Apollo.UseSuspenseQueryResult<BucketLanguagesQuery | undefined, BucketLanguagesQueryVariables>;
export function useBucketLanguagesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketLanguagesQuery, BucketLanguagesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<BucketLanguagesQuery, BucketLanguagesQueryVariables>(BucketLanguagesDocument, options);
        }
export type BucketLanguagesQueryHookResult = ReturnType<typeof useBucketLanguagesQuery>;
export type BucketLanguagesLazyQueryHookResult = ReturnType<typeof useBucketLanguagesLazyQuery>;
export type BucketLanguagesSuspenseQueryHookResult = ReturnType<typeof useBucketLanguagesSuspenseQuery>;
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
      first: 1000
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
export function useBucketTabsQuery(baseOptions: Apollo.QueryHookOptions<BucketTabsQuery, BucketTabsQueryVariables> & ({ variables: BucketTabsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketTabsQuery, BucketTabsQueryVariables>(BucketTabsDocument, options);
      }
export function useBucketTabsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketTabsQuery, BucketTabsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketTabsQuery, BucketTabsQueryVariables>(BucketTabsDocument, options);
        }
// @ts-ignore
export function useBucketTabsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<BucketTabsQuery, BucketTabsQueryVariables>): Apollo.UseSuspenseQueryResult<BucketTabsQuery, BucketTabsQueryVariables>;
export function useBucketTabsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketTabsQuery, BucketTabsQueryVariables>): Apollo.UseSuspenseQueryResult<BucketTabsQuery | undefined, BucketTabsQueryVariables>;
export function useBucketTabsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketTabsQuery, BucketTabsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<BucketTabsQuery, BucketTabsQueryVariables>(BucketTabsDocument, options);
        }
export type BucketTabsQueryHookResult = ReturnType<typeof useBucketTabsQuery>;
export type BucketTabsLazyQueryHookResult = ReturnType<typeof useBucketTabsLazyQuery>;
export type BucketTabsSuspenseQueryHookResult = ReturnType<typeof useBucketTabsSuspenseQuery>;
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
export const AddAutocorrectionToBucketDocument = gql`
    mutation AddAutocorrectionToBucket($autocorrectionId: ID!, $parentId: ID!) {
  bindAutocorrectionToBucket(
    autocorrectionId: $autocorrectionId
    bucketId: $parentId
  ) {
    left {
      id
    }
  }
}
    `;
export type AddAutocorrectionToBucketMutationFn = Apollo.MutationFunction<AddAutocorrectionToBucketMutation, AddAutocorrectionToBucketMutationVariables>;

/**
 * __useAddAutocorrectionToBucketMutation__
 *
 * To run a mutation, you first call `useAddAutocorrectionToBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddAutocorrectionToBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addAutocorrectionToBucketMutation, { data, loading, error }] = useAddAutocorrectionToBucketMutation({
 *   variables: {
 *      autocorrectionId: // value for 'autocorrectionId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddAutocorrectionToBucketMutation(baseOptions?: Apollo.MutationHookOptions<AddAutocorrectionToBucketMutation, AddAutocorrectionToBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddAutocorrectionToBucketMutation, AddAutocorrectionToBucketMutationVariables>(AddAutocorrectionToBucketDocument, options);
      }
export type AddAutocorrectionToBucketMutationHookResult = ReturnType<typeof useAddAutocorrectionToBucketMutation>;
export type AddAutocorrectionToBucketMutationResult = Apollo.MutationResult<AddAutocorrectionToBucketMutation>;
export type AddAutocorrectionToBucketMutationOptions = Apollo.BaseMutationOptions<AddAutocorrectionToBucketMutation, AddAutocorrectionToBucketMutationVariables>;
export const RemoveAutocorrectionFromBucketDocument = gql`
    mutation RemoveAutocorrectionFromBucket($parentId: ID!) {
  unbindAutocorrectionFromBucket(bucketId: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveAutocorrectionFromBucketMutationFn = Apollo.MutationFunction<RemoveAutocorrectionFromBucketMutation, RemoveAutocorrectionFromBucketMutationVariables>;

/**
 * __useRemoveAutocorrectionFromBucketMutation__
 *
 * To run a mutation, you first call `useRemoveAutocorrectionFromBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveAutocorrectionFromBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeAutocorrectionFromBucketMutation, { data, loading, error }] = useRemoveAutocorrectionFromBucketMutation({
 *   variables: {
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveAutocorrectionFromBucketMutation(baseOptions?: Apollo.MutationHookOptions<RemoveAutocorrectionFromBucketMutation, RemoveAutocorrectionFromBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveAutocorrectionFromBucketMutation, RemoveAutocorrectionFromBucketMutationVariables>(RemoveAutocorrectionFromBucketDocument, options);
      }
export type RemoveAutocorrectionFromBucketMutationHookResult = ReturnType<typeof useRemoveAutocorrectionFromBucketMutation>;
export type RemoveAutocorrectionFromBucketMutationResult = Apollo.MutationResult<RemoveAutocorrectionFromBucketMutation>;
export type RemoveAutocorrectionFromBucketMutationOptions = Apollo.BaseMutationOptions<RemoveAutocorrectionFromBucketMutation, RemoveAutocorrectionFromBucketMutationVariables>;
export const AddAutocompleteToBucketDocument = gql`
    mutation AddAutocompleteToBucket($autocompleteId: ID!, $parentId: ID!) {
  bindAutocompleteToBucket(autocompleteId: $autocompleteId, bucketId: $parentId) {
    left {
      id
    }
  }
}
    `;
export type AddAutocompleteToBucketMutationFn = Apollo.MutationFunction<AddAutocompleteToBucketMutation, AddAutocompleteToBucketMutationVariables>;

/**
 * __useAddAutocompleteToBucketMutation__
 *
 * To run a mutation, you first call `useAddAutocompleteToBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAddAutocompleteToBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [addAutocompleteToBucketMutation, { data, loading, error }] = useAddAutocompleteToBucketMutation({
 *   variables: {
 *      autocompleteId: // value for 'autocompleteId'
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useAddAutocompleteToBucketMutation(baseOptions?: Apollo.MutationHookOptions<AddAutocompleteToBucketMutation, AddAutocompleteToBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AddAutocompleteToBucketMutation, AddAutocompleteToBucketMutationVariables>(AddAutocompleteToBucketDocument, options);
      }
export type AddAutocompleteToBucketMutationHookResult = ReturnType<typeof useAddAutocompleteToBucketMutation>;
export type AddAutocompleteToBucketMutationResult = Apollo.MutationResult<AddAutocompleteToBucketMutation>;
export type AddAutocompleteToBucketMutationOptions = Apollo.BaseMutationOptions<AddAutocompleteToBucketMutation, AddAutocompleteToBucketMutationVariables>;
export const RemoveAutocompleteFromBucketDocument = gql`
    mutation RemoveAutocompleteFromBucket($parentId: ID!) {
  unbindAutocompleteFromBucket(bucketId: $parentId) {
    left {
      id
    }
    right {
      id
    }
  }
}
    `;
export type RemoveAutocompleteFromBucketMutationFn = Apollo.MutationFunction<RemoveAutocompleteFromBucketMutation, RemoveAutocompleteFromBucketMutationVariables>;

/**
 * __useRemoveAutocompleteFromBucketMutation__
 *
 * To run a mutation, you first call `useRemoveAutocompleteFromBucketMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRemoveAutocompleteFromBucketMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [removeAutocompleteFromBucketMutation, { data, loading, error }] = useRemoveAutocompleteFromBucketMutation({
 *   variables: {
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useRemoveAutocompleteFromBucketMutation(baseOptions?: Apollo.MutationHookOptions<RemoveAutocompleteFromBucketMutation, RemoveAutocompleteFromBucketMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RemoveAutocompleteFromBucketMutation, RemoveAutocompleteFromBucketMutationVariables>(RemoveAutocompleteFromBucketDocument, options);
      }
export type RemoveAutocompleteFromBucketMutationHookResult = ReturnType<typeof useRemoveAutocompleteFromBucketMutation>;
export type RemoveAutocompleteFromBucketMutationResult = Apollo.MutationResult<RemoveAutocompleteFromBucketMutation>;
export type RemoveAutocompleteFromBucketMutationOptions = Apollo.BaseMutationOptions<RemoveAutocompleteFromBucketMutation, RemoveAutocompleteFromBucketMutationVariables>;
export const BucketSuggestionCategoriesDocument = gql`
    query BucketSuggestionCategories($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
  bucket(id: $parentId) {
    id
    suggestionCategories(
      searchText: $searchText
      notEqual: $unassociated
      first: 1000
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
export function useBucketSuggestionCategoriesQuery(baseOptions: Apollo.QueryHookOptions<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables> & ({ variables: BucketSuggestionCategoriesQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>(BucketSuggestionCategoriesDocument, options);
      }
export function useBucketSuggestionCategoriesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>(BucketSuggestionCategoriesDocument, options);
        }
// @ts-ignore
export function useBucketSuggestionCategoriesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>): Apollo.UseSuspenseQueryResult<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>;
export function useBucketSuggestionCategoriesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>): Apollo.UseSuspenseQueryResult<BucketSuggestionCategoriesQuery | undefined, BucketSuggestionCategoriesQueryVariables>;
export function useBucketSuggestionCategoriesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>(BucketSuggestionCategoriesDocument, options);
        }
export type BucketSuggestionCategoriesQueryHookResult = ReturnType<typeof useBucketSuggestionCategoriesQuery>;
export type BucketSuggestionCategoriesLazyQueryHookResult = ReturnType<typeof useBucketSuggestionCategoriesLazyQuery>;
export type BucketSuggestionCategoriesSuspenseQueryHookResult = ReturnType<typeof useBucketSuggestionCategoriesSuspenseQuery>;
export type BucketSuggestionCategoriesQueryResult = Apollo.QueryResult<BucketSuggestionCategoriesQuery, BucketSuggestionCategoriesQueryVariables>;
export const UnboundAutocorrectionByBucketDocument = gql`
    query unboundAutocorrectionByBucket($parentId: ID!) {
  bucket(id: $parentId) {
    id
    name
  }
}
    `;

/**
 * __useUnboundAutocorrectionByBucketQuery__
 *
 * To run a query within a React component, call `useUnboundAutocorrectionByBucketQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundAutocorrectionByBucketQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundAutocorrectionByBucketQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useUnboundAutocorrectionByBucketQuery(baseOptions: Apollo.QueryHookOptions<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables> & ({ variables: UnboundAutocorrectionByBucketQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables>(UnboundAutocorrectionByBucketDocument, options);
      }
export function useUnboundAutocorrectionByBucketLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables>(UnboundAutocorrectionByBucketDocument, options);
        }
// @ts-ignore
export function useUnboundAutocorrectionByBucketSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables>;
export function useUnboundAutocorrectionByBucketSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAutocorrectionByBucketQuery | undefined, UnboundAutocorrectionByBucketQueryVariables>;
export function useUnboundAutocorrectionByBucketSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables>(UnboundAutocorrectionByBucketDocument, options);
        }
export type UnboundAutocorrectionByBucketQueryHookResult = ReturnType<typeof useUnboundAutocorrectionByBucketQuery>;
export type UnboundAutocorrectionByBucketLazyQueryHookResult = ReturnType<typeof useUnboundAutocorrectionByBucketLazyQuery>;
export type UnboundAutocorrectionByBucketSuspenseQueryHookResult = ReturnType<typeof useUnboundAutocorrectionByBucketSuspenseQuery>;
export type UnboundAutocorrectionByBucketQueryResult = Apollo.QueryResult<UnboundAutocorrectionByBucketQuery, UnboundAutocorrectionByBucketQueryVariables>;
export const UnboundAutocompleteByBucketDocument = gql`
    query unboundAutocompleteByBucket($parentId: ID!) {
  bucket(id: $parentId) {
    id
    name
  }
}
    `;

/**
 * __useUnboundAutocompleteByBucketQuery__
 *
 * To run a query within a React component, call `useUnboundAutocompleteByBucketQuery` and pass it any options that fit your needs.
 * When your component renders, `useUnboundAutocompleteByBucketQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useUnboundAutocompleteByBucketQuery({
 *   variables: {
 *      parentId: // value for 'parentId'
 *   },
 * });
 */
export function useUnboundAutocompleteByBucketQuery(baseOptions: Apollo.QueryHookOptions<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables> & ({ variables: UnboundAutocompleteByBucketQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables>(UnboundAutocompleteByBucketDocument, options);
      }
export function useUnboundAutocompleteByBucketLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables>(UnboundAutocompleteByBucketDocument, options);
        }
// @ts-ignore
export function useUnboundAutocompleteByBucketSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables>;
export function useUnboundAutocompleteByBucketSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundAutocompleteByBucketQuery | undefined, UnboundAutocompleteByBucketQueryVariables>;
export function useUnboundAutocompleteByBucketSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables>(UnboundAutocompleteByBucketDocument, options);
        }
export type UnboundAutocompleteByBucketQueryHookResult = ReturnType<typeof useUnboundAutocompleteByBucketQuery>;
export type UnboundAutocompleteByBucketLazyQueryHookResult = ReturnType<typeof useUnboundAutocompleteByBucketLazyQuery>;
export type UnboundAutocompleteByBucketSuspenseQueryHookResult = ReturnType<typeof useUnboundAutocompleteByBucketSuspenseQuery>;
export type UnboundAutocompleteByBucketQueryResult = Apollo.QueryResult<UnboundAutocompleteByBucketQuery, UnboundAutocompleteByBucketQueryVariables>;
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
export const AutocompletesOptionsDocument = gql`
    query AutocompletesOptions($searchText: String, $cursor: String) {
  autocompletes(searchText: $searchText, first: 5, after: $cursor) {
    edges {
      cursor
      node {
        id
        description
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
 * __useAutocompletesOptionsQuery__
 *
 * To run a query within a React component, call `useAutocompletesOptionsQuery` and pass it any options that fit your needs.
 * When your component renders, `useAutocompletesOptionsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useAutocompletesOptionsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useAutocompletesOptionsQuery(baseOptions?: Apollo.QueryHookOptions<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>(AutocompletesOptionsDocument, options);
      }
export function useAutocompletesOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>(AutocompletesOptionsDocument, options);
        }
// @ts-ignore
export function useAutocompletesOptionsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>;
export function useAutocompletesOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<AutocompletesOptionsQuery | undefined, AutocompletesOptionsQueryVariables>;
export function useAutocompletesOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>(AutocompletesOptionsDocument, options);
        }
export type AutocompletesOptionsQueryHookResult = ReturnType<typeof useAutocompletesOptionsQuery>;
export type AutocompletesOptionsLazyQueryHookResult = ReturnType<typeof useAutocompletesOptionsLazyQuery>;
export type AutocompletesOptionsSuspenseQueryHookResult = ReturnType<typeof useAutocompletesOptionsSuspenseQuery>;
export type AutocompletesOptionsQueryResult = Apollo.QueryResult<AutocompletesOptionsQuery, AutocompletesOptionsQueryVariables>;
export const CreateDataIndexDocument = gql`
    mutation CreateDataIndex($name: String!, $datasourceId: ID!, $description: String, $knnIndex: Boolean, $docTypeIds: [BigInteger], $chunkType: ChunkType, $chunkWindowSize: Int, $embeddingJsonConfig: String, $embeddingDocTypeFieldId: BigInteger, $chunkHeaderFieldIds: [BigInteger], $settings: String) {
  dataIndex(
    datasourceId: $datasourceId
    dataIndexDTO: {name: $name, description: $description, knnIndex: $knnIndex, docTypeIds: $docTypeIds, chunkType: $chunkType, chunkWindowSize: $chunkWindowSize, embeddingJsonConfig: $embeddingJsonConfig, embeddingDocTypeFieldId: $embeddingDocTypeFieldId, chunkHeaderFieldIds: $chunkHeaderFieldIds, settings: $settings}
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
 *      chunkHeaderFieldIds: // value for 'chunkHeaderFieldIds'
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
        docTypes(first: 1) {
          edges {
            node {
              id
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
// @ts-ignore
export function useDataIndicesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DataIndicesQuery, DataIndicesQueryVariables>): Apollo.UseSuspenseQueryResult<DataIndicesQuery, DataIndicesQueryVariables>;
export function useDataIndicesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataIndicesQuery, DataIndicesQueryVariables>): Apollo.UseSuspenseQueryResult<DataIndicesQuery | undefined, DataIndicesQueryVariables>;
export function useDataIndicesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataIndicesQuery, DataIndicesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DataIndicesQuery, DataIndicesQueryVariables>(DataIndicesDocument, options);
        }
export type DataIndicesQueryHookResult = ReturnType<typeof useDataIndicesQuery>;
export type DataIndicesLazyQueryHookResult = ReturnType<typeof useDataIndicesLazyQuery>;
export type DataIndicesSuspenseQueryHookResult = ReturnType<typeof useDataIndicesSuspenseQuery>;
export type DataIndicesQueryResult = Apollo.QueryResult<DataIndicesQuery, DataIndicesQueryVariables>;
export const DataIndexDocument = gql`
    query DataIndex($id: ID!) {
  dataIndex(id: $id) {
    name
    description
    settings
    customSettings
    chunkType
    chunkWindowSize
    embeddingJsonConfig
    knnIndex
    datasource {
      id
      name
    }
    embeddingDocTypeField {
      id
      name
    }
    chunkHeaderFields {
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
export function useDataIndexQuery(baseOptions: Apollo.QueryHookOptions<DataIndexQuery, DataIndexQueryVariables> & ({ variables: DataIndexQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataIndexQuery, DataIndexQueryVariables>(DataIndexDocument, options);
      }
export function useDataIndexLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataIndexQuery, DataIndexQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataIndexQuery, DataIndexQueryVariables>(DataIndexDocument, options);
        }
// @ts-ignore
export function useDataIndexSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DataIndexQuery, DataIndexQueryVariables>): Apollo.UseSuspenseQueryResult<DataIndexQuery, DataIndexQueryVariables>;
export function useDataIndexSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataIndexQuery, DataIndexQueryVariables>): Apollo.UseSuspenseQueryResult<DataIndexQuery | undefined, DataIndexQueryVariables>;
export function useDataIndexSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataIndexQuery, DataIndexQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DataIndexQuery, DataIndexQueryVariables>(DataIndexDocument, options);
        }
export type DataIndexQueryHookResult = ReturnType<typeof useDataIndexQuery>;
export type DataIndexLazyQueryHookResult = ReturnType<typeof useDataIndexLazyQuery>;
export type DataIndexSuspenseQueryHookResult = ReturnType<typeof useDataIndexSuspenseQuery>;
export type DataIndexQueryResult = Apollo.QueryResult<DataIndexQuery, DataIndexQueryVariables>;
export const DatasourceIndexConfigDocument = gql`
    query DatasourceIndexConfig($id: ID!) {
  datasource(id: $id) {
    knnIndex
    chunkType
    chunkWindowSize
    embeddingJsonConfig
    settings
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
  }
}
    `;

/**
 * __useDatasourceIndexConfigQuery__
 *
 * To run a query within a React component, call `useDatasourceIndexConfigQuery` and pass it any options that fit your needs.
 * When your component renders, `useDatasourceIndexConfigQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useDatasourceIndexConfigQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useDatasourceIndexConfigQuery(baseOptions: Apollo.QueryHookOptions<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables> & ({ variables: DatasourceIndexConfigQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables>(DatasourceIndexConfigDocument, options);
      }
export function useDatasourceIndexConfigLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables>(DatasourceIndexConfigDocument, options);
        }
// @ts-ignore
export function useDatasourceIndexConfigSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables>): Apollo.UseSuspenseQueryResult<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables>;
export function useDatasourceIndexConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables>): Apollo.UseSuspenseQueryResult<DatasourceIndexConfigQuery | undefined, DatasourceIndexConfigQueryVariables>;
export function useDatasourceIndexConfigSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables>(DatasourceIndexConfigDocument, options);
        }
export type DatasourceIndexConfigQueryHookResult = ReturnType<typeof useDatasourceIndexConfigQuery>;
export type DatasourceIndexConfigLazyQueryHookResult = ReturnType<typeof useDatasourceIndexConfigLazyQuery>;
export type DatasourceIndexConfigSuspenseQueryHookResult = ReturnType<typeof useDatasourceIndexConfigSuspenseQuery>;
export type DatasourceIndexConfigQueryResult = Apollo.QueryResult<DatasourceIndexConfigQuery, DatasourceIndexConfigQueryVariables>;
export const DeleteDataIndexDocument = gql`
    mutation DeleteDataIndex($dataIndexId: ID!, $dataIndexName: String!) {
  deleteDataIndex(dataIndexId: $dataIndexId, dataIndexName: $dataIndexName) {
    id
    name
  }
}
    `;
export type DeleteDataIndexMutationFn = Apollo.MutationFunction<DeleteDataIndexMutation, DeleteDataIndexMutationVariables>;

/**
 * __useDeleteDataIndexMutation__
 *
 * To run a mutation, you first call `useDeleteDataIndexMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteDataIndexMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteDataIndexMutation, { data, loading, error }] = useDeleteDataIndexMutation({
 *   variables: {
 *      dataIndexId: // value for 'dataIndexId'
 *      dataIndexName: // value for 'dataIndexName'
 *   },
 * });
 */
export function useDeleteDataIndexMutation(baseOptions?: Apollo.MutationHookOptions<DeleteDataIndexMutation, DeleteDataIndexMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteDataIndexMutation, DeleteDataIndexMutationVariables>(DeleteDataIndexDocument, options);
      }
export type DeleteDataIndexMutationHookResult = ReturnType<typeof useDeleteDataIndexMutation>;
export type DeleteDataIndexMutationResult = Apollo.MutationResult<DeleteDataIndexMutation>;
export type DeleteDataIndexMutationOptions = Apollo.BaseMutationOptions<DeleteDataIndexMutation, DeleteDataIndexMutationVariables>;
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
export function useDataIndexMappingQuery(baseOptions: Apollo.QueryHookOptions<DataIndexMappingQuery, DataIndexMappingQueryVariables> & ({ variables: DataIndexMappingQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataIndexMappingQuery, DataIndexMappingQueryVariables>(DataIndexMappingDocument, options);
      }
export function useDataIndexMappingLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataIndexMappingQuery, DataIndexMappingQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataIndexMappingQuery, DataIndexMappingQueryVariables>(DataIndexMappingDocument, options);
        }
// @ts-ignore
export function useDataIndexMappingSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DataIndexMappingQuery, DataIndexMappingQueryVariables>): Apollo.UseSuspenseQueryResult<DataIndexMappingQuery, DataIndexMappingQueryVariables>;
export function useDataIndexMappingSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataIndexMappingQuery, DataIndexMappingQueryVariables>): Apollo.UseSuspenseQueryResult<DataIndexMappingQuery | undefined, DataIndexMappingQueryVariables>;
export function useDataIndexMappingSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataIndexMappingQuery, DataIndexMappingQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DataIndexMappingQuery, DataIndexMappingQueryVariables>(DataIndexMappingDocument, options);
        }
export type DataIndexMappingQueryHookResult = ReturnType<typeof useDataIndexMappingQuery>;
export type DataIndexMappingLazyQueryHookResult = ReturnType<typeof useDataIndexMappingLazyQuery>;
export type DataIndexMappingSuspenseQueryHookResult = ReturnType<typeof useDataIndexMappingSuspenseQuery>;
export type DataIndexMappingQueryResult = Apollo.QueryResult<DataIndexMappingQuery, DataIndexMappingQueryVariables>;
export const UpdateIndexSettingsDocument = gql`
    mutation UpdateIndexSettings($dataIndexId: ID!, $settings: String, $closeIfNeeded: Boolean!) {
  updateIndexSettings(
    dataIndexId: $dataIndexId
    settings: $settings
    closeIfNeeded: $closeIfNeeded
  ) {
    dataIndexId
    indexName
    status
    reason
  }
}
    `;
export type UpdateIndexSettingsMutationFn = Apollo.MutationFunction<UpdateIndexSettingsMutation, UpdateIndexSettingsMutationVariables>;

/**
 * __useUpdateIndexSettingsMutation__
 *
 * To run a mutation, you first call `useUpdateIndexSettingsMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUpdateIndexSettingsMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [updateIndexSettingsMutation, { data, loading, error }] = useUpdateIndexSettingsMutation({
 *   variables: {
 *      dataIndexId: // value for 'dataIndexId'
 *      settings: // value for 'settings'
 *      closeIfNeeded: // value for 'closeIfNeeded'
 *   },
 * });
 */
export function useUpdateIndexSettingsMutation(baseOptions?: Apollo.MutationHookOptions<UpdateIndexSettingsMutation, UpdateIndexSettingsMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UpdateIndexSettingsMutation, UpdateIndexSettingsMutationVariables>(UpdateIndexSettingsDocument, options);
      }
export type UpdateIndexSettingsMutationHookResult = ReturnType<typeof useUpdateIndexSettingsMutation>;
export type UpdateIndexSettingsMutationResult = Apollo.MutationResult<UpdateIndexSettingsMutation>;
export type UpdateIndexSettingsMutationOptions = Apollo.BaseMutationOptions<UpdateIndexSettingsMutation, UpdateIndexSettingsMutationVariables>;
export const AlignIndexDocument = gql`
    mutation AlignIndex($dataIndexId: ID!, $closeIfNeeded: Boolean!) {
  alignIndex(dataIndexId: $dataIndexId, closeIfNeeded: $closeIfNeeded) {
    dataIndexId
    indexName
    status
    reason
  }
}
    `;
export type AlignIndexMutationFn = Apollo.MutationFunction<AlignIndexMutation, AlignIndexMutationVariables>;

/**
 * __useAlignIndexMutation__
 *
 * To run a mutation, you first call `useAlignIndexMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useAlignIndexMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [alignIndexMutation, { data, loading, error }] = useAlignIndexMutation({
 *   variables: {
 *      dataIndexId: // value for 'dataIndexId'
 *      closeIfNeeded: // value for 'closeIfNeeded'
 *   },
 * });
 */
export function useAlignIndexMutation(baseOptions?: Apollo.MutationHookOptions<AlignIndexMutation, AlignIndexMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<AlignIndexMutation, AlignIndexMutationVariables>(AlignIndexDocument, options);
      }
export type AlignIndexMutationHookResult = ReturnType<typeof useAlignIndexMutation>;
export type AlignIndexMutationResult = Apollo.MutationResult<AlignIndexMutation>;
export type AlignIndexMutationOptions = Apollo.BaseMutationOptions<AlignIndexMutation, AlignIndexMutationVariables>;
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
        scheduling
        reindexable
        reindexing
        lastIngestionDate
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
// @ts-ignore
export function useDataSourcesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DataSourcesQuery, DataSourcesQueryVariables>): Apollo.UseSuspenseQueryResult<DataSourcesQuery, DataSourcesQueryVariables>;
export function useDataSourcesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataSourcesQuery, DataSourcesQueryVariables>): Apollo.UseSuspenseQueryResult<DataSourcesQuery | undefined, DataSourcesQueryVariables>;
export function useDataSourcesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataSourcesQuery, DataSourcesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DataSourcesQuery, DataSourcesQueryVariables>(DataSourcesDocument, options);
        }
export type DataSourcesQueryHookResult = ReturnType<typeof useDataSourcesQuery>;
export type DataSourcesLazyQueryHookResult = ReturnType<typeof useDataSourcesLazyQuery>;
export type DataSourcesSuspenseQueryHookResult = ReturnType<typeof useDataSourcesSuspenseQuery>;
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
export function useUnboundBucketsByDatasourceQuery(baseOptions: Apollo.QueryHookOptions<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables> & ({ variables: UnboundBucketsByDatasourceQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>(UnboundBucketsByDatasourceDocument, options);
      }
export function useUnboundBucketsByDatasourceLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>(UnboundBucketsByDatasourceDocument, options);
        }
// @ts-ignore
export function useUnboundBucketsByDatasourceSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>;
export function useUnboundBucketsByDatasourceSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>): Apollo.UseSuspenseQueryResult<UnboundBucketsByDatasourceQuery | undefined, UnboundBucketsByDatasourceQueryVariables>;
export function useUnboundBucketsByDatasourceSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnboundBucketsByDatasourceQuery, UnboundBucketsByDatasourceQueryVariables>(UnboundBucketsByDatasourceDocument, options);
        }
export type UnboundBucketsByDatasourceQueryHookResult = ReturnType<typeof useUnboundBucketsByDatasourceQuery>;
export type UnboundBucketsByDatasourceLazyQueryHookResult = ReturnType<typeof useUnboundBucketsByDatasourceLazyQuery>;
export type UnboundBucketsByDatasourceSuspenseQueryHookResult = ReturnType<typeof useUnboundBucketsByDatasourceSuspenseQuery>;
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
      resourceUri {
        baseUri
        path
      }
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
export function useDataSourceQuery(baseOptions: Apollo.QueryHookOptions<DataSourceQuery, DataSourceQueryVariables> & ({ variables: DataSourceQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataSourceQuery, DataSourceQueryVariables>(DataSourceDocument, options);
      }
export function useDataSourceLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataSourceQuery, DataSourceQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataSourceQuery, DataSourceQueryVariables>(DataSourceDocument, options);
        }
// @ts-ignore
export function useDataSourceSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DataSourceQuery, DataSourceQueryVariables>): Apollo.UseSuspenseQueryResult<DataSourceQuery, DataSourceQueryVariables>;
export function useDataSourceSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataSourceQuery, DataSourceQueryVariables>): Apollo.UseSuspenseQueryResult<DataSourceQuery | undefined, DataSourceQueryVariables>;
export function useDataSourceSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataSourceQuery, DataSourceQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DataSourceQuery, DataSourceQueryVariables>(DataSourceDocument, options);
        }
export type DataSourceQueryHookResult = ReturnType<typeof useDataSourceQuery>;
export type DataSourceLazyQueryHookResult = ReturnType<typeof useDataSourceLazyQuery>;
export type DataSourceSuspenseQueryHookResult = ReturnType<typeof useDataSourceSuspenseQuery>;
export type DataSourceQueryResult = Apollo.QueryResult<DataSourceQuery, DataSourceQueryVariables>;
export const CreateDatasourceConnectionDocument = gql`
    mutation createDatasourceConnection($name: String!, $description: String, $schedulable: Boolean!, $scheduling: String!, $jsonConfig: String, $pluginDriverId: BigInteger!, $pipeline: PipelineWithItemsDTOInput, $pipelineId: BigInteger, $reindexable: Boolean!, $reindexing: String!, $purgeable: Boolean!, $purging: String!, $purgeMaxAge: String!, $dataIndex: DataIndexDTOInput!, $knnIndex: Boolean, $chunkType: ChunkType, $chunkWindowSize: Int, $embeddingJsonConfig: String, $embeddingDocTypeFieldId: BigInteger, $docTypeIds: [BigInteger]) {
  createDatasourceConnection(
    datasourceConnection: {name: $name, description: $description, schedulable: $schedulable, scheduling: $scheduling, jsonConfig: $jsonConfig, pluginDriverId: $pluginDriverId, pipeline: $pipeline, pipelineId: $pipelineId, reindexable: $reindexable, reindexing: $reindexing, purgeable: $purgeable, purging: $purging, purgeMaxAge: $purgeMaxAge, dataIndex: $dataIndex, knnIndex: $knnIndex, chunkType: $chunkType, chunkWindowSize: $chunkWindowSize, embeddingJsonConfig: $embeddingJsonConfig, embeddingDocTypeFieldId: $embeddingDocTypeFieldId, docTypeIds: $docTypeIds}
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
 *      knnIndex: // value for 'knnIndex'
 *      chunkType: // value for 'chunkType'
 *      chunkWindowSize: // value for 'chunkWindowSize'
 *      embeddingJsonConfig: // value for 'embeddingJsonConfig'
 *      embeddingDocTypeFieldId: // value for 'embeddingDocTypeFieldId'
 *      docTypeIds: // value for 'docTypeIds'
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
          reindex
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
export function useQDatasourceSchedulersQuery(baseOptions: Apollo.QueryHookOptions<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables> & ({ variables: QDatasourceSchedulersQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>(QDatasourceSchedulersDocument, options);
      }
export function useQDatasourceSchedulersLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>(QDatasourceSchedulersDocument, options);
        }
// @ts-ignore
export function useQDatasourceSchedulersSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>): Apollo.UseSuspenseQueryResult<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>;
export function useQDatasourceSchedulersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>): Apollo.UseSuspenseQueryResult<QDatasourceSchedulersQuery | undefined, QDatasourceSchedulersQueryVariables>;
export function useQDatasourceSchedulersSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<QDatasourceSchedulersQuery, QDatasourceSchedulersQueryVariables>(QDatasourceSchedulersDocument, options);
        }
export type QDatasourceSchedulersQueryHookResult = ReturnType<typeof useQDatasourceSchedulersQuery>;
export type QDatasourceSchedulersLazyQueryHookResult = ReturnType<typeof useQDatasourceSchedulersLazyQuery>;
export type QDatasourceSchedulersSuspenseQueryHookResult = ReturnType<typeof useQDatasourceSchedulersSuspenseQuery>;
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
export function useDataSourceInformationQuery(baseOptions: Apollo.QueryHookOptions<DataSourceInformationQuery, DataSourceInformationQueryVariables> & ({ variables: DataSourceInformationQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<DataSourceInformationQuery, DataSourceInformationQueryVariables>(DataSourceInformationDocument, options);
      }
export function useDataSourceInformationLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<DataSourceInformationQuery, DataSourceInformationQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<DataSourceInformationQuery, DataSourceInformationQueryVariables>(DataSourceInformationDocument, options);
        }
// @ts-ignore
export function useDataSourceInformationSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<DataSourceInformationQuery, DataSourceInformationQueryVariables>): Apollo.UseSuspenseQueryResult<DataSourceInformationQuery, DataSourceInformationQueryVariables>;
export function useDataSourceInformationSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataSourceInformationQuery, DataSourceInformationQueryVariables>): Apollo.UseSuspenseQueryResult<DataSourceInformationQuery | undefined, DataSourceInformationQueryVariables>;
export function useDataSourceInformationSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<DataSourceInformationQuery, DataSourceInformationQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<DataSourceInformationQuery, DataSourceInformationQueryVariables>(DataSourceInformationDocument, options);
        }
export type DataSourceInformationQueryHookResult = ReturnType<typeof useDataSourceInformationQuery>;
export type DataSourceInformationLazyQueryHookResult = ReturnType<typeof useDataSourceInformationLazyQuery>;
export type DataSourceInformationSuspenseQueryHookResult = ReturnType<typeof useDataSourceInformationSuspenseQuery>;
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
// @ts-ignore
export function useEnrichPipelineOptionsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>;
export function useEnrichPipelineOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichPipelineOptionsQuery | undefined, EnrichPipelineOptionsQueryVariables>;
export function useEnrichPipelineOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<EnrichPipelineOptionsQuery, EnrichPipelineOptionsQueryVariables>(EnrichPipelineOptionsDocument, options);
        }
export type EnrichPipelineOptionsQueryHookResult = ReturnType<typeof useEnrichPipelineOptionsQuery>;
export type EnrichPipelineOptionsLazyQueryHookResult = ReturnType<typeof useEnrichPipelineOptionsLazyQuery>;
export type EnrichPipelineOptionsSuspenseQueryHookResult = ReturnType<typeof useEnrichPipelineOptionsSuspenseQuery>;
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
        resourceUri {
          baseUri
          path
        }
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
// @ts-ignore
export function useEnrichItemsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<EnrichItemsQuery, EnrichItemsQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichItemsQuery, EnrichItemsQueryVariables>;
export function useEnrichItemsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichItemsQuery, EnrichItemsQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichItemsQuery | undefined, EnrichItemsQueryVariables>;
export function useEnrichItemsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichItemsQuery, EnrichItemsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<EnrichItemsQuery, EnrichItemsQueryVariables>(EnrichItemsDocument, options);
        }
export type EnrichItemsQueryHookResult = ReturnType<typeof useEnrichItemsQuery>;
export type EnrichItemsLazyQueryHookResult = ReturnType<typeof useEnrichItemsLazyQuery>;
export type EnrichItemsSuspenseQueryHookResult = ReturnType<typeof useEnrichItemsSuspenseQuery>;
export type EnrichItemsQueryResult = Apollo.QueryResult<EnrichItemsQuery, EnrichItemsQueryVariables>;
export const PluginDriversDocument = gql`
    query PluginDrivers {
  pluginDriversPageFilter(pageable: {}) {
    content {
      id
      name
      description
      resourceUri {
        baseUri
        path
      }
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
// @ts-ignore
export function usePluginDriversSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<PluginDriversQuery, PluginDriversQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriversQuery, PluginDriversQueryVariables>;
export function usePluginDriversSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriversQuery, PluginDriversQueryVariables>): Apollo.UseSuspenseQueryResult<PluginDriversQuery | undefined, PluginDriversQueryVariables>;
export function usePluginDriversSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PluginDriversQuery, PluginDriversQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<PluginDriversQuery, PluginDriversQueryVariables>(PluginDriversDocument, options);
        }
export type PluginDriversQueryHookResult = ReturnType<typeof usePluginDriversQuery>;
export type PluginDriversLazyQueryHookResult = ReturnType<typeof usePluginDriversLazyQuery>;
export type PluginDriversSuspenseQueryHookResult = ReturnType<typeof usePluginDriversSuspenseQuery>;
export type PluginDriversQueryResult = Apollo.QueryResult<PluginDriversQuery, PluginDriversQueryVariables>;
export const CreateOrUpdatePluginDriverDocument = gql`
    mutation CreateOrUpdatePluginDriver($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $resourceUri: ResourceUriInput, $provisioning: Provisioning) {
  pluginDriver(
    id: $id
    pluginDriverDTO: {name: $name, description: $description, type: $type, resourceUri: $resourceUri, provisioning: $provisioning}
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
 *      resourceUri: // value for 'resourceUri'
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
// @ts-ignore
export function useEnrichPipelinesSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>;
export function useEnrichPipelinesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichPipelinesQuery | undefined, EnrichPipelinesQueryVariables>;
export function useEnrichPipelinesSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<EnrichPipelinesQuery, EnrichPipelinesQueryVariables>(EnrichPipelinesDocument, options);
        }
export type EnrichPipelinesQueryHookResult = ReturnType<typeof useEnrichPipelinesQuery>;
export type EnrichPipelinesLazyQueryHookResult = ReturnType<typeof useEnrichPipelinesLazyQuery>;
export type EnrichPipelinesSuspenseQueryHookResult = ReturnType<typeof useEnrichPipelinesSuspenseQuery>;
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
export function useEnrichPipelinesValueOptionsQuery(baseOptions: Apollo.QueryHookOptions<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables> & ({ variables: EnrichPipelinesValueOptionsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>(EnrichPipelinesValueOptionsDocument, options);
      }
export function useEnrichPipelinesValueOptionsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>(EnrichPipelinesValueOptionsDocument, options);
        }
// @ts-ignore
export function useEnrichPipelinesValueOptionsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>;
export function useEnrichPipelinesValueOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichPipelinesValueOptionsQuery | undefined, EnrichPipelinesValueOptionsQueryVariables>;
export function useEnrichPipelinesValueOptionsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<EnrichPipelinesValueOptionsQuery, EnrichPipelinesValueOptionsQueryVariables>(EnrichPipelinesValueOptionsDocument, options);
        }
export type EnrichPipelinesValueOptionsQueryHookResult = ReturnType<typeof useEnrichPipelinesValueOptionsQuery>;
export type EnrichPipelinesValueOptionsLazyQueryHookResult = ReturnType<typeof useEnrichPipelinesValueOptionsLazyQuery>;
export type EnrichPipelinesValueOptionsSuspenseQueryHookResult = ReturnType<typeof useEnrichPipelinesValueOptionsSuspenseQuery>;
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
export function useEnrichPipelineQuery(baseOptions: Apollo.QueryHookOptions<EnrichPipelineQuery, EnrichPipelineQueryVariables> & ({ variables: EnrichPipelineQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<EnrichPipelineQuery, EnrichPipelineQueryVariables>(EnrichPipelineDocument, options);
      }
export function useEnrichPipelineLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<EnrichPipelineQuery, EnrichPipelineQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<EnrichPipelineQuery, EnrichPipelineQueryVariables>(EnrichPipelineDocument, options);
        }
// @ts-ignore
export function useEnrichPipelineSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<EnrichPipelineQuery, EnrichPipelineQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichPipelineQuery, EnrichPipelineQueryVariables>;
export function useEnrichPipelineSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichPipelineQuery, EnrichPipelineQueryVariables>): Apollo.UseSuspenseQueryResult<EnrichPipelineQuery | undefined, EnrichPipelineQueryVariables>;
export function useEnrichPipelineSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<EnrichPipelineQuery, EnrichPipelineQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<EnrichPipelineQuery, EnrichPipelineQueryVariables>(EnrichPipelineDocument, options);
        }
export type EnrichPipelineQueryHookResult = ReturnType<typeof useEnrichPipelineQuery>;
export type EnrichPipelineLazyQueryHookResult = ReturnType<typeof useEnrichPipelineLazyQuery>;
export type EnrichPipelineSuspenseQueryHookResult = ReturnType<typeof useEnrichPipelineSuspenseQuery>;
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
export function useAssociatedEnrichPipelineEnrichItemsQuery(baseOptions: Apollo.QueryHookOptions<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables> & ({ variables: AssociatedEnrichPipelineEnrichItemsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>(AssociatedEnrichPipelineEnrichItemsDocument, options);
      }
export function useAssociatedEnrichPipelineEnrichItemsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>(AssociatedEnrichPipelineEnrichItemsDocument, options);
        }
// @ts-ignore
export function useAssociatedEnrichPipelineEnrichItemsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>): Apollo.UseSuspenseQueryResult<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>;
export function useAssociatedEnrichPipelineEnrichItemsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>): Apollo.UseSuspenseQueryResult<AssociatedEnrichPipelineEnrichItemsQuery | undefined, AssociatedEnrichPipelineEnrichItemsQueryVariables>;
export function useAssociatedEnrichPipelineEnrichItemsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>(AssociatedEnrichPipelineEnrichItemsDocument, options);
        }
export type AssociatedEnrichPipelineEnrichItemsQueryHookResult = ReturnType<typeof useAssociatedEnrichPipelineEnrichItemsQuery>;
export type AssociatedEnrichPipelineEnrichItemsLazyQueryHookResult = ReturnType<typeof useAssociatedEnrichPipelineEnrichItemsLazyQuery>;
export type AssociatedEnrichPipelineEnrichItemsSuspenseQueryHookResult = ReturnType<typeof useAssociatedEnrichPipelineEnrichItemsSuspenseQuery>;
export type AssociatedEnrichPipelineEnrichItemsQueryResult = Apollo.QueryResult<AssociatedEnrichPipelineEnrichItemsQuery, AssociatedEnrichPipelineEnrichItemsQueryVariables>;
export const UnassociatedEnrichPipelineEnrichItemsDocument = gql`
    query UnassociatedEnrichPipelineEnrichItems($enrichPipelineId: ID!, $searchText: String) {
  enrichPipeline(id: $enrichPipelineId) {
    id
    enrichItems(searchText: $searchText, not: true, first: 1000) {
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
export function useUnassociatedEnrichPipelineEnrichItemsQuery(baseOptions: Apollo.QueryHookOptions<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables> & ({ variables: UnassociatedEnrichPipelineEnrichItemsQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>(UnassociatedEnrichPipelineEnrichItemsDocument, options);
      }
export function useUnassociatedEnrichPipelineEnrichItemsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>(UnassociatedEnrichPipelineEnrichItemsDocument, options);
        }
// @ts-ignore
export function useUnassociatedEnrichPipelineEnrichItemsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>): Apollo.UseSuspenseQueryResult<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>;
export function useUnassociatedEnrichPipelineEnrichItemsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>): Apollo.UseSuspenseQueryResult<UnassociatedEnrichPipelineEnrichItemsQuery | undefined, UnassociatedEnrichPipelineEnrichItemsQueryVariables>;
export function useUnassociatedEnrichPipelineEnrichItemsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<UnassociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQueryVariables>(UnassociatedEnrichPipelineEnrichItemsDocument, options);
        }
export type UnassociatedEnrichPipelineEnrichItemsQueryHookResult = ReturnType<typeof useUnassociatedEnrichPipelineEnrichItemsQuery>;
export type UnassociatedEnrichPipelineEnrichItemsLazyQueryHookResult = ReturnType<typeof useUnassociatedEnrichPipelineEnrichItemsLazyQuery>;
export type UnassociatedEnrichPipelineEnrichItemsSuspenseQueryHookResult = ReturnType<typeof useUnassociatedEnrichPipelineEnrichItemsSuspenseQuery>;
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