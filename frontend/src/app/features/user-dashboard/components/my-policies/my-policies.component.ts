import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PolicyAssignment, UserService } from '../../services/user.service';

@Component({
    selector: 'app-my-policies',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './my-policies.component.html'
})
export class MyPoliciesComponent {
    @Input() policies: PolicyAssignment[] = [];
    @Input() myPoliciesLoading: boolean = false;
    @Input() myPoliciesError: string = '';
    @Input() actionLoading: Record<number, boolean> = {};

    @Output() paymentCompleted = new EventEmitter<void>();

    constructor(private userService: UserService) { }

    installmentsLeft(pa: PolicyAssignment): number {
        return (pa.totalInstallments ?? 0) - (pa.paidInstallments ?? 0);
    }

    amountToPay(pa: PolicyAssignment): number {
        return Math.max(0, pa.totalPremiumAmount - pa.premiumPaid);
    }

    getPolicyStatusClass(status: string): string {
        switch (status) {
            case 'PENDING_PAYMENT': return 'bg-amber-50 text-amber-600 border-amber-200';
            case 'ACTIVE': return 'bg-green-50 text-green-600 border-green-200';
            case 'EXPIRED': return 'bg-red-50 text-red-600 border-red-200';
            default: return 'bg-gray-50 text-gray-600 border-gray-200';
        }
    }

    getPolicyStatusLabel(status: string): string {
        switch (status) {
            case 'PENDING_PAYMENT': return 'Payment Pending';
            case 'ACTIVE': return 'Active';
            case 'EXPIRED': return 'Expired';
            default: return status.replace('_', ' ');
        }
    }

    onMakePayment(id: number) {
        this.actionLoading[id] = true;
        this.userService.makePolicyPayment(id).subscribe({
            next: () => {
                this.actionLoading[id] = false;
                this.paymentCompleted.emit();
            },
            error: () => this.actionLoading[id] = false
        });
    }
}
