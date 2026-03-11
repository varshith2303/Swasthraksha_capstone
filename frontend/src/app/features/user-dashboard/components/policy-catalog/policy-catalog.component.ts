import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Policy } from '../../../../services/user.service';

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

    get filteredPolicies(): Policy[] {
        const q = this.searchQuery().toLowerCase();
        if (!q) return this.policies;
        return this.policies.filter(p =>
            p.policyName.toLowerCase().includes(q) ||
            p.policyCode.toLowerCase().includes(q)
        );
    }
}
