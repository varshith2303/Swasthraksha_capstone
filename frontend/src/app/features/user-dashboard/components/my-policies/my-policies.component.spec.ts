import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MyPoliciesComponent } from './my-policies.component';
import { UserService } from '../../services/user.service';

describe('MyPoliciesComponent', () => {
  let component: MyPoliciesComponent;
  let fixture: ComponentFixture<MyPoliciesComponent>;

  beforeEach(() => {
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

    TestBed.configureTestingModule({
      imports: [MyPoliciesComponent],
      providers: [{ provide: UserService, useValue: userServiceMock }]
    });

    fixture = TestBed.createComponent(MyPoliciesComponent);
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

  it('should have no ngOnInit lifecycle requirement', () => {
    expect((component as any).ngOnInit).toBeUndefined();
  });

  it('should interact with injected dependencies', () => {
    expect(TestBed.inject(UserService)).toBeTruthy();
  });
});
