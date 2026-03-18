/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoadingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should start with loading false', () => {
    expect(service.isLoading()).toBeFalse();
  });

  it('should set loading true after increment', () => {
    service.increment();
    expect(service.isLoading()).toBeTrue();
  });

  it('should return loading false after matching decrement', () => {
    service.increment();
    service.decrement();
    expect(service.isLoading()).toBeFalse();
  });

  it('should not go below zero on decrement edge case', () => {
    service.decrement();
    expect(service.isLoading()).toBeFalse();
  });
});
