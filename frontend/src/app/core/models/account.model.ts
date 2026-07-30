export interface Account {
  id: string;
  ownerId: string;
  number: string;
  type: string;
  balance: number;
  currency: string;
  status: string;
}

export interface AccountBalance {
  accountId: string;
  accountNumber: string;
  balance: number;
  currency: string;
  status: string;
}
