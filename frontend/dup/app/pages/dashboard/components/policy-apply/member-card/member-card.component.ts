import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PolicyMemberRequest } from '../../../../../services/user.service';

/**
 * MemberCardComponent — SRP: renders and edits a single policy member's health details.
 * Used by MemberListComponent for both individual and family plan member entries.
 */
@Component({
    selector: 'app-member-card',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="rounded-2xl overflow-hidden transition-all duration-300"
        style="border: 1.5px solid #e0e7ff; background: #fafbff;">

        <!-- Card Header -->
        <div class="flex items-center justify-between px-5 py-3.5"
            style="background: linear-gradient(135deg, #eff6ff 0%, #eef2ff 100%); border-bottom: 1px solid #e0e7ff;">
            <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-xl flex items-center justify-center text-white text-xs font-bold shadow-sm flex-shrink-0"
                    style="background: linear-gradient(135deg, #1d4ed8, #6366f1);">
                    {{ index + 1 }}
                </div>
                <div>
                    <span class="text-sm font-bold text-gray-800">
                        {{ member.name || 'Member ' + (index + 1) }}
                    </span>
                    @if (isFamily && member.relationship) {
                    <span class="ml-2 text-[0.65rem] font-bold px-1.5 py-0.5 rounded-md whitespace-nowrap shadow-sm"
                        [style]="relBadgeStyle">
                        {{ member.relationship === 'SELF' ? 'Primary' : member.relationship }}
                    </span>
                    }
                </div>
            </div>
            @if (canRemove) {
            <button type="button" (click)="remove.emit()"
                class="w-8 h-8 rounded-xl flex items-center justify-center text-red-400 hover:text-red-600 hover:bg-red-50 transition-all"
                title="Remove member">
                <i class="fa-solid fa-xmark text-sm"></i>
            </button>
            }
        </div>

        <!-- Fields Grid -->
        <div class="px-5 py-4 grid grid-cols-1 md:grid-cols-2 gap-4">

            <!-- Relationship (Only for Family) -->
            @if (isFamily) {
            <div class="flex flex-col gap-1.5 md:col-span-2">
                <label class="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Relationship <span class="text-red-400">*</span>
                </label>
                <div class="relative group">
                    <select [(ngModel)]="member.relationship" [name]="'relationship_' + index" required
                        [disabled]="index === 0"
                        class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all cursor-pointer appearance-none group-hover:border-blue-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/5 shadow-sm disabled:cursor-not-allowed disabled:bg-gray-100"
                        style="background: #f9fafb; border: 1.5px solid #e5e7eb;">
                        @for (opt of availableRelationships; track opt.value) {
                        <option [value]="opt.value" [disabled]="opt.disabled">{{ opt.label }}</option>
                        }
                    </select>
                    <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                        <i class="fa-solid fa-chevron-down text-[0.6rem]"></i>
                    </div>
                </div>
            </div>
            }

            <!-- Full Name -->
            <div class="flex flex-col gap-1.5 md:col-span-2">
                <label class="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Full Name <span class="text-red-400">*</span>
                </label>
                <input type="text" [(ngModel)]="member.name" [name]="'name_' + index"
                    required placeholder="e.g. Ravi Kumar"
                    class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm group-hover:border-blue-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/5"
                    style="background: #f9fafb; border: 1.5px solid #e5e7eb;" />
            </div>

            <!-- Age -->
            <div class="flex flex-col gap-1.5">
                <label class="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Age (years) <span class="text-red-400">*</span>
                </label>
                <input type="number" [(ngModel)]="member.age" [name]="'age_' + index"
                    required min="0" max="100" placeholder="e.g. 35"
                    class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm group-hover:border-blue-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/5"
                    style="background: #f9fafb; border: 1.5px solid #e5e7eb;" />
            </div>

            <!-- BMI -->
            <div class="flex flex-col gap-1.5">
                <label class="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    BMI <span class="text-red-400">*</span>
                </label>
                <input type="number" [(ngModel)]="member.bmi" [name]="'bmi_' + index"
                    required min="10" max="60" step="0.1" placeholder="e.g. 22.5"
                    class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm group-hover:border-blue-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/5"
                    style="background: #f9fafb; border: 1.5px solid #e5e7eb;" />
            </div>

            <!-- Existing Diseases -->
            <div class="flex flex-col gap-1.5 md:col-span-2">
                <label class="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Existing Medical Conditions
                </label>
                <input type="text" [(ngModel)]="member.existingDiseases" [name]="'diseases_' + index"
                    placeholder="e.g. Diabetes, Hypertension (leave blank if none)"
                    class="w-full px-3.5 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all shadow-sm group-hover:border-blue-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/5"
                    style="background: #f9fafb; border: 1.5px solid #e5e7eb;" />
            </div>

            <!-- Smoker Toggle -->
            <div class="md:col-span-2 flex flex-col gap-2">
                <span class="text-xs font-semibold text-gray-500 uppercase tracking-wide">Tobacco / Cigarette Use</span>
                <label class="flex items-center gap-3 cursor-pointer w-fit">
                    <input type="checkbox" class="hidden" [(ngModel)]="member.smoker" [name]="'smoker_' + index" />
                    <span class="w-12 h-6 rounded-full border relative transition-all duration-300 flex items-center p-0.5 shadow-sm flex-shrink-0"
                        [style]="member.smoker ? 'background:#1d4ed8; border-color:#1d4ed8;' : 'background:#e5e7eb; border-color:#d1d5db;'">
                        <span class="w-5 h-5 rounded-full bg-white shadow-md transition-all duration-300"
                            [style]="member.smoker ? 'transform:translateX(24px);' : 'transform:translateX(0);'"></span>
                    </span>
                    <span class="text-sm font-semibold transition-colors"
                        [style]="member.smoker ? 'color:#1d4ed8;' : 'color:#94a3b8;'">
                        {{ member.smoker ? 'Yes, tobacco user' : 'No tobacco use' }}
                    </span>
                </label>
            </div>

        </div>
    </div>

    `
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
