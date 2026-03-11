import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService, Application } from '../../../../services/user.service';

@Component({
    selector: 'app-my-applications',
    standalone: true,
    imports: [CommonModule],
    template: `
        @if (myAppsError) {
        <div class="flex items-center gap-3 px-4 py-3.5 rounded-xl text-sm font-medium animate-fade-in"
            style="background: #fef2f2; border: 1px solid #fecaca; color: #dc2626;">
            <i class="fa-solid fa-circle-exclamation text-base"></i>{{ myAppsError }}
        </div>
        }

        @if (myAppsLoading) {
        <div
            class="flex flex-col items-center justify-center gap-4 p-20 bg-white rounded-2xl border border-gray-100 shadow-sm text-gray-400">
            <span
                class="inline-block w-9 h-9 border-[3px] border-blue-200 border-t-blue-600 rounded-full animate-spin"></span>
            <p class="text-sm">Loading your applications...</p>
        </div>
        } @else if (applications.length === 0) {
        <div
            class="flex flex-col items-center justify-center gap-4 p-20 bg-white rounded-2xl border border-gray-100 shadow-sm text-gray-400">
            <i class="fa-solid fa-folder-open text-4xl opacity-30"></i>
            <p class="text-sm font-medium">No applications found.</p>
        </div>
        } @else {
        <div class="grid grid-cols-[repeat(auto-fill,minmax(400px,1fr))] gap-5">
            @for (app of applications; track app.id) {
            <div
                class="bg-white rounded-2xl p-6 flex flex-col gap-5 border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300">
                <div class="flex items-start justify-between gap-4">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 shadow-sm"
                            [style]="app.planType === 'FAMILY' ? 'background:#f5f3ff; border:1px solid #ddd6fe; color:#7c3aed;' : 'background:#eff6ff; border:1px solid #bfdbfe; color:#1d4ed8;'">
                            <i [class]="app.planType === 'FAMILY' ? 'fa-solid fa-people-roof text-base' : 'fa-solid fa-shield-heart text-base'"></i>
                        </div>
                        <div>
                            <h3 class="text-[0.95rem] font-bold text-gray-900 mb-0.5">{{ app.policy.policyName }}</h3>
                            <div class="flex items-center gap-2">
                                <span class="inline-block text-[0.72rem] font-semibold font-mono px-2 py-0.5 rounded shadow-sm"
                                    style="background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe;">{{ app.policy.policyCode }}</span>
                                <span class="inline-flex items-center gap-1 text-[0.68rem] font-bold px-2 py-0.5 rounded-full"
                                    [style]="app.planType === 'FAMILY' ? 'background:#f5f3ff; color:#7c3aed; border:1px solid #ddd6fe;' : 'background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe;'">
                                    <i [class]="app.planType === 'FAMILY' ? 'fa-solid fa-people-roof' : 'fa-solid fa-user'"></i>
                                    {{ app.planType === 'FAMILY' ? 'Family' : 'Individual' }}
                                </span>
                            </div>
                        </div>
                    </div>
                    <span [ngClass]="getStatusClass(app.status)"
                        class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[0.72rem] font-bold tracking-wide border whitespace-nowrap shadow-sm">
                        <span class="w-1.5 h-1.5 rounded-full bg-current opacity-70"></span>
                        {{ getStatusLabel(app.status) }}
                    </span>
                 </div>
                <div class="grid grid-cols-3 gap-2">
                    <div class="flex flex-col gap-0.5 rounded-lg px-3 py-2.5"
                        style="background: #f9fafb; border: 1px solid #f3f4f6;">
                        <span class="text-[0.62rem] font-semibold text-gray-400 uppercase tracking-widest">Coverage</span>
                        <span class="text-sm font-semibold text-gray-800">₹{{ app.requestedCoverage | number }}</span>
                    </div>
                    <div class="flex flex-col gap-0.5 rounded-lg px-3 py-2.5"
                        style="background: #f9fafb; border: 1px solid #f3f4f6;">
                        <span class="text-[0.62rem] font-semibold text-gray-400 uppercase tracking-widest">Duration</span>
                        <span class="text-sm font-semibold text-gray-800">{{ app.duration }} Yr{{ app.duration > 1 ? 's' : '' }}</span>
                    </div>
                    <div class="flex flex-col gap-0.5 rounded-lg px-3 py-2.5"
                        style="background: #f9fafb; border: 1px solid #f3f4f6;">
                        <span class="text-[0.62rem] font-semibold text-gray-400 uppercase tracking-widest">Members</span>
                        <span class="text-sm font-semibold text-gray-800">
                            {{ app.planType === 'FAMILY' ? ((app.members?.length || 0) + ' members') : 'Self only' }}
                        </span>
                    </div>
                    @if (app.riskScore != null) {
                    <div class="flex flex-col gap-0.5 rounded-lg px-3 py-2.5"
                        style="background: #f9fafb; border: 1px solid #f3f4f6;">
                        <span class="text-[0.62rem] font-semibold text-gray-400 uppercase tracking-widest">Risk Score</span>
                        <span class="text-sm font-semibold text-gray-800">{{ app.riskScore.toFixed(2) }}</span>
                    </div>
                    }
                    @if (app.finalPremium) {
                    <div class="flex flex-col gap-0.5 rounded-lg px-3 py-2.5 col-span-2 shadow-sm"
                        style="background: #eff6ff; border: 1px solid #bfdbfe;">
                        <span class="text-[0.62rem] font-semibold text-gray-400 uppercase tracking-widest">Final Premium</span>
                        <span class="text-sm font-bold" style="color: #1d4ed8;">₹{{ app.finalPremium.toFixed(2) }}/yr</span>
                    </div>
                    }
                </div>

                @if (app.status === 'WAITING_CUSTOMER_ACCEPTANCE') {
                <div class="flex gap-3 border-t border-gray-100 pt-5 mt-auto">
                    <button id="decline-offer-btn" (click)="onDecline(app)" [disabled]="actionLoading[app.id]"
                        class="flex-1 py-2.5 px-4 rounded-xl text-sm font-bold border border-red-200 text-red-600 hover:bg-red-50 transition-all flex items-center justify-center gap-2">
                        @if (actionLoading[app.id]) {
                        <span
                            class="w-4 h-4 border-2 border-red-600/30 border-t-red-600 rounded-full animate-spin"></span>
                        } @else {
                        <i class="fa-solid fa-xmark"></i>
                        }
                        Decline
                    </button>
                    <button id="accept-offer-btn" (click)="onAccept(app)" [disabled]="actionLoading[app.id]"
                        class="flex-1 py-2.5 px-4 rounded-xl text-sm font-bold text-white transition-all flex items-center justify-center gap-2 shadow-lg hover:shadow-xl disabled:opacity-50 hover:-translate-y-0.5"
                        style="background: linear-gradient(135deg, #1d4ed8, #2563eb); box-shadow: 0 4px 14px rgba(37,99,235,0.3);">
                        @if (actionLoading[app.id]) {
                        <span class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                        } @else {
                        <i class="fa-solid fa-check"></i>
                        }
                        Accept & Pay
                    </button>
                </div>
                }
            </div>
            }
        </div>
        }
  `
})
export class MyApplicationsComponent {
    @Input() applications: Application[] = [];
    @Input() myAppsLoading: boolean = false;
    @Input() myAppsError: string = '';
    @Input() actionLoading: Record<number, boolean> = {};

    @Output() statusUpdated = new EventEmitter<void>();

    constructor(private userService: UserService) { }

    getStatusClass(status: string): string {
        switch (status) {
            case 'DRAFT': return 'bg-slate-50 text-slate-500 border-slate-200';
            case 'SUBMITTED': return 'bg-blue-50 text-blue-600 border-blue-200';
            case 'UNDER_REVIEW': return 'bg-amber-50 text-amber-600 border-amber-200';
            case 'WAITING_CUSTOMER_ACCEPTANCE': return 'bg-indigo-50 text-indigo-600 border-indigo-200';
            case 'APPROVED':
            case 'CUSTOMER_ACCEPTED':
            case 'POLICY_ISSUED': return 'bg-emerald-50 text-emerald-600 border-emerald-200';
            case 'REJECTED':
            case 'CUSTOMER_DECLINED': return 'bg-red-50 text-red-600 border-red-200';
            default: return 'bg-slate-50 text-slate-500 border-slate-200';
        }
    }

    getStatusLabel(status: string): string {
        switch (status) {
            case 'DRAFT': return 'Draft';
            case 'SUBMITTED': return 'Submitted';
            case 'UNDER_REVIEW': return 'Under Review';
            case 'WAITING_CUSTOMER_ACCEPTANCE': return 'Offer Received';
            case 'APPROVED': return 'Approved';
            case 'CUSTOMER_ACCEPTED': return 'Accepted';
            case 'CUSTOMER_DECLINED': return 'Declined';
            case 'POLICY_ISSUED': return 'Policy Issued';
            case 'REJECTED': return 'Rejected';
            default: return status.replace(/_/g, ' ');
        }
    }

    onAccept(app: Application) {
        if (!app.applicationNumber) return;
        this.actionLoading[app.id] = true;
        this.userService.acceptApplication(app.applicationNumber).subscribe({
            next: () => {
                this.actionLoading[app.id] = false;
                this.statusUpdated.emit();
            },
            error: () => this.actionLoading[app.id] = false
        });
    }

    onDecline(app: Application) {
        this.actionLoading[app.id] = true;
        this.userService.declineApplication(app.id).subscribe({
            next: () => {
                this.actionLoading[app.id] = false;
                this.statusUpdated.emit();
            },
            error: () => this.actionLoading[app.id] = false
        });
    }
}
