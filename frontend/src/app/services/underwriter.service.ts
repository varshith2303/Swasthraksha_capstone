import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Application } from './user.service';

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

    /** All applications assigned to this underwriter (any status) */
    getAssignedApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.apiUrl}/assigned`);
    }

    /** Only UNDER_REVIEW applications assigned to this underwriter */
    getPendingApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.apiUrl}/pending`);
    }

    updateApplication(id: number, decision: UnderwriterDecision): Observable<Application> {
        return this.http.patch<Application>(`${this.apiUrl}/${id}`, decision);
    }
}
