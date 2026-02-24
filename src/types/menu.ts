/**
 * 菜单相关类型定义
 */

/**
 * 菜单项类型
 */
export type MenuType = 'menu' | 'submenu' | 'button'

/**
 * 菜单项接口
 */
export interface MenuItem {
  /** 菜单ID */
  id: string
  /** 菜单名称 */
  name: string
  /** 菜单路径 */
  path: string
  /** 菜单图标 */
  icon: string
  /** 菜单类型 */
  type: MenuType
  /** 排序号 */
  sort: number
  /** 子菜单 */
  children?: MenuItem[]
  /** 是否隐藏 */
  hidden?: boolean
  /** 权限标识 */
  permission?: string
}

/**
 * 菜单列表响应
 */
export interface MenuListResponse {
  /** 菜单列表 */
  menus: MenuItem[]
}
