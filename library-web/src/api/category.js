import request from '@/utils/request'

export function getCategoryList() {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

export function getCategoryDetail(id) {
  return request({
    url: `/category/${id}`,
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: `/category/${id}`,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/category/${id}`,
    method: 'delete'
  })
}
