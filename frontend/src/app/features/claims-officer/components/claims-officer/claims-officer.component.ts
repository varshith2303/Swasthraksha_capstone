import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ClaimsOfficerService, Claim } from '../../services/claims-officer.service';
import { AuthService } from '../../../../services/auth.service';
import { DocumentItem, DocumentService } from '../../../../services/document.service';

@Component({
    selector: 'app-claims-officer',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './claims-officer.component.html',
    styleUrl: './claims-officer.component.css'
})
export class ClaimsOfficerComponent implements OnInit {
    activeTab: 'pending' | 'all' = 'pending';
    userEmail = '';

    assignedClaims = signal<Claim[]>([]);
    isLoading = false;
    error = '';

    visibleDocuments: Record<number, boolean> = {};
    claimDocuments: Record<number, DocumentItem[]> = {};
    documentsLoading: Record<number, boolean> = {};
    documentsError: Record<number, string> = {};

    pendingClaims = computed(() =>
        this.assignedClaims().filter(c => c.status === 'PENDING')
    );

    constructor(
        private claimsOfficerService: ClaimsOfficerService,
        private authService: AuthService,
        private documentService: DocumentService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.userEmail = this.authService.getCurrentUser() || 'Officer';
        this.loadAssignedClaims();
    }

    loadAssignedClaims(): void {
        this.isLoading = true;
        this.error = '';
        this.claimsOfficerService.getAssignedClaims().subscribe({
            next: (data) => {
                this.assignedClaims.set(data);
                this.isLoading = false;
            },
            error: (err) => {
                this.error = 'Failed to load assigned claims.';
                this.isLoading = false;
            }
        });
    }

    setTab(tab: 'pending' | 'all'): void {
        this.activeTab = tab;
    }

    logout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    getStatusColor(status: string): string {
        const map: Record<string, string> = {
            'PENDING': 'background:#fefce8; color:#ca8a04; border:1px solid #fde68a;',
            'APPROVED': 'background:#ecfdf5; color:#059669; border:1px solid #a7f3d0;',
            'REJECTED': 'background:#fef2f2; color:#dc2626; border:1px solid #fecaca;',
        };
        return map[status] || 'background:#f9fafb; color:#6b7280; border:1px solid #e5e7eb;';
    }

    verifyClaim(claim: Claim, approve: boolean): void {
        if (!confirm(`Are you sure you want to ${approve ? 'approve' : 'reject'} claim ${claim.claimNumber}?`)) return;

        this.isLoading = true;
        this.claimsOfficerService.verifyClaim(claim.claimNumber, approve).subscribe({
            next: () => {
                this.loadAssignedClaims();
            },
            error: (err) => {
                this.error = 'Failed to verify claim.';
                this.isLoading = false;
            }
        });
    }

    toggleDocuments(claimId: number): void {
        const isVisible = !!this.visibleDocuments[claimId];
        this.visibleDocuments[claimId] = !isVisible;

        if (!isVisible && !this.claimDocuments[claimId]) {
            this.loadDocuments(claimId);
        }
    }

    private loadDocuments(claimId: number): void {
        this.documentsLoading[claimId] = true;
        this.documentsError[claimId] = '';

        this.documentService.getClaimDocuments(claimId).subscribe({
            next: (docs) => {
                this.claimDocuments[claimId] = docs;
                this.documentsLoading[claimId] = false;
            },
            error: (err) => {
                this.documentsError[claimId] = err.error?.message || 'Failed to load documents';
                this.documentsLoading[claimId] = false;
            }
        });
    }

    downloadDocument(doc: DocumentItem): void {
        this.documentService.downloadDocument(doc.id).subscribe({
            next: (blob) => {
                const blobUrl = URL.createObjectURL(blob);
                const anchor = document.createElement('a');
                anchor.href = blobUrl;
                anchor.download = doc.fileName || `document-${doc.id}`;
                anchor.click();
                URL.revokeObjectURL(blobUrl);
            },
            error: () => {
                alert('Failed to download document. Please try again.');
            }
        });
    }
}
