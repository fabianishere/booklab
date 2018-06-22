import {AfterViewChecked, AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Subject} from "rxjs/Rx";
import {HttpService} from "../../services/http/http.service";
import {Observable} from "rxjs/Observable";
import {BookDetection, BookItem, Box} from "../../interfaces/user";

@Component({
    selector: 'app-image-search',
    templateUrl: './image-search.component.html',
    styleUrls: ['./image-search.component.less']
})
export class ImageSearchComponent implements OnInit, AfterViewInit, AfterViewChecked {

    @ViewChild("imageCanvas") imageCanvas: ElementRef;
    @ViewChild("drawingCanvas") drawingCanvas: ElementRef;
    public imageContext: CanvasRenderingContext2D;
    public drawingContext: CanvasRenderingContext2D;
    public drawn: boolean = false;

    public img = null;
    public searching = false;

    constructor(private http: HttpService) {

    }

    ngOnInit() {
    }

    ngAfterViewInit() {
        this.imageContext = (this.imageCanvas.nativeElement as HTMLCanvasElement).getContext('2d');
        this.drawingContext = (this.drawingCanvas.nativeElement as HTMLCanvasElement).getContext('2d');
    }

    ngAfterViewChecked() {
        const element = document.getElementById('image');
        if (element && !this.drawn) {
            this.drawImage();
            this.drawn = true;
        }
    }

    /**
     * Invoked by the image upload button, sends the image from the event to the backend for processing.
     * @param img
     */
    submit(img: File): Observable<BookItem[]> {
        const subject = new Subject<BookItem[]>();
        const reader = new FileReader();
        reader.readAsDataURL(img);
        reader.onload = () => {
            this.img = reader.result;
        };
        this.http.checkHealth();
        this.searching = true;
        this.http.putImg(img).subscribe((res) => {
            this.searching = false;
            const books = res
                .filter(b => b.matches.length > 0)
                .map((b: BookDetection) => new BookItem(b.matches[0], true, false, b.box));
            subject.next(books);
        }, error => {
            this.searching = false;
            this.http.handleError(error)
        });
        return subject;
    }

    /**
     * Draws the uploaded image on the image canvas and sets the size of the drawing canvas to the image size.
     */
    drawImage() {
        let image = document.getElementById("image") as HTMLImageElement;
        let container = document.getElementById("container");
        image.onload = () => {
            this.imageContext.canvas.width = image.width;
            this.imageContext.canvas.height = image.height;
            this.drawingContext.canvas.width = image.width;
            this.drawingContext.canvas.height = image.height;
            container.style.height = document.getElementById("imageCanvas").clientHeight.toString() + 'px';
            this.imageContext.drawImage(image, 0, 0);
        }
    }

    /**
     * Draws the box on the drawing canvas
     * @param {Box} box the box to be drawn
     */
    drawBox(box: Box) {
        this.drawingContext.fillStyle = 'rgba(0, 255, 0, 0.5)';
        this.drawingContext.fillRect(box.x, box.y, box.width, box.height);
        this.drawingContext.strokeStyle = 'rgba(0, 255, 0, 1)';
        this.drawingContext.lineWidth = 5;
        this.drawingContext.strokeRect(box.x, box.y, box.width, box.height);

    }

    /**
     * Clears the box from the drawing canvas
     * @param {Box} box the box to be cleared
     */
    clearBox(box: Box) {
        const offset = this.drawingContext.lineWidth;
        this.drawingContext.clearRect(box.x - offset, box.y - offset, box.width + 2 * offset, box.height + 2 * offset);
    }
}
