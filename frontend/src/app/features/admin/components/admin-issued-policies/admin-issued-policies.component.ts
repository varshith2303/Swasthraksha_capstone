import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, IssuedPolicy } from '../../services/admin.service';

@Component({
    selector: 'app-admin-issued-policies',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './admin-issued-policies.component.html',
})
export class AdminIssuedPoliciesComponent implements OnInit {
    issuedPolicies = signal<IssuedPolicy[]>([]);
    loading = false;
    error = '';

    constructor(private adminService: AdminService) {}

    ngOnInit(): void {
        this.load();
    }

    load(force = false): void {
        if (!force && this.issuedPolicies().length > 0 && this.loading) return;
        this.loading = true;
        this.adminService.getAllIssuedPolicies().subscribe({
            next: (data) => { this.issuedPolicies.set(data); this.loading = false; },
            error: () => { this.error = 'Failed to load issued policies.'; this.loading = false; }
        });
    }

    getPolicyStatusColor(status: string): string {
        const map: Record<string, string> = {
            'ACTIVE': 'background:#ecfdf5; color:#059669; border:1px solid #a7f3d0;',
            'PENDING_PAYMENT': 'background:#fefce8; color:#ca8a04; border:1px solid #fde68a;',
            'EXPIRED': 'background:#fef2f2; color:#dc2626; border:1px solid #fecaca;',
            'CANCELLED': 'background:#fef2f2; color:#dc2626; border:1px solid #fecaca;',
        };
        return map[status] || 'background:#f9fafb; color:#6b7280; border:1px solid #e5e7eb;';
    }
}
