import { Component, OnInit, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../services/auth.service';
import { UserService, Policy, Application, ApplicationRequest, PolicyAssignment, PolicyMemberRequest } from '../../../../services/user.service';

// Sub-components
import { SidebarComponent } from '../sidebar/sidebar.component';
import { PolicyApplyComponent } from '../policy-apply/policy-apply.component';
import { MyApplicationsComponent } from '../my-applications/my-applications.component';
import { MyPoliciesComponent } from '../my-policies/my-policies.component';
import { PolicyCatalogComponent } from '../policy-catalog/policy-catalog.component';
import { MyClaimsComponent } from '../my-claims/my-claims.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SidebarComponent,
    PolicyApplyComponent,
    MyApplicationsComponent,
    MyPoliciesComponent,
    PolicyCatalogComponent,
    MyClaimsComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  userEmail = '';
  activeTab: 'apply' | 'my-applications' | 'policies' | 'my-policies' | 'my-claims' = 'apply';

  // Shared Data (State)
  policies = signal<Policy[]>([]);
  policiesLoading = false;

  myApplications = signal<Application[]>([]);
  myAppsLoading = false;
  myAppsError = '';

  myPolicyAssignments = signal<PolicyAssignment[]>([]);
  myPoliciesLoading = false;
  myPoliciesError = '';

  // Only ACTIVE (POLICY_ISSUED+paid) policies are eligible for claims
  activeIssuedPolicies = computed(() =>
    this.myPolicyAssignments().filter(p => p.status === 'ACTIVE')
  );

  // Selected policy shortcut from the catalog — passed into PolicyApplyComponent
  preselectedPolicyCode = '';

  actionLoading: Record<number, boolean> = {};

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.userEmail = this.authService.getCurrentUser() || 'User';
    this.loadAllData();
  }

  loadAllData(): void {
    this.loadPolicies();
    this.loadMyApplications();
    this.loadMyPolicies();
  }

  setTab(tab: string): void {
    this.activeTab = tab as any;

    // Refresh targeted data on tab switch
    if (tab === 'my-applications') this.loadMyApplications();
    if (tab === 'policies') this.loadPolicies();
    if (tab === 'my-policies') this.loadMyPolicies();
    if (tab === 'my-claims') this.loadMyPolicies(); // ensure active policies are up-to-date for the claim form
  }

  loadPolicies(): void {
    this.policiesLoading = true;
    this.userService.getAllPolicies().subscribe({
      next: (data) => {
        this.policies.set(data.filter(p => p.active));
        this.policiesLoading = false;
      },
      error: () => this.policiesLoading = false
    });
  }

  loadMyApplications(): void {
    this.myAppsLoading = true;
    this.myAppsError = '';
    this.userService.getMyApplications().subscribe({
      next: (data) => {
        this.myApplications.set(data);
        this.myAppsLoading = false;
      },
      error: () => {
        this.myAppsError = 'Failed to load applications.';
        this.myAppsLoading = false;
      }
    });
  }

  loadMyPolicies(): void {
    this.myPoliciesLoading = true;
    this.myPoliciesError = '';
    this.userService.getMyPolicyAssignments().subscribe({
      next: (data) => {
        this.myPolicyAssignments.set(data);
        this.myPoliciesLoading = false;
      },
      error: () => {
        this.myPoliciesError = 'Failed to load policies.';
        this.myPoliciesLoading = false;
      }
    });
  }

  onInitiateApply(policyCode: string): void {
    this.preselectedPolicyCode = policyCode;
    this.setTab('apply');
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
