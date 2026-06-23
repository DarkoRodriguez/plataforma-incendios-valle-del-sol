import React, { createContext, useContext, useReducer, ReactNode } from 'react';
import { UserDTO } from '../services/api';

export interface AuthState {
  user: UserDTO | null;
  token: string | null;
}

export type AuthAction =
  | { type: 'login'; payload: { user: UserDTO; token: string } }
  | { type: 'logout' }
  | { type: 'update_user'; payload: Partial<UserDTO> };

const AuthStateContext = createContext<AuthState | undefined>(undefined);
const AuthDispatchContext = createContext<React.Dispatch<AuthAction> | undefined>(undefined);

const initialState: AuthState = {
  user: null,
  token: null,
};

function reducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'login':
      return { ...state, user: action.payload.user, token: action.payload.token };
    case 'logout':
      return { ...state, user: null, token: null };
    case 'update_user':
      if (!state.user) return state;
      return { ...state, user: { ...state.user, ...action.payload } };
    default:
      return state;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(reducer, initialState);
  return (
    <AuthStateContext.Provider value={state}>
      <AuthDispatchContext.Provider value={dispatch}>
        {children}
      </AuthDispatchContext.Provider>
    </AuthStateContext.Provider>
  );
}

export function useAuthState() {
  const context = useContext(AuthStateContext);
  if (context === undefined) {
    throw new Error('useAuthState must be used within an AuthProvider');
  }
  return context;
}

export function useAuthDispatch() {
  const context = useContext(AuthDispatchContext);
  if (context === undefined) {
    throw new Error('useAuthDispatch must be used within an AuthProvider');
  }
  return context;
}
