export interface Transfer {
  id: string;
  sourceAccountNumber: string;
  targetAccountNumber: string;
  amount: number;
  currency: string;
  status: string;
  description: string | null;
  createdAt: string;
}

export interface CreateTransferPayload {
  sourceAccountNumber: string;
  targetAccountNumber: string;
  amount: number;
  description?: string;
}

export interface CreateTransferResponse extends Transfer {
  idempotencyKey: string;
}
