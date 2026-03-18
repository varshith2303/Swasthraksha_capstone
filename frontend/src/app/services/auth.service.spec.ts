/// <reference types="jasmine" />
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login and persist token and user', fakeAsync(() => {
    let responseToken = '';
    service.login('user@test.com', 'secret123').subscribe(res => {
      responseToken = res.token;
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'user@test.com', password: 'secret123' });
    req.flush({ token: 'jwt-token' });
    tick();

    expect(responseToken).toBe('jwt-token');
    expect(service.getToken()).toBe('jwt-token');
    expect(service.getCurrentUser()).toBe('user@test.com');
    expect(service.isLoggedIn()).toBeTrue();
  }));

  it('should register user with USER role', () => {
    service.register('john', 'john@test.com', 'pass12345').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      username: 'john',
      email: 'john@test.com',
      password: 'pass12345',
      role: 'USER'
    });
    req.flush({ success: true });
  });

  it('should logout and clear localStorage', () => {
    localStorage.setItem('swastha_token', 'x');
    localStorage.setItem('swastha_user', 'u');

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should return null role when token is absent', () => {
    expect(service.getRole()).toBeNull();
  });
});
