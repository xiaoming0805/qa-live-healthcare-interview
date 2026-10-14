/**
 * 极简 HTTP 封装：统一 baseURL、统一拆解后端 { code, message, data } 包装。
 * 开发环境通过 vite 的 server.proxy 把 /api 转发到 qa-service-user (8080)。
 */

const BASE_URL = '/api';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export class ApiError extends Error {
  readonly code: number;

  constructor(message: string, code: number) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
  }
}

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  let body: ApiResponse<T> | null = null;
  try {
    body = (await response.json()) as ApiResponse<T>;
  } catch {
    // 响应体为空或非 JSON
  }

  if (!response.ok) {
    throw new ApiError(body?.message ?? `请求失败 (HTTP ${response.status})`, body?.code ?? response.status);
  }
  if (!body) {
    throw new ApiError('响应体为空', -1);
  }
  if (body.code !== 0) {
    throw new ApiError(body.message, body.code);
  }
  return body.data;
}
