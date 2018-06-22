/**
 * This type represents a response returned from the API.
 */
export type Response<T> = Success<T> | Failure

/**
 * A document returned by the API.
 */
export interface Document {
    meta: any;
    links: any;
}

/**
 * An interface to represent a successful response from the API.
 */
export interface Success<T> extends Document {
    data: T;
}

/**
 * An interface to represent a failure response from the API.
 */
export interface Failure extends Document {
    error: Error;
}

/**
 * An interface to represent an error from the API.
 */
export interface Error {
    code?: string;
    source?: {
        attributes?: string;
        pointer: string;
    };
    title?: string;
    detail?: string;
}

/**
 * A function to determine whether the returned response is a failure.
 */
export function isFailure<T>(response: Response<T>): response is Failure {
    return (<Failure> response).error !== undefined;
}

/**
 * An interface to represent the health check returned from the API.
 */
export interface HealthCheck {
    success: boolean;
}
