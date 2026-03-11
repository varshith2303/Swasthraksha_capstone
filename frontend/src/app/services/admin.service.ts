import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
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
    planType: string;
}

export interface Underwriter {
    id?: number;
    username: string;
    email: string;
    password?: string;
    role?: string;
}

export interface AppUser {
    id: number;
    username: string;
    email: string;
    role: string;
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

    constructor(private http: HttpClient, private authService: AuthService) { }

    private getHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return new HttpHeaders({ Authorization: `Bearer ${token}` });
    }

    // ── Policies ──────────────────────────────────────────────────
    getAllPolicies(): Observable<Policy[]> {
        return this.http.get<Policy[]>(`${this.apiUrl}/policies`, { headers: this.getHeaders() });
    }

    addPolicy(policy: Policy): Observable<Policy> {
        return this.http.post<Policy>(`${this.apiUrl}/policies`, policy, { headers: this.getHeaders() });
    }

    updatePolicy(id: number, policy: Policy): Observable<Policy> {
        return this.http.put<Policy>(`${this.apiUrl}/policies/${id}`, policy, { headers: this.getHeaders() });
    }

    deletePolicy(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/policies/${id}`, { headers: this.getHeaders() });
    }

    // ── Underwriters ──────────────────────────────────────────────
    addUnderwriter(underwriter: Underwriter): Observable<Underwriter> {
        return this.http.post<Underwriter>(`${this.apiUrl}/admin/users`, underwriter, { headers: this.getHeaders() });
    }

    getUnderwriters(): Observable<Underwriter[]> {
        return this.http.get<Underwriter[]>(`${this.apiUrl}/admin/users`, { headers: this.getHeaders() });
    }

    // ── Claims Officers ───────────────────────────────────────────
    addClaimsOfficer(officer: ClaimsOfficer): Observable<ClaimsOfficer> {
        return this.http.post<ClaimsOfficer>(`${this.apiUrl}/admin/claims-officers`, officer, { headers: this.getHeaders() });
    }

    getClaimsOfficers(): Observable<ClaimsOfficer[]> {
        return this.http.get<ClaimsOfficer[]>(`${this.apiUrl}/admin/claims-officers`, { headers: this.getHeaders() });
    }

    deleteUser(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/admin/users/${id}`, { headers: this.getHeaders() });
    }

    // ── Applications ──────────────────────────────────────────────
    getAllApplications(): Observable<ApplicationSummary[]> {
        return this.http.get<ApplicationSummary[]>(`${this.apiUrl}/applications`, { headers: this.getHeaders() });
    }

    assignApplication(applicationId: number, underwriterEmail: string): Observable<ApplicationSummary> {
        const params = new HttpParams().set('underwriterEmail', underwriterEmail);
        return this.http.patch<ApplicationSummary>(
            `${this.apiUrl}/applications/${applicationId}/assign`,
            null,
            { headers: this.getHeaders(), params }
        );
    }

    // ── Claims ────────────────────────────────────────────────────
    getAllClaims(): Observable<Claim[]> {
        return this.http.get<Claim[]>(`${this.apiUrl}/claims`, { headers: this.getHeaders() });
    }

    assignClaim(claimNumber: string, officerEmail: string): Observable<any> {
        const params = new HttpParams().set('officerEmail', officerEmail);
        return this.http.post(`${this.apiUrl}/claims/${claimNumber}/assign`, null, {
            headers: this.getHeaders(),
            params,
            responseType: 'text'
        });
    }

    getOfficerAssignedClaims(): Observable<Claim[]> {
        return this.http.get<Claim[]>(`${this.apiUrl}/claims/assigned`, { headers: this.getHeaders() });
    }

    verifyClaim(claimNumber: string, approve: boolean): Observable<any> {
        const params = new HttpParams().set('approve', approve.toString());
        return this.http.post(`${this.apiUrl}/claims/${claimNumber}/verify`, null, {
            headers: this.getHeaders(),
            params,
            responseType: 'text'
        });
    }

    // ── Issued Policies ───────────────────────────────────────────
    getAllIssuedPolicies(): Observable<IssuedPolicy[]> {
        return this.http.get<IssuedPolicy[]>(`${this.apiUrl}/policyassignments/all`, { headers: this.getHeaders() });
    }
}
