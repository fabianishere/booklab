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
 * Class to model a book title.
 */
export class Title {

    /**
     * Constructor for Title.
     * @param {string} value
     * @param {string} type
     */
    constructor(public value: string,
                public type: string) {
    }
}

/**
 * Class to model a book.
 */
export class Book {

    /**
     * Constructor for Book.
     * @param {Title[]} titles
     * @param {string[]} authors
     * @param {string[]} ids
     * @param {boolean} isSearched
     */
    constructor(public titles: Title[],
                public authors: string[],
                public ids: string[],
                public isSearched: boolean = false) {
    }

    /**
     * Returns a new book with given data.
     * @param b container for data in the book
     * @returns {Book} a new Book
     */
    static getBook(b: any): Book {
        if (isUndefined(b)) {
            return new Book([new Title('Didn\'t find your book!', 'MAIN')], [''], ['']);
        }
        return new Book(b.titles, b.authors, b.ids);
    }

    static create(title: string, author: string, id: string): Book {
        return new Book([new Title(title, 'MAIN')], [author], [id]);
    }


    /**
     * Searches the main title of the book.
     * @returns {string} result of the search, string is empty if the book didn't have a main title
     */
    getMainTitle(): string {
        if (this.titles.length === 0) {
            return '';
        }
        const res = this.titles.find(t => t.type === 'MAIN');
        return isUndefined(res) ? '' : res.value;
    }

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

export class BookItem {
    constructor(public book: Book,
                public checked: boolean = true,
                public addedToShelf = false) {
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
