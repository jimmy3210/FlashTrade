import request from '@/utils/request'

export const loadIsPlatformAdmin = async (): Promise<boolean> => {
  const session = await request.get<{ roles: string[] }>('/api/v1/auth/me')
  if (!Array.isArray(session?.roles) || session.roles.some(role => typeof role !== 'string')) {
    throw new Error('服务器返回的角色信息格式无效')
  }
  return session.roles.includes('ROLE_SUPER_ADMIN')
}
