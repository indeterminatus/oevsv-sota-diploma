import {Component, Input, OnInit} from '@angular/core';
import {faAward, faBarsProgress, faCheck} from "@fortawesome/free-solid-svg-icons";
import {SignedCandidate} from "../api/signed-candidate";
import {Requester} from "../api/requester";
import {TranslateService} from "@ngx-translate/core";
import {DiplomaService} from "../diploma.service";
import {DiplomaRequest} from "../api/diploma-request";
import {BaseComponent} from "../base-component/base.component";

@Component({
  selector: 'app-oe20sota-candidate-display',
  templateUrl: './oe20sota-candidate-display.component.html',
  styleUrls: ['./oe20sota-candidate-display.component.css']
})
export class Oe20sotaCandidateDisplayComponent extends BaseComponent implements OnInit {

  checkIcon = faCheck;
  categoryIcon = faAward;
  wipIcon = faBarsProgress;

  checking: boolean = false;
  checked: boolean = false;
  success: boolean = false;

  @Input("ngForAs")
  signedCandidate: SignedCandidate | undefined;

  @Input()
  requester: Requester | undefined;

  private lastError: string | undefined;

  constructor(override readonly translate: TranslateService, private readonly diplomaService: DiplomaService) {
    super(translate);
  }

  ngOnInit(): void {
  }

  requestDiploma(): void {
    if (this.requester && this.signedCandidate && this.getTotalActivations() >= 20) {
      console.log("Current language: ", this.translate.currentLang);
      let request: DiplomaRequest = {
        requester: this.requester,
        candidates: [this.signedCandidate],
        language: this.translate.currentLang === 'en' ? 'en' : 'de'
      };
      this.checking = true;
      this.diplomaService.request(request).then((result: boolean) => {
        this.success = result;
      }, (reason: any) => {
        this.success = false;
        console.debug("Error!", reason);
      }).finally(() => {
        this.checked = true;
        this.checking = false;
        this.lastError = this.success ? '' : 'api.response.request.failure';
      });
    } else {
      this.checked = true;
      this.checking = false;
      this.success = false;
      this.lastError = '';
    }
  }

  getCategory(): string | undefined {
    return this.signedCandidate?.candidate.category;
  }

  getErrorMessage(): string {
    return this.lastError || '';
  }

  getTotalActivations(): number {
    const summing = (sum: number, current: number) => sum + current;

    return Object.values(this.signedCandidate?.candidate?.activations as Object).reduce(summing, 0);
  }
}
