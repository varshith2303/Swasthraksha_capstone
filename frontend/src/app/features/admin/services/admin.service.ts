import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface AppUser {
    id: number;
    username: string;
    email: string;
    role: string;
}

export interface Policy {
    id?: number;
    policyName: string;
    policyCode: string;
    minCoverage: number;
    maxCoverage: number;
    basePercent: number;
    active: boolean;
    planType: string;
    waitingPeriod: number;
}

export interface Underwriter {
    id?: number;
    username: string;
    email: string;
    password?: string;
    role?: string;
}

export interface ApplicationSummary {
    id: number;
    applicationNumber: string;
    status: string;
    requestedCoverage: number;
    proposedPremium: number;
    finalPremium: number;
    riskScore: number;
    planType: string;
    members: any[];
    duration: number;
    user: AppUser;
    policy: { id: number; policyName: string; policyCode: string };
    assignedTo: AppUser | null;
    decidedBy: AppUser | null;
}

export interface IssuedPolicy {
    id: number;
    policyNumber: string;
    status: string;
    coverageAmount: number;
    premiumAmount: number;
    premiumPaid: number;
    totalPremiumAmount: number;
    startDate: string;
    endDate: string;
    durationYears: number;
    paymentFrequency: string;
    paidInstallments: number;
    totalInstallments: number;
    totalClaimedAmount: number;
    claimCount: number;
    user: AppUser;
    application: { id: number; applicationNumber: string; policy: { policyName: string } };
}

export interface ClaimsOfficer {
    id?: number;
    username: string;
    email: string;
    password?: string;
    role?: string;
}

export interface Claim {
    id: number;
    claimNumber: string;
    claimAmount: number;
    approvedAmount: number;
    hospitalName: string;
    status: string;
    admissionDate: string;
    dischargeDate: string;
    claimReason: string;
    policyNumber?: string;
    claimant: AppUser;
    reviewedBy?: AppUser | null;
}

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) { }

    getAllPolicies(): Observable<Policy[]> {
        return this.http.get<Policy[]>(`${this.apiUrl}/policies?adminView=true`);
    }

    addPolicy(policy: Policy): Observable<Policy> {
        return this.http.post<Policy>(`${this.apiUrl}/policies`, policy);
    }

    updatePolicy(id: number, policy: Policy): Observable<Policy> {
        return this.http.put<Policy>(`${this.apiUrl}/policies/${id}`, policy);
    }

    deletePolicy(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/policies/${id}`);
    }

    togglePolicyStatus(id: number): Observable<Policy> {
        return this.http.patch<Policy>(`${this.apiUrl}/policies/${id}/toggle-status`, null);
    }

    addUnderwriter(underwriter: Underwriter): Observable<Underwriter> {
        return this.http.post<Underwriter>(`${this.apiUrl}/admin/users`, underwriter);
    }

    getUnderwriters(): Observable<Underwriter[]> {
        return this.http.get<Underwriter[]>(`${this.apiUrl}/admin/users`);
    }

    addClaimsOfficer(officer: ClaimsOfficer): Observable<ClaimsOfficer> {
        return this.http.post<ClaimsOfficer>(`${this.apiUrl}/admin/claims-officers`, officer);
    }

    getClaimsOfficers(): Observable<ClaimsOfficer[]> {
        return this.http.get<ClaimsOfficer[]>(`${this.apiUrl}/admin/claims-officers`);
    }

    deleteUser(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/admin/users/${id}`);
    }

    getAllApplications(): Observable<ApplicationSummary[]> {
        return this.http.get<ApplicationSummary[]>(`${this.apiUrl}/applications`);
    }

    assignApplication(applicationId: number, underwriterEmail: string): Observable<ApplicationSummary> {
        const params = new HttpParams().set('underwriterEmail', underwriterEmail);
        return this.http.patch<ApplicationSummary>(
            `${this.apiUrl}/applications/${applicationId}/assign`,
            null,
            { params }
        );
    }

    getAllClaims(): Observable<Claim[]> {
        return this.http.get<Claim[]>(`${this.apiUrl}/claims`);
    }

    assignClaim(claimNumber: string, officerEmail: string): Observable<any> {
        const params = new HttpParams().set('officerEmail', officerEmail);
        return this.http.post(`${this.apiUrl}/claims/${claimNumber}/assign`, null, {
            params,
            responseType: 'text'
        });
    }

    verifyClaim(claimNumber: string, approve: boolean): Observable<any> {
        const params = new HttpParams().set('approve', approve.toString());
        return this.http.post(`${this.apiUrl}/claims/${claimNumber}/verify`, null, {
            params,
            responseType: 'text'
        });
    }

    getOfficerAssignedClaims(): Observable<Claim[]> {
        return this.http.get<Claim[]>(`${this.apiUrl}/claims/assigned`);
    }

    getAllIssuedPolicies(): Observable<IssuedPolicy[]> {
        return this.http.get<IssuedPolicy[]>(`${this.apiUrl}/policyassignments/all`);
    }
}