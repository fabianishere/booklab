import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {BookDetection, Response, HealthCheck, isFailure, Secure, Book} from '../../dataTypes';
import {Router} from '@angular/router';
import { environment } from '../../../environments/environment';
import 'rxjs/add/operator/map'

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
        this.http.get(`${environment.apiUrl}/health`).subscribe((res: Response<HealthCheck>) => {
            if (!isFailure(res)) {
                console.log("Health check succeeded!");
            } else {
                console.log("Health check failed!");
            }
        });
    }

    /**
     * Sends an image to the backend for processing.
     * @param {Blob} img
     * @returns {Observable<BookDetection[]>}: result of the backend processing
     */
    putImg(img: Blob): Observable<BookDetection[]> {
        return this.http.post<Response<BookDetection[]>>(`${environment.apiUrl}/detection`, img)
            .map(res => {
                if (isFailure(res))
                    throw res;
                return res.data;
            });
    }

    /**
     * Finds a collection with the given title and author.
     * @param {string} nameInput
     * @param {string} authorInput
     * @returns {Observable<Book[]>}: result of the backend search
     */
    findBook(nameInput: string, authorInput: string): Observable<Book[]> {
        let title = Secure.checkInput(nameInput);
        let author = Secure.checkInput(authorInput);
        return this.http.get<Response<Book[]>>(`${environment.apiUrl}/catalogue/`, {
            params: { title: title, author: author }
        })
            .map(res => {
                if (isFailure(res))
                    throw res;
                return res.data
            });
    }

    handleError(error) {
        this.router.navigate(['/sorry']);
    }
}
