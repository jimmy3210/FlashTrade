import request from '@/utils/request'

export interface AdminCompanyOption {
  id: number
  name: string
  isActive: boolean
}

interface CompanyPage {
  items: AdminCompanyOption[]
  total: number
}

const PAGE_SIZE = 100

export const loadAdminCompanies = async (): Promise<AdminCompanyOption[]> => {
  const companies: AdminCompanyOption[] = []
  let page = 0
  let total = 0

  do {
    const result = await request.get<CompanyPage>('/api/v1/admin/companies', {
      params: { page, size: PAGE_SIZE }
    })
    if (!result || !Array.isArray(result.items) || !Number.isSafeInteger(result.total) || result.total < 0) {
      throw new Error('服务器返回的公司列表格式无效')
    }

    total = result.total
    for (const company of result.items) {
      if (!company
          || !Number.isSafeInteger(company.id)
          || company.id <= 0
          || typeof company.name !== 'string'
          || typeof company.isActive !== 'boolean') {
        throw new Error('服务器返回的公司记录格式无效')
      }
      companies.push(company)
    }

    if (result.items.length === 0 && companies.length < total) {
      throw new Error('公司列表分页响应不完整')
    }
    page += 1
  } while (companies.length < total)

  return companies
}
