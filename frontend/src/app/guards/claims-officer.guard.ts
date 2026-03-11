import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const claimsOfficerGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isLoggedIn()) {
        router.navigate(['/login']);
        return false;
    }

    if (auth.isClaimsOfficer()) {
        return true;
    }

    // Logged in but not claims officer â†’ send them to their dashboard
    router.navigate(['/dashboard']);
    return false;
};
