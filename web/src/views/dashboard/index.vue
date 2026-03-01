<template>
  <div class="dashboard-container" :class="{ 'dark-mode': isDark }">
    <!-- 面包屑导航 -->
    <el-breadcrumb class="dashboard-breadcrumb" separator=">">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>仪表盘</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 数据概览卡片 -->
    <el-row :gutter="16" class="data-overview">
      <el-col :xs="24" :sm="12" :md="8" :lg="4" v-for="(item, index) in overviewData" :key="index">
        <div class="overview-card">
          <div class="card-icon" :style="{ backgroundColor: item.bgColor }">
            <el-icon :size="24" color="#fff">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="card-content">
            <div class="card-value">{{ item.value }}</div>
            <div class="card-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-section">
      <!-- 趋势图 -->
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">访问量趋势</span>
            <el-radio-group v-model="trendPeriod" size="small">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 柱状图 -->
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">月度数据对比</span>
            <el-tag type="info" size="small">1-6月</el-tag>
          </div>
          <div ref="barChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 日历和订单区域 -->
    <el-row :gutter="16" class="info-section">
      <!-- 工作日程日历 -->
      <el-col :xs="24" :lg="8">
        <div class="info-card">
          <div class="card-header">
            <span class="card-title">工作日程</span>
          </div>
          <el-calendar v-model="calendarDate" class="dashboard-calendar">
            <template #date-cell="{ data }">
              <div class="calendar-cell" :class="{ 'is-selected': isSelectedDate(data.day) }">
                {{ data.day.split('-')[2] }}
              </div>
            </template>
          </el-calendar>
        </div>
      </el-col>

      <!-- 订单管理表格 -->
      <el-col :xs="24" :lg="16">
        <div class="info-card">
          <div class="card-header">
            <span class="card-title">订单管理</span>
            <el-button type="primary" size="small" @click="handleAddOrder">
              <el-icon><Plus /></el-icon>新增订单
            </el-button>
          </div>
          <el-table :data="orderList" stripe class="order-table">
            <el-table-column prop="orderNo" label="订单编号" width="160" />
            <el-table-column prop="publisher" label="发布人" width="120" />
            <el-table-column prop="name" label="名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button type="danger" link size="small" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 资讯信息列表 -->
    <el-row :gutter="16" class="news-section">
      <el-col :span="24">
        <div class="info-card">
          <div class="card-header">
            <span class="card-title">资讯信息</span>
            <el-button type="primary" link @click="viewMoreNews">查看更多</el-button>
          </div>
          <el-list class="news-list">
            <div v-for="(news, index) in newsList" :key="index" class="news-item">
              <div class="news-dot"></div>
              <div class="news-title" @click="viewNewsDetail(news)">{{ news.title }}</div>
              <div class="news-date">{{ news.date }}</div>
            </div>
          </el-list>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
/**
 * 仪表盘页面
 * 包含数据概览、图表、日历、订单表格、资讯列表等模块
 */
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/theme'

// ==================== 主题 ====================

const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)

// ==================== 数据概览 ====================

const overviewData = [
  { icon: 'UserFilled', value: '1,234', label: '用户总数', bgColor: '#1677ff' },
  { icon: 'View', value: '5,678', label: '今日访问', bgColor: '#13c2c2' },
  { icon: 'ShoppingCart', value: '890', label: '订单数量', bgColor: '#ff4d4f' },
  { icon: 'Money', value: '￥12.5w', label: '交易金额', bgColor: '#52c41a' },
  { icon: 'Message', value: '456', label: '消息通知', bgColor: '#fa8c16' },
  { icon: 'StarFilled', value: '789', label: '收藏数量', bgColor: '#722ed1' }
]

// ==================== 图表相关 ====================

const trendPeriod = ref('week')
const trendChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()
let trendChart: ECharts | null = null
let barChart: ECharts | null = null

/**
 * 初始化趋势图
 */
const initTrendChart = () => {
  if (!trendChartRef.value) return

  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    series: [
      {
        name: '访问量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        sampling: 'average',
        itemStyle: { color: '#1677ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 119, 255, 0.3)' },
            { offset: 1, color: 'rgba(22, 119, 255, 0.05)' }
          ])
        },
        data: [120, 232, 181, 334, 290, 430, 410]
      }
    ]
  }
  trendChart.setOption(option)
}

/**
 * 初始化柱状图
 */
const initBarChart = () => {
  if (!barChartRef.value) return

  barChart = echarts.init(barChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月'],
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    series: [
      {
        name: '销售额',
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#1677ff' },
            { offset: 1, color: '#69c0ff' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        data: [320, 452, 301, 534, 390, 630]
      },
      {
        name: '订单量',
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#52c41a' },
            { offset: 1, color: '#95de64' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        data: [220, 382, 291, 434, 290, 530]
      }
    ]
  }
  barChart.setOption(option)
}

/**
 * 窗口大小改变时重绘图表
 */
const handleResize = () => {
  trendChart?.resize()
  barChart?.resize()
}

// ==================== 日历相关 ====================

const calendarDate = ref(new Date('2022-12-02'))

const isSelectedDate = (day: string) => {
  return day === '2022-12-02'
}

// ==================== 订单表格相关 ====================

interface Order {
  orderNo: string
  publisher: string
  name: string
  status: string
}

const orderList = ref<Order[]>([
  { orderNo: 'ORD20221201001', publisher: '张三', name: '企业官网设计项目', status: '进行中' },
  { orderNo: 'ORD20221201002', publisher: '李四', name: '电商平台开发', status: '已完成' },
  { orderNo: 'ORD20221201003', publisher: '王五', name: '移动端APP设计', status: '待处理' },
  { orderNo: 'ORD20221201004', publisher: '赵六', name: '后台管理系统开发', status: '进行中' },
  { orderNo: 'ORD20221201005', publisher: '钱七', name: '数据可视化大屏', status: '已完成' }
])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(50)

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    '进行中': 'primary',
    '已完成': 'success',
    '待处理': 'warning',
    '已取消': 'danger'
  }
  return map[status] || 'info'
}

const handleAddOrder = () => {
  ElMessage.info('新增订单功能')
}

const handleEdit = (row: Order) => {
  ElMessage.info(`编辑订单: ${row.orderNo}`)
}

const handleDelete = async (row: Order) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    // 取消删除
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// ==================== 资讯列表相关 ====================

interface News {
  title: string
  date: string
}

const newsList = ref<News[]>([
  { title: 'Element Plus 2.0 版本正式发布，带来全新体验', date: '2022-12-02' },
  { title: 'Vue 3.3 新特性解析：性能提升与开发体验优化', date: '2022-12-01' },
  { title: 'TypeScript 5.0 发布：装饰器、const 类型参数等新特性', date: '2022-11-30' },
  { title: '2023年前端技术趋势展望', date: '2022-11-29' },
  { title: 'ECharts 5.4 版本更新：更强大的数据可视化能力', date: '2022-11-28' }
])

const viewMoreNews = () => {
  ElMessage.info('查看更多资讯')
}

const viewNewsDetail = (news: News) => {
  ElMessage.info(`查看资讯: ${news.title}`)
}

// ==================== 生命周期 ====================

/**
 * 更新图表主题
 */
const updateChartTheme = () => {
  const theme = isDark.value ? 'dark' : 'light'

  // 重新初始化图表以应用主题
  if (trendChart) {
    trendChart.dispose()
    initTrendChart()
  }
  if (barChart) {
    barChart.dispose()
    initBarChart()
  }
}

/**
 * 监听主题变化，更新图表
 */
watch(isDark, () => {
  nextTick(() => {
    updateChartTheme()
  })
})

onMounted(() => {
  nextTick(() => {
    initTrendChart()
    initBarChart()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding-bottom: 20px;
}

.dashboard-breadcrumb {
  margin-bottom: 20px;
  padding: 12px 16px;
  background-color: #fff;
  border-radius: 8px;
}

// 数据概览卡片
.data-overview {
  margin-bottom: 16px;

  .overview-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: transform 0.3s, box-shadow 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .card-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 56px;
      height: 56px;
      border-radius: 8px;
      margin-right: 16px;
    }

    .card-content {
      flex: 1;

      .card-value {
        font-size: 24px;
        font-weight: 600;
        color: #262626;
        line-height: 1.2;
      }

      .card-label {
        font-size: 14px;
        color: #8c8c8c;
        margin-top: 4px;
      }
    }
  }
}

// 图表区域
.chart-section {
  margin-bottom: 16px;

  .chart-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
    }

    .chart-container {
      height: 300px;
    }
  }
}

// 信息区域
.info-section {
  margin-bottom: 16px;

  .info-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
    }

    // 日历样式
    .dashboard-calendar {
      :deep(.el-calendar__header) {
        padding: 12px 0;
      }

      :deep(.el-calendar__body) {
        padding: 12px 0 0;
      }

      :deep(.el-calendar-table) {
        th {
          color: #595959;
          font-weight: 500;
        }

        td {
          border: none;
        }
      }

      .calendar-cell {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 36px;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background-color: #f5f5f5;
        }

        &.is-selected {
          background-color: #1677ff;
          color: #fff;
        }
      }
    }

    // 表格样式
    .order-table {
      margin-bottom: 16px;
    }

    .table-pagination {
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 资讯区域
.news-section {
  .info-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
    }

    .news-list {
      .news-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .news-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background-color: #1677ff;
          margin-right: 12px;
          flex-shrink: 0;
        }

        .news-title {
          flex: 1;
          font-size: 14px;
          color: #262626;
          cursor: pointer;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;

          &:hover {
            color: #1677ff;
          }
        }

        .news-date {
          font-size: 13px;
          color: #8c8c8c;
          margin-left: 16px;
          flex-shrink: 0;
        }
      }
    }
  }
}

// 响应式调整
@media (max-width: 1200px) {
  .data-overview {
    .el-col {
      margin-bottom: 16px;
    }
  }
}

// 暗黑模式样式
.dark-mode {
  .dashboard-breadcrumb {
    background-color: #1f1f1f;
    border-color: #333;

    :deep(.el-breadcrumb__item) {
      .el-breadcrumb__inner {
        color: #a6a6a6;
      }

      &:last-child .el-breadcrumb__inner {
        color: #e0e0e0;
      }
    }
  }

  .overview-card {
    background-color: #1f1f1f;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);

    .card-content {
      .card-value {
        color: #e0e0e0;
      }

      .card-label {
        color: #a6a6a6;
      }
    }
  }

  .chart-card,
  .info-card {
    background-color: #1f1f1f;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);

    .card-header {
      .card-title {
        color: #e0e0e0;
      }
    }
  }

  .info-section {
    .dashboard-calendar {
      :deep(.el-calendar__header) {
        border-bottom-color: #333;
      }

      :deep(.el-calendar-table) {
        th {
          color: #a6a6a6;
          border-bottom-color: #333;
        }

        td {
          border-bottom-color: #333;
          color: #e0e0e0;
        }

        .el-calendar-day {
          color: #e0e0e0;
        }
      }

      .calendar-cell {
        &:hover {
          background-color: #333;
        }
      }
    }

    :deep(.el-table) {
      background-color: transparent;
      color: #e0e0e0;

      th.el-table__cell {
        background-color: #2a2a2a;
        color: #e0e0e0;
      }

      td.el-table__cell {
        background-color: transparent;
        border-bottom-color: #333;
      }

      tr {
        background-color: transparent;
      }

      .el-table__body tr:hover > td.el-table__cell {
        background-color: #2a2a2a;
      }
    }
  }

  .news-section {
    .info-card {
      .news-list {
        .news-item {
          border-bottom-color: #333;

          .news-title {
            color: #e0e0e0;

            &:hover {
              color: #1677ff;
            }
          }

          .news-date {
            color: #a6a6a6;
          }
        }
      }
    }
  }
}
</style>
