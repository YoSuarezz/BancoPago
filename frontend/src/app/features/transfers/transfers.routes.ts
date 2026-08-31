import { Routes } from '@angular/router';

export const TRANSFERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./transfers.component').then((m) => m.TransfersComponent),
  },
];
