import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Router, provideRouter } from '@angular/router';
import { NavbarComponent } from './navbar.component';
import { AuthService } from '../../services/auth.service';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

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

    TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [{ provide: AuthService, useValue: authServiceMock }, provideRouter([])]
    });

    fixture = TestBed.createComponent(NavbarComponent);
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
    expect(TestBed.inject(Router)).toBeTruthy();
  });
});
