import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isLoggedIn()) {
        router.navigate(['/login']);
        return false;
    }

    if (auth.isAdmin()) {
        return true;
    }

    // Logged in but not admin → send to their dashboard
    router.navigate(['/dashboard']);
    return false;
};
