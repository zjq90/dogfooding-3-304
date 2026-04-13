import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  })
}

export function getMonthlyTrend() {
  return request({
    url: '/statistics/monthly-trend',
    method: 'get'
  })
}

export function getHotCategories() {
  return request({
    url: '/statistics/hot-categories',
    method: 'get'
  })
}

export function getHotBooks() {
  return request({
    url: '/statistics/hot-books',
    method: 'get'
  })
}

export function getUserGrowth() {
  return request({
    url: '/statistics/user-growth',
    method: 'get'
  })
}
