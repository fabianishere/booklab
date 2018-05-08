import { TestBed, inject } from '@angular/core/testing';

import { HttpServiceService } from './http-service.service';

describe('HttpServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HttpServiceService]
    });
  });

  it('should be created', inject([HttpServiceService], (service: HttpServiceService) => {
    expect(service).toBeTruthy();
  }));
});
