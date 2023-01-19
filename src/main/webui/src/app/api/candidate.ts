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

export interface Candidate {
  callSign: string;
  userID: string;
  category: 'ACTIVATOR' | 'CHASER' | 'S2S';
  rank: 'GOLD' | 'SILVER' | 'BRONZE' | 'NONE';
  activations: Map<State, number>;
}

export enum State {
  OE1 = 'OE1',
  OE2 = 'OE2',
  OE3 = 'OE3',
  OE4 = 'OE4',
  OE5 = 'OE5',
  OE6 = 'OE6',
  OE7 = 'OE7',
  OE8 = 'OE8',
  OE9 = 'OE9'
}
