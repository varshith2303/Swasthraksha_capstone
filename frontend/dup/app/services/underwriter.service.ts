import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
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

    constructor(private http: HttpClient, private authService: AuthService) { }

    private getHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return new HttpHeaders({ Authorization: `Bearer ${token}` });
    }

    /** All applications assigned to this underwriter (any status) */
    getAssignedApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.apiUrl}/assigned`, { headers: this.getHeaders() });
    }

    /** Only UNDER_REVIEW applications assigned to this underwriter */
    getPendingApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.apiUrl}/pending`, { headers: this.getHeaders() });
    }

    updateApplication(id: number, decision: UnderwriterDecision): Observable<Application> {
        return this.http.patch<Application>(`${this.apiUrl}/${id}`, decision, { headers: this.getHeaders() });
    }
}
