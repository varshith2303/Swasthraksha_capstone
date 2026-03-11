import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const underwriterGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const role = authService.getRole()?.toUpperCase();
    if (authService.isLoggedIn() && role === 'UNDERWRITER') {
        return true;
    }

    // Redirect to login if not underwriter
    router.navigate(['/login']);
    return false;
};
