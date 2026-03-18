import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Policy } from '../../services/user.service';

@Component({
    selector: 'app-policy-catalog',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './policy-catalog.component.html'
})
export class PolicyCatalogComponent {
    @Input() policies: Policy[] = [];
    @Input() policiesLoading: boolean = false;
    @Input() policiesError: string = '';

    @Output() applyForPolicy = new EventEmitter<string>();

    searchQuery = signal('');
    planTypeFilter = signal<'ALL' | 'INDIVIDUAL' | 'FAMILY'>('ALL');

    get filteredPolicies(): Policy[] {
        const q = this.searchQuery().toLowerCase();
        const pt = this.planTypeFilter();
        return this.policies.filter(p => {
            const matchesSearch = !q ||
                p.policyName.toLowerCase().includes(q) ||
                p.policyCode.toLowerCase().includes(q);
            const matchesPlan =
                pt === 'ALL' ||
                p.planType === 'BOTH' ||
                p.planType === pt;
            return matchesSearch && matchesPlan;
        });
    }

    getPlanTypeLabel(planType?: string): string {
        switch (planType) {
            case 'INDIVIDUAL': return 'Individual';
            case 'FAMILY': return 'Family';
            default: return 'Individual & Family';
        }
    }

    getPlanTypeIcon(planType?: string): string {
        switch (planType) {
            case 'INDIVIDUAL': return 'fa-user';
            case 'FAMILY': return 'fa-people-roof';
            default: return 'fa-users';
        }
    }

    getPlanTypeBadgeStyle(planType?: string): string {
        switch (planType) {
            case 'INDIVIDUAL': return 'background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe;';
            case 'FAMILY': return 'background:#f5f3ff; color:#7c3aed; border:1px solid #ddd6fe;';
            default: return 'background:#ecfdf5; color:#059669; border:1px solid #a7f3d0;';
        }
    }
}
