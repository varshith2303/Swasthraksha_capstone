/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { environment } from '../../../../environments/environment';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all policies with adminView=true', () => {
    service.getAllPolicies().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/policies?adminView=true`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should assign application with underwriterEmail query param', () => {
    service.assignApplication(99, 'underwriter@test.com').subscribe();
    const req = httpMock.expectOne(r => r.url === `${environment.apiUrl}/applications/99/assign` && r.params.get('underwriterEmail') === 'underwriter@test.com');
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('should verify claim with approve query param', () => {
    service.verifyClaim('CLM-7', true).subscribe(res => {
      expect(res).toBe('ok');
    });
    const req = httpMock.expectOne(r => r.url === `${environment.apiUrl}/claims/CLM-7/verify` && r.params.get('approve') === 'true');
    expect(req.request.method).toBe('POST');
    req.flush('ok');
  });

  it('should get all issued policies', () => {
    service.getAllIssuedPolicies().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/policyassignments/all`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
