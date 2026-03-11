import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PolicyAssignment, ClaimRequest, ClaimResponse, UserService } from '../../../../services/user.service';

@Component({
    selector: 'app-my-claims',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <!-- Submit Claim Form Card -->
    <div class="bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm mb-6">

        <!-- Header -->
        <div class="flex items-center gap-4 px-6 py-5 border-b" style="background:#eff6ff80; border-color:#dbeafe;">
            <div class="w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0 shadow-sm"
                style="background: linear-gradient(135deg,#1d4ed8,#2563eb);">
                <i class="fa-solid fa-file-medical text-white text-lg"></i>
            </div>
            <div>
                <h2 class="text-base font-bold text-gray-900 mb-0.5">Submit a Claim</h2>
                <p class="text-xs text-gray-400">Select your active POLICY_ISSUED policy and fill in claim details</p>
            </div>
        </div>

        <!-- Form -->
        <!-- Success / Error banners (outside form so form state doesn't interfere) -->
        @if (submitSuccess()) {
        <div class="flex items-center gap-2.5 px-5 py-3.5 rounded-xl text-sm font-medium"
            style="background:#ecfdf5; border:1px solid #a7f3d0; color:#059669;">
            <i class="fa-solid fa-circle-check text-base mr-1"></i>
            Claim <strong class="mx-1">{{ submitSuccess() }}</strong> submitted successfully!
        </div>
        }
        @if (submitError()) {
        <div class="flex items-center gap-2.5 px-5 py-3.5 rounded-xl text-sm font-medium"
            style="background:#fef2f2; border:1px solid #fecaca; color:#dc2626;">
            <i class="fa-solid fa-circle-exclamation text-base mr-1"></i>{{ submitError() }}
        </div>
        }

        <form class="px-6 py-5 flex flex-col gap-4" (ngSubmit)="onSubmitClaim()" #claimForm="ngForm">

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

                <!-- Policy Dropdown -->
                <div class="flex flex-col gap-1.5 md:col-span-2 group">
                    <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider" for="claimPolicy">
                        Select Policy <span class="text-red-400">*</span>
                    </label>
                    @if (activePolicies.length === 0) {
                    <div class="px-3.5 py-2.5 rounded-xl text-sm italic text-gray-400"
                        style="background:#f9fafb; border:1.5px solid #e5e7eb;">
                        No active POLICY_ISSUED policies found. Your policy must be active to file a claim.
                    </div>
                    } @else {
                    <select id="claimPolicy" name="policyNumber" [(ngModel)]="form.policyNumber" (change)="onPolicyChange()" required
                        class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm group-hover:border-blue-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/5"
                        style="background:#f9fafb; border:1.5px solid #e5e7eb;">
                        <option value="">-- Select a policy --</option>
                        @for (p of activePolicies; track p.id) {
                        <option [value]="p.policyNumber">
                            {{ p.application.policy.policyName }} · {{ p.policyNumber }} · ₹{{ p.coverageAmount | number }}
                        </option>
                        }
                    </select>
                    }
                </div>

                <!-- Patient Dropdown -->
                @if (selectedPolicy && selectedPolicy.application.planType === 'FAMILY') {
                <div class="flex flex-col gap-1.5 md:col-span-2 group">
                    <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider" for="claimPatient">
                        Select Patient/Member <span class="text-red-400">*</span>
                    </label>
                    <select id="claimPatient" name="memberId" [(ngModel)]="form.memberId" required
                        class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm group-hover:border-blue-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/5"
                        style="background:#f9fafb; border:1.5px solid #e5e7eb;">
                        <option [ngValue]="undefined">-- Select a member --</option>
                        @for (m of selectedPolicy.application.members; track m.id) {
                        <option [ngValue]="m.id">
                            {{ m.name }} ({{ m.relationship || 'Member' }}) - Age: {{ m.age }}
                        </option>
                        }
                    </select>
                </div>
                }

                <!-- Policy Summary Strip -->
                @if (selectedPolicy) {
                <div class="md:col-span-2 grid grid-cols-3 gap-3">
                    <div class="flex flex-col gap-0.5 px-4 py-3 rounded-xl"
                        style="background:#eff6ff; border:1px solid #bfdbfe;">
                        <span class="text-[10px] font-bold text-blue-400 uppercase tracking-widest">Coverage</span>
                        <span class="text-sm font-bold text-blue-800">₹{{ selectedPolicy.coverageAmount | number }}</span>
                    </div>
                    <div class="flex flex-col gap-0.5 px-4 py-3 rounded-xl"
                        [style]="(selectedPolicy.remainingCoverage / selectedPolicy.coverageAmount) < 0.25
                            ? 'background:#fef2f2; border:1px solid #fecaca;'
                            : 'background:#ecfdf5; border:1px solid #a7f3d0;'">
                        <span class="text-[10px] font-bold uppercase tracking-widest"
                            [style]="(selectedPolicy.remainingCoverage / selectedPolicy.coverageAmount) < 0.25 ? 'color:#dc2626' : 'color:#059669'">Remaining</span>
                        <span class="text-sm font-bold"
                            [style]="(selectedPolicy.remainingCoverage / selectedPolicy.coverageAmount) < 0.25 ? 'color:#dc2626' : 'color:#059669'">
                            ₹{{ selectedPolicy.remainingCoverage | number }}
                        </span>
                    </div>
                    <div class="flex flex-col gap-0.5 px-4 py-3 rounded-xl"
                        style="background:#f9fafb; border:1px solid #e5e7eb;">
                        <span class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Latest Claim</span>
                        <span class="text-sm font-bold text-gray-700">{{ latestClaimDate(selectedPolicy.policyNumber) }}</span>
                    </div>
                </div>
                }

                <!-- Claim Amount -->
                <div class="flex flex-col gap-1.5 group">
                    <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider" for="claimAmount">
                        Claim Amount (₹) <span class="text-red-400">*</span>
                    </label>
                    <input id="claimAmount" type="number" name="claimAmount" [(ngModel)]="form.claimAmount"
                        required min="1"
                        placeholder="e.g. 50000"
                        class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm"
                        style="background:#f9fafb; border:1.5px solid #e5e7eb;" />
                </div>

                <!-- Hospital Name -->
                <div class="flex flex-col gap-1.5 group">
                    <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider" for="hospitalName">
                        Hospital Name <span class="text-red-400">*</span>
                    </label>
                    <input id="hospitalName" type="text" name="hospitalName" [(ngModel)]="form.hospitalName"
                        required placeholder="e.g. Apollo Hospitals, Bangalore"
                        class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm"
                        style="background:#f9fafb; border:1.5px solid #e5e7eb;" />
                </div>

                <!-- Admission Date -->
                <div class="flex flex-col gap-1.5 group">
                    <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider" for="admissionDate">
                        Admission Date <span class="text-red-400">*</span>
                    </label>
                    <input id="admissionDate" type="date" name="admissionDate" [(ngModel)]="form.admissionDate"
                        required
                        class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm"
                        style="background:#f9fafb; border:1.5px solid #e5e7eb;" />
                </div>

                <!-- Discharge Date -->
                <div class="flex flex-col gap-1.5 group">
                    <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider" for="dischargeDate">
                        Discharge Date <span class="text-red-400">*</span>
                    </label>
                    <input id="dischargeDate" type="date" name="dischargeDate" [(ngModel)]="form.dischargeDate"
                        required
                        class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm"
                        style="background:#f9fafb; border:1.5px solid #e5e7eb;" />
                </div>

                <!-- Claim Reason -->
                <div class="flex flex-col gap-1.5 md:col-span-2 group">
                    <label class="text-xs font-semibold text-gray-500 uppercase tracking-wider" for="claimReason">
                        Reason for Claim <span class="text-red-400">*</span>
                    </label>
                    <textarea id="claimReason" name="claimReason" [(ngModel)]="form.claimReason"
                        required rows="3"
                        placeholder="Describe the medical condition, diagnosis, or procedure..."
                        class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm resize-none"
                        style="background:#f9fafb; border:1.5px solid #e5e7eb;">
                    </textarea>
                </div>

            </div>

            <!-- Submit -->
            <div class="flex justify-end pt-1 border-t border-gray-100 mt-1">
                <button id="submit-claim-btn" type="submit"
                    [disabled]="submitting || claimForm.invalid || activePolicies.length === 0"
                    class="inline-flex items-center gap-2 px-7 py-2.5 rounded-xl text-sm font-bold text-white shadow-lg transition-all hover:-translate-y-0.5 hover:shadow-xl disabled:opacity-50 disabled:cursor-not-allowed"
                    style="background:linear-gradient(135deg,#1d4ed8,#2563eb); box-shadow:0 4px 14px rgba(37,99,235,0.3);">
                    @if (submitting) {
                    <span class="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    } @else {
                    <i class="fa-solid fa-paper-plane text-xs"></i>
                    }
                    Submit Claim
                </button>
            </div>
        </form>
    </div>

    <!-- Claims History -->
    <div>
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-bold text-gray-800">My Claim History</h2>
            <button (click)="loadClaims()"
                class="p-2 rounded-xl text-gray-400 hover:text-blue-600 hover:bg-blue-50 transition-all border border-transparent hover:border-blue-100"
                title="Refresh" [disabled]="loadingClaims">
                <i class="fa-solid fa-arrows-rotate" [class.animate-spin]="loadingClaims"></i>
            </button>
        </div>

        @if (loadingClaims) {
        <div class="flex items-center justify-center gap-3 p-16 bg-white rounded-2xl border border-gray-100 shadow-sm">
            <span class="inline-block w-7 h-7 border-[3px] border-blue-200 border-t-blue-600 rounded-full animate-spin"></span>
            <p class="text-sm text-gray-400">Loading claims...</p>
        </div>
        } @else if (claims().length === 0) {
        <div class="flex flex-col items-center justify-center gap-4 p-16 bg-white rounded-2xl border border-gray-100 shadow-sm text-gray-400">
            <i class="fa-solid fa-file-medical text-5xl opacity-20"></i>
            <p class="text-sm font-medium">No claims submitted yet.</p>
        </div>
        } @else {
        <div class="bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm">
            <div class="overflow-x-auto">
                <table class="w-full border-collapse text-sm">
                    <thead>
                        <tr style="background:#eff6ff; border-bottom:1.5px solid #dbeafe;">
                            <th class="px-4 py-3.5 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Claim Details</th>
                            <th class="px-4 py-3.5 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Submitted</th>
                            <th class="px-4 py-3.5 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Hospital</th>
                            <th class="px-4 py-3.5 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Reason</th>
                            <th class="px-4 py-3.5 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Claim Amt</th>
                            <th class="px-4 py-3.5 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Approved Amt</th>
                            <th class="px-4 py-3.5 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Admission</th>
                            <th class="px-4 py-3.5 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Discharge</th>
                            <th class="px-4 py-3.5 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        @for (c of claims(); track c.claimNumber) {
                        <tr class="border-b border-gray-50 hover:bg-blue-50/30 transition-colors">
                            <td class="px-4 py-3.5 flex flex-col gap-1 items-start">
                                <span class="font-mono text-[10px] font-bold px-1.5 py-0.5 rounded shadow-sm"
                                    style="background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe;">
                                    {{ c.claimNumber }}
                                </span>
                                <span class="text-xs font-semibold text-gray-800">{{  c.policyNumber }}</span>
                                @if (c.patientName) {
                                <span class="text-[10px] font-medium text-gray-500"><i class="fa-solid fa-user text-[9px] mr-1"></i>{{ c.patientName }}</span>
                                }
                            </td>
                            <td class="px-4 py-3.5 text-gray-500 text-xs">{{ c.submittedDate ? (c.submittedDate | date:'mediumDate') : '—' }}</td>
                            <td class="px-4 py-3.5 text-gray-700 text-xs font-medium">{{ c.hospitalName }}</td>
                            <td class="px-4 py-3.5 text-gray-500 text-xs max-w-[180px]">
                                <span class="line-clamp-2">{{ c.claimReason || '—' }}</span>
                            </td>
                            <td class="px-4 py-3.5 text-gray-800 font-semibold text-xs">₹{{ c.claimAmount | number }}</td>
                            <td class="px-4 py-3.5 text-xs font-semibold" style="color:#059669;">
                                {{ c.approvedAmount > 0 ? ('₹' + (c.approvedAmount | number)) : '—' }}
                            </td>
                            <td class="px-4 py-3.5 text-gray-500 text-xs">{{ c.admissionDate | date:'mediumDate' }}</td>
                            <td class="px-4 py-3.5 text-gray-500 text-xs">{{ c.dischargeDate | date:'mediumDate' }}</td>
                            <td class="px-4 py-3.5">
                                <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold"
                                    [style]="getClaimStatusStyle(c.status)">
                                    <span class="w-1.5 h-1.5 rounded-full bg-current"></span>
                                    {{ c.status }}
                                </span>
                            </td>
                        </tr>
                        }
                    </tbody>
                </table>
            </div>
            <div class="px-4 py-3 text-xs text-gray-400 border-t border-gray-50 font-medium" style="background:#eff6ff50;">
                {{ claims().length }} claim{{ claims().length === 1 ? '' : 's' }} total
            </div>
        </div>
        }
    </div>
    `
})
export class MyClaimsComponent implements OnInit {
    @Input() activePolicies: PolicyAssignment[] = [];

    claims = signal<ClaimResponse[]>([]);
    loadingClaims = false;
    submitting = false;
    submitSuccess = signal('');
    submitError = signal('');

    form: ClaimRequest = {
        policyNumber: '',
        claimAmount: 0,
        hospitalName: '',
        claimReason: '',
        admissionDate: '',
        dischargeDate: ''
    };

    constructor(private userService: UserService) { }

    ngOnInit(): void {
        this.loadClaims();
    }

    onPolicyChange(): void {
        this.form.memberId = undefined;
    }

    loadClaims(): void {
        this.loadingClaims = true;
        this.userService.getMyClaims().subscribe({
            next: (data) => { this.claims.set(data); this.loadingClaims = false; },
            error: () => { this.loadingClaims = false; }
        });
    }

    onSubmitClaim(): void {
        this.submitSuccess.set('');
        this.submitError.set('');
        this.submitting = true;
        this.userService.submitClaim(this.form).subscribe({
            next: (res) => {
                this.submitting = false;
                this.submitSuccess.set(res.claimNumber);
                this.resetForm();
                this.loadClaims();
            },
            error: (err) => {
                this.submitting = false;
                this.submitError.set(this.parseError(err));
            }
        });
    }

    get selectedPolicy(): PolicyAssignment | null {
        return this.activePolicies.find(p => p.policyNumber === this.form.policyNumber) ?? null;
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
            dischargeDate: ''
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

    /**
     * Robustly extracts a human-readable message from Angular's HttpErrorResponse.
     * Angular can deliver err.error as:
     *   - A parsed JS object  → err.error.message  (ideal, but not always)
     *   - A raw JSON string   → need JSON.parse()  (happens when Content-Type isn't matched)
     *   - null/undefined      → network/CORS error, use err.message
     */
    parseError(err: any, fallback = 'An unexpected error occurred. Please try again.'): string {
        const body = err?.error;

        // Case 1: Angular already parsed the JSON body into an object
        if (body !== null && typeof body === 'object' && body.message) {
            return body.message;
        }

        // Case 2: Angular delivered the body as a raw string (unparsed JSON)
        if (typeof body === 'string' && body.trim().length > 0) {
            try {
                const parsed = JSON.parse(body);
                if (parsed?.message) return parsed.message;
                return body; // return raw string if no message field
            } catch {
                return body; // not JSON — use as-is
            }
        }

        // Case 3: Network error / CORS block / no response body
        return err?.message || fallback;
    }
}
