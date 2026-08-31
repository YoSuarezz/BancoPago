import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Transfer, CreateTransferPayload, CreateTransferResponse } from '../models/transfer.model';
import { ApiResponse } from '../models/api-response.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TransferService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/transfers`;

  create(payload: CreateTransferPayload, idempotencyKey: string): Observable<CreateTransferResponse> {
    return this.http
      .post<ApiResponse<CreateTransferResponse>>(this.baseUrl, payload, {
        headers: { 'Idempotency-Key': idempotencyKey },
      })
      .pipe(map((response) => response.data[0]));
  }

  getById(id: string): Observable<Transfer> {
    return this.http
      .get<ApiResponse<Transfer>>(`${this.baseUrl}/${id}`)
      .pipe(map((response) => response.data[0]));
  }

  listByAccount(accountNumber: string): Observable<Transfer[]> {
    return this.http
      .get<ApiResponse<Transfer[]>>(this.baseUrl, { params: { accountNumber } })
      .pipe(map((response) => response.data[0] ?? []));
  }
}
