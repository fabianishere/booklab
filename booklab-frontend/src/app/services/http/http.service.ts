import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {Book, DetectionResult, Secure} from '../../dataTypes';
import {catchError} from 'rxjs/operators';
import {Router} from '@angular/router';

interface Success {
    success: boolean;
}

@Injectable()
export class HttpService {

    constructor(private http: HttpClient, private router: Router) {
    }

    checkHealth() {
        this.http.get('http://localhost:8080/api/health').subscribe((res: Success) => {
            console.log(res.success);
        });
    }

    putImg(img: Blob): Observable<DetectionResult> {
        return this.http.put<DetectionResult>('http://localhost:8080/api/detection', img);
    }

    findBook(nameInput: string, authorInput: string): Observable<DetectionResult> {

        return this.http.get<DetectionResult>('http://localhost:8080/api/search?'
            + 'title=' + Secure.checkInput(nameInput)
            + '&author=' + Secure.checkInput(authorInput));

    }

    handleError(error) {
        this.router.navigate(['/sorry']);
    }
}
