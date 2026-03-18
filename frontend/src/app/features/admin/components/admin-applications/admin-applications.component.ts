import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, ApplicationSummary, Underwriter } from '../../services/admin.service';

@Component({
    selector: 'app-admin-applications',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './admin-applications.component.html',
})
export class AdminApplicationsComponent implements OnInit {
    applications = signal<ApplicationSummary[]>([]);
    underwriters = signal<Underwriter[]>([]);
    loading = false;
    error = '';
    success = '';
    statusFilter = 'ALL';

    showAssignModal = false;
    assigningApp: ApplicationSummary | null = null;
    selectedUnderwriterEmail = '';

    constructor(private adminService: AdminService) {}

    ngOnInit(): void {
        this.load();
        this.loadUnderwriters();
    }

    load(force = false): void {
        if (!force && this.applications().length > 0 && this.loading) return;
        this.loading = true;
        this.adminService.getAllApplications().subscribe({
            next: (data) => { this.applications.set(data); this.loading = false; },
            error: () => { this.error = 'Failed to load applications.'; this.loading = false; }
        });
    }

    loadUnderwriters(): void {
        this.adminService.getUnderwriters().subscribe({
            next: (data) => this.underwriters.set(data),
            error: () => {}
        });
    }

    get filteredApplications(): ApplicationSummary[] {
        const apps = this.applications();
        if (this.statusFilter === 'ALL') return apps;
        return apps.filter(a => a.status === this.statusFilter);
    }

    openAssignModal(app: ApplicationSummary): void {
        this.assigningApp = app;
        this.selectedUnderwriterEmail = app.assignedTo?.email || '';
        this.showAssignModal = true;
        this.error = ''; this.success = '';
    }

    closeAssignModal(): void {
        this.showAssignModal = false;
        this.assigningApp = null;
        this.selectedUnderwriterEmail = '';
    }

    confirmAssign(): void {
        if (!this.assigningApp || !this.selectedUnderwriterEmail) return;
        this.adminService.assignApplication(this.assigningApp.id, this.selectedUnderwriterEmail).subscribe({
            next: () => {
                this.success = `Application ${this.assigningApp!.applicationNumber} assigned successfully!`;
                this.closeAssignModal();
                this.load(true);
            },
            error: (err) => {
                this.error = err.error?.message || 'Assignment failed.';
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
