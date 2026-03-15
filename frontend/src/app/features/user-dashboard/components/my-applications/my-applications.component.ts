import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Application, UserService } from '../../../../services/user.service';

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

    actionLoading: Record<number, boolean> = {};

    constructor(private userService: UserService) {}

    getApplicationStatusClass(status: string): string {
        switch (status) {
            case 'DRAFT': return 'bg-slate-50 text-slate-500 border-slate-200';
            case 'SUBMITTED': return 'bg-blue-50 text-blue-600 border-blue-200';
            case 'UNDER_REVIEW': return 'bg-amber-50 text-amber-600 border-amber-200';
            case 'QUOTE_GENERATED': return 'bg-purple-50 text-purple-600 border-purple-200';
            case 'WAITING_CUSTOMER_ACCEPTANCE': return 'bg-indigo-50 text-indigo-600 border-indigo-200';
            case 'APPROVED':
            case 'CUSTOMER_ACCEPTED':
            case 'POLICY_ISSUED': return 'bg-emerald-50 text-emerald-600 border-emerald-200';
            case 'REJECTED':
            case 'CUSTOMER_DECLINED': return 'bg-red-50 text-red-600 border-red-200';
            default: return 'bg-gray-50 text-gray-600 border-gray-200';
        }
    }

    getApplicationStatusLabel(status: string): string {
        switch (status) {
            case 'DRAFT': return 'Draft';
            case 'SUBMITTED': return 'Submitted';
            case 'UNDER_REVIEW': return 'Under Review';
            case 'QUOTE_GENERATED': return 'Quote Ready';
            case 'WAITING_CUSTOMER_ACCEPTANCE': return 'Waiting Acceptance';
            case 'APPROVED': return 'Approved';
            case 'CUSTOMER_ACCEPTED': return 'Accepted';
            case 'CUSTOMER_DECLINED': return 'Declined';
            case 'POLICY_ISSUED': return 'Policy Issued';
            case 'REJECTED': return 'Rejected';
            default: return status.replace(/_/g, ' ');
        }
    }

    onAccept(app: Application): void {
        if (!app.applicationNumber) return;
        this.actionLoading[app.id] = true;
        this.userService.acceptApplication(app.applicationNumber).subscribe({
            next: () => {
                this.actionLoading[app.id] = false;
                this.statusUpdated.emit();
            },
            error: () => { this.actionLoading[app.id] = false; }
        });
    }

    onDecline(app: Application): void {
        this.actionLoading[app.id] = true;
        this.userService.declineApplication(app.id).subscribe({
            next: () => {
                this.actionLoading[app.id] = false;
                this.statusUpdated.emit();
            },
            error: () => { this.actionLoading[app.id] = false; }
        });
    }

    onMakeFirstPayment(app: Application): void {
        if (!app.applicationNumber) return;
        this.actionLoading[app.id] = true;
        this.userService.makeFirstPayment(app.applicationNumber).subscribe({
            next: () => {
                this.actionLoading[app.id] = false;
                this.statusUpdated.emit();
            },
            error: () => { this.actionLoading[app.id] = false; }
        });
    }
}
