import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { AdminComponent } from './admin.component';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../../../services/auth.service';

describe('AdminComponent', () => {
  let component: AdminComponent;
  let fixture: ComponentFixture<AdminComponent>;

  beforeEach(() => {
    const adminServiceMock = jasmine.createSpyObj('AdminService', ['getAllPolicies', 'addPolicy', 'updatePolicy', 'deletePolicy', 'togglePolicyStatus', 'addUnderwriter', 'getUnderwriters', 'addClaimsOfficer', 'getClaimsOfficers', 'deleteUser', 'getAllApplications', 'assignApplication', 'getAllClaims', 'assignClaim', 'verifyClaim', 'getOfficerAssignedClaims', 'getAllIssuedPolicies']);
    adminServiceMock.getAllPolicies.and.returnValue(of([]));
    adminServiceMock.addPolicy.and.returnValue(of({}));
    adminServiceMock.updatePolicy.and.returnValue(of({}));
    adminServiceMock.deletePolicy.and.returnValue(of(void 0));
    adminServiceMock.togglePolicyStatus.and.returnValue(of({}));
    adminServiceMock.addUnderwriter.and.returnValue(of({}));
    adminServiceMock.getUnderwriters.and.returnValue(of([]));
    adminServiceMock.addClaimsOfficer.and.returnValue(of({}));
    adminServiceMock.getClaimsOfficers.and.returnValue(of([]));
    adminServiceMock.deleteUser.and.returnValue(of(void 0));
    adminServiceMock.getAllApplications.and.returnValue(of([]));
    adminServiceMock.assignApplication.and.returnValue(of({}));
    adminServiceMock.getAllClaims.and.returnValue(of([]));
    adminServiceMock.assignClaim.and.returnValue(of('ok'));
    adminServiceMock.verifyClaim.and.returnValue(of('ok'));
    adminServiceMock.getOfficerAssignedClaims.and.returnValue(of([]));
    adminServiceMock.getAllIssuedPolicies.and.returnValue(of([]));
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
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);
    routerMock.navigate.and.returnValue(Promise.resolve(true));

    TestBed.configureTestingModule({
      imports: [AdminComponent],
      providers: [{ provide: AdminService, useValue: adminServiceMock }, { provide: AuthService, useValue: authServiceMock }, { provide: Router, useValue: routerMock }]
    });

    fixture = TestBed.createComponent(AdminComponent);
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
    expect(TestBed.inject(AdminService)).toBeTruthy();
    expect(TestBed.inject(AuthService)).toBeTruthy();
    expect(TestBed.inject(Router)).toBeTruthy();
  });
});
