import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface AppUser {
    id?: number;
    username: string;
    email: string;
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
export class ClaimsOfficerService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) { }

    getAssignedClaims(): Observable<Claim[]> {
        return this.http.get<Claim[]>(`${this.apiUrl}/claims/assigned`);
    }

    verifyClaim(claimNumber: string, approve: boolean): Observable<string> {
        const params = new HttpParams().set('approve', approve.toString());
        return this.http.post(`${this.apiUrl}/claims/${claimNumber}/verify`, null, {
            params,
            responseType: 'text'
        });
    }
}