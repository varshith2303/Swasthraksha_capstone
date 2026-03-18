/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UnderwriterService } from './underwriter.service';
import { environment } from '../../../../environments/environment';

describe('UnderwriterService', () => {
  let service: UnderwriterService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(UnderwriterService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch assigned applications', () => {
    service.getAssignedApplications().subscribe(res => expect(res).toEqual([]));
    const req = httpMock.expectOne(`${environment.apiUrl}/applications/assigned`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should fetch pending applications', () => {
    service.getPendingApplications().subscribe(res => expect(res).toEqual([]));
    const req = httpMock.expectOne(`${environment.apiUrl}/applications/pending`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should update application decision', () => {
    service.updateApplication(1, { status: 'APPROVED', finalPremium: 1234 }).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/applications/1`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'APPROVED', finalPremium: 1234 });
    req.flush({ id: 1 });
  });
});
