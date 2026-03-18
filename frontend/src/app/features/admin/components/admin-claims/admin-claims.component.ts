import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Claim, Underwriter } from '../../services/admin.service';

@Component({
    selector: 'app-admin-claims',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './admin-claims.component.html',
})
export class AdminClaimsComponent implements OnInit {
    claims = signal<Claim[]>([]);
    claimsOfficers = signal<Underwriter[]>([]);
    loading = false;
    error = '';
    success = '';
    statusFilter = 'ALL';

    showAssignModal = false;
    assigningClaim: Claim | null = null;
    selectedOfficerEmail = '';

    constructor(private adminService: AdminService) {}

    ngOnInit(): void {
        this.load();
        this.loadOfficers();
    }

    load(): void {
        this.loading = true;
        this.adminService.getAllClaims().subscribe({
            next: (data) => { this.claims.set(data); this.loading = false; },
            error: () => { this.error = 'Failed to load claims.'; this.loading = false; }
        });
    }

    loadOfficers(): void {
        this.adminService.getClaimsOfficers().subscribe({
            next: (data) => this.claimsOfficers.set(data),
            error: () => {}
        });
    }

    get filteredClaims(): Claim[] {
        const claims = this.claims();
        if (this.statusFilter === 'ALL') return claims;
        return claims.filter(c => c.status === this.statusFilter);
    }

    openAssignModal(claim: Claim): void {
        this.assigningClaim = claim;
        this.selectedOfficerEmail = claim.reviewedBy?.email || '';
        this.showAssignModal = true;
        this.error = ''; this.success = '';
    }

    closeAssignModal(): void {
        this.showAssignModal = false;
        this.assigningClaim = null;
        this.selectedOfficerEmail = '';
    }

    confirmAssign(): void {
        if (!this.assigningClaim || !this.selectedOfficerEmail) return;
        this.adminService.assignClaim(this.assigningClaim.claimNumber, this.selectedOfficerEmail).subscribe({
            next: () => {
                this.success = `Claim ${this.assigningClaim!.claimNumber} assigned successfully!`;
                this.closeAssignModal();
                this.load();
            },
            error: (err) => {
                this.error = err.error || 'Assignment failed.';
                this.closeAssignModal();
            }
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
            'DRAFT': 'background:#f9fafb; color:#6b7280; border:1px solid #e5e7eb;',
            'SUBMITTED': 'background:#eff6ff; color:#3b82f6; border:1px solid #bfdbfe;',
            'PENDING': 'background:#fefce8; color:#ca8a04; border:1px solid #fde68a;',
        };
        return map[status] || 'background:#f9fafb; color:#6b7280; border:1px solid #e5e7eb;';
    }
}
