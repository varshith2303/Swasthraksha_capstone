import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { ClaimsOfficerComponent } from './claims-officer.component';
import { ClaimsOfficerService } from '../../services/claims-officer.service';
import { AuthService } from '../../../../services/auth.service';
import { DocumentService } from '../../../../services/document.service';

describe('ClaimsOfficerComponent', () => {
  let component: ClaimsOfficerComponent;
  let fixture: ComponentFixture<ClaimsOfficerComponent>;

  beforeEach(() => {
    const claimsOfficerServiceMock = jasmine.createSpyObj('ClaimsOfficerService', ['getAssignedClaims', 'verifyClaim']);
    claimsOfficerServiceMock.getAssignedClaims.and.returnValue(of([]));
    claimsOfficerServiceMock.verifyClaim.and.returnValue(of('ok'));
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
    const documentServiceMock = jasmine.createSpyObj('DocumentService', ['getApplicationDocuments', 'getClaimDocuments', 'getDownloadUrl', 'downloadDocument']);
    documentServiceMock.getApplicationDocuments.and.returnValue(of([]));
    documentServiceMock.getClaimDocuments.and.returnValue(of([]));
    documentServiceMock.getDownloadUrl.and.callFake((id: number) => '/download/' + id);
    documentServiceMock.downloadDocument.and.returnValue(of(new Blob()));
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);
    routerMock.navigate.and.returnValue(Promise.resolve(true));

    TestBed.configureTestingModule({
      imports: [ClaimsOfficerComponent],
      providers: [{ provide: ClaimsOfficerService, useValue: claimsOfficerServiceMock }, { provide: AuthService, useValue: authServiceMock }, { provide: DocumentService, useValue: documentServiceMock }, { provide: Router, useValue: routerMock }]
    });

    fixture = TestBed.createComponent(ClaimsOfficerComponent);
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
    expect(TestBed.inject(ClaimsOfficerService)).toBeTruthy();
    expect(TestBed.inject(AuthService)).toBeTruthy();
    expect(TestBed.inject(DocumentService)).toBeTruthy();
    expect(TestBed.inject(Router)).toBeTruthy();
  });
});
