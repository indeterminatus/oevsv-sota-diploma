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

import {Component, OnInit, Output} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {FormBuilder} from "@angular/forms";
import {DiplomaService} from "./diploma.service";
import {Requester} from "./api/requester";
import {SignedCandidate} from "./api/signed-candidate";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'webui';

  checked: boolean = false;

  @Output()
  checkForm = this.formBuilder.group({});

  @Output()
  candidates: SignedCandidate[] = [];

  @Output()
  requester: Requester | undefined = undefined;

  constructor(private readonly translate: TranslateService, private readonly formBuilder: FormBuilder, private dataService: DiplomaService) {
    translate.addLangs(['de', 'en']);
    translate.setDefaultLang('de');
  }

  ngOnInit(): void {
  }

  onSubmit(): void {
    console.debug("SUBMITTED!", this.checkForm, this.candidates);

    const requester: Requester = {
      callSign: this.checkForm.get<string>('callSign')?.value,
      name: this.checkForm.get<string>('name')?.value,
      mail: this.checkForm.get<string>('email')?.value
    };

    this.dataService.check(requester).then(data => {
        const filtered: SignedCandidate[] = data.filter((value) => value.candidate.rank !== 'NONE');
        this.createComponentsFor(filtered);
        this.requester = requester;
        this.checked = true;
      },
      error => {
        this.createComponentsFor([]);
        console.debug("Received error!", error);
        this.checked = true;
      });
  }

  private createComponentsFor(data: SignedCandidate[]): void {
    this.candidates = [];
    data.forEach(candidate => this.candidates.push(candidate));
  }
}
