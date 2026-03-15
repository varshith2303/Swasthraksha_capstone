import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Underwriter } from '../../../../services/admin.service';

@Component({
    selector: 'app-admin-underwriters',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './admin-underwriters.component.html',
})
export class AdminUnderwritersComponent implements OnInit {
    underwriters = signal<Underwriter[]>([]);
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
        this.adminService.getUnderwriters().subscribe({
            next: (data) => { this.underwriters.set(data); this.loading = false; },
            error: () => { this.loading = false; }
        });
    }

    add(): void {
        this.loading = true;
        this.error = ''; this.success = '';
        this.adminService.addUnderwriter(this.form).subscribe({
            next: () => {
                this.success = `Underwriter "${this.form.username}" created successfully!`;
                this.form = this.emptyForm();
                this.loading = false;
                this.load();
            },
            error: () => {
                this.error = 'Failed to create underwriter. Email may already exist.';
                this.loading = false;
            }
        });
    }

    delete(id: number): void {
        if (!confirm('Are you sure you want to delete this underwriter?')) return;
        this.error = ''; this.success = '';
        this.adminService.deleteUser(id).subscribe({
            next: () => { this.success = 'Underwriter deleted successfully.'; this.load(); },
            error: (err) => {
                this.error = err.error?.message || err.error?.error || 'Failed to delete underwriter. Please try again.';
            }
        });
    }
}
