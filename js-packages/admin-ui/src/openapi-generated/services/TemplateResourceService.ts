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
    public getTemplates(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDocTypeTemplate> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/templates',
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
    public postTemplates(
requestBody?: DocTypeTemplateDTO,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/templates',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getTemplatesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/templates/count',
        });
    }

    /**
     * @returns K9EntityEventDocTypeTemplate OK
     * @throws ApiError
     */
    public getTemplatesStream(): CancelablePromise<Array<K9EntityEventDocTypeTemplate>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/templates/stream',
        });
    }

    /**
     * @param id 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public getTemplates1(
id: number,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/templates/{id}',
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
    public putTemplates(
id: number,
requestBody?: DocTypeTemplateDTO,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/templates/{id}',
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
    public deleteTemplates(
id: number,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/templates/{id}',
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
    public patchTemplates(
id: number,
requestBody?: DocTypeTemplateDTO,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/templates/{id}',
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
    public getTemplatesCompiled(
id: number,
): CancelablePromise<string> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/templates/{id}/compiled',
            path: {
                'id': id,
            },
        });
    }

}
