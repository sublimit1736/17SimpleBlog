import { createContext, useContext } from 'react';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

export interface ToastContextValue {
  showToast: (message: string, type?: Toast['type']) => void;
}

export const ToastContext = createContext<ToastContextValue>({ showToast: () => {} });

export const useToast = () => useContext(ToastContext);
