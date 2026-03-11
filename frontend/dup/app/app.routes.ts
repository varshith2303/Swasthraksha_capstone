import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { underwriterGuard } from './guards/underwriter.guard';
import { claimsOfficerGuard } from './guards/claims-officer.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/landing/landing.component').then(m => m.LandingComponent)
    },
    {
        path: 'login',
        loadComponent: () =>
            import('./pages/login/login.component').then(m => m.LoginComponent)
    },
    {
        path: 'register',
        loadComponent: () =>
            import('./pages/register/register.component').then(m => m.RegisterComponent)
    },
    {
        path: 'dashboard',
        loadComponent: () =>
            import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [authGuard]
    },
    {
        path: 'admin',
        loadComponent: () =>
            import('./pages/admin/admin.component').then(m => m.AdminComponent),
        canActivate: [adminGuard]
    },
    {
        path: 'underwriter',
        loadComponent: () =>
            import('./pages/underwriter/underwriter.component').then(m => m.UnderwriterComponent),
        canActivate: [underwriterGuard]
    },
    {
        path: 'claims-officer',
        loadComponent: () =>
            import('./pages/claims-officer/claims-officer.component').then(m => m.ClaimsOfficerComponent),
        canActivate: [claimsOfficerGuard]
    },
    {
        path: '**',
        redirectTo: ''
    }
];


