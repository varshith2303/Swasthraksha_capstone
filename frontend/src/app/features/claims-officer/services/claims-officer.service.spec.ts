/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ClaimsOfficerService } from './claims-officer.service';
import { environment } from '../../../../environments/environment';

describe('ClaimsOfficerService', () => {
  let service: ClaimsOfficerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(ClaimsOfficerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get assigned claims', () => {
    service.getAssignedClaims().subscribe(res => expect(res).toEqual([]));
    const req = httpMock.expectOne(`${environment.apiUrl}/claims/assigned`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should verify claim with approve parameter', () => {
    service.verifyClaim('CLM-22', false).subscribe(res => expect(res).toBe('done'));
    const req = httpMock.expectOne(r => r.url === `${environment.apiUrl}/claims/CLM-22/verify` && r.params.get('approve') === 'false');
    expect(req.request.method).toBe('POST');
    req.flush('done');
  });
});
