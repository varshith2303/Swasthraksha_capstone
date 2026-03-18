/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DocumentService } from './document.service';
import { environment } from '../../environments/environment';

describe('DocumentService', () => {
  let service: DocumentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(DocumentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch application documents', () => {
    const mockDocs = [{ id: 1, fileName: 'a.pdf', filePath: '/a', fileType: 'application/pdf', documentType: 'ID_PROOF', uploadedDate: '2026-01-01' } as any];

    service.getApplicationDocuments(10).subscribe(docs => {
      expect(docs.length).toBe(1);
      expect(docs[0].id).toBe(1);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/documents/application/10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockDocs);
  });

  it('should fetch claim documents', () => {
    service.getClaimDocuments(20).subscribe(docs => {
      expect(docs).toEqual([]);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/documents/claim/20`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should return download url', () => {
    expect(service.getDownloadUrl(7)).toBe(`${environment.apiUrl}/api/documents/7`);
  });

  it('should download document blob', () => {
    const blob = new Blob(['x'], { type: 'text/plain' });
    service.downloadDocument(7).subscribe(res => {
      expect(res).toEqual(blob);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/documents/7`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(blob);
  });
});
