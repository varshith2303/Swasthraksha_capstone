import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Policy, ApplicationRequest, PolicyMemberRequest, UserService } from '../../../../services/user.service';
import { MemberListComponent } from './member-list/member-list.component';

/**
 * PolicyApplyComponent — SRP: orchestrates the full application form.
 * Delegates member detail rendering to MemberListComponent.
 * Delegates individual member editing to MemberCardComponent (via MemberListComponent).
 */
@Component({
    selector: 'app-policy-apply',
    standalone: true,
    imports: [CommonModule, FormsModule, MemberListComponent],
    template: `
    <!-- Banners -->
    @if (applySuccess()) {
    <div class="flex items-center gap-3 px-5 py-4 rounded-2xl text-sm font-medium mb-2"
        style="background:#ecfdf5; border:1px solid #a7f3d0; color:#059669;">
        <i class="fa-solid fa-circle-check text-base"></i>
        Application <strong class="mx-1">{{ applySuccess() }}</strong> submitted! Awaiting review.
    </div>
    }
    @if (applyError()) {
    <div class="flex items-center gap-3 px-5 py-4 rounded-2xl text-sm font-medium mb-2"
        style="background:#fef2f2; border:1px solid #fecaca; color:#dc2626;">
        <i class="fa-solid fa-circle-exclamation text-base"></i>{{ applyError() }}
    </div>
    }

    <div class="bg-white rounded-2xl shadow-sm overflow-hidden" style="border:1px solid #dbeafe;">

        <!-- Header -->
        <div class="flex items-center gap-4 px-6 py-5 border-b" style="background:#eff6ff80; border-color:#dbeafe;">
            <div class="w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0 shadow-sm"
                style="background:#eff6ff; border:1px solid #bfdbfe; color:#1d4ed8;">
                <i class="fa-solid fa-file-invoice text-xl"></i>
            </div>
            <div>
                <h2 class="text-base font-bold text-gray-900 mb-0.5">Policy Application Form</h2>
                <p class="text-xs text-gray-400">Choose a plan type and fill in all required details</p>
            </div>
        </div>

        <form class="px-7 py-6 flex flex-col gap-0" (ngSubmit)="submitApplication()" #af="ngForm">

            <!-- ─── Section 1: Policy Selection ──────────────────────────── -->
            <div class="py-5 border-b border-gray-100 flex flex-col gap-4">
                <div class="flex items-center gap-2 text-xs font-semibold text-gray-400 uppercase tracking-widest">
                    <span class="w-5 h-5 rounded-full text-white text-[0.65rem] font-bold flex items-center justify-center shadow-sm"
                        style="background:linear-gradient(135deg,#1d4ed8,#2563eb);">1</span>
                    Policy Selection
                </div>
                <div class="flex flex-col gap-1.5">
                    <label class="text-xs font-semibold text-gray-500 tracking-wide" for="policySelect">
                        Select a Policy <span class="text-red-500">*</span>
                    </label>
                    @if (policiesLoading) {
                    <div class="flex items-center gap-2 text-sm text-gray-400 px-4 py-3 rounded-xl"
                        style="background:#f9fafb; border:1.5px solid #e5e7eb;">
                        <span class="inline-block w-3.5 h-3.5 border-2 border-blue-200 border-t-blue-600 rounded-full animate-spin"></span>
                        Loading policies...
                    </div>
                    } @else {
                    <div class="relative group">
                        <select id="policySelect"
                            class="w-full px-4 py-3 rounded-xl text-sm text-gray-900 outline-none transition-all cursor-pointer appearance-none group-hover:border-blue-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/5 shadow-sm"
                            style="background:#f9fafb; border:1.5px solid #e5e7eb;"
                            [(ngModel)]="appForm.policyCode" name="policyCode"
                            (change)="onPolicySelect()" required>
                            <option value="" disabled>— Choose a policy —</option>
                            @for (p of filteredPolicies; track p.id) {
                            <option [value]="p.policyCode">{{ p.policyName }} ({{ p.policyCode }})</option>
                            }
                        </select>
                        <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                            <i class="fa-solid fa-chevron-down text-[0.7rem]"></i>
                        </div>
                    </div>
                    }
                </div>

                <!-- Selected Policy Info Strip -->
                @if (selectedPolicy) {
                <div class="flex flex-wrap gap-3 p-4 rounded-xl shadow-sm"
                    style="background:#eff6ff; border:1px solid #bfdbfe;">
                    <div class="flex flex-col gap-0.5">
                        <span class="text-[0.65rem] font-semibold text-gray-400 uppercase tracking-widest">Coverage Range</span>
                        <span class="text-sm font-semibold text-gray-800">₹{{ selectedPolicy.minCoverage | number }} – ₹{{ selectedPolicy.maxCoverage | number }}</span>
                    </div>
                    <div class="flex flex-col gap-0.5">
                        <span class="text-[0.65rem] font-semibold text-gray-400 uppercase tracking-widest">Base Premium</span>
                        <span class="text-sm font-semibold text-gray-800">{{ selectedPolicy.basePercent }}% of coverage</span>
                    </div>
                    <div class="flex flex-col gap-0.5">
                        <span class="text-[0.65rem] font-semibold text-gray-400 uppercase tracking-widest">Plan Types</span>
                        <span class="text-sm font-semibold text-blue-700">{{ planTypeBadge(selectedPolicy.planType) }}</span>
                    </div>
                </div>
                }
            </div>

            <!-- ─── Section 2: Coverage & Duration ───────────────────────── -->
            <div class="py-5 border-b border-gray-100 flex flex-col gap-4">
                <div class="flex items-center gap-2 text-xs font-semibold text-gray-400 uppercase tracking-widest">
                    <span class="w-5 h-5 rounded-full text-white text-[0.65rem] font-bold flex items-center justify-center shadow-sm"
                        style="background:linear-gradient(135deg,#1d4ed8,#2563eb);">2</span>
                    Coverage &amp; Duration
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-semibold text-gray-500 tracking-wide" for="requestedCoverage">
                            Requested Coverage (₹) <span class="text-red-500">*</span>
                        </label>
                        <div class="relative group">
                            <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-sm font-semibold pointer-events-none">₹</span>
                            <input id="requestedCoverage" type="number"
                                class="w-full pl-8 pr-4 py-3 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm group-hover:border-blue-300 focus:border-blue-500"
                                style="background:#f9fafb; border:1.5px solid #e5e7eb;"
                                [(ngModel)]="appForm.requestedCoverage" name="requestedCoverage" min="0" required
                                [attr.placeholder]="selectedPolicy ? selectedPolicy.minCoverage + ' – ' + selectedPolicy.maxCoverage : 'Select a policy first'" />
                        </div>
                        @if (selectedPolicy && appForm.requestedCoverage > 0 &&
                            (appForm.requestedCoverage < selectedPolicy.minCoverage || appForm.requestedCoverage > selectedPolicy.maxCoverage)) {
                        <p class="text-xs text-red-500 font-medium">
                            Between ₹{{ selectedPolicy.minCoverage | number }} and ₹{{ selectedPolicy.maxCoverage | number }}
                        </p>
                        }
                    </div>
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-semibold text-gray-500 tracking-wide" for="duration">
                            Policy Duration <span class="text-red-500">*</span>
                        </label>
                        <div class="relative group">
                            <select id="duration"
                                class="w-full px-4 py-3 rounded-xl text-sm text-gray-900 outline-none transition-all cursor-pointer appearance-none group-hover:border-blue-300 focus:border-blue-500 shadow-sm"
                                style="background:#f9fafb; border:1.5px solid #e5e7eb;"
                                [(ngModel)]="appForm.duration" name="duration" required>
                                <option [value]="1">1 Year (No Discount)</option>
                                <option [value]="2">2 Years (No Discount)</option>
                                <option [value]="3">3 Years (5% Discount)</option>
                                <option [value]="5">5 Years (10% Discount)</option>
                                <option [value]="10">10 Years (10% Discount)</option>
                            </select>
                            <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                                <i class="fa-solid fa-chevron-down text-[0.7rem]"></i>
                            </div>
                        </div>
                        <p class="text-[0.65rem] font-bold uppercase tracking-wider mt-0.5" style="color:#1d4ed8;">⚡ Save up to 10% on longer plans!</p>
                    </div>
                </div>
            </div>

            <!-- ─── Section 3: Plan Type Toggle ──────────────────────────── -->
            <div class="py-5 border-b border-gray-100 flex flex-col gap-4">
                <div class="flex items-center gap-2 text-xs font-semibold text-gray-400 uppercase tracking-widest">
                    <span class="w-5 h-5 rounded-full text-white text-[0.65rem] font-bold flex items-center justify-center shadow-sm"
                        style="background:linear-gradient(135deg,#1d4ed8,#2563eb);">3</span>
                    Plan Type
                </div>

                <!-- Toggle Pill Group -->
                <div class="flex gap-3">
                    <button type="button" (click)="setPlanType('INDIVIDUAL')"
                        class="flex-1 flex items-center justify-center gap-2.5 py-3 px-4 rounded-xl text-sm font-bold transition-all duration-200 border"
                        [style]="planType === 'INDIVIDUAL'
                            ? 'background:linear-gradient(135deg,#1d4ed8,#2563eb); color:#fff; border-color:transparent; box-shadow:0 4px 14px rgba(37,99,235,0.3);'
                            : 'background:#f9fafb; color:#6b7280; border-color:#e5e7eb;'">
                        <i class="fa-solid fa-user text-base"></i>
                        Individual
                    </button>
                    <button type="button" (click)="setPlanType('FAMILY')"
                        class="flex-1 flex items-center justify-center gap-2.5 py-3 px-4 rounded-xl text-sm font-bold transition-all duration-200 border"
                        [style]="planType === 'FAMILY'
                            ? 'background:linear-gradient(135deg,#7c3aed,#6366f1); color:#fff; border-color:transparent; box-shadow:0 4px 14px rgba(99,102,241,0.3);'
                            : 'background:#f9fafb; color:#6b7280; border-color:#e5e7eb;'">
                        <i class="fa-solid fa-people-roof text-base"></i>
                        Family
                    </button>
                </div>

                <!-- Plan type explanation -->
                @if (planType === 'INDIVIDUAL') {
                <div class="flex items-start gap-3 px-4 py-3 rounded-xl text-sm"
                    style="background:#eff6ff; border:1px solid #bfdbfe; color:#1e40af;">
                    <i class="fa-solid fa-circle-info mt-0.5 flex-shrink-0"></i>
                    <span>Individual plan — covers <strong>you only</strong>. Auto-underwritten for instant decisions on low-risk profiles.</span>
                </div>
                } @else {
                <div class="flex items-start gap-3 px-4 py-3 rounded-xl text-sm"
                    style="background:#f5f3ff; border:1px solid #ddd6fe; color:#5b21b6;">
                    <i class="fa-solid fa-circle-info mt-0.5 flex-shrink-0"></i>
                    <span>Family plan — add <strong>2–6 members</strong>. All share one coverage pool. Manual review required. Get up to <strong>15% multi-member discount</strong>.</span>
                </div>
                }
            </div>

            <!-- ─── Section 4: Member Details ────────────────────────────── -->
            <div class="py-5 flex flex-col gap-4">
                <div class="flex items-center gap-2 text-xs font-semibold text-gray-400 uppercase tracking-widest">
                    <span class="w-5 h-5 rounded-full text-white text-[0.65rem] font-bold flex items-center justify-center shadow-sm"
                        style="background:linear-gradient(135deg,#1d4ed8,#2563eb);">4</span>
                    {{ planType === 'INDIVIDUAL' ? 'Your Health Details' : 'Member Health Details' }}
                </div>

                <app-member-list [members]="appForm.members" [isFamily]="planType === 'FAMILY'">
                </app-member-list>
            </div>

            <!-- ─── Actions ──────────────────────────────────────────────── -->
            <div class="flex items-center justify-end gap-3 pt-5 border-t border-gray-100 mt-2">
                <button type="button" (click)="resetForm()"
                    class="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl text-sm font-bold bg-white text-gray-500 border border-gray-200 hover:bg-gray-50 hover:text-gray-800 transition-all shadow-sm">
                    <i class="fa-solid fa-rotate-left text-xs"></i>Reset
                </button>
                <button id="submit-application-btn" type="submit"
                    [disabled]="applyLoading || af.invalid || !appForm.policyCode || !isMembersValid()"
                    class="inline-flex items-center gap-2 px-8 py-2.5 rounded-xl text-sm font-black text-white transition-all duration-300 hover:-translate-y-0.5 hover:shadow-xl shadow-lg disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                    style="background:linear-gradient(135deg,#1d4ed8,#2563eb); box-shadow:0 4px 14px rgba(37,99,235,0.3);">
                    @if (applyLoading) {
                    <span class="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    } @else {
                    <i class="fa-solid fa-paper-plane text-xs"></i>
                    }
                    {{ applyLoading ? 'Submitting...' : 'Submit Application' }}
                </button>
            </div>

        </form>
    </div>
    `
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

    appForm: ApplicationRequest = this.buildEmptyForm();

    get filteredPolicies(): Policy[] {
        return this.policies.filter(p => {
            const pt = p.planType || 'BOTH';
            return pt === 'BOTH' || pt === this.planType;
        });
    }

    constructor(private userService: UserService) { }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['preselectedPolicyCode'] && this.preselectedPolicyCode) {
            this.appForm.policyCode = this.preselectedPolicyCode;
            this.onPolicySelect();
        }
        if (changes['policies']) {
            this.onPolicySelect();
        }
    }

    setPlanType(type: 'INDIVIDUAL' | 'FAMILY'): void {
        if (this.planType === type) return;
        this.planType = type;
        // Reset members: 1 for individual, 2 for family
        // First one is always SELF
        this.appForm.members = type === 'INDIVIDUAL'
            ? [this.buildEmptyMember('SELF')]
            : [this.buildEmptyMember('SELF'), this.buildEmptyMember('SPOUSE')];
    }

    onPolicySelect(): void {
        this.selectedPolicy = this.policies.find(p => p.policyCode === this.appForm.policyCode) || null;
        if (this.selectedPolicy && (!this.appForm.requestedCoverage || this.appForm.requestedCoverage === 0)) {
            this.appForm.requestedCoverage = this.selectedPolicy.minCoverage;
        }
    }

    planTypeBadge(pt?: string): string {
        if (!pt || pt === 'BOTH') return 'Individual & Family';
        if (pt === 'INDIVIDUAL') return 'Individual Only';
        if (pt === 'FAMILY') return 'Family Only';
        return pt;
    }

    isMembersValid(): boolean {
        if (this.planType === 'FAMILY' && this.appForm.members.length < 2) return false;
        return this.appForm.members.every(m =>
            m.name?.trim() && m.age > 0 && m.bmi > 0 && m.relationship
        );
    }

    submitApplication(): void {
        if (!this.isMembersValid()) return;
        this.applyLoading = true;
        this.applyError.set('');
        this.applySuccess.set('');

        this.userService.applyForPolicy(this.appForm).subscribe({
            next: (res: any) => {
                this.applyLoading = false;
                this.applySuccess.set(res.applicationNumber || 'submitted');
                this.resetForm();
                this.applicationSubmitted.emit();
                setTimeout(() => this.applySuccess.set(''), 6000);
            },
            error: (err: any) => {
                this.applyLoading = false;
                this.applyError.set(this.parseError(err));
            }
        });
    }

    resetForm(): void {
        this.planType = 'INDIVIDUAL';
        this.appForm = this.buildEmptyForm();
        this.selectedPolicy = null;
        this.applyError.set('');
    }

    private buildEmptyForm(): ApplicationRequest {
        return {
            policyCode: '',
            requestedCoverage: 0,
            duration: 1,
            members: [this.buildEmptyMember('SELF')]
        };
    }

    private buildEmptyMember(rel: string = ''): PolicyMemberRequest {
        return { name: '', age: 0, bmi: 0, smoker: false, existingDiseases: '', relationship: rel };
    }

    private parseError(err: any): string {
        const body = err?.error;
        if (body && typeof body === 'object' && body.message) return body.message;
        if (typeof body === 'string' && body.trim()) {
            try { return JSON.parse(body)?.message || body; } catch { return body; }
        }
        return err?.message || 'Failed to submit application. Please try again.';
    }
}
