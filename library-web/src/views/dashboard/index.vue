<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card blue">
          <div class="stat-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <p class="stat-label">注册用户</p>
            <p class="stat-value">{{ stats.userCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card green">
          <div class="stat-icon">
            <i class="el-icon-reading"></i>
          </div>
          <div class="stat-info">
            <p class="stat-label">图书总数</p>
            <p class="stat-value">{{ stats.totalBookQuantity || 0 }}</p>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card orange">
          <div class="stat-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-info">
            <p class="stat-label">借阅次数</p>
            <p class="stat-value">{{ stats.totalBorrowCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card red">
          <div class="stat-icon">
            <i class="el-icon-warning"></i>
          </div>
          <div class="stat-info">
            <p class="stat-label">逾期未还</p>
            <p class="stat-value">{{ stats.overdueCount || 0 }}</p>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="24" class="chart-row">
      <!-- 借阅趋势图 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-data-line"></i> 借阅趋势统计</span>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 热门分类图 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-pie-chart"></i> 图书分类分布</span>
          </div>
          <div ref="categoryChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="24" class="chart-row">
      <!-- 热门图书 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-trophy"></i> 热门图书TOP10</span>
          </div>
          <div ref="hotBookChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 用户增长 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span><i class="el-icon-s-custom"></i> 用户增长趋势</span>
          </div>
          <div ref="userGrowthChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/statistics'
import { getMonthlyTrend } from '@/api/statistics'
import { getHotCategories } from '@/api/statistics'
import { getHotBooks } from '@/api/statistics'
import { getUserGrowth } from '@/api/statistics'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {},
      trendChart: null,
      categoryChart: null,
      hotBookChart: null,
      userGrowthChart: null
    }
  },
  mounted() {
    this.initCharts()
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    this.disposeCharts()
  },
  methods: {
    initCharts() {
      this.trendChart = echarts.init(this.$refs.trendChart)
      this.categoryChart = echarts.init(this.$refs.categoryChart)
      this.hotBookChart = echarts.init(this.$refs.hotBookChart)
      this.userGrowthChart = echarts.init(this.$refs.userGrowthChart)
    },
    
    disposeCharts() {
      this.trendChart && this.trendChart.dispose()
      this.categoryChart && this.categoryChart.dispose()
      this.hotBookChart && this.hotBookChart.dispose()
      this.userGrowthChart && this.userGrowthChart.dispose()
    },
    
    handleResize() {
      this.trendChart && this.trendChart.resize()
      this.categoryChart && this.categoryChart.resize()
      this.hotBookChart && this.hotBookChart.resize()
      this.userGrowthChart && this.userGrowthChart.resize()
    },
    
    async loadData() {
      await Promise.all([
        this.loadStats(),
        this.loadTrendData(),
        this.loadCategoryData(),
        this.loadHotBooks(),
        this.loadUserGrowth()
      ])
    },
    
    async loadStats() {
      try {
        const res = await getDashboardStats()
        if (res.code === 200) {
          this.stats = res.data
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    
    async loadTrendData() {
      try {
        const res = await getMonthlyTrend()
        if (res.code === 200) {
          const data = res.data || []
          const months = data.map(item => item.month)
          const counts = data.map(item => item.count)
          
          const option = {
            tooltip: {
              trigger: 'axis',
              backgroundColor: 'rgba(255, 255, 255, 0.9)',
              borderColor: '#E8E8E8',
              textStyle: { color: '#262626' }
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'category',
              data: months,
              axisLine: { lineStyle: { color: '#E8E8E8' } },
              axisLabel: { color: '#8C8C8C' }
            },
            yAxis: {
              type: 'value',
              axisLine: { show: false },
              axisTick: { show: false },
              splitLine: { lineStyle: { color: '#F0F0F0' } },
              axisLabel: { color: '#8C8C8C' }
            },
            series: [{
              name: '借阅量',
              type: 'line',
              smooth: true,
              symbol: 'circle',
              symbolSize: 8,
              data: counts,
              itemStyle: { color: '#5B8FF9' },
              lineStyle: { width: 3 },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(91, 143, 249, 0.3)' },
                  { offset: 1, color: 'rgba(91, 143, 249, 0.05)' }
                ])
              }
            }]
          }
          
          this.trendChart.setOption(option)
        }
      } catch (error) {
        console.error('加载趋势数据失败:', error)
      }
    },
    
    async loadCategoryData() {
      try {
        const res = await getHotCategories()
        if (res.code === 200) {
          const data = res.data || []
          const pieData = data.map(item => ({
            name: item.category_name || '未分类',
            value: item.count
          }))
          
          const option = {
            tooltip: {
              trigger: 'item',
              backgroundColor: 'rgba(255, 255, 255, 0.9)',
              borderColor: '#E8E8E8',
              textStyle: { color: '#262626' },
              formatter: '{b}: {c} ({d}%)'
            },
            legend: {
              orient: 'vertical',
              right: '5%',
              top: 'center',
              textStyle: { color: '#595959' }
            },
            series: [{
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['35%', '50%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 8,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: { show: false },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 14,
                  fontWeight: 'bold'
                }
              },
              data: pieData,
              color: ['#5B8FF9', '#5AD8A6', '#F6BD16', '#E8684A', '#6DC8EC', '#9270CA']
            }]
          }
          
          this.categoryChart.setOption(option)
        }
      } catch (error) {
        console.error('加载分类数据失败:', error)
      }
    },
    
    async loadHotBooks() {
      try {
        const res = await getHotBooks()
        if (res.code === 200) {
          const data = res.data || []
          const books = data.map(item => item.book_title).slice(0, 8)
          const counts = data.map(item => item.borrow_count).slice(0, 8)
          
          const option = {
            tooltip: {
              trigger: 'axis',
              axisPointer: { type: 'shadow' },
              backgroundColor: 'rgba(255, 255, 255, 0.9)',
              borderColor: '#E8E8E8',
              textStyle: { color: '#262626' }
            },
            grid: {
              left: '3%',
              right: '8%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'value',
              axisLine: { show: false },
              axisTick: { show: false },
              splitLine: { lineStyle: { color: '#F0F0F0' } },
              axisLabel: { color: '#8C8C8C' }
            },
            yAxis: {
              type: 'category',
              data: books.reverse(),
              axisLine: { lineStyle: { color: '#E8E8E8' } },
              axisTick: { show: false },
              axisLabel: { 
                color: '#595959',
                width: 100,
                overflow: 'truncate'
              }
            },
            series: [{
              name: '借阅次数',
              type: 'bar',
              data: counts.reverse(),
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: '#5AD8A6' },
                  { offset: 1, color: '#5B8FF9' }
                ]),
                borderRadius: [0, 4, 4, 0]
              },
              barWidth: 16
            }]
          }
          
          this.hotBookChart.setOption(option)
        }
      } catch (error) {
        console.error('加载热门图书失败:', error)
      }
    },
    
    async loadUserGrowth() {
      try {
        const res = await getUserGrowth()
        if (res.code === 200) {
          const data = res.data
          
          const option = {
            tooltip: {
              trigger: 'axis',
              backgroundColor: 'rgba(255, 255, 255, 0.9)',
              borderColor: '#E8E8E8',
              textStyle: { color: '#262626' }
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'category',
              data: data.months,
              axisLine: { lineStyle: { color: '#E8E8E8' } },
              axisLabel: { color: '#8C8C8C' }
            },
            yAxis: {
              type: 'value',
              axisLine: { show: false },
              axisTick: { show: false },
              splitLine: { lineStyle: { color: '#F0F0F0' } },
              axisLabel: { color: '#8C8C8C' }
            },
            series: [{
              name: '新增用户',
              type: 'bar',
              data: data.userCounts,
              itemStyle: {
                color: '#F6BD16',
                borderRadius: [4, 4, 0, 0]
              },
              barWidth: '60%'
            }]
          }
          
          this.userGrowthChart.setOption(option)
        }
      } catch (error) {
        console.error('加载用户增长数据失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding-bottom: 24px;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon i {
  font-size: 32px;
  color: #fff;
}

.stat-card.blue .stat-icon {
  background: linear-gradient(135deg, #5B8FF9 0%, #6F9EFD 100%);
}

.stat-card.green .stat-icon {
  background: linear-gradient(135deg, #5AD8A6 0%, #6EE2B7 100%);
}

.stat-card.orange .stat-icon {
  background: linear-gradient(135deg, #F6BD16 0%, #F8C93A 100%);
}

.stat-card.red .stat-icon {
  background: linear-gradient(135deg, #E8684A 0%, #ED8569 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #8C8C8C;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #262626;
  line-height: 1;
}

.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.chart-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #F0F0F0;
}

.chart-header {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
}

.chart-header i {
  margin-right: 8px;
  color: #5B8FF9;
}

.chart-container {
  height: 320px;
}
</style>
