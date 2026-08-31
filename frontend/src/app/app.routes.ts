import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'accounts' },
  {
    path: 'accounts',
    loadComponent: () =>
      import('./features/accounts/accounts-list/accounts-list.component').then(
        (m) => m.AccountsListComponent
      ),
  },
  {
    path: 'transfers',
    loadChildren: () =>
      import('./features/transfers/transfers.routes').then(
        (m) => m.TRANSFERS_ROUTES
      ),
  },
];
