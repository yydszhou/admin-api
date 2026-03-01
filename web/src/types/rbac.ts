export interface PageResp<T> {
  count: number
  list: T[]
}

export interface RoleSimple {
  id: number
  roleCode: string
  roleName: string
}

export interface UserItem {
  id: number
  avatar?: string
  username: string
  email: string
  roles: RoleSimple[]
  status: number
  createTime?: string
}

export interface RoleItem {
  id: number
  roleCode: string
  roleName: string
  description?: string
  userCount: number
  permissionCount: number
}

export interface PermissionItem {
  id: number
  permissionCode: string
  permissionName: string
  module: string
  description?: string
  enabled: boolean
}

export interface PermissionTree {
  module: string
  children: PermissionItem[]
}
