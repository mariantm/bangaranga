import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { ConsumeSpringSecurityService } from './ConsumeSpringSecurityService';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('bangaranga');


  clicked() {
      alert('asdasd')
  }

  result = signal('');
  constructor(private service: ConsumeSpringSecurityService) { }
  ngOnInit() {
      this.service.getHome().subscribe({
        next: (data) => this.result.set(data),
        error: (err) => console.error('Error:', err)
      });
  }


}
