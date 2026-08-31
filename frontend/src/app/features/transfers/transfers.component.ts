import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Transfer } from '../../core/models/transfer.model';
import { TransferService } from '../../core/services/transfer.service';
import { TRANSFER_STATUS_LABEL } from '../../core/helpers/enum-labels';

@Component({
  selector: 'app-transfers',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './transfers.component.html',
  styleUrl: './transfers.component.scss',
})
export class TransfersComponent implements OnInit {
  private readonly transferService = inject(TransferService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly activePanel = signal<'transfer' | 'history'>('transfer');
  readonly transfers = signal<Transfer[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  readonly transferForm = this.fb.nonNullable.group({
    sourceAccountNumber: ['', [Validators.required, Validators.minLength(1)]],
    targetAccountNumber: ['', [Validators.required, Validators.minLength(1)]],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    description: [''],
  });

  readonly historyForm = this.fb.nonNullable.group({
    accountNumber: ['', Validators.required],
  });

  ngOnInit(): void {}

  setPanel(panel: 'transfer' | 'history'): void {
    this.activePanel.set(panel);
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  submitTransfer(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const value = this.transferForm.getRawValue();
    const idempotencyKey = crypto.randomUUID();

    this.transferService
      .create(
        {
          sourceAccountNumber: value.sourceAccountNumber.trim(),
          targetAccountNumber: value.targetAccountNumber.trim(),
          amount: value.amount,
          description: value.description.trim() || undefined,
        },
        idempotencyKey
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.saving.set(false);
          this.successMessage.set(
            `Transferencia completada. ID: ${result.id}`
          );
          this.transferForm.reset({ sourceAccountNumber: '', targetAccountNumber: '', amount: 0, description: '' });
        },
        error: (err) => {
          this.saving.set(false);
          this.errorMessage.set(this.readApiError(err, 'No se pudo realizar la transferencia.'));
        },
      });
  }

  loadHistory(): void {
    if (this.historyForm.invalid) {
      this.historyForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');
    this.transfers.set([]);

    const accountNumber = this.historyForm.controls.accountNumber.value.trim();

    this.transferService
      .listByAccount(accountNumber)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.transfers.set(list);
          this.loading.set(false);
          if (list.length === 0) {
            this.successMessage.set('No hay transferencias para esta cuenta.');
          }
        },
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(this.readApiError(err, 'No se pudo cargar el historial.'));
        },
      });
  }

  statusLabel(status: string): string {
    return TRANSFER_STATUS_LABEL[status] ?? status;
  }

  directionLabel(transfer: Transfer, accountNumber: string): string {
    if (transfer.sourceAccountNumber === accountNumber) return 'Enviada';
    if (transfer.targetAccountNumber === accountNumber) return 'Recibida';
    return '—';
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
}
