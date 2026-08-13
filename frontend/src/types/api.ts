export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T | null;
}

export interface ValidationError {
  field: string;
  rejectedValue: string;
  reason: string;
}
