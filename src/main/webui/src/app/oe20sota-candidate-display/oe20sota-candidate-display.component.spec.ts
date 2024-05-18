import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Oe20sotaCandidateDisplayComponent } from './oe20sota-candidate-display.component';

describe('Oe20sotaCandidateDisplayComponent', () => {
  let component: Oe20sotaCandidateDisplayComponent;
  let fixture: ComponentFixture<Oe20sotaCandidateDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Oe20sotaCandidateDisplayComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Oe20sotaCandidateDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
