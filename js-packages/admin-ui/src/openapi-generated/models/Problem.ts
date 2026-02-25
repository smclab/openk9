/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

/**
 * Problem details for HTTP APIs (rfc7807)
 */
export type Problem = {
    /**
     * An absolute URI that identifies the problem type.
     */
    type?: string;
    title?: string;
    status?: number;
    detail?: string;
    /**
     * An absolute URI that identifies the specific occurrence of the problem.
     */
    instance?: string;
};
