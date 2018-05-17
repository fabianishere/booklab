import {isUndefined} from "util";

export class Book {
    constructor(public titles: Title[],
                public authors: string[],
                public ids: string[],
                public isSearched: boolean = false) {
    }
    static getBook(b: any): Book {
        if(isUndefined(b)) {
            return new Book([new Title("Didn't find your book!", 'MAIN')], [''], [''])
        }
        return new Book(b.titles, b.authors, b.ids)
    }
    getMainTitle(): string {
        if(this.titles.length == 0) {
            return '';
        }
        else {
            const res = this.titles.find(t => t.type=="MAIN");
            return isUndefined(res)? '' : res.value;
        }
    }
}

export class Title {
    constructor(public value: string,
                public type: string) {
    }
}
