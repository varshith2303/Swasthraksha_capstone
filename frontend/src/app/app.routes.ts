import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { underwriterGuard } from './guards/underwriter.guard';
import { claimsOfficerGuard } from './guards/claims-officer.guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./features/landing/components/landing/landing.component').then(m => m.LandingComponent)
    },
    {
        path: 'login',
        canActivate: [guestGuard],
        loadComponent: () =>
            import('./features/auth/components/login/login.component').then(m => m.LoginComponent)
    },
    {
        path: 'register',
        canActivate: [guestGuard],
        loadComponent: () =>
            import('./features/auth/components/register/register.component').then(m => m.RegisterComponent)
    },
    {
        path: 'dashboard',
        loadComponent: () =>
            import('./features/user-dashboard/components/dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [authGuard]
    },
    {
        path: 'admin',
        loadComponent: () =>
            import('./features/admin/components/admin/admin.component').then(m => m.AdminComponent),
        canActivate: [adminGuard]
    },
    {
        path: 'underwriter',
        loadComponent: () =>
            import('./features/underwriter/components/underwriter/underwriter.component').then(m => m.UnderwriterComponent),
        canActivate: [underwriterGuard]
    },
    {
        path: 'claims-officer',
        loadComponent: () =>
            import('./features/claims-officer/components/claims-officer/claims-officer.component').then(m => m.ClaimsOfficerComponent),
        canActivate: [claimsOfficerGuard]
    },
    {
        path: '**',
        redirectTo: ''
    }
];


