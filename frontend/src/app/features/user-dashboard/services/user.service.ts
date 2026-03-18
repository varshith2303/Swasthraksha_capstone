import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Policy {
    id?: number;
    policyName: string;
    policyCode: string;
    minCoverage: number;
    maxCoverage: number;
    basePercent: number;
    active: boolean;
    planType?: string;
}

export interface PolicyMemberRequest {
    name: string;
    age: number;
    bmi: number;
    smoker: boolean;
    existingDiseases: string;
    relationship?: string;
}

export interface ApplicationRequest {
    policyCode: string;
    requestedCoverage: number;
    duration: number;
    members: PolicyMemberRequest[];
}

export interface Application {
    id: number;
    applicationNumber?: string;
    status: string;
    planType?: string;
    requestedCoverage: number;
    duration: number;
    riskScore?: number;
    proposedPremium?: number;
    finalPremium?: number;
    members?: { id: number; name: string; age: number; bmi: number; smoker: boolean; existingDiseases?: string; relationship?: string }[];
    policy: Policy;
    user: {
        id?: number;
        email: string;
        username: string;
    };
    assignedTo?: {
        id?: number;
        email: string;
        username: string;
    } | null;
}

export interface PolicyAssignment {
    id: number;
    policyNumber: string;
    coverageAmount: number;
    remainingCoverage: number;
    premiumAmount: number;
    totalPremiumAmount: number;
    premiumPaid: number;
    startDate?: string;
    endDate?: string;
    durationYears: number;
    paymentFrequency: string;
    status: string;
    claimCount?: number;
    totalClaimedAmount?: number;
    totalInstallments?: number;
    paidInstallments?: number;
    application: Application;
}

export interface ClaimRequest {
    policyNumber: string;
    claimAmount: number;
    hospitalName: string;
    claimReason: string;
    admissionDate: string;
    dischargeDate: string;
    memberId?: number;
}

export interface ClaimResponse {
    claimNumber: string;
    claimAmount: number;
    approvedAmount: number;
    hospitalName: string;
    status: string;
    admissionDate: string;
    dischargeDate: string;
    claimReason?: string;
    policyNumber?: string;
    submittedDate?: string;
    reviewedDate?: string;
    policyName?: string;
    patientName?: string;
}

export type DocumentType =
    | 'ID_PROOF'
    | 'ADDRESS_PROOF'
    | 'MEDICAL_REPORT'
    | 'HOSPITAL_BILL'
    | 'DISCHARGE_SUMMARY'
    | 'PRESCRIPTION';

export interface DocumentUploadResponse {
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
export class UserService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) { }

    getAllPolicies(): Observable<Policy[]> {
        return this.http.get<Policy[]>(`${this.apiUrl}/policies`);
    }

    applyForPolicy(req: ApplicationRequest): Observable<Application> {
        return this.http.post<Application>(`${this.apiUrl}/applications`, req);
    }

    getMyApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.apiUrl}/applications/myapplications`);
    }

    getMyPolicyAssignments(): Observable<PolicyAssignment[]> {
        return this.http.get<PolicyAssignment[]>(`${this.apiUrl}/policyassignments/my`);
    }

    makePolicyPayment(policyId: number): Observable<PolicyAssignment> {
        return this.http.patch<PolicyAssignment>(`${this.apiUrl}/policyassignments/${policyId}/pay`, {});
    }

    acceptApplication(applicationNumber: string): Observable<Application> {
        return this.http.patch<Application>(`${this.apiUrl}/applications/${applicationNumber}/accept`, {});
    }

    makeFirstPayment(applicationNumber: string): Observable<PolicyAssignment> {
        return this.http.post<PolicyAssignment>(`${this.apiUrl}/policyassignments`, applicationNumber, {
            headers: { 'Content-Type': 'application/json' }
        });
    }

    declineApplication(applicationId: number): Observable<Application> {
        return this.http.patch<Application>(`${this.apiUrl}/applications/${applicationId}/decline`, {});
    }

    submitClaim(req: ClaimRequest): Observable<ClaimResponse> {
        return this.http.post<ClaimResponse>(`${this.apiUrl}/claims`, req);
    }

    getMyClaims(): Observable<ClaimResponse[]> {
        return this.http.get<ClaimResponse[]>(`${this.apiUrl}/claims/my`);
    }

    uploadApplicationDocument(applicationNumber: string, documentType: DocumentType, file: File): Observable<DocumentUploadResponse> {
        const formData = new FormData();
        formData.append('applicationNumber', applicationNumber);
        formData.append('documentType', documentType);
        formData.append('file', file);
        return this.http.post<DocumentUploadResponse>(`${this.apiUrl}/api/documents/upload/application`, formData);
    }

    uploadClaimDocument(claimNumber: string, documentType: DocumentType, file: File): Observable<DocumentUploadResponse> {
        const formData = new FormData();
        formData.append('claimNumber', claimNumber);
        formData.append('documentType', documentType);
        formData.append('file', file);
        return this.http.post<DocumentUploadResponse>(`${this.apiUrl}/api/documents/upload/claim`, formData);
    }
}