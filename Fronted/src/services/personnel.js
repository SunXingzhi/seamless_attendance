import api from './api'

export const personnelService = {
        // 获取人员列表
        getPersonnelList: () => {
                console.log('getPersonnelList')
                return api.get('/personnel/users/all')
        },

        // 获取人员详情
        getPersonnelById: (id) => {
                return api.get(`/personnel/user/detail/${id}`)
        },

        // 获取人员考勤记录列表
        getPersonnelAttendance: (id) => {
                return api.get(`/personnel/users/attendanceRecords/all`)
        },

        // 查询人员考勤记录
        getPersonnelAttendanceByDate: (id, date) => {
                return api.get(`/personnel/users/attendanceRecords/date/${id}/${date}`)
        },

        // 添加人员
        addPersonnel: (data) => {
                return api.post('/personnel/user/info', data)
        },

        // 更新人员信息
        updatePersonnel: (id, data) => {
                return api.put(`/personnel/user/info/${id}`, data)
        },

        // 删除人员
        deletePersonnel: (id) => {
                return api.delete(`/personnel/user/info/${id}`)
        },

        // 更新考勤状态
        updateAttendanceStatus: (id, status) => {
                return api.patch(`/personnel/update/status/${id}`, { status })
        },

        // 获取工作室人员
        getStudioPersonnel: (studioId) => {
                return api.get(`/studio/info/${studioId}`)
        },

        // 获取所有考勤记录
        getAttendanceRecords: (params = {}) => {
                return api.get('/personnel/users/attendanceRecords/all', { params })
        },

        // 根据日期范围查询考勤记录
        getAttendanceRecordsByDateRange: (startDate, endDate) => {
                return api.get(`/users/attendanceRecords/dateRange/${startDate}/${endDate}`)
        },

        // 根据人员ID查询考勤记录
        getAttendanceRecordsByUserId: (userId) => {
                return api.get(`/users/attendanceRecords/user/${userId}`)
        },

        // 根据人员ID和日期查询考勤记录
        getAttendanceRecordsByUserAndDate: (userId, date) => {
                return api.get(`/users/attendanceRecords/user/${userId}/date/${date}`)
        }
}