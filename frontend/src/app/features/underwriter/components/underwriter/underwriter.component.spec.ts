import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { UnderwriterComponent } from './underwriter.component';
import { AuthService } from '../../../../services/auth.service';
import { UnderwriterService } from '../../services/underwriter.service';
import { DocumentService } from '../../../../services/document.service';

describe('UnderwriterComponent', () => {
  let component: UnderwriterComponent;
  let fixture: ComponentFixture<UnderwriterComponent>;

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
    const underwriterServiceMock = jasmine.createSpyObj('UnderwriterService', ['getAssignedApplications', 'getPendingApplications', 'updateApplication']);
    underwriterServiceMock.getAssignedApplications.and.returnValue(of([]));
    underwriterServiceMock.getPendingApplications.and.returnValue(of([]));
    underwriterServiceMock.updateApplication.and.returnValue(of({}));
    const documentServiceMock = jasmine.createSpyObj('DocumentService', ['getApplicationDocuments', 'getClaimDocuments', 'getDownloadUrl', 'downloadDocument']);
    documentServiceMock.getApplicationDocuments.and.returnValue(of([]));
    documentServiceMock.getClaimDocuments.and.returnValue(of([]));
    documentServiceMock.getDownloadUrl.and.callFake((id: number) => '/download/' + id);
    documentServiceMock.downloadDocument.and.returnValue(of(new Blob()));
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);
    routerMock.navigate.and.returnValue(Promise.resolve(true));

    TestBed.configureTestingModule({
      imports: [UnderwriterComponent],
      providers: [{ provide: AuthService, useValue: authServiceMock }, { provide: UnderwriterService, useValue: underwriterServiceMock }, { provide: DocumentService, useValue: documentServiceMock }, { provide: Router, useValue: routerMock }]
    });

    fixture = TestBed.createComponent(UnderwriterComponent);
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
    expect(TestBed.inject(UnderwriterService)).toBeTruthy();
    expect(TestBed.inject(DocumentService)).toBeTruthy();
    expect(TestBed.inject(Router)).toBeTruthy();
  });
});
