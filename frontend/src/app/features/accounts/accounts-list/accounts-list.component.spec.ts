import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AccountsListComponent } from './accounts-list.component';
import { AccountService } from '../../../core/services/account.service';
import { PersonService } from '../../../core/services/person.service';
import { Account } from '../../../core/models/account.model';
import { Person } from '../../../core/models/person.model';
import { PageResponse } from '../../../core/models/page-response.model';

describe('AccountsListComponent', () => {
  let fixture: ComponentFixture<AccountsListComponent>;
  let component: AccountsListComponent;

  const accounts: Account[] = [
    {
      id: '22222222-2222-2222-2222-222222222222',
      ownerId: '11111111-1111-1111-1111-111111111111',
      number: '5300000001',
      type: 'SAVINGS',
      balance: 1500,
      currency: 'COP',
      status: 'ACTIVE',
    },
  ];

  const person: Person = {
    id: '11111111-1111-1111-1111-111111111111',
    name: 'Ana Lopez',
    documentNumber: '1002003001',
    documentType: 'CC',
    email: 'ana@example.com',
    personType: 'CLIENT',
  };

  const peoplePage: PageResponse<Person> = {
    content: [person],
    page: 0,
    size: 8,
    totalElements: 1,
    totalPages: 1,
  };

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [AccountsListComponent],
      providers: [
        {
          provide: AccountService,
          useValue: {
            listByOwner: jest.fn().mockReturnValue(of(accounts)),
            create: jest.fn(),
            streamBalance: jest.fn().mockReturnValue(
              of({
                accountId: accounts[0].id,
                accountNumber: accounts[0].number,
                balance: 1500,
                currency: 'COP',
                status: 'ACTIVE',
              })
            ),
          },
        },
        {
          provide: PersonService,
          useValue: {
            getById: jest.fn().mockReturnValue(of(person)),
            create: jest.fn(),
            search: jest.fn().mockReturnValue(of(peoplePage)),
            listAll: jest.fn().mockReturnValue(of([person])),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AccountsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render accounts after load', () => {
    component.selectPerson(person);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('5300000001');
    expect(compiled.textContent).toContain('Ana Lopez');
    expect(compiled.textContent).toContain('Ahorros');
  });
});
