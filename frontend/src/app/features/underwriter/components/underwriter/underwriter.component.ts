import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../services/auth.service';
import { UnderwriterService, UnderwriterDecision } from '../../../../services/underwriter.service';
import { Application } from '../../../../services/user.service';

@Component({
    selector: 'app-underwriter',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './underwriter.component.html',
    styleUrl: './underwriter.component.css'
})
export class UnderwriterComponent implements OnInit {
    userEmail = '';
    activeTab: 'pending' | 'all' = 'pending';

    pendingApplications = signal<Application[]>([]);
    allApplications = signal<Application[]>([]);
    isLoading = false;

    constructor(
        private authService: AuthService,
        private underwriterService: UnderwriterService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.userEmail = this.authService.getCurrentUser() || 'Underwriter';
        this.loadPendingApplications();
        this.loadAllApplications();
    }

    loadPendingApplications(): void {
        this.isLoading = true;
        this.underwriterService.getPendingApplications().subscribe({
            next: (data) => {
                this.pendingApplications.set(data);
                this.isLoading = false;
            },
            error: () => { this.isLoading = false; }
        });
    }

    loadAllApplications(): void {
        this.underwriterService.getAssignedApplications().subscribe({
            next: (data) => this.allApplications.set(data),
            error: () => { }
        });
    }

    setTab(tab: 'pending' | 'all'): void {
        this.activeTab = tab;
    }

    approve(app: Application): void {
        const base = app.finalPremium ?? app.proposedPremium ?? 0;
        const finalPremium = prompt(`Enter final premium for ${app.user?.email} (Base: ₹${base}):`, base.toString());
        if (finalPremium === null) return;

        const decision: UnderwriterDecision = {
            status: 'APPROVED',
            finalPremium: parseFloat(finalPremium)
        };

        this.underwriterService.updateApplication(app.id, decision).subscribe({
            next: () => {
                alert('Application approved!');
                this.loadPendingApplications();
                this.loadAllApplications();
            },
            error: (err) => alert('Operation failed: ' + (err.error?.message || 'Unknown error'))
        });
    }

    reject(app: Application): void {
        if (!confirm(`Are you sure you want to REJECT this application?`)) return;

        const decision: UnderwriterDecision = { status: 'REJECTED' };

        this.underwriterService.updateApplication(app.id, decision).subscribe({
            next: () => {
                alert('Application rejected.');
                this.loadPendingApplications();
                this.loadAllApplications();
            },
            error: (err) => alert('Operation failed: ' + (err.error?.message || 'Unknown error'))
        });
    }

    getStatusColor(status: string): string {
        const map: Record<string, string> = {
            'UNDER_REVIEW': 'background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe;',
            'WAITING_CUSTOMER_ACCEPTANCE': 'background:#fefce8; color:#ca8a04; border:1px solid #fde68a;',
            'APPROVED': 'background:#ecfdf5; color:#059669; border:1px solid #a7f3d0;',
            'REJECTED': 'background:#fef2f2; color:#dc2626; border:1px solid #fecaca;',
            'CUSTOMER_DECLINED': 'background:#fef2f2; color:#dc2626; border:1px solid #fecaca;',
            'CUSTOMER_ACCEPTED': 'background:#ecfdf5; color:#059669; border:1px solid #a7f3d0;',
            'POLICY_ISSUED': 'background:#f0fdf4; color:#16a34a; border:1px solid #86efac;',
        };
        return map[status] || 'background:#f9fafb; color:#6b7280; border:1px solid #e5e7eb;';
    }

    logout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
