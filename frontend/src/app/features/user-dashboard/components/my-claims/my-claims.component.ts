import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PolicyAssignment, ClaimRequest, ClaimResponse, UserService } from '../../../../services/user.service';

@Component({
    selector: 'app-my-claims',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './my-claims.component.html'
})
export class MyClaimsComponent implements OnInit {
    @Input() activePolicies: PolicyAssignment[] = [];

    claims = signal<ClaimResponse[]>([]);
    loadingClaims = false;
    submitting = false;
    submitSuccess = '';
    submitError = '';

    form: ClaimRequest = {
        policyNumber: '',
        claimAmount: 0,
        hospitalName: '',
        claimReason: '',
        admissionDate: '',
        dischargeDate: '',
        memberId: undefined
    };

    constructor(private userService: UserService) { }

    ngOnInit(): void {
        this.loadClaims();
    }

    loadClaims(): void {
        this.loadingClaims = true;
        this.userService.getMyClaims().subscribe({
            next: (data) => { this.claims.set(data); this.loadingClaims = false; },
            error: () => { this.loadingClaims = false; }
        });
    }

    onSubmitClaim(): void {
        this.submitSuccess = '';
        this.submitError = '';
        this.submitting = true;
        this.userService.submitClaim(this.form).subscribe({
            next: (res) => {
                this.submitting = false;
                this.submitSuccess = res.claimNumber;
                this.resetForm();
                this.loadClaims();
            },
            error: (err) => {
                this.submitting = false;
                this.submitError = err.error?.message || 'Failed to submit claim. Please try again.';
            }
        });
    }

    get selectedPolicy(): PolicyAssignment | null {
        return this.activePolicies.find(p => p.policyNumber === this.form.policyNumber) ?? null;
    }

    onPolicyChange(): void {
        this.form.memberId = undefined;
    }

    latestClaimDate(policyNumber: string): string {
        const dates = this.claims()
            .filter(c => c.policyNumber === policyNumber && c.submittedDate)
            .map(c => c.submittedDate!);
        if (dates.length === 0) return 'No claims yet';
        const latest = dates.sort().at(-1)!;
        return new Date(latest).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
    }

    resetForm(): void {
        this.form = {
            policyNumber: '',
            claimAmount: 0,
            hospitalName: '',
            claimReason: '',
            admissionDate: '',
            dischargeDate: '',
            memberId: undefined
        };
    }

    getClaimStatusStyle(status: string): string {
        switch (status) {
            case 'PENDING': return 'background:#fefce8; color:#ca8a04; border:1px solid #fde68a;';
            case 'APPROVED': return 'background:#ecfdf5; color:#059669; border:1px solid #a7f3d0;';
            case 'REJECTED': return 'background:#fef2f2; color:#dc2626; border:1px solid #fecaca;';
            default: return 'background:#f9fafb; color:#6b7280; border:1px solid #e5e7eb;';
        }
    }
}
