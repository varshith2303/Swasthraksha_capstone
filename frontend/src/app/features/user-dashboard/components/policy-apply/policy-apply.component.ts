import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Policy, ApplicationRequest, PolicyMemberRequest, UserService } from '../../../../services/user.service';
import { MemberListComponent } from './member-list/member-list.component';

@Component({
    selector: 'app-policy-apply',
    standalone: true,
    imports: [CommonModule, FormsModule, MemberListComponent],
    templateUrl: './policy-apply.component.html'
})
export class PolicyApplyComponent implements OnChanges {
    @Input() policies: Policy[] = [];
    @Input() policiesLoading: boolean = false;
    @Input() preselectedPolicyCode: string = '';

    @Output() applicationSubmitted = new EventEmitter<void>();

    planType: 'INDIVIDUAL' | 'FAMILY' = 'INDIVIDUAL';
    selectedPolicy: Policy | null = null;
    applyLoading = false;
    applySuccess = signal('');
    applyError = signal('');

    form = {
        policyCode: '',
        requestedCoverage: 0,
        duration: 1
    };

    members: PolicyMemberRequest[] = [];

    constructor(private userService: UserService) {
        this.resetMembers();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['preselectedPolicyCode'] && this.preselectedPolicyCode) {
            this.form.policyCode = this.preselectedPolicyCode;
            this.onPolicyChange();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    }

    togglePlanType(type: 'INDIVIDUAL' | 'FAMILY'): void {
        this.planType = type;
        this.resetMembers();
    }

    resetMembers(): void {
        this.members = [
            {
                name: '',
                age: 0,
                bmi: 0,
                smoker: false,
                existingDiseases: '',
                relationship: 'SELF'
            }
        ];
    }

    onPolicyChange(): void {
        this.selectedPolicy = this.policies.find(p => p.policyCode === this.form.policyCode) || null;
        if (this.selectedPolicy) {
            this.form.requestedCoverage = this.selectedPolicy.minCoverage;
            if (this.selectedPolicy.planType === 'INDIVIDUAL') {
                this.planType = 'INDIVIDUAL';
                this.resetMembers();
            } else if (this.selectedPolicy.planType === 'FAMILY') {
                this.planType = 'FAMILY';
                this.resetMembers();
            }
        } else {
            this.form.requestedCoverage = 0;
        }
    }

    get estimatedPremium(): number {
        if (!this.selectedPolicy || this.form.requestedCoverage <= 0 || !this.members.length || this.form.duration < 1) return 0;

        const baseRate = this.selectedPolicy.basePercent / 100;
        let basePremium = this.form.requestedCoverage * baseRate;

        // Base risk score = max age among members
        let riskScore = 0;
        for (const m of this.members) {
            if (m.age > riskScore) riskScore = m.age;
        }

        // Apply health modifiers dynamically
        for (const m of this.members) {
            if (m.smoker) riskScore += 10;
            if (m.bmi > 30) riskScore += 5;
            if (m.existingDiseases && m.existingDiseases.trim().length > 0) riskScore += 15;
            if (m.age > 50) riskScore += 10;
        }

        // Apply age multiplier to base premium
        basePremium *= (1 + (riskScore / 100));

        // Family discounts
        if (this.planType === 'FAMILY' && this.members.length > 1) {
            const extraMembers = this.members.length - 1;
            const discount = Math.min(extraMembers * 0.05, 0.15); // Max 15% discount for families
            basePremium *= (1 - discount);
        }

        return basePremium * this.form.duration;
    }

    onSubmit(): void {
        this.applyError.set('');
        this.applySuccess.set('');

        // Basic validation
        if (!this.form.policyCode) {
            this.applyError.set('Please select a policy.');
            return;
        }

        // Validate members
        for (let i = 0; i < this.members.length; i++) {
            const m = this.members[i];
            if (!m.name || m.age <= 0 || m.bmi <= 0) {
                this.applyError.set(`Please fill in all required fields for Member ${i + 1}.`);
                return;
            }
        }

        const request: ApplicationRequest = {
            policyCode: this.form.policyCode,
            requestedCoverage: this.form.requestedCoverage,
            duration: this.form.duration,
            members: this.members
        };

        this.applyLoading = true;
        this.userService.applyForPolicy(request).subscribe({
            next: (res) => {
                this.applyLoading = false;
                this.applySuccess.set(res.applicationNumber!);
                this.resetForm();
                this.applicationSubmitted.emit();
            },
            error: (err) => {
                this.applyLoading = false;
                this.applyError.set(err.error?.message || 'Failed to submit application. Please verify all details or try again later.');
            }
        });
    }

    resetForm(): void {
        this.form = {
            policyCode: '',
            requestedCoverage: 0,
            duration: 1
        };
        this.selectedPolicy = null;
        this.resetMembers();
    }
}
