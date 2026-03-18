import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type DocumentType =
    | 'ID_PROOF'
    | 'ADDRESS_PROOF'
    | 'MEDICAL_REPORT'
    | 'HOSPITAL_BILL'
    | 'DISCHARGE_SUMMARY'
    | 'PRESCRIPTION';

export interface DocumentItem {
    id: number;
    fileName: string;
    filePath: string;
    fileType: string;
    documentType: DocumentType;
    uploadedDate: string;
}

@Injectable({
    providedIn: 'root'
})
export class DocumentService {
    private apiUrl = `${environment.apiUrl}/api/documents`;

    constructor(private http: HttpClient) { }

    getApplicationDocuments(applicationId: number): Observable<DocumentItem[]> {
        return this.http.get<DocumentItem[]>(`${this.apiUrl}/application/${applicationId}`);
    }

    getClaimDocuments(claimId: number): Observable<DocumentItem[]> {
        return this.http.get<DocumentItem[]>(`${this.apiUrl}/claim/${claimId}`);
    }

    getDownloadUrl(documentId: number): string {
        return `${this.apiUrl}/${documentId}`;
    }

    downloadDocument(documentId: number): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/${documentId}`, { responseType: 'blob' });
    }
}
