import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {Book, DetectionResult, Secure} from '../../dataTypes';
import {catchError} from 'rxjs/operators';
import {Router} from '@angular/router';

/**
 * Interface to model health response from api.
 */
interface Success {
    success: boolean;
}

/**
 * Service to handle all http requests and interactions with the backend.
 */
@Injectable()
export class HttpService {

    /**
     * Constructor for HttpService
     * @param {HttpClient} http The HTTP client to use.
     * @param {Router} router The Angular router to use.
     */
    constructor(private http: HttpClient, private router: Router) {}
    /**
     * Checks if the backend is running.
     */
    checkHealth() {
        this.http.get('http://localhost:8080/api/health').subscribe((res: Success) => {
            console.log(res.success);
        });
    }

    /**
     * Sends an image to the backend for processing.
     * @param {Blob} img
     * @returns {Observable<DetectionResult>}: result of the backend processing
     */
    putImg(img: Blob): Observable<DetectionResult> {
        return this.http.post<DetectionResult>('http://localhost:8080/api/detection', img);
    }

    /**
     * Finds a book with the given title and author.
     * @param {string} nameInput
     * @param {string} authorInput
     * @returns {Observable<DetectionResult>}: result of the backend search
     */
    findBook(nameInput: string, authorInput: string): Observable<DetectionResult> {

        return this.http.get<DetectionResult>('http://localhost:8080/api/search?'
            + 'title=' + Secure.checkInput(nameInput)
            + '&author=' + Secure.checkInput(authorInput));

    }

    handleError(error) {
        this.router.navigate(['/sorry']);
    }
}
