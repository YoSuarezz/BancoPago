import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AccountService } from './account.service';
import { environment } from '../../../environments/environment';
import { Account } from '../models/account.model';

describe('AccountService', () => {
  let service: AccountService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(AccountService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should list accounts by ownerId', () => {
    const ownerId = '11111111-1111-1111-1111-111111111111';
    const accounts: Account[] = [
      {
        id: '22222222-2222-2222-2222-222222222222',
        ownerId,
        number: '5300000001',
        type: 'SAVINGS',
        balance: 0,
        currency: 'COP',
        status: 'ACTIVE',
      },
    ];

    service.listByOwner(ownerId).subscribe((result) => {
      expect(result).toHaveLength(1);
      expect(result[0].number).toBe('5300000001');
    });

    const req = httpMock.expectOne(
      `${environment.apiBaseUrl}/api/v1/accounts?ownerId=${ownerId}`
    );
    expect(req.request.method).toBe('GET');
    req.flush({ data: accounts, messages: [] });
  });

  it('should create an account', () => {
    const payload = {
      ownerId: '11111111-1111-1111-1111-111111111111',
      type: 'SAVINGS' as const,
    };
    const account: Account = {
      id: '22222222-2222-2222-2222-222222222222',
      ownerId: payload.ownerId,
      number: '5300000001',
      type: 'SAVINGS',
      balance: 0,
      currency: 'COP',
      status: 'ACTIVE',
    };

    service.create(payload).subscribe((result) => {
      expect(result.number).toBe('5300000001');
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/v1/accounts`);
    expect(req.request.method).toBe('POST');
    req.flush({ data: [account], messages: [] });
  });
});
