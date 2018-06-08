import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {BookDetection, Response, HealthCheck, isFailure, Secure, Book, User} from '../../dataTypes';
import 'rxjs/add/operator/map'
import {Router} from '@angular/router';
import {environment} from '../../../environments/environment';
import {OAuthService} from "angular-oauth2-oidc";
import 'rxjs/add/operator/map'
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
            params: {title: title, author: author}
        })
            .map(res => {
                if (isFailure(res))
                    throw res;
                return res.data
            });
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

                return this.http.post<User>(`${environment.apiUrl}/users`, body, { headers: headers });
            });
    }

    handleError(error) {
        this.router.navigate(['/sorry']);
    }
}
