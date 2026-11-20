import axios, { type AxiosResponse } from 'axios'

const webAuthClient = axios.create({
    baseURL: '/',
    timeout: 5000,
    withCredentials: true,
    withXSRFToken: true,
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN'
})

const csrfPath = '/api/v1/auth/web/csrf'
const loginPath = '/api/v1/auth/web/login'
const refreshPath = '/api/v1/auth/web/refresh'
const logoutPath = '/api/v1/auth/web/logout'

let accessToken: string | null = null
let csrfInitialized = false
let refreshInFlight: Promise<string> | null = null

const isRecord = (value: unknown): value is Record<string, unknown> =>
    typeof value === 'object' && value !== null

const unwrapResponse = (response: AxiosResponse<unknown>): unknown => {
    const body = response.data
    if (!isRecord(body)) return body

    if (body.code === 'SUCCESS' || body.success === true) return body.data

    if (typeof body.code === 'string') {
        const error = new Error(
            typeof body.message === 'string' ? body.message : '认证请求失败'
        ) as Error & { code: string; status: number }
        error.code = body.code
        error.status = response.status
        throw error
    }

    return body
}

const getAccessTokenFromResponse = (response: AxiosResponse<unknown>): string => {
    const data = unwrapResponse(response)
    if (!isRecord(data) || typeof data.accessToken !== 'string') {
        throw new Error('认证响应缺少 Access Token')
    }
    return data.accessToken
}

const ensureCsrfCookie = async (): Promise<void> => {
    if (csrfInitialized) return
    unwrapResponse(await webAuthClient.get(csrfPath))
    csrfInitialized = true
}

export const getAccessToken = (): string | null => accessToken

export const setAccessToken = (token: string): void => {
    accessToken = token
}

export const clearAccessToken = (): void => {
    accessToken = null
}

export const loginWebSession = async (
    username: string,
    password: string
): Promise<void> => {
    await ensureCsrfCookie()
    accessToken = getAccessTokenFromResponse(
        await webAuthClient.post(loginPath, { username, password })
    )
}

export const refreshWebSession = (): Promise<string> => {
    if (refreshInFlight) return refreshInFlight

    let refreshRequest: Promise<string>
    refreshRequest = (async () => {
        await ensureCsrfCookie()
        const token = getAccessTokenFromResponse(await webAuthClient.post(refreshPath))
        accessToken = token
        return token
    })()
        .catch(error => {
            clearAccessToken()
            throw error
        })
        .finally(() => {
            if (refreshInFlight === refreshRequest) refreshInFlight = null
        })

    refreshInFlight = refreshRequest
    return refreshRequest
}

export const revokeCurrentSession = async (): Promise<void> => {
    try {
        await ensureCsrfCookie()
        const revoke = (token: string) => webAuthClient.post(logoutPath, null, {
            headers: { Authorization: `Bearer ${token}` }
        })

        if (!accessToken) {
            await revoke(await refreshWebSession())
            return
        }

        try {
            await revoke(accessToken)
        } catch (error) {
            if (!axios.isAxiosError(error) || error.response?.status !== 401) throw error
            await revoke(await refreshWebSession())
        }
    } finally {
        clearAccessToken()
        csrfInitialized = false
    }
}
