/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventPluginDriver } from '../models/K9EntityEventPluginDriver';
import type { PagePluginDriver } from '../models/PagePluginDriver';
import type { PluginDriver } from '../models/PluginDriver';
import type { PluginDriverDTO } from '../models/PluginDriverDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class PluginDriverFieldResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PagePluginDriver OK
     * @throws ApiError
     */
    public getPluginDrivers(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PagePluginDriver> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/plugin-drivers',
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
     * @returns PluginDriver OK
     * @throws ApiError
     */
    public postPluginDrivers(
requestBody?: PluginDriverDTO,
): CancelablePromise<PluginDriver> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/plugin-drivers',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getPluginDriversCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/plugin-drivers/count',
        });
    }

    /**
     * @returns K9EntityEventPluginDriver OK
     * @throws ApiError
     */
    public getPluginDriversStream(): CancelablePromise<Array<K9EntityEventPluginDriver>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/plugin-drivers/stream',
        });
    }

    /**
     * @param id 
     * @returns PluginDriver OK
     * @throws ApiError
     */
    public getPluginDrivers1(
id: number,
): CancelablePromise<PluginDriver> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/plugin-drivers/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns PluginDriver OK
     * @throws ApiError
     */
    public putPluginDrivers(
id: number,
requestBody?: PluginDriverDTO,
): CancelablePromise<PluginDriver> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/plugin-drivers/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns PluginDriver OK
     * @throws ApiError
     */
    public deletePluginDrivers(
id: number,
): CancelablePromise<PluginDriver> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/plugin-drivers/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns PluginDriver OK
     * @throws ApiError
     */
    public patchPluginDrivers(
id: number,
requestBody?: PluginDriverDTO,
): CancelablePromise<PluginDriver> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/plugin-drivers/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}
