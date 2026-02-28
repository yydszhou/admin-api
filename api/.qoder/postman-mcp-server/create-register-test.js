import axios from 'axios';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const postmanClient = axios.create({
  baseURL: 'https://api.getpostman.com',
  headers: {
    'X-Api-Key': 'PMAK-699c2cf4c7a1bc0001bdc9b0-a5c166b910b5f8786d0a89a8bde0bc84c6',
    'Content-Type': 'application/json',
  },
});

const WORKSPACE_ID = '200a4182-9cb5-4bc1-aaaf-f321a49375dc';
const COLLECTION_NAME = 'Admin API 完整测试集合';
const CONFIG_FILE = path.join(__dirname, '.collection-id');

// 读取已保存的集合ID
function getSavedCollectionId() {
  try {
    if (fs.existsSync(CONFIG_FILE)) {
      return fs.readFileSync(CONFIG_FILE, 'utf8').trim();
    }
  } catch (e) {
    console.error('读取配置失败:', e.message);
  }
  return null;
}

// 保存集合ID
function saveCollectionId(id) {
  try {
    fs.writeFileSync(CONFIG_FILE, id);
  } catch (e) {
    console.error('保存配置失败:', e.message);
  }
}

// 查找已存在的集合
async function findExistingCollection() {
  try {
    const response = await postmanClient.get(`/workspaces/${WORKSPACE_ID}/collections`);
    const collections = response.data.collections || [];
    return collections.find(c => c.name === COLLECTION_NAME);
  } catch (error) {
    console.error('查找集合失败:', error.message);
    return null;
  }
}

// 构建完整的测试集合结构
function buildCollection() {
  return {
    info: {
      name: COLLECTION_NAME,
      description: 'Admin API 完整接口测试集合 - 按模块划分',
      schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
    },
    item: [
      {
        name: '认证模块 (Auth)',
        description: '用户认证相关接口 - 注册、登录、登出等',
        item: [
          {
            name: '用户登录',
            description: '用户登录接口测试用例',
            item: [
              {
                name: '登录成功',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'testuser001',
                      password: '123456'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/login',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'login']
                  },
                  description: '正常登录流程，预期返回200和token'
                },
                response: []
              },
              {
                name: '登录失败-用户不存在',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'notexistuser',
                      password: '123456'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/login',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'login']
                  },
                  description: '用户名不存在，预期返回1003错误'
                }
              },
              {
                name: '登录失败-密码错误',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'testuser001',
                      password: 'wrongpassword'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/login',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'login']
                  },
                  description: '密码不正确，预期返回1004错误'
                }
              }
            ]
          },
          {
            name: '用户注册',
            description: '用户注册接口测试用例',
            item: [
              {
                name: '注册成功',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'testuser001',
                      email: 'test001@example.com',
                      password: '123456'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/register',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'register']
                  },
                  description: '正常注册流程，预期返回200和用户信息'
                },
                response: []
              },
              {
                name: '注册失败-用户名已存在',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'testuser001',
                      email: 'another@example.com',
                      password: '123456'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/register',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'register']
                  },
                  description: '重复用户名注册，预期返回400错误'
                }
              },
              {
                name: '注册失败-邮箱已存在',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'anotheruser',
                      email: 'test001@example.com',
                      password: '123456'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/register',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'register']
                  },
                  description: '重复邮箱注册，预期返回400错误'
                }
              },
              {
                name: '注册失败-用户名过短',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'ab',
                      email: 'short@example.com',
                      password: '123456'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/register',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'register']
                  },
                  description: '用户名长度小于3，预期返回400参数校验错误'
                }
              },
              {
                name: '注册失败-密码过短',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'validuser',
                      email: 'valid@example.com',
                      password: '123'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/register',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'register']
                  },
                  description: '密码长度小于6，预期返回400参数校验错误'
                }
              },
              {
                name: '注册失败-邮箱格式错误',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'validuser2',
                      email: 'invalid-email-format',
                      password: '123456'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/register',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'register']
                  },
                  description: '邮箱格式不正确，预期返回400参数校验错误'
                }
              },
              {
                name: '注册失败-缺少必填字段',
                request: {
                  method: 'POST',
                  header: [{ key: 'Content-Type', value: 'application/json' }],
                  body: {
                    mode: 'raw',
                    raw: JSON.stringify({
                      username: 'onlyusername'
                    }, null, 2)
                  },
                  url: {
                    raw: '{{baseUrl}}/api/users/register',
                    host: ['{{baseUrl}}'],
                    path: ['api', 'users', 'register']
                  },
                  description: '缺少email和password，预期返回400参数校验错误'
                }
              }
            ]
          }
        ]
      },
      {
        name: '用户模块 (User)',
        description: '用户管理相关接口 - 查询、更新、删除等',
        item: [
          {
            name: '占位 - 待开发',
            request: {
              method: 'GET',
              header: [],
              url: {
                raw: '{{baseUrl}}/api/users',
                host: ['{{baseUrl}}'],
                path: ['api', 'users']
              },
              description: '用户列表查询接口（待实现）'
            }
          }
        ]
      },
      {
        name: '系统模块 (System)',
        description: '系统健康检查、监控等接口',
        item: [
          {
            name: '健康检查',
            request: {
              method: 'GET',
              header: [],
              url: {
                raw: '{{baseUrl}}/actuator/health',
                host: ['{{baseUrl}}'],
                path: ['actuator', 'health']
              },
              description: 'Spring Boot Actuator健康检查端点'
            }
          }
        ]
      }
    ],
    variable: [
      {
        key: 'baseUrl',
        value: 'http://localhost:8080',
        type: 'string',
        description: 'API基础URL'
      }
    ]
  };
}

async function main() {
  try {
    let collectionId = getSavedCollectionId();
    let existingCollection = null;
    
    // 尝试查找已存在的集合
    if (!collectionId) {
      existingCollection = await findExistingCollection();
      if (existingCollection) {
        collectionId = existingCollection.id;
        console.log('找到已存在的集合:', collectionId);
      }
    }
    
    const collectionData = buildCollection();
    
    if (collectionId) {
      // 更新现有集合
      console.log('正在更新集合...');
      await postmanClient.put(`/collections/${collectionId}`, {
        collection: collectionData
      });
      console.log('✓ 集合更新成功！');
    } else {
      // 创建新集合
      console.log('正在创建新集合...');
      const response = await postmanClient.post(`/collections?workspace=${WORKSPACE_ID}`, {
        collection: collectionData
      });
      collectionId = response.data.collection.id;
      console.log('✓ 集合创建成功！');
    }
    
    // 保存集合ID
    saveCollectionId(collectionId);
    
    console.log('  集合ID:', collectionId);
    console.log('  集合名称:', COLLECTION_NAME);
    console.log('  结构:');
    console.log('    ├─ 认证模块 (Auth)');
    console.log('    │   └─ 用户注册 (7个测试用例)');
    console.log('    ├─ 用户模块 (User)');
    console.log('    │   └─ 占位 - 待开发');
    console.log('    └─ 系统模块 (System)');
    console.log('        └─ 健康检查');
    console.log('  环境变量: baseUrl = http://localhost:8080');
    
  } catch (error) {
    console.error('错误:', JSON.stringify(error.response?.data, null, 2) || error.message);
    process.exit(1);
  }
}

main();
