import request from '@/utils/request'

export function getBorrowList(params) {
  return request({
    url: '/borrow/page',
    method: 'get',
    params
  })
}

export function getMyBorrows(params) {
  return request({
    url: '/borrow/my',
    method: 'get',
    params
  })
}

export function borrowBook(bookId, borrowDays = 30) {
  const params = new URLSearchParams()
  params.append('bookId', bookId)
  params.append('borrowDays', borrowDays)
  
  return request({
    url: '/borrow',
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

export function returnBook(recordId) {
  return request({
    url: `/borrow/${recordId}/return`,
    method: 'put'
  })
}

export function getMonthlyStats() {
  return request({
    url: '/borrow/stats/monthly',
    method: 'get'
  })
}

export function getHotBooks() {
  return request({
    url: '/borrow/stats/hot',
    method: 'get'
  })
}

export function getBorrowCount() {
  return request({
    url: '/borrow/count',
    method: 'get'
  })
}
