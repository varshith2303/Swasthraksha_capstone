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

export interface UnderwriterDecision {
    status: 'APPROVED' | 'REJECTED';
    finalPremium?: number;
}

@Injectable({
    providedIn: 'root'
})
export class UnderwriterService {
    private apiUrl = `${environment.apiUrl}/applications`;

    constructor(private http: HttpClient) { }

    getAssignedApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.apiUrl}/assigned`);
    }

    getPendingApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.apiUrl}/pending`);
    }

    updateApplication(id: number, decision: UnderwriterDecision): Observable<Application> {
        return this.http.patch<Application>(`${this.apiUrl}/${id}`, decision);
    }
}
