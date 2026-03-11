import { HttpInterceptorFn } from '@angular/common/http';
import { createLinkedSignal } from '@angular/core/primitives/signals';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // console.log("Auth Interceptor: request",req);
  const token=localStorage.getItem("token");
  if(token){
    req=req.clone({
      setHeaders:{
        Authorization:`Bearer ${token}`
      }
    })
  }
  return next(req);
};
