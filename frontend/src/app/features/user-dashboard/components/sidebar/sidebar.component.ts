import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Application } from '../../services/user.service';

@Component({
    selector: 'app-sidebar',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './sidebar.component.html'
})
export class SidebarComponent {
    @Input() userEmail: string = '';
    @Input() selectedTab: string = 'APPLY';
    @Input() pendingActionsCount: number = 0;
    @Input() latestApplications: Application[] = [];
    @Output() tabChange = new EventEmitter<string>();
    @Output() logout = new EventEmitter<void>();

    selectTab(tab: string) {
        this.tabChange.emit(tab);
    }

    getApplicationStatusClass(status: string): string {
        switch (status) {
            case 'SUBMITTED':
            case 'UNDER_REVIEW': return 'bg-amber-400';
            case 'APPROVED':
            case 'CUSTOMER_ACCEPTED': return 'bg-green-500';
            case 'REJECTED':
            case 'CUSTOMER_DECLINED': return 'bg-red-500';
            case 'WAITING_CUSTOMER_ACCEPTANCE': return 'bg-indigo-400';
            case 'POLICY_ISSUED': return 'bg-blue-500';
            case 'QUOTE_GENERATED': return 'bg-purple-400';
            case 'DRAFT': return 'bg-gray-400';
            default: return 'bg-gray-400';
        }
    }

    getApplicationStatusIcon(status: string): string {
        switch (status) {
            case 'PENDING': return 'fa-clock';
            case 'APPROVED': return 'fa-check-circle';
            case 'REJECTED': return 'fa-times-circle';
            case 'POLICY_ISSUED': return 'fa-shield-check';
            default: return 'fa-circle-info';
        }
    }
}
