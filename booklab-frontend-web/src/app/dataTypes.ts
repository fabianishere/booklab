import {isUndefined} from 'util';

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

/**
 * A user on the website.
 */
export interface User {
    /**
     * The identifier of the user.
     */
    id: number;

    /**
     * The email address of the user.
     */
    email: string;
}

/**
 * A request to register a user.
 */
export interface UserRegistration {
    /**
     * The email of the user.
     */
    email: string;

    /**
     * The chosen password of the user.
     */
    password: string;
}

/**
 * Class to model a book title.
 */
export interface Book {
    /**
     * The internal identifier of the book.
     */
    id: string;

    /**
     * The external identifiers of the book.
     */
    identifiers: any;

    /**
     * The title of the book.
     */
    title: string;

    /**
     * The subtitle of the book.
     */
    subtitle?: string;

    /**
     * The authors of the book.
     */
    authors: string[];

    /**
     * The publisher of the book.
     */
    publisher?: string;

    /**
     * The categories of the book.
     */
    categories: string[];

    /**
     * The date of publishing of the book.
     */
    published?: string;

    /**
     * A description of the book.
     */
    description?: string;

    /**
     * The language of the book.
     */
    language?: string;

    /**
     * The ratings of the book.
     */
    ratings?: {
        average: number;
        count: number;
    }

    /**
     * A map of images of the book.
     */
    images: any;
}

/**
 * A book that has been detected by the server.
 */
export interface BookDetection {
    matches: Book[];
    box: {
        x: number,
        y: number,
        width: number,
        height: number
    }
}

export class Box {
    constructor(public x: number,
                public y: number,
                public width: number,
                public height: number) {
    }
}

export class BookItem {
    constructor(public book: Book,
                public checked: boolean = true,
                public added = false,
                public box: Box = null) {
    }
}

export class Secure {
    static checkInput(input: string): string {
        let res: string;
        res = input.replace('%0d%0a', '');
        return res;
    }

    constructor() {
    }
}
