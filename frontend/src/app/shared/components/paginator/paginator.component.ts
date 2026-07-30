import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-paginator',
  standalone: true,
  template: `
    <nav class="paginator" aria-label="Paginacion">
      <button type="button" class="btn" (click)="goTo(page - 1)" [disabled]="page <= 0">
        Anterior
      </button>
      <span>Pagina {{ page + 1 }} de {{ totalPages || 1 }}</span>
      <button type="button" class="btn" (click)="goTo(page + 1)" [disabled]="page + 1 >= totalPages">
        Siguiente
      </button>
    </nav>
  `,
  styles: [`
    .paginator {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 0.75rem;
      margin-top: 1rem;
      color: var(--bp-muted);
      font-size: 0.85rem;
    }
    .btn {
      border: 1px solid var(--bp-line);
      background: #fff;
      color: var(--bp-ink);
      border-radius: 0.5rem;
      padding: 0.3rem 0.7rem;
      cursor: pointer;
    }
    .btn:disabled {
      opacity: 0.45;
      cursor: not-allowed;
    }
  `],
})
export class PaginatorComponent {
  @Input({ required: true }) page = 0;
  @Input({ required: true }) totalPages = 0;
  @Output() readonly pageChange = new EventEmitter<number>();

  goTo(nextPage: number): void {
    if (nextPage < 0 || nextPage >= this.totalPages || nextPage === this.page) {
      return;
    }
    this.pageChange.emit(nextPage);
  }
}
