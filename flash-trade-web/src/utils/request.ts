import axios, {
    type AxiosInstance,
    type AxiosRequestConfig,
    type InternalAxiosRequestConfig
} from 'axios'
import {
    clearAccessToken,
    getAccessToken,
    refreshWebSession
} from '@/utils/session'

declare module 'axios' {
    interface AxiosRequestConfig<D = any> {
        _sessionRefreshRetry?: boolean
    }
}

const instance: AxiosInstance = axios.create({
    baseURL: '/',
    timeout: 5000,
    withCredentials: true,
    withXSRFToken: true,
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN'
})

const redirectToLogin = () => {
    const appBaseUrl = new URL(import.meta.env.BASE_URL, window.location.origin)
    const appBasePath = appBaseUrl.pathname
    const currentPath = window.location.pathname.startsWith(appBasePath)
        ? window.location.pathname.slice(appBasePath.length - 1)
        : '/dashboard'
    const redirect = `${currentPath}${window.location.search}${window.location.hash}`
    const loginUrl = new URL('login', appBaseUrl)

    loginUrl.searchParams.set('redirect', redirect)
    window.location.href = `${loginUrl.pathname}${loginUrl.search}`
}

const normalizeApiError = (body: unknown): unknown => {
    if (typeof body !== 'object' || body === null) return body

    if ('code' in body && typeof body.code === 'string') {
        const data = 'data' in body && typeof body.data === 'object' && body.data !== null
            ? body.data
            : {}
        return {
            ...data,
            code: body.code,
            message: 'message' in body ? body.message : undefined
        }
    }

    if ('error' in body) return body.error
    return body
}

// request 拦截
instance.interceptors.request.use(config => {
    const token = getAccessToken()
    if (token) {
        config.headers = config.headers || {}
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// response 拦截（解包 ApiResponse）
instance.interceptors.response.use(
    response => {
        const responseData = response.data
        if (responseData && typeof responseData === 'object') {
            if ('code' in responseData) {
                if (responseData.code === 'SUCCESS') {
                    return responseData.data
                }
                return Promise.reject(normalizeApiError(responseData))
            }
            if ('success' in responseData) {
                if (responseData.success) {
                    return responseData.data
                }
                return Promise.reject(normalizeApiError(responseData))
            }
        }
        return responseData
    },
    async error => {
        const isAxiosError = axios.isAxiosError(error)
        const res = isAxiosError ? error.response?.data : undefined
        if (isAxiosError && error.response?.status === 401) {
            const config = error.config as InternalAxiosRequestConfig | undefined
            const url = config?.url ?? ''
            const isWebAuthRequest = url.startsWith('/api/v1/auth/web/')
            const isLoginPage = window.location.pathname.endsWith('/login')
            if (!isWebAuthRequest && !isLoginPage) {
                if (config?._sessionRefreshRetry) {
                    clearAccessToken()
                    redirectToLogin()
                } else {
                    try {
                        const token = await refreshWebSession()
                        const safeMethods = new Set(['GET', 'HEAD', 'OPTIONS'])
                        const method = config?.method?.toUpperCase()
                        if (config && method && safeMethods.has(method)) {
                            config._sessionRefreshRetry = true
                            config.headers.Authorization = `Bearer ${token}`
                            return instance.request(config)
                        }
                    } catch {
                        clearAccessToken()
                        redirectToLogin()
                    }
                }
            }
        }
        return Promise.reject(res === undefined ? error : normalizeApiError(res))
    }
)

/**
 * 关键：重新声明导出类型
 * 告诉 TS：post / get 返回 Promise<T>
 */
type Request = {
    get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
    post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
    put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
    delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
}

const request = instance as unknown as Request

export default request
