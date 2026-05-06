import request from '../request'

export function getActivities(params) {
  return request.get('/api/activities', { params })
}

export function getActivityDetail(id) {
  return request.get('/api/activities/' + id)
}

export function publishActivity(data) {
  return request.post('/api/activities', data)
}

export function updateActivity(id, data) {
  return request.put('/api/activities/' + id, data)
}

export function deleteActivity(id) {
  return request.delete('/api/activities/' + id)
}

export function getMyActivities(params) {
  return request.get('/api/activities/my', { params })
}
