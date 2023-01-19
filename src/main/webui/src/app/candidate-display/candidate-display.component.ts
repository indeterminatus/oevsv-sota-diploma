/*
 * Copyright (C) 2023 David Schwingenschlögl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, Input, OnInit} from '@angular/core';
import {BaseComponent} from "../base-component/base.component";
import {TranslateService} from "@ngx-translate/core";
import {SignedCandidate} from "../api/signed-candidate";
import {DiplomaService} from "../diploma.service";
import {DiplomaRequest} from "../api/diploma-request";
import {Requester} from "../api/requester";

@Component({
  selector: 'app-candidate-display',
  templateUrl: './candidate-display.component.html',
  styleUrls: ['./candidate-display.component.css']
})
export class CandidateDisplayComponent extends BaseComponent implements OnInit {

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
    if (this.requester && this.signedCandidate) {
      let request: DiplomaRequest = {
        requester: this.requester,
        candidates: [this.signedCandidate]
      };
      this.checking = true;
      this.diplomaService.request(request).then((result: boolean) => {
        this.checked = true;
        this.success = result;
        this.checking = false;
      }, (reason: any) => {
        this.checked = true;
        this.success = false;
        this.lastError = reason;
        this.checking = false;
      });
    }
    else {
      this.checked = true;
      this.checking = false;
      this.success = false;
      this.lastError = '';
    }
  }

  getRank(): string {
    return this.signedCandidate?.candidate.rank || "NONE";
  }

  getCategory(): string | undefined {
    return this.signedCandidate?.candidate.category;
  }

  getErrorMessage(): string {
    return this.lastError || '';
  }
}
