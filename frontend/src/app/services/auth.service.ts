import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { jwtDecode } from 'jwt-decode';

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    role: string;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = environment.apiUrl;
    private TOKEN_KEY = 'swastha_token';
    private USER_KEY = 'swastha_user';

    constructor(private http: HttpClient) { }

    login(email: string, password: string): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
            tap(res => {
                localStorage.setItem(this.TOKEN_KEY, res.token);
                localStorage.setItem(this.USER_KEY, email);
                //console.log("token", res.token);
            })
        );
    }

    register(username: string, email: string, password: string): Observable<any> {
        return this.http.post(`${this.apiUrl}/register`, { username, email, password, role: 'USER' });
    }

    logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    }

    isLoggedIn(): boolean {
        return !!localStorage.getItem(this.TOKEN_KEY);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    getCurrentUser(): string | null {
        return localStorage.getItem(this.USER_KEY);
    }

    /** Decode JWT payload using jwt-decode */
    private decodeToken(token: string): any {
        try {
            return jwtDecode(token);
        } catch {
            return null;
        }
    }

    /**
     * Returns the user's role extracted from the JWT.
     * Spring stores it as [{authority: "ROLE_admin"}], so we strip the ROLE_ prefix.
     * Returns e.g. 'admin', 'underwriter', 'USER', or null if not found.
     */
    getRole(): string | null {
        const token = this.getToken();
        if (!token) return null;
        const payload = this.decodeToken(token);
        if (!payload) return null;

        // Spring Security stores authorities as [{authority: "ROLE_xxx"}]
        const roles: any[] = payload['role'] ?? [];
        if (Array.isArray(roles) && roles.length > 0) {
            const authority: string = roles[0]?.authority ?? '';
            return authority.replace(/^ROLE_/i, '');
        }
        // Fallback: plain string claim
        return payload['role'] ?? null;
    }

    isAdmin(): boolean {
        return this.getRole()?.toLowerCase() === 'admin';
    }

    isUnderwriter(): boolean {
        return this.getRole()?.toLowerCase() === 'underwriter';
    }

    isClaimsOfficer(): boolean {
        return this.getRole()?.toLowerCase() === 'claims_officer';
    }
}
