import { Moment } from 'moment';

export interface IBook {
  id?: number;
  bookTitle?: string;
  author?: string;
  publisher?: string;
  publicationDate?: Moment;
  dataContentType?: string;
  data?: any;
  categoryId?: number;
}

export class Book implements IBook {
  constructor(
    public id?: number,
    public bookTitle?: string,
    public author?: string,
    public publisher?: string,
    public publicationDate?: Moment,
    public dataContentType?: string,
    public data?: any,
    public categoryId?: number
  ) {}
}
