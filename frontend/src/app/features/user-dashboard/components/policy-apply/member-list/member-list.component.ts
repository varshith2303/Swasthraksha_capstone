import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PolicyMemberRequest } from '../../../../../services/user.service';
import { MemberCardComponent } from '../member-card/member-card.component';

@Component({
    selector: 'app-member-list',
    standalone: true,
    imports: [CommonModule, FormsModule, MemberCardComponent],
    templateUrl: './member-list.component.html'
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
