import {isUndefined} from 'util';


export class Title {
    constructor(public value: string,
                public type: string) {
    }
}

export class Book {
    constructor(public titles: Title[],
                public authors: string[],
                public ids: string[],
                public isSearched: boolean = false) {
    }

    static getBook(b: any): Book {
        if (isUndefined(b)) {
            return new Book([new Title('Didn\'t find your book!', 'MAIN')], [''], ['']);
        }
        return new Book(b.titles, b.authors, b.ids);
    }

    getMainTitle(): string {
        if (this.titles.length === 0) {
            return '';
        }
        const res = this.titles.find(t => t.type === 'MAIN');
        return isUndefined(res) ? '' : res.value;
    }
}


export interface DetectionResult {
    results: Book[];
}

export class Secure {

    static checkInput(input: string):string {
        let res: string;
        res = input.replace('%0d%0a', '');
        return res;
    }

    constructor () {
}
}
