import { HttpParams } from '@angular/common/http';

export interface Pagination {
  page: number;
  size: number;
  sort: string[];
}

export interface Search {
  query: string;
}

export interface SearchWithPagination extends Search, Pagination {}

export const createRequestOption = (req?: any): HttpParams => {
  let options: HttpParams = new HttpParams();

  if (req) {
    Object.keys(req).forEach(key => {
      if (key !== 'sort') {
        options = options.set(key, req[key]);
      }
    });

    if (req.sort) {
      req.sort.forEach((val: string) => {
        options = options.append('sort', val);
      });
    }
    if (req.criteria && req.criteria.length > 0) {
      req.criteria.forEach((criterion: { key: string; value: string }) => {
        options = options.append(criterion.key, criterion.value);
      });
    }
  }

  return options;
};
