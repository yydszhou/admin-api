#!/usr/bin/env node

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from '@modelcontextprotocol/sdk/types.js';
import axios from 'axios';

const POSTMAN_API_BASE = 'https://api.getpostman.com';
const API_KEY = process.env.POSTMAN_API_KEY;
const DEFAULT_WORKSPACE_ID = process.env.POSTMAN_WORKSPACE_ID;

if (!API_KEY) {
  console.error('Error: POSTMAN_API_KEY environment variable is required');
  process.exit(1);
}

const postmanClient = axios.create({
  baseURL: POSTMAN_API_BASE,
  headers: {
    'X-Api-Key': API_KEY,
    'Content-Type': 'application/json',
  },
});

const server = new Server(
  {
    name: 'postman-mcp-server',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: 'list_workspaces',
        description: '获取所有Postman工作区列表',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
      {
        name: 'list_collections',
        description: '获取指定工作区的所有集合',
        inputSchema: {
          type: 'object',
          properties: {
            workspaceId: {
              type: 'string',
              description: '工作区ID',
            },
          },
          required: ['workspaceId'],
        },
      },
      {
        name: 'create_collection',
        description: '创建新的Postman集合',
        inputSchema: {
          type: 'object',
          properties: {
            workspaceId: {
              type: 'string',
              description: '工作区ID（可选，默认使用配置的WORKSPACE_ID）',
            },
            name: {
              type: 'string',
              description: '集合名称',
            },
            description: {
              type: 'string',
              description: '集合描述',
            },
          },
          required: ['name'],
        },
      },
      {
        name: 'create_request',
        description: '在集合中创建新的请求',
        inputSchema: {
          type: 'object',
          properties: {
            collectionId: {
              type: 'string',
              description: '集合ID',
            },
            name: {
              type: 'string',
              description: '请求名称',
            },
            method: {
              type: 'string',
              description: 'HTTP方法 (GET, POST, PUT, DELETE等)',
              enum: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'],
            },
            url: {
              type: 'string',
              description: '请求URL',
            },
            headers: {
              type: 'array',
              description: '请求头列表',
              items: {
                type: 'object',
                properties: {
                  key: { type: 'string' },
                  value: { type: 'string' },
                },
              },
            },
            body: {
              type: 'object',
              description: '请求体',
              properties: {
                mode: {
                  type: 'string',
                  enum: ['raw', 'urlencoded', 'formdata'],
                },
                raw: { type: 'string' },
              },
            },
          },
          required: ['collectionId', 'name', 'method', 'url'],
        },
      },
      {
        name: 'update_request',
        description: '更新现有请求',
        inputSchema: {
          type: 'object',
          properties: {
            collectionId: {
              type: 'string',
              description: '集合ID',
            },
            requestId: {
              type: 'string',
              description: '请求ID',
            },
            name: {
              type: 'string',
              description: '请求名称',
            },
            method: {
              type: 'string',
              description: 'HTTP方法',
            },
            url: {
              type: 'string',
              description: '请求URL',
            },
            headers: {
              type: 'array',
              description: '请求头列表',
            },
            body: {
              type: 'object',
              description: '请求体',
            },
          },
          required: ['collectionId', 'requestId'],
        },
      },
      {
        name: 'delete_request',
        description: '删除请求',
        inputSchema: {
          type: 'object',
          properties: {
            collectionId: {
              type: 'string',
              description: '集合ID',
            },
            requestId: {
              type: 'string',
              description: '请求ID',
            },
          },
          required: ['collectionId', 'requestId'],
        },
      },
      {
        name: 'run_collection',
        description: '使用Newman运行集合并返回结果',
        inputSchema: {
          type: 'object',
          properties: {
            collectionId: {
              type: 'string',
              description: '集合ID或集合JSON的URL',
            },
            environmentId: {
              type: 'string',
              description: '环境ID（可选）',
            },
          },
          required: ['collectionId'],
        },
      },
      {
        name: 'get_collection',
        description: '获取集合详情',
        inputSchema: {
          type: 'object',
          properties: {
            collectionId: {
              type: 'string',
              description: '集合ID',
            },
          },
          required: ['collectionId'],
        },
      },
    ],
  };
});

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    switch (name) {
      case 'list_workspaces': {
        const response = await postmanClient.get('/workspaces');
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(response.data.workspaces, null, 2),
            },
          ],
        };
      }

      case 'list_collections': {
        const { workspaceId } = args;
        const response = await postmanClient.get(`/workspaces/${workspaceId}/collections`);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(response.data.collections, null, 2),
            },
          ],
        };
      }

      case 'create_collection': {
        const { workspaceId, name, description } = args;
        const targetWorkspaceId = workspaceId || DEFAULT_WORKSPACE_ID;
        
        if (!targetWorkspaceId) {
          throw new Error('未提供workspaceId，且未配置POSTMAN_WORKSPACE_ID环境变量');
        }
        
        const collection = {
          collection: {
            info: {
              name,
              description: description || '',
              schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
            },
            item: [],
          },
        };
        const response = await postmanClient.post(`/collections?workspace=${targetWorkspaceId}`, collection);
        return {
          content: [
            {
              type: 'text',
              text: `集合创建成功！\nID: ${response.data.collection.id}\n名称: ${response.data.collection.name}\nURL: ${response.data.collection.url}`,
            },
          ],
        };
      }

      case 'create_request': {
        const { collectionId, name, method, url, headers, body } = args;
        
        // 先获取现有集合
        const getResponse = await postmanClient.get(`/collections/${collectionId}`);
        const collection = getResponse.data.collection;
        
        // 创建新请求
        const newRequest = {
          name,
          request: {
            method,
            header: headers || [],
            url: {
              raw: url,
              host: [url.replace(/^https?:\/\//, '').split('/')[0]],
              path: url.split('/').slice(3),
            },
          },
        };
        
        if (body) {
          newRequest.request.body = body;
        }
        
        // 添加到集合
        collection.item.push(newRequest);
        
        // 更新集合
        const updateResponse = await postmanClient.put(`/collections/${collectionId}`, {
          collection: {
            info: collection.info,
            item: collection.item,
          },
        });
        
        return {
          content: [
            {
              type: 'text',
              text: `请求创建成功！\n集合: ${collection.name}\n请求: ${name}\n方法: ${method}\nURL: ${url}`,
            },
          ],
        };
      }

      case 'update_request': {
        const { collectionId, requestId, name, method, url, headers, body } = args;
        
        const getResponse = await postmanClient.get(`/collections/${collectionId}`);
        const collection = getResponse.data.collection;
        
        // 找到并更新请求
        const requestIndex = collection.item.findIndex(item => item.id === requestId);
        if (requestIndex === -1) {
          throw new Error(`请求 ${requestId} 未找到`);
        }
        
        if (name) collection.item[requestIndex].name = name;
        if (method) collection.item[requestIndex].request.method = method;
        if (url) collection.item[requestIndex].request.url = { raw: url, host: [url.replace(/^https?:\/\//, '').split('/')[0]], path: url.split('/').slice(3) };
        if (headers) collection.item[requestIndex].request.header = headers;
        if (body) collection.item[requestIndex].request.body = body;
        
        await postmanClient.put(`/collections/${collectionId}`, {
          collection: {
            info: collection.info,
            item: collection.item,
          },
        });
        
        return {
          content: [
            {
              type: 'text',
              text: `请求更新成功！`,
            },
          ],
        };
      }

      case 'delete_request': {
        const { collectionId, requestId } = args;
        
        const getResponse = await postmanClient.get(`/collections/${collectionId}`);
        const collection = getResponse.data.collection;
        
        collection.item = collection.item.filter(item => item.id !== requestId);
        
        await postmanClient.put(`/collections/${collectionId}`, {
          collection: {
            info: collection.info,
            item: collection.item,
          },
        });
        
        return {
          content: [
            {
              type: 'text',
              text: `请求删除成功！`,
            },
          ],
        };
      }

      case 'get_collection': {
        const { collectionId } = args;
        const response = await postmanClient.get(`/collections/${collectionId}`);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(response.data.collection, null, 2),
            },
          ],
        };
      }

      case 'run_collection': {
        const { collectionId } = args;
        // 获取集合的JSON URL
        const response = await postmanClient.get(`/collections/${collectionId}`);
        const collectionUrl = response.data.collection.url;
        
        return {
          content: [
            {
              type: 'text',
              text: `集合已准备好运行。使用以下命令执行：\n\nnpx newman run "${collectionUrl}?apikey=${API_KEY}"\n\n或者安装newman后运行：\nnewman run "${collectionUrl}?apikey=${API_KEY}"`,
            },
          ],
        };
      }

      default:
        throw new Error(`未知工具: ${name}`);
    }
  } catch (error) {
    return {
      content: [
        {
          type: 'text',
          text: `错误: ${error.message}\n${error.response?.data ? JSON.stringify(error.response.data, null, 2) : ''}`,
        },
      ],
      isError: true,
    };
  }
});

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('Postman MCP Server running on stdio');
}

main().catch((error) => {
  console.error('Server error:', error);
  process.exit(1);
});
