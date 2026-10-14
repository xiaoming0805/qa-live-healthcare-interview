import { request } from './http';
import type { Patient } from '../types';

export interface PatientRegisterPayload {
  username: string;
  password: string;
  name: string;
  /** yyyy-MM-dd */
  birthday: string;
}

/** POST /api/patients/register */
export function registerPatient(payload: PatientRegisterPayload): Promise<Patient> {
  return request<Patient>('/patients/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

/** POST /api/patients/login */
export function loginPatientApi(username: string, password: string): Promise<Patient> {
  return request<Patient>('/patients/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}
