export interface Person {
  id: string;
  name: string;
  documentNumber: string;
  documentType: string;
  email: string;
  phone?: string | null;
  personType: string;
  clientNumber?: string | null;
  membershipDate?: string | null;
  position?: string | null;
  area?: string | null;
  costCenter?: string | null;
  contractType?: string | null;
}
