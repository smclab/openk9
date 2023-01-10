/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DocTypeTemplate } from '../models/DocTypeTemplate';
import type { DocTypeTemplateDTO } from '../models/DocTypeTemplateDTO';
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventDocTypeTemplate } from '../models/K9EntityEventDocTypeTemplate';
import type { PageDocTypeTemplate } from '../models/PageDocTypeTemplate';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TemplateResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageDocTypeTemplate OK
     * @throws ApiError
     */
    public getApiDatasourceTemplates(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDocTypeTemplate> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates',
            query: {
                'after_id': afterId,
                'before_id': beforeId,
                'limit': limit,
                'searchText': searchText,
                'sortBy': sortBy,
            },
        });
    }

    /**
     * @param requestBody 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public postApiDatasourceTemplates(
requestBody?: DocTypeTemplateDTO,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/templates',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getApiDatasourceTemplatesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates/count',
        });
    }

    /**
     * @returns K9EntityEventDocTypeTemplate OK
     * @throws ApiError
     */
    public getApiDatasourceTemplatesStream(): CancelablePromise<Array<K9EntityEventDocTypeTemplate>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates/stream',
        });
    }

    /**
     * @param id 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public getApiDatasourceTemplates1(
id: number,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public putApiDatasourceTemplates(
id: number,
requestBody?: DocTypeTemplateDTO,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/templates/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public deleteApiDatasourceTemplates(
id: number,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/templates/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public patchApiDatasourceTemplates(
id: number,
requestBody?: DocTypeTemplateDTO,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/datasource/templates/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns string OK
     * @throws ApiError
     */
    public getApiDatasourceTemplatesCompiled(
id: number,
): CancelablePromise<string> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates/{id}/compiled',
            path: {
                'id': id,
            },
        });
    }

}
