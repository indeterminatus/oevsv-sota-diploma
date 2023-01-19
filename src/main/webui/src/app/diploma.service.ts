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

import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Requester} from "./api/requester";
import {SignedCandidate} from "./api/signed-candidate";
import {DiplomaRequest} from "./api/diploma-request";
import {lastValueFrom} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class DiplomaService {

  constructor(private readonly http: HttpClient) {
  }

  public async check(requester: Requester): Promise<SignedCandidate[]> {
    return new Promise((resolve, reject) => {
      const request =
        this.http.get<SignedCandidate[]>("/api/diploma/candidates", {params: new HttpParams().set('callsign', requester.callSign)});

      return lastValueFrom(request)
        .then(data => {
            resolve(data);
          },
          error => {
            reject(error);
          });
    });
  }

  public async request(request: DiplomaRequest): Promise<boolean> {
    return new Promise((resolve, reject) => {
      const call = this.http.post<boolean>("/api/diploma/request", request);

      return lastValueFrom(call).then(data => {
        resolve(data);
      }, error => {
        reject(error);
      });
    });
  }
}
