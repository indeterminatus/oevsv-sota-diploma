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
  selector: 'app-name-editor',
  templateUrl: './name-editor.component.html',
  styleUrls: ['./name-editor.component.css']
})
export class NameEditorComponent extends BaseComponent implements OnInit {

  readonly name = new FormControl('', [Validators.required, Validators.maxLength(100)]);

  constructor(public override readonly translate: TranslateService) {
    super(translate);
  }

  ngOnInit(): void {
    this.target.addControl("name", this.name);
  }

  getErrorMessage(): string {
    if (this.name.hasError('required')) {
      return this.translate.instant('input.name.error.required');
    }
    if (this.name.hasError('maxLength')) {
      return this.translate.instant('input.name.error.maxLength');
    }
    return '';
  }
}
