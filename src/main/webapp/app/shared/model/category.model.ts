export interface ICategory {
  id?: number;
  categoryDescription?: string;
}

export class Category implements ICategory {
  constructor(public id?: number, public categoryDescription?: string) {}
}
