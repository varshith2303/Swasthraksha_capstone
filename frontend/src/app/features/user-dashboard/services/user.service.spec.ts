/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';
import { environment } from '../../../../environments/environment';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all policies', () => {
    service.getAllPolicies().subscribe(res => expect(res).toEqual([]));
    const req = httpMock.expectOne(`${environment.apiUrl}/policies`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should apply for policy', () => {
    const body = { policyCode: 'P1', requestedCoverage: 10000, duration: 1, members: [] };
    service.applyForPolicy(body as any).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/applications`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ id: 1 });
  });

  it('should submit claim', () => {
    const body = { policyNumber: 'PN-1', claimAmount: 1000, hospitalName: 'H', claimReason: 'R', admissionDate: '2026-01-01', dischargeDate: '2026-01-02' };
    service.submitClaim(body as any).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/claims`);
    expect(req.request.method).toBe('POST');
    req.flush({ claimNumber: 'CLM-1' });
  });

  it('should upload application document', () => {
    const file = new File(['x'], 'a.pdf', { type: 'application/pdf' });
    service.uploadApplicationDocument('APP-1', 'ID_PROOF', file).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/api/documents/upload/application`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({ id: 1 });
  });
});
