import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Application } from '../../../../services/user.service';

@Component({
    selector: 'app-my-applications',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './my-applications.component.html'
})
export class MyApplicationsComponent {
    @Input() applications: Application[] = [];
    @Input() myApplicationsLoading: boolean = false;
    @Input() myApplicationsError: string = '';

    @Output() statusUpdated = new EventEmitter<void>();

    getApplicationStatusClass(status: string): string {
        switch (status) {
            case 'PENDING': return 'bg-amber-50 text-amber-600 border-amber-200';
            case 'APPROVED': return 'bg-green-50 text-green-600 border-green-200';
            case 'REJECTED': return 'bg-red-50 text-red-600 border-red-200';
            case 'POLICY_ISSUED': return 'bg-blue-50 text-blue-600 border-blue-200';
            default: return 'bg-gray-50 text-gray-600 border-gray-200';
        }
    }

    getApplicationStatusLabel(status: string): string {
        switch (status) {
            case 'PENDING': return 'Review Pending';
            case 'APPROVED': return 'Approved - Awaiting Policy';
            case 'REJECTED': return 'Rejected';
            case 'POLICY_ISSUED': return 'Policy Issued';
            default: return status.replace('_', ' ');
        }
    }
}
