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

import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CallSignEditorComponent} from './call-sign-editor/call-sign-editor.component';
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {LanguageSwitchComponent} from './language-switch/language-switch.component';
import {MatSelectModule} from "@angular/material/select";
import {MatInputModule} from "@angular/material/input";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {EmailEditorComponent} from './email-editor/email-editor.component';
import {NameEditorComponent} from './name-editor/name-editor.component';
import {MatToolbarModule} from "@angular/material/toolbar";
import {CandidateDisplayComponent} from './candidate-display/candidate-display.component';
import {MatButtonModule} from "@angular/material/button";
import {BaseComponent} from './base-component/base.component';
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatCardModule} from "@angular/material/card";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatIconModule} from "@angular/material/icon";
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {SpinnerComponent} from './spinner/spinner.component';
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatDividerModule} from "@angular/material/divider";
import { BackButtonDirective } from './directives/back-button.directive';
import { Oe20sotaCandidateDisplayComponent } from './oe20sota-candidate-display/oe20sota-candidate-display.component';

export function httpTranslateLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http);
}

@NgModule({
  declarations: [
    AppComponent,
    CallSignEditorComponent,
    LanguageSwitchComponent,
    EmailEditorComponent,
    NameEditorComponent,
    CandidateDisplayComponent,
    BaseComponent,
    SpinnerComponent,
    BackButtonDirective,
    Oe20sotaCandidateDisplayComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: httpTranslateLoaderFactory,
        deps: [HttpClient]
      }
    }),
    MatSelectModule,
    MatInputModule,
    MatToolbarModule,
    MatButtonModule,
    FormsModule,
    ReactiveFormsModule,
    FlexLayoutModule,
    MatCheckboxModule,
    MatCardModule,
    MatProgressBarModule,
    MatIconModule,
    FontAwesomeModule,
    MatDividerModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
