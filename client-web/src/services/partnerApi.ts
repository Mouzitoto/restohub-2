// Используем прокси из vite.config.ts для /admin-api
const ADMIN_API_BASE_URL = '';

export interface RegisterPartnerRequest {
  email: string;
  password: string;
  confirmPassword: string;
  agreeToTerms: boolean;
}

export interface RegisterPartnerResponse {
  message: string;
  email: string;
}

export interface VerifyEmailRequest {
  email: string;
  code: string;
}

export interface VerifyEmailResponse {
  message: string;
  userId: number;
}

export interface ResendVerificationCodeRequest {
  email: string;
}

export interface ResendVerificationCodeResponse {
  message: string;
  email: string;
}

export interface TermsResponse {
  terms: string;
}

export interface ApiError {
  message: string;
  exceptionName?: string;
}

class PartnerApiService {
  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const response = await fetch(`${ADMIN_API_BASE_URL}${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    });

    if (!response.ok) {
      const errorData: ApiError = await response.json().catch(() => ({
        message: `HTTP error! status: ${response.status}`,
      }));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async getTerms(): Promise<TermsResponse> {
    return this.request<TermsResponse>('/admin-api/auth/terms');
  }

  async registerPartner(data: RegisterPartnerRequest): Promise<RegisterPartnerResponse> {
    return this.request<RegisterPartnerResponse>('/admin-api/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async verifyEmail(data: VerifyEmailRequest): Promise<VerifyEmailResponse> {
    return this.request<VerifyEmailResponse>('/admin-api/auth/verify-email', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async resendVerificationCode(
    data: ResendVerificationCodeRequest
  ): Promise<ResendVerificationCodeResponse> {
    return this.request<ResendVerificationCodeResponse>(
      '/admin-api/auth/resend-verification-code',
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    );
  }
}

export const partnerApi = new PartnerApiService();

