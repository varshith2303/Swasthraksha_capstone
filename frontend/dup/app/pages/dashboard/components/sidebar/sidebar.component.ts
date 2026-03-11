import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-dashboard-sidebar',
    standalone: true,
    imports: [CommonModule],
    template: `
    <aside class="w-64 flex-shrink-0 flex flex-col p-5 gap-1 overflow-y-auto h-full relative"
        style="background: linear-gradient(160deg, #dbeafe 0%, #bfdbfe 45%, #93c5fd 100%); border-right: 1px solid rgba(255,255,255,0.5);">

        <!-- Soft blobs (same as landing page) -->
        <div class="absolute inset-0 overflow-hidden pointer-events-none">
            <div class="absolute -top-16 -left-16 w-48 h-48 rounded-full"
                style="background: radial-gradient(circle, rgba(219,234,254,0.6) 0%, transparent 70%);"></div>
            <div class="absolute bottom-0 right-0 w-40 h-40 rounded-full"
                style="background: radial-gradient(circle, rgba(191,219,254,0.5) 0%, transparent 70%);"></div>
        </div>

        <!-- Brand -->
        <div class="relative z-10 flex items-center gap-3 pb-4 mb-2 border-b" style="border-color: rgba(255,255,255,0.5);">
            <div class="w-9 h-9 rounded-xl flex items-center justify-center shadow-lg flex-shrink-0"
                style="background: rgba(255,255,255,0.7); border: 1px solid rgba(255,255,255,0.9);">
                <i class="fa-solid fa-shield-heart text-lg" style="color: #1d4ed8;"></i>
            </div>
            <div>
                <span class="block text-sm font-bold tracking-tight" style="color: #0f172a;">SwasthaRaksha</span>
                <span class="block text-[0.65rem] font-bold uppercase tracking-widest" style="color: #1d4ed8;">My Portal</span>
            </div>
        </div>

        <!-- User Info -->
        <div class="relative z-10 flex items-center gap-3 p-3 rounded-xl mb-2"
            style="background: rgba(255,255,255,0.55); border: 1px solid rgba(255,255,255,0.75);">
            <div class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 shadow-md"
                style="background: linear-gradient(135deg, #1d4ed8, #2563eb);">
                <i class="fa-solid fa-user text-white text-[0.65rem]"></i>
            </div>
            <div class="min-w-0">
                <p class="text-[0.65rem] font-medium leading-none mb-0.5" style="color: #475569;">Welcome back!</p>
                <p class="text-xs font-bold truncate max-w-[150px]" style="color: #0f172a;" [title]="userEmail">{{ userEmail }}</p>
            </div>
        </div>

        <!-- Nav -->
        <nav class="relative z-10 flex flex-col gap-0.5 flex-1">
            <button id="nav-apply"
                class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 text-left w-full"
                [style]="activeTab === 'apply'
                    ? 'background: rgba(255,255,255,0.75); color: #1d4ed8; font-weight: 700; box-shadow: 0 2px 8px rgba(37,99,235,0.15);'
                    : 'color: #1e3a5f;'"
                (click)="tabChange.emit('apply')">
                <i class="fa-solid fa-file-signature text-xs flex-shrink-0"
                    [style.color]="activeTab === 'apply' ? '#1d4ed8' : '#2563eb'"></i>
                Apply for Policy
                <span *ngIf="activeTab === 'apply'" class="ml-auto w-1.5 h-1.5 rounded-full"
                    style="background: #1d4ed8;"></span>
            </button>

            <button id="nav-my-applications"
                class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 text-left w-full"
                [style]="activeTab === 'my-applications'
                    ? 'background: rgba(255,255,255,0.75); color: #1d4ed8; font-weight: 700; box-shadow: 0 2px 8px rgba(37,99,235,0.15);'
                    : 'color: #1e3a5f;'"
                (click)="tabChange.emit('my-applications')">
                <i class="fa-solid fa-folder-open text-xs flex-shrink-0"
                    [style.color]="activeTab === 'my-applications' ? '#1d4ed8' : '#2563eb'"></i>
                My Applications
                <span *ngIf="applicationCount > 0"
                    class="ml-auto text-white text-[0.65rem] font-bold px-1.5 py-0.5 rounded-full shadow-sm"
                    style="background: #1d4ed8;">{{ applicationCount }}</span>
            </button>

            <button id="nav-policies"
                class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 text-left w-full"
                [style]="activeTab === 'policies'
                    ? 'background: rgba(255,255,255,0.75); color: #1d4ed8; font-weight: 700; box-shadow: 0 2px 8px rgba(37,99,235,0.15);'
                    : 'color: #1e3a5f;'"
                (click)="tabChange.emit('policies')">
                <i class="fa-solid fa-magnifying-glass text-xs flex-shrink-0"
                    [style.color]="activeTab === 'policies' ? '#1d4ed8' : '#2563eb'"></i>
                Browse Policies
            </button>

            <button id="nav-my-policies"
                class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 text-left w-full"
                [style]="activeTab === 'my-policies'
                    ? 'background: rgba(255,255,255,0.75); color: #1d4ed8; font-weight: 700; box-shadow: 0 2px 8px rgba(37,99,235,0.15);'
                    : 'color: #1e3a5f;'"
                (click)="tabChange.emit('my-policies')">
                <i class="fa-solid fa-shield-halved text-xs flex-shrink-0"
                    [style.color]="activeTab === 'my-policies' ? '#1d4ed8' : '#2563eb'"></i>
                My Policies
                <span *ngIf="policyCount > 0"
                    class="ml-auto text-white text-[0.65rem] font-bold px-1.5 py-0.5 rounded-full shadow-sm"
                    style="background: #10b981;">{{ policyCount }}</span>
            </button>

            <button id="nav-my-claims"
                class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 text-left w-full"
                [style]="activeTab === 'my-claims'
                    ? 'background: rgba(255,255,255,0.75); color: #1d4ed8; font-weight: 700; box-shadow: 0 2px 8px rgba(37,99,235,0.15);'
                    : 'color: #1e3a5f;'"
                (click)="tabChange.emit('my-claims')">
                <i class="fa-solid fa-file-medical text-xs flex-shrink-0"
                    [style.color]="activeTab === 'my-claims' ? '#1d4ed8' : '#2563eb'"></i>
                My Claims
            </button>
        </nav>

        <!-- Logout -->
        <button id="logout-btn"
            class="relative z-10 flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-semibold transition-all duration-200 w-full mt-1"
            style="color: #dc2626; background: rgba(255,255,255,0.35);"
            (click)="logoutTrigger.emit()">
            <i class="fa-solid fa-right-from-bracket text-[0.7rem]"></i>
            Logout
        </button>
    </aside>
  `
})
export class DashboardSidebarComponent {
    @Input() userEmail: string = '';
    @Input() activeTab: string = 'apply';
    @Input() applicationCount: number = 0;
    @Input() policyCount: number = 0;

    @Output() tabChange = new EventEmitter<'apply' | 'my-applications' | 'policies' | 'my-policies' | 'my-claims'>();
    @Output() logoutTrigger = new EventEmitter<void>();
}
