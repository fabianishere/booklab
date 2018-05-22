import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {DetectionResult} from '../../dataTypes';

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
     * @param {HttpClient} http
     */
    constructor(private http: HttpClient) {
    }

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
        return this.http.put<DetectionResult>('http://localhost:8080/api/detection', img);
    }

    /**
     * Finds a book with the given title and author.
     * @param {string} nameInput
     * @param {string} authorInput
     * @returns {Observable<DetectionResult>}: result of the backend search
     */
    findBook(nameInput: string, authorInput: string): Observable<DetectionResult> {
        return this.http.get<DetectionResult>('http://localhost:8080/api/search?title=' + nameInput + '&author=' + authorInput);
    }
}
