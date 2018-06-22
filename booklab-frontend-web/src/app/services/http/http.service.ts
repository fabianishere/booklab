import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {BookDetection, Response, HealthCheck, isFailure, Secure, Book, User} from '../../dataTypes';
import 'rxjs/add/operator/map'
import {Router} from '@angular/router';
import {environment} from '../../../environments/environment';
import {OAuthService} from "angular-oauth2-oidc";
import 'rxjs/add/observable/throw';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/mergeMap';

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
    constructor(private http: HttpClient, private router: Router, private oauth: OAuthService) {}

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
            .pipe(extract);
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
            params: {title: title, author: author}
        }).pipe(extract);
    }

    /**
     * Register a user with the given email and password.
     * @param {string} email The email address of the user.
     * @param {string} password The password of the user.
     * @returns {Observable<User>}: result of the registration request.
     */
    register(email: string, password: string): Observable<User> {
        const params = new HttpParams()
            .set('grant_type', 'client_credentials')
            .set('client_id', this.oauth.clientId)
            .set('client_secret', this.oauth.dummyClientSecret)
            .set('scope', 'user:registration');

        const headers = new HttpHeaders({
            'Content-Type':  'application/x-www-form-urlencoded'
        });

        return this.http.post<any>(`${environment.apiUrl}/auth/token`, params, { headers: headers })
            .map(res => res.access_token)
            .mergeMap(token => {
                const headers = new HttpHeaders({ 'Authorization' : `Bearer ${token}`});
                const body = { email : email, password : password};

                return this.http.post<Response<User>>(`${environment.apiUrl}/users`, body, { headers: headers });
            })
            .pipe(extract);
    }

    /**
     * Get the recommendations for the given books.
     * @param collection The collection of books.
     * @param candidates The list of candidate books.
     * @returns {Observable<book[]>}
     */
    getRecommendations(collection: Book[], candidates: Book[]): Observable<Book[]> {
        return this.http.post<Response<Book[]>>(`${environment.apiUrl}/recommendations`, {
            collection: collection.map(book => book.id),
            candidates: candidates.map(book => book.id)
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

/**
 * Extract the response from the envelope
 */
function extract<T>(source: Observable<Response<T>>): Observable<T> {
    return source
        .map(res => {
            if (isFailure(res))
                throw res.error;
            return res.data;
        })
        .catch(error => {
            if (error.error && error.error.error) {
                return Observable.throwError(error.error.error);
            }
            return Observable.throwError(error);
        });
}
