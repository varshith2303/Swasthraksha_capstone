import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../../../components/navbar/navbar.component';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-landing',
    standalone: true,
    imports: [RouterLink, NavbarComponent, CommonModule],
    templateUrl: './landing.component.html',
})
export class LandingComponent {
    stats = [
        { value: '50K+', label: 'Happy Members', icon: 'fa-users' },
        { value: '99%', label: 'Claims Approved', icon: 'fa-circle-check' },
        { value: '24/7', label: 'Customer Support', icon: 'fa-headset' },
        { value: '500+', label: 'Hospital Network', icon: 'fa-hospital' },
    ];

    features = [
        {
            icon: 'fa-shield-heart',
            title: 'Comprehensive Plans',
            description: 'Choose from a wide array of health insurance plans tailored for individuals, families, and senior citizens.',
            color: 'from-cyan-500 to-teal-500'
        },
        {
            icon: 'fa-bolt',
            title: 'Instant Claim Settlement',
            description: 'Experience lightning-fast claim processing with our AI-powered system. Get reimbursed within 24 hours.',
            color: 'from-purple-500 to-pink-500'
        },
        {
            icon: 'fa-hospital',
            title: 'Nationwide Coverage',
            description: 'Access 500+ network hospitals across the country with cashless treatment facilities.',
            color: 'from-orange-500 to-red-500'
        },
        {
            icon: 'fa-mobile-screen',
            title: 'Digital First',
            description: 'Manage your policies, submit claims, and track everything from your smartphone anytime, anywhere.',
            color: 'from-green-500 to-emerald-500'
        },
        {
            icon: 'fa-user-doctor',
            title: 'Expert Consultations',
            description: 'Get free unlimited teleconsultations with certified doctors — available 24/7 from your home.',
            color: 'from-blue-500 to-indigo-500'
        },
        {
            icon: 'fa-lock',
            title: 'Secure & Transparent',
            description: 'Bank-grade security for your data and complete transparency in policy terms and claim status.',
            color: 'from-yellow-500 to-orange-500'
        },
    ];

    plans = [
        {
            name: 'Basic',
            price: '₹499',
            period: '/month',
            coverage: '₹3 Lakh',
            features: ['Individual Coverage', '100+ Network Hospitals', 'Basic Claim Support', 'Annual Health Check-up'],
            popular: false,
            gradient: 'border-white/10'
        },
        {
            name: 'Family',
            price: '₹999',
            period: '/month',
            coverage: '₹10 Lakh',
            features: ['Family of 4 Covered', '500+ Network Hospitals', 'Priority Claims', 'Free Teleconsultation', 'Dental & Vision Add-on'],
            popular: true,
            gradient: 'border-primary-400'
        },
        {
            name: 'Premium',
            price: '₹1,999',
            period: '/month',
            coverage: '₹25 Lakh',
            features: ['Family + Senior Covered', 'All Network Hospitals', 'Dedicated Case Manager', 'International Coverage', 'Critical Illness Rider', 'No Room Rent Limit'],
            popular: false,
            gradient: 'border-white/10'
        },
    ];

    testimonials = [
        {
            name: 'Priya Sharma',
            role: 'Software Engineer, Bangalore',
            avatar: 'PS',
            text: 'SwasthaRaksha made my hospital claim incredibly easy. Filed online, got approved in 6 hours. Absolutely amazing service!',
            rating: 5
        },
        {
            name: 'Rajesh Kumar',
            role: 'Business Owner, Mumbai',
            avatar: 'RK',
            text: 'Enrolled my entire family in the Premium plan. The cashless experience at network hospitals is seamless. Worth every rupee!',
            rating: 5
        },
        {
            name: 'Anita Desai',
            role: 'Teacher, Delhi',
            avatar: 'AD',
            text: 'The free teleconsultation feature alone is worth it. My children get doctor advice at 11 PM without any extra cost!',
            rating: 5
        },
    ];
}
