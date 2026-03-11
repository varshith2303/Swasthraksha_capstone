import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Application } from '../../../../services/user.service';

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
            case 'PENDING': return 'bg-amber-100 text-amber-700';
            case 'APPROVED': return 'bg-green-100 text-green-700';
            case 'REJECTED': return 'bg-red-100 text-red-700';
            case 'POLICY_ISSUED': return 'bg-blue-100 text-blue-700';
            default: return 'bg-gray-100 text-gray-700';
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
