import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Account, AccountBalance } from '../models/account.model';
import { ApiResponse } from '../models/api-response.model';
import { environment } from '../../../environments/environment';

export interface CreateAccountPayload {
  ownerId: string;
  type: 'SAVINGS' | 'CHECKING' | 'PAYROLL' | 'TREASURY' | 'SUPPLIER';
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/accounts`;

  create(payload: CreateAccountPayload): Observable<Account> {
    return this.http
      .post<ApiResponse<Account>>(this.baseUrl, payload)
      .pipe(map((response) => response.data[0]));
  }

  listByOwner(ownerId: string): Observable<Account[]> {
    return this.http
      .get<ApiResponse<Account>>(this.baseUrl, { params: { ownerId } })
      .pipe(map((response) => response.data ?? []));
  }

  getBalance(accountId: string): Observable<AccountBalance> {
    return this.http
      .get<ApiResponse<AccountBalance>>(`${this.baseUrl}/${accountId}/balance`)
      .pipe(map((response) => response.data[0]));
  }

  /**
   * SSE stream. Must receive accountId (never ownerId/personId).
   * Event name from backend: `balance`.
   */
  streamBalance(accountId: string): Observable<AccountBalance> {
    return new Observable<AccountBalance>((subscriber) => {
      const url = `${this.baseUrl}/${accountId}/balance/stream`;
      const source = new EventSource(url);

      const onBalance = (event: MessageEvent) => {
        try {
          subscriber.next(JSON.parse(event.data) as AccountBalance);
        } catch (error) {
          subscriber.error(error);
        }
      };

      source.addEventListener('balance', onBalance as EventListener);

      source.onerror = () => {
        // EventSource does not expose response body; readyState 2 = CLOSED after fatal error
        if (source.readyState === EventSource.CLOSED) {
          subscriber.error(
            new Error(
              `SSE cerrado para accountId ${accountId}. Verifica que el id sea de una CUENTA (no de persona) y que el backend esté en ${environment.apiBaseUrl}.`
            )
          );
        }
      };

      return () => {
        source.removeEventListener('balance', onBalance as EventListener);
        source.close();
      };
    });
  }
}
