import { request } from './http';
import type { Doctor } from '../types';

/** GET /api/doctors 或 /api/doctors?active=true */
export function fetchDoctors(activeOnly = false): Promise<Doctor[]> {
  return request<Doctor[]>(`/doctors${activeOnly ? '?active=true' : ''}`);
}

/** GET /api/doctors/username/{username} */
export function fetchDoctorByUsername(username: string): Promise<Doctor> {
  return request<Doctor>(`/doctors/username/${encodeURIComponent(username)}`);
}

/** POST /api/doctors/login */
export function loginDoctorApi(username: string, password: string): Promise<Doctor> {
  return request<Doctor>('/doctors/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}
