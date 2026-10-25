import http from '../utils/http'

// CTI 管理
export const ctiApis = {
  links: '/api/cti/links',
  extensions: '/api/cti/extensions',
  profiles: '/api/cti/profiles',
  records: '/api/cti/records',
  skills: '/api/cti/skills'
}

// IVR 管理
export const ivrApis = {
  services: '/api/ivr/services',
  trunkGroups: '/api/ivr/trunk-groups',
  files: '/api/ivr/files',
  params: '/api/ivr/params',
  routes: '/api/ivr/routes'
}

export function ctiPage(resource, params) {
  return http.get(ctiApis[resource], { params })
}
export function ctiSave(resource, data) {
  return http.post(ctiApis[resource], data)
}
export function ctiDelete(resource, id) {
  return http.delete(`${ctiApis[resource]}/${id}`)
}

export function ivrPage(resource, params) {
  return http.get(ivrApis[resource], { params })
}
export function ivrSave(resource, data) {
  return http.post(ivrApis[resource], data)
}
export function ivrDelete(resource, id) {
  return http.delete(`${ivrApis[resource]}/${id}`)
}
