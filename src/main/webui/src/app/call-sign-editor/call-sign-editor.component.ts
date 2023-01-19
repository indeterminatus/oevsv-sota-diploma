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

import {Component, OnInit} from '@angular/core';
import {FormControl, Validators} from "@angular/forms";
import {TranslateService} from "@ngx-translate/core";
import {BaseComponent} from "../base-component/base.component";

@Component({
  selector: 'app-callsign-editor',
  templateUrl: './call-sign-editor.component.html',
  styleUrls: ['./call-sign-editor.component.css']
})
export class CallSignEditorComponent extends BaseComponent implements OnInit {

  readonly callSign = new FormControl('', [Validators.required, Validators.pattern(/^([A-Z0-9]+\/)?[A-Z0-9]{1,2}\d+[A-Z]+(\/[A-Z0-9]+)?$/iu)]);

  constructor(override readonly translate: TranslateService) {
    super(translate);
  }

  ngOnInit(): void {
    this.target.addControl("callSign", this.callSign);
  }

  getErrorMessage(): string {
    if (this.callSign.hasError('required')) {
      return this.translate.instant('input.callsign.error.required');
    }
    if (this.callSign.hasError('pattern')) {
      return this.translate.instant('input.callsign.error.invalid');
    }
    return '';
  }
}
