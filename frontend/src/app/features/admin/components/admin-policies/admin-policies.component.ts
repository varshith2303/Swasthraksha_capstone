import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Policy } from '../../../../services/admin.service';

@Component({
    selector: 'app-admin-policies',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './admin-policies.component.html',
})
export class AdminPoliciesComponent implements OnInit {
    policies = signal<Policy[]>([]);
    loading = false;
    error = '';
    success = '';
    showForm = false;
    editingPolicy: Policy | null = null;
    form: Policy = this.emptyPolicy();

    constructor(private adminService: AdminService) {}

    ngOnInit(): void {
        this.load();
    }

    emptyPolicy(): Policy {
        return { policyName: '', policyCode: '', minCoverage: 0, maxCoverage: 0, basePercent: 0, active: true, planType: 'BOTH' };
    }

    load(): void {
        this.loading = true;
        this.adminService.getAllPolicies().subscribe({
            next: (data) => { this.policies.set(data); this.loading = false; },
            error: () => { this.error = 'Failed to load policies.'; this.loading = false; }
        });
    }

    openAdd(): void {
        this.editingPolicy = null;
        this.form = this.emptyPolicy();
        this.showForm = true;
        this.error = ''; this.success = '';
    }

    openEdit(policy: Policy): void {
        this.editingPolicy = policy;
        this.form = { ...policy };
        this.showForm = true;
        this.error = ''; this.success = '';
    }

    cancelForm(): void {
        this.showForm = false;
        this.editingPolicy = null;
    }

    save(): void {
        this.error = ''; this.success = '';
        const all = this.policies();
        const isDuplicateCode = all.some(p =>
            p.policyCode.toLowerCase() === this.form.policyCode.toLowerCase() &&
            (!this.editingPolicy || p.id !== this.editingPolicy.id));
        const isDuplicateName = all.some(p =>
            p.policyName.toLowerCase() === this.form.policyName.toLowerCase() &&
            (!this.editingPolicy || p.id !== this.editingPolicy.id));

        if (isDuplicateCode) { this.error = `Policy code "${this.form.policyCode}" is already in use.`; return; }
        if (isDuplicateName) { this.error = `Policy name "${this.form.policyName}" is already in use.`; return; }
        if (this.form.minCoverage <= 0 || this.form.maxCoverage <= 0) { this.error = 'Coverage amounts must be greater than zero.'; return; }
        if (this.form.maxCoverage <= this.form.minCoverage) { this.error = 'Maximum coverage must be greater than minimum coverage.'; return; }

        this.loading = true;
        const obs = this.editingPolicy
            ? this.adminService.updatePolicy(this.editingPolicy.id!, this.form)
            : this.adminService.addPolicy(this.form);

        obs.subscribe({
            next: () => {
                this.success = this.editingPolicy ? 'Policy updated!' : 'Policy added!';
                this.showForm = false;
                this.editingPolicy = null;
                this.load();
            },
            error: (err) => { this.error = err.error?.message || 'Operation failed.'; this.loading = false; }
        });
    }

    toggleStatus(policy: Policy): void {
        const action = policy.active ? 'deactivate' : 'activate';
        if (!confirm(`Are you sure you want to ${action} this policy?`)) return;
        this.adminService.togglePolicyStatus(policy.id!).subscribe({
            next: (updated) => {
                this.success = `Policy "${updated.policyName}" is now ${updated.active ? 'Active' : 'Inactive'}.`;
                this.load();
            },
            error: () => { this.error = 'Failed to update policy status.'; }
        });
    }
}
