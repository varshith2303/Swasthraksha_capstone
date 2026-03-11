import { Component, Input, Output, EventEmitter, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Policy } from '../../../../services/user.service';

/**
 * PolicyCatalogComponent — SRP: displays and filters the policy browse grid.
 * Owns only display and filtering logic; delegates apply action via event.
 */
@Component({
    selector: 'app-policy-catalog',
    standalone: true,
    imports: [CommonModule],
    template: `
        <!-- Filter Tabs -->
        <div class="flex items-center gap-2 mb-6 flex-wrap">
            @for (tab of tabs; track tab.value) {
            <button (click)="activeFilter.set(tab.value)"
                class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-bold transition-all duration-200 border"
                [style]="activeFilter() === tab.value ? tab.activeStyle : 'background:#fff; color:#6b7280; border-color:#e5e7eb;'">
                <i [class]="tab.icon"></i>
                {{ tab.label }}
                <span class="text-[0.7rem] px-1.5 py-0.5 rounded-full font-bold"
                    [style]="activeFilter() === tab.value ? 'background:rgba(255,255,255,0.25);' : 'background:#f3f4f6; color:#9ca3af;'">
                    {{ tab.count() }}
                </span>
            </button>
            }
        </div>

        @if (policiesLoading) {
        <div class="flex flex-col items-center justify-center gap-4 p-20 bg-white rounded-2xl border border-gray-100 shadow-sm text-gray-400">
            <span class="inline-block w-9 h-9 border-[3px] border-blue-200 border-t-blue-600 rounded-full animate-spin"></span>
            <p class="text-sm">Loading policies...</p>
        </div>
        } @else if (filteredPolicies().length === 0) {
        <div class="flex flex-col items-center justify-center gap-4 p-20 bg-white rounded-2xl border border-gray-100 shadow-sm text-gray-400">
            <i class="fa-solid fa-file-circle-xmark text-4xl opacity-30"></i>
            <p class="text-sm font-medium">No {{ activeFilter() === 'ALL' ? '' : activeFilter().toLowerCase() }} plans available at the moment.</p>
        </div>
        } @else {
        <div class="grid grid-cols-[repeat(auto-fill,minmax(290px,1fr))] gap-5">
            @for (policy of filteredPolicies(); track policy.id) {
            <div class="bg-white rounded-2xl p-6 flex flex-col gap-3.5 border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group">

                <!-- Card Header -->
                <div class="flex items-center justify-between">
                    <div class="w-11 h-11 rounded-xl flex items-center justify-center shadow-sm transition-transform group-hover:scale-105"
                        [style]="planIconStyle(policy.planType)">
                        <i [class]="planIcon(policy.planType) + ' text-xl'"></i>
                    </div>
                    <div class="flex items-center gap-2">
                        <span class="inline-flex items-center px-2.5 py-1 rounded-full text-[0.7rem] font-bold"
                            [style]="planTypePillStyle(policy.planType)">
                            <i [class]="planIcon(policy.planType) + ' mr-1 text-[0.6rem]'"></i>
                            {{ planTypeLabel(policy.planType) }}
                        </span>
                        <span class="inline-flex items-center px-2.5 py-1 rounded-full text-[0.72rem] font-bold shadow-sm"
                            style="background:#ecfdf5; color:#059669; border:1px solid #a7f3d0;">Active</span>
                    </div>
                </div>

                <!-- Info -->
                <div>
                    <h3 class="text-base font-bold text-gray-900 mb-1">{{ policy.policyName }}</h3>
                    <span class="inline-block text-[0.72rem] font-semibold font-mono px-2 py-0.5 rounded shadow-sm"
                        style="background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe;">{{ policy.policyCode }}</span>
                </div>

                <!-- Stats -->
                <div class="grid grid-cols-2 gap-2">
                    <div class="flex flex-col gap-0.5 rounded-lg px-3 py-2.5"
                        style="background:#f9fafb; border:1px solid #f3f4f6;">
                        <span class="text-[0.62rem] font-semibold text-gray-400 uppercase tracking-widest">Min Coverage</span>
                        <span class="text-sm font-bold text-gray-800">₹{{ policy.minCoverage | number }}</span>
                    </div>
                    <div class="flex flex-col gap-0.5 rounded-lg px-3 py-2.5"
                        style="background:#f9fafb; border:1px solid #f3f4f6;">
                        <span class="text-[0.62rem] font-semibold text-gray-400 uppercase tracking-widest">Max Coverage</span>
                        <span class="text-sm font-bold text-gray-800">₹{{ policy.maxCoverage | number }}</span>
                    </div>
                    <div class="flex flex-col gap-0.5 rounded-lg px-3 py-2.5 col-span-2 shadow-sm"
                        style="background:#eff6ff; border:1px solid #bfdbfe;">
                        <span class="text-[0.62rem] font-semibold text-gray-400 uppercase tracking-widest">Base Premium Rate</span>
                        <span class="text-lg font-bold" style="color:#1d4ed8;">{{ policy.basePercent }}%</span>
                    </div>
                </div>

                <!-- Apply Button -->
                <button
                    class="w-full mt-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-bold text-white transition-all duration-300 hover:-translate-y-0.5 hover:shadow-xl shadow-lg"
                    style="background:linear-gradient(135deg,#1d4ed8,#2563eb); box-shadow:0 4px 12px rgba(37,99,235,0.25);"
                    (click)="applyPolicy.emit(policy.policyCode)">
                    <i class="fa-solid fa-file-signature text-xs"></i>
                    Apply for This Policy
                </button>
            </div>
            }
        </div>
        }
    `
})
export class PolicyCatalogComponent {
    @Input() policies: Policy[] = [];
    @Input() policiesLoading: boolean = false;

    @Output() applyPolicy = new EventEmitter<string>();

    activeFilter = signal<'ALL' | 'INDIVIDUAL' | 'FAMILY'>('ALL');

    filteredPolicies = computed(() => {
        const f = this.activeFilter();
        if (f === 'ALL') return this.policies;
        return this.policies.filter(p => {
            const pt = p.planType || 'BOTH';
            return pt === 'BOTH' || pt === f;
        });
    });

    tabs = [
        {
            value: 'ALL' as const,
            label: 'All Plans',
            icon: 'fa-solid fa-layer-group',
            activeStyle: 'background:linear-gradient(135deg,#1d4ed8,#2563eb); color:#fff; border-color:transparent; box-shadow:0 4px 12px rgba(37,99,235,0.25);',
            count: computed(() => this.policies.length)
        },
        {
            value: 'INDIVIDUAL' as const,
            label: 'Individual',
            icon: 'fa-solid fa-user',
            activeStyle: 'background:linear-gradient(135deg,#0284c7,#0369a1); color:#fff; border-color:transparent; box-shadow:0 4px 12px rgba(3,105,161,0.25);',
            count: computed(() => this.policies.filter(p => !p.planType || p.planType === 'BOTH' || p.planType === 'INDIVIDUAL').length)
        },
        {
            value: 'FAMILY' as const,
            label: 'Family',
            icon: 'fa-solid fa-people-roof',
            activeStyle: 'background:linear-gradient(135deg,#7c3aed,#6366f1); color:#fff; border-color:transparent; box-shadow:0 4px 12px rgba(99,102,241,0.25);',
            count: computed(() => this.policies.filter(p => !p.planType || p.planType === 'BOTH' || p.planType === 'FAMILY').length)
        }
    ];

    planTypeLabel(pt?: string): string {
        if (!pt || pt === 'BOTH') return 'All Plans';
        if (pt === 'INDIVIDUAL') return 'Individual';
        if (pt === 'FAMILY') return 'Family';
        return pt;
    }

    planIcon(pt?: string): string {
        if (pt === 'FAMILY') return 'fa-solid fa-people-roof';
        if (pt === 'INDIVIDUAL') return 'fa-solid fa-user';
        return 'fa-solid fa-shield-heart';
    }

    planIconStyle(pt?: string): string {
        if (pt === 'FAMILY') return 'background:#f5f3ff; border:1px solid #ddd6fe; color:#7c3aed;';
        return 'background:#eff6ff; border:1px solid #bfdbfe; color:#1d4ed8;';
    }

    planTypePillStyle(pt?: string): string {
        if (pt === 'FAMILY') return 'background:#f5f3ff; color:#7c3aed; border:1px solid #ddd6fe;';
        if (pt === 'INDIVIDUAL') return 'background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe;';
        return 'background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0;';
    }
}
