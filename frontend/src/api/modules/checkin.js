import request from '../request'

export function buttonCheckIn(activityId, data) {
  return request.post('/api/checkin/button/' + activityId, data)
}

export function codeCheckIn(activityId, data) {
  return request.post('/api/checkin/code/' + activityId, data)
}

export function imageCheckIn(activityId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/checkin/image/' + activityId, formData)
}

export function checkCheckInStatus(activityId) {
  return request.get('/api/checkin/check/' + activityId)
}

export function getActivityCheckIns(activityId, params) {
  return request.get('/api/checkin/activity/' + activityId, { params })
}
