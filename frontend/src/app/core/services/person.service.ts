import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
    const params: Record<string, string> = {
      page: String(query.page ?? 0),
      size: String(query.size ?? 8),
      sortBy: query.sortBy ?? 'name',
      sortDirection: query.sortDirection ?? 'ASC',
    };
    if (query.name?.trim()) {
      params.name = query.name.trim();
    }
    if (query.personType) {
      params.personType = query.personType;
    }

    return this.http.get<PageResponse<Person>>(`${this.baseUrl}/search`, { params });
  }
}
