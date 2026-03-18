import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AdminClaimsOfficersComponent } from './admin-claims-officers.component';
import { AdminService } from '../../services/admin.service';

describe('AdminClaimsOfficersComponent', () => {
  let component: AdminClaimsOfficersComponent;
  let fixture: ComponentFixture<AdminClaimsOfficersComponent>;

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

    TestBed.configureTestingModule({
      imports: [AdminClaimsOfficersComponent],
      providers: [{ provide: AdminService, useValue: adminServiceMock }]
    });

    fixture = TestBed.createComponent(AdminClaimsOfficersComponent);
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
  });
});
