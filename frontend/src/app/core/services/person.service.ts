import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Person } from '../models/person.model';
import { ApiResponse } from '../models/api-response.model';
import { PageResponse } from '../models/page-response.model';
import { environment } from '../../../environments/environment';

export interface CreatePersonPayload {
  name: string;
  documentNumber: string;
  documentType: string;
  email: string;
  phone?: string;
  personType: 'CLIENT' | 'EMPLOYEE';
  position?: string;
  area?: string;
  costCenter?: string;
  contractType?: string;
}

export interface PersonSearchQuery {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  name?: string;
  personType?: 'CLIENT' | 'EMPLOYEE';
}

@Injectable({ providedIn: 'root' })
export class PersonService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/persons`;

  create(payload: CreatePersonPayload): Observable<Person> {
    return this.http
      .post<ApiResponse<Person>>(this.baseUrl, payload)
      .pipe(map((response) => response.data[0]));
  }

  getById(personId: string): Observable<Person> {
    return this.http
      .get<ApiResponse<Person>>(`${this.baseUrl}/${personId}`)
      .pipe(map((response) => response.data[0]));
  }

  listAll(): Observable<Person[]> {
    return this.http
      .get<ApiResponse<Person>>(this.baseUrl)
      .pipe(map((response) => response.data ?? []));
  }

  search(query: PersonSearchQuery): Observable<PageResponse<Person>> {
    let params = new HttpParams()
      .set('page', String(query.page ?? 0))
      .set('size', String(query.size ?? 8))
      .set('sortBy', query.sortBy ?? 'name')
      .set('sortDirection', query.sortDirection ?? 'ASC');

    if (query.name?.trim()) {
      params = params.set('name', query.name.trim());
    }
    if (query.personType) {
      params = params.set('personType', query.personType);
    }

    return this.http.get<PageResponse<Person>>(`${this.baseUrl}/search`, { params });
  }
}
