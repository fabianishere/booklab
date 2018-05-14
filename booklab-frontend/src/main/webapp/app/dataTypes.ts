export class MockBook {
    constructor(public isbn: string,
                public title: string,
                public isSearched: boolean = false) {
    }
}

export class Book {
    constructor(public titles: Title[],
                public authors: string[],
                public ids: string[]) {
    }
}

export class Title {
    constructor(public value: string,
                public type: string) {
    }
}
