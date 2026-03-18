import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
    AdminService, Policy, Underwriter, ApplicationSummary, IssuedPolicy, Claim
} from '../../services/admin.service';
import { AuthService } from '../../../../services/auth.service';
import { claimsOfficerGuard } from '../../../../guards/claims-officer.guard';

@Component({
    selector: 'app-admin',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './admin.component.html',
    styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
    activeTab: 'policies' | 'underwriters' | 'claims-officers' | 'applications' | 'claims' | 'issued-policies' = 'policies';

    // ── Policies ──────────────────────────────────────────────────
    policies = signal<Policy[]>([]);
    policyLoading = false;
    policyError = '';
    policySuccess = '';
    showPolicyForm = false;
    editingPolicy: Policy | null = null;
    policyForm: Policy = this.emptyPolicy();

    // ── Underwriters ──────────────────────────────────────────────
    underwriterForm: Underwriter = this.emptyUnderwriter();
    underwriterLoading = false;
    underwriterError = '';
    underwriterSuccess = '';
    underwriters = signal<Underwriter[]>([]);

    // ── Claims Officers ───────────────────────────────────────────
    claimsOfficerForm: Underwriter = this.emptyUnderwriter();
    claimsOfficerLoading = false;
    claimsOfficerError = '';
    claimsOfficerSuccess = signal<string>('');
    claimsOfficers = signal<Underwriter[]>([]);

    // ── Applications ──────────────────────────────────────────────
    applications = signal<ApplicationSummary[]>([]);
    appLoading = false;
    appError = '';
    appSuccess = '';

    // Assign modal for Applications
    showAssignModal = false;
    assigningApp: ApplicationSummary | null = null;
    selectedUnderwriterEmail = '';

    // ── Claims ────────────────────────────────────────────────────
    claims = signal<Claim[]>([]);
    claimLoading = false;
    claimError = '';
    claimSuccess = '';

    // Assign modal for Claims
    showClaimAssignModal = false;
    assigningClaim: Claim | null = null;
    selectedOfficerEmail = '';

    // ── Issued Policies ───────────────────────────────────────────
    issuedPolicies = signal<IssuedPolicy[]>([]);
    issuedLoading = false;
    issuedError = '';

    // Status filters
    appStatusFilter = 'ALL';
    claimStatusFilter = 'ALL';

    constructor(
        private adminService: AdminService,
        private authService: AuthService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.loadPolicies();
    }

    // ── Helpers ───────────────────────────────────────────────────
    emptyPolicy(): Policy {
        return { policyName: '', policyCode: '', minCoverage: 0, maxCoverage: 0, basePercent: 0, active: true, planType: 'BOTH', waitingPeriod: 0 };
    }

    emptyUnderwriter(): Underwriter {
        return { username: '', email: '', password: '' };
    }

    logout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    setTab(tab: 'policies' | 'underwriters' | 'claims-officers' | 'applications' | 'claims' | 'issued-policies'): void {
        if (this.activeTab === tab) return;
        this.activeTab = tab;
        this.clearMessages();
        if (tab === 'applications' && this.applications().length === 0) this.loadApplications();
        if (tab === 'claims' && this.claims().length === 0) this.loadClaims();
        if (tab === 'issued-policies' && this.issuedPolicies().length === 0) this.loadIssuedPolicies();
        if (tab === 'underwriters' && this.underwriters().length === 0) this.loadUnderwriters();
        if (tab === 'claims-officers' && this.claimsOfficers().length === 0) this.loadClaimsOfficers();
    }

    clearMessages(): void {
        this.policyError = ''; this.policySuccess = '';
        this.underwriterError = ''; this.underwriterSuccess = '';
        this.claimsOfficerError = ''; this.claimsOfficerSuccess.set('');
        this.appError = ''; this.appSuccess = '';
        this.claimError = ''; this.claimSuccess = '';
        this.issuedError = '';
    }

    // ── Policy Methods ────────────────────────────────────────────
    loadPolicies(): void {
        this.policyLoading = true;
        this.adminService.getAllPolicies().subscribe({
            next: (data) => { this.policies.set(data); this.policyLoading = false; },
            error: () => { this.policyError = 'Failed to load policies.'; this.policyLoading = false; }
        });
    }

    openAddPolicy(): void {
        this.editingPolicy = null;
        this.policyForm = this.emptyPolicy();
        this.showPolicyForm = true;
        this.clearMessages();
    }

    openEditPolicy(policy: Policy): void {
        this.editingPolicy = policy;
        this.policyForm = { ...policy };
        this.showPolicyForm = true;
        this.clearMessages();
    }

    cancelPolicyForm(): void {
        this.showPolicyForm = false;
        this.editingPolicy = null;
    }

    savePolicy(): void {
        this.clearMessages();
        const currentPolicies = this.policies();
        const isDuplicateCode = currentPolicies.some(p =>
            p.policyCode.toLowerCase() === this.policyForm.policyCode.toLowerCase() &&
            (!this.editingPolicy || p.id !== this.editingPolicy.id));
        const isDuplicateName = currentPolicies.some(p =>
            p.policyName.toLowerCase() === this.policyForm.policyName.toLowerCase() &&
            (!this.editingPolicy || p.id !== this.editingPolicy.id));

        if (isDuplicateCode) { this.policyError = `Policy code "${this.policyForm.policyCode}" is already in use.`; return; }
        if (isDuplicateName) { this.policyError = `Policy name "${this.policyForm.policyName}" is already in use.`; return; }
        if (this.policyForm.minCoverage <= 0 || this.policyForm.maxCoverage <= 0) { this.policyError = 'Coverage amounts must be greater than zero.'; return; }
        if (this.policyForm.maxCoverage <= this.policyForm.minCoverage) { this.policyError = 'Maximum coverage must be greater than minimum coverage.'; return; }

        this.policyLoading = true;
        const obs = this.editingPolicy
            ? this.adminService.updatePolicy(this.editingPolicy.id!, this.policyForm)
            : this.adminService.addPolicy(this.policyForm);

        obs.subscribe({
            next: () => {
                this.policySuccess = this.editingPolicy ? 'Policy updated!' : 'Policy added!';
                this.showPolicyForm = false;
                this.editingPolicy = null;
                this.loadPolicies();
            },
            error: (err) => { this.policyError = err.error?.message || 'Operation failed.'; this.policyLoading = false; }
        });
    }

    deletePolicy(id: number): void {
        if (!confirm('Are you sure you want to delete this policy?')) return;
        this.adminService.deletePolicy(id).subscribe({
            next: () => { this.policySuccess = 'Policy deleted.'; this.loadPolicies(); },
            error: () => { this.policyError = 'Delete failed.'; }
        });
    }

    togglePolicyStatus(policy: Policy): void {
        const action = policy.active ? 'deactivate' : 'activate';
        if (!confirm(`Are you sure you want to ${action} this policy?`)) return;
        this.adminService.togglePolicyStatus(policy.id!).subscribe({
            next: (updated) => {
                this.policySuccess = `Policy "${updated.policyName}" is now ${updated.active ? 'Active' : 'Inactive'}.`;
                this.loadPolicies();
            },
            error: () => { this.policyError = 'Failed to update policy status.'; }
        });
    }

    // ── Underwriter Methods ───────────────────────────────────────
    loadUnderwriters(): void {
        this.underwriterLoading = true;
        this.adminService.getUnderwriters().subscribe({
            next: (data) => { this.underwriters.set(data); this.underwriterLoading = false; },
            error: () => { this.underwriterLoading = false; }
        });
    }

    addUnderwriter(): void {
        this.underwriterLoading = true;
        this.clearMessages();
        this.adminService.addUnderwriter(this.underwriterForm).subscribe({
            next: () => {
                this.underwriterSuccess = `Underwriter "${this.underwriterForm.username}" created successfully!`;
                this.underwriterForm = this.emptyUnderwriter();
                this.underwriterLoading = false;
                this.loadUnderwriters();
            },
            error: () => {
                this.underwriterError = 'Failed to create underwriter. Email may already exist.';
                this.underwriterLoading = false;
            }
        });
    }

    deleteUnderwriter(id: number): void {
        if (!confirm('Are you sure you want to delete this underwriter?')) return;
        this.clearMessages();
        this.adminService.deleteUser(id).subscribe({
            next: () => { this.underwriterSuccess = 'Underwriter deleted successfully.'; this.loadUnderwriters(); },
            error: (err) => {
                this.underwriterError = err.error?.message
                    || err.error?.error
                    || 'Failed to delete underwriter. Please try again.';
            }
        });
    }

    // ── Claims Officer Methods ────────────────────────────────────
    loadClaimsOfficers(): void {
        this.claimsOfficerLoading = true;
        this.adminService.getClaimsOfficers().subscribe({
            next: (data) => { this.claimsOfficers.set(data); this.claimsOfficerLoading = false; },
            error: () => { this.claimsOfficerLoading = false; }
        });
    }

    addClaimsOfficer(): void {
        this.claimsOfficerLoading = true;
        this.clearMessages();
        this.adminService.addClaimsOfficer(this.claimsOfficerForm).subscribe({
            next: () => {
                this.claimsOfficerSuccess.set(`Claims Officer "${this.claimsOfficerForm.username}" created successfully!`);
                this.claimsOfficerForm = this.emptyUnderwriter();
                this.claimsOfficerLoading = false;
                this.loadClaimsOfficers();
            },
            error: () => {
                this.claimsOfficerError = 'Failed to create claims officer. Email may already exist.';
                this.claimsOfficerLoading = false;
            }
        });
    }

    deleteClaimsOfficer(id: number): void {
        if (!confirm('Are you sure you want to delete this claims officer?')) return;
        this.clearMessages();
        this.adminService.deleteUser(id).subscribe({
            next: () => {
                this.claimsOfficerSuccess.set('Claims officer deleted successfully.');
                console.log(this.claimsOfficerSuccess());
                this.loadClaimsOfficers();
            },
            error: (err) => {
                this.claimsOfficerError = err.error?.message
                    || err.error?.error
                    || 'Failed to delete claims officer. Please try again.';
            }
        });
    }

    // ── Application Methods ───────────────────────────────────────
    loadApplications(force = false): void {
        if (!force && this.applications().length > 0 && this.appLoading) return;
        this.appLoading = true;
        this.adminService.getAllApplications().subscribe({
            next: (data) => { this.applications.set(data); this.appLoading = false; },
            error: () => { this.appError = 'Failed to load applications.'; this.appLoading = false; }
        });
        if (this.underwriters().length === 0) this.loadUnderwriters();
    }

    get filteredApplications(): ApplicationSummary[] {
        const apps = this.applications();
        if (this.appStatusFilter === 'ALL') return apps;
        return apps.filter(a => a.status === this.appStatusFilter);
    }

    openAssignModal(app: ApplicationSummary): void {
        this.assigningApp = app;
        this.selectedUnderwriterEmail = app.assignedTo?.email || '';
        this.showAssignModal = true;
        this.clearMessages();
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
                this.appSuccess = `Application ${this.assigningApp!.applicationNumber} assigned successfully!`;
                this.closeAssignModal();
                this.loadApplications();
            },
            error: (err) => {
                this.appError = err.error?.message || 'Assignment failed.';
                this.closeAssignModal();
            }
        });
    }

    // ── Claim Methods ─────────────────────────────────────────────
    loadClaims(): void {
        this.claimLoading = true;
        this.adminService.getAllClaims().subscribe({
            next: (data) => { this.claims.set(data); this.claimLoading = false; },
            error: () => { this.claimError = 'Failed to load claims.'; this.claimLoading = false; }
        });
        if (this.claimsOfficers().length === 0) this.loadClaimsOfficers();
    }

    get filteredClaims(): Claim[] {
        const claims = this.claims();
        if (this.claimStatusFilter === 'ALL') return claims;
        return claims.filter(c => c.status === this.claimStatusFilter);
    }

    openClaimAssignModal(claim: Claim): void {
        this.assigningClaim = claim;
        this.selectedOfficerEmail = claim.reviewedBy?.email || '';
        this.showClaimAssignModal = true;
        this.clearMessages();
    }

    closeClaimAssignModal(): void {
        this.showClaimAssignModal = false;
        this.assigningClaim = null;
        this.selectedOfficerEmail = '';
    }

    confirmClaimAssign(): void {
        if (!this.assigningClaim || !this.selectedOfficerEmail) return;
        this.adminService.assignClaim(this.assigningClaim.claimNumber, this.selectedOfficerEmail).subscribe({
            next: () => {
                this.claimSuccess = `Claim ${this.assigningClaim!.claimNumber} assigned successfully!`;
                this.closeClaimAssignModal();
                this.loadClaims();
            },
            error: (err) => {
                this.claimError = err.error || 'Assignment failed.';
                this.closeClaimAssignModal();
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

    getPolicyStatusColor(status: string): string {
        const map: Record<string, string> = {
            'ACTIVE': 'background:#ecfdf5; color:#059669; border:1px solid #a7f3d0;',
            'PENDING_PAYMENT': 'background:#fefce8; color:#ca8a04; border:1px solid #fde68a;',
            'EXPIRED': 'background:#fef2f2; color:#dc2626; border:1px solid #fecaca;',
            'CANCELLED': 'background:#fef2f2; color:#dc2626; border:1px solid #fecaca;',
        };
        return map[status] || 'background:#f9fafb; color:#6b7280; border:1px solid #e5e7eb;';
    }

    // ── Issued Policies ───────────────────────────────────────────
    loadIssuedPolicies(force = false): void {
        if (!force && this.issuedPolicies().length > 0 && this.issuedLoading) return;
        this.issuedLoading = true;
        this.adminService.getAllIssuedPolicies().subscribe({
            next: (data) => { this.issuedPolicies.set(data); this.issuedLoading = false; },
            error: () => { this.issuedError = 'Failed to load issued policies.'; this.issuedLoading = false; }
        });
    }
}
