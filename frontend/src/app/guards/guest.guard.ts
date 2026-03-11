import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const guestGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isLoggedIn()) {
        return true;
    }

    alert('You are already logged in.');

    const role = auth.getRole()?.toLowerCase();
    if (role === 'admin') {
        router.navigate(['/admin']);
    } else if (role === 'underwriter') {
        router.navigate(['/underwriter']);
    } else if (role === 'claims_officer') {
        router.navigate(['/claims-officer']);
    } else {
        router.navigate(['/dashboard']);
    }

    return false;
};
