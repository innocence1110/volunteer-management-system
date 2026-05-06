import request from '../request'

export function registerActivity(activityId) {
  return request.post('/api/registrations/' + activityId)
}

export function cancelRegistration(activityId) {
  return request.delete('/api/registrations/' + activityId)
}

export function checkRegistration(activityId) {
  return request.get('/api/registrations/check/' + activityId)
}

export function getMyRegistrations(params) {
  return request.get('/api/registrations/my', { params })
}

export function getActivityRegistrations(activityId, params) {
  return request.get('/api/registrations/activity/' + activityId, { params })
}
