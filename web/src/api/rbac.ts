import { del, get, post, put } from '@/utils/request'
import type {
  PageResp,
  PermissionTree,
  RoleItem,
  RoleSimple,
  UserItem
} from '@/types/rbac'

export const pageUsers = (params: {
  page: number
  pageSize: number
  keyword?: string
  status?: number
  roleId?: number
}) => get<PageResp<UserItem>>('/rbac/users', params as unknown as Record<string, unknown>)

export const createUser = (data: {
  username: string
  email: string
  password: string
  status: string
  roleIds: number[]
  traceId: string
}) => post<void>('/rbac/users', data)

export const updateUserRoles = (userId: number, data: { roleIds: number[]; traceId: string }) =>
  put<void>(`/rbac/users/${userId}/roles`, data)

export const getUserRoleIds = (userId: number) => get<number[]>(`/rbac/users/${userId}/roles`)

export const batchDeleteUsers = (data: { userIds: number[]; traceId: string }) =>
  post<void>('/rbac/users/batch-delete', data)

export const listRoles = () => get<RoleItem[]>('/rbac/roles')

export const roleOptions = () => get<RoleSimple[]>('/rbac/roles/options')

export const createRole = (data: {
  roleName: string
  description?: string
  permissionIds: number[]
  traceId: string
}) => post<void>('/rbac/roles', data)

export const deleteRole = (roleId: number) => del<void>(`/rbac/roles/${roleId}`)

export const getRolePermissionIds = (roleId: number) => get<number[]>(`/rbac/roles/${roleId}/permissions`)

export const updateRolePermissions = (
  roleId: number,
  data: { permissionIds: number[]; traceId: string }
) => put<void>(`/rbac/roles/${roleId}/permissions`, data)

export const permissionTree = (keyword?: string) =>
  get<PermissionTree[]>('/rbac/permissions/tree', keyword ? { keyword } : undefined)

export const updatePermissionStatus = (
  permissionId: number,
  data: { enabled: boolean; traceId: string }
) => put<void>(`/rbac/permissions/${permissionId}/status`, data)
