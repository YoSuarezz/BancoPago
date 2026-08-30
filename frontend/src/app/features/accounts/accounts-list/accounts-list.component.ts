import {
  Component,
  computed,
  DestroyRef,
  OnDestroy,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, Subscription } from 'rxjs';
import { Account, AccountBalance } from '../../../core/models/account.model';
import { Person } from '../../../core/models/person.model';
import { PageResponse } from '../../../core/models/page-response.model';
import {
  ACCOUNT_STATUS_LABEL,
  ACCOUNT_TYPE_LABEL,
  DOCUMENT_TYPE_LABEL,
  PERSON_TYPE_LABEL,
} from '../../../core/helpers/enum-labels';
import { AccountService } from '../../../core/services/account.service';
import { PersonService } from '../../../core/services/person.service';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';

const OWNER_STORAGE_KEY = 'bancopago.lastOwnerId';
const EMPTY_PAGE: PageResponse<Person> = {
  content: [],
  page: 0,
  size: 8,
  totalElements: 0,
  totalPages: 0,
};

type AccountTypeValue = 'SAVINGS' | 'CHECKING' | 'PAYROLL' | 'TREASURY' | 'SUPPLIER';
type AccountTypeFilter = 'ALL' | AccountTypeValue;
type AccountStatusValue = 'ACTIVE' | 'INACTIVE' | 'BLOCKED' | 'SEIZED' | 'CLOSED';
type AccountStatusFilter = 'ALL' | AccountStatusValue;

@Component({
  selector: 'app-accounts-list',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, PaginatorComponent],
  templateUrl: './accounts-list.component.html',
  styleUrl: './accounts-list.component.scss',
})
export class AccountsListComponent implements OnInit, OnDestroy {
  private readonly accountService = inject(AccountService);
  private readonly personService = inject(PersonService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly personSearch$ = new Subject<string>();

  readonly activePanel = signal<'people' | 'register' | 'open-account'>('people');
  readonly selectedPerson = signal<Person | null>(null);
  readonly personPage = signal<PageResponse<Person>>(EMPTY_PAGE);
  readonly accounts = signal<Account[]>([]);
  readonly liveBalances = signal<Record<string, AccountBalance>>({});
  readonly loading = signal(false);
  readonly loadingPeople = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly streamHints = signal<Record<string, string>>({});
  readonly selectedAccountTypeFilter = signal<AccountTypeFilter>('ALL');
  readonly selectedAccountStatusFilter = signal<AccountStatusFilter>('ALL');

  readonly filteredAccounts = computed(() => {
    return this.accounts().filter((account) => {
      const matchesType =
        this.selectedAccountTypeFilter() === 'ALL' || account.type === this.selectedAccountTypeFilter();
      const matchesStatus =
        this.selectedAccountStatusFilter() === 'ALL' || this.statusFor(account) === this.selectedAccountStatusFilter();
      return matchesType && matchesStatus;
    });
  });

  readonly registerForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    documentNumber: ['', Validators.required],
    documentType: ['CC', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    personType: ['CLIENT' as 'CLIENT' | 'EMPLOYEE', Validators.required],
    position: [''],
    area: [''],
  });

  readonly accountForm = this.fb.nonNullable.group({
    type: ['SAVINGS' as 'SAVINGS' | 'CHECKING' | 'PAYROLL' | 'TREASURY' | 'SUPPLIER', Validators.required],
  });

  readonly personSearchForm = this.fb.nonNullable.group({
    name: [''],
    personType: ['ALL' as 'ALL' | 'CLIENT' | 'EMPLOYEE'],
  });

  private streamSubs: Subscription[] = [];

  ngOnInit(): void {
    this.loadPersons(0);
    this.restoreLastSelectedPerson();
    this.personSearch$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadPersons(0));
  }

  ngOnDestroy(): void {
    this.clearStreams();
  }

  setPanel(panel: 'people' | 'register' | 'open-account'): void {
    this.activePanel.set(panel);
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  onSearchNameInput(): void {
    this.personSearch$.next(this.personSearchForm.controls.name.value);
  }

  onPersonTypeFilterChange(): void {
    this.loadPersons(0);
  }

  onPersonsPageChange(nextPage: number): void {
    this.loadPersons(nextPage);
  }

  selectPerson(person: Person): void {
    this.selectedPerson.set(person);
    localStorage.setItem(OWNER_STORAGE_KEY, person.id);
    this.successMessage.set(`Persona seleccionada: ${person.name}.`);
    this.loadAccountsForSelectedPerson();
  }

  loadAccountsForSelectedPerson(): void {
    const selected = this.selectedPerson();
    if (!selected) {
      this.errorMessage.set('Selecciona una persona para ver o abrir cuentas.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');
    this.clearStreams();
    this.accounts.set([]);
    this.liveBalances.set({});
    this.streamHints.set({});
    this.selectedAccountTypeFilter.set('ALL');
    this.selectedAccountStatusFilter.set('ALL');

    this.accountService
      .listByOwner(selected.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (accounts) => {
          this.accounts.set(accounts);
          this.loading.set(false);
          accounts.forEach((account) => this.subscribeBalanceStream(account.id));
          if (accounts.length === 0) {
            this.successMessage.set(
              'La persona no tiene cuentas aun. Usa "Abrir cuenta" para crear la primera.'
            );
          } else {
            this.successMessage.set('Cuentas cargadas correctamente.');
          }
        },
        error: () => {
          this.loading.set(false);
          this.errorMessage.set('No se pudieron cargar las cuentas en este momento.');
        },
      });
  }

  registerPerson(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const value = this.registerForm.getRawValue();
    if (value.personType === 'EMPLOYEE' && (!value.position.trim() || !value.area.trim())) {
      this.errorMessage.set('Para empleado debes diligenciar cargo y area.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');

    this.personService
      .create({
        name: value.name.trim(),
        documentNumber: value.documentNumber.trim(),
        documentType: value.documentType,
        email: value.email.trim(),
        phone: value.phone.trim() || undefined,
        personType: value.personType,
        position: value.personType === 'EMPLOYEE' ? value.position.trim() : undefined,
        area: value.personType === 'EMPLOYEE' ? value.area.trim() : undefined,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (person) => {
          this.saving.set(false);
          this.selectedPerson.set(person);
          localStorage.setItem(OWNER_STORAGE_KEY, person.id);
          this.successMessage.set(`Persona creada: ${person.name}.`);
          this.loadPersons(0);
          this.setPanel('open-account');
          this.loadAccountsForSelectedPerson();
        },
        error: (err) => {
          this.saving.set(false);
          this.errorMessage.set(this.readApiError(err, 'No se pudo crear la persona.'));
        },
      });
  }

  openAccount(): void {
    const selected = this.selectedPerson();
    if (!selected) {
      this.errorMessage.set('Primero selecciona una persona para abrir la cuenta.');
      this.setPanel('people');
      return;
    }
    if (this.accountForm.invalid) {
      this.accountForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');

    this.accountService
      .create({ ownerId: selected.id, type: this.accountForm.controls.type.value })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (account) => {
          this.saving.set(false);
          this.successMessage.set(`Cuenta ${account.number} creada correctamente.`);
          this.setPanel('people');
          this.loadAccountsForSelectedPerson();
        },
        error: (err) => {
          this.saving.set(false);
          this.errorMessage.set(this.readApiError(err, 'No se pudo abrir la cuenta.'));
        },
      });
  }

  balanceFor(account: Account): number {
    return this.liveBalances()[account.id]?.balance ?? account.balance;
  }

  statusFor(account: Account): string {
    return this.liveBalances()[account.id]?.status ?? account.status;
  }

  statusLabel(status: string): string {
    return ACCOUNT_STATUS_LABEL[status] ?? status;
  }

  typeLabel(type: string): string {
    return ACCOUNT_TYPE_LABEL[type] ?? type;
  }

  personTypeLabel(type: string): string {
    return PERSON_TYPE_LABEL[type] ?? type;
  }

  documentTypeLabel(type: string): string {
    return DOCUMENT_TYPE_LABEL[type] ?? type;
  }

  isLive(accountId: string): boolean {
    return !!this.liveBalances()[accountId];
  }

  setAccountTypeFilter(type: AccountTypeFilter): void {
    this.selectedAccountTypeFilter.set(type);
  }

  setAccountStatusFilter(status: AccountStatusFilter): void {
    this.selectedAccountStatusFilter.set(status);
  }

  private subscribeBalanceStream(accountId: string): void {
    const sub = this.accountService.streamBalance(accountId).subscribe({
      next: (balance) => {
        this.liveBalances.update((current) => ({ ...current, [accountId]: balance }));
        this.streamHints.update((hints) => {
          const next = { ...hints };
          delete next[accountId];
          return next;
        });
      },
      error: (err: Error) => {
        this.streamHints.update((hints) => ({
          ...hints,
          [accountId]: err.message || 'La actualizacion en vivo se desconecto.',
        }));
      },
    });
    this.streamSubs.push(sub);
  }

  private clearStreams(): void {
    this.streamSubs.forEach((sub) => sub.unsubscribe());
    this.streamSubs = [];
  }

  private readApiError(err: unknown, fallback: string): string {
    const httpErr = err as { error?: { message?: string; messages?: string[]; code?: string } };
    if (httpErr?.error?.messages?.length) {
      return httpErr.error.messages[0];
    }
    if (httpErr?.error?.message) {
      return httpErr.error.message;
    }
    return fallback;
  }

  private loadPersons(page: number): void {
    const personType = this.personSearchForm.controls.personType.value;
    this.loadingPeople.set(true);
    this.personService
      .search({
        page,
        size: this.personPage().size || 8,
        sortBy: 'name',
        sortDirection: 'ASC',
        name: this.personSearchForm.controls.name.value,
        personType: personType === 'ALL' ? undefined : personType,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.personPage.set(result);
          this.loadingPeople.set(false);
        },
        error: () => {
          this.loadingPeople.set(false);
          this.errorMessage.set('No se pudo cargar la lista de personas.');
        },
      });
  }

  private restoreLastSelectedPerson(): void {
    const savedOwnerId = localStorage.getItem(OWNER_STORAGE_KEY);
    if (!savedOwnerId) {
      return;
    }

    this.personService
      .getById(savedOwnerId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (person) => {
          this.selectedPerson.set(person);
          this.loadAccountsForSelectedPerson();
        },
        error: () => localStorage.removeItem(OWNER_STORAGE_KEY),
      });
  }
}
