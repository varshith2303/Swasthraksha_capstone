import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PolicyMemberRequest } from '../../../../../services/user.service';
import { MemberCardComponent } from '../member-card/member-card.component';

/**
 * MemberListComponent — SRP: manages a dynamic list of PolicyMember cards.
 * Handles add/remove member logic and enforces min/max constraints.
 */
@Component({
    selector: 'app-member-list',
    standalone: true,
    imports: [CommonModule, FormsModule, MemberCardComponent],
    template: `
    <div class="flex flex-col gap-4">

        <!-- Member Cards -->
        @for (member of members; track $index) {
        <app-member-card
            [member]="member"
            [index]="$index"
            [canRemove]="isFamily && $index > 0 && members.length > 2"
            [isFamily]="isFamily"
            [availableRelationships]="getAvailableRelationships($index)"
            (remove)="removeMember($index)">
        </app-member-card>
        }

        <!-- Add Member button (Family only, max 6) -->
        @if (isFamily && members.length < 6) {
        <button type="button" (click)="addMember()"
            class="flex items-center justify-center gap-2.5 w-full py-3 rounded-2xl text-sm font-bold transition-all duration-200 hover:-translate-y-0.5"
            style="border: 2px dashed #bfdbfe; color: #2563eb; background: #eff6ff;">
            <span class="w-7 h-7 rounded-xl flex items-center justify-center text-white text-sm shadow-sm flex-shrink-0"
                style="background: linear-gradient(135deg,#1d4ed8,#2563eb);">
                <i class="fa-solid fa-plus text-xs"></i>
            </span>
            Add Family Member
            <span class="text-xs font-normal text-blue-400">({{ members.length }}/6)</span>
        </button>
        }

        <!-- Discount badge for family plans -->
        @if (isFamily && members.length >= 2) {
        <div class="flex items-center gap-2.5 px-4 py-3 rounded-xl text-xs font-medium"
            style="background: #f0fdf4; border: 1px solid #bbf7d0; color: #16a34a;">
            <i class="fa-solid fa-tag"></i>
            @if (members.length >= 4) { Up to <strong class="mx-1">15% multi-member discount</strong> applied! }
            @else if (members.length === 3) { <strong class="mr-1">10%</strong> multi-member discount applied! }
            @else { <strong class="mr-1">5%</strong> multi-member discount applied! }
        </div>
        }
    </div>
    `
})
export class MemberListComponent {
    @Input() members: PolicyMemberRequest[] = [];
    @Input() isFamily: boolean = false;

    addMember(): void {
        if (this.members.length < 6) {
            // Find a relationship that isn't full yet
            const counts = this.getRelationshipCounts();
            let defaultRel = 'CHILD';
            if (counts['CHILD'] >= 2) defaultRel = 'PARENT';
            if (counts['PARENT'] >= 2 && counts['SPOUSE'] < 1) defaultRel = 'SPOUSE';

            this.members.push({
                name: '',
                age: 0,
                bmi: 0,
                smoker: false,
                existingDiseases: '',
                relationship: defaultRel
            });
        }
    }

    private getRelationshipCounts(): Record<string, number> {
        const counts: Record<string, number> = { SELF: 0, SPOUSE: 0, PARENT: 0, CHILD: 0 };
        this.members.forEach(m => {
            if (m.relationship) counts[m.relationship]++;
        });
        return counts;
    }

    getAvailableRelationships(index: number): any[] {
        const counts = this.getRelationshipCounts();
        const currentMemberRel = this.members[index].relationship;

        return [
            { value: 'SELF', label: 'Self (Primary)', disabled: index !== 0 },
            { value: 'SPOUSE', label: 'Spouse', disabled: index === 0 || (counts['SPOUSE'] >= 1 && currentMemberRel !== 'SPOUSE') },
            { value: 'PARENT', label: 'Parent', disabled: index === 0 || (counts['PARENT'] >= 2 && currentMemberRel !== 'PARENT') },
            { value: 'CHILD', label: 'Child', disabled: index === 0 || (counts['CHILD'] >= 2 && currentMemberRel !== 'CHILD') }
        ];
    }

    removeMember(index: number): void {
        if (this.members.length > 2) {
            this.members.splice(index, 1);
        }
    }
}
