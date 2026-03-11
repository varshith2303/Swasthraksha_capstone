import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface Policy {
    id?: number;
    policyName: string;
    policyCode: string;
    minCoverage: number;
    maxCoverage: number;
    basePercent: number;
    active: boolean;
    planType?: string; // 'INDIVIDUAL' | 'FAMILY' | 'BOTH'
}

export interface PolicyMemberRequest {
    name: string;
    age: number;
    bmi: number;
    smoker: boolean;
    existingDiseases: string;
    relationship?: string; // 'SELF' | 'SPOUSE' | 'PARENT' | 'CHILD'
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
    planType?: string;        // 'INDIVIDUAL' | 'FAMILY'
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
    admissionDate: string; // ISO date e.g. 2024-01-15
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
    policyName?: string;
    patientName?: string;
}

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient, private authService: AuthService) { }

    private getHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return new HttpHeaders({ Authorization: `Bearer ${token}` });
    }

    // Policies (public - no auth needed by backend, but we send token anyway)
    getAllPolicies(): Observable<Policy[]> {
        return this.http.get<Policy[]>(`${this.apiUrl}/policies`, { headers: this.getHeaders() });
    }

    // Applications
    applyForPolicy(req: ApplicationRequest): Observable<Application> {
        return this.http.post<Application>(`${this.apiUrl}/applications`, req, { headers: this.getHeaders() });
    }

    getMyApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.apiUrl}/applications/myapplications`, { headers: this.getHeaders() });
    }

    getMyPolicyAssignments(): Observable<PolicyAssignment[]> {
        return this.http.get<PolicyAssignment[]>(`${this.apiUrl}/policyassignments/my`, { headers: this.getHeaders() });
    }

    makePolicyPayment(policyId: number): Observable<PolicyAssignment> {
        return this.http.patch<PolicyAssignment>(`${this.apiUrl}/policyassignments/${policyId}/pay`, {}, { headers: this.getHeaders() });
    }

    // Accept an offer — creates a PolicyAssignment
    acceptApplication(applicationNumber: string): Observable<any> {
        return this.http.post(`${this.apiUrl}/policyassignments`, applicationNumber, {
            headers: this.getHeaders().set('Content-Type', 'application/json')
        });
    }

    // Decline an offer
    declineApplication(applicationId: number): Observable<Application> {
        return this.http.patch<Application>(
            `${this.apiUrl}/applications/${applicationId}/decline`,
            {},
            { headers: this.getHeaders() }
        );
    }

    // Claims
    submitClaim(req: ClaimRequest): Observable<ClaimResponse> {
        return this.http.post<ClaimResponse>(`${this.apiUrl}/claims`, req, { headers: this.getHeaders() });
    }

    getMyClaims(): Observable<ClaimResponse[]> {
        return this.http.get<ClaimResponse[]>(`${this.apiUrl}/claims/my`, { headers: this.getHeaders() });
    }
}
