import { Component, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import {
    ReactiveFormsModule,
    FormBuilder,
    FormGroup,
    Validators,
    AbstractControl,
    ValidationErrors
} from '@angular/forms';
import { AuthService } from '../../../../services/auth.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirm = control.get('confirmPassword');
    if (password && confirm && password.value !== confirm.value) {
        return { passwordMismatch: true };
    }
    return null;
}

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [RouterLink, CommonModule, ReactiveFormsModule],
    templateUrl: './register.component.html',
})
export class RegisterComponent {
    registerForm: FormGroup;
    isLoading = false;
    errorMessage = signal('');
    successMessage = signal('');
    showPassword = false;
    showConfirm = false;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.registerForm = this.fb.group({
            username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', Validators.required],
            agreeToTerms: [false, Validators.requiredTrue]
        }, { validators: passwordMatchValidator });
    }

    get username() { return this.registerForm.get('username')!; }
    get email() { return this.registerForm.get('email')!; }
    get password() { return this.registerForm.get('password')!; }
    get confirmPassword() { return this.registerForm.get('confirmPassword')!; }
    get agreeToTerms() { return this.registerForm.get('agreeToTerms')!; }

    get passwordStrength(): { level: number; label: string; color: string } {
        const val = this.password.value || '';
        let score = 0;
        if (val.length >= 8) score++;
        if (/[A-Z]/.test(val)) score++;
        if (/[0-9]/.test(val)) score++;
        if (/[^A-Za-z0-9]/.test(val)) score++;
        const levels = [
            { level: 0, label: '', color: '' },
            { level: 1, label: 'Weak', color: 'bg-red-500' },
            { level: 2, label: 'Fair', color: 'bg-yellow-500' },
            { level: 3, label: 'Good', color: 'bg-blue-500' },
            { level: 4, label: 'Strong', color: 'bg-green-500' },
        ];
        return levels[score] || levels[0];
    }

    togglePassword(): void { this.showPassword = !this.showPassword; }
    toggleConfirm(): void { this.showConfirm = !this.showConfirm; }

    onSubmit(): void {
        if (this.registerForm.invalid) {
            this.registerForm.markAllAsTouched();
            return;
        }
        this.isLoading = true;
        this.errorMessage.set('');
        this.successMessage.set('');

        this.authService.register(
            this.username.value,
            this.email.value,
            this.password.value
        ).subscribe({
            next: () => {
                this.isLoading = false;
                this.successMessage.set('Account created successfully! Please log in.');
                setTimeout(() => this.router.navigate(['/login']), 1000);
            },
            error: (err) => {
                this.isLoading = false;
                if (err.status === 400) {
                    this.errorMessage.set('An account with this email already exists.');
                } else {
                    this.errorMessage.set('Registration failed. Please try again.');
                }
            }
        });
    }
}
