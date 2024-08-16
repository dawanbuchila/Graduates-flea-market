import request from '@/utils/request'

// 查询静态信息
export function getStaticData() {
  return request({
    url: '/admin/static/data',
    method: 'get',
  })
}


// 获取日期添加用户数据
export function getDateUser(start,end) {
    return request({
      url: '/admin/static/dateUser/'+start+'/'+end,
      method: 'get',
    })
  }