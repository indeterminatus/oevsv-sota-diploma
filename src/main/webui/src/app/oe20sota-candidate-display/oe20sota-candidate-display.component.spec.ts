import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Oe20sotaCandidateDisplayComponent } from './oe20sota-candidate-display.component';
import { TranslateService } from '@ngx-translate/core';
import { DiplomaService } from '../diploma.service';
import { SignedCandidate } from '../api/signed-candidate';
import { Requester } from '../api/requester';
import { of } from 'rxjs';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

describe('Oe20sotaCandidateDisplayComponent', () => {
  let component: Oe20sotaCandidateDisplayComponent;
  let fixture: ComponentFixture<Oe20sotaCandidateDisplayComponent>;
  let translateServiceMock: jasmine.SpyObj<TranslateService>;
  let diplomaServiceMock: jasmine.SpyObj<DiplomaService>;

  beforeEach(async () => {
    translateServiceMock = jasmine.createSpyObj('TranslateService', ['get'], {
      currentLang: 'en'
    });
    translateServiceMock.get.and.returnValue(of('translated text'));

    diplomaServiceMock = jasmine.createSpyObj('DiplomaService', ['request']);

    await TestBed.configureTestingModule({
      declarations: [Oe20sotaCandidateDisplayComponent],
      imports: [FontAwesomeModule],
      providers: [
        { provide: TranslateService, useValue: translateServiceMock },
        { provide: DiplomaService, useValue: diplomaServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Oe20sotaCandidateDisplayComponent);
    component = fixture.componentInstance;
  });

  // Test 1: Component initialization
  it('should create the component', () => {
    expect(component).toBeTruthy();
    expect(component.checking).toBeFalse();
    expect(component.checked).toBeFalse();
    expect(component.success).toBeFalse();
  });

  // Test 2: Request diploma with valid data
  it('should request diploma with valid data', fakeAsync(() => {
    // Arrange
    const mockRequester: Requester = { callSign: 'oe5idt', name: 'Test User', mail: 'test@example.com' };
    const mockCandidate: SignedCandidate = {
      candidate: {
        callsign: 'oe5idt',
        category: 'OE20SOTA',
        activations: { '2023': 25 }
      },
      signature: 'test-signature'
    };

    component.requester = mockRequester;
    component.signedCandidate = mockCandidate;

    diplomaServiceMock.request.and.returnValue(Promise.resolve(true));

    // Act
    component.requestDiploma();

    // Assert - initial state
    expect(component.checking).toBeTrue();
    expect(component.checked).toBeFalse();

    // Complete the promise
    tick();

    // Assert - final state
    expect(component.checking).toBeFalse();
    expect(component.checked).toBeTrue();
    expect(component.success).toBeTrue();
    expect(component.getErrorMessage()).toBe('');
    expect(diplomaServiceMock.request).toHaveBeenCalledWith({
      requester: mockRequester,
      candidates: [mockCandidate],
      language: 'en'
    });
  }));

  // Test 3: Request diploma with insufficient activations
  it('should not request diploma with insufficient activations', () => {
    // Arrange
    const mockRequester: Requester = { name: 'Test User', email: 'test@example.com' };
    const mockCandidate: SignedCandidate = {
      candidate: {
        callsign: 'oe5idt',
        category: 'OE20SOTA',
        activations: { '2023': 19 }
      },
      signature: 'test-signature'
    };

    component.requester = mockRequester;
    component.signedCandidate = mockCandidate;

    // Act
    component.requestDiploma();

    // Assert
    expect(component.checking).toBeFalse();
    expect(component.checked).toBeTrue();
    expect(component.success).toBeFalse();
    expect(diplomaServiceMock.request).not.toHaveBeenCalled();
  });

  // Test 4: Request diploma with missing requester
  it('should not request diploma with missing requester', () => {
    // Arrange
    component.requester = undefined;
    component.signedCandidate = {
      candidate: {
        callsign: 'oe5idt',
        category: 'OE20SOTA',
        activations: { '2023': 25 }
      },
      signature: 'test-signature'
    };

    // Act
    component.requestDiploma();

    // Assert
    expect(component.checking).toBeFalse();
    expect(component.checked).toBeTrue();
    expect(component.success).toBeFalse();
    expect(diplomaServiceMock.request).not.toHaveBeenCalled();
  });

  // Test 5: Request diploma with missing candidate
  it('should not request diploma with missing candidate', () => {
    // Arrange
    component.requester = { name: 'Test User', email: 'test@example.com' };
    component.signedCandidate = undefined;

    // Act
    component.requestDiploma();

    // Assert
    expect(component.checking).toBeFalse();
    expect(component.checked).toBeTrue();
    expect(component.success).toBeFalse();
    expect(diplomaServiceMock.request).not.toHaveBeenCalled();
  });

  // Test 6: Get category returns correct value
  it('should return correct category', () => {
    // Arrange
    component.signedCandidate = {
      candidate: {
        callsign: 'oe5idt',
        category: 'OE20SOTA',
        activations: { '2023': 25 }
      },
      signature: 'test-signature'
    };

    // Act & Assert
    expect(component.getCategory()).toBe('SOTA');

    // Test with undefined
    component.signedCandidate = undefined;
    expect(component.getCategory()).toBeUndefined();
  });

  // Test 7: Get error message returns value
  it('should return error message', () => {
    // Test with undefined lastError
    expect(component.getErrorMessage()).toBe('');

    // Set up error scenario
    component.requester = { name: 'Test User', email: 'test@example.com' };
    component.signedCandidate = {
      candidate: {
        callsign: 'oe5idt',
        category: 'OE20SOTA',
        activations: { '2023': 25 }
      },
      signature: 'test-signature'
    };

    diplomaServiceMock.request.and.returnValue(Promise.resolve(false));

    // Act
    component.requestDiploma();

    // Complete the promise
    fixture.detectChanges();

    // Assert
    expect(component.getErrorMessage()).toBe('api.response.request.failure');
  });

  // Test 8: Get total activations calculation
  it('should calculate total activations correctly', () => {
    // Arrange
    component.signedCandidate = {
      candidate: {
        callsign: 'oe5idt',
        category: 'OE20SOTA',
        activations: { '2021': 10, '2022': 15, '2023': 25 }
      },
      signature: 'test-signature'
    };

    // Act & Assert
    expect(component.getTotalActivations()).toBe(50);

    // Test with empty activations
    component.signedCandidate = {
      candidate: {
        callsign: 'oe5idt',
        category: 'OE20SOTA',
        activations: {}
      },
      signature: 'test-signature'
    };
    expect(component.getTotalActivations()).toBe(0);
  });
});
