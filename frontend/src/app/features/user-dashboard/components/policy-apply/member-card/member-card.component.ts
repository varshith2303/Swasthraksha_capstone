import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PolicyMemberRequest } from '../../../services/user.service';

@Component({
    selector: 'app-member-card',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './member-card.component.html'
})
export class MemberCardComponent {
    @Input() member!: PolicyMemberRequest;
    @Input() index: number = 0;
    @Input() canRemove: boolean = false;
    @Input() isFamily: boolean = false;
    @Input() availableRelationships: any[] = [];
    @Output() remove = new EventEmitter<void>();

    get relBadgeStyle(): string {
        const rel = this.member.relationship;
        if (rel === 'SELF') return 'background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe;';
        if (rel === 'SPOUSE') return 'background:#f5f3ff; color:#7c3aed; border:1px solid #ddd6fe;';
        if (rel === 'PARENT') return 'background:#fff7ed; color:#c2410c; border:1px solid #fed7aa;';
        if (rel === 'CHILD') return 'background:#f0fdf4; color:#15803d; border:1px solid #bbf7d0;';
        return 'background:#f9fafb; color:#6b7280; border:1px solid #e5e7eb;';
    }
}
