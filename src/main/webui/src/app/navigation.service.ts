import {Injectable} from '@angular/core';
import {Location} from '@angular/common';
import {NavigationEnd, Router} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class NavigationService {

  private history: string[] = [];

  constructor(private readonly router: Router, private readonly location: Location) {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.history.push(event.urlAfterRedirects);
      }
    });
  }

  public back(): void {
    this.history.pop();
    if (this.history.length > 0) {
      this.location.back();
    } else {
      window.location.reload();
    }
  }
}
