import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MemberCardComponent } from './member-card.component';

describe('MemberCardComponent', () => {
  let component: MemberCardComponent;
  let fixture: ComponentFixture<MemberCardComponent>;

  beforeEach(() => {
    

    TestBed.configureTestingModule({
      imports: [MemberCardComponent],
      providers: []
    });

    fixture = TestBed.createComponent(MemberCardComponent);
    component = fixture.componentInstance;
    component.member = {
      name: 'John Doe',
      age: 30,
      bmi: 22,
      smoker: false,
      existingDiseases: '',
      relationship: 'SELF'
    };
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

  it('should run without injected services', () => {
    expect(component).toBeTruthy();
  });
});
