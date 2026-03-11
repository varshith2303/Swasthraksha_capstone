import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { underwriterGuard } from './guards/underwriter.guard';
import { claimsOfficerGuard } from './guards/claims-officer.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./features/landing/landing.component').then(m => m.LandingComponent)
    },
    {
        path: 'login',
        loadComponent: () =>
            import('./features/auth/login/login.component').then(m => m.LoginComponent)
    },
    {
        path: 'register',
        loadComponent: () =>
            import('./features/auth/register/register.component').then(m => m.RegisterComponent)
    },
    {
        path: 'dashboard',
        loadComponent: () =>
            import('./features/user-dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [authGuard]
    },
    {
        path: 'admin',
        loadComponent: () =>
            import('./features/admin/admin.component').then(m => m.AdminComponent),
        canActivate: [adminGuard]
    },
    {
        path: 'underwriter',
        loadComponent: () =>
            import('./features/underwriter/underwriter.component').then(m => m.UnderwriterComponent),
        canActivate: [underwriterGuard]
    },
    {
        path: 'claims-officer',
        loadComponent: () =>
            import('./features/claims-officer/claims-officer.component').then(m => m.ClaimsOfficerComponent),
        canActivate: [claimsOfficerGuard]
    },
    {
        path: '**',
        redirectTo: ''
    }
];


