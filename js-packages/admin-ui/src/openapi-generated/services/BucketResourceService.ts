/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { Bucket } from "../models/Bucket";
import type { BucketDTO } from "../models/BucketDTO";
import type { BucketResponse } from "../models/BucketResponse";
import type { K9Column } from "../models/K9Column";
import type { K9EntityEventBucket } from "../models/K9EntityEventBucket";
import type { PageBucket } from "../models/PageBucket";
import type { PageDatasource } from "../models/PageDatasource";
import type { PageSuggestionCategory } from "../models/PageSuggestionCategory";
import type { PartialDocTypeFieldDTO } from "../models/PartialDocTypeFieldDTO";
import type { SuggestionCategory } from "../models/SuggestionCategory";
import type { Tuple2BucketDatasource } from "../models/Tuple2BucketDatasource";
import type { Tuple2BucketSuggestionCategory } from "../models/Tuple2BucketSuggestionCategory";

import type { CancelablePromise } from "../core/CancelablePromise";
import type { BaseHttpRequest } from "../core/BaseHttpRequest";
import { TabResponseDTO } from "../models/TabResponseDto";
import { TemplateResponseDTO } from "../models/TemplateResponseDto";

export class BucketResourceService {
  constructor(public readonly httpRequest: BaseHttpRequest) {}

  /**
   * @param afterId
   * @param beforeId
   * @param limit
   * @param searchText
   * @param sortBy
   * @returns PageBucket OK
   * @throws ApiError
   */
  public getApiDatasourceBuckets(
    afterId: number = -1,
    beforeId: number = -1,
    limit: number = 20,
    searchText?: string,
    sortBy?: K9Column
  ): CancelablePromise<PageBucket> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets",
      query: {
        after_id: afterId,
        before_id: beforeId,
        limit: limit,
        searchText: searchText,
        sortBy: sortBy,
      },
    });
  }

  /**
   * @param requestBody
   * @returns Bucket OK
   * @throws ApiError
   */
  public postApiDatasourceBuckets(requestBody?: BucketDTO): CancelablePromise<Bucket> {
    return this.httpRequest.request({
      method: "POST",
      url: "/api/datasource/buckets",
      body: requestBody,
      mediaType: "application/json",
    });
  }

  /**
   * @returns number OK
   * @throws ApiError
   */
  public getApiDatasourceBucketsCount(): CancelablePromise<number> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/count",
    });
  }

  /**
   * @returns BucketResponse OK
   * @throws ApiError
   */
  public getApiDatasourceBucketsCurrent(): CancelablePromise<BucketResponse> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/current",
    });
  }

  /**
   * @returns PartialDocTypeFieldDTO OK
   * @throws ApiError
   */
  public getApiDatasourceBucketsCurrentDocTypeFieldsSortable(): CancelablePromise<Array<PartialDocTypeFieldDTO>> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/current/doc-type-fields-sortable",
    });
  }

  /**
   * @returns SuggestionCategory OK
   * @throws ApiError
   */
  public getApiDatasourceBucketsCurrentSuggestionCategories(): CancelablePromise<Array<SuggestionCategory>> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/current/suggestionCategories",
    });
  }

  /**
   * @returns TabResponseDTO OK
   * @throws ApiError
   */
  public getApiDatasourceBucketsCurrentTabs(): CancelablePromise<Array<TabResponseDTO>> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/current/tabs",
    });
  }

  /**
   * @returns TemplateResponseDTO OK
   * @throws ApiError
   */
  public getApiDatasourceBucketsCurrentTemplates(): CancelablePromise<Array<TemplateResponseDTO>> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/current/templates",
    });
  }

  /**
   * @returns K9EntityEventBucket OK
   * @throws ApiError
   */
  public getApiDatasourceBucketsStream(): CancelablePromise<Array<K9EntityEventBucket>> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/stream",
    });
  }

  /**
   * @param id
   * @returns Bucket OK
   * @throws ApiError
   */
  public getApiDatasourceBuckets1(id: number): CancelablePromise<Bucket> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/{id}",
      path: {
        id: id,
      },
    });
  }

  /**
   * @param id
   * @param requestBody
   * @returns Bucket OK
   * @throws ApiError
   */
  public putApiDatasourceBuckets(id: number, requestBody?: BucketDTO): CancelablePromise<Bucket> {
    return this.httpRequest.request({
      method: "PUT",
      url: "/api/datasource/buckets/{id}",
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: "application/json",
    });
  }

  /**
   * @param id
   * @returns Bucket OK
   * @throws ApiError
   */
  public deleteApiDatasourceBuckets(id: number): CancelablePromise<Bucket> {
    return this.httpRequest.request({
      method: "DELETE",
      url: "/api/datasource/buckets/{id}",
      path: {
        id: id,
      },
    });
  }

  /**
   * @param id
   * @param requestBody
   * @returns Bucket OK
   * @throws ApiError
   */
  public patchApiDatasourceBuckets(id: number, requestBody?: BucketDTO): CancelablePromise<Bucket> {
    return this.httpRequest.request({
      method: "PATCH",
      url: "/api/datasource/buckets/{id}",
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: "application/json",
    });
  }

  /**
   * @param id
   * @param afterId
   * @param beforeId
   * @param limit
   * @param searchText
   * @param sortBy
   * @returns PageDatasource OK
   * @throws ApiError
   */
  public getApiDatasourceBucketsDatasources(
    id: number,
    afterId: number = -1,
    beforeId: number = -1,
    limit: number = 20,
    searchText?: string,
    sortBy?: K9Column
  ): CancelablePromise<PageDatasource> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/{id}/datasources",
      path: {
        id: id,
      },
      query: {
        after_id: afterId,
        before_id: beforeId,
        limit: limit,
        searchText: searchText,
        sortBy: sortBy,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }

  /**
   * @param datasourceId
   * @param id
   * @returns Tuple2BucketDatasource OK
   * @throws ApiError
   */
  public putApiDatasourceBucketsDatasources(datasourceId: number, id: number): CancelablePromise<Tuple2BucketDatasource> {
    return this.httpRequest.request({
      method: "PUT",
      url: "/api/datasource/buckets/{id}/datasources/{datasourceId}",
      path: {
        datasourceId: datasourceId,
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }

  /**
   * @param datasourceId
   * @param id
   * @returns Tuple2BucketDatasource OK
   * @throws ApiError
   */
  public deleteApiDatasourceBucketsDatasources(datasourceId: number, id: number): CancelablePromise<Tuple2BucketDatasource> {
    return this.httpRequest.request({
      method: "DELETE",
      url: "/api/datasource/buckets/{id}/datasources/{datasourceId}",
      path: {
        datasourceId: datasourceId,
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }

  /**
   * @param id
   * @param afterId
   * @param beforeId
   * @param limit
   * @param searchText
   * @param sortBy
   * @returns PageSuggestionCategory OK
   * @throws ApiError
   */
  public getApiDatasourceBucketsSuggestionCategories(
    id: number,
    afterId: number = -1,
    beforeId: number = -1,
    limit: number = 20,
    searchText?: string,
    sortBy?: K9Column
  ): CancelablePromise<PageSuggestionCategory> {
    return this.httpRequest.request({
      method: "GET",
      url: "/api/datasource/buckets/{id}/suggestion-categories",
      path: {
        id: id,
      },
      query: {
        after_id: afterId,
        before_id: beforeId,
        limit: limit,
        searchText: searchText,
        sortBy: sortBy,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }

  /**
   * @param suggestionCategoryId
   * @param requestBody
   * @returns Tuple2BucketSuggestionCategory OK
   * @throws ApiError
   */
  public putApiDatasourceBucketsSuggestionCategories(
    suggestionCategoryId: number,
    requestBody?: number
  ): CancelablePromise<Tuple2BucketSuggestionCategory> {
    return this.httpRequest.request({
      method: "PUT",
      url: "/api/datasource/buckets/{id}/suggestion-categories/{suggestionCategoryId}",
      path: {
        suggestionCategoryId: suggestionCategoryId,
      },
      body: requestBody,
      mediaType: "application/json",
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }

  /**
   * @param suggestionCategoryId
   * @param requestBody
   * @returns Tuple2BucketSuggestionCategory OK
   * @throws ApiError
   */
  public deleteApiDatasourceBucketsSuggestionCategories(
    suggestionCategoryId: number,
    requestBody?: number
  ): CancelablePromise<Tuple2BucketSuggestionCategory> {
    return this.httpRequest.request({
      method: "DELETE",
      url: "/api/datasource/buckets/{id}/suggestion-categories/{suggestionCategoryId}",
      path: {
        suggestionCategoryId: suggestionCategoryId,
      },
      body: requestBody,
      mediaType: "application/json",
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
