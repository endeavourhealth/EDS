import { api } from './apiClient.js'

export async function getFolderContents(folderUuid) {
  const response = await api.get('/library/getFolderContents', { params: { folderUuid } })
  return response.data
}

export async function getLibraryItem(uuid) {
  const response = await api.get('/library/getLibraryItem', { params: { uuid } })
  return response.data
}

export async function saveLibraryItem(libraryItem) {
  const response = await api.post('/library/saveLibraryItem', libraryItem)
  return response.data
}

export async function deleteLibraryItem(libraryItem) {
  const response = await api.post('/library/deleteLibraryItem', libraryItem)
  return response.data
}

export async function getSystems() {
  const response = await api.get('/library/getSystems')
  return response.data
}

export async function getSystemList() {
  const response = await api.get('/library/getSystemList')
  return response.data
}

export async function deleteSystem(uuid) {
  const response = await api.delete('/library/deleteSystem', { params: { uuid } })
  return response.data
}

export async function getQueries() {
  const response = await api.get('/library/getQueries')
  return response.data
}

export async function getDataSets() {
  const response = await api.get('/library/dataSets')
  return response.data
}

export async function getContentNamesForReportLibraryItem(uuid) {
  const response = await api.get('/library/getContentNamesForReportLibraryItem', { params: { uuid } })
  return response.data
}

export async function moveLibraryItems(itemUuids, targetFolderUuid) {
  const response = await api.post('/library/moveLibraryItems', {
    itemUuids,
    targetFolderUuid
  })
  return response.data
}
