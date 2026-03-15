import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Underwriter } from '../../../../services/admin.service';

@Component({
    selector: 'app-admin-claims-officers',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './admin-claims-officers.component.html',
})
export class AdminClaimsOfficersComponent implements OnInit {
    claimsOfficers = signal<Underwriter[]>([]);
    form: Underwriter = this.emptyForm();
    loading = false;
    error = '';
    success = '';

    constructor(private adminService: AdminService) {}

    ngOnInit(): void {
        this.load();
    }

    emptyForm(): Underwriter {
        return { username: '', email: '', password: '' };
    }

    load(): void {
        this.loading = true;
        this.adminService.getClaimsOfficers().subscribe({
            next: (data) => { this.claimsOfficers.set(data); this.loading = false; },
            error: () => { this.loading = false; }
        });
    }

    add(): void {
        this.loading = true;
        this.error = ''; this.success = '';
        this.adminService.addClaimsOfficer(this.form).subscribe({
            next: () => {
                this.success = `Claims Officer "${this.form.username}" created successfully!`;
                this.form = this.emptyForm();
                this.loading = false;
                this.load();
            },
            error: () => {
                this.error = 'Failed to create claims officer. Email may already exist.';
                this.loading = false;
            }
        });
    }

    delete(id: number): void {
        if (!confirm('Are you sure you want to delete this claims officer?')) return;
        this.error = ''; this.success = '';
        this.adminService.deleteUser(id).subscribe({
            next: () => { this.success = 'Claims officer deleted successfully.'; this.load(); },
            error: (err) => {
                this.error = err.error?.message || err.error?.error || 'Failed to delete claims officer. Please try again.';
            }
        });
    }
}
