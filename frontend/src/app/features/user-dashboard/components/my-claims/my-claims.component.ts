import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { PolicyAssignment, ClaimRequest, ClaimResponse, DocumentType, UserService } from '../../../../services/user.service';

@Component({
    selector: 'app-my-claims',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './my-claims.component.html'
})
export class MyClaimsComponent implements OnInit {
    @Input() activePolicies: PolicyAssignment[] = [];

    claims = signal<ClaimResponse[]>([]);
    loadingClaims = signal(false);
    submitting = signal(false);
    submitSuccess = signal('');
    submitError = signal('');
    documentUploadSuccess = signal('');
    documentUploadError = signal('');

    readonly allowedFileTypes = ['application/pdf', 'image/jpeg', 'image/png'];
    readonly maxFileSize = 10 * 1024 * 1024;

    selectedDocuments: Partial<Record<DocumentType, File>> = {};

    form: ClaimRequest = {
        policyNumber: '',
        claimAmount: 0,
        hospitalName: '',
        claimReason: '',
        admissionDate: '',
        dischargeDate: '',
        memberId: undefined
    };

    constructor(private userService: UserService) { }

    ngOnInit(): void {
        this.loadClaims();
    }

    loadClaims(): void {
        this.loadingClaims.set(true);
        this.userService.getMyClaims().subscribe({
            next: (data) => { this.claims.set(data); this.loadingClaims.set(false); },
            error: () => { this.loadingClaims.set(false); }
        });
    }

    onSubmitClaim(): void {
        this.submitSuccess.set('');
        this.submitError.set('');
        this.documentUploadSuccess.set('');
        this.documentUploadError.set('');
        this.submitting.set(true);
        this.userService.submitClaim(this.form).subscribe({
            next: (res) => {
                const uploads = this.buildClaimUploadRequests(res.claimNumber);
                if (uploads.length === 0) {
                    this.submitting.set(false);
                    this.submitSuccess.set(res.claimNumber);
                    this.resetForm();
                    this.loadClaims();
                    return;
                }

                forkJoin(uploads).subscribe({
                    next: (results) => {
                        const successCount = results.filter(r => r.ok).length;
                        const failed = results.filter(r => !r.ok);

                        if (successCount > 0) {
                            this.documentUploadSuccess.set(`${successCount} document(s) uploaded successfully.`);
                        }
                        if (failed.length > 0) {
                            this.documentUploadError.set(failed[0].message || 'Some documents failed to upload.');
                        }

                        this.submitting.set(false);
                        this.submitSuccess.set(res.claimNumber);
                        this.resetForm();
                        this.loadClaims();
                    },
                    error: () => {
                        this.submitting.set(false);
                        this.submitSuccess.set(res.claimNumber);
                        this.documentUploadError.set('Claim submitted, but document upload failed.');
                        this.resetForm();
                        this.loadClaims();
                    }
                });
            },
            error: (err) => {
                this.submitting.set(false);
                this.submitError.set(err.error?.message || 'Failed to submit claim. Please try again.');
            }
        });
    }

    onFileSelected(event: Event, documentType: DocumentType): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];

        if (!file) {
            this.selectedDocuments[documentType] = undefined;
            return;
        }

        const validationMessage = this.validateFile(file);
        if (validationMessage) {
            this.documentUploadError.set(validationMessage);
            this.selectedDocuments[documentType] = undefined;
            input.value = '';
            return;
        }

        this.documentUploadError.set('');
        this.selectedDocuments[documentType] = file;
    }

    getSelectedFileName(documentType: DocumentType): string {
        return this.selectedDocuments[documentType]?.name || 'No file selected';
    }

    get selectedPolicy(): PolicyAssignment | null {
        return this.activePolicies.find(p => p.policyNumber === this.form.policyNumber) ?? null;
    }

    onPolicyChange(): void {
        this.form.memberId = undefined;
    }

    latestClaimDate(policyNumber: string): string {
        console.log(this.claims());
        const dates = this.claims()
            .filter(c => c.policyNumber === policyNumber && c.status === 'APPROVED' && c.reviewedDate)
            .map(c => c.reviewedDate!);
        if (dates.length === 0) return 'No claims yet';
        const latest = dates.sort().at(-1)!;
        return new Date(latest).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
    }

    resetForm(): void {
        this.form = {
            policyNumber: '',
            claimAmount: 0,
            hospitalName: '',
            claimReason: '',
            admissionDate: '',
            dischargeDate: '',
            memberId: undefined
        };
        this.selectedDocuments = {};
    }

    private validateFile(file: File): string | null {
        if (!this.allowedFileTypes.includes(file.type)) {
            return 'Only PDF, JPG, and PNG files are allowed.';
        }
        if (file.size > this.maxFileSize) {
            return 'File size must be 10MB or less.';
        }
        return null;
    }

    private buildClaimUploadRequests(claimNumber: string) {
        return (Object.entries(this.selectedDocuments) as [DocumentType, File | undefined][])
            .filter(([, file]) => !!file)
            .map(([documentType, file]) => this.userService
                .uploadClaimDocument(claimNumber, documentType, file!)
                .pipe(
                    map(() => ({ ok: true, message: '' })),
                    catchError((err) => of({
                        ok: false,
                        message: err.error?.message || `Failed to upload ${documentType}`
                    }))
                )
            );
    }

    getClaimStatusStyle(status: string): string {
        switch (status) {
            case 'PENDING': return 'background:#fefce8; color:#ca8a04; border:1px solid #fde68a;';
            case 'APPROVED': return 'background:#ecfdf5; color:#059669; border:1px solid #a7f3d0;';
            case 'REJECTED': return 'background:#fef2f2; color:#dc2626; border:1px solid #fecaca;';
            default: return 'background:#f9fafb; color:#6b7280; border:1px solid #e5e7eb;';
        }
    }
}
