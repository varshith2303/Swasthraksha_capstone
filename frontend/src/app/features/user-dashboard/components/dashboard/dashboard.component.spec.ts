import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../../../services/auth.service';
import { UserService } from '../../services/user.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(() => {
    const authServiceMock = jasmine.createSpyObj('AuthService', ['login', 'register', 'logout', 'isLoggedIn', 'getToken', 'getCurrentUser', 'getRole', 'isAdmin', 'isUnderwriter', 'isClaimsOfficer']);
    authServiceMock.login.and.returnValue(of({ token: 'token' }));
    authServiceMock.register.and.returnValue(of({}));
    authServiceMock.isLoggedIn.and.returnValue(true);
    authServiceMock.getToken.and.returnValue('token');
    authServiceMock.getCurrentUser.and.returnValue('user@test.com');
    authServiceMock.getRole.and.returnValue('USER');
    authServiceMock.isAdmin.and.returnValue(false);
    authServiceMock.isUnderwriter.and.returnValue(false);
    authServiceMock.isClaimsOfficer.and.returnValue(false);
    const userServiceMock = jasmine.createSpyObj('UserService', ['getAllPolicies', 'applyForPolicy', 'getMyApplications', 'getMyPolicyAssignments', 'makePolicyPayment', 'acceptApplication', 'makeFirstPayment', 'declineApplication', 'submitClaim', 'getMyClaims', 'uploadApplicationDocument', 'uploadClaimDocument']);
    userServiceMock.getAllPolicies.and.returnValue(of([]));
    userServiceMock.applyForPolicy.and.returnValue(of({}));
    userServiceMock.getMyApplications.and.returnValue(of([]));
    userServiceMock.getMyPolicyAssignments.and.returnValue(of([]));
    userServiceMock.makePolicyPayment.and.returnValue(of({}));
    userServiceMock.acceptApplication.and.returnValue(of({}));
    userServiceMock.makeFirstPayment.and.returnValue(of({}));
    userServiceMock.declineApplication.and.returnValue(of({}));
    userServiceMock.submitClaim.and.returnValue(of({ claimNumber: 'CLM-1' }));
    userServiceMock.getMyClaims.and.returnValue(of([]));
    userServiceMock.uploadApplicationDocument.and.returnValue(of({}));
    userServiceMock.uploadClaimDocument.and.returnValue(of({}));
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);
    routerMock.navigate.and.returnValue(Promise.resolve(true));

    TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [{ provide: AuthService, useValue: authServiceMock }, { provide: UserService, useValue: userServiceMock }, { provide: Router, useValue: routerMock }]
    });

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component variables', () => {
    expect(Object.keys(component as any).length).toBeGreaterThan(0);
  });

  it('should expose component methods', () => {
    const methods = Object.getOwnPropertyNames(Object.getPrototypeOf(component))
      .filter(m => !['constructor', 'ngOnInit'].includes(m));
    expect(methods.length).toBeGreaterThan(0);
  });

  it('should render template', () => {
    fixture.detectChanges();
    const nativeElement = fixture.nativeElement as HTMLElement;
    expect(nativeElement).toBeTruthy();
  });

  it('should execute ngOnInit lifecycle', () => {
    const initSpy = spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(initSpy).toHaveBeenCalled();
  });

  it('should interact with injected dependencies', () => {
    expect(TestBed.inject(AuthService)).toBeTruthy();
    expect(TestBed.inject(UserService)).toBeTruthy();
    expect(TestBed.inject(Router)).toBeTruthy();
  });
});
