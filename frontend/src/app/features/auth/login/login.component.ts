import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [RouterLink, CommonModule, ReactiveFormsModule],
    templateUrl: './login.component.html',
})
export class LoginComponent {
    loginForm: FormGroup;
    isLoading = false;
    errorMessage = '';
    successMessage = '';
    showPassword = false;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(6)]],
            rememberMe: [false]
        });
    }

    get email() { return this.loginForm.get('email')!; }
    get password() { return this.loginForm.get('password')!; }

    togglePassword(): void {
        this.showPassword = !this.showPassword;
    }

    onSubmit(): void {
        if (this.loginForm.invalid) {
            this.loginForm.markAllAsTouched();
            return;
        }
        this.isLoading = true;
        this.errorMessage = '';
        this.successMessage = '';

        this.authService.login(this.email.value, this.password.value).subscribe({
            next: () => {
                this.isLoading = false;
                this.successMessage = 'Login successful! Redirecting...';
                const role = this.authService.getRole()?.toUpperCase();
                let destination = '/dashboard'; // Default
                if (role === 'ADMIN') {
                    destination = '/admin';
                } else if (role === 'UNDERWRITER') {
                    destination = '/underwriter';
                } else if (role === 'CLAIMS_OFFICER') {
                    destination = '/claims-officer';
                }
                this.router.navigate([destination]);

            },
            error: (err) => {
                this.isLoading = false;
                this.errorMessage = err.status === 401
                    ? 'Invalid email or password. Please try again.'
                    : 'Something went wrong. Please try again later.';
            }
        });
    }
}
