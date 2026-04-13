import request from '@/utils/request'

export function getBookList(params) {
  return request({
    url: '/book/page',
    method: 'get',
    params
  })
}

export function getBookDetail(id) {
  return request({
    url: `/book/${id}`,
    method: 'get'
  })
}

export function addBook(data) {
  return request({
    url: '/book',
    method: 'post',
    data
  })
}

export function updateBook(id, data) {
  return request({
    url: `/book/${id}`,
    method: 'put',
    data
  })
}

export function deleteBook(id) {
  return request({
    url: `/book/${id}`,
    method: 'delete'
  })
}

export function getCategoryStats() {
  return request({
    url: '/book/stats/category',
    method: 'get'
  })
}

export function getBookCount() {
  return request({
    url: '/book/count',
    method: 'get'
  })
}
