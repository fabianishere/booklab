import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {MockBook} from "../../dataTypes";

interface Success {
    success: boolean;
}

@Injectable()
export class HttpService {

    constructor(private http: HttpClient) {
    }

    checkHealth() {
        this.http.get('http://localhost:8080/api/health').subscribe((res: Success) => {
            console.log(res.success);
        });
    }

    putImg(img: Blob): Observable<Object> {
        return this.http.put('http://localhost:8080/api/detection', img);
    }

    findBook(nameInput: string, authorInput: string): Observable<Object> {
        return this.http.get('http://localhost:8080/api/search?title='+ nameInput + '&author=' + authorInput);
    }
}
