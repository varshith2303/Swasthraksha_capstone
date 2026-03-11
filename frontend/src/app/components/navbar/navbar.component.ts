import { Component, HostListener, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, CommonModule],
    templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit {
    isScrolled = false;
    isMenuOpen = false;
    isLoggedIn = false;

    constructor(private authService: AuthService, private router: Router) { }

    ngOnInit(): void {
        this.isLoggedIn = this.authService.isLoggedIn();
    }

    @HostListener('window:scroll')
    onScroll(): void {
        this.isScrolled = window.scrollY > 30;
    }

    toggleMenu(): void {
        this.isMenuOpen = !this.isMenuOpen;
    }

    logout(): void {
        this.authService.logout();
        this.isLoggedIn = false;
        this.router.navigate(['/']);
    }
}
